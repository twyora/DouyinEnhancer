package io.github.twyora.douyinenhancer.ui

import android.app.AlertDialog
import android.content.Context
import android.content.res.Resources
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Switch // 【修复 3】引入系统原生 Switch，删除 androidx.appcompat.widget.SwitchCompat
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager

object SettingDialog {
    fun show(context: Context, res: Resources) {
        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            val pad = dp2px(context, 16f)
            setPadding(pad, pad, pad, pad)
        }

        // 【修复 5】使用 FastKVConfigManager 实现的 SharedPreferences 接口，而不是直接 .kv
        container.addView(
            createSwitchRow(
                context,
                res.getString(R.string.pref_comment_image),
                FastKVConfigManager.getBoolean("comment_image", false)
            ) { checked ->
                FastKVConfigManager.edit().putBoolean("comment_image", checked).apply()
            })

        container.addView(
            createSwitchRow(
                context,
                res.getString(R.string.pref_comment_emoji),
                FastKVConfigManager.getBoolean("comment_emoji", false)
            ) { checked ->
                FastKVConfigManager.edit().putBoolean("comment_emoji", checked).apply()
            })

        AlertDialog.Builder(context)
            .setTitle(res.getString(R.string.settings_title))
            .setView(container)
            .setPositiveButton(res.getString(R.string.settings_close), null)
            .show()
    }

    private fun createSwitchRow(
        context: Context,
        title: String,
        initialValue: Boolean,
        onChanged: (Boolean) -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp2px(context, 8f)
                bottomMargin = dp2px(context, 8f)
            }
            gravity = Gravity.CENTER_VERTICAL

            addView(TextView(context).apply {
                text = title
                layoutParams = LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f)
                textSize = 16f
            })

            // 【修复 6】使用系统原生的 android.widget.Switch，彻底避开 AppCompat Theme 崩溃
            addView(Switch(context).apply {
                isChecked = initialValue
                setOnCheckedChangeListener { _, checked -> onChanged(checked) }
            })
        }
    }

    private fun dp2px(context: Context, dp: Float): Int {
        return (dp * context.resources.displayMetrics.density).toInt()
    }
}