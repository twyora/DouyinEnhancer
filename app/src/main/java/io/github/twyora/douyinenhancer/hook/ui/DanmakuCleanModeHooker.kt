package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.hook.NativeCleanModeSymbols
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

/** Apply the host's own danmaku whitelist before clean commands enter its state model. */
@HookOnMainProcess
object DanmakuCleanModeHooker : YukiBaseHooker() {
    private const val TAG = "DanmakuCleanModeHooker"

    override fun onHook() {
        if (!ConfigManager.ui.keepDanmakuVisible.value && !ConfigManager.ui.cleanMode.value) {
            if (!ConfigManager.module.verboseDisabled.value) {
                YLog.debug("$TAG: keep danmaku visible disabled, skip danmaku policy")
            }
            return
        }

        runCatching {
            val native = DouyinPackage.instance.nativeCleanMode
            val serviceClass = checkNotNull(native.serviceClass) { "Clean mode service is unavailable" }
            val commandClass = checkNotNull(native.commandClass) { "Clean mode command is unavailable" }
            val policyClass = checkNotNull(native.danmakuPolicyClass) { "Native danmaku policy is unavailable" }
            // The host factory constructs a command without posting it; its synthetic argument is unused.
            val template = checkNotNull(policyClass.invokeStaticMethod<Any>(native.danmakuPolicyFactory(), null))
            check(commandClass.isInstance(template)) { "Native danmaku policy did not create a clean command" }
            val ids = NativeCleanModeSymbols.danmakuWhiteList(
                checkNotNull(template.getField<List<*>>(native.whiteList()))
            )
            val names = setOf(checkNotNull(native.toggle().name), "toggleCleanModeInVH")
            val methods = serviceClass.declaredMethods.filter { method ->
                method.name in names && method.parameterTypes.size == 3 &&
                    method.parameterTypes[1] == String::class.java && method.parameterTypes[2] == commandClass
            }
            check(methods.isNotEmpty()) { "Clean mode command entry points are unavailable" }

            methods.forEach { method ->
                runCatching {
                    serviceClass.resolveMethod(Method(method.name, method.parameterTypes.map { it.name })).hook {
                        before {
                            val command = checkNotNull(args[2])
                            check(commandClass.isInstance(command)) { "Clean mode argument is not a native command" }
                            val whiteList = checkNotNull(command.getField<List<*>>(native.whiteList()))
                            val missing = ids.filterNot { it in whiteList }
                            if (missing.isNotEmpty()) {
                                command.resolveMethod(native.appendWhiteList()).invoke(missing)
                            }
                        }
                    }.result {
                        onConductFailure { _, error ->
                            YLog.error("$TAG: failed to apply danmaku policy at ${method.name}", error)
                        }
                        onHookingFailure { error ->
                            YLog.error("$TAG: failed to hook ${method.toGenericString()}", error)
                        }
                    }
                }.onFailure { error ->
                    YLog.error("$TAG: failed to install ${method.toGenericString()}", error)
                }
            }
        }.onFailure { error ->
            YLog.error("$TAG: native danmaku policy unavailable, policy disabled", error)
        }
    }
}
