@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui.legacy

import android.app.Activity
import android.app.Activity.RESULT_CANCELED
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceCategory
import android.preference.PreferenceFragment
import android.preference.SwitchPreference
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.provider.MiscConfigProvider
import io.github.twyora.douyinenhancer.config.provider.ModuleConfigProvider
import io.github.twyora.douyinenhancer.hook.comment.CommentAudioHooker.hook
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import java.io.File
import java.net.URL
import java.security.DigestInputStream
import java.security.DigestOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import org.json.JSONObject

/**
 * Settings dialog for DouyinEnhancer.
 *
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/SettingDialog.kt)
 */
class SettingsDialog(context: Context) :
    AlertDialog.Builder(
        ContextThemeWrapper(
            context,
            R.style.MainTheme
        )
    ) {
    class PrefsFragment :
        PreferenceFragment(),
        Preference.OnPreferenceClickListener,
        Preference.OnPreferenceChangeListener {
        private var hiddenFeatureClickCount = 0
        private val scope = MainScope()

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            preferenceManager.setField(
                Field("mSharedPreferences"),
                // TODO: Urgent refactor required. This relies on internal implementation details
                ((ConfigManager.settingsStorage as FastKVStorage).fastKV) as SharedPreferences
            )
            preferenceManager.setField(Field("mEditor"), null)
            addPreferencesFromResource(R.xml.prefs_setting)

            if (!ConfigManager.misc.hiddenFeatureEnabled.value) {
                val miscCategory = findPreference("pref_category_misc") as? PreferenceCategory
                miscCategory?.let { category ->
                    findPreference(MiscConfigProvider.ENABLE_HIDDEN_FEATURES)?.let {
                        category.removePreference(it)
                    }
                    if (category.preferenceCount == 0) {
                        category.parent?.removePreference(category)
                    }
                }
            }

            findPreference("recommend_feed_filter")?.onPreferenceClickListener = this
            findPreference("playback_component_block")?.onPreferenceClickListener = this
            findPreference("export_config")?.onPreferenceClickListener = this
            findPreference("import_config")?.onPreferenceClickListener = this
            (findPreference("disable_verbose_logs") as? SwitchPreference)?.apply {
                isChecked = ConfigManager.module.verboseDisabled.value
                onPreferenceChangeListener = this@PrefsFragment
            }
            findPreference("version")?.summary = BuildConfig.VERSION_NAME
            findPreference("version")?.onPreferenceClickListener = this
            findPreference("build_time")?.summary =
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(BuildConfig.BUILD_TIMESTAMP)

            checkUpdate()
        }

        @Deprecated("Deprecated in Java")
        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference?) = when (preference?.key) {
            "recommend_feed_filter" -> {
                RecommendedFeedFilterDialog(context).show()
                true
            }

            "playback_component_block" -> {
                PlaybackComponentBlockDialog(context).show()
                true
            }

            "version" -> {
                if (!ConfigManager.misc.hiddenFeatureEnabled.value) {
                    if (++hiddenFeatureClickCount == HIDDEN_FEATURE_TRIGGER_CLICK_COUNT) {
                        ConfigManager.misc.hiddenFeatureEnabled.value = true
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                context.getString(R.string.pref_misc_enable_hidden_features_restart_required),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else if (hiddenFeatureClickCount >= HIDDEN_FEATURE_HINT_FROM_CLICK_COUNT) {
                        activity.runOnUiThread {
                            Toast.makeText(
                                context,
                                context.getString(
                                    R.string.pref_misc_enable_hidden_features_steps_remaining,
                                    HIDDEN_FEATURE_TRIGGER_CLICK_COUNT - hiddenFeatureClickCount
                                ),
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

            "export_config" -> onExportConfigClick()

            "import_config" -> onImportConfigClick()

            else -> false
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean = when (preference.key) {
            "disable_verbose_logs" -> {
                val verboseLogsDisabled = newValue as Boolean
                ConfigManager.module.verboseDisabled.value = verboseLogsDisabled
                YLog.info("!!verbose logging disabled is $verboseLogsDisabled!!")
                true
            }

            else -> false
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            when (requestCode) {
                EXPORT_CONFIG, IMPORT_CONFIG -> {
                    val settingsKva = File(context.filesDir, "./fastkv/douyinenhancer_prefs.kva")
                    val settingsKvb = File(context.filesDir, "./fastkv/douyinenhancer_prefs.kvb")
                    val digest = MessageDigest.getInstance("SHA-256")

                    val uri = data?.data
                    if (resultCode == RESULT_CANCELED || uri == null) {
                        return
                    }

                    when (requestCode) {
                        EXPORT_CONFIG -> {
                            runCatching {
                                activity.contentResolver.openOutputStream(uri)?.use { outputStream ->
                                    ZipOutputStream(outputStream).use { zipOut ->
                                        listOf(
                                            settingsKva,
                                            settingsKvb
                                        ).filter {
                                            it.exists()
                                        }.forEach { file ->
                                            zipOut.putNextEntry(ZipEntry(file.name))
                                            DigestInputStream(
                                                file.inputStream(),
                                                digest
                                            ).use { input ->
                                                input.copyTo(zipOut)
                                            }
                                            zipOut.closeEntry()
                                        }
                                        zipOut.putNextEntry(ZipEntry("checksum"))
                                        zipOut.write(
                                            digest.digest().joinToString("") {
                                                "%02x".format(it)
                                            }.toByteArray()
                                        )
                                        zipOut.closeEntry()
                                    }
                                }
                            }.onFailure {
                                activity.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.config_export_failed, it.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                YLog.error("$TAG: export config failed", it)
                            }.onSuccess {
                                activity.runOnUiThread {
                                    Toast.makeText(context, R.string.config_export_success, Toast.LENGTH_SHORT).show()
                                }
                            }
                        }

                        IMPORT_CONFIG -> {
                            val tempBaseName = "douyinenhancer_prefs_temp"
                            val settingsKvaTemp = File(context.cacheDir, "$tempBaseName.kva")
                            val settingsKvbTemp = File(context.cacheDir, "$tempBaseName.kvb")
                            runCatching {
                                var checksum: String? = null

                                activity.contentResolver.openInputStream(uri)?.use { inputStream ->
                                    ZipInputStream(inputStream).use { zipIn ->
                                        generateSequence {
                                            zipIn.nextEntry
                                        }.forEach { zipEntry ->
                                            when (val fileName = zipEntry.name) {
                                                "checksum" -> {
                                                    val checksumBytes = ByteArray(1024)
                                                    val readCount = zipIn.read(checksumBytes)
                                                    checksum = String(checksumBytes, 0, readCount)
                                                }

                                                else -> {
                                                    val targetFile = when (fileName) {
                                                        settingsKva.name -> settingsKvaTemp
                                                        settingsKvb.name -> settingsKvbTemp
                                                        else -> null
                                                    }
                                                    targetFile?.outputStream()?.let {
                                                        DigestOutputStream(it, digest).use { out ->
                                                            zipIn.copyTo(out)
                                                        }
                                                    }
                                                }
                                            }
                                            zipIn.closeEntry()
                                        }
                                    }
                                }

                                val expectedChecksum = digest.digest().joinToString("") {
                                    "%02x".format(it)
                                }
                                if (checksum != expectedChecksum) {
                                    throw IOException(context.getString(R.string.config_import_corrupted))
                                }

                                val settings = ConfigManager.settingsStorage
                                val hiddenFeatureValue = ConfigManager.misc.hiddenFeatureEnabled.value
                                val importedSettings = FastKVStorage.open(context.cacheDir.absolutePath, tempBaseName)
                                try {
                                    settings.putAll(importedSettings.getAll())
                                    ConfigManager.misc.hiddenFeatureEnabled.value = hiddenFeatureValue
                                } finally {
                                    importedSettings.close()
                                }
                            }.onFailure {
                                activity.runOnUiThread {
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.config_import_failed, it.message),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                YLog.error("$TAG: import config failed", it)
                            }.onSuccess {
                                activity.runOnUiThread {
                                    Toast.makeText(context, R.string.config_import_success, Toast.LENGTH_SHORT).show()
                                }
                            }
                            settingsKvaTemp.delete()
                            settingsKvbTemp.delete()
                        }
                    }
                }

                else -> {}
            }
        }

        private fun onExportConfigClick(): Boolean {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.type = "application/zip"
            intent.putExtra(
                Intent.EXTRA_TITLE,
                "douyinenhancer_backup_${
                    SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())
                }.zip"
            )
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            runCatching {
                startActivityForResult(Intent.createChooser(intent, context.getString(R.string.config_export_chooser)), EXPORT_CONFIG)
            }.onFailure {
                activity.runOnUiThread {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }

            return true
        }

        private fun onImportConfigClick(): Boolean {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/zip"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            runCatching {
                startActivityForResult(Intent.createChooser(intent, context.getString(R.string.config_import_chooser)), IMPORT_CONFIG)
            }.onFailure {
                activity.runOnUiThread {
                    Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                }
            }

            return true
        }

        private fun checkUpdate() = scope.launch {
            val latestReleaseJson = runCatching {
                withContext(Dispatchers.IO) {
                    JSONObject(
                        URL(
                            context.getString(
                                R.string.latest_release_api_url
                            )
                        ).readText()
                    )
                }
            }.onFailure {
                YLog.error("$TAG: fetch latest release failed", it)
            }.getOrNull()
            if (latestReleaseJson == null) {
                YLog.info("$TAG: skip update check, no release data")
                return@launch
            }

            val latestReleaseVer = latestReleaseJson.optString("name").removePrefix("v").removePrefix("V")
            if (latestReleaseVer.isNotBlank() && BuildConfig.VERSION_NAME != latestReleaseVer) {
                findPreference("version")?.apply {
                    summary = "${BuildConfig.VERSION_NAME} ($latestReleaseVer)"
                }
                findPreference("update")?.apply {
                    title = context.getString(R.string.pref_about_update_available_title)
                    summary = latestReleaseJson.optString("body").takeIf {
                        it.isNotBlank()
                    }?.let {
                        if (it.length > 80) {
                            it.take(80) + "..."
                        } else {
                            it
                        }
                    } ?: context.getString(R.string.pref_about_update_available_summary)
                }
                val counter = ConfigManager.module.notifyUpdateCooldown.value
                val newCounter =
                    (counter - 1 + ModuleConfigProvider.NOTIFY_UPDATE_COOLDOWN_PERIOD) % ModuleConfigProvider.NOTIFY_UPDATE_COOLDOWN_PERIOD
                ConfigManager.module.notifyUpdateCooldown.value = newCounter
                if (newCounter == 0) {
                    activity.runOnUiThread {
                        Toast.makeText(
                            context,
                            context.getString(R.string.notify_update_available),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                findPreference("update")?.apply {
                    title = context.getString(R.string.pref_about_up_to_date_title)
                    summary = latestReleaseJson.optString("body").ifEmpty {
                        context.getString(R.string.pref_about_up_to_date_summary)
                    }
                }
            }
        }

        companion object {
            private const val HIDDEN_FEATURE_TRIGGER_CLICK_COUNT = 20
            private const val HIDDEN_FEATURE_HINT_FROM_CLICK_COUNT = 17
        }
    }

    init {
        val activity = context as Activity

        val prefsFragment = PrefsFragment()
        activity.fragmentManager.beginTransaction().add(prefsFragment, "Settings").commit()
        activity.fragmentManager.executePendingTransactions()

        val inNightMode =
            (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val nightModeTextHookResult = if (inNightMode) {
            if (verbose) {
                YLog.debug("$TAG: night mode on, recoloring settings text white")
            }
            Preference::class.java.resolveMethod(
                Method(name = "onBindView", parameters = null)
            )?.hook {
                after {
                    val preference = instance as? Preference ?: run {
                        YLog.error("$TAG: bound target ${instance::class.qualifiedName} not a Preference, skip recolor")
                        return@after
                    }
                    if (preference is PreferenceCategory) {
                        return@after
                    }

                    val view = args[0] as? View ?: run {
                        YLog.error("$TAG: bound target ${instance::class.qualifiedName} has no view, skip recolor")
                        return@after
                    }
                    view.findViewById<TextView>(android.R.id.title)
                        ?.setTextColor(activity.resources.getColor(R.color.white))
                    view.findViewById<TextView>(android.R.id.summary)
                        ?.setTextColor(activity.resources.getColor(R.color.white_50))
                }
            }
        } else {
            null
        }

        setView(prefsFragment.view)
        setTitle(context.getString(R.string.settings_dialog_title))
        setNegativeButton(context.getString(R.string.settings_dialog_back), null)
        setPositiveButton(context.getString(R.string.settings_dialog_confirm_and_restart)) { _, _ ->
            restartApplication(activity)
        }
        setOnDismissListener {
            activity.runOnUiThread {
                Toast.makeText(context, context.getString(R.string.restart_required), Toast.LENGTH_SHORT).show()
            }
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
            nightModeTextHookResult?.remove()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        private val verbose
            get() = !ConfigManager.module.verboseDisabled.value

        private const val EXPORT_CONFIG = 0
        private const val IMPORT_CONFIG = 1

        fun show(context: Context) {
            if (VerifyDialog.shouldVerify()) {
                YLog.info("$TAG: unverified version, redirecting to verify dialog")
                VerifyDialog.show(context)
            } else {
                runCatching {
                    (context as? Activity)?.injectModuleAppResources()
                    SettingsDialog(context).show()
                }.onFailure {
                    YLog.error("$TAG: failed to show settings dialog", it)
                }
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
