package io.github.twyora.douyinenhancer.hook.comment

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.core.graphics.createBitmap
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.shakster.gifkt.GifEncoder
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.crypto.md5Hex
import io.github.twyora.douyinenhancer.utils.file.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.hook.HookTransaction
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.getStaticFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOnlyQuietly
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeStaticMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setFieldQuietly
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@HookOnMainProcess
object CommentEmojiHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    private val packageInstance
        get() = DouyinPackage.instance

    override fun onHook() {
        if (!ConfigManager.save.unlockCommentEmoji.value) {
            YLog.info("$TAG: unlock comment emoji is disabled, skipping hook")
            return
        }

        val transaction = HookTransaction(TAG)
        // Force "save to album" button visible for emoji comments.
        transaction.add(::installSaveEmojiToAlbumButtonHook.name) {
            installSaveEmojiToAlbumButtonHook()
        }
        // Before the save button's download callback fires, inject emoji URLs into
        // comment.imageList[0].downloadUrl so the downloader picks them up.
        // Without it: the downloader fetches nothing because imageList is empty.
        transaction.add(::installClickSaveEmojiToAlbumButtonCallbackHook.name) {
            installClickSaveEmojiToAlbumButtonCallbackHook()
        }
        // Intercept emoji download completion: detect real file type, convert video
        // to GIF if needed, copy to album, show result toast, skip original handler
        // — otherwise every downloaded emoji is saved as .png with MIME image/png.
        transaction.add(::installEmojiDownloadedCallbackHook.name) {
            installEmojiDownloadedCallbackHook()
        }
        // The host app does not implement createUri for some file types (e.g. heif), so it
        // returns no URI for them. This hook supplements that missing logic: when createUri
        // yields no URI, it builds one via getImageUri manually and also writes it into the
        // caller's out-parameter array.
        transaction.add(::installCreateUriHook.name) {
            installCreateUriHook()
        }
        transaction.commit()
    }

    private fun installSaveEmojiToAlbumButtonHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.saveImageActionItem.selfClass?.resolveMethodOrNull(
            packageInstance.saveImageActionItem.isVisible()
        )?.intercept {
            val comment = instance.getFieldOrNull(
                packageInstance.commentLongPressItemModel.commentActionParams()
            )?.getFieldOrNull(
                packageInstance.commentActionParams.comment()
            ) ?: run {
                YLog.error("$TAG: failed to get comment from save image action item")
                return@intercept proceed()
            }

            val emojiUrls = comment.getFieldOrNull(
                packageInstance.comment.emoji()
            )?.getFieldOrNull(
                packageInstance.emoji.animateUrl()
            )?.getFieldOrNull<List<String>>(
                packageInstance.urlModel.urlList()
            )?.takeIf {
                it.isNotEmpty()
            }?.also {
                if (verbose) {
                    YLog.debug("$TAG: emoji comment detected, forcing save image button visible")
                }
            }

            if (emojiUrls != null) {
                true
            } else {
                proceed()
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to make save image button visible for emoji", throwable)
            }
        }
    }

    private fun installClickSaveEmojiToAlbumButtonCallbackHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.saveImageActionItem.selfClass?.resolveMethodOrNull(
            packageInstance.saveImageActionItem.onClick()
        )?.intercept {
            val comment = instance.getFieldOrNull(
                packageInstance.commentLongPressItemModel.commentActionParams()
            )?.getFieldOrNull(
                packageInstance.commentActionParams.comment()
            ) ?: run {
                YLog.error("$TAG: failed to get comment from ${instance::class.qualifiedName}")
                return@intercept proceed()
            }

            val emojiUrls = comment.getFieldOrNull(
                packageInstance.comment.emoji()
            )?.getFieldOrNull(
                packageInstance.emoji.animateUrl()
            )?.getFieldOrNull<List<String>>(
                packageInstance.urlModel.urlList()
            ) ?: return@intercept proceed()

            if (emojiUrls.isEmpty()) {
                YLog.warn("$TAG: emoji URL list is empty, skipping emoji injection")
                return@intercept proceed()
            }

            injectEmojiUrls(comment, emojiUrls)
            if (verbose) {
                YLog.debug("$TAG: injected ${emojiUrls.size} emoji URL(s) into comment")
            }

            proceed(arrayOf(0))
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to inject emoji URL(s) into save image download pipeline", throwable)
            }
        }
    }

    private fun installEmojiDownloadedCallbackHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.commentImageSaveDownloadListener.selfClass?.resolveMethodOrNull(
            packageInstance.commentImageSaveDownloadListener.onSuccessed()
        )?.intercept {
            val downloadInfo = args.firstOrNull {
                packageInstance.downloadInfo.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: downloadInfo not found in argument list")
                return@intercept proceed()
            }

            val dlUrl = downloadInfo.getFieldOrNull<String>(
                packageInstance.downloadInfo.url()
            ) ?: run {
                YLog.error("$TAG: failed to get download url from download info, skipping emoji handling")
                return@intercept proceed()
            }
            // emoji animateUrl file extension is always .heif
            if (!dlUrl.contains(".heif")) {
                if (verbose) {
                    YLog.debug("$TAG: download url does not contain \".heif\", skipping")
                }
                return@intercept proceed()
            }

            // downloaded file save path
            val sourcePath = downloadInfo.invokeMethodOrNull<String?>(
                packageInstance.downloadInfo.getTargetFilePath()
            ) ?: run {
                YLog.error("$TAG: failed to get downloaded emoji file path")
                return@intercept proceed()
            }
            val sourceFileInfo = FileTypeDetector.detect(sourcePath)
            YLog.info("$TAG: source MIME type: ${sourceFileInfo.mimeType}")

            val saveFilePrefix = "comment_${
                (dlUrl + System.currentTimeMillis().toString()).md5Hex()
            }"

            // prepare file to save
            var fileToSave = sourcePath
            var saveFileExt = sourceFileInfo.extensions.first()
            var hasTempFile = false

            if (!sourceFileInfo.mimeType.startsWith("image/") &&
                !sourceFileInfo.mimeType.startsWith("video/")
            ) {
                YLog.error(
                    "$TAG: source MIME type restricted: ${sourceFileInfo.mimeType}"
                )
                return@intercept proceed()
            }

            // convert video source to GIF animation
            if (sourceFileInfo.mimeType.startsWith("video/")) {
                YLog.info("$TAG: source MIME type is video, converting to GIF")

                val gifTempPath = "${
                    packageInstance.ugFileUtils.selfClass?.invokeStaticMethodOrNull<String>(
                        packageInstance.ugFileUtils.getStorageDir(),
                        "/comment/images",
                        false
                    )
                }${File.separator}$saveFilePrefix.gif"

                if (convertMedia2Gif(sourcePath, gifTempPath)) {
                    fileToSave = gifTempPath
                    saveFileExt = "gif"
                    hasTempFile = true
                } else {
                    YLog.error("$TAG: convert video to GIF failed, falling back to direct copy")
                }
            }

            // copy to album
            val saveFilePath = packageInstance.ugFileUtils.selfClass?.invokeStaticMethodOrNull<String>(
                packageInstance.ugFileUtils.getExternalStorageDir(),
                "/douyin/comment",
                false,
                false
            )?.let {
                "$it${File.separator}$saveFilePrefix.$saveFileExt"
            }?.also {
                if (verbose) {
                    YLog.debug("$TAG: saving emoji to $it")
                }
            } ?: run {
                YLog.error("$TAG: failed to get album save path")
                return@intercept proceed()
            }

            val cpRet = packageInstance.ugFileUtils.selfClass?.invokeStaticMethodOrNull<Boolean>(
                packageInstance.ugFileUtils.copyFile(),
                fileToSave,
                saveFilePath,
                false,
                null,
                instance.getFieldOrNull(
                    packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
                )?.getFieldOrNull(
                    packageInstance.listenerProviderParam.cert()
                )
            )

            // shows success dialog
            val context = instance.getFieldOrNull(
                packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
            )?.getFieldOrNull<Context>(
                packageInstance.listenerProviderParam.context()
            ) ?: run {
                YLog.error("$TAG: unable to get Context from download listener")
                return@intercept proceed()
            }

            instance.invokeMethodOnlyQuietly(
                packageInstance.commentImageSaveDownloadListener.notifyResult(),
                context,
                cpRet
            )

            if (hasTempFile) {
                File(fileToSave).delete()
            }

            null
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to run emoji download completion callback", throwable)
            }
        }
    }

    private fun installCreateUriHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.ugFileUtils.selfClass?.resolveMethodOrNull(
            packageInstance.ugFileUtils.createUri()
        )?.intercept {
            val uriRet = proceed<Uri?>()
            // original createUri succeeded
            if (uriRet != null && uriRet != Uri.EMPTY) {
                return@intercept uriRet
            }

            // original createUri failed; build a URI manually via getImageUri
            val filePath = args[0] as? String
            if (filePath.isNullOrEmpty()) {
                return@intercept uriRet
            }
            val fileExt = filePath.substringAfterLast('.', "")

            val fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt)
            if (fileMimeType == null) {
                YLog.error("$TAG: createUri failed, unable to resolve MIME type")
                return@intercept uriRet
            } else if (
                !(fileMimeType.startsWith("image/") || fileMimeType.startsWith("video/"))
            ) {
                YLog.error(
                    "$TAG: MIME type restricted when invoking createUri: $fileMimeType"
                )
                return@intercept uriRet
            }

            val fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1)
            // ensure the virtual path has no leading '/' but strictly retains a trailing '/'.
            val fileRelPath = filePath.substring(
                1,
                filePath.lastIndexOf(File.separator) + 1
            )

            val context = packageInstance.ugFileUtils.selfClass?.getStaticFieldOrNull<Context>(
                packageInstance.ugFileUtils.context()
            ) ?: run {
                YLog.error("$TAG: unable to get Context from ugFileUtils")
                return@intercept uriRet
            }
            val tokenCert = args[3]

            val finalUri = packageInstance.ugFileUtils.selfClass?.invokeStaticMethodOrNull<Uri>(
                packageInstance.ugFileUtils.getImageUri(),
                context,
                fileName,
                fileMimeType,
                fileRelPath,
                tokenCert
            ) ?: run {
                YLog.error("$TAG: failed to create URI for $fileName")
                return@intercept uriRet
            }

            // replace the hook's return value with the URI we just created
            args.firstNotNullOfOrNull {
                @Suppress("UNCHECKED_CAST")
                it as? Array<Uri>
            }?.takeIf {
                it.isNotEmpty()
            }?.let {
                it[0] = finalUri
            }
            finalUri
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to build URI for image or video", throwable)
            }
        }
    }

    private fun injectEmojiUrls(comment: Any, emojiUrls: List<String>) {
        var imageList = comment.getFieldOrNull<List<*>>(
            packageInstance.comment.imageList()
        )
        if (imageList.isNullOrEmpty()) {
            val newStruct = packageInstance.commentImageStruct.selfClass?.createInstance()
            imageList = listOf(newStruct)
            comment.setFieldQuietly(
                packageInstance.comment.imageList(),
                imageList
            )
        }

        val targetStruct = imageList[0]
        var urlModel = targetStruct?.getFieldOrNull(
            packageInstance.commentImageStruct.downloadUrl()
        )
        if (urlModel == null) {
            urlModel = packageInstance.urlModel.selfClass?.createInstance()
            targetStruct?.setFieldQuietly(
                packageInstance.commentImageStruct.downloadUrl(),
                urlModel
            )
        }
        urlModel?.setFieldQuietly(packageInstance.urlModel.urlList(), emojiUrls)
    }

    private fun convertMedia2Gif(mediaPath: String, gifPath: String): Boolean {
        val heifImage = packageInstance.heifImage.selfClass?.invokeStaticMethodOrNull(
            packageInstance.heifImage.create(),
            File(mediaPath).readBytes()
        )
        if (heifImage == null) {
            YLog.error("$TAG: failed to create HeifImage from $mediaPath")
            return false
        }

        try {
            val frameCount = heifImage.invokeMethodOrNull<Int>(
                packageInstance.heifImage.getFrameCount()
            ) ?: run {
                YLog.error("$TAG: failed to get frame count")
                return false
            }
            val frameDuration = heifImage.invokeMethodOrNull<IntArray>(
                packageInstance.heifImage.getFrameDurations()
            ) ?: run {
                YLog.error("$TAG: failed to get frame durations")
                return false
            }
            val frameWidth = heifImage.invokeMethodOrNull<Int>(
                packageInstance.heifImage.getWidth()
            ) ?: run {
                YLog.error("$TAG: failed to get frame width")
                return false
            }
            val frameHeight = heifImage.invokeMethodOrNull<Int>(
                packageInstance.heifImage.getHeight()
            ) ?: run {
                YLog.error("$TAG: failed to get frame height")
                return false
            }

            YLog.info("$TAG: decoding HeifImage ${frameWidth}x$frameHeight, $frameCount frame(s)")

            GifEncoder(SystemFileSystem.sink(Path(gifPath)).buffered()).use { gifEncoder ->
                for (i in 0 until frameCount) {
                    val heifFrame = heifImage.invokeMethodOrNull(
                        packageInstance.heifImage.getFrame(),
                        i
                    )
                    if (heifFrame == null) {
                        YLog.warn("$TAG: failed to get frame $i, skipping")
                        continue
                    }

                    val frameBitmap = createBitmap(frameWidth, frameHeight)
                    try {
                        heifFrame.invokeMethodOnlyQuietly(
                            packageInstance.heifFrame.renderFrame(),
                            frameWidth,
                            frameHeight,
                            frameBitmap
                        )
                        gifEncoder.writeFrame(frameBitmap, frameDuration[i].milliseconds)
                    } catch (e: Exception) {
                        YLog.error("$TAG: failed to render or write frame $i", e)
                    } finally {
                        heifFrame.invokeMethodOnlyQuietly(
                            packageInstance.heifFrame.dispose()
                        )
                        frameBitmap.recycle()
                    }
                }
            }
            return true
        } catch (e: Exception) {
            YLog.error("$TAG: convert media to gif failed", e)
            return false
        } finally {
            heifImage.invokeMethodOnlyQuietly(
                packageInstance.heifImage.dispose()
            )
        }
    }
}
