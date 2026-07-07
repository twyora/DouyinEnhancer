/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package io.github.twyora.douyinenhancer.hook

import android.app.AndroidAppHelper
import android.content.Context
import android.provider.Settings
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.toClass
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.generated.AppProperties
import io.github.twyora.douyinenhancer.hook.utils.weak
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import kotlin.time.measureTimedValue
import org.luckypray.dexkit.DexKitBridge

val Configs.Class.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Field.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Method.nameOrNull
    get() =
        if (hasName()) {
            name
        } else {
            null
        }

val Configs.Method.Parameters.valuesListOrNull
    get() =
        valuesList.ifEmpty {
            null
        }

class DouyinPackage(private val classLoader: ClassLoader, context: Context) {
    data class Field(val name: String?)

    data class Method(val name: String?, val parameters: List<String>?)

    init {
        instance = this
    }

    private val hookInfo: Configs.HookInfo =
        run {
            val (result, time) =
                measureTimedValue {
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
    val commentAudioStruct = CommentAudioStructModule()
    val emoji = EmojiModule()
    val commentActionParams = CommentActionParamsModule()
    val commentLongPressItemModel = CommentLongPressItemModelModule()
    val saveImageActionItem = SaveImageActionItemModule()
    val commentExtensionsKt = CommentExtensionsKtModule()
    val listenerProviderParam = ListenerProviderParamModule()
    val commentImageSaveHelper = CommentImageSaveHelperModule()
    val downloadInfo = DownloadInfoModule()
    val digestUtils = DigestUtilsModule()
    val ugFileUtils = UGFileUtilsKtModule()
    val tokenCert = TokenCertModule()
    val commonItemView = CommonItemViewModule()
    val douYinSettingNewVersionActivity = DouYinSettingNewVersionActivityModule()
    val user = UserModule()
    val aweme = AwemeModule()
    val feedResponseHandler = FeedResponseHandlerModule()
    val commentLongPressWhiteListProvider = CommentLongPressWhiteListProviderModule()

    inner class CommentImageStructModule {
        val selfClass by weak {
            hookInfo.commentImageStruct.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun originUrl() = Field(hookInfo.commentImageStruct.originUrl.nameOrNull)

        fun downloadUrl() = Field(hookInfo.commentImageStruct.downloadUrl.nameOrNull)

        fun getDownloadUrl() = Method(
            hookInfo.commentImageStruct.getDownloadUrl.nameOrNull,
            hookInfo.commentImageStruct.getDownloadUrl.parameters.valuesListOrNull
        )
    }

    inner class UrlModelModule {
        val selfClass by weak {
            hookInfo.urlModel.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun urlList() = Field(hookInfo.urlModel.urlList.nameOrNull)
    }

    inner class CommentModule {
        val selfClass by weak {
            hookInfo.comment.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun emoji() = Field(hookInfo.comment.emoji.nameOrNull)

        fun imageList() = Field(hookInfo.comment.imageList.nameOrNull)

        fun commentAudio() = Field(hookInfo.comment.commentAudio.nameOrNull)
    }

    inner class CommentAudioStructModule {
        val selfClass by weak {
            hookInfo.commentAudioStruct.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun content() = Field(hookInfo.commentAudioStruct.content.nameOrNull)
    }

    inner class EmojiModule {
        val selfClass by weak {
            hookInfo.emoji.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun animateUrl() = Field(hookInfo.emoji.animateUrl.nameOrNull)
    }

    inner class CommentActionParamsModule {
        val selfClass by weak {
            hookInfo.commentActionParams.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun comment() = Field(hookInfo.commentActionParams.comment.nameOrNull)

        fun imageIndex() = Field(hookInfo.commentActionParams.imageIndex.nameOrNull)
    }

    inner class CommentLongPressItemModelModule {
        val selfClass by weak {
            hookInfo.commentLongPressItemModel.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun commentActionParams() = Field(hookInfo.commentLongPressItemModel.commentActionParams.nameOrNull)
    }

    inner class SaveImageActionItemModule {
        val selfClass by weak {
            hookInfo.saveImageActionItem.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun commentActionParams() = Field(hookInfo.saveImageActionItem.cmtActionParams.nameOrNull)

        fun saveImageActionParams() = Field(hookInfo.saveImageActionItem.saveImgActionParams.nameOrNull)

        inner class OnClickExecutorModule {
            val selfClass by weak {
                hookInfo.saveImageActionItem.onClickExecutor.class_.nameOrNull?.toClass(classLoader)
            }

            fun onClick() = Method(
                hookInfo.saveImageActionItem.onClickExecutor.onClick.nameOrNull,
                hookInfo.saveImageActionItem.onClickExecutor.onClick.parameters.valuesListOrNull
            )

            fun hostItem() = Field(hookInfo.saveImageActionItem.onClickExecutor.hostItem.nameOrNull)
        }

        val onClickExecutor = OnClickExecutorModule()

        fun isVisible() = Method(
            hookInfo.saveImageActionItem.isVisible.nameOrNull,
            hookInfo.saveImageActionItem.isVisible.parameters.valuesListOrNull
        )
    }

    inner class CommentExtensionsKtModule {
        val selfClass by weak {
            hookInfo.commentExtensionKt.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun hasValidImageUrl() = Method(
            hookInfo.commentExtensionKt.hasValidImageUrl.nameOrNull,
            hookInfo.commentExtensionKt.hasValidImageUrl.parameters.valuesListOrNull
        )
    }

    inner class ListenerProviderParamModule {
        val selfClass by weak {
            hookInfo.listenerProviderParam.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun context() = Field(hookInfo.listenerProviderParam.context.nameOrNull)

        fun cert() = Field(hookInfo.listenerProviderParam.cert.nameOrNull)
    }

    inner class CommentImageSaveHelperModule {
        val selfClass by weak {
            hookInfo.commentImageSaveHelper.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun onSuccessed() = Method(
            hookInfo.commentImageSaveHelper.onSuccessed.nameOrNull,
            hookInfo.commentImageSaveHelper.onSuccessed.parameters.valuesListOrNull
        )

        fun notifyResult() = Method(
            hookInfo.commentImageSaveHelper.notifyResult.nameOrNull,
            hookInfo.commentImageSaveHelper.notifyResult.parameters.valuesListOrNull
        )

        fun listenerProviderParam() = Field(hookInfo.commentImageSaveHelper.listenerProviderParam.nameOrNull)
    }

    inner class DownloadInfoModule {
        val selfClass by weak {
            hookInfo.downloadInfo.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun url() = Field(hookInfo.downloadInfo.url.nameOrNull)

        fun getTargetFilePath() = Method(
            hookInfo.downloadInfo.getTargetFilePath.nameOrNull,
            hookInfo.downloadInfo.getTargetFilePath.parameters.valuesListOrNull
        )
    }

    inner class DigestUtilsModule {
        val selfClass by weak {
            hookInfo.digestUtils.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun md5Hex() = Method(
            hookInfo.digestUtils.md5Hex.nameOrNull,
            hookInfo.digestUtils.md5Hex.parameters.valuesListOrNull
        )
    }

    inner class UGFileUtilsKtModule {
        val selfClass by weak {
            hookInfo.ugFileUtils.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun context() = Field(hookInfo.ugFileUtils.context.nameOrNull)

        fun copyFile() = Method(
            hookInfo.ugFileUtils.copyFile.nameOrNull,
            hookInfo.ugFileUtils.copyFile.parameters.valuesListOrNull
        )

        fun getStorageDir() = Method(
            hookInfo.ugFileUtils.getStorageDir.nameOrNull,
            hookInfo.ugFileUtils.getStorageDir.parameters.valuesListOrNull
        )

        fun getExternalStorageDir() = Method(
            hookInfo.ugFileUtils.getExternalStorageDir.nameOrNull,
            hookInfo.ugFileUtils.getExternalStorageDir.parameters.valuesListOrNull
        )

        fun getImageUri() = Method(
            hookInfo.ugFileUtils.getImageUri.nameOrNull,
            hookInfo.ugFileUtils.getImageUri.parameters.valuesListOrNull
        )

        fun createUri() = Method(
            hookInfo.ugFileUtils.createUri.nameOrNull,
            hookInfo.ugFileUtils.createUri.parameters.valuesListOrNull
        )

        fun getAudioUri() = Method(
            hookInfo.ugFileUtils.getAudioUri.nameOrNull,
            hookInfo.ugFileUtils.getAudioUri.parameters.valuesListOrNull
        )
    }

    inner class TokenCertModule {
        val selfClass by weak {
            hookInfo.tokenCert.class_.nameOrNull
                ?.toClass(classLoader)
        }
    }

    inner class CommonItemViewModule {
        val selfClass by weak {
            hookInfo.commonItemView.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun setLeftText() = Method(
            hookInfo.commonItemView.setLeftText.nameOrNull,
            hookInfo.commonItemView.setLeftText.parameters.valuesListOrNull
        )

        fun setRightUIMode() = Method(
            hookInfo.commonItemView.setRightUiMode.nameOrNull,
            hookInfo.commonItemView.setRightUiMode.parameters.valuesListOrNull
        )

        fun setLeftIcon() = Method(
            hookInfo.commonItemView.setLeftIcon.nameOrNull,
            hookInfo.commonItemView.setLeftIcon.parameters.valuesListOrNull
        )

        fun setRightText() = Method(
            hookInfo.commonItemView.setRightText.nameOrNull,
            hookInfo.commonItemView.setRightText.parameters.valuesListOrNull
        )

        fun setLeftTextAndIcon() = Method(
            hookInfo.commonItemView.setLeftTextAndIcon.nameOrNull,
            hookInfo.commonItemView.setLeftTextAndIcon.parameters.valuesListOrNull
        )
    }

    inner class DouYinSettingNewVersionActivityModule {
        val selfClass by weak {
            hookInfo.douYinSettingNewVersionActivity.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun settingsScrollView() = Field(
            hookInfo.douYinSettingNewVersionActivity.settingsScrollView.nameOrNull
        )

        fun onResume() = Method(
            hookInfo.douYinSettingNewVersionActivity.onResume.nameOrNull,
            hookInfo.douYinSettingNewVersionActivity.onResume.parameters.valuesListOrNull
        )
    }

    inner class UserModule {
        val selfClass by weak {
            hookInfo.user.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun nickname() = Field(hookInfo.user.nickname.nameOrNull)
        fun uid() = Field(hookInfo.user.uid.nameOrNull)
    }

    inner class AwemeModule {
        val selfClass by weak {
            hookInfo.aweme.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun desc() = Field(hookInfo.aweme.desc.nameOrNull)

        fun author() = Field(hookInfo.aweme.author.nameOrNull)

        fun getAd() = Method(
            hookInfo.aweme.getAd.nameOrNull,
            hookInfo.aweme.getAd.parameters.valuesListOrNull
        )

        fun itemTitle() = Field(hookInfo.aweme.itemTitle.nameOrNull)

        fun duration() = Field(hookInfo.aweme.duration.nameOrNull)

        fun isNormalVideo() = Method(
            hookInfo.aweme.isNormalVideo.nameOrNull,
            hookInfo.aweme.isNormalVideo.parameters.valuesListOrNull
        )

        fun isEcomAweme() = Method(
            hookInfo.aweme.isEcomAweme.nameOrNull,
            hookInfo.aweme.isEcomAweme.parameters.valuesListOrNull
        )

        fun grouponLargeCard() = Field(hookInfo.aweme.grouponLargeCard.nameOrNull)
    }

    inner class FeedResponseHandlerModule {
        val selfClass by weak {
            hookInfo.feedResponseHandler.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun processAwemeList() = Method(
            hookInfo.feedResponseHandler.processAwemeList.nameOrNull,
            hookInfo.feedResponseHandler.processAwemeList.parameters.valuesListOrNull
        )
    }

    inner class CommentLongPressWhiteListProviderModule {
        val selfClass by weak {
            hookInfo.commentLongPressWhiteListProvider.class_.nameOrNull
                ?.toClass(classLoader)
        }

        fun buildWhiteList() = Method(
            hookInfo.commentLongPressWhiteListProvider.buildWhiteList.nameOrNull,
            hookInfo.commentLongPressWhiteListProvider.buildWhiteList.parameters.valuesListOrNull
        )
    }

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        @Volatile
        lateinit var instance: DouyinPackage

        private fun readHookInfo(context: Context): Configs.HookInfo {
            val androidId =
                Settings.Secure.getString(
                    context.contentResolver,
                    Settings.Secure.ANDROID_ID
                ) ?: "unknown"
            val hookInfoFileName =
                "${AppProperties.PROJECT_APPLICATION_ID}-${androidId.hashCode().toUInt()}"
                    .hashCode().toHexString()
            YLog.debug("$TAG: hookInfoFileName: $hookInfoFileName")

            runCatching {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (!(hookInfoFile.isFile && hookInfoFile.canRead())) {
                    YLog.debug("$TAG: hookInfoFile is not a file or can not be read")
                    return@runCatching null
                }

                val hostAppPackageInfo =
                    context.packageManager.getPackageInfo(
                        AndroidAppHelper.currentPackageName(),
                        0
                    )
                val hostAppLastUpdateTime = hostAppPackageInfo.lastUpdateTime
                val hostAppVersionCode = hostAppPackageInfo.versionCode

                val moduleLastUpdateTime =
                    runCatching {
                        context.packageManager
                            .getPackageInfo(
                                AppProperties.PROJECT_NAMESPACE,
                                0
                            ).lastUpdateTime
                    }.getOrDefault(hostAppLastUpdateTime)

                val hookInfo =
                    FileInputStream(hookInfoFile).use {
                        runCatching {
                            Configs.HookInfo.parseFrom(it)
                        }.getOrNull() ?: Configs.HookInfo.newBuilder().build()
                    }

                if (hookInfo.lastUpdateTime >= moduleLastUpdateTime &&
                    hookInfo.lastUpdateTime >= hostAppLastUpdateTime &&
                    hookInfo.hostVersionCode == hostAppVersionCode &&
                    hookInfo.moduleVersionCode == AppProperties.PROJECT_VERSION_CODE &&
                    hookInfo.moduleVersionName == AppProperties.PROJECT_VERSION_NAME
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
            val hostAppPackageInfo =
                context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                )

            lastUpdateTime =
                maxOf(
                    hostAppPackageInfo.lastUpdateTime,
                    runCatching {
                        context.packageManager
                            .getPackageInfo(
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
                commentImageStruct =
                    commentImageStruct {
                        runCatching {
                            val cmtImgClsName =
                                "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                            val originUrlFieldName = "originUrl"
                            val downloadUrlFieldName = "downloadUrl"
                            val getDownloadUrlMethodData =
                                bridge
                                    .findMethod {
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
                                    YLog.error(
                                        "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                    )
                                    return@commentImageStruct
                                }

                            class_ =
                                class_ {
                                    name = cmtImgClsName
                                }
                            originUrl =
                                field {
                                    name = originUrlFieldName
                                }
                            downloadUrl =
                                field {
                                    name = downloadUrlFieldName
                                }
                            getDownloadUrl =
                                method {
                                    name = getDownloadUrlMethodData.methodName
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            values.addAll(getDownloadUrlMethodData.paramTypeNames)
                                        }
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                urlModel =
                    urlModel {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.base.model.UrlModel"
                            }
                        urlList =
                            field {
                                name = "urlList"
                            }
                    }

                comment =
                    comment {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.comment.model.Comment"
                            }
                        emoji =
                            field {
                                name = "emoji"
                            }
                        imageList =
                            field {
                                name = "imageList"
                            }
                        commentAudio =
                            field {
                                name = "commentAudio"
                            }
                    }

                commentAudioStruct =
                    commentAudioStruct {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.comment.model.CommentAudioStruct"
                            }
                        content =
                            field {
                                name = "content"
                            }
                    }

                emoji =
                    emoji {
                        class_ =
                            class_ {
                                name = "com.ss.android.ugc.aweme.emoji.model.Emoji"
                            }
                        animateUrl =
                            method {
                                name = "animateUrl"
                            }
                    }

                commentActionParams =
                    commentActionParams {
                        runCatching {
                            val cmtActionParamsClsName =
                                "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            val commentFieldName =
                                cmtActionParamsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.model.Comment"
                                    }?.self
                                    ?.name
                            val imageFieldName =
                                cmtActionParamsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = Int::class
                                    }?.self
                                    ?.name
                            if (commentFieldName == null || imageFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentActionParams
                            }

                            class_ =
                                class_ {
                                    name = cmtActionParamsClsName
                                }
                            comment =
                                field {
                                    name = commentFieldName
                                }
                            imageIndex =
                                field {
                                    name = imageFieldName
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                commentLongPressItemModel =
                    commentLongPressItemModel {
                        runCatching {
                            val commentLongPressItemModelClsName =
                                "com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel"
                            val commentActionParamsFieldName =
                                commentLongPressItemModelClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                                    }?.self
                                    ?.name

                            if (commentActionParamsFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentLongPressItemModel
                            }

                            class_ =
                                class_ {
                                    name = commentLongPressItemModelClsName
                                }
                            commentActionParams =
                                field {
                                    name = commentActionParamsFieldName
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                saveImageActionItem =
                    saveImageActionItem {
                        runCatching {
                            val saveImageActionItemClsName =
                                "com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem"
                            // SaveImageActionItem extends CommentLongPressItemModel, ensure commentLongPressItemModel is populated first!
                            val cmtActionParamsFieldName =
                                this@hookInfo.commentLongPressItemModel.commentActionParams?.name
                            val saveImageActionParamsFieldName =
                                saveImageActionItemClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                                    }?.self
                                    ?.name
                            val onClickMethodData = bridge
                                .findMethod {
                                    matcher {
                                        modifiers = Modifier.STATIC + Modifier.FINAL + Modifier.PUBLIC
                                        returnType = "java.lang.Object"
                                        params {
                                            count = 1
                                        }
                                        addUsingString("bpea-comment_save_image_to_album")
                                    }
                                }.singleOrNull()
                            val onClickHostItemFieldName =
                                onClickMethodData?.declaredClassName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                                    type = Object::class
                                }?.self?.name
                            val isVisibleMethodData = bridge
                                .findMethod {
                                    matcher {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        declaredClass = saveImageActionItemClsName
                                        returnType = "boolean"
                                        usingFields {
                                            add {
                                                field {
                                                    cmtActionParamsFieldName?.let {
                                                        name = it
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }.singleOrNull()
                            if (cmtActionParamsFieldName == null || saveImageActionParamsFieldName == null || onClickMethodData == null ||
                                onClickHostItemFieldName == null ||
                                isVisibleMethodData == null
                            ) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@saveImageActionItem
                            }

                            class_ =
                                class_ {
                                    name = saveImageActionItemClsName
                                }
                            cmtActionParams =
                                field {
                                    name = cmtActionParamsFieldName
                                }
                            saveImgActionParams =
                                field {
                                    name = saveImageActionParamsFieldName
                                }
                            onClickExecutor = saveImageActionItemOnClickExecutor {
                                class_ = class_ {
                                    name = onClickMethodData.className
                                }
                                onClick = method {
                                    name = onClickMethodData.methodName
                                    parameters = MethodKt.parameters {
                                        values.clear()
                                        values.addAll(onClickMethodData.paramTypeNames)
                                    }
                                }
                                hostItem = field {
                                    name = onClickHostItemFieldName
                                }
                            }
                            isVisible = method {
                                name = isVisibleMethodData.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(isVisibleMethodData.paramTypeNames)
                                }
                            }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                commentExtensionKt = commentExtensionKt {
                    runCatching {
                        bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                declaredClass = "com.ss.android.ugc.aweme.comment.util.CommentExtensionsKt"
                                returnType = "boolean"
                                params {
                                    add("com.ss.android.ugc.aweme.comment.model.Comment")
                                    add("int")
                                }
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
                        }.singleOrNull()
                            ?.also { match ->
                                class_ =
                                    class_ {
                                        name = match.className
                                    }
                                hasValidImageUrl =
                                    method {
                                        name = match.methodName
                                        parameters =
                                            MethodKt.parameters {
                                                values.clear()
                                                values.addAll(match.paramTypeNames)
                                            }
                                    }
                            } ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@commentExtensionKt
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                listenerProviderParam = listenerProviderParam {
                    runCatching {
                        val clsData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                fields {
                                    add {
                                        type {
                                            descriptor = "Landroid/content/Context;"
                                        }
                                    }
                                    add {
                                        type {
                                            descriptor = "Lcom/bytedance/bpea/cert/token/TokenCert;"
                                        }
                                    }
                                }
                                method {
                                    name = "toString"
                                    usingStrings {
                                        add("ListenerProviderParam(context=")
                                    }
                                }
                            }
                        }.singleOrNull()

                        val clsName = clsData?.name

                        val contextFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "android.content.Context"
                            }?.self?.name

                        val certFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "com.bytedance.bpea.cert.token.TokenCert"
                            }?.self?.name

                        if (clsName == null || contextFieldName == null || certFieldName == null) {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated fields"
                            )
                            return@listenerProviderParam
                        }

                        class_ = class_ {
                            name = clsName
                        }
                        this.context = field {
                            name = contextFieldName
                        }
                        cert = field {
                            name = certFieldName
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                commentImageSaveHelper =
                    commentImageSaveHelper {
                        runCatching {
                            val onSuccessedMethodData = bridge
                                .findMethod {
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
                                }.singleOrNull()

                            val clsName = onSuccessedMethodData?.declaredClassName

                            val notifyResultMethod =
                                clsName?.toClass(hostAppClassLoader)?.resolve()
                                    ?.firstMethodOrNull {
                                        modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                                        parameters(Context::class, Boolean::class)
                                        parameterCount = 2
                                        superclass()
                                    }?.self
                            val listenerProviderParamFieldName = clsName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                                type = this@hookInfo.listenerProviderParam.class_.nameOrNull
                            }?.self?.name
                            if (onSuccessedMethodData == null || notifyResultMethod == null || listenerProviderParamFieldName == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@commentImageSaveHelper
                            }

                            class_ =
                                class_ {
                                    name = clsName
                                }
                            onSuccessed =
                                method {
                                    name = onSuccessedMethodData.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            values.addAll(onSuccessedMethodData.paramTypeNames)
                                        }
                                }
                            notifyResult =
                                method {
                                    name = notifyResultMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            notifyResultMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            listenerProviderParam = field {
                                name = listenerProviderParamFieldName
                            }
                            return@commentImageSaveHelper
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                downloadInfo =
                    downloadInfo {
                        class_ =
                            class_ {
                                name = "com.ss.android.socialbase.downloader.model.DownloadInfo"
                            }
                        url =
                            field {
                                name = "url"
                            }
                        getTargetFilePath =
                            method {
                                name = "getTargetFilePath"
                            }
                    }

                digestUtils =
                    digestUtils {
                        runCatching {
                            val digestUtilsClsName = "com.bytedance.common.utility.DigestUtils"
                            val md5HexFieldMethod =
                                digestUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "md5Hex"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC)
                                        parameters(String::class)
                                    }?.self
                            if (md5HexFieldMethod == null) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@digestUtils
                            }

                            class_ =
                                class_ {
                                    name = digestUtilsClsName
                                }
                            md5Hex =
                                method {
                                    name = md5HexFieldMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            md5HexFieldMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                ugFileUtils =
                    uGFileUtilsKt {
                        runCatching {
                            val ugFileUtilsClsName = "com.bytedance.android.ug.UGFileUtilsKt"
                            val copyFileMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
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
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getStorageDir"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(String::class, Boolean::class)
                                        parameterCount = 2
                                    }?.self
                            val getExternalStorageDirectoryMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getExternalStorageDirectory"
                                        returnType = String::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(String::class, Boolean::class)
                                        parameterCount = 2
                                    }?.self
                            val getImageUriMethod =
                                ugFileUtilsClsName
                                    .toClass(hostAppClassLoader)
                                    .resolve()
                                    .firstMethodOrNull {
                                        name = "getImageUri"
                                        returnType = android.net.Uri::class
                                        modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                        parameters(
                                            Context::class,
                                            String::class,
                                            String::class,
                                            String::class,
                                            "com.bytedance.bpea.cert.token.TokenCert"
                                        )
                                        parameterCount = 5
                                    }?.self
                            val createUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                                name = "createUri"
                                returnType = android.net.Uri::class
                                modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                parameters(
                                    String::class,
                                    Boolean::class,
                                    Array<android.net.Uri>::class,
                                    "com.bytedance.bpea.cert.token.TokenCert"
                                )
                                parameterCount = 4
                            }?.self
                            val getAudioUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                                name = "getAudioUri"
                                parameters(
                                    Context::class,
                                    String::class,
                                    String::class,
                                    String::class,
                                    "com.bytedance.bpea.cert.token.TokenCert"
                                )
                            }?.self
                            if (copyFileMethod == null || getStorageDirMethod == null || getExternalStorageDirectoryMethod == null ||
                                getImageUriMethod == null || createUriMethod == null || getAudioUriMethod == null
                            ) {
                                YLog.error(
                                    "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                                )
                                return@uGFileUtilsKt
                            }

                            class_ =
                                class_ {
                                    name = ugFileUtilsClsName
                                }
                            this.context =
                                field {
                                    name = "context"
                                }
                            copyFile =
                                method {
                                    name = copyFileMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            copyFileMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getStorageDir =
                                method {
                                    name = getStorageDirMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getStorageDirMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getExternalStorageDir =
                                method {
                                    name = getExternalStorageDirectoryMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getExternalStorageDirectoryMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            getImageUri =
                                method {
                                    name = getImageUriMethod.name
                                    parameters =
                                        MethodKt.parameters {
                                            values.clear()
                                            getImageUriMethod.parameterTypes.forEach { paramType ->
                                                values.add(paramType.name)
                                            }
                                        }
                                }
                            createUri = method {
                                name = createUriMethod.name
                                parameters =
                                    MethodKt.parameters {
                                        values.clear()
                                        createUriMethod.parameterTypes.forEach { paramType ->
                                            values.add(paramType.name)
                                        }
                                    }
                            }
                            getAudioUri = method {
                                name = getAudioUriMethod.name
                                parameters =
                                    MethodKt.parameters {
                                        values.clear()
                                        getAudioUriMethod.parameterTypes.forEach { paramType ->
                                            values.add(paramType.name)
                                        }
                                    }
                            }
                        }.onFailure {
                            YLog.error("$TAG: Unable to populate config", it)
                        }
                    }

                tokenCert =
                    tokenCert {
                        class_ =
                            class_ {
                                name = "com.bytedance.bpea.cert.token.TokenCert"
                            }
                    }

                commonItemView = commonItemView {
                    class_ = class_ {
                        name = "com.bytedance.ies.dmt.ui.common.views.CommonItemView"
                    }
                    setLeftText = method {
                        name = "setLeftText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                    setRightUiMode = method {
                        name = "setRightUIMode"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setLeftIcon = method {
                        name = "setLeftIcon"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setRightText = method {
                        name = "setRightText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                    setLeftTextAndIcon = method {
                        name = "setLeftTextAndIcon"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                            values.add("int")
                        }
                    }
                }

                douYinSettingNewVersionActivity = douYinSettingNewVersionActivity {
                    runCatching {
                        val dySettingsNewVersionActivityClsName = "com.ss.android.ugc.aweme.setting.ui.DouYinSettingNewVersionActivity"
                        val settingsScrollViewFieldName = dySettingsNewVersionActivityClsName.toClass(
                            hostAppClassLoader
                        ).resolve().firstFieldOrNull {
                            type = "com.ss.android.ugc.aweme.setting.ui.SettingNestedScrollView"
                        }?.self?.name

                        if (settingsScrollViewFieldName == null) {
                            YLog.error("$TAG: Unable to populate config, settingsScrollViewFieldName is null")
                            return@runCatching
                        }

                        class_ = class_ {
                            name = dySettingsNewVersionActivityClsName
                        }
                        settingsScrollView = field {
                            name = settingsScrollViewFieldName
                        }
                        onResume = method {
                            name = "onResume"
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                user = user {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.profile.model.User"
                    }
                    nickname = field {
                        name = "nickname"
                    }
                    uid = field {
                        name = "uid"
                    }
                }

                aweme = aweme {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.Aweme"
                    }
                    desc = field {
                        name = "desc"
                    }
                    author = field {
                        name = "author"
                    }
                    getAd = method {
                        name = "getAd"
                    }
                    itemTitle = field {
                        name = "itemTitle"
                    }
                    duration = field {
                        name = "duration"
                    }
                    isNormalVideo = method {
                        name = "isNormalVideo"
                    }
                    isEcomAweme = method {
                        name = "isEcomAweme"
                    }
                    grouponLargeCard = field {
                        name = "grouponLargeCard"
                    }
                }

                feedResponseHandler = feedResponseHandler {
                    runCatching {
                        bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC + Modifier.STATIC
                                returnType = "void"
                                params {
                                    add("int")
                                    add("java.lang.String")
                                    add("java.util.List")
                                }
                                invokeMethods {
                                    add {
                                        descriptor = "Ljava/util/List;->size()I"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->setRequestId(Ljava/lang/String;)V"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAd()Z"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/awemeservice/api/IAwemeService;->updateAweme(Lcom/ss/android/ugc/aweme/feed/model/Aweme;I)Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->isLive()Z"
                                    }
                                }
                            }
                        }.singleOrNull()?.also { match ->
                            class_ = class_ {
                                name = match.className
                            }
                            processAwemeList = method {
                                name = match.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(match.paramTypeNames)
                                }
                            }
                        } ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@feedResponseHandler
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }

                commentLongPressWhiteListProvider = commentLongPressWhiteListProvider {
                    runCatching {
                        val buildWhiteListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC
                                returnType = "java.util.Set"
                                params {
                                    add("com.ss.android.ugc.aweme.comment.CommentActionParams")
                                }
                                usingStrings {
                                    add("custom")
                                    add("default")
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(
                                "$TAG: Unable to populate ${this::class.simpleName} config, possibly due to unfound obfuscated methods"
                            )
                            return@commentLongPressWhiteListProvider
                        }

                        class_ = class_ {
                            name = buildWhiteListMethodData.className
                        }
                        buildWhiteList = method {
                            name = buildWhiteListMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(buildWhiteListMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error("$TAG: Unable to populate config", it)
                    }
                }
            }
        }
    }
}
