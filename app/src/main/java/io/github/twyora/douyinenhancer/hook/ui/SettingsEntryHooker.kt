package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.factory.injectModuleResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.ui.legacy.SettingsDialog
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import androidx.core.net.toUri

@HookOnMainProcess
object SettingsEntryHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        installModuleSettingsEntryHook()
        installAboutAwemeLongClickOpenSettingsHook()
        installLaunchStartSettingsIntentHook()
        installIncomingStartSettingsIntentHook()
    }

    private fun installModuleSettingsEntryHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethodOrNull(
            packageInstance.douYinSettingNewVersionActivity.onResume()
        )?.intercept {
            val result = proceed()

            val activity = instanceOrNull<Activity>() ?: run {
                YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                return@intercept result
            }

            val settingsScrollView = instance.getFieldOrNull<ViewGroup?>(
                packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
            ) ?: run {
                YLog.error("$TAG: settings scroll view field not found")
                return@intercept result
            }

            val moduleSettingsTag = "douyinenhancer_settings"
            if (settingsScrollView.findViewWithTag<View>(moduleSettingsTag) != null) {
                YLog.info("$TAG: module settings entry already present, skip to avoid duplicate entry")
                return@intercept result
            }

            val moduleSettingsCommonItemView =
                packageInstance.commonItemView.selfClass?.createInstance(activity, null) as? ViewGroup
                    ?: run {
                        YLog.error("$TAG: failed to create module settings entry")
                        return@intercept result
                    }

            moduleSettingsCommonItemView.apply {
                tag = moduleSettingsTag
                id = View.generateViewId()
            }
            // ensure the host app can find the module settings icon
            activity.injectModuleResources()
            moduleSettingsCommonItemView.invokeMethodOnly(
                packageInstance.commonItemView.setLeftText(),
                module.resources.getString(R.string.app_name)
            )
            moduleSettingsCommonItemView.invokeMethodOnly(
                packageInstance.commonItemView.setLeftIcon(),
                R.drawable.ic_module_settings
            )
            moduleSettingsCommonItemView.invokeMethodOnly(
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
                    return@intercept result
                }

            if (verbose) {
                YLog.debug("$TAG: module settings entry prepared, adding view to settings")
            }
            targetParent.addView(
                moduleSettingsCommonItemView,
                0
            )

            result
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to inject module settings entry", throwable)
            }
        }
    }

    private fun installAboutAwemeLongClickOpenSettingsHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethodOrNull(
            packageInstance.douYinSettingNewVersionActivity.onResume()
        )?.intercept {
            val result = proceed()

            val activity = instanceOrNull<Activity>() ?: run {
                YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                return@intercept result
            }

            val settingsScrollView = activity.getFieldOrNull<ViewGroup>(
                packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
            ) ?: run {
                YLog.error("$TAG: settings scroll view field not found")
                return@intercept result
            }

            val aboutAwemeView = settingsScrollView.findViewWithTag<View?>("about_ame") ?: run {
                YLog.error("$TAG: about_ame view not found by tag in settings scroll view")
                return@intercept result
            }

            if (verbose) {
                YLog.debug("$TAG: attaching long click listener on about_ame view to open settings dialog")
            }
            aboutAwemeView.setOnLongClickListener {
                activity.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "douyinenhancer://io.github.twyora.douyinenhancer/settings/main".toUri()
                    )
                )
                true
            }

            result
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to attach long click to open settings", throwable)
            }
        }
    }

    private fun installLaunchStartSettingsIntentHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.mainActivity.selfClass?.resolveMethodOrNull(
            packageInstance.mainActivity.onResume()
        )?.intercept {
            val activity = instanceOrNull<Activity>() ?: run {
                YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                return@intercept proceed()
            }

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

            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on resume", throwable)
            }
        }
    }

    private fun installIncomingStartSettingsIntentHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.mainActivity.selfClass?.resolveMethodOrNull(
            packageInstance.mainActivity.onNewIntent()
        )?.intercept {
            val activity = instanceOrNull<Activity>() ?: run {
                YLog.error("$TAG: ${instance::class.qualifiedName} is not an Activity")
                return@intercept proceed()
            }
            val intent = args.firstNotNullOfOrNull {
                it as? Intent
            } ?: run {
                YLog.error("$TAG: intent not found in argument list")
                return@intercept proceed()
            }

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

            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to show settings dialog on new intent", throwable)
            }
        }
    }
}
