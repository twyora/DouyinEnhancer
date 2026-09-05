package io.github.twyora.douyinenhancer.hook.comment

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.webkit.MimeTypeMap
import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.shakster.gifkt.GifEncoder
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.HookTransaction
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeMethodOnly
import io.github.twyora.douyinenhancer.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import java.io.File
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.io.Sink
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem

@HookOnMainProcess
object CommentEmojiHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    private val packageInstance
        get() = DouyinPackage.instance

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.UNLOCK_COMMENT_EMOJI, false)) {
            if (verbose) {
                YLog.debug("$TAG: unlock comment emoji is disabled, skipping hook")
            }
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

    private fun installSaveEmojiToAlbumButtonHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.saveImageActionItem.selfClass?.resolveMethod(
            packageInstance.saveImageActionItem.isVisible()
        )?.hook {
            before {
                val comment = instance.getField<Any>(
                    packageInstance.saveImageActionItem.commentActionParams()
                )?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                ) ?: run {
                    YLog.error("$TAG: failed to get comment from save image action item")
                    return@before
                }

                val emojiUrls = comment.getField<Any>(
                    packageInstance.comment.emoji()
                )?.getField<Any>(
                    packageInstance.emoji.animateUrl()
                )?.getField<List<String>>(
                    packageInstance.urlModel.urlList()
                )
                if (!emojiUrls.isNullOrEmpty()) {
                    if (verbose) {
                        YLog.debug("$TAG: emoji comment detected, forcing save image button visible")
                    }
                    // force the visibility check to return true — show save button
                    resultTrue()
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to make save image button visible for emoji", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook save image button for emoji", throwable)
            }
        }
    }

    private fun installClickSaveEmojiToAlbumButtonCallbackHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.saveImageActionItem.onClickExecutor.selfClass?.resolveMethod(
            packageInstance.saveImageActionItem.onClickExecutor.onClick()
        )?.hook {
            var savedComment: Any? = null
            var savedActionItem: Any? = null
            var originImageUrlList: List<*>? = null
            var originImageIndex = -1
            before {
                val actionItem = args[0]?.getField<Any>(
                    packageInstance.saveImageActionItem.onClickExecutor.hostItem()
                ) ?: run {
                    YLog.error("$TAG: ${args[0]?.javaClass?.name} is not the click executor held by save image action item")
                    return@before
                }

                val comment = actionItem.getField<Any>(
                    packageInstance.saveImageActionItem.commentActionParams()
                )?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                ) ?: run {
                    YLog.error("$TAG: failed to get comment from ${actionItem::class.qualifiedName}")
                    return@before
                }
                val emojiUrls = comment.getField<Any>(
                    packageInstance.comment.emoji()
                )?.getField<Any>(
                    packageInstance.emoji.animateUrl()
                )?.getField<List<String>>(
                    packageInstance.urlModel.urlList()
                ) ?: run {
                    YLog.error("$TAG: failed to get emoji URLs from ${comment::class.qualifiedName}")
                    return@before
                }

                if (emojiUrls.isEmpty()) {
                    YLog.warn("$TAG: emoji URL list is empty, skipping emoji injection")
                    return@before
                }

                // temporarily install emoji URLs so the downloader sees them
                // save original state to be restored in after{}
                originImageUrlList = comment.getField<List<*>>(
                    packageInstance.comment.imageList()
                )
                // target the first (just-injected) image
                originImageIndex = overrideImageIndex(actionItem, 0)
                savedActionItem = actionItem
                savedComment = comment

                injectEmojiUrls(comment, emojiUrls)

                if (verbose) {
                    YLog.debug("$TAG: injected ${emojiUrls.size} emoji URL(s) into comment")
                }
            }
            // undo temporary edits — restore comment.imageList and actionItem.imageIndex
            after {
                savedComment?.let { comment ->
                    comment.setField(
                        packageInstance.comment.imageList(),
                        originImageUrlList
                    )
                    if (originImageIndex >= 0) {
                        savedActionItem?.let {
                            overrideImageIndex(it, originImageIndex)
                        }
                    }
                }
                savedComment = null
                savedActionItem = null
                originImageUrlList = null
                originImageIndex = -1
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to inject emoji URL(s) into save image download pipeline", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook save image download pipeline for emoji", throwable)
            }
        }
    }

    private fun installEmojiDownloadedCallbackHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.commentImageSaveDownloadListener.selfClass?.resolveMethod(
            packageInstance.commentImageSaveDownloadListener.onSuccessed()
        )?.hook {
            before {
                val downloadInfo = args[0] ?: return@before

                val dlUrl = downloadInfo.getField<String>(
                    packageInstance.downloadInfo.url()
                ) ?: run {
                    YLog.error("$TAG: failed to get download url from download info, skipping emoji handling")
                    return@before
                }
                // Emoji animateUrl file extension is always .heif
                if (!dlUrl.contains(".heif")) {
                    if (verbose) {
                        YLog.debug("$TAG: download url does not contain \".heif\", skipping")
                    }
                    return@before
                }

                // Downloaded file save path
                val sourcePath = downloadInfo.invokeMethod<String?>(
                    packageInstance.downloadInfo.getTargetFilePath()
                ) ?: return@before
                val sourceFileInfo = FileTypeDetector.detect(sourcePath)
                YLog.info("$TAG: source MIME type: ${sourceFileInfo.mimeType}")

                val saveFilePrefix = "comment_${
                    packageInstance.digestUtils.selfClass?.invokeStaticMethod<String>(
                        packageInstance.digestUtils.md5Hex(),
                        dlUrl + System.currentTimeMillis().toString()
                    )
                }"

                // Prepare file to save
                var fileToSave = sourcePath
                var saveFileExt = sourceFileInfo.extensions.first()
                var hasTempFile = false

                if (!sourceFileInfo.mimeType.startsWith("image/") &&
                    !sourceFileInfo.mimeType.startsWith("video/")
                ) {
                    YLog.error(
                        "$TAG: source MIME type restricted: ${sourceFileInfo.mimeType}"
                    )
                    return@before
                }

                // Convert video source to GIF animation
                if (sourceFileInfo.mimeType.startsWith("video/")) {
                    YLog.info("$TAG: source MIME type is video, converting to GIF")

                    val gifTempPath = "${
                        packageInstance.ugFileUtils.selfClass
                            ?.invokeStaticMethod<String>(
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

                // Copy to album
                val saveFilePath = "${
                    packageInstance.ugFileUtils.selfClass
                        ?.invokeStaticMethod<String>(
                            packageInstance.ugFileUtils.getExternalStorageDir(),
                            "/douyin/comment",
                            false,
                            false
                        )
                }${File.separator}$saveFilePrefix.$saveFileExt"

                if (verbose) {
                    YLog.debug("$TAG: saving emoji to $saveFilePath")
                }

                val cpRet = packageInstance.ugFileUtils.selfClass?.invokeStaticMethod<Boolean>(
                    packageInstance.ugFileUtils.copyFile(),
                    fileToSave,
                    saveFilePath,
                    false,
                    null,
                    instance.getField<Any>(
                        packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
                    )?.getField<Any>(
                        packageInstance.listenerProviderParam.cert()
                    )
                )

                // shows success dialog
                val context = instance.getField<Any>(
                    packageInstance.commentImageSaveDownloadListener.listenerProviderParam()
                )?.getField<Context>(
                    packageInstance.listenerProviderParam.context()
                ) ?: run {
                    YLog.error("$TAG: unable to get Context from download listener")
                    return@before
                }

                instance.invokeMethodOnly(
                    packageInstance.commentImageSaveDownloadListener.notifyResult(),
                    context,
                    cpRet
                )

                if (hasTempFile) {
                    File(fileToSave).delete()
                }

                // Return null to skip original method execution
                resultNull()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to run emoji download completion callback", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook emoji download completion callback", throwable)
            }
        }
    }

    private fun installCreateUriHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.ugFileUtils.selfClass?.resolveMethod(
            packageInstance.ugFileUtils.createUri()
        )?.hook {
            after {
                val uriRet = result as? Uri
                // original createUri succeeded
                if (uriRet != null && uriRet != Uri.EMPTY) {
                    return@after
                }

                // original createUri failed; build a URI manually via getImageUri
                val filePath = args[0] as? String
                if (filePath.isNullOrEmpty()) {
                    return@after
                }
                val fileExt = filePath.substringAfterLast('.', "")

                val fileMimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExt)
                if (fileMimeType == null) {
                    YLog.error("$TAG: createUri failed, unable to resolve MIME type")
                    return@after
                } else if (
                    !(fileMimeType.startsWith("image/") || fileMimeType.startsWith("video/"))
                ) {
                    YLog.error(
                        "$TAG: MIME type restricted when invoking createUri: $fileMimeType"
                    )
                    return@after
                }

                val fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1)
                // Ensure the virtual path has no leading '/' but strictly retains a trailing '/'.
                val fileRelPath = filePath.substring(
                    1,
                    filePath.lastIndexOf(File.separator) + 1
                )

                val context = packageInstance.ugFileUtils.selfClass?.getStaticField<Context>(
                    packageInstance.ugFileUtils.context()
                ) ?: run {
                    YLog.error("$TAG: unable to get Context from ugFileUtils")
                    return@after
                }
                val tokenCert = args[3]

                val finalUri = packageInstance.ugFileUtils.selfClass?.invokeStaticMethod<Uri>(
                    packageInstance.ugFileUtils.getImageUri(),
                    context,
                    fileName,
                    fileMimeType,
                    fileRelPath,
                    tokenCert
                ) ?: return@after
                // replace the hook's return value with the URI we just created
                result = finalUri

                // also write into the caller's out-parameter array
                @Suppress("UNCHECKED_CAST")
                val uriArr = args[2] as? Array<Uri> ?: return@after
                if (uriArr.isNotEmpty()) {
                    uriArr[0] = finalUri
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to build URI for image or video", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook create URI for image or video", throwable)
            }
        }
    }

    private fun injectEmojiUrls(comment: Any, emojiUrls: List<String>) {
        var imageList = comment.getField<List<*>>(
            packageInstance.comment.imageList()
        )
        if (imageList.isNullOrEmpty()) {
            val newStruct = packageInstance.commentImageStruct.selfClass?.createInstance()
            imageList = listOf(newStruct)
            comment.setField(
                packageInstance.comment.imageList(),
                imageList
            )
        }

        val targetStruct = imageList[0]
        var urlModel = targetStruct?.getField<Any>(
            packageInstance.commentImageStruct.downloadUrl()
        )
        if (urlModel == null) {
            urlModel = packageInstance.urlModel.selfClass?.createInstance()
            targetStruct?.setField(
                packageInstance.commentImageStruct.downloadUrl(),
                urlModel
            )
        }

        val finalUrls: List<String> = run {
            val imageUrls = urlModel?.getField<List<String>>(
                packageInstance.urlModel.urlList()
            )
            if (imageUrls.isNullOrEmpty()) {
                emojiUrls
            } else {
                emojiUrls + imageUrls
            }
        }
        urlModel?.setField(packageInstance.urlModel.urlList(), finalUrls)
    }

    // Sets a new image index on the save-image action, returns the previous one
    private fun overrideImageIndex(actionItem: Any, index: Int): Int {
        val params = actionItem.getField<Any>(
            packageInstance.saveImageActionItem.saveImageActionParams()
        ) ?: return -1
        val originImageIndex = params.getField<Int>(
            packageInstance.commentActionParams.imageIndex()
        ) ?: return 0
        params.setField(
            packageInstance.commentActionParams.imageIndex(),
            index
        )
        return originImageIndex
    }

    private fun convertMedia2Gif(mediaPath: String, gifPath: String): Boolean {
        var extractor: MediaExtractor? = null
        var retriever: MediaMetadataRetriever? = null
        var encoder: GifEncoder? = null
        var sink: Sink? = null

        try {
            // 1. Initialize MediaExtractor
            extractor = MediaExtractor().apply {
                setDataSource(mediaPath)
            }

            // Find the visual track
            var trackIndex = -1
            for (i in 0 until extractor.trackCount) {
                val format = extractor.getTrackFormat(i)
                val mime = format.getString(MediaFormat.KEY_MIME) ?: ""
                if (mime.startsWith("video/") || mime.startsWith("image/")) {
                    if (verbose) {
                        YLog.debug("$TAG: found visual track[$i], MIME: $mime")
                    }
                    trackIndex = i
                    break
                }
            }

            // If no track is found, we can't process it
            if (trackIndex == -1) {
                YLog.warn("$TAG: no video/image track found in $mediaPath")
                return false
            }
            extractor.selectTrack(trackIndex)

            // 2. Iterate to fetch timestamps
            val timestampsUs = mutableListOf<Long>()
            while (true) {
                val time = extractor.sampleTime
                if (time < 0) {
                    break // End of stream
                }
                timestampsUs.add(time)
                extractor.advance()
            }

            if (timestampsUs.isEmpty()) {
                YLog.warn("$TAG: no timestamps extracted from $mediaPath")
                return false
            }

            // 3. Initialize MediaMetadataRetriever
            retriever = MediaMetadataRetriever()
            retriever.setDataSource(mediaPath)

            // 4. Initialize gif.kt Multiplatform Encoder
            val outPath = Path(gifPath)
            sink = SystemFileSystem.sink(outPath).buffered()
            encoder = GifEncoder(sink)

            // 5. Process each frame
            for (i in timestampsUs.indices) {
                val currentUs = timestampsUs[i]

                // Calculate delay: difference to next timestamp, duplicate the previous delta for the final frame
                val nextUs = if (i + 1 < timestampsUs.size) {
                    timestampsUs[i + 1]
                } else {
                    if (i > 0) {
                        timestampsUs[i] + (timestampsUs[i] - timestampsUs[i - 1])
                    } else {
                        currentUs + 100_000L
                    }
                }

                val delayMs = ((nextUs - currentUs) / 1000L).coerceAtLeast(10L)

                // Extract frame
                var bitmap: Bitmap? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    runCatching {
                        // Try by index for animated
                        bitmap = retriever.getFrameAtIndex(i)
                    }
                }

                // Fallback for static or if index retrieval fails
                if (bitmap == null) {
                    // If it's a single frame, getFrameAtTime(0) works reliably for HEIC
                    bitmap = retriever.getFrameAtTime(
                        currentUs,
                        MediaMetadataRetriever.OPTION_CLOSEST
                    )
                }

                if (bitmap == null) {
                    continue
                }

                val width = bitmap.width
                val height = bitmap.height
                val pixels = IntArray(width * height)

                // 6. Convert Bitmap to IntArray
                bitmap.getPixels(pixels, 0, width, 0, 0, width, height)

                // 7. Write frame to GIF
                encoder.writeFrame(pixels, width, height, delayMs.milliseconds)

                bitmap.recycle()
            }

            return true
        } catch (e: Exception) {
            YLog.error("$TAG: convert media to gif failed", e)
            return false
        } finally {
            // Enforce rigorous cleanup
            runCatching {
                extractor?.release()
            }.onFailure {
                YLog.error("$TAG: failed to release media extractor", it)
            }
            runCatching {
                retriever?.release()
            }.onFailure {
                YLog.error("$TAG: failed to release media metadata retriever", it)
            }
            runCatching {
                encoder?.close()
            }.onFailure {
                YLog.error("$TAG: failed to close gif encoder", it)
            }
            runCatching {
                sink?.close()
            }.onFailure {
                YLog.error("$TAG: failed to close output sink", it)
            }
        }
    }
}
