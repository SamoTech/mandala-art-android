# Privacy Policy for Mandala Art — Color, Relax, Create

**Last Updated:** October 26, 2026

Mandala Art ("the Application") is an offline-first coloring app. This policy explains what the app stores on your device, what information may be processed by its advertising services, and the choices available to you.

---

### 1. Information Stored by the Application

- The Application does not require account creation, login, or registration.
- The Application does not ask you to provide your name, email address, physical address, phone number, contacts, or payment details.
- Coloring progress, custom color choices, favorites, app settings, and related artwork records are stored locally on your device using Android Room / SQLite and SharedPreferences.
- The app's own functionality is designed to keep coloring progress and artwork on your device; it does not require a Mandala Art account or app-operated cloud service to use these features.
- Exported mandala PNG images are prepared in the app's private cache and shared only when you choose to use Android's system share interface. Once shared, the receiving app or service handles the image under its own policies.

---

### 2. Third-Party Services and Advertising

The Application integrates the **Google Mobile Ads SDK (Google AdMob)** and **Google User Messaging Platform (UMP)**. The app may request an advertisement, including an optional rewarded ad used to unlock designs.

When the advertising SDK operates, Google Mobile Ads may automatically collect or share information such as:
- IP address, which may be used to estimate general location.
- Advertising and other device identifiers, where available.
- Interactions with the app and ads, such as app launches, taps, ad views, and clicks.
- Diagnostic and performance information, including information used to help detect fraud and abuse.

These practices are controlled in part by Google and may vary with SDK version, device settings, consent status, and the features used. The app does not receive all information processed by the advertising SDK. Review Google's [Google Mobile Ads data disclosure](https://developers.google.com/admob/android/privacy/play-data-disclosure) and [Advertising Privacy Policy](https://policies.google.com/technologies/ads) for more information.

---

### 3. Consent and Privacy Controls

- **Consent choices:** UMP manages advertising consent and privacy options where required by Google's consent configuration and applicable regulations. The options and notices shown can vary by region and circumstances.
- **In-app controls:** Where available, you can reopen the consent and privacy options from **Settings > Manage Ad & Privacy Consent**.
- **Android advertising ID:** You can reset or delete your advertising ID using your device's Android privacy or ads settings. The exact path varies by Android version and manufacturer. This affects the advertising ID; it does not necessarily stop all advertising or prevent use of other signals.

---

### 4. Children and Content Rating

The app configures Google Mobile Ads requests with a maximum ad content rating of **G (General Audiences)**. This limits the requested ad content rating; it does not mean that every ad is guaranteed to be suitable for every child or that the app is certified under a particular store rating or family program.

The Application does not require children or other users to submit personal details to use its local coloring features. Advertising services may process the device and ad-related information described in Section 2. Parents and guardians should review Google's policies and the app's available consent settings when deciding whether the app is appropriate for a child.

---

### 5. Data Retention, Backup, and Deletion

- **Local storage:** App data remains in the app's local storage until it is deleted or the app is uninstalled, subject to Android's backup and restore behavior.
- **Delete one artwork's progress:** Use **Reset** in that mandala's editor to clear its saved progress, or delete the relevant saved artwork from **My Art**.
- **Delete all app-local data:** Open **Android Settings > Apps > Mandala Art > Storage** and select **Clear data** (the wording may differ by device). Uninstalling the app also removes its local app data, subject to any backup or restore process.
- **Backups and device transfer:** The app's Android backup configuration includes its local database and preferences. Whether data is actually backed up or restored depends on Android, device, account, and backup settings. Data restored from a backup may reappear after reinstalling or moving to another device.

Deleting local app data does not delete information already processed by Google or another service, or copies of an image that you chose to share with another app. Refer to those services for their data controls.

---

### 6. Security and Sharing

The app's local database operates within Android's application sandbox. When sharing completed artwork, the app uses Android's FileProvider with temporary, content-scoped access permissions. No method of storage or transmission can be guaranteed to be completely secure.

---

### 7. Contact and Policy Updates

For questions or corrections about this policy, you can open an issue in the project's public GitHub repository: [SamoTech/mandala-art-android](https://github.com/SamoTech/mandala-art-android/issues).

This policy may be updated when app functionality, third-party SDKs, or applicable requirements change. The "Last Updated" date above indicates the most recent policy revision.
