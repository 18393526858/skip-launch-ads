package com.launchad.skip.core

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SkipClickGuardTest {
    @Test
    fun `accepts compact control anywhere on screen`() {
        assertTrue(
            SkipClickGuard.isSafe(
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
    fun `rejects oversized clickable container without its own skip label`() {
        assertFalse(
            SkipClickGuard.isSafe(
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
    fun `accepts full screen clickable control with explicit skip label`() {
        assertTrue(
            SkipClickGuard.isSafe(
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
