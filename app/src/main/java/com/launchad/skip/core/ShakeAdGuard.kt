package com.launchad.skip.core

import java.util.Locale

object ShakeAdDetector {
    private val hints = listOf(
        "摇一摇",
        "摇动手机",
        "摇晃手机",
        "扭一扭",
    )

    fun containsHint(candidates: Iterable<String?>): Boolean {
        return candidates.any { candidate ->
            if (candidate.isNullOrBlank()) {
                false
            } else {
                val normalized = candidate
                    .lowercase(Locale.ROOT)
                    .filter(Char::isLetterOrDigit)
                hints.any(normalized::contains)
            }
        }
    }
}

class ShakeJumpGuard(
    private val activeWindowMs: Long = 4_000L,
) {
    private var targetPackage: String? = null
    private var detectedAtMs: Long = 0L
    private var consumed = false

    fun recordHint(packageName: String, nowMs: Long) {
        targetPackage = packageName
        detectedAtMs = nowMs
        consumed = false
    }

    fun shouldSuppress(currentPackage: String, nowMs: Long): String? {
        val target = targetPackage ?: return null
        if (consumed || nowMs - detectedAtMs > activeWindowMs) return null
        if (currentPackage == target || !isLikelyJumpDestination(currentPackage)) return null

        consumed = true
        return target
    }

    fun reset() {
        targetPackage = null
        detectedAtMs = 0L
        consumed = false
    }

    private fun isLikelyJumpDestination(packageName: String): Boolean {
        if (packageName in jumpDestinations) return true
        val normalized = packageName.lowercase(Locale.ROOT)
        return normalized.endsWith(".browser") ||
            normalized.contains("appstore") ||
            normalized.contains("market")
    }

    private companion object {
        val jumpDestinations = setOf(
            "android",
            "com.android.intentresolver",
            "com.android.chrome",
            "com.chrome.beta",
            "com.sec.android.app.sbrowser",
            "com.android.browser",
            "com.miui.browser",
            "com.vivo.browser",
            "com.bbk.browser",
            "com.tencent.mtt",
            "com.UCMobile",
            "com.quark.browser",
            "com.baidu.searchbox",
            "com.microsoft.emmx",
            "org.mozilla.firefox",
            "com.android.vending",
            "com.vivo.appstore",
            "com.bbk.appstore",
            "com.xiaomi.market",
            "com.huawei.appmarket",
            "com.oppo.market",
            "com.heytap.market",
            "com.sec.android.app.samsungapps",
            "com.taobao.taobao",
            "com.tmall.wireless",
            "com.jingdong.app.mall",
            "com.xunmeng.pinduoduo",
            "com.achievo.vipshop",
            "com.suning.mobile.ebuy",
        )
    }
}
