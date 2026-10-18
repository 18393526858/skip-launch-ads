package com.launchad.skip

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.launchad.skip.ui.LaunchAdSkipApp
import com.launchad.skip.ui.LaunchAdSkipTheme

class MainActivity : ComponentActivity() {
    private val viewModel: SkipAppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            LaunchAdSkipTheme {
                val lifecycleOwner = LocalLifecycleOwner.current
                DisposableEffect(lifecycleOwner) {
                    val observer = LifecycleEventObserver { _, event ->
                        if (event == Lifecycle.Event.ON_RESUME) {
                            viewModel.refreshRuntimeState()
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)
                    onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
                }

                val serviceEnabled by viewModel.serviceEnabled.collectAsStateWithLifecycle()
                val protectionEnabled by viewModel.protectionEnabled.collectAsStateWithLifecycle()
                val rules by viewModel.rules.collectAsStateWithLifecycle()
                val events by viewModel.events.collectAsStateWithLifecycle()

                LaunchAdSkipApp(
                    serviceEnabled = serviceEnabled,
                    protectionEnabled = protectionEnabled,
                    rules = rules,
                    events = events,
                    onProtectionEnabledChange = viewModel::setProtectionEnabled,
                    onAddRule = viewModel::addRule,
                    onRuleEnabledChange = viewModel::setRuleEnabled,
                    onDeleteRule = viewModel::deleteRule,
                    onResetRules = viewModel::resetRules,
                )
            }
        }
    }
}
