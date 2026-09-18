@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui.legacy

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.ContextThemeWrapper
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.FeatureGate
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.setField

class PlaybackComponentBlockDialog(context: Context) : AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {
    class PrefsFragment : PreferenceFragment() {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            preferenceManager.setField(
                Field("mSharedPreferences"),
                ((ConfigManager.settings as FastKVStorage).fastKV) as SharedPreferences
            )
            preferenceManager.setField(Field("mEditor"), null)
            addPreferencesFromResource(R.xml.pref_playback_component_block)

            if (!ConfigManager.miscConfig.hiddenFeatureEnabled) {
                HIDDEN_KEYS.forEach { key ->
                    findPreference(key)?.let {
                        preferenceScreen?.removePreference(it)
                    }
                }
            }
        }
    }

    init {
        val activity = context as Activity

        val prefsFragment = PrefsFragment()
        activity.fragmentManager.beginTransaction().add(prefsFragment, "PlaybackComponentBlock").commit()
        activity.fragmentManager.executePendingTransactions()

        setView(prefsFragment.view)
        setTitle(R.string.playback_component_block_dialog_title)
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            if (!ConfigManager.miscConfig.hiddenFeatureEnabled) {
                HIDDEN_KEYS.forEach { key ->
                    ConfigManager.settings.put(key, false)
                }
            }
        }
        setOnDismissListener {
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        private val HIDDEN_KEYS
            get() = ConfigManager.playbackComponentBlockConfig
                .gatedKeys[FeatureGate.HIDDEN].orEmpty()

        fun show(context: Context) {
            runCatching {
                (context as? Activity)?.injectModuleAppResources()
                PlaybackComponentBlockDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: failed to show playback component block dialog", it)
            }
        }
    }
}
