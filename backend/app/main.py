import asyncio
import html
import os
import sqlite3
from contextlib import closing
from typing import Any

import httpx
from fastapi import FastAPI, Header, HTTPException, Request

MANAGER_TOKEN = os.getenv("MANAGER_BOT_TOKEN", "")
WEBHOOK_SECRET = os.getenv("WEBHOOK_SECRET", "")
DB_PATH = os.getenv("DATABASE_PATH", "appmarket.sqlite3")
app = FastAPI(title="AppMarket Managed Bots Backend", version="0.2.0")
_update_lock = asyncio.Lock()


def connect():
    con = sqlite3.connect(DB_PATH)
    con.row_factory = sqlite3.Row
    return con


@app.on_event("startup")
def startup():
    with closing(connect()) as con:
        con.execute("""CREATE TABLE IF NOT EXISTS managed_bots (
            bot_id INTEGER PRIMARY KEY, owner_id INTEGER NOT NULL,
            username TEXT, first_name TEXT, created_at TEXT DEFAULT CURRENT_TIMESTAMP
        )""")
        con.execute("""CREATE TABLE IF NOT EXISTS events (
            id INTEGER PRIMARY KEY AUTOINCREMENT, event_type TEXT NOT NULL,
            bot_id INTEGER, owner_id INTEGER, created_at TEXT DEFAULT CURRENT_TIMESTAMP
        )""")
        con.execute("""CREATE TABLE IF NOT EXISTS processed_updates (
            update_id INTEGER PRIMARY KEY, processed_at TEXT DEFAULT CURRENT_TIMESTAMP
        )""")
        con.commit()


async def telegram(method: str, payload: dict[str, Any]):
    if not MANAGER_TOKEN:
        raise RuntimeError("MANAGER_BOT_TOKEN is not configured")
    url = "https://api.telegram.org/bot" + MANAGER_TOKEN + "/" + method
    async with httpx.AsyncClient(timeout=20) as client:
        response = await client.post(url, json=payload)
        response.raise_for_status()
        result = response.json()
    if not result.get("ok"):
        raise RuntimeError("Telegram API request failed")
    return result.get("result")


@app.get("/health")
def health():
    return {"status": "ok", "service": "appmarket-backend", "version": "0.2.0"}


async def handle_start(update: dict[str, Any]):
    msg = update.get("message") or {}
    chat = msg.get("chat") or {}
    if (msg.get("text") or "").split(" ", 1)[0] == "/start" and chat.get("type") == "private":
        await telegram("sendMessage", {
            "chat_id": chat.get("id"),
            "text": (
                "Welcome to AppMarket! Keep this private chat open. "
                "After you create a managed bot, its token can be delivered here privately."
            ),
        })


async def handle_managed_bot(update: dict[str, Any]):
    managed = update.get("managed_bot") or {}
    owner = managed.get("user") or {}
    bot = managed.get("bot") or {}
    owner_id, bot_id = owner.get("id"), bot.get("id")
    if not isinstance(owner_id, int) or not isinstance(bot_id, int):
        raise ValueError("Managed bot update is missing owner or bot ID")

    # Telegram only sends this update to the manager bot. Fetch the child token
    # just-in-time and send it only to the owner in a private chat. Never store/log it.
    token = await telegram("getManagedBotToken", {"user_id": bot_id})
    if not isinstance(token, str) or not token:
        raise RuntimeError("Telegram returned an empty managed-bot token")

    username = bot.get("username")
    label = "@" + username if username else (bot.get("first_name") or "your bot")
    message = (
        f"Your managed bot {html.escape(label)} is ready.\n\n"
        "Here is your Bot API token. Keep it secret and never post it in a group or channel:\n"
        f"<code>{html.escape(token)}</code>\n\n"
        "If you think this token was exposed, contact support to rotate it."
    )
    # If the owner has not started this manager bot, Telegram rejects the private
    # message. We return an error so Telegram can retry; the token is not persisted.
    await telegram("sendMessage", {
        "chat_id": owner_id,
        "text": message,
        "parse_mode": "HTML",
        "protect_content": True,
    })

    with closing(connect()) as con:
        con.execute(
            "INSERT OR REPLACE INTO managed_bots (bot_id, owner_id, username, first_name) VALUES (?, ?, ?, ?)",
            (bot_id, owner_id, username, bot.get("first_name")),
        )
        con.execute(
            "INSERT INTO events (event_type, bot_id, owner_id) VALUES (?, ?, ?)",
            ("managed_bot_token_delivered", bot_id, owner_id),
        )
        con.commit()


@app.post("/telegram/webhook")
async def webhook(
    request: Request,
    x_telegram_bot_api_secret_token: str | None = Header(default=None),
):
    if not WEBHOOK_SECRET or x_telegram_bot_api_secret_token != WEBHOOK_SECRET:
        raise HTTPException(status_code=401, detail="Invalid webhook secret")

    update = await request.json()
    update_id = update.get("update_id")
    if not isinstance(update_id, int):
        raise HTTPException(status_code=400, detail="Missing update_id")

    async with _update_lock:
        with closing(connect()) as con:
            already_done = con.execute(
                "SELECT 1 FROM processed_updates WHERE update_id = ?", (update_id,)
            ).fetchone()
        if already_done:
            return {"ok": True, "duplicate": True}

        try:
            if update.get("managed_bot"):
                await handle_managed_bot(update)
            else:
                await handle_start(update)
        except Exception:
            # Do not log exception details: they may contain sensitive request data.
            # A non-2xx response asks Telegram to retry delivery.
            raise HTTPException(status_code=503, detail="Update processing failed")

        with closing(connect()) as con:
            con.execute("INSERT OR IGNORE INTO processed_updates (update_id) VALUES (?)", (update_id,))
            con.commit()

    return {"ok": True}
