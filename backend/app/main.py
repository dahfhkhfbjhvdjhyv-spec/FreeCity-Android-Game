import os
import sqlite3
from contextlib import closing
from typing import Any

import httpx
from fastapi import FastAPI, Header, HTTPException, Request

MANAGER_TOKEN = os.getenv("MANAGER_BOT_TOKEN", "")
WEBHOOK_SECRET = os.getenv("WEBHOOK_SECRET", "")
DB_PATH = os.getenv("DATABASE_PATH", "appmarket.sqlite3")
app = FastAPI(title="AppMarket Backend", version="0.1.0")


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
    return {"status": "ok", "service": "appmarket-backend", "version": "0.1.0"}


@app.post("/telegram/webhook")
async def webhook(request: Request, x_telegram_bot_api_secret_token: str | None = Header(default=None)):
    if not WEBHOOK_SECRET or x_telegram_bot_api_secret_token != WEBHOOK_SECRET:
        raise HTTPException(status_code=401, detail="Invalid webhook secret")

    update = await request.json()
    msg = update.get("message") or {}
    sender = msg.get("from") or {}
    chat = msg.get("chat") or {}
    if (msg.get("text") or "").startswith("/start") and chat.get("type") == "private":
        await telegram("sendMessage", {
            "chat_id": sender.get("id"),
            "text": "Welcome to AppMarket. Start here before creating a managed bot so we can send private setup messages."
        })

    managed = update.get("managed_bot")
    if managed:
        owner = managed.get("user") or {}
        bot = managed.get("bot") or {}
        owner_id, bot_id = owner.get("id"), bot.get("id")
        if owner_id and bot_id:
            with closing(connect()) as con:
                con.execute(
                    "INSERT OR REPLACE INTO managed_bots (bot_id, owner_id, username, first_name) VALUES (?, ?, ?, ?)",
                    (bot_id, owner_id, bot.get("username"), bot.get("first_name")),
                )
                con.execute(
                    "INSERT INTO events (event_type, bot_id, owner_id) VALUES (?, ?, ?)",
                    ("managed_bot_created_or_updated", bot_id, owner_id),
                )
                con.commit()
            # Token retrieval and owner-only delivery are enabled in the next security-reviewed milestone.
            await telegram("sendMessage", {
                "chat_id": owner_id,
                "text": "Your managed bot was created. AppMarket has recorded its details. Secure token delivery is being prepared."
            })

    return {"ok": True}
