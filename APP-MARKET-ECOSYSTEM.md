# AppMarket BotFather Ecosystem

A Telegram Managed Bots platform with two Android companion apps and one always-on backend.

## Verified Telegram model
Telegram's Managed Bots feature is the supported way for a manager bot to create and manage bots for users. The owner confirms bot creation in Telegram; the manager bot receives a `managed_bot` update and can call `getManagedBotToken` for that bot. The manager bot must have **Bot Management Mode** enabled in the @BotFather Mini App. This platform does not forge tokens or bypass Telegram.

Official references:
- https://core.telegram.org/bots/features#managed-bots
- https://core.telegram.org/bots/api
- https://core.telegram.org/api/bots/managed-bots

## Components
1. **AppMarket Controller** — owner dashboard: service health, managed-bot count, event history, configuration status, and safe operational controls.
2. **AppMarket Guardian** — independent health monitor: backend heartbeat, webhook status, alerting, and recovery guidance. Android cannot guarantee 24/7 execution; the cloud backend is the always-on component.
3. **Cloud backend** — receives Telegram webhooks, records bot metadata, obtains managed-bot tokens only when needed, and delivers a token privately to the bot owner after they have started the manager bot.

## Token handling
- Never request or publish a user's unrelated login code, password, or session.
- Never place bot tokens in a public channel, Git repository, analytics, or logs.
- Only deliver a managed bot's token to its recorded owner in a private chat.
- Do not persist child bot tokens in the MVP database; fetch on demand and deliver privately.
- Rotate a token only after explicit owner action and warn that the previous token will stop working.
- The manager bot token and webhook secret are deployment secrets, not source-code values.

## First-run checklist
1. Create a manager bot using @BotFather.
2. Open the bot's settings in the BotFather Mini App and enable **Bot Management Mode**.
3. Deploy the backend with `MANAGER_BOT_TOKEN` and `WEBHOOK_SECRET` secrets.
4. Configure Telegram webhook to `/telegram/webhook` with the same secret token.
5. Ask each user to send `/start` to the manager bot before creating a managed bot, so private delivery is possible.
6. Share the official managed-bot creation link:
   `https://t.me/newbot/<manager_bot_username>/<suggested_username>?name=<suggested_name>`
7. The user confirms creation in Telegram. The manager receives the event, obtains the managed token, and sends it only to that owner in private chat.

## Current milestone
Milestone 1 is the secure webhook backend and architecture documentation. Android apps, authenticated dashboard APIs, deployment automation, and end-to-end tests are subsequent milestones. No APK or production deployment is claimed yet.
