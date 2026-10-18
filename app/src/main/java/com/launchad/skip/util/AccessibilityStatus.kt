package com.launchad.skip.util

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.ComponentName
import android.content.Context
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import com.launchad.skip.service.AdSkipAccessibilityService

fun isAccessibilityServiceEnabled(context: Context): Boolean {
    val component = ComponentName(context, AdSkipAccessibilityService::class.java)
    val enabledServices = Settings.Secure.getString(
        context.contentResolver,
        Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES,
    ).orEmpty()

    if (enabledServices.split(':').any { service ->
            service.equals(component.flattenToString(), ignoreCase = true) ||
                service.equals(component.flattenToShortString(), ignoreCase = true)
        }
    ) {
        return true
    }

    val manager = context.getSystemService(AccessibilityManager::class.java)
    return manager
        .getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        .any { info -> info.resolveInfo.serviceInfo.packageName == context.packageName }
}
