package io.github.twyora.douyinenhancer.hook.feed

import android.graphics.Bitmap
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.file.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeStaticMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setFieldQuietly
import java.io.File
import java.io.FileInputStream
import org.apache.commons.collections4.queue.CircularFifoQueue

@HookOnMainProcess
object FeedMultiImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    private val seenAwemeIds = CircularFifoQueue<String>(5)

    override fun onHook() {
        if (!ConfigManager.save.feedMultiImageRemoveWatermark.value) {
            YLog.info("$TAG: remove watermark is disabled, skipping hook")
            return
        }

        installInjectPlayUrlIntoImageDownloadHook()

        installConvertVvicImageToPngHook()

        installConvertSingleVvicImageToMp4Hook()
        installConvertMultiVvicImagesToMp4Hook()
        installDisableSaveImageToVideoLocalWaterMaskHook()

        installConvertVvicCoverImageToPngHook()
        installDisableVEAddLiveVideoWaterMarkHook()
    }

    private fun installInjectPlayUrlIntoImageDownloadHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.downloadAction.selfClass?.resolveMethodOrNull(
            packageInstance.downloadAction.startDownload()
        )?.intercept {
            val aweme = instance.getFieldOrNull(
                packageInstance.downloadAction.aweme()
            ) ?: run {
                YLog.error("$TAG: aweme not found in download action")
                return@intercept proceed()
            }
            if (aweme.invokeMethodOrNull<Boolean>(
                    packageInstance.aweme.isMultiImage()
                ) == false
            ) {
                return@intercept proceed()
            }

            // skip if this post was already processed
            aweme.invokeMethodOrNull<String>(
                packageInstance.aweme.getAid()
            )?.let {
                if (seenAwemeIds.contains(it)) {
                    return@intercept proceed()
                }
                seenAwemeIds.add(it)
            }

            val awemeImages = aweme.getFieldOrNull<List<*>>(
                packageInstance.aweme.images()
            ).takeIf {
                !it.isNullOrEmpty()
            } ?: run {
                YLog.error("$TAG: aweme.images is null or empty")
                return@intercept proceed()
            }

            awemeImages.forEach { imageStruct ->
                if (imageStruct == null) {
                    return@forEach
                }

                // use the play URL as the image download URL
                imageStruct.getFieldOrNull<List<*>>(
                    packageInstance.imageUrlStruct.urlList()
                ).takeIf {
                    !it.isNullOrEmpty()
                }?.let {
                    imageStruct.setFieldQuietly(
                        packageInstance.imageUrlStruct.downloadUrlList(),
                        it
                    )
                } ?: run {
                    YLog.warn("$TAG: image has no play URL, skipping watermark-free injection")
                }

                // also replace the video's download URL when the post has a video
                imageStruct.getFieldOrNull(
                    packageInstance.imageUrlStruct.video()
                )?.let { video ->
                    video.invokeMethodOrNull(
                        packageInstance.video.getPlayAddr()
                    )?.let { playAddr ->
                        video.setFieldQuietly(
                            packageInstance.video.downloadAddr(),
                            playAddr
                        )
                        if (verbose) {
                            YLog.debug("$TAG: play URL used as video download address")
                        }
                    }?.also {
                        video.setFieldQuietly(
                            packageInstance.video.hasWaterMark(),
                            false
                        )
                        video.setFieldQuietly(
                            packageInstance.video.hasSuffixWaterMark(),
                            false
                        )
                    }
                }
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to inject play URL into image download", throwable)
            }
        }
    }

    private fun installDisableSaveImageToVideoLocalWaterMaskHook(): YukiHookCreator.MemberHooker<*>.Handle? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethodOrNull(
            packageInstance.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask()
        )?.intercept {
            if (verbose) {
                YLog.debug("$TAG: disabling local watermark for save-image-to-video")
            }
            false
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to disable save-image-to-video local watermark", throwable)
            }
        }

    private fun installConvertVvicCoverImageToPngHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.downloadLivePhotoExecutor.selfClass?.resolveMethodOrNull(
            packageInstance.downloadLivePhotoExecutor.encodeLivePhoto()
        )?.intercept {
            val downloadTask = args.firstOrNull {
                packageInstance.absTask.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: download task not found in argument list")
                return@intercept proceed()
            }

            val vvicImagePathList = downloadTask.invokeMethodOrNull<List<String?>>(
                packageInstance.absTask.getTargetFilePaths()
            )?.filterNotNull()?.filter {
                it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
            }

            if (verbose) {
                YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
            }

            vvicImagePathList?.forEach {
                if (!overwriteVvicWithPng(it)) {
                    YLog.error("$TAG: failed to convert vvic cover image to png: $it")
                }
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic cover image to png", throwable)
            }
        }
    }

    private fun installDisableVEAddLiveVideoWaterMarkHook(): YukiHookCreator.MemberHooker<*>.Handle? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethodOrNull(
            packageInstance.abTestServiceImpl.enableVEAddLiveVideoWaterMark()
        )?.intercept {
            if (verbose) {
                YLog.debug("$TAG: disabling watermark for live video")
            }
            false
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to disable ve-add-live-video-water-mark", throwable)
            }
        }

    private fun installConvertVvicImageToPngHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.downLoadExecutor.selfClass?.resolveMethodOrNull(
            packageInstance.downLoadExecutor.execute()
        )?.intercept {
            val downloadTask = args.firstOrNull {
                packageInstance.absTask.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: download task not found in argument list")
                return@intercept proceed()
            }

            val imageFilePath = downloadTask.invokeMethodOrNull<List<String?>>(
                packageInstance.absTask.getTargetFilePaths()
            )?.filterNotNull()?.filter {
                it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
            } ?: run {
                YLog.error("$TAG: failed to get image file path when downloading image")
                return@intercept proceed()
            }

            imageFilePath.forEach {
                if (!overwriteVvicWithPng(it)) {
                    YLog.error("$TAG: failed to convert vvic image to png when downloading image: $it")
                }
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic image to png", throwable)
            }
        }
    }

    private fun installConvertSingleVvicImageToMp4Hook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.storyServiceImpl.selfClass?.resolveMethodOrNull(
            packageInstance.storyServiceImpl.convertImgToMp4()
        )?.intercept {
            val imagePath = args[2] as? String ?: run {
                YLog.error("$TAG: ${args[2]?.javaClass?.name} is not String")
                return@intercept proceed()
            }
            val vvicImagePath = imagePath.takeIf {
                it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
            }
            if (vvicImagePath == null) {
                return@intercept proceed()
            } else if (verbose) {
                YLog.debug("$TAG: vvic image path: $vvicImagePath")
            }

            if (!overwriteVvicWithPng(vvicImagePath)) {
                YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer: $vvicImagePath")
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer", throwable)
            }
        }
    }

    private fun installConvertMultiVvicImagesToMp4Hook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.storyServiceImpl.selfClass?.resolveMethodOrNull(
            packageInstance.storyServiceImpl.convertSingleLivePhotoToMp4UseMusicUrl()
        )?.intercept {
            @Suppress("UNCHECKED_CAST")
            val imagePathList = args[2] as? List<List<String?>> ?: run {
                YLog.error("$TAG: image path list is null")
                return@intercept proceed()
            }
            val vvicImagePathList = imagePathList.flatten().filterNotNull().filter {
                it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
            }

            if (verbose) {
                YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
            }

            vvicImagePathList.forEach {
                if (!overwriteVvicWithPng(it)) {
                    YLog.error("$TAG: failed to convert multi vvic images to png in mp4 composer: $it")
                }
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to convert multi vvic images to png in mp4 composer", throwable)
            }
        }
    }

    private fun overwriteVvicWithPng(imageFilePath: String): Boolean {
        val imageFile = File(imageFilePath)
        if (!imageFile.exists()) {
            YLog.error("$TAG: failed to overwrite vvic image, source file does not exist: ${imageFile.absolutePath}")
            return false
        }
        if (verbose) {
            YLog.debug("$TAG: image file absolute path: ${imageFile.absolutePath}")
        }

        val fvvicInfo = FileTypeDetector.detect(imageFile)
        if (fvvicInfo.mimeType != "image/vvic") {
            if (verbose) {
                YLog.debug("$TAG: image is not in vvic format (got ${fvvicInfo.mimeType}), skipping")
            }
            return true
        }

        val imageBytes = runCatching {
            FileInputStream(imageFile).use { it.readBytes() }
        }.getOrElse {
            YLog.error("$TAG: failed to read vvic image for png conversion: ${imageFile.absolutePath}", it)
            return false
        }

        val bitmap = packageInstance.heif.selfClass?.invokeStaticMethodOrNull(
            packageInstance.heif.toRgba(),
            /* vvicBytes = */
            imageBytes,
            /* ttheifOpt = */
            true,
            /* length = */
            imageBytes.size,
            /* vvicDecOpt = */
            true,
            /* vvicOptMode = */
            0,
            /* heicUseWpp = */
            true,
            /* heicDecodeThreads = */
            1,
            /* vvicUseWpp = */
            true,
            /* vvicDecodeThreads = */
            1,
            /* sampleSize = */
            1,
            /* cropLeft = */
            -1,
            /* cropTop = */
            -1,
            /* cropHeight = */
            -1,
            /* cropWidth = */
            -1,
            /* fixVvicDecode = */
            true
        )?.invokeMethodOrNull(
            packageInstance.heifData.newBitmap(),
            null,
            Bitmap.Config.ARGB_8888
        )?.invokeMethodOrNull<Bitmap>(
            packageInstance.closeableReference.get()
        )
        if (bitmap == null) {
            YLog.error("$TAG: failed to decode vvic image to bitmap: ${imageFile.absolutePath}")
            return false
        }

        val pngFile = imageFile.resolveSibling("${imageFile.nameWithoutExtension}.png")
        try {
            runCatching {
                pngFile.outputStream().use { out ->
                    require(bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)) {
                        "failed to compress vvic image to png"
                    }
                }
                imageFile.writeBytes(pngFile.readBytes())
            }.onFailure {
                YLog.error("$TAG: failed to overwrite vvic image with png: ${imageFile.absolutePath}", it)
                return false
            }
            if (verbose) {
                YLog.debug("$TAG: converted vvic image to png: ${imageFile.absolutePath}")
            }
            return true
        } finally {
            bitmap.recycle()
            pngFile.delete()
        }
    }
}
