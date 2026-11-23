# AppMarket — Free global Android app business

Working brand: **AppMarket** (name can be changed before public launch).

## Business decisions
- Budget: zero-cost tools and free tiers only.
- Audience: worldwide Android users.
- Distribution/marketing: Telegram channel and bot, with GitHub Releases as a public download source.
- First product: QuickPDF, an offline-first utility app.
- Product policy: only original apps, transparent feature lists, clear privacy notes, and tested APKs.

## Product roadmap
1. QuickPDF: text-to-PDF, image-to-PDF, share.
2. File Toolkit: file rename, ZIP/unzip, format and storage helpers.
3. Photo Toolkit: resize, crop, compress, image conversion.
4. Notes & Scanner: offline notes and document capture.
5. AI tools only after a sustainable free/low-cost inference plan exists.

## Revenue approach
Start with free, useful apps to earn trust and collect feedback. Later consider optional paid upgrades or support. For digital goods sold inside Telegram bots, follow Telegram's current Stars rules. Never claim an app is paid/premium until a working payment, entitlement, refund, and support process exists.

## Launch checklist
- [ ] Build succeeds in GitHub Actions.
- [ ] Install and test on multiple Android versions/devices.
- [ ] Add screenshots, changelog, support contact, and privacy policy.
- [ ] Publish signed release APK and checksum.
- [ ] Create Telegram channel and bot from the owner's Telegram account.
- [ ] Configure a compliant payment and delivery flow.
- [ ] Publish a free release first and gather genuine user feedback.

## Zero-budget constraints
GitHub Actions free usage and artifact retention are subject to account quotas and limits. A Telegram bot requires a bot token created by the owner via BotFather; keep that token private and store it only as a GitHub Actions secret if needed. Do not put tokens, passwords, or payment credentials in source code.
