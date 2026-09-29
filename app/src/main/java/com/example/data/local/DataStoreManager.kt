package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.model.ProtectionStatus
import com.example.domain.model.UnblockSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "sabr_preferences")

class DataStoreManager(private val context: Context) {

    private object PreferencesKeys {
        val PROTECTION_STATUS = stringPreferencesKey("protection_status")
        val CURRENT_STAGE = intPreferencesKey("current_stage")
        val COMPLETED_STAGES = intPreferencesKey("completed_stages")
        val STAGE_START_TIMESTAMP = longPreferencesKey("stage_start_timestamp")
        val UNBLOCK_REQUEST_TIMESTAMP = longPreferencesKey("unblock_request_timestamp")
        val STAGE_DURATION_MS = longPreferencesKey("stage_duration_ms")
        val SAFESEARCH_ENABLED = booleanPreferencesKey("safesearch_enabled")
        val BLOCK_GAMBLING = booleanPreferencesKey("block_gambling")
        val BLOCK_DATING = booleanPreferencesKey("block_dating")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
    }

    val unblockSessionStateFlow: Flow<UnblockSessionState> = context.dataStore.data.map { prefs ->
        val statusString = prefs[PreferencesKeys.PROTECTION_STATUS] ?: ProtectionStatus.PROTECTION_OFF.name
        val status = try {
            ProtectionStatus.valueOf(statusString)
        } catch (_: Exception) {
            ProtectionStatus.PROTECTION_OFF
        }

        val currentStage = prefs[PreferencesKeys.CURRENT_STAGE] ?: 1
        val completedStages = prefs[PreferencesKeys.COMPLETED_STAGES] ?: 0
        val stageStart = prefs[PreferencesKeys.STAGE_START_TIMESTAMP] ?: 0L
        val requestTime = prefs[PreferencesKeys.UNBLOCK_REQUEST_TIMESTAMP] ?: 0L
        val durationMs = prefs[PreferencesKeys.STAGE_DURATION_MS] ?: (60 * 60 * 1000L)

        UnblockSessionState(
            status = status,
            currentStage = currentStage.coerceIn(1, 24),
            completedStages = completedStages.coerceIn(0, 24),
            stageStartTimestampEpochMs = stageStart,
            unblockRequestTimestampEpochMs = requestTime,
            stageDurationMs = durationMs
        )
    }

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.HAS_SEEN_ONBOARDING] ?: false
    }

    val safeSearchEnabledFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.SAFESEARCH_ENABLED] ?: true
    }

    val blockGamblingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BLOCK_GAMBLING] ?: true
    }

    val blockDatingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[PreferencesKeys.BLOCK_DATING] ?: true
    }

    suspend fun saveUnblockSessionState(state: UnblockSessionState) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROTECTION_STATUS] = state.status.name
            prefs[PreferencesKeys.CURRENT_STAGE] = state.currentStage
            prefs[PreferencesKeys.COMPLETED_STAGES] = state.completedStages
            prefs[PreferencesKeys.STAGE_START_TIMESTAMP] = state.stageStartTimestampEpochMs
            prefs[PreferencesKeys.UNBLOCK_REQUEST_TIMESTAMP] = state.unblockRequestTimestampEpochMs
            prefs[PreferencesKeys.STAGE_DURATION_MS] = state.stageDurationMs
        }
    }

    suspend fun setProtectionStatus(status: ProtectionStatus) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.PROTECTION_STATUS] = status.name
        }
    }

    suspend fun setHasSeenOnboarding(hasSeen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.HAS_SEEN_ONBOARDING] = hasSeen
        }
    }

    suspend fun setSafeSearchEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.SAFESEARCH_ENABLED] = enabled
        }
    }

    suspend fun setBlockGambling(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BLOCK_GAMBLING] = enabled
        }
    }

    suspend fun setBlockDating(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.BLOCK_DATING] = enabled
        }
    }

    suspend fun setStageDurationForTesting(durationMs: Long) {
        context.dataStore.edit { prefs ->
            prefs[PreferencesKeys.STAGE_DURATION_MS] = durationMs
        }
    }
}
