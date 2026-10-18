package com.launchad.skip.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AdSignalsTest {
    @Test
    fun `detects countdown formats`() {
        assertTrue(AdSignals.hasCountdown(listOf("3s 跳过")))
        assertTrue(AdSignals.hasCountdown(listOf("5秒后关闭")))
        assertFalse(AdSignals.hasCountdown(listOf("跳过教程")))
    }

    @Test
    fun `detects explicit ad markers`() {
        assertTrue(AdSignals.hasAdMarker(listOf("广告")))
        assertTrue(AdSignals.hasAdMarker(listOf("AD")))
        assertTrue(AdSignals.hasAdMarker(listOf("Sponsored")))
        assertFalse(AdSignals.hasAdMarker(listOf("Skip")))
    }

    @Test
    fun `classifies strong and weak rules`() {
        assertTrue(AdSignals.isStrongRule("跳过广告"))
        assertTrue(AdSignals.isStrongRule("Skip Ad"))
        assertFalse(AdSignals.isStrongRule("跳过"))
        assertFalse(AdSignals.isStrongRule("Skip"))
    }
}
