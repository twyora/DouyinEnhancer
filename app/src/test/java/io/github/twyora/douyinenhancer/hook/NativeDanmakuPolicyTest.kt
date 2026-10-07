package io.github.twyora.douyinenhancer.hook

import org.junit.Assert.assertEquals
import org.junit.Test

class NativeDanmakuPolicyTest {
    @Test
    fun acceptsSingleNativeResourceAcrossResourceTables() {
        listOf(0x7f0a1234, 0x7f0b4321).forEach { id ->
            assertEquals(listOf(id), NativeCleanModeSymbols.danmakuWhiteList(listOf(id)))
        }
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsGeneratedViewIds() {
        NativeCleanModeSymbols.danmakuWhiteList(listOf(0x00123456))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsPolicyWithUnrelatedWidgets() {
        NativeCleanModeSymbols.danmakuWhiteList(listOf(0x7f0a1234, 0x7f0a4321))
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsEmptyPolicy() {
        NativeCleanModeSymbols.danmakuWhiteList(emptyList<Any>())
    }

    @Test(expected = IllegalStateException::class)
    fun rejectsUnexpectedValueType() {
        NativeCleanModeSymbols.danmakuWhiteList(listOf(0x7f0a1234L))
    }
}
