package com.jdailer.core.privacy

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class IntegrationPrivacyPolicy(
    val allowThirdPartyIntegrations: Boolean = true,
    val allowThirdPartyMessaging: Boolean = true,
    val allowThirdPartyCalls: Boolean = true,
    val allowWhatsApp: Boolean = true,
    val allowTelegram: Boolean = true,
    val allowSignal: Boolean = true,
    val allowEmail: Boolean = true,
    val allowMessageMedia: Boolean = false,
    val requireExplicitConsent: Boolean = true,
    val redactOutboundIdentity: Boolean = true
)

private val Context.integrationPrivacyDataStore by preferencesDataStore(name = "integration_privacy_prefs")

class IntegrationPrivacyPolicyStore(
    private val context: Context
) {
    private val dataStore = context.integrationPrivacyDataStore

    private object Keys {
        val ALLOW_THIRD_PARTY_INTEGRATIONS = booleanPreferencesKey("allow_third_party_integrations")
        val ALLOW_THIRD_PARTY_MESSAGING = booleanPreferencesKey("allow_third_party_messaging")
        val ALLOW_THIRD_PARTY_CALLS = booleanPreferencesKey("allow_third_party_calls")
        val ALLOW_WHATSAPP = booleanPreferencesKey("allow_whatsapp")
        val ALLOW_TELEGRAM = booleanPreferencesKey("allow_telegram")
        val ALLOW_SIGNAL = booleanPreferencesKey("allow_signal")
        val ALLOW_EMAIL = booleanPreferencesKey("allow_email")
        val ALLOW_MESSAGE_MEDIA = booleanPreferencesKey("allow_message_media")
        val REQUIRE_EXPLICIT_CONSENT = booleanPreferencesKey("require_explicit_consent")
        val REDACT_OUTBOUND_IDENTITY = booleanPreferencesKey("redact_outbound_identity")
    }

    val policy: Flow<IntegrationPrivacyPolicy> = dataStore.data.map { prefs ->
        IntegrationPrivacyPolicy(
            allowThirdPartyIntegrations = prefs[Keys.ALLOW_THIRD_PARTY_INTEGRATIONS] ?: true,
            allowThirdPartyMessaging = prefs[Keys.ALLOW_THIRD_PARTY_MESSAGING] ?: true,
            allowThirdPartyCalls = prefs[Keys.ALLOW_THIRD_PARTY_CALLS] ?: true,
            allowWhatsApp = prefs[Keys.ALLOW_WHATSAPP] ?: true,
            allowTelegram = prefs[Keys.ALLOW_TELEGRAM] ?: true,
            allowSignal = prefs[Keys.ALLOW_SIGNAL] ?: true,
            allowEmail = prefs[Keys.ALLOW_EMAIL] ?: true,
            allowMessageMedia = prefs[Keys.ALLOW_MESSAGE_MEDIA] ?: false,
            requireExplicitConsent = prefs[Keys.REQUIRE_EXPLICIT_CONSENT] ?: true,
            redactOutboundIdentity = prefs[Keys.REDACT_OUTBOUND_IDENTITY] ?: true
        )
    }

    suspend fun setAllowThirdPartyIntegrations(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_THIRD_PARTY_INTEGRATIONS] = enabled
        }
    }

    suspend fun setAllowThirdPartyMessaging(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_THIRD_PARTY_MESSAGING] = enabled
        }
    }

    suspend fun setAllowThirdPartyCalls(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_THIRD_PARTY_CALLS] = enabled
        }
    }

    suspend fun setAllowWhatsApp(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_WHATSAPP] = enabled
        }
    }

    suspend fun setAllowTelegram(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_TELEGRAM] = enabled
        }
    }

    suspend fun setAllowSignal(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_SIGNAL] = enabled
        }
    }

    suspend fun setAllowEmail(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_EMAIL] = enabled
        }
    }

    suspend fun setAllowMessageMedia(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.ALLOW_MESSAGE_MEDIA] = enabled
        }
    }

    suspend fun setRequireExplicitConsent(required: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REQUIRE_EXPLICIT_CONSENT] = required
        }
    }

    suspend fun setRedactOutboundIdentity(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[Keys.REDACT_OUTBOUND_IDENTITY] = enabled
        }
    }
}

