package com.launchad.skip.core

import com.launchad.skip.data.DefaultRules
import com.launchad.skip.data.SkipRule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SkipMatcherTest {
    @Test
    fun `matches exact Chinese skip label`() {
        val match = SkipMatcher.findMatch(listOf("跳过"), DefaultRules.all)

        assertEquals("跳过", match?.keyword)
    }

    @Test
    fun `matches Chinese label split from countdown`() {
        val match = SkipMatcher.findMatch(listOf("5S | 跳过广告"), DefaultRules.all)

        assertEquals("跳过广告", match?.keyword)
    }

    @Test
    fun `matches English label with trailing countdown`() {
        val match = SkipMatcher.findMatch(listOf("  Skip Ad 5s "), DefaultRules.all)

        assertEquals("Skip Ad", match?.keyword)
    }

    @Test
    fun `matches explicit close ad label`() {
        val match = SkipMatcher.findMatch(listOf("关闭广告"), DefaultRules.all)

        assertEquals("关闭广告", match?.keyword)
    }

    @Test
    fun `does not match tutorial wording`() {
        assertNull(SkipMatcher.findMatch(listOf("跳过教程"), DefaultRules.all))
    }

    @Test
    fun `does not match generic close control`() {
        assertNull(SkipMatcher.findMatch(listOf("关闭"), DefaultRules.all))
    }

    @Test
    fun `does not match unrelated English control`() {
        assertNull(SkipMatcher.findMatch(listOf("Skip intro"), DefaultRules.all))
    }

    @Test
    fun `ignores disabled rules`() {
        val rules = listOf(SkipRule("跳过", enabled = false))

        assertNull(SkipMatcher.findMatch(listOf("跳过"), rules))
    }

    @Test
    fun `supports custom content description`() {
        val rules = listOf(SkipRule("广告跳过按钮"))

        val match = SkipMatcher.findMatch(
            listOf(null, "广告跳过按钮 3秒"),
            rules,
        )

        assertEquals("广告跳过按钮", match?.keyword)
    }
}
