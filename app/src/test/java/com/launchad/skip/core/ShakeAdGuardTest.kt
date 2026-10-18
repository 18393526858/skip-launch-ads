package com.launchad.skip.core

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ShakeAdGuardTest {
    @Test
    fun `detects common shake ad hints`() {
        assertTrue(ShakeAdDetector.containsHint(listOf("摇一摇跳转详情")))
        assertTrue(ShakeAdDetector.containsHint(listOf("摇晃手机 打开广告")))
        assertFalse(ShakeAdDetector.containsHint(listOf("点击跳过广告")))
    }

    @Test
    fun `suppresses a quick jump to browser`() {
        val guard = ShakeJumpGuard()
        guard.recordHint("com.example.news", 1_000L)

        assertEquals(
            "com.example.news",
            guard.shouldSuppress("com.android.chrome", 2_000L),
        )
    }

    @Test
    fun `does not suppress unrelated foreground changes`() {
        val guard = ShakeJumpGuard()
        guard.recordHint("com.example.news", 1_000L)

        assertNull(guard.shouldSuppress("com.example.editor", 2_000L))
        assertNull(guard.shouldSuppress("com.example.news", 2_000L))
    }

    @Test
    fun `does not suppress after the guard window expires`() {
        val guard = ShakeJumpGuard(activeWindowMs = 4_000L)
        guard.recordHint("com.example.news", 1_000L)

        assertNull(guard.shouldSuppress("com.android.chrome", 5_001L))
    }

    @Test
    fun `suppresses only once per detected hint`() {
        val guard = ShakeJumpGuard()
        guard.recordHint("com.example.news", 1_000L)

        assertEquals(
            "com.example.news",
            guard.shouldSuppress("com.android.chrome", 2_000L),
        )
        assertNull(guard.shouldSuppress("com.android.vending", 2_100L))
    }

    @Test
    fun `a new shake hint rearms the guard`() {
        val guard = ShakeJumpGuard()
        guard.recordHint("com.example.news", 1_000L)
        guard.shouldSuppress("com.android.chrome", 2_000L)
        guard.recordHint("com.example.news", 3_000L)

        assertEquals(
            "com.example.news",
            guard.shouldSuppress("com.android.vending", 3_500L),
        )
    }
}
