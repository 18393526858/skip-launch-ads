package com.launchad.skip.core

import java.util.Locale

object AdSignals {
    private val countdown = Regex("\\d{1,2}\\s*(?:s|秒)", RegexOption.IGNORE_CASE)

    fun hasCountdown(candidates: Iterable<String?>): Boolean {
        return candidates.any { candidate ->
            candidate != null && countdown.containsMatchIn(candidate)
        }
    }

    fun hasAdMarker(candidates: Iterable<String?>): Boolean {
        return candidates.any { candidate ->
            if (candidate.isNullOrBlank()) {
                false
            } else {
                val normalized = candidate
                    .lowercase(Locale.ROOT)
                    .filter(Char::isLetterOrDigit)
                normalized.contains("广告") ||
                    normalized == "ad" ||
                    normalized == "sponsored"
            }
        }
    }

    fun isStrongRule(keyword: String): Boolean {
        val normalized = keyword
            .lowercase(Locale.ROOT)
            .filter(Char::isLetterOrDigit)
        return normalized.contains("广告") ||
            normalized == "skipad" ||
            normalized == "closead"
    }

    fun isGenericClose(candidates: Iterable<String?>): Boolean {
        return candidates.any { candidate ->
            if (candidate == null) {
                false
            } else {
                val normalized = candidate
                    .lowercase(Locale.ROOT)
                    .filter(Char::isLetterOrDigit)
                normalized in genericCloseLabels
            }
        }
    }

    private val genericCloseLabels = setOf(
        "关闭",
        "关闭按钮",
        "close",
    )
}

object GenericCloseGuard {
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

        val compact = width <= screenWidth * 0.65f &&
            height <= screenHeight * 0.35f

        return compact || clickableNodeMatches
    }
}
