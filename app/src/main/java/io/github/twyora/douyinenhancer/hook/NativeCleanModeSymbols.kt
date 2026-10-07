package io.github.twyora.douyinenhancer.hook

import io.github.twyora.douyinenhancer.utils.Method
import java.lang.reflect.Modifier
import org.luckypray.dexkit.DexKitBridge

/** Resolve command fields from their consumers, since their names are obfuscated. */
internal object NativeCleanModeSymbols {
    const val CALLER = "douyinenhancer_clean_mode"
    const val HIDE_SEEKBAR = 8
    const val PLAYER_STARTED = 2
    const val PLAYER_RESUMED = 3
    const val PLAYER_PAUSED = 4
    const val MAIN_ACTIVITY = "com.ss.android.ugc.aweme.main.MainActivity"

    val getFragment = Method("getFragment", emptyList())
    val getEventType = Method("getEventType", emptyList())
    val userVisible = Method("getUserVisibleHint", emptyList())
    val getCurrentAweme = Method("getCurrentAweme", emptyList())
    val isLive = Method("isLive", emptyList())
    val pageSelected = Method("onPageSelected", emptyList())
    val spacePolicy = Method("shouldHandleTopAndBottomSpaceInPinch", emptyList())
    val updateSpaces = Method("handleTopAndBottomSpace", emptyList())
    val adapt = Method("adaptation", emptyList())
    val onPause = Method("onPause", emptyList())
    val onDestroyView = Method("onDestroyView", emptyList())
    val scrollState = Method("onPageScrollStateChanged", listOf("int"))
    val fragmentAdded = Method("isAdded", emptyList())
    val fragmentActivity = Method("getActivity", emptyList())

    fun resolve(bridge: DexKitBridge): Configs.NativeCleanMode {
        val command = bridge.findClass {
            matcher {
                usingStrings("stateOn: ", ", autoQuit: ", ", seekbar:")
            }
        }.single()
        val service = checkNotNull(
            bridge.getClassData("com.ss.android.ugc.aweme.feed.plato.business.contentconsumption.cleanmode.CleanModeServiceImpl")
        )
        val instance = bridge.findMethod {
            searchClasses = listOf(service)
            matcher {
                modifiers = Modifier.PUBLIC or Modifier.STATIC
                paramCount = 0
                returnType = "com.ss.android.ugc.aweme.feed.cleanmode.ICleanModeService"
            }
        }.single()
        val toggle = bridge.findMethod {
            searchClasses = listOf(service)
            matcher {
                name = "toggleCleanMode"
                paramTypes("androidx.fragment.app.Fragment", "java.lang.String", command.name)
            }
        }.single()
        val inverse = bridge.findMethod {
            searchClasses = listOf(command)
            matcher {
                paramCount = 0
                returnType = command.name
            }
        }.single()
        val description = bridge.findMethod {
            searchClasses = listOf(command)
            matcher { name = "toString" }
        }.single()
        val autoQuit = description.usingFields.map { it.field }.single {
            it.typeName == "boolean" && !Modifier.isFinal(it.modifiers)
        }
        val seekbar = bridge.findMethod {
            searchClasses = listOf(command)
            matcher {
                paramCount = 0
                returnType = "boolean"
                usingNumbers(HIDE_SEEKBAR)
            }
        }.single()
        val content = seekbar.usingFields.map { it.field }.distinctBy { it.name }.single {
            it.typeName == "int"
        }
        return nativeCleanMode {
            serviceClass = class_ { name = service.name }
            commandClass = class_ { name = command.name }
            serviceInstance = method {
                name = instance.name
                parameters = MethodKt.parameters { values.addAll(instance.paramTypeNames) }
            }
            this.toggle = method {
                name = toggle.name
                parameters = MethodKt.parameters { values.addAll(toggle.paramTypeNames) }
            }
            this.inverse = method {
                name = inverse.name
                parameters = MethodKt.parameters { values.addAll(inverse.paramTypeNames) }
            }
            this.autoQuit = field { name = autoQuit.name }
            this.content = field { name = content.name }
        }
    }

    /** Reuse the host's command policy instead of assigning IDs to rendered views. */
    fun resolveDanmakuPolicy(bridge: DexKitBridge): Configs.NativeCleanMode {
        val command = bridge.findClass {
            matcher { usingStrings("stateOn: ", ", autoQuit: ", ", seekbar:") }
        }.single()
        val append = bridge.findMethod {
            searchClasses = listOf(command)
            matcher {
                modifiers = Modifier.PUBLIC
                paramTypes("java.util.List")
                returnType = "void"
            }
        }.single()
        val whiteList = bridge.findField {
            searchClasses = listOf(command)
            matcher { type = "java.util.List" }
        }.single()
        val factory = bridge.findMethod {
            matcher {
                modifiers = Modifier.PUBLIC or Modifier.STATIC
                paramCount = 1
                returnType = "java.lang.Object"
                usingStrings("lpp_play_control")
                invokeMethods {
                    add { descriptor = "L${command.name.replace('.', '/')};-><init>(ZLjava/lang/String;)V" }
                    add { descriptor = append.descriptor }
                }
            }
        }.single()
        return nativeCleanMode {
            danmakuPolicyClass = class_ { name = factory.declaredClassName }
            danmakuPolicyFactory = method {
                name = factory.name
                parameters = MethodKt.parameters { values.addAll(factory.paramTypeNames) }
            }
            this.whiteList = field { name = whiteList.name }
            appendWhiteList = method {
                name = append.name
                parameters = MethodKt.parameters { values.addAll(append.paramTypeNames) }
            }
        }
    }

    /** Fail closed if the native template changes to preserve unrelated widgets. */
    fun danmakuWhiteList(value: List<*>): List<Int> {
        val id = value.singleOrNull() as? Int
        check(id != null && id > 0x00ffffff && (id ushr 16 and 0xff) != 0) {
            "Native danmaku policy must contain exactly one resource ID"
        }
        return listOf(id)
    }
}
