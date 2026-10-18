package com.launchad.skip.core

object SkipClickGuard {
    fun isSafe(
        screenWidth: Int,
        screenHeight: Int,
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        clickableNodeMatches: Boolean,
    ): Boolean {
        if (screenWidth <= 0 || screenHeight <= 0) return false
        val width = right - left
        val height = bottom - top
        if (width <= 0 || height <= 0) return false

        val compactControl = width <= screenWidth * 0.6f &&
            height <= screenHeight * 0.3f

        return compactControl || clickableNodeMatches
    }
}
