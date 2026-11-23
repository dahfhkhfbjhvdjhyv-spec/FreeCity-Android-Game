# AppMarket backend — milestone 1

FastAPI webhook receiver for Telegram Managed Bots.

## Local run
```bash
python -m venv .venv
. .venv/bin/activate
pip install -r requirements.txt
export MANAGER_BOT_TOKEN='your-manager-bot-token'
export WEBHOOK_SECRET='use-a-long-random-secret'
uvicorn app.main:app --host 0.0.0.0 --port 8080
```

Check `GET /health`. Configure Telegram's webhook to the public HTTPS URL ending in `/telegram/webhook`, using the same value as the webhook `secret_token`.

## Telegram setup
1. Create a manager bot with @BotFather.
2. In the BotFather Mini App, open that bot's settings and enable **Bot Management Mode**.
3. Deploy the backend with secrets stored in the host's secret manager.
4. Ask each user to send `/start` to the manager bot.
5. Share a managed-bot deep link: `https://t.me/newbot/<manager_username>/<suggested_username>?name=<suggested_name>`.
6. The user confirms bot creation in Telegram. The manager receives a `managed_bot` update.

## Current security scope
This first milestone stores only bot metadata and event history. It does not yet fetch or send child-bot tokens. That flow will be added only with owner-only authorization, delivery confirmation, redacted logging, and token non-persistence. Never commit real tokens or paste them into chat.

## Production work still required
Authenticated dashboard API, rate limiting, webhook replay/idempotency protection, persistent managed database, monitoring, tests, backup strategy, deployment, and end-to-end token delivery.
