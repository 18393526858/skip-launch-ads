package com.launchad.skip.service

import android.accessibilityservice.AccessibilityService
import android.graphics.Rect
import android.os.SystemClock
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import com.launchad.skip.BuildConfig
import com.launchad.skip.core.AdSignals
import com.launchad.skip.core.GenericCloseGuard
import com.launchad.skip.core.AdSkipEngine
import com.launchad.skip.core.ProtectablePackageResolver
import com.launchad.skip.core.ShakeAdDetector
import com.launchad.skip.core.ShakeJumpGuard
import com.launchad.skip.core.SkipMatch
import com.launchad.skip.core.SkipClickGuard
import com.launchad.skip.core.SkipMatcher
import com.launchad.skip.data.SkipEvent
import com.launchad.skip.data.SkipResult
import com.launchad.skip.data.SkipRule
import com.launchad.skip.data.SkipRuleStore
import com.launchad.skip.demo.DemoSplashActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AdSkipAccessibilityService : AccessibilityService() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val engine = AdSkipEngine(
        detectionWindowMs = 10_000L,
        scanIntervalMs = 150L,
    )
    private val shakeGuard = ShakeJumpGuard()
    private lateinit var store: SkipRuleStore
    private var suppressReturnPackage: String? = null
    private var lastWindowPackage: String? = null
    private var scanJob: Job? = null

    @Volatile
    private var protectableApps: Map<String, String> = emptyMap()

    @Volatile
    private var homePackages: Set<String> = emptySet()

    @Volatile
    private var rules: List<SkipRule> = emptyList()

    @Volatile
    private var protectionEnabled: Boolean = false

    override fun onServiceConnected() {
        super.onServiceConnected()
        store = SkipRuleStore(this)
        val packageResolver = ProtectablePackageResolver(this)
        protectableApps = packageResolver.loadLabels()
        homePackages = packageResolver.loadHomePackages()
        lastWindowPackage = rootInActiveWindow?.packageName?.toString()
        engine.reset()
        shakeGuard.reset()
        suppressReturnPackage = null
        serviceScope.launch {
            store.rules.collect { currentRules ->
                rules = currentRules
            }
        }
        serviceScope.launch {
            store.ensureSafeUpgradeDefaults()
            store.protectionEnabled.collect { enabled ->
                protectionEnabled = enabled
                if (!enabled) engine.reset()
            }
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || !::store.isInitialized || !protectionEnabled) return
        val packageName = event.packageName?.toString() ?: return
        val className = event.className?.toString().orEmpty()
        val now = SystemClock.elapsedRealtime()

        when (event.eventType) {
            AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED -> {
                val isDebugDemo = BuildConfig.DEBUG &&
                    packageName == this.packageName &&
                    className == DemoSplashActivity::class.java.name
                val launchedFromHome = lastWindowPackage == null ||
                    lastWindowPackage in homePackages ||
                    lastWindowPackage == "com.android.systemui"
                scheduleShakeJumpSuppression(packageName, now)
                val isSuppressedReturn = packageName == suppressReturnPackage
                if (isSuppressedReturn) suppressReturnPackage = null
                val targetPackage = when {
                    isDebugDemo -> packageName
                    launchedFromHome &&
                        !isSuppressedReturn &&
                        packageName in protectableApps -> packageName
                    else -> null
                }
                val foregroundKey = if (isDebugDemo) "$packageName#$className" else packageName
                val armed = engine.onForegroundChange(foregroundKey, targetPackage, now)
                if (armed && targetPackage != null) {
                    startScanLoop(targetPackage)
                }
                lastWindowPackage = packageName
            }

            AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED,
            AccessibilityEvent.TYPE_WINDOWS_CHANGED,
            -> scanWindow(packageName, now)

            else -> Unit
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        shakeGuard.reset()
        scanJob?.cancel()
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun startScanLoop(packageName: String) {
        scanJob?.cancel()
        scanJob = serviceScope.launch {
            repeat(SCAN_LOOP_ITERATIONS) {
                delay(SCAN_LOOP_DELAY_MS)
                if (!protectionEnabled) return@launch
                scanWindow(packageName, SystemClock.elapsedRealtime())
            }
        }
    }

    private fun scanWindow(packageName: String, now: Long) {
        if (!engine.canScan(packageName, now)) return
        val root = rootInActiveWindow ?: return
        if (root.packageName?.toString() != packageName) return

        val scan = inspectWindow(root)
        if (scan.hasShakeHint) {
            shakeGuard.recordHint(packageName, now)
        }
        val result = scan.target ?: return

        val clicked = engine.canClick() &&
            result.clickableNode.performAction(AccessibilityNodeInfo.ACTION_CLICK)

        val appLabel = if (BuildConfig.DEBUG && packageName == this.packageName) {
            "内置测试广告"
        } else {
            protectableApps[packageName] ?: packageName
        }

        if (clicked) {
            engine.markClicked(now)
            recordEvent(packageName, appLabel, result.match.displayText, SkipResult.CLICKED)
        } else if (engine.shouldReportUnclickable()) {
            engine.markUnclickableReported()
            recordEvent(packageName, appLabel, result.match.displayText, SkipResult.UNCLICKABLE)
        }
    }

    private fun inspectWindow(root: AccessibilityNodeInfo): WindowScan {
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        var unclickableFallback: ClickTarget? = null
        var weakClickableFallback: ClickTarget? = null
        var hasShakeHint = false
        var hasCountdown = false
        var hasAdMarker = false
        var genericCloseTarget: ClickTarget? = null
        val screenBounds = Rect().also(root::getBoundsInScreen)
        val display = resources.displayMetrics
        val isFullScreenWindow =
            screenBounds.width() >= display.widthPixels * 0.9f &&
                screenBounds.height() >= display.heightPixels * 0.9f

        while (queue.isNotEmpty() && visited < MAX_NODES) {
            val node = queue.removeFirst()
            visited += 1
            if (!node.isVisibleToUser) continue

            val candidates = listOf(
                node.text?.toString(),
                node.contentDescription?.toString(),
            )
            if (ShakeAdDetector.containsHint(candidates)) {
                hasShakeHint = true
            }
            if (AdSignals.hasCountdown(candidates)) hasCountdown = true
            if (AdSignals.hasAdMarker(candidates)) hasAdMarker = true
            val match = SkipMatcher.findMatch(candidates, rules)
            if (match != null) {
                val clickableNode = findClickableNode(node)
                if (clickableNode != null) {
                    val clickableBounds = Rect().also(clickableNode::getBoundsInScreen)
                    val clickableNodeMatches = SkipMatcher.findMatch(
                        candidates = listOf(
                            clickableNode.text?.toString(),
                            clickableNode.contentDescription?.toString(),
                        ),
                        rules = rules,
                    ) != null
                    if (!SkipClickGuard.isSafe(
                            screenWidth = screenBounds.width(),
                            screenHeight = screenBounds.height(),
                            left = clickableBounds.left,
                            top = clickableBounds.top,
                            right = clickableBounds.right,
                            bottom = clickableBounds.bottom,
                            clickableNodeMatches = clickableNodeMatches,
                        )
                    ) {
                        continue
                    }
                    val clickTarget = ClickTarget(clickableNode, match)
                    if (AdSignals.isStrongRule(match.keyword) || hasCountdown || hasAdMarker) {
                        return WindowScan(
                            target = clickTarget,
                            hasShakeHint = hasShakeHint,
                        )
                    }
                    if (weakClickableFallback == null) {
                        weakClickableFallback = clickTarget
                    }
                }
                if (unclickableFallback == null) {
                    unclickableFallback = ClickTarget(node, match)
                }
            } else if (genericCloseTarget == null &&
                AdSignals.isGenericClose(candidates)
            ) {
                val clickableNode = findClickableNode(node)
                if (clickableNode != null) {
                    val clickableBounds = Rect().also(clickableNode::getBoundsInScreen)
                    val clickableNodeMatches = AdSignals.isGenericClose(
                        listOf(
                            clickableNode.text?.toString(),
                            clickableNode.contentDescription?.toString(),
                        ),
                    )
                    if (GenericCloseGuard.isSafe(
                            screenWidth = screenBounds.width(),
                            screenHeight = screenBounds.height(),
                            left = clickableBounds.left,
                            top = clickableBounds.top,
                            right = clickableBounds.right,
                            bottom = clickableBounds.bottom,
                            clickableNodeMatches = clickableNodeMatches,
                        )
                    ) {
                        genericCloseTarget = ClickTarget(
                            clickableNode = clickableNode,
                            match = SkipMatch(
                                displayText = candidates.firstOrNull { it != null } ?: "关闭",
                                keyword = "关闭",
                            ),
                        )
                    }
                }
            }

            for (index in 0 until node.childCount) {
                node.getChild(index)?.let(queue::addLast)
            }
        }
        return WindowScan(
            target = weakClickableFallback
                ?.takeIf { hasCountdown || hasAdMarker || isFullScreenWindow }
                ?: genericCloseTarget?.takeIf { hasAdMarker || isFullScreenWindow }
                ?: unclickableFallback,
            hasShakeHint = hasShakeHint,
        )
    }

    private fun scheduleShakeJumpSuppression(currentPackage: String, now: Long) {
        val targetPackage = shakeGuard.shouldSuppress(currentPackage, now) ?: return
        suppressReturnPackage = targetPackage
        serviceScope.launch {
            delay(160L)
            if (rootInActiveWindow?.packageName?.toString() != currentPackage) return@launch
            if (performGlobalAction(GLOBAL_ACTION_BACK)) {
                recordEvent(
                    packageName = targetPackage,
                    appLabel = protectableApps[targetPackage] ?: targetPackage,
                    matchedText = "摇一摇跳转",
                    result = SkipResult.BLOCKED_JUMP,
                )
            }
        }
    }

    private fun findClickableNode(source: AccessibilityNodeInfo): AccessibilityNodeInfo? {
        var node: AccessibilityNodeInfo? = source
        var depth = 0
        while (node != null && depth <= MAX_PARENT_DEPTH) {
            if (node.isClickable && node.isEnabled) {
                node.refresh()
                return node
            }
            node = node.parent
            depth += 1
        }
        return null
    }

    private fun recordEvent(
        packageName: String,
        appLabel: String,
        matchedText: String,
        result: SkipResult,
    ) {
        serviceScope.launch {
            store.appendEvent(
                SkipEvent(
                    packageName = packageName,
                    appLabel = appLabel,
                    matchedText = matchedText,
                    timestamp = System.currentTimeMillis(),
                    result = result,
                ),
            )
        }
    }

    private data class ClickTarget(
        val clickableNode: AccessibilityNodeInfo,
        val match: SkipMatch,
    )

    private data class WindowScan(
        val target: ClickTarget?,
        val hasShakeHint: Boolean,
    )

    private companion object {
        const val MAX_NODES = 600
        const val MAX_PARENT_DEPTH = 6
        const val SCAN_LOOP_DELAY_MS = 150L
        const val SCAN_LOOP_ITERATIONS = 70
    }
}
