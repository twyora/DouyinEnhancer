package io.github.twyora.douyinenhancer.hook

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object SettingsHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.douYinSettingNewVersionActivity.selfClass?.resolveMethod(
                packageInstance.douYinSettingNewVersionActivity.onResume()
            )?.hook {
                after {
                    after {
                        val thisObj = instance as Activity

                        val settingsScrollView = instance.getField<View?>(
                            packageInstance.douYinSettingNewVersionActivity.settingsScrollView()
                        )

                        if (settingsScrollView == null) {
                            YLog.warn("$TAG: settingsScrollView is null")
                            return@after
                        } else if (settingsScrollView.findViewWithTag<View>("dyenhancer_settings") != null) {
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
                            packageInstance.commonItemView.setTextLeft(),
                            "Douyin Enhancer"
                        )
                        dyEnhancerCommonItemView.invokeMethod<Unit>(
                            packageInstance.commonItemView.setRightUiMode(),
                            0
                        )
                        dyEnhancerCommonItemView.invokeMethod<Unit>(
                            packageInstance.commonItemView.setRightText(),
                            "点击进入"
                        )
                        dyEnhancerCommonItemView.layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        )

                        dyEnhancerCommonItemView.setOnClickListener {
                            YLog.debug("$TAG: dyEnhancerCommonItemView clicked")
                        }

                        val scrollViewGroup = settingsScrollView as? ViewGroup
                        val bigLinearLayout = scrollViewGroup?.getChildAt(0) as? ViewGroup

                        bigLinearLayout?.apply {
                            YLog.debug("$TAG: bigLinearLayout child count: $childCount")
                            addView(dyEnhancerCommonItemView)
                        } ?: YLog.warn("$TAG: Failed to cast to ViewGroup or get child at 0")
                    }
                }
            }
        }
    }
}
