/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package com.yst.mkga.hook.dy.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.provider.Settings
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import kotlin.time.measureTimedValue

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge

import com.yst.mkga.hook.dy.generated.AppProperties
import com.yst.mkga.hook.dy.hook.utils.weak

val Configs.Class.orNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Method.orNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Field.orNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Method.Parameters
    get() = if (hasParameters()) {
        parameters
    } else {
        null
    }

class DouyinPackage(private val classLoader: ClassLoader, context: Context) {
    data class Method(val name: String, val parameters: List<String>)
    data class Field(val name: String)

    init {
        instance = this
    }

    private val hookInfo: Configs.HookInfo = run {
        val (result, time) = measureTimedValue {
            readHookInfo(context)
        }
        YLog.debug("$TAG: load hookInfo time: $time")
        YLog.info("$TAG: hookInfo: $result")

        result
    }

    fun hostVersionCode() = hookInfo.hostVersionCode

    val commentImageStruct = CommentImageStructModule()
    val urlModel = UrlModelModule()
    val comment = CommentModule()
    val emoji = EmojiModule()
    val commentActionParams = CommentActionParamsModule()
    val commentLongPressItemModel = CommentLongPressItemModelModule()
    val saveImageActionItem = SaveImageActionItemModule()
    val commentExtensionsKt = CommentExtensionsKtModule()
    val commentSaveToAlbumButtonClick = CommentSaveToAlbumButtonClickModule()
    val commentImageSaveHelper = CommentImageSaveHelperModule()
    val downloadInfo = DownloadInfoModule()
    val digestUtils = DigestUtilsModule()
    val ugFileUtils = UGFileUiltsKtModule()
    val tokenCert = TokenCertModule()

    inner class CommentImageStructModule {
        val selfClass by weak {
            hookInfo.commentImageStruct.class_.name.toClass(classLoader)
        }

        fun originUrl() = Field(hookInfo.commentImageStruct.originUrl.name)
        fun downloadUrl() = Field(hookInfo.commentImageStruct.downloadUrl.name)
        fun getDownloadUrl() = Method(
            hookInfo.commentImageStruct.getDownloadUrl.name,
            hookInfo.commentImageStruct.getDownloadUrl.parameters.valuesList
        )
    }

    inner class UrlModelModule {
        val selfClass by weak {
            hookInfo.urlModel.class_.name.toClass(classLoader)
        }

        fun urlList() = Field(hookInfo.urlModel.urlList.name)
    }

    inner class CommentModule {
        val selfClass by weak {
            hookInfo.comment.class_.name.toClass(classLoader)
        }

        fun emoji() = Field(hookInfo.comment.emoji.name)
        fun imageList() = Field(hookInfo.comment.imageList.name)
    }

    inner class EmojiModule {
        val selfClass by weak {
            hookInfo.emoji.class_.name.toClass(classLoader)
        }

        fun animateUrl() = Field(hookInfo.emoji.animateUrl.name)
    }

    inner class CommentActionParamsModule {
        val selfClass by weak {
            hookInfo.commentActionParams.class_.name.toClass(classLoader)
        }

        fun comment() = Field(hookInfo.commentActionParams.comment.name)
        fun imageIndex() = Field(hookInfo.commentActionParams.imageIndex.name)
    }

    inner class CommentLongPressItemModelModule {
        val selfClass by weak {
            hookInfo.commentLongPressItemModel.class_.name.toClass(classLoader)
        }

        fun commentActionParams() =
            Field(hookInfo.commentLongPressItemModel.commentActionParams.name)
    }

    inner class SaveImageActionItemModule {
        val selfClass by weak {
            hookInfo.saveImageActionItem.class_.name.toClass(classLoader)
        }

        fun commentActionParams() = Field(hookInfo.saveImageActionItem.cmtActionParams.name)
        fun saveImageActionParams() = Field(hookInfo.saveImageActionItem.saveImgActionParams.name)
    }

    inner class CommentExtensionsKtModule {
        val selfClass by weak {
            hookInfo.cmtSaveToAlbumBtnVisibility.class_.name.toClass(classLoader)
        }

        fun saveToAlbumVisibility() = Method(
            hookInfo.cmtSaveToAlbumBtnVisibility.checkVisibility.name,
            hookInfo.cmtSaveToAlbumBtnVisibility.checkVisibility.parameters.valuesList
        )
    }

    inner class CommentSaveToAlbumButtonClickModule {
        val selfClass by weak {
            hookInfo.cmtSaveToAlbumBtnClickedCallback.class_.name.toClass(classLoader)
        }

        fun onClicked() = Method(
            hookInfo.cmtSaveToAlbumBtnClickedCallback.clickedCallback.name,
            hookInfo.cmtSaveToAlbumBtnClickedCallback.clickedCallback.parameters.valuesList
        )
    }

    inner class CommentImageSaveHelperModule {
        val selfClass by weak {
            hookInfo.commentImageSaveHelper.class_.name.toClass(classLoader)
        }


        fun onSuccessed() = Method(
            hookInfo.commentImageSaveHelper.onSuccessed.name,
            hookInfo.commentImageSaveHelper.onSuccessed.parameters.valuesList
        )

        fun notifyResult() = Method(
            hookInfo.commentImageSaveHelper.notifyResult.name,
            hookInfo.commentImageSaveHelper.notifyResult.parameters.valuesList
        )
    }

    inner class DownloadInfoModule {
        val selfClass by weak {
            hookInfo.downloadInfo.class_.name.toClass(classLoader)
        }

        fun url() = Field(hookInfo.downloadInfo.url.name)
        fun getTargetFilePath() = Method(
            hookInfo.downloadInfo.getTargetFilePath.name,
            hookInfo.downloadInfo.getTargetFilePath.parameters.valuesList
        )
    }

    inner class DigestUtilsModule {
        val selfClass by weak {
            hookInfo.digestUtils.class_.name.toClass(classLoader)
        }

        fun md5Hex() = Method(
            hookInfo.digestUtils.md5Hex.name,
            hookInfo.digestUtils.md5Hex.parameters.valuesList
        )
    }

    inner class UGFileUiltsKtModule {
        val selfClass by weak {
            hookInfo.ugFileUtils.class_.name.toClass(classLoader)
        }

        fun context() = Field(hookInfo.ugFileUtils.context.name)
        fun copyFile() = Method(
            hookInfo.ugFileUtils.copyFile.name,
            hookInfo.ugFileUtils.copyFile.parameters.valuesList
        )

        fun getStorageDir() =
            Method(
                hookInfo.ugFileUtils.getStorageDir.name,
                hookInfo.ugFileUtils.getStorageDir.parameters.valuesList
            )

        fun getExternalStorageDir() = Method(
            hookInfo.ugFileUtils.getExternalStorageDir.name,
            hookInfo.ugFileUtils.getExternalStorageDir.parameters.valuesList
        )

        fun getImageUri() =
            Method(
                hookInfo.ugFileUtils.getImageUri.name,
                hookInfo.ugFileUtils.getImageUri.parameters.valuesList
            )

        fun createUri() =
            Method(
                hookInfo.ugFileUtils.createUri.name,
                hookInfo.ugFileUtils.createUri.parameters.valuesList
            )
    }

    inner class TokenCertModule {
        val selfClass by weak {
            hookInfo.tokenCert.class_.name.toClass(classLoader)
        }
    }

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        @Volatile
        lateinit var instance: DouyinPackage

        private fun readHookInfo(context: Context): Configs.HookInfo {
            val androidId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            ) ?: "unknown"
            val hookInfoFileName = androidId.hashCode().toUInt().toHexString()
            YLog.debug("$TAG: hookInfoFileName: $hookInfoFileName")

            runCatching {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (!(hookInfoFile.isFile && hookInfoFile.canRead())) {
                    YLog.debug("$TAG: hookInfoFile is not a file or can not be read")
                    return@runCatching null
                }

                val hostAppPackageInfo = context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                )
                val hostAppLastUpdateTime = hostAppPackageInfo.lastUpdateTime
                val hostAppVersionCode = hostAppPackageInfo.versionCode

                val moduleLastUpdateTime = runCatching {
                    context.packageManager.getPackageInfo(
                        AppProperties.PROJECT_NAMESPACE,
                        0
                    ).lastUpdateTime
                }.getOrDefault(hostAppLastUpdateTime)

                val hookInfo = FileInputStream(hookInfoFile).use {
                    runCatching {
                        Configs.HookInfo.parseFrom(it)
                    }.getOrNull() ?: Configs.HookInfo.newBuilder().build()
                }

                if (hookInfo.lastUpdateTime >= moduleLastUpdateTime
                    && hookInfo.lastUpdateTime >= hostAppLastUpdateTime
                    && hookInfo.hostVersionCode == hostAppVersionCode
                    && hookInfo.moduleVersionCode == AppProperties.PROJECT_VERSION_CODE
                    && hookInfo.moduleVersionName == AppProperties.PROJECT_VERSION_NAME
                ) {
                    return hookInfo
                } else {
                    YLog.debug("$TAG: hookInfo is outdated, will re-generate")
                }
            }.onFailure {
                YLog.error("$TAG: failed to read hookInfo: ", it)
            }

            return initHookInfo(context).also {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (hookInfoFile.exists()) {
                    hookInfoFile.delete()
                }
                FileOutputStream(hookInfoFile).use { o ->
                    it.writeTo(o)
                }
            }
        }

        private fun initHookInfo(context: Context) = hookInfo {
            val hostAppClassLoader = context.classLoader
            val hostAppPackageInfo = context.packageManager.getPackageInfo(
                AndroidAppHelper.currentPackageName(),
                0
            )

            lastUpdateTime = maxOf(
                hostAppPackageInfo.lastUpdateTime,
                runCatching {
                    context.packageManager.getPackageInfo(
                        AppProperties.PROJECT_NAMESPACE,
                        0
                    ).lastUpdateTime
                }.getOrDefault(hostAppPackageInfo.lastUpdateTime)
            )
            moduleVersionCode = AppProperties.PROJECT_VERSION_CODE
            moduleVersionName = AppProperties.PROJECT_VERSION_NAME
            hostVersionCode = hostAppPackageInfo.versionCode
            generation = 0

            DexKitBridge.create(context.applicationInfo.sourceDir).use { bridge ->
                commentImageStruct = commentImageStruct {
                    val cmtImgClsName = "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                    val originUrlFieldName = "originUrl"
                    val downloadUrlFieldName = "downloadUrl"
                    val getDownloadUrlMethodData = bridge.findMethod {
                        matcher {
                            declaredClass =
                                "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                            returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                            paramCount = 0
                            addUsingField {
                                name = "downloadUrl"
                            }
                        }
                    }.singleOrNull() ?: run {
                        YLog.debug("$TAG: sourceDir: ${context.applicationInfo.sourceDir}")
                        YLog.error("$TAG: getDownloadUrl in CommentImageStruct not matched, hookInfo will lack getDownloadUrl config")
                        return@commentImageStruct
                    }

                    class_ = class_ {
                        name = cmtImgClsName
                    }
                    originUrl = field {
                        name = originUrlFieldName
                    }
                    downloadUrl = field {
                        name = downloadUrlFieldName
                    }
                    getDownloadUrl = method {
                        name = getDownloadUrlMethodData.methodName
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.addAll(getDownloadUrlMethodData.paramTypeNames)
                        }
                    }
                }

                urlModel = urlModel {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.base.model.UrlModel"
                    }
                    urlList = field {
                        name = "urlList"
                    }
                }

                comment = comment {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.comment.model.Comment"
                    }
                    emoji = field {
                        name = "emoji"
                    }
                    imageList = field {
                        name = "imageList"
                    }
                }

                emoji = emoji {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.emoji.model.Emoji"
                    }
                    animateUrl = method {
                        name = "animateUrl"
                    }
                }

                commentActionParams = commentActionParams {
                    val cmtActionParamsClsName =
                        "com.ss.android.ugc.aweme.comment.CommentActionParams"
                    val commentFieldName =
                        cmtActionParamsClsName.toClass(hostAppClassLoader).resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.model.Comment"
                            }?.self?.name
                    val imageFieldName =
                        cmtActionParamsClsName.toClass(hostAppClassLoader).resolve()
                            .firstFieldOrNull {
                                type = Int::class
                            }?.self?.name
                    if (commentFieldName == null || imageFieldName == null) {
                        YLog.error("$TAG: commentField or intField is null, hookInfo will lack commentField or intField config")
                        return@commentActionParams
                    }

                    class_ = class_ {
                        name = cmtActionParamsClsName
                    }
                    comment = field {
                        name = commentFieldName
                    }
                    imageIndex = field {
                        name = imageFieldName
                    }
                }

                commentLongPressItemModel = commentLongPressItemModel {
                    val commentLongPressItemModelClsName =
                        "com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel"
                    val commentActionParamsFieldName =
                        commentLongPressItemModelClsName.toClass(hostAppClassLoader).resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            }?.self?.name

                    if (commentActionParamsFieldName == null) {
                        YLog.error("$TAG: commentActionParamsField is null, hookInfo will lack commentActionParamsField config")
                        return@commentLongPressItemModel
                    }

                    class_ = class_ {
                        name = commentLongPressItemModelClsName
                    }
                    commentActionParams = field {
                        name = commentActionParamsFieldName
                    }
                }

                saveImageActionItem = saveImageActionItem {
                    val saveImageActionItemClsName =
                        "com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem"
                    // SaveImageActionItem extends CommentLongPressItemModel, ensure commentLongPressItemModel is populated first!
                    val cmtActionParamsFieldName =
                        this@hookInfo.commentLongPressItemModel.commentActionParams?.name
                    val saveImageActionParamsFieldName =
                        saveImageActionItemClsName.toClass(hostAppClassLoader).resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            }?.self?.name
                    if (cmtActionParamsFieldName == null || saveImageActionParamsFieldName == null) {
                        YLog.error("$TAG: cmtLongPressItemModelField or saveImageActionParamsField is null, hookInfo will lack cmtLongPressItemModelField or saveImageActionParamsField config")
                        return@saveImageActionItem
                    }

                    class_ = class_ {
                        name = saveImageActionItemClsName
                    }
                    cmtActionParams = field {
                        name = cmtActionParamsFieldName
                    }
                    saveImgActionParams = field {
                        name = saveImageActionParamsFieldName
                    }
                }

                cmtSaveToAlbumBtnVisibility = cmtSaveToAlbumBtnVisibility {
                    bridge.findMethod {
                        matcher {
                            modifiers = Modifier.STATIC
                            params {
                                add("com.ss.android.ugc.aweme.comment.model.Comment")
                                add("int")
                            }
                            returnType = "boolean"
                            invokeMethods {
                                add {
                                    declaredClass =
                                        "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                                    returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                    paramCount = 0
                                    addUsingField {
                                        name = "downloadUrl"
                                    }
                                }
                            }
                        }
                    }.singleOrNull()?.also { match ->
                        class_ = class_ {
                            name = match.className
                        }
                        checkVisibility = method {
                            name = match.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(match.paramTypeNames)
                            }
                        }
                    }
                }

                cmtSaveToAlbumBtnClickedCallback = cmtSaveToAlbumBtnClickedCallback {
                    bridge.findMethod {
                        matcher {
                            modifiers = Modifier.STATIC + Modifier.FINAL + Modifier.PUBLIC
                            returnType = "java.lang.Object"
                            params {
                                count = 1
                            }
                            addUsingString("bpea-comment_save_image_to_album")
                        }
                    }.singleOrNull()?.also { match ->
                        class_ = class_ {
                            name = match.className
                        }
                        clickedCallback = method {
                            name = match.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(match.paramTypeNames)
                            }
                        }
                    }
                }

                commentImageSaveHelper = commentImageSaveHelper {
                    bridge.findMethod {
                        matcher {
                            name = "onSuccessed"
                            modifiers = Modifier.FINAL + Modifier.PUBLIC
                            returnType = "void"
                            params {
                                add("com.ss.android.socialbase.downloader.model.DownloadInfo")
                            }
                            usingStrings {
                                add("/douyin/comment")
                                add("comment_")
                            }
                            invokeMethods {
                                add {
                                    descriptor =
                                        "Lcom/bytedance/android/ug/UGFileUtilsKt;->copyFile(Ljava/lang/String;Ljava/lang/String;Lcom/bytedance/bpea/cert/token/TokenCert;)Z"
                                }
                            }
                        }
                    }.singleOrNull()?.also { match ->
                        val notifyResultMethod =
                            match.className.toClass(hostAppClassLoader).resolve()
                                .firstMethodOrNull {
                                    modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                                    parameters(Context::class, Boolean::class)
                                    parameterCount = 2
                                    superclass()
                                }?.self
                        if (notifyResultMethod == null) {
                            YLog.error("$TAG: notifyResultMethod is null, hookInfo will lack notifyResultMethod config")
                            return@commentImageSaveHelper
                        }

                        class_ = class_ {
                            name = match.className
                        }
                        onSuccessed = method {
                            name = match.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(match.paramTypeNames)
                            }
                        }
                        notifyResult = method {
                            name = notifyResultMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                notifyResultMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                    }
                }

                downloadInfo = downloadInfo {
                    class_ = class_ {
                        name = "com.ss.android.socialbase.downloader.model.DownloadInfo"
                    }
                    url = field {
                        name = "url"
                    }
                    getTargetFilePath = method {
                        name = "getTargetFilePath"
                    }
                }

                digestUtils = digestUtils {
                    val digestUtilsClsName = "com.bytedance.common.utility.DigestUtils"
                    val md5HexFieldMethod =
                        digestUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "md5Hex"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC)
                            parameters(String::class)
                        }?.self
                    if (md5HexFieldMethod == null) {
                        YLog.error("$TAG: md5HexFieldMethod is null, hookInfo will lack md5HexFieldMethod config")
                        return@digestUtils
                    }

                    class_ = class_ {
                        name = digestUtilsClsName
                    }
                    md5Hex = method {
                        name = md5HexFieldMethod.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            md5HexFieldMethod.parameterTypes.forEach { paramType ->
                                values.add(paramType.name)
                            }
                        }
                    }
                }

                ugFileUtils = uGFileUtilsKt {
                    val ugFileUtilsClsName = "com.bytedance.android.ug.UGFileUtilsKt"
                    val copyFileMethod =
                        ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "copyFile"
                            returnType = Boolean::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(
                                String::class,
                                String::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                            parameterCount = 3
                        }?.self
                    val getStorageDirMethod =
                        ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getStorageDir"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(String::class, Boolean::class)
                            parameterCount = 2
                        }?.self
                    val getExternalStorageDirectoryMethod =
                        ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getExternalStorageDirectory"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(String::class, Boolean::class)
                            parameterCount = 2
                        }?.self
                    val getImageUriMethod =
                        ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getImageUri"
                            returnType = android.net.Uri::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(
                                android.content.Context::class,
                                String::class,
                                String::class,
                                String::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                            parameterCount = 5
                        }?.self
                    if (copyFileMethod == null || getStorageDirMethod == null || getExternalStorageDirectoryMethod == null || getImageUriMethod == null) {
                        YLog.error("$TAG: copyFileMethod or getStorageDirMethod or getExternalStorageDirectoryMethod or getImageUriMethod is null, hookInfo will lack copyFileMethod or getStorageDirMethod or getExternalStorageDirectoryMethod or getImageUriMethod config")
                        return@uGFileUtilsKt
                    }

                    class_ = class_ {
                        name = ugFileUtilsClsName
                    }
                    this.context = field {
                        name = "context"
                    }
                    copyFile = method {
                        name = copyFileMethod.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            copyFileMethod.parameterTypes.forEach { paramType ->
                                values.add(paramType.name)
                            }
                        }
                    }
                    getStorageDir = method {
                        name = getStorageDirMethod.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            getStorageDirMethod.parameterTypes.forEach { paramType ->
                                values.add(paramType.name)
                            }
                        }
                    }
                    getExternalStorageDir = method {
                        name = getExternalStorageDirectoryMethod.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            getExternalStorageDirectoryMethod.parameterTypes.forEach { paramType ->
                                values.add(paramType.name)
                            }
                        }
                    }
                    getImageUri = method {
                        name = getImageUriMethod.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            getImageUriMethod.parameterTypes.forEach { paramType ->
                                values.add(paramType.name)
                            }
                        }
                    }
                }

                tokenCert = tokenCert {
                    class_ = class_ {
                        name = "com.bytedance.bpea.cert.token.TokenCert"
                    }
                }
            }
        }
    }
}