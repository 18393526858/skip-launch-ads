package com.launchad.skip.core

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager

class ProtectablePackageResolver(private val context: Context) {
    private val packageManager = context.packageManager

    fun loadLabels(): Map<String, String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        @Suppress("DEPRECATION")
        val activities = packageManager.queryIntentActivities(intent, 0)

        return activities
            .asSequence()
            .map { it.activityInfo.applicationInfo }
            .filter { applicationInfo ->
                applicationInfo.packageName != context.packageName &&
                    !isSystemOnly(applicationInfo) &&
                    !isSensitivePackage(applicationInfo.packageName)
            }
            .distinctBy(ApplicationInfo::packageName)
            .associate { applicationInfo ->
                applicationInfo.packageName to
                    packageManager.getApplicationLabel(applicationInfo).toString()
            }
    }

    fun loadHomePackages(): Set<String> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        @Suppress("DEPRECATION")
        val activities = packageManager.queryIntentActivities(
            intent,
            PackageManager.MATCH_DEFAULT_ONLY,
        )
        return activities
            .mapTo(mutableSetOf()) { it.activityInfo.packageName }
            .apply { addAll(commonHomePackages) }
    }

    private fun isSystemOnly(applicationInfo: ApplicationInfo): Boolean {
        val isSystem = applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM != 0
        val isUpdatedSystem = applicationInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
        return isSystem && !isUpdatedSystem
    }

    private fun isSensitivePackage(packageName: String): Boolean {
        return packageName.contains("permissioncontroller", ignoreCase = true) ||
            packageName in sensitivePackages
    }

    private companion object {
        val sensitivePackages = setOf(
            "android",
            "com.android.systemui",
            "com.android.settings",
            "com.google.android.settings",
        )
        val commonHomePackages = setOf(
            "com.android.launcher",
            "com.android.launcher3",
            "com.google.android.apps.nexuslauncher",
            "com.miui.home",
            "com.huawei.android.launcher",
            "com.sec.android.app.launcher",
            "com.oppo.launcher",
            "com.heytap.launcher",
            "com.bbk.launcher",
            "com.bbk.launcher2",
            "com.bbk.launcher3",
            "com.vivo.hiboard",
            "com.vivo.launcher",
            "com.vivo.launcher6",
            "com.coloros.launcher",
            "com.hihonor.android.launcher",
        )
    }
}
