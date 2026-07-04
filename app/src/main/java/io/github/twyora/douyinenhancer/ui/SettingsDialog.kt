@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.widget.Toast
import androidx.core.content.edit
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
        private var hiddenFeatureClickCount = 0

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val prefs = FastKVConfigManager.settings

            preferenceManager.setField<Any?>(DouyinPackage.Field("mSharedPreferences"), prefs)
            preferenceManager.setField<Any?>(DouyinPackage.Field("mEditor"), null)
            addPreferencesFromResource(R.xml.prefs_setting)

            if (!prefs.getBoolean("enable_hidden_features", false)) {
                val miscCategory = findPreference("pref_category_misc") as? PreferenceCategory
                miscCategory?.let { category ->
                    findPreference("enable_hidden_features")?.let {
                        category.removePreference(it)
                    }
                    if (category.preferenceCount == 0) {
                        category.parent?.removePreference(category)
                    }
                }
            }


            findPreference("version")?.summary = AppProperties.PROJECT_VERSION_NAME
            findPreference("version")?.onPreferenceClickListener = this
            findPreference("recommend_feed_filter")?.onPreferenceClickListener = this
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference?) = when (preference?.key) {
            "recommend_feed_filter" -> {
                RecommendedFeedFilterDialog(context).show()
                true
            }

            "version" -> {
                val prefs = FastKVConfigManager.settings
                if (!prefs.getBoolean("enable_hidden_features", false)) {
                    if (++hiddenFeatureClickCount == 10) {
                        prefs.edit(commit = true) {
                            putBoolean("enable_hidden_features", true)
                        }
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                context.getString(R.string.pref_misc_enable_hidden_features_restart_required),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (hiddenFeatureClickCount >= 6) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                context.getString(R.string.pref_misc_enable_hidden_features_steps_remaining, 10 - hiddenFeatureClickCount),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } else {
                    activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            context.getString(R.string.pref_misc_enable_hidden_features_already_enabled),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
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
