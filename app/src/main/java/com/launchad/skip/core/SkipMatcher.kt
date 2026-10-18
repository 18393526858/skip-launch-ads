package com.launchad.skip.core

import com.launchad.skip.data.SkipRule
import java.util.Locale
import java.util.regex.Pattern

data class SkipMatch(
    val displayText: String,
    val keyword: String,
)

object SkipMatcher {
    private val trailingCountdown = Pattern.compile(
        "(?:\\s*[·|•/\\-]?\\s*[（(]?\\d{1,2}\\s*(?:s|秒)?[)）]?\\s*)$",
        Pattern.CASE_INSENSITIVE,
    )
    private val leadingCountdown = Pattern.compile(
        "^\\s*[（(]?\\d{1,2}\\s*(?:s|秒)?[)）]?\\s*(?:后)?\\s*[·|•/\\-]?\\s*",
        Pattern.CASE_INSENSITIVE,
    )

    fun findMatch(candidates: Iterable<String?>, rules: Iterable<SkipRule>): SkipMatch? {
        val activeRules = rules
            .filter(SkipRule::enabled)
            .map { it.keyword to normalize(it.keyword) }
            .filter { it.second.isNotEmpty() }

        for (candidate in candidates) {
            if (candidate.isNullOrBlank()) continue
            val trimmed = candidate.trim()
            val variants = buildSet {
                add(trimmed)
                add(trailingCountdown.matcher(trimmed).replaceFirst(""))
                add(leadingCountdown.matcher(trimmed).replaceFirst(""))
            }

            for (variant in variants) {
                val normalized = normalize(variant)
                if (normalized.isEmpty()) continue
                activeRules.firstOrNull { (_, normalizedRule) -> normalizedRule == normalized }
                    ?.let { (keyword, _) -> return SkipMatch(trimmed, keyword) }
            }
        }
        return null
    }

    internal fun normalize(value: String): String {
        return value
            .lowercase(Locale.ROOT)
            .filter(Char::isLetterOrDigit)
    }
}
