package com.launchad.skip.core

class AdSkipEngine(
    val detectionWindowMs: Long = 12_000L,
    val scanIntervalMs: Long = 250L,
) {
    private var lastForegroundKey: String? = null
    private var armedPackage: String? = null
    private var armedUntilMs: Long = 0L
    private var lastScanAtMs: Long = Long.MIN_VALUE
    private var clickedForLaunch = false
    private var unclickableReported = false

    fun onForegroundChange(
        foregroundKey: String?,
        targetPackage: String?,
        nowMs: Long,
    ): Boolean {
        if (foregroundKey == lastForegroundKey) return false
        lastForegroundKey = foregroundKey
        if (targetPackage == null || foregroundKey == null) return false

        armedPackage = targetPackage
        armedUntilMs = nowMs + detectionWindowMs
        lastScanAtMs = Long.MIN_VALUE
        clickedForLaunch = false
        unclickableReported = false
        return true
    }

    fun canScan(packageName: String?, nowMs: Long): Boolean {
        if (packageName == null || packageName != armedPackage) return false
        if (clickedForLaunch || nowMs > armedUntilMs) return false
        if (lastScanAtMs != Long.MIN_VALUE && nowMs - lastScanAtMs < scanIntervalMs) return false
        lastScanAtMs = nowMs
        return true
    }

    fun canClick(): Boolean {
        return !clickedForLaunch && armedPackage != null
    }

    fun markClicked(nowMs: Long) {
        clickedForLaunch = true
        lastScanAtMs = nowMs
    }

    fun shouldReportUnclickable(): Boolean {
        return !clickedForLaunch && !unclickableReported
    }

    fun markUnclickableReported() {
        unclickableReported = true
    }

    fun reset() {
        lastForegroundKey = null
        armedPackage = null
        armedUntilMs = 0L
        lastScanAtMs = Long.MIN_VALUE
        clickedForLaunch = false
        unclickableReported = false
    }
}
