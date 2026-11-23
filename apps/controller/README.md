# AppMarket Controller — Android app 1

Owner-facing control center.

Planned screens:
- Overview: backend health, managed-bot count, recent events.
- Managed bots: bot username, owner, creation time and status.
- Operations: generate official managed-bot creation links.
- Settings: webhook health, alerts and support.
- Token actions: owner-authorized reveal/rotation with confirmation and audit history.

The app will call an authenticated backend API. Manager credentials must never be embedded in the APK. Android project and build pipeline are a later milestone.
