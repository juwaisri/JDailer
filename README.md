# JDialer

JDialer is a production-oriented **Android Communications Hub (Dialer + Contacts + Unified Messaging)** implemented with Kotlin and AndroidX.  
It combines native telephony workflows (calls, contacts, history), modern messaging surfaces, and integration adapters for major communication channels while keeping privacy and on-device data control as first-class concerns.

## Overview

JDialer is designed to replace fragmented communication apps by unifying:

- Dialing and call handling (native and SIP/VoIP paths)
- Contacts management
- Call logs + SMS/MMS history
- Cross-channel conversation context (WhatsApp, Telegram, Signal, Email)
- In-call and in-app messaging actions with a single contact-centric UX

The app is structured for long-term maintainability and incremental rollout of sensitive telecom capabilities.

## Target capabilities

This project is organized to deliver both:

1. A stable, installable dialer/contact baseline
2. A production extension path for unified communications and policy-compliant integrations

## Feature set

### Core communication
- Smart dialer (T9 search, abbreviation matching, suggestion ranking)
- Dedicated dial pad with haptics and digit-aware filtering
- Native call placement via Android Telecom
- Floating bubble for quick dial flow (where supported by platform permissions)
- Separate dialer home, in-call views, and contact-quick-call experiences
- Call log integration and thread-linked call context

### Unified history and search
- Unified call/message timeline feed
- Unified paging source (extensible for high-volume threads/logs)
- Contact-aware grouping and deduplication strategy
- Unified search across calls, SMS/MMS, and external thread metadata
- Time-sectioned history with per-source filtering (SMS, MMS, calls, etc.)

### Contacts
- Contact browsing and details with action shortcuts
- Contact create/edit workflows
- Contact enrichment via sync metadata
- Tag and note model hooks for smart contact cards
- Local cache-first reads with sync merge conflict strategy

### SMS/MMS + messaging
- Message thread browsing scaffold
- Send/receive path hooks for SMS and MMS content providers
- Message body/media abstraction models
- RCS-capable extension points (where Google Messages / platform provides access)
- Group-thread support primitives
- Optional offline message draft persistence

### External communication integrations (adapter-based)
- WhatsApp / WhatsApp Business integration adapters via intent-based handoff
- Telegram integration adapter layer
- Signal integration adapter layer
- Email compose/forward/reply handoff adapters (Gmail/Outlook/default clients)
- Unified actions from contact/history surfaces to external channels

### VoIP and SIP
- Android Telecom integration hooks
- Route abstraction enabling native PSTN vs SIP path selection
- Ready for `ConnectionService` / `PhoneAccount` style routing and registration
- Structured extension for future SIP transport/provider plugins

### Caller intelligence and protection
- Caller ID resolution placeholders + data merge points
- Spam scoring/labeling extension points
- Blocking and filtering policy hooks in call/message flows

### Call recording
- Call recording lifecycle scaffold (permission-aware)
- On-device storage integration point
- Consent-aware metadata model and action tracking

### Quick actions and contact intelligence
- Contextual actions (call, message, open app, share, details)
- Contact cards enriched with fast action menu
- Swipe/context gestures to jump between actions quickly

### Data, sync and offline
- Room-first local persistence model
- Large dataset optimization strategy:
  - proper indexing
  - pagination
  - background synchronization
  - reduced-duplication merge policies
- Offline-first reads and UI graceful fallback while syncing
- DataStore-driven app settings/preferences

### Background and platform behavior
- Foreground/background service boundaries for long-lived telephony features
- WorkManager pipelines for sync and housekeeping
- Notification channel coverage for calls, messaging, and missed actions
- Runtime permission UX and role-manager integration
- Default phone role support path

### Security and privacy
- App-local persistence with hardening hooks for encrypted payloads
- Scoped permission strategy and runtime checks
- Telemetry-minimal principle
- Consent and data retention controls
- Privacy boundary docs for integrations and exported surfaces

### Localization and UI system
- Material 3 foundation
- Theme support and UI tokens for light/dark and custom accents
- String resource-driven localization model
- Composable-first screens with clear separation for legacy compatibility

### Operations and release quality
- CI/CD-ready project layout
- Lint/test/build task integration points
- Crash reporting and analytics hooks prepared at architecture edges
- Structured testing scaffolding for unit, domain, integration, and UI layers

## Module and package topology

```text
app
 ├─ ui/
 │   ├─ dialer
 │   ├─ contacts
 │   ├─ messages
 │   ├─ history
 │   ├─ voip
 │   └─ settings
 ├─ feature/
 │   ├─ dialer
 │   ├─ contacts
 │   ├─ messages
 │   ├─ voip
 │   ├─ home
 │   └─ history
 ├─ core/
 │   ├─ database
 │   ├─ data
 │   ├─ domain
 │   ├─ telecom
 │   ├─ notification
 │   └─ security
 └─ integration/
     ├─ whatsapp
     ├─ telegram
     ├─ signal
     └─ email
```

## Top-level domains and architecture layers

- `presentation`: screens, state holders, UI state, user actions
- `domain`: use cases, domain models, platform-agnostic interfaces
- `data`: repositories, Room DAOs, local source adapters, sync orchestrators
- `platform`: telecom/SMS/notifications/permissions integration adapters
- `integration`: third-party app integration surfaces and adapter contracts

## Build and run

Minimum requirements:
- Android Gradle Plugin + Kotlin toolchain supported by the repository
- Android API 24+ target profile

Common commands:

```bash
./gradlew assembleDebug
./gradlew test
./gradlew lint
```

If you want a release build:

```bash
./gradlew assembleRelease
```
Use `docs/PRODUCTION_RELEASE_CHECKLIST.md` for signing policy, release integrity verification, and Play Console upload steps.

If `app-release-unsigned.apk` is required, check under:

```text
app/build/outputs/apk/release/
```

## Release and deployment notes

- Build tasks should be executed in CI before release.
- Enforce runtime permissions and role-management checks in release variant QA.
- Validate telephony and message permissions on target devices before public rollout.
- Keep third-party integration intents documented and versioned.
- Follow `docs/PRODUCTION_RELEASE_CHECKLIST.md` for release signing and publication readiness checks.

## Privacy and compliance notes

- The app architecture is intended to avoid unnecessary analytics collection by default.
- External platform handoffs should disclose recipient app routing.
- For message/call metadata retention and call recording, retain user consent and local policy gating.

## Contributing

1. Keep changes isolated per layer (`presentation`, `domain`, `data`, `platform`, `integration`).
2. Prefer immutable data models and single-responsibility use cases.
3. Add tests alongside new features (unit + repository + UI).
4. Update this README feature section when new platform integrations land.

## License

GPLv3
