@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui.legacy

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.ContextThemeWrapper
import com.highcapable.yukihookapi.hook.factory.injectModuleResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.bridge.ModuleApp
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.utils.reflect.Field
import io.github.twyora.douyinenhancer.utils.reflect.setFieldOrNull

@Deprecated(
    "V1 config dialogs are no longer maintained in the host;" +
        " use the Compose-based settings in the module environment instead."
)
class PlaybackComponentBlockDialog(context: Context) : AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {
    class PrefsFragment : PreferenceFragment() {
        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            preferenceManager.setFieldOrNull(
                Field("mSharedPreferences"),
                // TODO: Urgent refactor required. This relies on internal implementation details
                ((ConfigManager.playbackComponentBlock.kvConfig as FastKVStorage).fastKV) as SharedPreferences
            )
            preferenceManager.setFieldOrNull(Field("mEditor"), null)
            addPreferencesFromResource(R.xml.pref_playback_component_block)

            ConfigManager.playbackComponentBlock.allConfigItems.filter { configItem ->
                configItem.status != ConfigStateMode.NORMAL
            }.forEach { hiddenConfigItem ->
                findPreference(hiddenConfigItem.key)?.let {
                    preferenceScreen?.removePreference(it)
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
        setTitle(ModuleApp.instance.resources.getString(R.string.playback_component_block_dialog_title))
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok, null)
        setOnDismissListener {
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        @Deprecated(
            "V1 config dialogs are no longer maintained in the host;" +
                " use the Compose-based settings in the module environment instead."
        )
        fun show(context: Context) {
            runCatching {
                context.injectModuleResources()
                PlaybackComponentBlockDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: failed to show playback component block dialog", it)
            }
        }
    }
}
