package io.github.twyora.douyinenhancer.hook

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod
import io.github.twyora.douyinenhancer.R

object SettingsHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethod(
                packageInstance.douYinSettingNewVersionActivity.onResume()
            )?.hook {
                after {
                    val currentActivity = instance as? Activity ?: run {
                        YLog.error("$TAG: instance is not an Activity")
                        return@after
                    }

                    val settingsScrollView = instance.getField<ViewGroup?>(
                        packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
                    ) ?: run {
                        YLog.error("$TAG: settingsScrollView is null")
                        return@after
                    }

                    val logoutView = settingsScrollView.findViewWithTag<View?>("logout") ?: run {
                        YLog.error("$TAG: logoutView is null")
                        return@after
                    }

                    val logoutPanel = logoutView.parent as? ViewGroup ?: run {
                        YLog.error("$TAG: logoutPanel is null")
                        return@after
                    }

                    if (logoutPanel.findViewWithTag<View>("dyenhancer_settings") != null) {
                        YLog.warn("$TAG: settingsScrollView already has dyenhancer_settings")
                        return@after
                    }

                    val dyEnhancerCommonItemView =
                        packageInstance.commonItemView.selfClass?.getConstructor(Context::class.java)
                            ?.newInstance(instance) as? ViewGroup
                    if (dyEnhancerCommonItemView == null) {
                        YLog.warn("$TAG: dyEnhancerCommonItemView is null")
                        return@after
                    }

                    dyEnhancerCommonItemView.tag = "dyenhancer_settings"
                    dyEnhancerCommonItemView.id = View.generateViewId()

                    dyEnhancerCommonItemView.invokeMethod<Unit>(
                        packageInstance.commonItemView.setLeftText(),
                        moduleAppResources.getString(R.string.app_name)
                    )
                    dyEnhancerCommonItemView.invokeMethod<Unit>(
                        packageInstance.commonItemView.setRightUIMode(),
                        0
                    )
                    dyEnhancerCommonItemView.layoutParams = LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )

                    dyEnhancerCommonItemView.setOnClickListener {
                        YLog.debug("$TAG: dyEnhancerCommonItemView clicked")
                        SettingsDialog.show(currentActivity)
                    }

                    logoutPanel.apply {
                        YLog.debug("$TAG: logoutPanel child count: $childCount")
                        addView(dyEnhancerCommonItemView, 0)
                    }
                }
            }
        }
    }
}
