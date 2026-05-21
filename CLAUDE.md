# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project overview

Blindo is an Android app (applicationId `com.pbaltazar.blindo`) that distributes Talkback/screenreader labels for Android apps. It also provides "Blindo Vision", an `AccessibilityService` that describes screen contents for blind users. Backend is GraphQL.

## Local setup prerequisites

A clean clone will not build until you provide the credentials the project keeps out of git. See `README.md` for the exhaustive list; the minimum is:

1. `local.properties` (or `debug.properties` / `beta.properties` / `release.properties`) with `BLINDO_API_URL`, `ACCOUNT_TYPE`, `ADMOB_*` keys, `admobApplicationId`, `DEBUG_GEOGRAPHY`, `TEST_DEVICE_ID`. These are consumed by the `secrets-gradle-plugin` and injected as `BuildConfig` / manifest placeholders.
2. `keystore.properties` (copy from `keystore.properties.example`) populated with both DEBUG and RELEASE keystore paths/passwords/aliases. `app/build.gradle` reads this file *unconditionally at configuration time*, so missing it breaks every gradle task.
3. `app/src/main/res/values/secrets.xml` with `facebook_application_id`, `fb_login_protocol_scheme`, `twitter_consumer_key`, `twitter_consumer_secret`.
4. `app/google-services.json` from the Firebase project.

## Build / run / test

Gradle wrapper: `./gradlew`. Java 11 in CI (`actions/setup-java@v3` temurin 11). Gradle daemon is disabled in CI compile script.

Common tasks:

```bash
./gradlew :app:assembleDebug          # debug APK
./gradlew :app:assembleBeta           # beta APK (release-signed, versionNameSuffix=-beta)
./gradlew :app:assembleRelease        # release APK
./gradlew :app:bundleRelease          # release AAB (Play Console format)
./gradlew :app:testDebugUnitTest      # JVM unit tests
./gradlew :app:connectedDebugAndroidTest  # instrumented tests (needs device)
./gradlew :app:lintDebug              # lint
./gradlew clean
```

Run a single unit test class: `./gradlew :app:testDebugUnitTest --tests "com.pbaltazar.blindo.ExampleUnitTest"`.

Build variants are `debug`, `beta`, `release`. `beta` and `release` minify with R8 and use `multidex-config.pro`. `lintOptions.abortOnError = false` — lint warnings will not fail the build.

## Architecture

Three Gradle modules:

- **`:app`** — the application. Package `com.pbaltazar.blindo`. Uses Kotlin, AndroidX, single-Activity + Jetpack Navigation, ViewBinding, Koin DI, Paging 3, coroutines, Glide, Firebase (Auth/Analytics/Crashlytics/Performance/Messaging/Ads), Play Billing, Facebook/Twitter SDKs.
- **`:apollito`** — internal wrapper around Apollo Kotlin 3 (`ApollitoClient.Builder` in `com.blindo.apollito.api`). Centralises GraphQL client config: server URL, custom scalar adapters (`DateTime`, `JSONString`), HTTP/normalized caches, debug logging. `:app` constructs two named clients in `DataModule.kt` (`BASIC_APOLLITO_CLIENT`, `EXTENDED_TIMEOUT_APOLLITO_CLIENT` for uploads).
- **`:screenshot-watcher`** — small library exposing `ScreenshotWatcherDelegate`. Watches `MediaStore` for new screenshots; consumed by `BlindoVisionService` to trigger image-description requests.

### Application layering (inside `:app`)

```
ui/         — Fragments/Activities/ViewModels, grouped by feature (home, pack, rating, user, vision, ...)
usecases/   — One file per GraphQL operation: QueryXxx / MutationXxx. The "domain" layer.
data/       — Gateways: <Feature>Gateway interface + BlindoApi<Feature>Gateway impl that calls Apollo.
entities/   — Domain models, inputs, filters, errors. Apollo-generated types live under `graphql/` (build dir).
services/   — Android services. Notably BlindoVisionService (AccessibilityService).
utils/      — Cross-cutting: ads, analytics, authentication, billing, preferences, messaging, vision, etc.
components/ — Reusable UI building blocks (filter screens, etc.).
```

Flow for a typical screen: Fragment → ViewModel → UseCase (`QueryListApps` etc.) → Gateway (`AppGateway`/`BlindoApiAppGateway`) → `ApollitoClient` → GraphQL.

### Dependency injection (Koin)

`Blindo.kt` (`Application` subclass) registers every Koin module in `onCreate`. **When you add a new feature package with its own ViewModel/UseCase/Gateway, you must:**
1. Create a `<feature>Module.kt` exposing a `val xxxModule = module { ... }`.
2. Register it in the `modules(listOf(...))` call in `Blindo.kt`. Forgetting this is the #1 cause of `NoBeanDefFoundException` at runtime.

### GraphQL / Apollo

Schema and operations live in `app/src/main/graphql/com/pbaltazar/blindo/graphql/` (`*.graphql` operations, `schema.json`). The `com.apollographql.apollo3` Gradle plugin generates Kotlin models into package `com.pbaltazar.blindo.graphql` (configured in `app/build.gradle`). Custom scalars: `DateTime` → `java.util.Date`, `JSONString` → `org.json.JSONObject`, `Upload` mapped via `mapScalarToUpload`. After editing `.graphql` files, rebuild to regenerate models.

### Manifest / runtime entry points

- Launcher: `BlindoActivity` (single-Activity, hosts `main_navigation.xml`).
- Auxiliary activities: `LoginActivity`, `UploadPackActivity` (registered for `ACTION_SEND` of `.tbl` Talkback label files and `application/json`), `BlindoVisionPreferencesActivity`.
- Services: `BlindoVisionService` (AccessibilityService — feedback type SPOKEN), `AccountAuthenticatorService` (runs in `:authentication` process), `MessagingService` (FCM).

### SDK / versioning

`libraries.gradle` is the single source of truth for plugin versions, dependency versions, and SDK levels (`compile/target=32`, `min=21`). `version.gradle` holds `versionCode`/`versionName` and is auto-bumped by the CI deploy workflow — do not hand-edit it as part of feature PRs unless you know what you're doing.

## CI / release

GitHub Actions in `.github/workflows/`:

- `deploy-blindo-version.yaml` — reusable workflow. Decodes secrets into the working tree (keystores, `google-services.json`, `secrets.xml`, gradle secret properties), bumps version, builds the requested variant/packageType (APK or AAB), optionally uploads to Play Console via `r0adkll/upload-google-play`, and commits the new `version.gradle` back to the repo.
- `deploy-manually.yaml` — `workflow_dispatch` wrapper that lets you trigger the above with chosen variant/track.

Helper scripts in `.github/scripts/` regenerate the local-only secret files from GitHub secrets — useful as a reference for what each secret feeds into.

Play Store `whatsnew` notes live in `assets/whatsnew/` and are uploaded by the publish step.

## Conventions worth knowing

- Kotlin only. JVM target 1.8, core library desugaring enabled (`minSdk 21` needs it for `java.time`).
- ViewBinding is enabled; do not reintroduce Kotlin synthetics.
- Logging goes through Timber (`Blindo.kt` plants `BlindoDebugger`). Use `Timber.x(...)` rather than `android.util.Log`.
- Navigation uses safe-args; nav graph is `app/src/main/res/navigation/main_navigation.xml`.
- Strings: English defaults in `values/`, Spanish in `values-es/`. `secrets.xml` is gitignored — never check it in.
