# AppMarket Managed Bots backend — milestone 2

FastAPI webhook receiver for Telegram Managed Bots. It accepts Telegram updates, detects `managed_bot`, fetches the child token with `getManagedBotToken`, and sends it in a protected private message to the owner ID supplied by Telegram.

## Local run

```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
export MANAGER_BOT_TOKEN='your-manager-bot-token'
export WEBHOOK_SECRET='a-random-secret-of-at-least-32-characters'
export DATABASE_PATH='./appmarket.sqlite3'
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

Check `GET /health`. Use a public HTTPS deployment URL for Telegram webhooks.

## Configure the Telegram webhook

After deployment, set the webhook using your manager bot token and the exact same webhook secret configured on the server:

```bash
curl -X POST "https://api.telegram.org/bot${MANAGER_BOT_TOKEN}/setWebhook" \
  -H "Content-Type: application/json" \
  -d '{"url":"https://YOUR-DOMAIN/telegram/webhook","secret_token":"YOUR_WEBHOOK_SECRET","allowed_updates":["message","managed_bot"]}'
```

Check it with:

```bash
curl "https://api.telegram.org/bot${MANAGER_BOT_TOKEN}/getWebhookInfo"
```

Do not put real values in source control or share them in chat.

## Telegram setup and customer flow

1. Create the manager bot with the real @BotFather.
2. In the BotFather Mini App, enable **Bot Management Mode** for the manager bot.
3. Deploy this backend with `MANAGER_BOT_TOKEN`, `WEBHOOK_SECRET`, and a persistent `DATABASE_PATH` configured as hosting secrets/settings.
4. Each customer must open the manager bot and press Start before creating a managed bot.
5. Send the customer a managed-bot link:
   `https://t.me/newbot/<manager_bot_username>/<suggested_bot_username>?name=<suggested_bot_name>`
6. The customer confirms creation in Telegram. Telegram sends a `managed_bot` update to the manager bot.
7. The backend fetches the managed bot token and sends it only to the owner in their private chat.

## Security behavior

- The webhook checks Telegram's `X-Telegram-Bot-Api-Secret-Token` header.
- Child tokens are fetched only when a `managed_bot` update arrives.
- Child tokens are not written to SQLite, source control, or application logs.
- The token is sent only to the owner user ID in the Telegram update, in a private message with protected content.
- If the owner has not started the manager bot, Telegram may reject the private message; have the owner press Start first.
- Update IDs are recorded after successful processing to reduce duplicate deliveries.
- Never send tokens to public channels, groups, support chats, or third parties.

## Important limitations before production

This is a working backend milestone, not a complete commercial service. Deploy it before testing. Use persistent storage and backups; SQLite on ephemeral hosting loses metadata. Add authenticated admin/customer APIs, rate limits, monitoring, automated tests, operational alerting, and a documented token-rotation/support process before accepting paying customers. A successful GitHub commit does not mean the server is deployed or that end-to-end token delivery has been tested.
