@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.generated.AppProperties
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.utils.setField
import kotlin.system.exitProcess

/**
 * Settings dialog for DouyinEnhancer.
 *
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/SettingDialog.kt)
 */
class SettingsDialog(context: Context) : AlertDialog.Builder(context) {
    class PrefsFragment :
        PreferenceFragment(),
        Preference.OnPreferenceClickListener {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            preferenceManager.setField<Any?>(DouyinPackage.Field("mSharedPreferences"), FastKVConfigManager.settings)
            preferenceManager.setField<Any?>(DouyinPackage.Field("mEditor"), null)
            addPreferencesFromResource(R.xml.prefs_setting)

            findPreference("version")?.summary = AppProperties.PROJECT_VERSION_NAME
            findPreference("recommend_feed_filter")?.onPreferenceClickListener = this
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference?) = when (preference?.key) {
            "recommend_feed_filter" -> {
                RecommendedFeedFilterDialog(context).show()
                true
            }

            else -> false
        }
    }

    init {
        val activity = context as Activity
        activity.injectModuleAppResources()

        val prefsFragment = PrefsFragment()
        activity.fragmentManager.beginTransaction().add(prefsFragment, "Settings").commit()
        activity.fragmentManager.executePendingTransactions()

        setView(prefsFragment.view)
        setTitle(context.getString(R.string.settings_dialog_title))
        setNegativeButton(context.getString(R.string.settings_dialog_back), null)
        setPositiveButton(context.getString(R.string.settings_dialog_confirm_and_restart)) { _, _ ->
            restartApplication(activity)
        }
        setOnDismissListener {
            activity.runOnUiThread {
                Toast.makeText(context, R.string.restart_required, Toast.LENGTH_SHORT).show()
            }
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        fun show(context: Context) {
            runCatching {
                SettingsDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: SettingDialog show failed", it)
            }
        }

        private fun restartApplication(activity: Activity) {
            // https://stackoverflow.com/a/58530756
            val pm = activity.packageManager
            val intent = pm.getLaunchIntentForPackage(activity.packageName)
            activity.finishAffinity()
            activity.startActivity(intent)
            exitProcess(0)
        }
    }
}
