# JDialer — Sections 1, 2, and 3

## 1) Architecture Overview

JDialer is a **single Gradle module Android app** with layered organization:

```
UI/Presentation Layer
  - Activities, screens, ViewModels, widgets, Compose components
  - Receives UI intents and emits view state/events

Domain Layer
  - Use cases and domain models that encode business behavior
  - Platform-neutral business rules (suggestion ranking, spam policy, message policy, transport resolution)

Data Layer
  - Repositories (data interfaces + default implementations)
  - Room DAOs/entities, sync workers, telecom/SIP service bindings, external adapters
  - Shared infra providers for permissions, role handling, privacy, privacy policy stores

Platform / External Integrations
  - Android framework services: Telecom, Telephony, SMS/MMS, Contacts, Notifications, WorkManager
  - Integration adapters for WhatsApp/WhatsApp Business, Telegram, Signal, Email
```

Dependency direction is unidirectional (`UI -> Domain -> Data`) and each feature is package-owned:

- **UI owns rendering/state orchestration**
- **Domain owns business policy/use cases**
- **Data owns system I/O, persistence, and sync adapters**

Critical cross-cutting concerns are centralized:
- Dependency injection (`Koin`) at `app/src/main/kotlin/com/jdailer/core/di`
- Background work and periodic sync via WorkManager (`core/sync`, `feature/messages/sync`, `core/sync`)
- Security and policy gates in messaging, recording, permissions, and integration routing
- Unified communication entities are persisted in Room and enriched at domain layer before UI consumption

## 2) High-level Module Diagram (Single module, multi-feature package graph)

```text
+---------------------------------------------------------------------------------------+
|                                  app (single Gradle module)                              |
+---------------------------------------------------------------------------------------+
|                                                                                       |
|  [presentation]          [feature]                      [core]           [service]      |
|  - Dialer/Contacts/      - dialer/                     - di            - CallRecording |
|    History/Settings/     - contacts/                  - database       - CallScreening |
|    In-call/Widgets/      - messages/                  - database DAO   - FloatingBubble|
|    Quick actions          - integrations/             - permissions     - QuickSettings |
|  screens and ViewModels  - call/recording/            - sync            - Sync service |
|                          - call/spam/                 - privacy         - PhoneState svc|
|                          - history/                                                     |
|                          - quickactions/                                                |
|                          - voip/                                                        |
|                                                                                       |
|  [receiver]                           [sync]                  [receiver]                 |
|  - Boot/PhoneState receivers           - Authenticator         - call state watchers       |
|                                                                                       |
|                         data stores & Android system bindings                             |
|                         - Room entities/DAOs                          Manifest        |
|                         - Telecom/SIP/Call services                    - permissions   |
+---------------------------------------------------------------------------------------+
```

This diagram is intentionally feature-oriented to mirror the requested architecture while remaining inside a single module.

## 3) Top-level Kotlin Packages and Class Overview

### `com.jdailer.core`
- `core.di.Modules` — Koin module declarations for repositories, use cases, data stores, and services
- `core.database.*` — Room `AppDatabase`, entities, DAOs, converters
- `core.permissions.PermissionCatalog` — grouped runtime permission sets and rationale metadata
- `core.privacy.RecordingPrivacyPolicyStore` — persistent user policy state for call recording
- `core.sync.*` — sync scheduling and contact sync worker pipeline
- `core.common.*` — shared wrappers/utilities

### `com.jdailer.feature.dialer`
- `feature.dialer.data.T9Query`, `DialerScoreCalculator`, `DefaultDialerRepository`
- `feature.dialer.domain.repository.DialerRepository`
- `feature.dialer.domain.usecase.ObserveDialSuggestionsUseCase`
- `presentation.DialerActivity`, `DialerScreen`, `DialerViewModel`

### `com.jdailer.feature.history`
- `feature.history.data.repository.DefaultUnifiedHistoryRepository`
- `feature.history.domain.model.UnifiedHistoryItem`
- `feature.history.domain.usecase.GetUnifiedHistoryUseCase`
- `presentation.HistoryScreen`, `HistoryViewModel`

### `com.jdailer.feature.contacts`
- `feature.contacts.data.ContactsSyncRepository`
- `feature.contacts.domain.model.UnifiedContact`, `ResolveContactEmailsUseCase`
- `feature.contacts.domain.repository.ContactsRepository`
- `presentation.ContactsScreen`, `ContactsViewModel`, `ContactDetailActivity`

### `com.jdailer.feature.messages`
- `feature.messages.domain.model.*` — message thread/item, attachments, transport metadata
- `feature.messages.domain.usecase.*` — sync, observe items/threads, delivery resolution, attachment policy
- `feature.messages.data.repository.DefaultUnifiedMessageRepository`, `DefaultMessageCapabilityRepository`
- `feature.messages.presentation.MessageThreadsScreen`, `MessageThreadScreen`
- `feature.messages.sync.*`, `receiver.SmsMmsReceiver`
- Sync adapters for SMS/MMS and RCS capability probing

### `com.jdailer.feature.integrations`
- `feature.integrations.base.*` — adapter interface/registry/result contracts
- `feature.integrations.whatsapp.*` — WhatsApp & WhatsApp Business route/adapters + advanced services
- `feature.integrations.telegram.*`, `feature.integrations.signal.*`, `feature.integrations.email.*`
- `feature.integrations.linking.*` — external conversation mapping persistence
- `feature.integrations.router.CommunicationHubRouter`

### `com.jdailer.feature.voip`
- `feature.voip.data.telecom.*` — `JDialerTelecomManager`, `JDialerConnection`, `JDialerConnectionService`
- `feature.voip.domain` — routing/role/session models and use cases
- `feature.voip.data.repository` — SIP/session repositories

### `com.jdailer.feature.call`
- `feature.call.spam.*` — caller ID provider abstraction, spam policies, evaluation, blocking
- `feature.call.recording.*` — recording policies, use cases, managers, persistence entities/services

### `com.jdailer.feature.quickactions`
- `feature.quickactions.domain.*` — smart action/card state and use cases
- `feature.quickactions.data.*` — repository default implementation
- `presentation.*` — action strip, contact card, quick action rows

### `com.jdailer.presentation`
- `presentation.main.MainActivity` with bottom nav shell
- `presentation.dialer`, `presentation.history`, `presentation.contacts`, `presentation.settings`
- `presentation.incall.InCallActivity`
- `presentation.widget` home-screen widgets

### `com.jdailer.receiver`, `com.jdailer.service`, `com.jdailer.sync`
- Receivers and services for phone state, boot, sync orchestration, call screening/recording, floating bubble, tiles

