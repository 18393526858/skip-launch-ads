package com.launchad.skip.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdSkipEngineTest {
    @Test
    fun `arms only when foreground key changes to target`() {
        val engine = AdSkipEngine()

        assertTrue(engine.onForegroundChange("com.example", "com.example", 1_000L))
        assertFalse(engine.onForegroundChange("com.example", "com.example", 1_100L))
        assertFalse(engine.onForegroundChange("com.other", null, 1_200L))
    }

    @Test
    fun `throttles scans to configured interval`() {
        val engine = AdSkipEngine(scanIntervalMs = 250L)
        engine.onForegroundChange("com.example", "com.example", 1_000L)

        assertTrue(engine.canScan("com.example", 1_000L))
        assertFalse(engine.canScan("com.example", 1_100L))
        assertTrue(engine.canScan("com.example", 1_250L))
    }

    @Test
    fun `does not scan after launch window expires`() {
        val engine = AdSkipEngine(detectionWindowMs = 12_000L)
        engine.onForegroundChange("com.example", "com.example", 1_000L)

        assertTrue(engine.canScan("com.example", 13_000L))
        assertFalse(engine.canScan("com.example", 13_001L))
    }

    @Test
    fun `allows only one click for each launch`() {
        val engine = AdSkipEngine()
        engine.onForegroundChange("com.example", "com.example", 1_000L)

        assertTrue(engine.canClick())
        engine.markClicked(1_100L)

        assertFalse(engine.canClick())
        assertFalse(engine.canScan("com.example", 1_500L))
    }

    @Test
    fun `rearms after leaving and returning to target`() {
        val engine = AdSkipEngine()
        engine.onForegroundChange("com.example", "com.example", 1_000L)
        engine.markClicked(1_100L)
        engine.onForegroundChange("com.other", null, 2_000L)

        assertTrue(engine.onForegroundChange("com.example", "com.example", 3_000L))
        assertTrue(engine.canClick())
    }

    @Test
    fun `reports an unclickable match only once per launch`() {
        val engine = AdSkipEngine()
        engine.onForegroundChange("com.example", "com.example", 1_000L)

        assertTrue(engine.shouldReportUnclickable())
        engine.markUnclickableReported()
        assertFalse(engine.shouldReportUnclickable())
    }
}
