@file:Suppress("DEPRECATION")

package io.github.twyora.douyinenhancer.ui.legacy

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.Preference
import android.preference.PreferenceFragment
import android.view.ContextThemeWrapper
import androidx.core.net.toUri
import com.highcapable.yukihookapi.hook.factory.injectModuleResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.kvstorage.encodeToKVBytes
import io.github.twyora.douyinenhancer.constant.IKVStorages
import io.github.twyora.douyinenhancer.utils.android.toast
import io.github.twyora.douyinenhancer.utils.crypto.sha256Hex
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.json.JSONArray
import org.json.JSONObject

class LegacyConfigDeprecationDialog(context: Context) :
    AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {

    private val fragment = PrefsFragment()

    init {
        val activity = context as Activity

        activity.fragmentManager.beginTransaction().add(fragment, TAG).commit()
        activity.fragmentManager.executePendingTransactions()

        setView(fragment.view)
        setTitle(R.string.app_name)
        setNegativeButton(android.R.string.cancel, null)
        setOnDismissListener {
            activity.fragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    class PrefsFragment :
        PreferenceFragment(),
        Preference.OnPreferenceClickListener {

        private val scope = MainScope()

        @Deprecated("Deprecated in Java")
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_legacy_config_deprecation)
            findPreference(KEY_GO_TO_NEW_SETTINGS)?.onPreferenceClickListener = this
            findPreference(KEY_EXPORT)?.onPreferenceClickListener = this
            findPreference(KEY_OPEN_LEGACY_SETTINGS)?.onPreferenceClickListener = this
        }

        @Deprecated("Deprecated in Java")
        override fun onDestroy() {
            super.onDestroy()
            scope.cancel()
        }

        @Deprecated("Deprecated in Java")
        override fun onPreferenceClick(preference: Preference?) = when (preference?.key) {
            KEY_GO_TO_NEW_SETTINGS -> {
                context.startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        "douyinenhancer://io.github.twyora.douyinenhancer/settings/main".toUri()
                    )
                )
                true
            }

            KEY_EXPORT -> onExportClick()

            KEY_OPEN_LEGACY_SETTINGS -> {
                SettingsDialog.show(context)
                true
            }

            else -> false
        }

        @Deprecated("Deprecated in Java")
        override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            val uri = data?.data
            if (resultCode == Activity.RESULT_CANCELED || uri == null) {
                return
            }

            when (requestCode) {
                EXPORT_CONFIG -> scope.launch(Dispatchers.IO) {
                    runCatching {
                        context.contentResolver.openOutputStream(uri)?.use { os ->
                            val storage = FastKVStorage.open(context, IKVStorages.SETTINGS_STORAGE_NAME)
                            try {
                                ZipOutputStream(os).use { out ->
                                    val storageBytes = storage.encodeToKVBytes()
                                    out.putNextEntry(ZipEntry(storage.name))
                                    out.write(storageBytes)
                                    out.closeEntry()
                                    val manifestFiles = JSONArray().apply {
                                        put(
                                            JSONObject().apply {
                                                put("name", storage.name)
                                                put("hash", storageBytes.inputStream().sha256Hex())
                                            }
                                        )
                                    }
                                    val manifest = JSONObject().apply {
                                        put("schemaVersion", 2)
                                        put("algorithm", "SHA-256")
                                        put("exportVersionCode", BuildConfig.VERSION_CODE)
                                        put("files", manifestFiles)
                                    }
                                    out.putNextEntry(ZipEntry("manifest.json"))
                                    out.write(manifest.toString().toByteArray())
                                    out.closeEntry()
                                }
                            } finally {
                                storage.close()
                            }
                        } ?: throw IOException("cannot open output uri")
                    }.onSuccess {
                        withContext(Dispatchers.Main) {
                            context.toast(context.getString(R.string.config_export_success))
                        }
                    }.onFailure {
                        YLog.error("$TAG: export settings failed", it)
                        withContext(Dispatchers.Main) {
                            context.toast(context.getString(R.string.config_export_failed, it.message ?: it.toString()))
                        }
                    }
                }
            }
        }

        private fun onExportClick(): Boolean {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                type = "application/zip"
                putExtra(
                    Intent.EXTRA_TITLE,
                    "douyinenhancer_settings_backup_${
                        SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())
                    }.zip"
                )
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            runCatching {
                startActivityForResult(
                    Intent.createChooser(intent, context.getString(R.string.config_export_chooser)),
                    EXPORT_CONFIG
                )
            }.onFailure {
                context.toast(it.message ?: it.toString())
            }
            return true
        }

        companion object {
            private const val EXPORT_CONFIG = 0
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        private const val KEY_GO_TO_NEW_SETTINGS = "go_to_new_settings"
        private const val KEY_EXPORT = "export"
        private const val KEY_OPEN_LEGACY_SETTINGS = "open_legacy_settings"

        fun show(context: Context) {
            runCatching {
                context.injectModuleResources()
                LegacyConfigDeprecationDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: failed to show LegacyConfigDeprecationDialog", it)
            }
        }
    }
}
