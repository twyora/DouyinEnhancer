package io.github.twyora.douyinenhancer.hook.comment

import android.content.Context
import android.net.Uri
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.crypto.md5Hex
import io.github.twyora.douyinenhancer.utils.file.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.hook.HookTransaction
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOnlyQuietly
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeStaticMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setFieldQuietly
import java.io.FileInputStream
import org.json.JSONObject

@HookOnMainProcess
object CommentAudioHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.save.downloadCommentAudio.value) {
            YLog.info("$TAG: download comment audio is disabled, skipping hook")
            return
        }

        val transaction = HookTransaction(TAG)

        transaction.add(::installForceSaveImageVisibleHook.name) {
            installForceSaveImageVisibleHook()
        }
        transaction.add(::installAddSaveImageToWhiteListHook.name) {
            installAddSaveImageToWhiteListHook()
        }
        transaction.add(::installInjectAudioUrlOnSaveClickHook.name) {
            installInjectAudioUrlOnSaveClickHook()
        }
        transaction.add(::installSaveDownloadedAudioHook.name) {
            installSaveDownloadedAudioHook()
        }

        transaction.commit()
    }

    private fun installForceSaveImageVisibleHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        // if the comment contains audio, show the save image button
        return packageInstance.saveImageActionItem.selfClass?.resolveMethodOrNull(
            packageInstance.saveImageActionItem.isVisible()
        )?.intercept {
            val visible = proceed<Boolean>()
            if (visible == true) {
                return@intercept visible
            }

            // if the comment contains audio, show the save image button
            val commentAudio = instance.getFieldOrNull(
                packageInstance.commentLongPressItemModel.commentActionParams()
            )?.getFieldOrNull(
                packageInstance.commentActionParams.comment()
            )?.getFieldOrNull(
                packageInstance.comment.commentAudio()
            )?.also {
                if (verbose) {
                    YLog.debug("$TAG: comment contains audio, showing save image button")
                }
            }

            if (commentAudio != null) {
                true
            } else {
                proceed()
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to make save image button visible for audio", throwable)
            }
        }
    }

    private fun installAddSaveImageToWhiteListHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.commentLongPressWhiteListProvider.selfClass?.resolveMethodOrNull(
            packageInstance.commentLongPressWhiteListProvider.buildWhiteList()
        )?.intercept {
            @Suppress("UNCHECKED_CAST")
            val whiteList = proceed<MutableSet<String>>() ?: run {
                YLog.error("$TAG: white list is null")
                return@intercept null
            }
            if (whiteList.contains("save_image")) {
                return@intercept whiteList
            }

            args.firstOrNull {
                packageInstance.comment.selfClass?.isInstance(it) == true
            }?.getFieldOrNull(
                packageInstance.commentActionParams.comment()
            )?.getFieldOrNull(
                packageInstance.comment.commentAudio()
            )?.also {
                if (verbose) {
                    YLog.debug("$TAG: comment contains audio, added \"save_image\" to white list")
                }
            }?.let {
                whiteList.add("save_image")
            }

            whiteList
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to add \"save_image\" to white list", throwable)
            }
        }
    }

    private fun installInjectAudioUrlOnSaveClickHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.saveImageActionItem.selfClass?.resolveMethodOrNull(
            packageInstance.saveImageActionItem.onClick()
        )?.intercept {
            val comment = instance.getFieldOrNull(
                packageInstance.commentLongPressItemModel.commentActionParams()
            )?.getFieldOrNull(
                packageInstance.commentActionParams.comment()
            ) ?: run {
                YLog.error("$TAG: failed to get comment from save image action item")
                return@intercept proceed()
            }

            val commentAudioContent = comment.getFieldOrNull(
                packageInstance.comment.commentAudio()
            )?.getFieldOrNull<String>(
                packageInstance.commentAudioStruct.content()
            ) ?: return@intercept proceed()

            val commentAudioJson = runCatching {
                JSONObject(commentAudioContent)
            }.getOrElse {
                YLog.error("$TAG: failed to parse comment audio content as JSON", it)
                return@intercept proceed()
            }

            val firstCommentAudio = commentAudioJson.optJSONArray("video_list")?.optJSONObject(0) ?: run {
                YLog.error("$TAG: no video_list in comment audio content")
                return@intercept proceed()
            }
            val commentAudioMainUrl = firstCommentAudio.optString("main_url")
            val commentAudioBackupUrl = firstCommentAudio.optString("backup_url")
            val audioUrls = listOf(
                commentAudioMainUrl,
                commentAudioBackupUrl
            ).filter {
                !it.isNullOrBlank()
            }.ifEmpty {
                YLog.error("$TAG: no valid audio URL found in comment audio content, cannot inject")
                return@intercept proceed()
            }

            injectAudioUrl(comment, audioUrls)
            if (verbose) {
                YLog.debug("$TAG: injected ${audioUrls.size} audio URL(s) into comment")
            }

            proceed(arrayOf(0))
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to inject audio URLs into save image download pipeline", throwable)
            }
        }
    }

    private fun installSaveDownloadedAudioHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.commentImageSaveDownloadListener.selfClass?.resolveMethodOrNull(
            packageInstance.commentImageSaveDownloadListener.onSuccessed()
        )?.intercept {
            val downloadInfo = args.firstOrNull {
                packageInstance.downloadInfo.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: downloadInfo not found in argument list")
                return@intercept proceed()
            }

            val sourcePath = downloadInfo.invokeMethodOrNull<String>(
                packageInstance.downloadInfo.getTargetFilePath()
            ) ?: run {
                YLog.error("$TAG: failed to get downloaded audio file path")
                return@intercept proceed()
            }
            val ftypeInfo = FileTypeDetector.detect(sourcePath)

            val dlUrl = downloadInfo.getFieldOrNull<String>(
                packageInstance.downloadInfo.url()
            ) ?: run {
                YLog.error("$TAG: failed to get download url from download info")
                return@intercept proceed()
            }
            if (!dlUrl.contains("mime_type=audio")) {
                if (verbose) {
                    YLog.debug("$TAG: download url is not an audio url, skipping saving audio")
                }
                return@intercept proceed()
            } else if (!ftypeInfo.mimeType.startsWith("audio/")) {
                YLog.error("$TAG: audio MIME type restricted: ${ftypeInfo.mimeType}")
                return@intercept proceed()
            }

            val context = instance.getFieldOrNull(
                packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
            )?.getFieldOrNull<Context>(
                packageInstance.listenerProviderParam.context()
            ) ?: run {
                YLog.error("$TAG: unable to get Context from download listener")
                return@intercept proceed()
            }

            val targetFileName = "audio_${
                (dlUrl + System.currentTimeMillis().toString()).md5Hex()
            }.${ftypeInfo.extensions.first()}".also {
                if (verbose) {
                    YLog.debug("$TAG: audio file name: $it")
                }
            }
            val targetUri = packageInstance.ugFileUtils.selfClass?.invokeStaticMethodOrNull<Uri>(
                packageInstance.ugFileUtils.getAudioUri(),
                context,
                targetFileName,
                ftypeInfo.mimeType,
                "Music/douyin/audio",
                instance.getFieldOrNull(
                    packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
                )?.getFieldOrNull(
                    packageInstance.listenerProviderParam.cert()
                )
            )?.takeIf {
                it != Uri.EMPTY
            } ?: run {
                YLog.error("$TAG: audioUri is null or empty")
                return@intercept proceed()
            }

            val copyRet = runCatching {
                context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                    FileInputStream(sourcePath).use { inputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
                true
            }.onFailure {
                YLog.error("$TAG: failed to copy audio file to media store", it)
            }.getOrElse {
                return@intercept proceed()
            }

            instance.invokeMethodOnlyQuietly(
                packageInstance.commentImageSaveDownloadListener.notifyResult(),
                context,
                copyRet
            )

            // short-circuits the remaining chain and the original method, returning null to the caller
            null
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to save downloaded audio to media store", throwable)
            }
        }
    }

    private fun injectAudioUrl(comment: Any, audioUrls: List<String>): Boolean {
        val existingImageUrlList = comment.getFieldOrNull<List<*>>(
            packageInstance.comment.imageList()
        )
        val imageUrlList = if (existingImageUrlList.isNullOrEmpty()) {
            val newImageUrlList = listOf(
                packageInstance.commentImageStruct.selfClass?.createInstance()
            )
            comment.setFieldQuietly(
                packageInstance.comment.imageList(),
                newImageUrlList
            )
            newImageUrlList
        } else {
            existingImageUrlList
        }

        val existingImageUrlModel = imageUrlList.first()?.getFieldOrNull(
            packageInstance.commentImageStruct.downloadUrl()
        )
        val imageUrlModel = if (existingImageUrlModel == null) {
            val newImageUrlModel = packageInstance.urlModel.selfClass?.createInstance()
            imageUrlList.first()?.setFieldQuietly(
                packageInstance.commentImageStruct.downloadUrl(),
                newImageUrlModel
            )
            newImageUrlModel
        } else {
            existingImageUrlList
        } ?: return false

        imageUrlModel.setFieldQuietly(
            packageInstance.urlModel.urlList(),
            audioUrls
        )

        return true
    }
}
