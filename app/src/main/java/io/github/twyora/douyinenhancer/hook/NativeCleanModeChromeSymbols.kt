package io.github.twyora.douyinenhancer.hook

import org.luckypray.dexkit.DexKitBridge

/** Resolve final chrome rendering separately from native command/state ownership. */
internal object NativeCleanModeChromeSymbols {
    fun resolve(bridge: DexKitBridge): Configs.CleanModeChrome {
        val top = checkNotNull(bridge.getClassData("com.ss.android.ugc.aweme.feed.ui.visibility.VisibilityControlledFrameLayout"))
        val bottom = checkNotNull(bridge.getClassData("com.ss.android.ugc.aweme.homepage.tab.ui.bottom.MainBottomTabViewNew"))
        val ui = checkNotNull(bridge.getClassData("com.ss.android.ugc.aweme.services.HomePageUIService"))
        val applyTop = bridge.findMethod {
            searchClasses = listOf(top)
            matcher {
                paramTypes("int", "boolean")
                returnType = "void"
                usingStrings("super.setVisibility: ")
            }
        }.single()
        val applyBottom = bridge.findMethod {
            searchClasses = listOf(bottom)
            matcher {
                paramTypes("int")
                returnType = "void"
                invokeMethods {
                    add {
                        name = "setHasFixSize"
                        paramTypes("boolean")
                    }
                }
            }
        }.single()
        val singleton = bridge.findField {
            searchClasses = listOf(ui)
            matcher {
                name = "INSTANCE"
                type = ui.name
            }
        }.single()
        val containerId = bridge.findMethod {
            searchClasses = listOf(ui)
            matcher {
                name = "getTitleBarContainerResId"
                paramCount = 0
                returnType = "int"
            }
        }.single()
        val shadowId = bridge.findMethod {
            searchClasses = listOf(ui)
            matcher {
                name = "getTitleShadowRedId"
                paramCount = 0
                returnType = "int"
            }
        }.single()
        return cleanModeChrome {
            topViewClass = class_ { name = top.name }
            applyTopVisibility = method {
                name = applyTop.name
                parameters = MethodKt.parameters { values.addAll(applyTop.paramTypeNames) }
            }
            bottomViewClass = class_ { name = bottom.name }
            applyBottomVisibility = method {
                name = applyBottom.name
                parameters = MethodKt.parameters { values.addAll(applyBottom.paramTypeNames) }
            }
            uiServiceClass = class_ { name = ui.name }
            uiServiceInstance = field { name = singleton.name }
            topContainerId = method { name = containerId.name }
            topShadowId = method { name = shadowId.name }
        }
    }
}
