package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setStaticFieldQuietly

@HookOnMainProcess
object HomeTopCornerEntranceHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val blockRightEntranceList by lazy {
        setOfNotNull(
            if (ConfigManager.hideHomeTopCornerEntrance.hideRightSearch.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_RIGHT_SEARCH
            } else {
                null
            }
        )
    }

    private val blockLeftEntranceList by lazy {
        setOfNotNull(
            if (ConfigManager.hideHomeTopCornerEntrance.hideLeftScan.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_LEFT_SCAN
            } else {
                null
            },
            if (ConfigManager.hideHomeTopCornerEntrance.hideLeftSidebar.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_LEFT_SIDEBAR
            } else {
                null
            },
            if (ConfigManager.hideHomeTopCornerEntrance.hideLeftSidebarJx.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_LEFT_SIDEBAR_JX
            } else {
                null
            },
            if (ConfigManager.hideHomeTopCornerEntrance.hideLeftLogin.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_LEFT_LOGIN
            } else {
                null
            },
            if (ConfigManager.hideHomeTopCornerEntrance.hideLeftExtensionComponent.value) {
                DouyinPackage.TopCornerEntranceDataModule.HOMEPAGE_LEFT_EXTENSION_COMPONENT
            } else {
                null
            }
        )
    }

    override fun onHook() {
        if (!ConfigManager.hideHomeTopCornerEntrance.rightMainSwitch.value) {
            YLog.info("$TAG: right corner entrance hide is disabled, skipping hook")
        } else {
            installRightCornerEntranceHook()
        }
        if (!ConfigManager.hideHomeTopCornerEntrance.leftMainSwitch.value) {
            YLog.info("$TAG: left corner entrance hide is disabled, skipping hook")
        } else {
            installLeftCornerEntranceHook()
        }
    }

    private fun installRightCornerEntranceHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.topCornerEntranceDataSource.selfClass?.resolveMethodOrNull(
            packageInstance.topCornerEntranceDataSource.getRightEntranceList()
        )?.intercept {
            val rightEntrances = proceed<MutableList<Any>>() ?: run {
                YLog.error("$TAG: failed to get right-corner entrance list")
                return@intercept null
            }

            val removed = rightEntrances.removeIf { entrance ->
                entrance.getFieldOrNull<String>(
                    packageInstance.topCornerEntranceData.entranceId()
                ) in blockRightEntranceList
            }
            if (!removed) {
                YLog.warn("$TAG: no blocked right-corner entrances")
            }

            rightEntrances
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook right-corner entrance list", throwable)
            }
        }
    }

    private fun installLeftCornerEntranceHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.topCornerEntranceDataSource.selfClass?.resolveMethodOrNull(
            packageInstance.topCornerEntranceDataSource.getLeftEntranceList()
        )?.intercept {
            // The original method may return SingletonList, which is unmodifiable
            val leftEntrances = proceed<List<Any>>()?.toMutableList() ?: run {
                YLog.error("$TAG: failed to get left-corner entrance list")
                return@intercept null
            }

            val removed = leftEntrances.removeIf { entrance ->
                entrance.getFieldOrNull<String>(
                    packageInstance.topCornerEntranceData.entranceId()
                ) in blockLeftEntranceList
            }
            if (!removed) {
                YLog.warn("$TAG: no blocked left-corner entrances")
            } else {
                // Host caches the list after `proceed`, and replaces it with the modified one
                packageInstance.topCornerEntranceDataSource.selfClass?.setStaticFieldQuietly(
                    packageInstance.topCornerEntranceDataSource.leftEntranceList(),
                    leftEntrances
                )
            }

            leftEntrances
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook left-corner entrance list", throwable)
            }
        }
    }
}
