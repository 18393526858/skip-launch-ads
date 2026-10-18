package com.launchad.skip.data

data class SkipRule(
    val keyword: String,
    val builtin: Boolean = false,
    val enabled: Boolean = true,
)

data class SkipEvent(
    val packageName: String,
    val appLabel: String,
    val matchedText: String,
    val timestamp: Long,
    val result: SkipResult,
)

enum class SkipResult {
    CLICKED,
    UNCLICKABLE,
    BLOCKED_JUMP,
}

object DefaultRules {
    val all = listOf(
        SkipRule("跳过", builtin = true),
        SkipRule("跳过广告", builtin = true),
        SkipRule("跳过此广告", builtin = true),
        SkipRule("关闭广告", builtin = true),
        SkipRule("Skip", builtin = true),
        SkipRule("Skip Ad", builtin = true),
    )
}
