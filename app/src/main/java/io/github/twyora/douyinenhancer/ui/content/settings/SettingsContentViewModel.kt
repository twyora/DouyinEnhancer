package io.github.twyora.douyinenhancer.ui.content.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import com.highcapable.yukihookapi.YukiHook
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.kvstorage.FastKVStorage
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.kvstorage.decodeAsKVMap
import io.github.twyora.douyinenhancer.config.kvstorage.encodeToKVBytes
import io.github.twyora.douyinenhancer.config.provider.ModuleConfigProvider
import io.github.twyora.douyinenhancer.constant.HookInfoFiles
import io.github.twyora.douyinenhancer.constant.SignatureKeys
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import io.github.twyora.douyinenhancer.utils.crypto.sha256Hex
import io.github.twyora.douyinenhancer.utils.crypto.verifySha256RsaSignature
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.security.MessageDigest
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.io.encoding.Base64
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import org.erdtman.jcs.JsonCanonicalizer
import org.json.JSONArray
import org.json.JSONObject

class SettingsContentViewModel : ViewModel() {
    val snackFlow: SharedFlow<SnackEvent>
        field = MutableSharedFlow<SnackEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val triggerNavigation: SharedFlow<NavKey>
        field = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)

    val triggerExportConfig: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val triggerImportConfig: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val triggerLoadCustomMapping: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private val saveConfig = with(ConfigManager.save) {
        combine(
            purifyCommentImage.observe(),
            unlockCommentEmoji.observe(),
            downloadCommentAudio.observe(),
            feedVideoRemoveWatermark.observe(),
            feedMultiImageRemoveWatermark.observe(),
            feedDownloadBypass.observe()
        ) { values ->
            SettingsContentSaveConfig(
                purifyCommentImage = values[0],
                unlockCommentEmoji = values[1],
                downloadCommentAudio = values[2],
                removeVideoWatermark = values[3],
                removeMultiImageWatermark = values[4],
                bypassVideoDownload = values[5]
            )
        }
    }

    private val feedState = with(ConfigManager.feed) {
        combine(
            interceptDoubleTapDigg.observe(),
            doubleTapOpenComment.observe(),
            bypassListenAwemeRestriction.observe(),
            blockAutoReplay.observe(),
            blockResumePlayback.observe()
        ) { disableDoubleTapDigg, doubleTapOpenComment, bypassListenAweme, blockAutoReplay, blockAutoResumePlayback ->
            SettingsContentFeedConfig(
                disableDoubleTapDigg = disableDoubleTapDigg,
                doubleTapOpenComment = doubleTapOpenComment,
                bypassListenAweme = bypassListenAweme,
                blockAutoReplay = blockAutoReplay,
                blockAutoResumePlayback = blockAutoResumePlayback
            )
        }
    }

    private val uiState = with(ConfigManager.ui) {
        combine(
            keepDanmakuVisible.observe(),
            hideFollowButton.observe(),
            keepCleanMode.observe()
        ) { keepDanmakuVisible, hideFollowButton, keepCleanMode ->
            SettingsContentUiConfig(
                keepDanmakuVisible = keepDanmakuVisible,
                hideFollowButton = hideFollowButton,
                keepCleanMode = keepCleanMode
            )
        }
    }

    private val moduleState = combine(
        ConfigManager.misc.hiddenFeatureEnabled.observe(),
        ConfigManager.module.verboseDisabled.observe()
    ) { hiddenFeature, disableVerbose ->
        SettingsContentModuleConfig(
            hiddenFeature = hiddenFeature,
            disableVerbose = disableVerbose
        )
    }

    val settingsConfig: StateFlow<SettingsContentConfig> = combine(
        saveConfig,
        feedState,
        uiState,
        moduleState
    ) { save, feed, ui, module ->
        SettingsContentConfig(
            saveState = save,
            feedState = feed,
            uiState = ui,
            moduleState = module
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsContentConfig(
            saveState = with(ConfigManager.save) {
                SettingsContentSaveConfig(
                    purifyCommentImage = purifyCommentImage.defValue,
                    unlockCommentEmoji = unlockCommentEmoji.defValue,
                    downloadCommentAudio = downloadCommentAudio.defValue,
                    removeVideoWatermark = feedVideoRemoveWatermark.defValue,
                    bypassVideoDownload = feedDownloadBypass.defValue,
                    removeMultiImageWatermark = feedMultiImageRemoveWatermark.defValue
                )
            },
            feedState = with(ConfigManager.feed) {
                SettingsContentFeedConfig(
                    disableDoubleTapDigg = interceptDoubleTapDigg.defValue,
                    doubleTapOpenComment = doubleTapOpenComment.defValue,
                    bypassListenAweme = bypassListenAwemeRestriction.defValue,
                    blockAutoReplay = blockAutoReplay.defValue,
                    blockAutoResumePlayback = blockResumePlayback.defValue
                )
            },
            uiState = with(ConfigManager.ui) {
                SettingsContentUiConfig(
                    keepDanmakuVisible = keepDanmakuVisible.defValue,
                    hideFollowButton = hideFollowButton.defValue,
                    keepCleanMode = keepCleanMode.defValue
                )
            },
            moduleState = SettingsContentModuleConfig(
                hiddenFeature = ConfigManager.misc.hiddenFeatureEnabled.defValue,
                disableVerbose = ConfigManager.module.verboseDisabled.defValue
            )
        )
    )

    fun requestExportConfig() {
        triggerExportConfig.tryEmit(Unit)
    }

    fun requestImportConfig() {
        triggerImportConfig.tryEmit(Unit)
    }

    fun requestLoadCustomMapping() {
        triggerLoadCustomMapping.tryEmit(Unit)
    }

    fun exportConfigZip(storages: List<IKVStorage>, zipOutputStream: OutputStream) {
        ZipOutputStream(zipOutputStream).use { out ->
            val manifestFiles = JSONArray()
            storages.forEach { storage ->
                val storageBytes = storage.encodeToKVBytes()
                out.putNextEntry(ZipEntry(storage.name))
                out.write(storageBytes)
                out.closeEntry()
                manifestFiles.put(
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
    }

    fun importV1ConfigZip(storages: List<IKVStorage>, checksumFile: File, storageDir: File) {
        val digest = MessageDigest.getInstance("SHA-256")
        storages.forEach { storage ->
            listOf("kva", "kvb").forEach { extension ->
                val file = File(storageDir, "${storage.name}.$extension")
                if (file.exists()) {
                    digest.update(file.readBytes())
                }
            }
        }
        val actualChecksum = digest.digest().joinToString("") {
            "%02x".format(it)
        }
        require(checksumFile.readText() == actualChecksum) {
            "v1 config checksum mismatch"
        }

        val snapshotHiddenFeature = ConfigManager.misc.hiddenFeatureEnabled.value
        storages.forEach { storage ->
            val importedSettings = FastKVStorage.open(storageDir.absolutePath, storage.name)
            try {
                storage.putAll(importedSettings.getAll())
            } finally {
                importedSettings.close()
            }
        }
        ConfigManager.misc.hiddenFeatureEnabled.value = snapshotHiddenFeature
    }

    fun importConfigZip(storages: List<IKVStorage>, inputFiles: List<File>, manifestFile: File) {
        if (!manifestFile.exists()) {
            throw IllegalArgumentException("manifest.json missing")
        }

        val manifestJson = JSONObject(String(manifestFile.readBytes(), Charsets.UTF_8))
        require(manifestJson.optString("algorithm") == "SHA-256") {
            "unsupported hash algorithm: ${manifestJson.optString("algorithm")}"
        }

        val filesArray = manifestJson.getJSONArray("files")
        val expectedHashes = (0 until filesArray.length()).associate {
            val entry = filesArray.getJSONObject(it)
            entry.getString("name") to entry.getString("hash")
        }

        val inputFilesByName = inputFiles.associateBy { it.name }
        val verifiedFiles = expectedHashes.map { (name, expectedHash) ->
            val file = requireNotNull(inputFilesByName[name]) {
                "missing imported file: $name"
            }
            val actualHash = file.readBytes().inputStream().sha256Hex()
            require(actualHash == expectedHash) {
                "hash mismatch: $name"
            }
            file
        }

        val storagesByName = storages.associateBy {
            it.name
        }
        val snapshotHiddenFeature = ConfigManager.misc.hiddenFeatureEnabled.value
        verifiedFiles.forEach { file ->
            val live = storagesByName[file.name] ?: return@forEach
            val importedBytes = file.readBytes()
            val importedMap = importedBytes.decodeAsKVMap()
            live.clear()
            live.putAll(importedMap)
        }
        ConfigManager.misc.hiddenFeatureEnabled.value = snapshotHiddenFeature
    }

    fun loadCustomHookInfo(hookInfoInputStream: InputStream) {
        val customHookInfoBytes = hookInfoInputStream.readBytes()
        val customHookInfoJson = JSONObject(String(customHookInfoBytes, Charsets.UTF_8))
        val declaredSignature = customHookInfoJson.optString("signature")
        customHookInfoJson.remove("signature")
        val canonicalHookInfoPresetBytes = JsonCanonicalizer(
            customHookInfoJson.toString()
        ).encodedString.toByteArray(Charsets.UTF_8)
        val signatureVerified = declaredSignature.isNotBlank() &&
            ByteArrayInputStream(canonicalHookInfoPresetBytes).use { stream ->
                verifySha256RsaSignature(
                    stream,
                    Base64.decode(declaredSignature),
                    Base64.decode(SignatureKeys.HOOK_INFO_PRESET_PUBLIC_KEY_B64)
                )
            }

        if (signatureVerified) {
            ConfigManager.module.hookInfoSource.value = ModuleConfigProvider.HOOK_INFO_SOURCE_TRUSTED_OVERLAY
            sendSnack(SnackEvent(R.string.import_success_restart_required))
        } else {
            ConfigManager.module.hookInfoSource.value = ModuleConfigProvider.HOOK_INFO_SOURCE_UNTRUSTED_OVERLAY
            sendSnack(SnackEvent(R.string.untrusted_obfuscation_map))
        }

        ConfigManager.module.hookInfoGeneration.value++
    }

    fun setPurifyCommentImage(enable: Boolean) {
        ConfigManager.save.purifyCommentImage.value = enable
    }

    fun setUnlockCommentEmoji(enable: Boolean) {
        ConfigManager.save.unlockCommentEmoji.value = enable
    }

    fun setDownloadCommentAudio(enable: Boolean) {
        ConfigManager.save.downloadCommentAudio.value = enable
    }

    fun setRemoveVideoWatermark(enable: Boolean) {
        ConfigManager.save.feedVideoRemoveWatermark.value = enable
    }

    fun setBypassVideoDownload(enable: Boolean) {
        ConfigManager.save.feedDownloadBypass.value = enable
    }

    fun setRemoveMultiImageWatermark(enable: Boolean) {
        ConfigManager.save.feedMultiImageRemoveWatermark.value = enable
    }

    fun setDisableDoubleTapDigg(enable: Boolean) {
        ConfigManager.feed.interceptDoubleTapDigg.value = enable
    }

    fun setDoubleTapOpenComment(enable: Boolean) {
        ConfigManager.feed.doubleTapOpenComment.value = enable
    }

    fun setBypassListenAweme(enable: Boolean) {
        ConfigManager.feed.bypassListenAwemeRestriction.value = enable
    }

    fun setBlockAutoReplay(enable: Boolean) {
        ConfigManager.feed.blockAutoReplay.value = enable
    }

    fun setBlockResumePlayback(enable: Boolean) {
        ConfigManager.feed.blockResumePlayback.value = enable
    }

    fun setKeepDanmakuVisible(enable: Boolean) {
        ConfigManager.ui.keepDanmakuVisible.value = enable
    }

    fun setHideFollowButton(enable: Boolean) {
        ConfigManager.ui.hideFollowButton.value = enable
    }

    fun setKeepCleanMode(enable: Boolean) {
        ConfigManager.ui.keepCleanMode.value = enable
    }

    fun setHiddenFeature(enable: Boolean) {
        ConfigManager.misc.hiddenFeatureEnabled.value = enable
    }

    fun setVerboseDisabled(enable: Boolean) {
        ConfigManager.module.verboseDisabled.value = enable
    }

    fun resetMapping() {
        ConfigManager.module.hookInfoGeneration.value++
        sendSnack(SnackEvent(R.string.success))
    }

    fun clearCustomMapping() {
        YukiHook.Module.sharedFiles.delete(
            HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME
        )
        ConfigManager.module.hookInfoSource.value = ModuleConfigProvider.HOOK_INFO_SOURCE_BUILTIN

        ConfigManager.module.hookInfoGeneration.value++
        sendSnack(SnackEvent(R.string.success))
    }

    fun requestNavigation(navKey: NavKey) {
        triggerNavigation.tryEmit(navKey)
    }

    fun sendSnack(event: SnackEvent) {
        snackFlow.tryEmit(event)
    }
}
