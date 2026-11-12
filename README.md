# Banking

Banking is a mobile app: Native Android (Kotlin + Jetpack Compose).

## Screens

- **Sign-in**: Biometric or PIN entry, update check
- **Verify (MFA)**: Second factor and session re-entry
- **Home**: Dashboard hub opening each feature
- **Accounts and Cards**: Balances, cards and transactions
- **Wealth overview**: Total wealth, holdings, link accounts
- **Coach**: Insights, categories, saving rounds
- **Pay**: Scan QR or pick contact
- **Confirm and receipt**: Review, re-verify, send, result
- **Vault**: Store, open, delete documents
- **More and settings**: Easy Mode, notifications, menu
- **Help and support**: FAQs, contact, security info
- **Privacy and devices**: Block device, consents, account closure
- **Client list**: Assigned clients
- **Client detail**: Holdings, advice, risk profile
- **Trade**: Order, checks, confirm

## Technology

- Kotlin, Jetpack Compose with Material 3, MVVM with ViewModel + StateFlow, Navigation Compose, Room for local storage, Hilt or manual DI, Gradle Kotlin DSL, JUnit + Compose UI tests
- Package / bundle: `com.mobile360.banking`

## Build and run

Requirements: JDK 17 and Android Studio (or the Android SDK command-line tools).

```bash
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # install it on a running emulator or a connected phone
```

Or open the folder in Android Studio and run the `app` configuration.

## Tests

```bash
./gradlew testDebugUnitTest
```

Device tests (Maestro) are in `.maestro/`: start an emulator, install the app, then run `maestro test -e APP_ID=<application id> .maestro`.

## Sample data

The app runs without a backend: its repositories serve seeded sample records, so every screen works out of the box. To connect the real service, implement the repository interfaces against the API and change the bindings - the screens do not change.

Sample images and people are placeholders: replace them with your own before a release.

## Status

Built; the tests that need a device or a build tool this machine lacks did not run here - the CI pipeline runs them.

## Continuous integration

`.github/workflows/android-ci.yml` builds and tests every push and pull request.
