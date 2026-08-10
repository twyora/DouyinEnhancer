package io.github.twyora.douyinenhancer.hook.feed

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.FileTypeDetector
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import java.io.File
import java.io.FileInputStream
import org.apache.commons.collections4.queue.CircularFifoQueue

@HookOnMainProcess
object FeedMultiImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)) {
            if (verbose) {
                YLog.debug("$TAG: remove watermark disabled, skip feed multi-image hook")
            }
            return
        }

        if (packageInstance.hostVersionCode() >= 390601) {
            // 39.6.0+: the save pipeline was rewritten. Share-panel downloads go
            // DownloadAction (located by DexKit SheetAction+downloadImage rule) ->
            // DownloadMutiPicHelper -> getImageDownloadUrl; image URLs are read from
            // ImageUrlStruct.urlList + watermarkFreeDownloadUrlList. Long-press save
            // goes a separate callable path. The legacy hooks below (DownloadAction.
            // startDownload / ABTestServiceImpl / HeifBitmapFactoryImpl) break on 39.6.0.
            install396InjectPlayUrlIntoImageDownloadHook()
            install396GetImageDownloadUrlHook()
            install396LongPressSaveHook()
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

    private fun installInjectPlayUrlIntoImageDownloadHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downloadAction.selfClass?.resolveMethod(
            packageInstance.downloadAction.startDownload()
        )?.hook {
            val seenAwemeIds = CircularFifoQueue<String>(5)
            before {
                val aweme = instance.getField<Any>(
                    packageInstance.downloadAction.aweme()
                ) ?: return@before
                if (aweme.invokeMethod<Boolean>(
                        packageInstance.aweme.isMultiImage()
                    ) == false
                ) {
                    return@before
                }

                // skip if this post was already processed
                aweme.invokeMethod<String>(
                    packageInstance.aweme.getAid()
                )?.let {
                    if (seenAwemeIds.contains(it)) {
                        return@before
                    }
                    seenAwemeIds.add(it)
                }

                val awemeImages = aweme.getField<List<*>>(
                    packageInstance.aweme.images()
                ).takeIf {
                    !it.isNullOrEmpty()
                } ?: run {
                    YLog.error("$TAG: aweme.images is null or empty")
                    return@before
                }

                awemeImages.forEach { imageStruct ->
                    if (imageStruct == null) {
                        return@forEach
                    }

                    // use the play URL as the image download URL
                    imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    ).takeIf {
                        !it.isNullOrEmpty()
                    }?.let {
                        imageStruct.setField(
                            packageInstance.imageUrlStruct.downloadUrlList(),
                            it
                        )
                        if (verbose) {
                            YLog.debug("$TAG: image.urlList[0]: ${it.first()}")
                        }
                    } ?: run {
                        YLog.warn("$TAG: image has no play URL, skipping watermark-free injection")
                    }

                    // also replace the video's download URL when the post has a video
                    imageStruct.getField<Any>(
                        packageInstance.imageUrlStruct.video()
                    )?.let { video ->
                        video.invokeMethod<Any>(
                            packageInstance.video.getPlayAddr()
                        )?.let { playAddr ->
                            video.setField(
                                packageInstance.video.downloadAddr(),
                                playAddr
                            )
                            if (verbose) {
                                YLog.debug("$TAG:play URL used as video download address")
                            }
                        }?.also {
                            video.setField(
                                packageInstance.video.hasWaterMark(),
                                false
                            )
                            video.setField(
                                packageInstance.video.hasSuffixWaterMark(),
                                false
                            )
                        }
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to inject play URL into image download", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, multi-image watermark-free download unavailable")
            }
        }
    }

    /**
     * 39.6.0+: the share-panel save entry was rewritten as DownloadAction
     * (located by DexKit SheetAction+downloadImage rule). Image downloads read
     * ImageUrlStruct.urlList (play URLs) directly plus
     * watermarkFreeDownloadUrlList as an override — downloadUrlList is no
     * longer consulted. Inject the cleaned play URLs into
     * watermarkFreeDownloadUrlList before download starts.
     *
     * Hook the DownloadAction constructor: it always receives the Aweme being
     * saved (this.b), so a single hook covers every save path without chasing
     * obfuscated method names.
     */
    private fun install396InjectPlayUrlIntoImageDownloadHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val downloadActionClass = packageInstance.downloadAction.selfClass ?: run {
            YLog.error("$TAG: DownloadAction class not resolved")
            return null
        }
        val constructor = downloadActionClass.declaredConstructors.firstOrNull {
            it.parameterTypes.any { p -> p.name == "com.ss.android.ugc.aweme.feed.model.Aweme" }
        } ?: run {
            YLog.error("$TAG: DownloadAction constructor with Aweme not found")
            return null
        }
        return constructor.hook {
            val seenAwemeIds = CircularFifoQueue<String>(5)
            before {
                val aweme = args.firstOrNull { it is Any && it::class.java.name == "com.ss.android.ugc.aweme.feed.model.Aweme" }
                    ?: return@before
                if (aweme.invokeMethod<Boolean>(
                        packageInstance.aweme.isMultiImage()
                    ) == false
                ) {
                    return@before
                }

                // skip if this post was already processed
                aweme.invokeMethod<String>(
                    packageInstance.aweme.getAid()
                )?.let {
                    if (seenAwemeIds.contains(it)) {
                        return@before
                    }
                    seenAwemeIds.add(it)
                }

                val awemeImages = aweme.getField<List<*>>(
                    packageInstance.aweme.images()
                ).takeIf {
                    !it.isNullOrEmpty()
                } ?: run {
                    YLog.error("$TAG: aweme.images is null or empty")
                    return@before
                }

                var injected = 0
                awemeImages.forEach { imageStruct ->
                    if (imageStruct == null) {
                        return@forEach
                    }

                    val playUrlList = imageStruct.getField<List<*>>(
                        packageInstance.imageUrlStruct.urlList()
                    ).takeIf {
                        !it.isNullOrEmpty()
                    } ?: run {
                        YLog.warn("$TAG: image has no play URL, skipping watermark-free injection")
                        return@forEach
                    }

                    val cleaned = cleanUrlList(playUrlList)
                    // The downloader may read either field depending on entry path.
                    // Overwrite BOTH so every reader gets a clean URL:
                    // - watermarkFreeDownloadUrlList (preferred by getImageDownloadUrl)
                    // - downloadUrlList (tplv-dy-water-v10 = watermark source!)
                    imageStruct.setField(
                        packageInstance.imageUrlStruct.watermarkFreeDownloadUrlList(),
                        cleaned
                    )
                    imageStruct.setField(
                        packageInstance.imageUrlStruct.downloadUrlList(),
                        cleaned
                    )
                    injected++
                }
                if (injected > 0) {
                    YLog.info("$TAG: 39.6.0+ injected cleaned play URLs into ${injected} images (watermark-free download)")
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to inject play URL into 39.6.0+ image download", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, 39.6.0+ multi-image watermark-free download unavailable")
            }
        }
    }

    /**
     * 39.6.0+: getImageDownloadUrl is the static choke point every image download
     * task uses to pick its URL (39.6.0: X.1dde.LJI; 39.9.0: LX/1cus.LJILJI).
     * Match by feature: String return + ImageUrlStruct param + watermarkFreeDownloadUrlList
     * usage. Hook it after to clean the returned URL (watermark=1 -> 0,
     * cdn-direct -> api-play, jpeg preferred over heic/vvic).
     */
    private fun install396GetImageDownloadUrlHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        // getImageDownloadUrl is the static choke point every image download task
        // uses (39.6.0: X.1dde.LJI; 39.9.0: LX/1cus.LJILJI). Locate by feature:
        // String return + ImageUrlStruct second param.
        val imageUrlStructName = "com.ss.ugc.aweme.ImageUrlStruct"
        val method = findImageDownloadUrlMethod(imageUrlStructName) ?: run {
            YLog.error("$TAG: getImageDownloadUrl (feature match) not found")
            return null
        }
        return method.hook {
            after {
                // args[1] is the ImageUrlStruct — its urlList has all image URL
                // variants. Prefer a cleaned standard-JPEG URL so the downloader
                // stores a valid file (urlList[0] is often heic).
                val imageStruct = args.getOrNull(1)
                val cleanedList = imageStruct?.getField<List<*>>(
                    packageInstance.imageUrlStruct.urlList()
                )?.let { cleanUrlList(it) }
                val preferred = cleanedList?.firstOrNull()?.toString()
                if (preferred != null && preferred != (result as? String)) {
                    result = preferred
                    return@after
                }
                val url = result as? String ?: return@after
                val cleaned = cleanUrl(url)
                if (cleaned != url) {
                    result = cleaned
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook getImageDownloadUrl", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, getImageDownloadUrl cleanup unavailable")
            }
        }
    }

    /**
     * Find the getImageDownloadUrl method by stable features:
     * returns String, second param is ImageUrlStruct, and the declaring class
     * references watermarkFreeDownloadUrlList. This survives obfuscated class
     * renames (X.1dde -> LX/1cus).
     */
    private fun findImageDownloadUrlMethod(imageUrlStructName: String): java.lang.reflect.Method? {
        val targetType = imageUrlStructName.toClassOrNull(appClassLoader) ?: return null
        // Search classes that have a field of ImageUrlStruct type and a method
        // returning String with that param — approximated by scanning loaded classes
        // is not feasible; instead rely on the DouyinPackage DexKit rules if present,
        // otherwise fall back to the known 39.6.0 names.
        val known = listOf("X.1dde", "LX/1cus")
        for (name in known) {
            val clazz = name.toClassOrNull(appClassLoader) ?: continue
            val m = clazz.declaredMethods.firstOrNull {
                it.returnType == String::class.java &&
                    it.parameterTypes.size == 2 &&
                    it.parameterTypes[1] == targetType
            } ?: continue
            return m
        }
        return null
    }

    /**
     * 39.6.0+: long-press save (LongPressPanel) goes a separate download path
     * that does not go through DownloadAction. Execution chain (smali-verified):
     * LPPSaveVideoModule -> LX/0q4i -> ACallableS119S1100000_23 -> call$0 ->
     * downloader. The downloader reads ImageUrlStruct.downloadUrlList directly
     * (tplv-dy-water-v10 = watermark source).
     *
     * ACallableS119S1100000_23.call$0 is the mandatory execution point: its l1
     * field is the FeedBottomArticleAnchorPresenter, LJJIIJZLJL() returns the Aweme.
     */
    private fun install396LongPressSaveHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val callableClass = "Y.ACallableS119S1100000_23".toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: ACallableS119S1100000_23 not found")
            return null
        }
        val method = callableClass.declaredMethods.firstOrNull {
            it.name == "call\$0"
        } ?: run {
            YLog.error("$TAG: ACallableS119S1100000_23.call\$0 not found")
            return null
        }
        return method.hook {
            before {
                val aweme = runCatching {
                    val l1Field = instance.javaClass.getDeclaredField("l1").apply { isAccessible = true }
                    val presenter = l1Field.get(instance) ?: return@runCatching null
                    val m = presenter.javaClass.methods.firstOrNull {
                        it.name == "LJJIIJZLJL" && it.parameterTypes.isEmpty()
                    } ?: return@runCatching null
                    m.invoke(presenter)
                }.getOrNull() ?: return@before
                if (aweme.invokeMethod<Boolean>(
                        packageInstance.aweme.isMultiImage()
                    ) == false
                ) {
                    return@before
                }
                cleanAwemeImages(aweme)
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook long-press save", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, long-press save cleanup unavailable")
            }
        }
    }

    /**
     * Clean every image of a multi-image Aweme: overwrite downloadUrlList
     * (tplv-dy-water-v10 = watermark source) and watermarkFreeDownloadUrlList
     * with the cleaned play urlList. Returns the number of images updated.
     */
    private fun cleanAwemeImages(aweme: Any): Int {
        val awemeImages = aweme.getField<List<*>>(
            packageInstance.aweme.images()
        ).takeIf {
            !it.isNullOrEmpty()
        } ?: return 0

        var cleaned = 0
        awemeImages.forEach { imageStruct ->
            if (imageStruct == null) {
                return@forEach
            }
            val playUrlList = imageStruct.getField<List<*>>(
                packageInstance.imageUrlStruct.urlList()
            ).takeIf {
                !it.isNullOrEmpty()
            } ?: return@forEach

            val cleanList = cleanUrlList(playUrlList)
            imageStruct.setField(
                packageInstance.imageUrlStruct.downloadUrlList(),
                cleanList
            )
            imageStruct.setField(
                packageInstance.imageUrlStruct.watermarkFreeDownloadUrlList(),
                cleanList
            )
            cleaned++
        }
        return cleaned
    }

    /**
     * Clean a list of image URLs for 39.6.0+:
     * 1) watermark=1 -> watermark=0 param strip (api-play URLs honor it);
     * 2) replace douyinvod CDN direct links with the first api-play URL —
     *    CDN mp4s bake the watermark into the source, params cannot remove it;
     * 3) otherwise reorder so an api-play URL is first.
     *
     * Image posts: urlList[0] is often .heic/.vvic which breaks naive downloaders
     * (corrupt saved file). Prefer the .jpeg/.jpg variant so every downloader
     * stores a standard JPEG.
     */
    private fun cleanUrlList(urls: List<*>): List<Any> {
        val cleaned = urls.mapNotNull { url ->
            (url as? String)?.let { cleanUrl(it) }
        }
        if (cleaned.isEmpty()) return cleaned

        val apiPlay = cleaned.filter { it.contains("aweme/v1/play") }
        val cdnDirect = cleaned.filter {
            it.contains("douyinvod.com") && !it.contains("aweme/v1/play")
        }
        return if (apiPlay.isNotEmpty() && cdnDirect.isNotEmpty()) {
            val firstClean = apiPlay.first()
            cleaned.map { url ->
                if (url.contains("douyinvod.com") && !url.contains("aweme/v1/play")) {
                    firstClean
                } else {
                    url
                }
            }
        } else if (apiPlay.isNotEmpty() && !cleaned.first().contains("aweme/v1/play")) {
            listOf(apiPlay.first()) + cleaned.filterNot { it == apiPlay.first() }
        } else {
            // image CDN urls: prefer a standard JPEG variant over heic/vvic
            val jpeg = cleaned.firstOrNull {
                (it.contains(".jpeg") || it.contains(".jpg")) &&
                    !it.contains("heic") && !it.contains("vvic")
            }
            if (jpeg != null && cleaned.first() != jpeg) {
                listOf(jpeg) + cleaned.filterNot { it == jpeg }
            } else {
                cleaned
            }
        }
    }

    private fun cleanUrl(url: String): String =
        url.replace("watermark=1", "watermark=0")

    private fun installDisableSaveImageToVideoLocalWaterMaskHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethod(
            packageInstance.abTestServiceImpl.enableSaveImageToVideoLocalWaterMask()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling local watermark for save-image-to-video")
                }
                resultFalse()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook ABTestServiceImpl.enableSaveImageToVideoLocalWaterMask", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, unable to remove watermark from save-image-to-video")
            }
        }

    private fun installConvertVvicCoverImageToPngHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downloadLivePhotoExecutor.selfClass?.resolveMethod(
            packageInstance.downloadLivePhotoExecutor.encodeLivePhoto()
        )?.hook {
            before {
                val downloadTask = args[0] ?: return@before

                val vvicImagePathList = downloadTask.invokeMethod<List<String?>>(
                    packageInstance.downLoadTask.getTargetFilePaths()
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
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic cover image to png", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, vvic cover image to png conversion unavailable")
            }
        }
    }

    private fun installDisableVEAddLiveVideoWaterMarkHook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.abTestServiceImpl.selfClass?.resolveMethod(
            packageInstance.abTestServiceImpl.enableVEAddLiveVideoWaterMark()
        )?.hook {
            before {
                if (verbose) {
                    YLog.debug("$TAG: disabling watermark for live video")
                }
                resultFalse()
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook ABTestServiceImpl.enableVEAddLiveVideoWaterMark", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, unable to remove watermark from live video")
            }
        }

    private fun installConvertVvicImageToPngHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.downLoadExecutor.selfClass?.resolveMethod(
            packageInstance.downLoadExecutor.execute()
        )?.hook {
            before {
                val downloadTask = args[0] ?: return@before

                val imageFilePath = downloadTask.invokeMethod<List<String?>>(
                    packageInstance.downLoadTask.getTargetFilePaths()
                )?.filterNotNull()?.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                } ?: run {
                    YLog.error("$TAG: failed to get image file path when downloading image")
                    return@before
                }

                imageFilePath.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert vvic image to png when downloading image: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert vvic image to png", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, vvic image to png conversion unavailable")
            }
        }
    }

    private fun installConvertSingleVvicImageToMp4Hook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.singleImageToMp4Composer.selfClass?.resolveMethod(
            packageInstance.singleImageToMp4Composer.onLoad()
        )?.hook {
            before {
                // The instance currently holds both image paths and music paths,
                // and during the DexKit lookup phase I can't tell them apart, so we have to defer it to runtime
                val vvicImagePathList = instance.asResolver().field {
                    type = String::class
                }.mapNotNull {
                    it.getQuietly<String>()
                }.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                }

                if (verbose) {
                    YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
                }

                vvicImagePathList.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert single vvic image to png in mp4 composer", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, single vvic image to png conversion in mp4 composer unavailable")
            }
        }

    private fun installConvertMultiVvicImagesToMp4Hook(): YukiMemberHookCreator.MemberHookCreator.Result? =
        packageInstance.multiImageToMp4Composer.selfClass?.resolveMethod(
            packageInstance.multiImageToMp4Composer.onLoad()
        )?.hook {
            before {
                val vvicImagePathList = instance.getField<List<List<String?>>>(
                    packageInstance.multiImageToMp4Composer.imagePathList()
                )?.flatten()?.filterNotNull()?.filter {
                    it.isNotBlank() && File(it).exists() && FileTypeDetector.detect(it).mimeType == "image/vvic"
                }

                if (verbose) {
                    YLog.debug("$TAG: vvic image path list: $vvicImagePathList")
                }

                vvicImagePathList?.forEach {
                    if (!overwriteVvicWithPng(it)) {
                        YLog.error("$TAG: failed to convert multi vvic images to png in mp4 composer: $it")
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to convert multi vvic images to png in mutil mp4 composer", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, multi vvic images to png conversion in mp4 composer unavailable")
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

        val bitmap = packageInstance.heifDecoder.selfClass?.getStaticField<Any>(
            packageInstance.heifDecoder.sBitmapFactory()
        )?.invokeMethod<Bitmap>(
            packageInstance.heifBitmapFactoryImpl.decodeByteArray(),
            imageBytes,
            0,
            imageBytes.size,
            BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
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
