package io.github.twyora.douyinenhancer.hook

import android.content.Context
import android.net.Uri
import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.hook.utils.HookTransaction
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod
import io.github.twyora.douyinenhancer.hook.utils.setField
import org.json.JSONObject
import java.io.FileInputStream

object CommentVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(name = mainProcessName) {
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
    }

    private fun installForceSaveImageVisibleHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.saveImageActionItem.selfClass?.resolveMethod(
            packageInstance.saveImageActionItem.isVisible()
        )?.hook {
            after {
                if ((result as? Boolean) == true) {
                    return@after
                }

                // if the comment contains audio, show the save image button
                val commentAudio = instance.getField<Any>(
                    packageInstance.commentLongPressItemModel.commentActionParams()
                )?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                )?.getField<Any>(
                    packageInstance.comment.commentAudio()
                )
                if (commentAudio != null) {
                    resultTrue()
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: hook failed to show save image button for audio comment", throwable)
            }
        }
    }

    private fun installAddSaveImageToWhiteListHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.commentLongPressWhiteListProvider.selfClass?.resolveMethod(
            packageInstance.commentLongPressWhiteListProvider.buildWhiteList()
        )?.hook {
            after {
                @Suppress("UNCHECKED_CAST")
                val whiteList = result as? MutableSet<String> ?: run {
                    YLog.warn("$TAG: whiteList is not a mutable set")
                    return@after
                }
                if (whiteList.contains("save_image")) {
                    return@after
                }

                val commentAudio = args[0]?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                )?.getField<Any>(
                    packageInstance.comment.commentAudio()
                )
                if (commentAudio != null) {
                    whiteList.add("save_image")
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: hook failed to add \"save_image\" to comment white list", throwable)
            }
        }
    }

    private fun installInjectAudioUrlOnSaveClickHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.saveImageActionItem.onClickExecutor.selfClass?.resolveMethod(
            packageInstance.saveImageActionItem.onClickExecutor.onClick()
        )?.hook {
            var originImageIndex = -1
            var originImageUrlList: List<*>? = null
            before {
                val saveImageActionItem = args[0]?.getField<Any>(
                    packageInstance.saveImageActionItem.onClickExecutor.hostItem()
                ) ?: return@before

                val comment = saveImageActionItem.getField<Any>(
                    packageInstance.saveImageActionItem.commentActionParams()
                )?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                ) ?: return@before

                val commentAudio = comment.getField<Any>(
                    packageInstance.comment.commentAudio()
                ) ?: return@before
                val commentAudioContent = commentAudio.getField<String>(
                    packageInstance.commentAudioStruct.content()
                ) ?: return@before

                val commentAudioJson = runCatching {
                    JSONObject(commentAudioContent)
                }.onFailure {
                    YLog.error("$TAG: failed to parse comment audio content", it)
                }.getOrNull() ?: return@before
                val commentAudioList = commentAudioJson.optJSONArray("video_list") ?: return@before
                val firstCommentAudio = commentAudioList.optJSONObject(0) ?: return@before
                val commentAudioMainUrl = firstCommentAudio.optString("main_url") ?: return@before
                val commentAudioBackupUrl = firstCommentAudio.optString("backup_url") ?: return@before

                // store and override image index
                originImageIndex = overrideImageIndex(saveImageActionItem, 0)

                // store origin image url list
                originImageUrlList = comment.getField<List<*>>(
                    packageInstance.comment.imageList()
                )
                // inject audio url into image url list
                if (!injectAudioUrl(comment, listOf(commentAudioMainUrl, commentAudioBackupUrl))) {
                    YLog.error("$TAG: failed to inject audio url(s) into comment")
                    return@before
                }

                YLog.debug("$TAG: injected audio url(s) into comment")
            }
            after {
                val saveImageActionItem = args[0]?.getField<Any>(
                    packageInstance.saveImageActionItem.onClickExecutor.hostItem()
                ) ?: return@after
                originImageIndex.takeIf {
                    it >= 0
                }?.let {
                    overrideImageIndex(saveImageActionItem, it)
                    originImageIndex = -1
                }

                val comment = saveImageActionItem.getField<Any>(
                    packageInstance.saveImageActionItem.commentActionParams()
                )?.getField<Any>(
                    packageInstance.commentActionParams.comment()
                ) ?: return@after
                originImageUrlList?.let {
                    comment.setField(
                        packageInstance.comment.imageList(),
                        it
                    )
                    originImageUrlList = null
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: hook failed to inject audio url on save click", throwable)
            }
        }
    }

    private fun installSaveDownloadedAudioHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        val packageInstance = DouyinPackage.instance

        return packageInstance.commentImageSaveHelper.selfClass?.resolveMethod(
            packageInstance.commentImageSaveHelper.onSuccessed()
        )?.hook {
            before {
                val downloadInfo = args[0] ?: return@before

                val dlUrl = downloadInfo.getField<String>(
                    packageInstance.downloadInfo.url()
                ) ?: return@before
                if (!dlUrl.contains("&mime_type=audio")) {
                    return@before
                }

                val dlInfoMimeType = downloadInfo.invokeMethod<String>(
                    DouyinPackage.Method(
                        name = "getMimeType",
                        parameters = null
                    )
                )
                YLog.debug("$TAG: dlInfoMimeType: $dlInfoMimeType")

                val sourcePath = downloadInfo.invokeMethod<String>(
                    packageInstance.downloadInfo.getTargetFilePath()
                ) ?: return@before
                val targetFileName = "audio_${
                    packageInstance.digestUtils.selfClass?.invokeStaticMethod<String>(
                        packageInstance.digestUtils.md5Hex(),
                        dlUrl + System.currentTimeMillis().toString()
                    )
                }.m4a"

                val context = instance.asResolver().firstFieldOrNull()?.get()?.asResolver()?.firstFieldOrNull {
                    type = Context::class.java
                }?.get<Context>() ?: run {
                    YLog.error("$TAG: hook failed to extract context from download info")
                    return@before
                }

                val targetUri = packageInstance.ugFileUtils.selfClass?.invokeStaticMethod<Uri>(
                    DouyinPackage.Method(
                        name = "getAudioUri",
                        parameters = null
                    ),
                    context,
                    targetFileName,
                    "audio/mp4",
                    "Music/douyin/audio",
                    packageInstance.tokenCert.selfClass?.getConstructor(
                        String::class.java
                    )?.newInstance(
                        "bpea-comment_save_image_to_album"
                    )
                ) ?: run {
                    YLog.error("$TAG: hook failed to create audio media uri")
                    return@before
                }
                if (targetUri == Uri.EMPTY) {
                    YLog.error("$TAG: hook failed to create audio media uri, uri is empty")
                    return@before
                }

                val copyState = runCatching {
                    context.contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                        FileInputStream(sourcePath).use { inputStream ->
                            inputStream.copyTo(outputStream)
                        }
                    }
                    true
                }.onFailure {
                    YLog.error("$TAG: hook failed to copy audio to media store", it)
                }.getOrDefault(false)

                instance.invokeMethod<Any?>(
                    packageInstance.commentImageSaveHelper.notifyResult(),
                    context,
                    copyState
                )

                resultNull()
            }
        }?.onConductFailure { _, throwable ->
            YLog.error("$TAG: hook failed to save downloaded audio", throwable)
        }
    }

    private fun overrideImageIndex(actionItem: Any, index: Int): Int {
        val packageInstance = DouyinPackage.instance

        val actionParams = actionItem.getField<Any>(
            packageInstance.saveImageActionItem.saveImageActionParams()
        ) ?: return -1

        // store origin image index
        val originImageIndex = actionParams.getField<Int>(
            packageInstance.commentActionParams.imageIndex()
        ) ?: -1
        // set image index
        actionParams.setField(
            packageInstance.commentActionParams.imageIndex(),
            index
        )

        return originImageIndex
    }

    private fun injectAudioUrl(comment: Any, url: List<String>): Boolean {
        val packageInstance = DouyinPackage.instance

        val existingImageUrlList = comment.getField<List<*>>(
            packageInstance.comment.imageList()
        )
        val imageUrlList = if (existingImageUrlList.isNullOrEmpty()) {
            val newImageUrlList = listOf<Any?>(
                packageInstance.commentImageStruct.selfClass?.getConstructor()?.newInstance()
            )
            comment.setField(
                packageInstance.comment.imageList(),
                newImageUrlList
            )
            newImageUrlList
        } else {
            existingImageUrlList
        }

        val existingImageUrlModel = imageUrlList.first()?.getField<Any?>(
            packageInstance.commentImageStruct.downloadUrl()
        )
        val imageUrlModel = if (existingImageUrlModel == null) {
            val newImageUrlModel = packageInstance.urlModel.selfClass?.getConstructor()?.newInstance()
            imageUrlList.first()?.setField(
                packageInstance.commentImageStruct.downloadUrl(),
                newImageUrlModel
            )
            newImageUrlModel
        } else {
            existingImageUrlList
        } ?: return false

        val existingUrlList = imageUrlModel.getField<List<String>>(
            packageInstance.urlModel.urlList()
        )
        val finalUrlList = if (existingUrlList == null) {
            url
        } else {
            existingUrlList + url
        }

        imageUrlModel.setField(
            packageInstance.urlModel.urlList(),
            finalUrlList
        )

        return true
    }
}
