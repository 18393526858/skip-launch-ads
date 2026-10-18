package com.launchad.skip.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.skipDataStore by preferencesDataStore(name = "launch_ad_skip")

class SkipRuleStore(context: Context) {
    private val dataStore = context.applicationContext.skipDataStore

    val rules: Flow<List<SkipRule>> = dataStore.data.map { preferences ->
        decodeRules(preferences[RULES]) ?: DefaultRules.all
    }

    val events: Flow<List<SkipEvent>> = dataStore.data.map { preferences ->
        decodeEvents(preferences[EVENTS])
    }

    val protectionEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        if (preferences[SAFETY_VERSION] != CURRENT_SAFETY_VERSION) {
            false
        } else {
            preferences[PROTECTION_ENABLED] ?: false
        }
    }

    suspend fun updateRules(transform: (List<SkipRule>) -> List<SkipRule>) {
        dataStore.edit { preferences ->
            val current = decodeRules(preferences[RULES]) ?: DefaultRules.all
            preferences[RULES] = encodeRules(transform(current))
        }
    }

    suspend fun appendEvent(event: SkipEvent) {
        dataStore.edit { preferences ->
            val events = decodeEvents(preferences[EVENTS])
            preferences[EVENTS] = encodeEvents((listOf(event) + events).take(MAX_EVENTS))
        }
    }

    suspend fun setProtectionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[PROTECTION_ENABLED] = enabled
        }
    }

    suspend fun ensureSafeUpgradeDefaults() {
        dataStore.edit { preferences ->
            if (preferences[SAFETY_VERSION] != CURRENT_SAFETY_VERSION) {
                preferences[PROTECTION_ENABLED] = false
                preferences[SAFETY_VERSION] = CURRENT_SAFETY_VERSION
            }
        }
    }

    private fun decodeRules(raw: String?): List<SkipRule>? {
        if (raw == null) return null
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val keyword = item.optString("keyword").trim()
                    if (keyword.isBlank()) continue
                    add(
                        SkipRule(
                            keyword = keyword,
                            builtin = item.optBoolean("builtin", false),
                            enabled = item.optBoolean("enabled", true),
                        ),
                    )
                }
            }.distinctBy { it.keyword.lowercase() }
        }.getOrDefault(emptyList())
    }

    private fun encodeRules(rules: List<SkipRule>): String {
        val array = JSONArray()
        rules.forEach { rule ->
            array.put(
                JSONObject()
                    .put("keyword", rule.keyword)
                    .put("builtin", rule.builtin)
                    .put("enabled", rule.enabled),
            )
        }
        return array.toString()
    }

    private fun decodeEvents(raw: String?): List<SkipEvent> {
        if (raw.isNullOrBlank()) return emptyList()
        return runCatching {
            val array = JSONArray(raw)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.getJSONObject(index)
                    val packageName = item.optString("packageName")
                    if (packageName.isBlank()) continue
                    add(
                        SkipEvent(
                            packageName = packageName,
                            appLabel = item.optString("appLabel", packageName),
                            matchedText = item.optString("matchedText"),
                            timestamp = item.optLong("timestamp"),
                            result = runCatching {
                                SkipResult.valueOf(item.optString("result", SkipResult.CLICKED.name))
                            }.getOrDefault(SkipResult.CLICKED),
                        ),
                    )
                }
            }
        }.getOrDefault(emptyList())
    }

    private fun encodeEvents(events: List<SkipEvent>): String {
        val array = JSONArray()
        events.forEach { event ->
            array.put(
                JSONObject()
                    .put("packageName", event.packageName)
                    .put("appLabel", event.appLabel)
                    .put("matchedText", event.matchedText)
                    .put("timestamp", event.timestamp)
                    .put("result", event.result.name),
            )
        }
        return array.toString()
    }

    private companion object {
        const val MAX_EVENTS = 50
        val RULES = stringPreferencesKey("rules")
        val EVENTS = stringPreferencesKey("events")
        val PROTECTION_ENABLED = booleanPreferencesKey("protection_enabled")
        val SAFETY_VERSION = stringPreferencesKey("safety_version")
        const val CURRENT_SAFETY_VERSION = "2"
    }
}
