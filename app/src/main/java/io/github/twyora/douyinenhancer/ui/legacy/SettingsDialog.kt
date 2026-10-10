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
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.bridge.ModuleApp
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.provider.ModuleConfigProvider
import io.github.twyora.douyinenhancer.constant.HookInfoFiles
import io.github.twyora.douyinenhancer.constant.SignatureKeys
import io.github.twyora.douyinenhancer.hook.comment.CommentAudioHooker.hook
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.setFieldOrNull
import io.github.twyora.douyinenhancer.utils.toast
import io.github.twyora.douyinenhancer.utils.verifySha256RsaSignature
import java.io.ByteArrayInputStream
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
import kotlin.io.encoding.Base64
import kotlin.system.exitProcess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import org.erdtman.jcs.JsonCanonicalizer
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

            preferenceManager.setFieldOrNull(
                Field("mSharedPreferences"),
                // TODO: Urgent refactor required. This relies on internal implementation details
                ((ConfigManager.settingsStorage as FastKVStorage).fastKV) as SharedPreferences
            )
            preferenceManager.setFieldOrNull(Field("mEditor"), null)
            addPreferencesFromResource(R.xml.prefs_setting)

            if (!ConfigManager.misc.hiddenFeatureEnabled.value) {
                val miscCategory = findPreference("pref_category_misc") as? PreferenceCategory
                miscCategory?.let { category ->
                    findPreference(ConfigManager.misc.hiddenFeatureEnabled.key)?.let {
                        category.removePreference(it)
                    }
                    if (category.preferenceCount == 0) {
                        category.parent?.removePreference(category)
                    }
                }
            }

            findPreference("recommend_feed_filter")?.onPreferenceClickListener = this
            findPreference("playback_component_block")?.onPreferenceClickListener = this
            findPreference("bottom_tab_block")?.onPreferenceClickListener = this
            findPreference("export_config")?.onPreferenceClickListener = this
            findPreference("import_config")?.onPreferenceClickListener = this
            (findPreference("disable_verbose_logs") as? SwitchPreference)?.apply {
                isChecked = ConfigManager.module.verboseDisabled.value
                onPreferenceChangeListener = this@PrefsFragment
            }
            findPreference("invalid_hook_info")?.onPreferenceClickListener = this
            findPreference("load_custom_hook_info")?.onPreferenceClickListener = this
            findPreference("reset_custom_hook_info")?.onPreferenceClickListener = this
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
                RecommendedFeedFilterDialog.show(context)
                true
            }

            "playback_component_block" -> {
                PlaybackComponentBlockDialog.show(context)
                true
            }

            "bottom_tab_block" -> {
                HomeTabBlockDialog.show(context)
                true
            }

            "version" -> {
                if (!ConfigManager.misc.hiddenFeatureEnabled.value) {
                    if (++hiddenFeatureClickCount == HIDDEN_FEATURE_TRIGGER_CLICK_COUNT) {
                        ConfigManager.misc.hiddenFeatureEnabled.value = true
                        context.toast(ModuleApp.instance.resources.getString(R.string.pref_misc_enable_hidden_features_restart_required))
                    } else if (hiddenFeatureClickCount >= HIDDEN_FEATURE_HINT_FROM_CLICK_COUNT) {
                        context.toast(
                            ModuleApp.instance.resources.getString(
                                R.string.pref_misc_enable_hidden_features_steps_remaining,
                                HIDDEN_FEATURE_TRIGGER_CLICK_COUNT - hiddenFeatureClickCount
                            )
                        )
                    }
                } else {
                    context.toast(ModuleApp.instance.resources.getString(R.string.pref_misc_enable_hidden_features_already_enabled))
                }
                true
            }

            "export_config" -> onExportConfigClick()

            "import_config" -> onImportConfigClick()

            "invalid_hook_info" -> {
                ConfigManager.module.hookInfoGeneration.value++
                context.toast(ModuleApp.instance.resources.getString(R.string.success))
                true
            }

            "load_custom_hook_info" -> onLoadCustomHookInfoClick()

            "reset_custom_hook_info" -> {
                val presetFile = File(context.cacheDir, HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME)
                if (presetFile.exists()) {
                    presetFile.writeText("")
                }
                ConfigManager.module.hookInfoGeneration.value++
                context.toast(ModuleApp.instance.resources.getString(R.string.success))

                true
            }

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
                                context.toast(
                                    ModuleApp.instance.resources.getString(
                                        R.string.config_export_failed,
                                        it.message ?: it.toString()
                                    )
                                )
                                YLog.error("$TAG: export config failed", it)
                            }.onSuccess {
                                context.toast(ModuleApp.instance.resources.getString(R.string.config_export_success))
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
                                    throw IOException(ModuleApp.instance.resources.getString(R.string.config_import_corrupted))
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
                                context.toast(
                                    ModuleApp.instance.resources.getString(
                                        R.string.config_import_failed,
                                        it.message ?: it.toString()
                                    )
                                )
                                YLog.error("$TAG: import config failed", it)
                            }.onSuccess {
                                context.toast(ModuleApp.instance.resources.getString(R.string.config_import_success))
                            }
                            settingsKvaTemp.delete()
                            settingsKvbTemp.delete()
                        }
                    }
                }

                LOAD_CUSTOM_HOOK_INFO -> {
                    val uri = data?.data
                    if (resultCode == RESULT_CANCELED || uri == null) {
                        return
                    }
                    runCatching {
                        val customHookInfoBytes = requireNotNull(
                            activity.contentResolver.openInputStream(uri)
                        ) {
                            "custom hook info file input stream is null"
                        }.use {
                            it.readBytes()
                        }
                        val customHookInfoJson = JSONObject(customHookInfoBytes.toString(Charsets.UTF_8))
                        val expectedSignature = customHookInfoJson.optString("signature")

                        customHookInfoJson.remove("signature")
                        val canonicalHookInfoPresetBytes = JsonCanonicalizer(
                            customHookInfoJson.toString()
                        ).encodedString.toByteArray(Charsets.UTF_8)

                        if (expectedSignature.isBlank() ||
                            ByteArrayInputStream(canonicalHookInfoPresetBytes).use { stream ->
                                !verifySha256RsaSignature(
                                    stream,
                                    Base64.decode(expectedSignature),
                                    Base64.decode(SignatureKeys.HOOK_INFO_PRESET_PUBLIC_KEY_B64)
                                )
                            }
                        ) {
                            context.toast(ModuleApp.instance.resources.getString(R.string.untrusted_obfuscation_map))
                        }

                        File(
                            context.cacheDir,
                            HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME
                        ).outputStream().use { out ->
                            out.write(customHookInfoBytes)
                        }
                        ConfigManager.module.hookInfoGeneration.value++
                    }.onFailure {
                        context.toast(
                            ModuleApp.instance.resources.getString(
                                R.string.import_failed,
                                it.message ?: it.toString()
                            )
                        )
                        YLog.error("$TAG: load custom hook info failed", it)
                    }.onSuccess {
                        context.toast(ModuleApp.instance.resources.getString(R.string.import_success_restart_required))
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
                startActivityForResult(
                    Intent.createChooser(intent, ModuleApp.instance.resources.getString(R.string.config_export_chooser)),
                    EXPORT_CONFIG
                )
            }.onFailure {
                context.toast(it.message ?: it.toString())
            }

            return true
        }

        private fun onImportConfigClick(): Boolean {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/zip"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            runCatching {
                startActivityForResult(
                    Intent.createChooser(intent, ModuleApp.instance.resources.getString(R.string.config_import_chooser)),
                    IMPORT_CONFIG
                )
            }.onFailure {
                context.toast(it.message ?: it.toString())
            }

            return true
        }

        private fun onLoadCustomHookInfoClick(): Boolean {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
            intent.type = "application/json"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            runCatching {
                startActivityForResult(
                    Intent.createChooser(intent, ModuleApp.instance.resources.getString(R.string.load_custom_hook_info_chooser)),
                    LOAD_CUSTOM_HOOK_INFO
                )
            }.onFailure {
                context.toast(it.message ?: it.toString())
            }

            return true
        }

        private fun checkUpdate() = scope.launch {
            val latestReleaseURL = ModuleApp.instance.resources.getString(R.string.latest_release_api_url)
            val latestReleaseJson = runCatching {
                withContext(Dispatchers.IO) {
                    JSONObject(
                        URL(latestReleaseURL).readText()
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
                    title = ModuleApp.instance.resources.getString(R.string.pref_about_update_available_title)
                    summary = latestReleaseJson.optString("body").takeIf {
                        it.isNotBlank()
                    }?.let {
                        if (it.length > 80) {
                            it.take(80) + "..."
                        } else {
                            it
                        }
                    } ?: ModuleApp.instance.resources.getString(R.string.pref_about_update_available_summary)
                }
                val counter = ConfigManager.module.notifyUpdateCooldown.value
                val newCounter =
                    (counter - 1 + ModuleConfigProvider.NOTIFY_UPDATE_COOLDOWN_PERIOD) % ModuleConfigProvider.NOTIFY_UPDATE_COOLDOWN_PERIOD
                ConfigManager.module.notifyUpdateCooldown.value = newCounter
                if (newCounter == 0) {
                    context.toast(ModuleApp.instance.resources.getString(R.string.notify_update_available))
                }
            } else {
                findPreference("update")?.apply {
                    title = ModuleApp.instance.resources.getString(R.string.pref_about_up_to_date_title)
                    summary = latestReleaseJson.optString("body").ifEmpty {
                        ModuleApp.instance.resources.getString(R.string.pref_about_up_to_date_summary)
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
            Preference::class.java.resolveMethodOrNull(
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
                        ?.setTextColor(ModuleApp.instance.resources.getColor(R.color.white))
                    view.findViewById<TextView>(android.R.id.summary)
                        ?.setTextColor(ModuleApp.instance.resources.getColor(R.color.white_50))
                }
            }
        } else {
            null
        }

        setView(prefsFragment.view)
        setTitle(ModuleApp.instance.resources.getString(R.string.settings_dialog_title))
        setNegativeButton(ModuleApp.instance.resources.getString(R.string.settings_dialog_back), null)
        setPositiveButton(ModuleApp.instance.resources.getString(R.string.settings_dialog_confirm_and_restart)) { _, _ ->
            restartApplication(activity)
        }
        setOnDismissListener {
            context.toast(ModuleApp.instance.resources.getString(R.string.restart_required))
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
        private const val LOAD_CUSTOM_HOOK_INFO = 2

        fun show(context: Context) {
            if (VerifyDialog.shouldVerify(context)) {
                YLog.info("$TAG: unverified version, redirecting to verify dialog")
                VerifyDialog.show(context)
            } else {
                runCatching {
                    context.injectModuleAppResources()
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
