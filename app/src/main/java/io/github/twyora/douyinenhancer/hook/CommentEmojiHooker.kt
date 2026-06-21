package io.github.twyora.douyinenhancer.hook

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build
import android.webkit.MimeTypeMap
import java.io.File
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.Sink
import kotlin.time.Duration.Companion.milliseconds

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.shakster.gifkt.GifEncoder
import org.luckypray.dexkit.DexKitBridge

import io.github.twyora.douyinenhancer.hook.utils.HookTransaction
import io.github.twyora.douyinenhancer.hook.utils.FileTypeDetector
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.getStaticField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.hook.utils.setField

object CommentEmojiHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            val transaction = HookTransaction(TAG)
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                // Force "save to album" button visible for emoji comments.
                transaction.add(::installSaveEmojiToAlbumButtonHook.name) {
                    installSaveEmojiToAlbumButtonHook(bridge)
                }
                // Before the save button's download callback fires, inject emoji URLs into
                // comment.imageList[0].downloadUrl so the downloader picks them up.
                // Without it: the downloader fetches nothing because imageList is empty.
                transaction.add(::installClickSaveEmojiToAlbumButtonCallbackHook.name) {
                    installClickSaveEmojiToAlbumButtonCallbackHook(bridge)
                }
                // Intercept emoji download completion: detect real file type, convert video
                // to GIF if needed, copy to album, show result toast, skip original handler
                // — otherwise every downloaded emoji is saved as .png with MIME image/png.
                transaction.add(::installEmojiDownloadedCallbackHook.name) {
                    installEmojiDownloadedCallbackHook(bridge)
                }
                // Fix MediaStore URI creation for converted GIF files: internal logic has a
                // file-extension whitelist, so non-whitelisted extensions (e.g. gif) fail
                // createUri and cannot be saved to external storage — this hook falls back
                // to getImageUri manually and writes into output array.
                transaction.add(::installCreateUriHook.name) {
                    installCreateUriHook(bridge)
                }
                transaction.commit()
            }
        }
    }

    private fun installSaveEmojiToAlbumButtonHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        val packageInstance = DouyinPackage.instance

        packageInstance.commentExtensionsKt.selfClass?.resolve()?.firstMethod {
            name = packageInstance.commentExtensionsKt.saveToAlbumVisibility().name
        }?.hook {
            before {
                val comment = args[0] ?: return@before
                val emojiUrls = extractEmojiUrls(comment)
                if (!emojiUrls.isNullOrEmpty()) {
                    // force the visibility check to return true — show save button
                    resultTrue()
                }
            }
        }?.result {
            onConductFailure { param, throwable ->
                YLog.error("$TAG: Save emoji button hook runtime error", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: Failed to hook save emoji button", throwable)
            }
            onHooked {
                YLog.info("$TAG: Save emoji button hook installed successfully. The button will be shown for emoji comments")
            }.also {
                ret = it
            }
        } ?: run {
            YLog.error("$TAG: Target method not found, save emoji to album button will not be shown")
        }
        return ret
    }

    private fun installClickSaveEmojiToAlbumButtonCallbackHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        val packageInstance = DouyinPackage.instance

        packageInstance.commentSaveToAlbumButtonClick.selfClass?.resolve()?.firstMethodOrNull {
            name = packageInstance.commentSaveToAlbumButtonClick.onClicked().name
        }?.hook {
            var savedComment: Any? = null
            var savedActionItem: Any? = null
            var originImageUrlList: List<*>? = null
            var originImageIndex = -1
            before {
                val cbkInstance = args[0] ?: return@before

                val actionItem = cbkInstance.asResolver().firstField {
                    type = Object::class
                }.get() ?: return@before

                val comment = extractComment(actionItem) ?: return@before
                val emojiUrls = extractEmojiUrls(comment) ?: return@before

                // temporarily install emoji URLs so the downloader sees them
                // save original state to be restored in after{}
                originImageUrlList = comment.getField<List<*>>(DouyinPackage.instance.comment.imageList())
                originImageIndex = overrideImageIndex(actionItem, 0) // target the first (just-injected) image
                savedActionItem = actionItem
                savedComment = comment

                injectEmojiUrls(comment, emojiUrls, prepend = true)

                YLog.debug("$TAG: Injected ${emojiUrls.size} emoji url(s) into comment")
            }
            // undo temporary edits — restore comment.imageList and actionItem.imageIndex
            after {
                savedComment?.let { c ->
                    c.setField(DouyinPackage.instance.comment.imageList(), originImageUrlList)
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
            onConductFailure { param, throwable ->
                YLog.error("$TAG: Download callback hook runtime error", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: Failed to hook download callback", throwable)
            }
            onHooked {
                YLog.info("$TAG: Download callback hook installed successfully. Emoji URLs will be injected when saving")
            }.also {
                ret = it
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, download callback hook will not be installed")
        }

        return ret
    }

    private fun installEmojiDownloadedCallbackHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        val packageInstance = DouyinPackage.instance

        packageInstance.commentImageSaveHelper.selfClass?.resolve()?.firstMethodOrNull {
            name = packageInstance.commentImageSaveHelper.onSuccessed().name
        }?.hook {
            before {
                val downloadInfo = args[0] ?: return@before

                val dlUrl = downloadInfo.getField<String>(DouyinPackage.instance.downloadInfo.url()) ?: return@before
                // Emoji animateUrl file extension is always .heif
                if (!dlUrl.contains(".heif")) {
                    return@before
                }

                // Downloaded file save path
                val sourcePath =
                    downloadInfo.invokeMethod<String?>(DouyinPackage.instance.downloadInfo.getTargetFilePath())
                        ?: return@before
                val sourceMimeType = FileTypeDetector.detect(sourcePath).mimeType
                YLog.info("$TAG: Source MIME type: $sourceMimeType")

                val saveFilePrefix = "comment_${
                    DouyinPackage.instance.digestUtils.selfClass?.invokeStaticMethod<String>(
                        DouyinPackage.instance.digestUtils.md5Hex(),
                        dlUrl + System.currentTimeMillis().toString()
                    )
                }"

                // Prepare file to save
                var fileToSave = sourcePath
                var saveFileExt = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(sourceMimeType)
                    ?: sourcePath.substringAfterLast('.', "")
                var hasTempFile = false

                if (!sourceMimeType.startsWith("image/") && !sourceMimeType.startsWith("video/")) {
                    YLog.error("$TAG: Source MIME type restricted, not allowed: $sourceMimeType")
                    return@before
                }

                // Convert video source to GIF animation
                if (sourceMimeType.startsWith("video/")) {
                    YLog.info("$TAG: Source MIME type is video, converting to GIF")

                    val gifTempPath = "${
                        DouyinPackage.instance.ugFileUtils.selfClass?.invokeStaticMethod<String>(
                            DouyinPackage.instance.ugFileUtils.getStorageDir(),
                            "/comment/images", false
                        )
                    }${File.separator}${saveFilePrefix}.gif"

                    if (convertMedia2Gif(sourcePath, gifTempPath)) {
                        fileToSave = gifTempPath
                        saveFileExt = "gif"
                        hasTempFile = true
                    } else {
                        YLog.error("$TAG: Convert video to GIF failed, fallback to direct copy")
                    }
                }

                // Copy to album
                val saveFilePath = "${
                    DouyinPackage.instance.ugFileUtils.selfClass?.invokeStaticMethod<String>(
                        DouyinPackage.instance.ugFileUtils.getExternalStorageDir(),
                        "/douyin/comment", false
                    )
                }${File.separator}${saveFilePrefix}.${saveFileExt}"

                val cpRet = DouyinPackage.instance.ugFileUtils.selfClass?.invokeStaticMethod<Boolean>(
                        DouyinPackage.instance.ugFileUtils.copyFile(),
                        fileToSave, saveFilePath,
                        DouyinPackage.instance.tokenCert.selfClass?.getConstructor(String::class.java)
                            ?.newInstance("bpea-comment_save_image_to_album")
                    )

                // shows success dialog
                val context =
                    instance.asResolver().firstField().get()?.asResolver()?.firstFieldOrNull {
                        type = Context::class.java
                    }?.get<Context?>()

                instance.invokeMethod<Any?>(
                    DouyinPackage.instance.commentImageSaveHelper.notifyResult(),
                    context, cpRet
                )

                if (hasTempFile) {
                    File(fileToSave).delete()
                }

                // Return null to skip original method execution
                resultNull()
            }
        }?.result {
            onConductFailure { param, throwable ->
                YLog.error("$TAG: Emoji download completion callback runtime error", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: Failed to hook emoji download completion callback", throwable)
            }
            onHooked {
                YLog.info("$TAG: Emoji download completion callback hooked")
            }.also {
                ret = it
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, emoji download completion callback will not be installed")
        }

        return ret
    }

    @Suppress("UNUSED_PARAMETER")
    private fun installCreateUriHook(unused: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        val packageInstance = DouyinPackage.instance

        packageInstance.ugFileUtils.selfClass?.resolve()?.firstMethodOrNull {
            name = "createUri"
            returnType = android.net.Uri::class
            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
            parameters(
                String::class,
                Boolean::class,
                Array<android.net.Uri>::class,
                DouyinPackage.instance.tokenCert.selfClass!!
            )
            parameterCount = 4
        }?.hook {
            after {
                val uriRet = result as? android.net.Uri
                // original createUri succeeded — nothing to fix
                if (uriRet != null && uriRet != android.net.Uri.EMPTY) {
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
                } else if (!(fileMimeType.startsWith("image/") || fileMimeType.startsWith("video/"))) {
                    YLog.error("$TAG: createUri is not allowed for restricted MIME type. mimeType=$fileMimeType")
                    return@after
                }

                val fileName = filePath.substring(filePath.lastIndexOf(File.separator) + 1)
                // Ensure the virtual path has no leading '/' but strictly retains a trailing '/'.
                val fileRelPath = filePath.substring(1, filePath.lastIndexOf(File.separator) + 1)

                val context =
                    DouyinPackage.instance.ugFileUtils.selfClass?.getStaticField<Context>(DouyinPackage.instance.ugFileUtils.context())
                        ?: return@after
                val tokenCert = args[3]

                val finalUri = DouyinPackage.instance.ugFileUtils.selfClass?.invokeStaticMethod<android.net.Uri>(
                        DouyinPackage.instance.ugFileUtils.getImageUri(),
                        context, fileName, fileMimeType, fileRelPath, tokenCert
                    ) ?: return@after
                // replace the hook's return value with the URI we just created
                result = finalUri

                // also write into the caller's out-parameter array
                @Suppress("UNCHECKED_CAST")
                val uriArr = args[2] as? Array<android.net.Uri> ?: return@after
                if (uriArr.isNotEmpty()) {
                    uriArr[0] = finalUri
                }
            }
        }?.result {
            onConductFailure { param, throwable ->
                YLog.error("$TAG: createUri hook runtime error", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: Failed to hook createUri", throwable)
            }
            onHooked {
                YLog.info("$TAG: createUri hooked. URI creation will be intercepted for emoji GIF conversion.")
            }.also {
                ret = it
            }
        }

        return ret
    }

    // Gets CommentActionParams held by a long-press menu item
    private fun extractActionParams(actionItem: Any): Any? {
        return actionItem.getField(DouyinPackage.instance.commentLongPressItemModel.commentActionParams())
    }

    // Gets the Comment from a long-press menu item
    private fun extractComment(actionItem: Any): Any? {
        val params = extractActionParams(actionItem) ?: return null
        return params.getField(DouyinPackage.instance.commentActionParams.comment())
    }

    // Gets emoji download URLs from comment.emoji.animateUrl.urlList
    private fun extractEmojiUrls(comment: Any): List<String>? {
        return runCatching {
            val emoji = comment.getField<Any>(DouyinPackage.instance.comment.emoji()) ?: return@runCatching null
            val animateUrl = emoji.getField<Any>(DouyinPackage.instance.emoji.animateUrl()) ?: return@runCatching null
            animateUrl.getField<List<String>>(DouyinPackage.instance.urlModel.urlList())
        }.getOrNull()
    }

    // Writes emoji URLs into comment.imageList[0].downloadUrl.urlList, optionally prepended to existing URLs
    private fun injectEmojiUrls(comment: Any, emojiUrls: List<String>, prepend: Boolean = true) {
        var imageList = comment.getField<List<*>>(DouyinPackage.instance.comment.imageList())
        if (imageList.isNullOrEmpty()) {
            val newStruct = DouyinPackage.instance.commentImageStruct.selfClass?.getConstructor()?.newInstance()
            imageList = listOf(newStruct)
            comment.setField(DouyinPackage.instance.comment.imageList(), imageList)
        }

        val targetStruct = imageList[0]
        var urlModel = targetStruct?.getField<Any>(DouyinPackage.instance.commentImageStruct.downloadUrl())
        if (urlModel == null) {
            urlModel = DouyinPackage.instance.urlModel.selfClass?.getConstructor()?.newInstance()
            targetStruct?.setField(DouyinPackage.instance.commentImageStruct.downloadUrl(), urlModel)
        }

        var finalUrls: List<String>? = if (prepend) {
            val imageUrls = urlModel?.getField<List<String>>(DouyinPackage.instance.urlModel.urlList())
            if (imageUrls.isNullOrEmpty()) {
                emojiUrls
            } else {
                emojiUrls + imageUrls
            }
        } else {
            emojiUrls
        }
        urlModel?.setField(DouyinPackage.instance.urlModel.urlList(), finalUrls)
    }

    // Sets a new image index on the save-image action, returns the previous one
    private fun overrideImageIndex(actionItem: Any, index: Int): Int {
        val params =
            actionItem.getField<Any>(DouyinPackage.instance.saveImageActionItem.saveImageActionParams()) ?: return -1
        val originImageIndex = params.getField<Int>(DouyinPackage.instance.commentActionParams.imageIndex())!!
        params.setField(DouyinPackage.instance.commentActionParams.imageIndex(), index)
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
                YLog.debug("$TAG: track[$i] mime=$mime")
                if (mime.startsWith("video/") || mime.startsWith("image/")) {
                    trackIndex = i
                    break
                }
            }

            // If no track is found, we can't process it
            if (trackIndex == -1) {
                YLog.warn("$TAG: No video/image track found in $mediaPath")
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
                YLog.warn("$TAG: No timestamps extracted from $mediaPath")
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
                    if (i > 0) timestampsUs[i] + (timestampsUs[i] - timestampsUs[i - 1]) else currentUs + 100_000L
                }

                val delayMs = ((nextUs - currentUs) / 1000L).coerceAtLeast(10L)

                // Extract frame
                var bitmap: Bitmap? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    try {
                        // Try by index for animated
                        bitmap = retriever.getFrameAtIndex(i)
                    } catch (e: Exception) {
                        // Fallback
                    }
                }

                // Fallback for static or if index retrieval fails
                if (bitmap == null) {
                    // If it's a single frame, getFrameAtTime(0) works reliably for HEIC
                    bitmap =
                        retriever.getFrameAtTime(currentUs, MediaMetadataRetriever.OPTION_CLOSEST)
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
            YLog.error("$TAG: convertMedia2Gif failed", e)
            return false
        } finally {
            // Enforce rigorous cleanup
            try {
                extractor?.release()
            } catch (e: Exception) {
            }
            try {
                retriever?.release()
            } catch (e: Exception) {
            }
            try {
                encoder?.close()
            } catch (e: Exception) {
            }
            try {
                sink?.close()
            } catch (e: Exception) {
            }
        }
    }
}