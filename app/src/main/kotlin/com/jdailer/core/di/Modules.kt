package com.jdailer.core.di

import android.content.Context
import androidx.room.Room
import com.jdailer.BuildConfig
import com.jdailer.core.common.coroutines.DispatcherProvider
import com.jdailer.core.database.AppDatabase
import com.jdailer.core.privacy.RecordingPrivacyPolicyStore
import com.jdailer.core.privacy.IntegrationPrivacyPolicyStore
import com.jdailer.feature.call.recording.CallRecordingManager
import com.jdailer.feature.call.recording.DefaultCallRecordingManager
import com.jdailer.feature.call.recording.domain.usecase.StartRecordingWithPolicyUseCase
import com.jdailer.feature.call.recording.domain.usecase.StopRecordingWithPolicyUseCase
import com.jdailer.feature.call.spam.CallerIdService
import com.jdailer.feature.call.spam.DefaultCallerIdService
import com.jdailer.feature.call.spam.HttpCallerIdProvider
import com.jdailer.feature.call.spam.DefaultSpamFilterService
import com.jdailer.feature.call.spam.RemoteCallerIdProvider
import com.jdailer.feature.call.spam.NoopCallerIdProvider
import com.jdailer.feature.call.spam.SpamFilterService
import com.jdailer.feature.call.spam.domain.usecase.BlockCallerUseCase
import com.jdailer.feature.call.spam.domain.usecase.EvaluateCallerRiskUseCase
import com.jdailer.feature.call.spam.domain.usecase.EvaluateCallerProfileUseCase
import com.jdailer.feature.contacts.data.ContactsSyncRepository
import com.jdailer.feature.contacts.domain.repository.ContactsRepository
import com.jdailer.feature.dialer.data.repository.DefaultDialerRepository
import com.jdailer.feature.dialer.domain.repository.DialerRepository
import com.jdailer.feature.dialer.domain.usecase.ObserveDialSuggestionsUseCase
import com.jdailer.feature.history.data.repository.DefaultUnifiedHistoryRepository
import com.jdailer.feature.history.domain.usecase.GetUnifiedHistoryUseCase
import com.jdailer.feature.history.domain.repository.UnifiedHistoryRepository
import com.jdailer.feature.integrations.base.CommunicationAdapterRegistry
import com.jdailer.feature.integrations.base.CommunicationAppAdapter
import com.jdailer.feature.integrations.base.DefaultIntegrationPolicyService
import com.jdailer.feature.integrations.base.IntegrationPolicyService
import com.jdailer.feature.integrations.base.PlatformIntentLauncher
import com.jdailer.feature.integrations.email.EmailAdapter
import com.jdailer.feature.integrations.email.EmailIntegrationService
import com.jdailer.feature.integrations.router.CommunicationHubRouter
import com.jdailer.feature.integrations.signal.SignalAdapter
import com.jdailer.feature.integrations.signal.SignalIntegrationService
import com.jdailer.feature.integrations.telegram.TelegramAdapter
import com.jdailer.feature.integrations.telegram.TelegramIntegrationService
import com.jdailer.feature.integrations.whatsapp.WhatsAppAdapter
import com.jdailer.feature.integrations.whatsapp.WhatsAppBusinessAdapter
import com.jdailer.feature.integrations.whatsapp.WhatsAppIntegrationService
import com.jdailer.feature.integrations.linking.data.repository.RoomExternalConversationLinkRepository
import com.jdailer.feature.integrations.linking.domain.repository.ExternalConversationLinkRepository
import com.jdailer.feature.messages.data.repository.DefaultMessageCapabilityRepository
import com.jdailer.feature.messages.data.repository.DefaultUnifiedMessageRepository
import com.jdailer.feature.messages.domain.repository.MessageCapabilityRepository
import com.jdailer.feature.messages.domain.repository.UnifiedMessageRepository
import com.jdailer.feature.messages.domain.usecase.GetRcsCapabilityUseCase
import com.jdailer.feature.messages.domain.usecase.ObserveMessageItemsUseCase
import com.jdailer.feature.messages.domain.usecase.ObserveMessageThreadsUseCase
import com.jdailer.feature.messages.domain.usecase.SendMessageUseCase
import com.jdailer.feature.messages.domain.usecase.SendMessageWithPolicyUseCase
import com.jdailer.feature.messages.domain.usecase.SyncMessagesUseCase
import com.jdailer.feature.voip.domain.usecase.ResolveCallTransportUseCase
import com.jdailer.feature.voip.domain.usecase.PlaceCallWithTransportUseCase
import com.jdailer.feature.quickactions.domain.usecase.LoadSmartContactCardStateUseCase
import com.jdailer.feature.quickactions.domain.usecase.GetAdvancedQuickActionsUseCase
import com.jdailer.feature.messages.domain.usecase.ResolveMessageDeliveryUseCase
import com.jdailer.feature.integrations.email.EmailAdvancedIntegrationService
import com.jdailer.feature.integrations.signal.SignalAdvancedIntegrationService
import com.jdailer.feature.integrations.telegram.TelegramAdvancedIntegrationService
import com.jdailer.feature.integrations.whatsapp.WhatsAppAdvancedIntegrationService
import com.jdailer.feature.integrations.whatsapp.ResolveWhatsAppRouteUseCase
import com.jdailer.feature.integrations.telegram.domain.usecase.ResolveTelegramRouteUseCase
import com.jdailer.feature.integrations.signal.domain.usecase.ResolveSignalRouteUseCase
import com.jdailer.feature.integrations.email.domain.usecase.ResolveEmailRouteUseCase
import com.jdailer.feature.call.recording.domain.usecase.ValidateRecordingPolicyUseCase
import com.jdailer.feature.call.recording.domain.usecase.StartRecordingWithConsentUseCase
import com.jdailer.feature.messages.domain.usecase.SyncMessagesWithPolicyUseCase
import com.jdailer.feature.messages.domain.usecase.ValidateMessageAttachmentPolicyUseCase
import com.jdailer.feature.call.recording.presentation.RecordingsListViewModel
import com.jdailer.feature.messages.presentation.MessageThreadViewModel
import com.jdailer.feature.messages.presentation.MessageThreadsViewModel
import com.jdailer.feature.quickactions.data.repository.DefaultSmartContactCardRepository
import com.jdailer.feature.quickactions.domain.repository.SmartContactCardRepository
import com.jdailer.feature.voip.data.repository.InMemorySipProfileRepository
import com.jdailer.feature.voip.data.repository.InMemoryVoipRepository
import com.jdailer.feature.voip.data.telecom.JDialerTelecomManager
import com.jdailer.feature.voip.data.telecom.TelecomRoleManager
import com.jdailer.feature.voip.domain.model.SipProfile
import com.jdailer.feature.voip.domain.repository.SipProfileRepository
import com.jdailer.feature.voip.domain.repository.VoipRepository
import com.jdailer.feature.voip.domain.usecase.GetSipProfileUseCase
import com.jdailer.feature.voip.domain.usecase.ObserveVoipSessionsUseCase
import com.jdailer.feature.voip.domain.usecase.PlaceTelecomCallUseCase
import com.jdailer.feature.voip.domain.usecase.ResolveTelecomRoleUseCase
import com.jdailer.feature.voip.domain.usecase.SaveSipProfileUseCase
import com.jdailer.presentation.contacts.ContactsViewModel
import com.jdailer.presentation.dialer.DialerViewModel
import com.jdailer.presentation.history.HistoryViewModel
import com.jdailer.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.bind
import org.koin.dsl.module
import com.google.gson.Gson
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

val coreModule = module {
    single { DispatcherProvider() }
    single { RecordingPrivacyPolicyStore(get()) }
    single { IntegrationPrivacyPolicyStore(get()) }
    single { Gson() }
    single {
        OkHttpClient.Builder()
            .connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()
    }
    single<RemoteCallerIdProvider> {
        val baseUrl = BuildConfig.SPAM_API_URL
        if (baseUrl.isBlank() || baseUrl.contains("example.com")) {
            NoopCallerIdProvider()
        } else {
            HttpCallerIdProvider(baseUrl, get())
        }
    }
}

val dataModule = module {
    single<AppDatabase> {
        Room.databaseBuilder(
            get<Context>(),
            AppDatabase::class.java,
            AppDatabase.NAME
        ).fallbackToDestructiveMigration()
            .build()
    }

    single { get<AppDatabase>().contactDao() }
    single { get<AppDatabase>().communicationEventDao() }
    single { get<AppDatabase>().spamDao() }
    single { get<AppDatabase>().callRecordingDao() }
    single { get<AppDatabase>().messageDao() }
    single { get<AppDatabase>().callerLookupDao() }
    single { get<AppDatabase>().externalConversationLinkDao() }
    single { get<AppDatabase>().contactTagDao() }
    single { get<AppDatabase>().contactNoteDao() }
}

val domainModule = module {
    single<DialerRepository> { DefaultDialerRepository(get(), get(), get()) }
    single<UnifiedHistoryRepository> { DefaultUnifiedHistoryRepository(get(), get()) }
    single<ContactsRepository> { ContactsSyncRepository(get(), get(), get()) }
    single<SpamFilterService> { DefaultSpamFilterService(get(), get()) }
    single<CallerIdService> { DefaultCallerIdService(get(), get(), get(), get()) }
    single<CallRecordingManager> { DefaultCallRecordingManager(get(), get(), get()) }
    single<UnifiedMessageRepository> { DefaultUnifiedMessageRepository(get(), get(), get(), get(), get()) }
    single<MessageCapabilityRepository> { DefaultMessageCapabilityRepository(get()) }
    single<ExternalConversationLinkRepository> { RoomExternalConversationLinkRepository(get()) }
    single<SmartContactCardRepository> { DefaultSmartContactCardRepository(get(), get(), get(), get()) }
    single<VoipRepository> { InMemoryVoipRepository() }
    single<SipProfileRepository> {
        InMemorySipProfileRepository(
            SipProfile(
                profileId = "default",
                domain = "",
                username = "",
                password = ""
            )
        )
    }
    single { TelecomRoleManager(get()) }
    single { JDialerTelecomManager(get(), get()) }
    single { ObserveVoipSessionsUseCase(get()) }
    single { GetSipProfileUseCase(get()) }
    single { SaveSipProfileUseCase(get()) }
    single { PlaceTelecomCallUseCase(get()) }
    single { ResolveTelecomRoleUseCase(get()) }
    single { ObserveDialSuggestionsUseCase(get()) }
    single { GetUnifiedHistoryUseCase(get()) }
    single { ObserveMessageThreadsUseCase(get()) }
    single { ObserveMessageItemsUseCase(get()) }
    single { SyncMessagesUseCase(get()) }
    single { SyncMessagesWithPolicyUseCase(get()) }
    single { ResolveMessageDeliveryUseCase(get()) }
    single { ResolveWhatsAppRouteUseCase(get()) }
    single { ResolveTelegramRouteUseCase(get()) }
    single { ResolveSignalRouteUseCase(get()) }
    single { ResolveEmailRouteUseCase(get()) }
    single { EvaluateCallerRiskUseCase(get()) }
    single { SendMessageUseCase(get()) }
    single { SendMessageWithPolicyUseCase(get(), get()) }
    single { GetRcsCapabilityUseCase(get()) }
    single { ValidateMessageAttachmentPolicyUseCase(get()) }
    single { ValidateRecordingPolicyUseCase(get(), get()) }
    single { StartRecordingWithConsentUseCase(get(), get()) }
    single { GetAdvancedQuickActionsUseCase(get()) }
    single { LoadSmartContactCardStateUseCase(get(), get()) }
    single { ResolveCallTransportUseCase(get(), get(), get()) }
    single { PlaceCallWithTransportUseCase(get(), get()) }
    single { StartRecordingWithPolicyUseCase(get(), get()) }
    single { StopRecordingWithPolicyUseCase(get()) }
    single { EvaluateCallerProfileUseCase(get(), get()) }
    single { BlockCallerUseCase(get()) }
}

val appModules = module {
    single { WhatsAppAdapter() } bind(CommunicationAppAdapter::class)
    single { WhatsAppBusinessAdapter() } bind(CommunicationAppAdapter::class)
    single { TelegramAdapter() } bind(CommunicationAppAdapter::class)
    single { SignalAdapter() } bind(CommunicationAppAdapter::class)
    single { EmailAdapter() } bind(CommunicationAppAdapter::class)
    single<IntegrationPolicyService> { DefaultIntegrationPolicyService(get()) }
    single { PlatformIntentLauncher(get(), get()) }
    single { WhatsAppIntegrationService(get()) }
    single { TelegramIntegrationService(get()) }
    single { SignalIntegrationService(get()) }
    single { EmailIntegrationService(get()) }
    single { WhatsAppAdvancedIntegrationService(get(), get()) }
    single { TelegramAdvancedIntegrationService(get(), get()) }
    single { SignalAdvancedIntegrationService(get(), get()) }
    single { EmailAdvancedIntegrationService(get(), get()) }
    single { CommunicationAdapterRegistry(getAll()) }
    single { CommunicationHubRouter(get(), get()) }
}

val presentationModule = module {
    viewModel { DialerViewModel(get(), get()) }
    viewModel { HistoryViewModel(get()) }
    viewModel { ContactsViewModel(get(), get(), get(), get(), get()) }
    viewModel { MessageThreadsViewModel(get(), get()) }
    viewModel { MessageThreadViewModel(get(), get()) }
    viewModel { SettingsViewModel(get(), get()) }
    viewModel { RecordingsListViewModel(get(), get()) }
}





