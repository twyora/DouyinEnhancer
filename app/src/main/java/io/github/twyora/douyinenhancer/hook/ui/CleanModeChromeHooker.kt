package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import java.lang.reflect.Method
import java.util.WeakHashMap

/** Keep host animation callbacks from showing home chrome while our command owns the page. */
@HookOnMainProcess
object CleanModeChromeHooker : YukiBaseHooker() {
    private const val TAG = "CleanModeChromeHooker"
    private val suppressed = WeakHashMap<View, Method>()

    override fun onHook() {
        if (!ConfigManager.ui.cleanMode.value) return
        runCatching {
            val chrome = DouyinPackage.instance.cleanModeChrome
            val top = checkNotNull(chrome.topViewClass).resolveMethod(chrome.applyTopVisibility())
            val bottom = checkNotNull(chrome.bottomViewClass).resolveMethod(chrome.applyBottomVisibility())
            val ui = checkNotNull(checkNotNull(chrome.uiServiceClass).getStaticField<Any>(chrome.uiServiceInstance()))
            val ids = setOf(
                checkNotNull(ui.invokeMethod<Int>(chrome.topContainerId())),
                checkNotNull(ui.invokeMethod<Int>(chrome.topShadowId()))
            )
            check(ids.all { it > 0 }) { "Home chrome resource IDs are unavailable" }
            top.hook {
                before {
                    val view = instance as View
                    if (view.id !in ids) return@before
                    if (suppress(view, args[0] as Int, top.self)) {
                        args[0] = View.GONE
                        args[1] = false
                    }
                }
            }.result {
                onConductFailure { _, error -> YLog.error("$TAG: top chrome protection failed", error) }
                onHookingFailure { error -> YLog.error("$TAG: top chrome hook failed", error) }
            }
            bottom.hook {
                before {
                    if (suppress(instance as View, args[0] as Int, bottom.self)) args[0] = View.GONE
                }
            }.result {
                onConductFailure { _, error -> YLog.error("$TAG: bottom chrome protection failed", error) }
                onHookingFailure { error -> YLog.error("$TAG: bottom chrome hook failed", error) }
            }
        }.onFailure { YLog.error("$TAG: chrome mapping unavailable, protection disabled", it) }
    }

    private fun suppress(view: View, visibility: Int, renderer: Method): Boolean {
        val activity = activity(view.context)
        if (activity == null || !CleanModeHooker.ownsCleanMode(activity) || visibility != View.VISIBLE) {
            // A later effective hide request supersedes any earlier suppressed show request.
            suppressed.remove(view)
            return false
        }
        // Change only the final rendering. The host's visibility priority entries stay intact.
        suppressed[view] = renderer
        return true
    }

    internal fun restoreSuppressed() {
        val pending = suppressed.entries.map { it.key to it.value }
        suppressed.clear()
        pending.forEach { (view, renderer) ->
            runCatching {
                // Native withdrawal usually renders the bar itself. Replay only show requests
                // still pending when the host already removed our priority entry during a dialog.
                if (renderer.parameterCount == 2) {
                    renderer.invoke(view, View.VISIBLE, false)
                } else {
                    renderer.invoke(view, View.VISIBLE)
                }
            }.onFailure { YLog.error("$TAG: chrome restoration failed", it) }
        }
    }

    private fun activity(context: Context): Activity? {
        var current = context
        while (current is ContextWrapper) {
            if (current is Activity) return current
            val base = current.baseContext
            if (base === current) return null
            current = base
        }
        return current as? Activity
    }
}
