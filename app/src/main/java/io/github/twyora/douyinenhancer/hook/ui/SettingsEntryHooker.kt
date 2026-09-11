package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.SettingsDialog
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object SettingsEntryHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        installModuleSettingsEntryHook()
        installAboutAwemeLongClickOpenSettingsHook()
        installLaunchStartSettingsIntentHook()
        installIncomingStartSettingsIntentHook()
    }

    private fun installModuleSettingsEntryHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethod(
            packageInstance.douYinSettingNewVersionActivity.onResume()
        )?.hook {
            after {
                val activity = instance as? Activity ?: run {
                    YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                    return@after
                }

                val settingsScrollView = instance.getField<ViewGroup?>(
                    packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
                ) ?: run {
                    YLog.error("$TAG: settings scroll view field not found")
                    return@after
                }

                val moduleSettingsTag = "douyinenhancer_settings"
                if (settingsScrollView.findViewWithTag<View>(moduleSettingsTag) != null) {
                    YLog.info("$TAG: module settings entry already present, skip to avoid duplicate entry")
                    return@after
                }

                val moduleSettingsCommonItemView =
                    packageInstance.commonItemView.selfClass?.createInstance(activity, null) as? ViewGroup
                        ?: run {
                            YLog.error("$TAG: failed to create module settings entry")
                            return@after
                        }

                moduleSettingsCommonItemView.apply {
                    tag = moduleSettingsTag
                    id = View.generateViewId()
                }
                // ensure the host app can find the module settings icon
                activity.injectModuleAppResources()
                moduleSettingsCommonItemView.invokeMethodOnly(
                    packageInstance.commonItemView.setLeftText(),
                    moduleAppResources.getString(R.string.app_name)
                )
                moduleSettingsCommonItemView.invokeMethodOnly(
                    packageInstance.commonItemView.setLeftIcon(),
                    R.drawable.ic_module_settings
                )
                moduleSettingsCommonItemView.invokeMethod<Unit>(
                    packageInstance.commonItemView.setRightUIMode(),
                    0 // arrow mode for the right UI element
                )
                moduleSettingsCommonItemView.layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )

                moduleSettingsCommonItemView.setOnClickListener {
                    SettingsDialog.show(activity)
                }

                // prefer inserting above the logout button; fallback to direct insert
                val targetParent = settingsScrollView.findViewWithTag<View?>(
                    "logout"
                )?.parent as? ViewGroup
                    ?: (settingsScrollView.getChildAt(0) as? ViewGroup)
                    ?: run {
                        YLog.error("$TAG: unable to find a suitable parent for module settings entry")
                        return@after
                    }

                if (verbose) {
                    YLog.debug("$TAG: module settings entry prepared, adding view to settings")
                }
                targetParent.addView(
                    moduleSettingsCommonItemView,
                    0
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to inject module settings entry", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for injecting module settings entry", throwable)
            }
        }
    }

    private fun installAboutAwemeLongClickOpenSettingsHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethod(
            packageInstance.douYinSettingNewVersionActivity.onResume()
        )?.hook {
            after {
                val activity = instance as? Activity ?: run {
                    YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                    return@after
                }

                val settingsScrollView = activity.getField<ViewGroup>(
                    packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
                ) ?: run {
                    YLog.error("$TAG: settings scroll view field not found")
                    return@after
                }

                val aboutAwemeView = settingsScrollView.findViewWithTag<View?>("about_ame") ?: run {
                    YLog.error("$TAG: about_ame view not found by tag in settings scroll view")
                    return@after
                }

                if (verbose) {
                    YLog.debug("$TAG: attaching long click listener on about_ame view to open settings dialog")
                }
                aboutAwemeView.setOnLongClickListener {
                    SettingsDialog.show(activity)
                    true
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to attach long click to open settings", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for attaching long click to open settings", throwable)
            }
        }
    }

    private fun installLaunchStartSettingsIntentHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onResume()
        )?.hook {
            before {
                val activity = instance as? Activity ?: return@before

                val shouldStartSettings = activity.intent?.getBooleanExtra(
                    "douyinenhancer_start_settings",
                    false
                )
                if (shouldStartSettings == true) {
                    if (verbose) {
                        YLog.debug("$TAG: start-settings flag detected in onResume intent, showing settings dialog")
                    }
                    activity.intent?.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                    removeSelf {
                        if (verbose) {
                            YLog.debug("$TAG: settings dialog shown, unregistering onResume hook to prevent re-show")
                        }
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on resume", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing settings dialog on resume", throwable)
            }
        }
    }

    private fun installIncomingStartSettingsIntentHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.mainActivity.selfClass?.resolveMethod(
            packageInstance.mainActivity.onNewIntent()
        )?.hook {
            before {
                val activity = instance as? Activity ?: return@before
                val intent = args[0] as? Intent ?: return@before

                val shouldStartSettings = intent.getBooleanExtra(
                    "douyinenhancer_start_settings",
                    false
                )
                if (shouldStartSettings) {
                    if (verbose) {
                        YLog.debug("$TAG: start-settings flag detected in onNewIntent, showing settings dialog")
                    }
                    intent.removeExtra("douyinenhancer_start_settings")
                    SettingsDialog.show(activity)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on new intent", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for showing settings dialog on new intent", throwable)
            }
        }
    }
}
