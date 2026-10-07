package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.view.Window
import android.view.WindowManager
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.hook.NativeCleanModeSymbols
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.resolveMethodOrNull

@HookOnMainProcess
object CleanModeWindowHooker : YukiBaseHooker() {
    private const val TAG = "CleanModeWindowHooker"

    override fun onHook() {
        if (!ConfigManager.ui.cleanMode.value) return
        listOf(Method("onPostResume", emptyList()), Method("onWindowFocusChanged", listOf("boolean"))).forEach { method ->
            Activity::class.java.resolveMethodOrNull(method)?.hook {
                after {
                    val activity = instance as Activity
                    if (activity.javaClass.name == NativeCleanModeSymbols.MAIN_ACTIVITY && (args.isEmpty() || args[0] == true)) {
                        hideSystemBars(activity.window)
                    }
                }
            }?.result {
                onConductFailure { _, error -> YLog.error("$TAG: fullscreen update failed", error) }
                onHookingFailure { error -> YLog.error("$TAG: activity window hook failed", error) }
            }
        }
        Window::class.java.resolveMethodOrNull(Method("setFlags", listOf("int", "int")))?.hook {
            before {
                val window = instance as Window
                if (window.context.javaClass.name == NativeCleanModeSymbols.MAIN_ACTIVITY) {
                    // Host tab/live transitions may clear FULLSCREEN after focus has already arrived.
                    args[0] = (args[0] as Int) or WindowManager.LayoutParams.FLAG_FULLSCREEN
                    args[1] = (args[1] as Int) or WindowManager.LayoutParams.FLAG_FULLSCREEN
                }
            }
        }?.result {
            onConductFailure { _, error -> YLog.error("$TAG: fullscreen flags failed", error) }
            onHookingFailure { error -> YLog.error("$TAG: window flags hook failed", error) }
        }
    }

    private fun hideSystemBars(window: Window) {
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).apply {
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            hide(WindowInsetsCompat.Type.systemBars())
        }
    }
}
