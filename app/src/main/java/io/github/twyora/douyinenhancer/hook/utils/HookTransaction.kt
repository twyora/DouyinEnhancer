package io.github.twyora.douyinenhancer.hook.utils

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator.MemberHookCreator
import com.highcapable.yukihookapi.hook.log.YLog

class HookTransaction(private val tag: String?) {
    private val hookActions = mutableListOf<Pair<String, () -> MemberHookCreator.Result?>>()
    private val hookResult = mutableListOf<MemberHookCreator.Result>()

    private fun rollback() {
        hookResult.asReversed().forEachIndexed { index, result ->
            runCatching {
                result.remove()
            }.onFailure { e ->
                YLog.error(
                    "$tag: Hook at index $index rollback failed, ignore it. Stack trace: ",
                    e
                )
            }
        }
        hookResult.clear()
    }

    fun reset() {
        hookActions.clear()
        hookResult.clear()
    }

    fun add(name: String, action: () -> MemberHookCreator.Result?) {
        hookActions.add(Pair(name, action))
    }

    fun commit(): Boolean {
        var hasFailure = false
        for ((name, action) in hookActions) {
            if (hasFailure) {
                break
            }

            try {
                action()?.also {
                    hookResult.add(it)
                } ?: run {
                    hasFailure = true
                    YLog.error(
                        "$tag: $name returned null, considered as hook failed. The remaining hooks will not be executed and rollback will be performed."
                    )
                }
            } catch (e: Exception) {
                hasFailure = true
                YLog.error(
                    "$tag: $name threw an exception! The remaining hooks will not be executed and rollback will be performed. Stack trace: ",
                    e
                )
            }
        }

        if (hasFailure) {
            rollback()
        }

        return !hasFailure
    }
}
