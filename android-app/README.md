# SIM Inventory Android

Android installable client for the SIM Inventory portal.

The app loads the existing SIM Inventory website, so it uses the exact same `/api/state` endpoint and Neon PostgreSQL database. It does not create a second inventory or a local database.

## Build
Run the GitHub Actions workflow **Build SIM Inventory APK** manually, or push changes under `android-app/`. Download the `sim-inventory-apk` artifact from the completed workflow run.

Package: `br.com.siminventory.app`
