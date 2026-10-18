package com.launchad.skip

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.launchad.skip.data.DefaultRules
import com.launchad.skip.data.SkipRule
import com.launchad.skip.data.SkipRuleStore
import com.launchad.skip.util.isAccessibilityServiceEnabled
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SkipAppViewModel(application: Application) : AndroidViewModel(application) {
    private val store = SkipRuleStore(application)

    val rules: StateFlow<List<SkipRule>> = store.rules.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        DefaultRules.all,
    )

    val events = store.events.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList(),
    )

    private val _serviceEnabled = MutableStateFlow(false)
    val serviceEnabled: StateFlow<Boolean> = _serviceEnabled.asStateFlow()

    val protectionEnabled: StateFlow<Boolean> = store.protectionEnabled.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        false,
    )

    init {
        refreshRuntimeState()
        viewModelScope.launch {
            store.ensureSafeUpgradeDefaults()
        }
    }

    fun refreshRuntimeState() {
        val context = getApplication<Application>()
        _serviceEnabled.value = isAccessibilityServiceEnabled(context)
    }

    fun addRule(keyword: String) {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            store.updateRules { current ->
                val normalized = trimmed.lowercase(Locale.ROOT)
                if (current.any { it.keyword.lowercase(Locale.ROOT) == normalized }) {
                    current.map {
                        if (it.keyword.lowercase(Locale.ROOT) == normalized) {
                            it.copy(enabled = true)
                        } else {
                            it
                        }
                    }
                } else {
                    current + SkipRule(trimmed)
                }
            }
        }
    }

    fun setRuleEnabled(rule: SkipRule, enabled: Boolean) {
        viewModelScope.launch {
            store.updateRules { current ->
                current.map {
                    if (it.keyword.equals(rule.keyword, ignoreCase = true)) {
                        it.copy(enabled = enabled)
                    } else {
                        it
                    }
                }
            }
        }
    }

    fun deleteRule(rule: SkipRule) {
        viewModelScope.launch {
            store.updateRules { current ->
                current.filterNot { it.keyword.equals(rule.keyword, ignoreCase = true) }
            }
        }
    }

    fun resetRules() {
        viewModelScope.launch {
            store.updateRules { DefaultRules.all }
        }
    }

    fun setProtectionEnabled(enabled: Boolean) {
        viewModelScope.launch {
            store.setProtectionEnabled(enabled)
        }
    }
}
