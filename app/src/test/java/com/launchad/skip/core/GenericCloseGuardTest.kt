package com.launchad.skip.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericCloseGuardTest {
    @Test
    fun `recognizes exact generic close labels`() {
        assertTrue(AdSignals.isGenericClose(listOf("关闭")))
        assertTrue(AdSignals.isGenericClose(listOf("Close")))
        assertFalse(AdSignals.isGenericClose(listOf("关闭广告")))
        assertFalse(AdSignals.isGenericClose(listOf("退出登录")))
    }

    @Test
    fun `accepts compact ad close anywhere on screen`() {
        assertTrue(
            GenericCloseGuard.isSafe(
                screenWidth = 1080,
                screenHeight = 2400,
                left = 450,
                top = 1100,
                right = 650,
                bottom = 1220,
                clickableNodeMatches = false,
            ),
        )
    }

    @Test
    fun `rejects oversized ad container without close label`() {
        assertFalse(
            GenericCloseGuard.isSafe(
                screenWidth = 1080,
                screenHeight = 2400,
                left = 0,
                top = 0,
                right = 1080,
                bottom = 2400,
                clickableNodeMatches = false,
            ),
        )
    }

    @Test
    fun `accepts full screen close control with explicit close label`() {
        assertTrue(
            GenericCloseGuard.isSafe(
                screenWidth = 1080,
                screenHeight = 2400,
                left = 0,
                top = 0,
                right = 1080,
                bottom = 2400,
                clickableNodeMatches = true,
            ),
        )
    }
}
