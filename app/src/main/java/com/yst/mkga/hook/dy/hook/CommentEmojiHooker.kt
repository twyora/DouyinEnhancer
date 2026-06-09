package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import com.yst.mkga.hook.dy.hook.utils.HookTransaction
import org.luckypray.dexkit.DexKitBridge
import java.lang.reflect.Field
import java.lang.reflect.Modifier

object CommentEmojiHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val CommentClass by lazyClass("com.ss.android.ugc.aweme.comment.model.Comment")
    private val EmojiClass by lazyClass("com.ss.android.ugc.aweme.emoji.model.Emoji")
    private val UrlModelClass by lazyClass("com.ss.android.ugc.aweme.base.model.UrlModel")
    private val SaveImageActionItemClass by lazyClass("com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem")
    private val CommentActionParamsClass by lazyClass("com.ss.android.ugc.aweme.comment.CommentActionParams")
    private val CommentLongPressItemModelClass by lazyClass(
        "com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel"
    )
    private val CommentImageStructClass by lazyClass(
        "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
    )
    private var saveImageActionItemClassCommentActionParamsField: Field? = null
    private var commentActionParamsClassFragmentActivityField: Field? = null
    private var commentActionParamsClassCommentField: Field? = null
    private var commentActionParamsClassImageIndexField: Field? = null
    private var CommentLongPressItemModelClassCommentActionParamsField: Field? = null

    private val CommentClassEmojiField: Field by lazy {
        CommentClass.getDeclaredField("emoji").apply {
            isAccessible = true
        }
    }

    private val EmojiClassAnimateUrlField: Field by lazy {
        EmojiClass.getDeclaredField("animateUrl")
            .apply {
                isAccessible = true
            }
    }

    private val UrlModelClassUrlListField: Field by lazy {
        UrlModelClass.getDeclaredField("urlList").apply {
            isAccessible = true
        }
    }

    private val CommentClassImageListField: Field by lazy {
        CommentClass.getDeclaredField("imageList").apply {
            isAccessible = true
        }
    }

    private val CommentImageStructClassDownloadUrlField: Field by lazy {
        CommentImageStructClass.getDeclaredField("downloadUrl").apply {
            isAccessible = true
        }
    }

    override fun onHook() {
        withProcess(mainProcessName) {
            val transaction = HookTransaction(TAG)

            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                transaction.add(::installSaveEmojiBtnHook.name) {
                    installSaveEmojiBtnHook(bridge)
                }

                transaction.add(::installSaveEmojiCallbackHook.name) {
                    installSaveEmojiCallbackHook(bridge)
                }

                transaction.commit()
            }
        }
    }

    private fun getEmojiUrlListFromCommentOrNull(comment: Any?): List<String>? {
        return runCatching {
            val emoji = CommentClassEmojiField.get(comment)
            val animateUrl = EmojiClassAnimateUrlField.get(emoji)
            @Suppress("UNCHECKED_CAST")
            (UrlModelClassUrlListField.get(animateUrl) as? List<String>)
        }.getOrNull()
    }

    private fun getImageUrlListFromCommentOrNull(
        comment: Any?,
        imageIndex: Int = 0
    ): List<String>? {
        comment ?: return null

        // start to extract image url list
        val imageList = (CommentClassImageListField.get(comment) as? List<*>)
            ?: return null//List<CommentImageStruct>

        val imageStruct = imageList.getOrNull(imageIndex) ?: return null

        val downloadUrlModel =
            CommentImageStructClassDownloadUrlField.get(imageStruct) ?: return null

        @Suppress("UNCHECKED_CAST")
        val downloadUrlList = UrlModelClassUrlListField.get(downloadUrlModel) as? List<String>

        return downloadUrlList
    }

    private fun getCommentFromSaveImageActionItemOrNull(saveImageActionItem: Any?): Any? {
        saveImageActionItem ?: return null

        val longPressActionParamsField = CommentLongPressItemModelClassCommentActionParamsField
            ?: CommentLongPressItemModelClass.resolve().firstField {
                type = CommentActionParamsClass.name
            }.self.also {
                it.isAccessible = true
                CommentLongPressItemModelClassCommentActionParamsField = it
            }
        val longPressActionParams =
            longPressActionParamsField.get(saveImageActionItem) ?: return null

        // get comment
        val commentField =
            commentActionParamsClassCommentField ?: CommentActionParamsClass.resolve().firstField {
                type = CommentClass.name
            }.self.also {
                it.isAccessible = true
                commentActionParamsClassCommentField = it
            }

        return commentField.get(longPressActionParams)
    }

    private fun getImageUrlListFromSaveImageActionItemOrNull(saveImageActionItem: Any?): List<String>? {
        saveImageActionItem ?: return null

        val actionParamsField =
            saveImageActionItemClassCommentActionParamsField ?: SaveImageActionItemClass.resolve()
                .firstField {
                    type = CommentActionParamsClass.name
                }.self.also {
                    it.isAccessible = true
                    saveImageActionItemClassCommentActionParamsField = it
                }

        val commentActionParams = actionParamsField.get(saveImageActionItem)

        // get the image index from the long-pressed comment
        val imageIndexField =
            commentActionParamsClassImageIndexField ?: CommentActionParamsClass.resolve()
                .firstField {
                    type = Int::class
                }.self.also {
                    it.isAccessible = true
                    commentActionParamsClassImageIndexField = it
                }
        val imageIndex = imageIndexField.get(commentActionParams) as Int

        val comment = getCommentFromSaveImageActionItemOrNull(saveImageActionItem) ?: return null

        return getImageUrlListFromCommentOrNull(comment, imageIndex)
    }

    private fun installSaveEmojiBtnHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        // Force enable "Save to Album" button for emoji comments
        bridge.findMethod {
            matcher {
                modifiers = Modifier.STATIC
                params {
                    add(CommentClass.name)
                    add("int")
                }
                returnType = "boolean"
                invokeMethods {
                    // TODO: This method has already been looked up in CommentImageHooker, consider caching and sharing it here
                    add {
                        declaredClass = "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                        returnType = UrlModelClass.name
                        paramCount = 0
                        addUsingField {
                            name = "downloadUrl"
                        }
                    }
                }
            }
        }.singleOrNull()?.also { result ->
            YLog.debug("$TAG: Target method found: ${result.className}.${result.methodName}")

            result.className.toClass().resolve().firstMethod {
                name = result.methodName
            }.hook {
                before {
                    val comment = args[0] ?: return@before

                    val emojiUrlList = getEmojiUrlListFromCommentOrNull(comment)
                    if (emojiUrlList?.isNotEmpty() == true) {
                        resultTrue()
                    }
                }
            }.result {
                onHookingFailure { throwable ->
                    YLog.error("$TAG: Unable to replace with original emoji URL: ${throwable.message}")
                }
                onConductFailure { param, throwable ->
                    YLog.error("$TAG: Error during download callback hook: ${throwable.message}")
                    param.result = param.callOriginal()
                }
                onHooked {
                    YLog.info("$TAG: Hook installed, 'Save to album' button will show for emoji comments")
                }.also {
                    ret = it
                }
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, save to album feature is not active")
        }

        return ret
    }

    private fun installSaveEmojiCallbackHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        bridge.findMethod {
            matcher {
                modifiers = Modifier.STATIC + Modifier.FINAL + Modifier.PUBLIC
                returnType = "java.lang.Object"
                params {
                    count = 1
                }
                addUsingString("bpea-comment_save_image_to_album")
            }
        }.singleOrNull()?.also { result ->
            YLog.debug("$TAG: Target method found: ${result.className}.${result.methodName}")

            result.className.toClass().resolve().firstMethod {
                name = result.methodName
            }.hook {
                before {
                    val cbkInstance = args[0] ?: run {
                        YLog.debug("$TAG: Failed to extract cbkInstance from args")
                        return@before
                    }

                    val saveImageActionItem = cbkInstance.asResolver().firstField {
                        type = "java.lang.Object"
                    }.get() ?: run {
                        YLog.debug("$TAG: Failed to extract saveImageActionItem from args")
                        return@before
                    }

                    val comment = getCommentFromSaveImageActionItemOrNull(saveImageActionItem)
                        ?: return@before

                    val emojiUrlList = getEmojiUrlListFromCommentOrNull(comment) ?: return@before

                    YLog.warn("$TAG: Download Emoji Function is NOT IMPLEMENT yet, got ${emojiUrlList.size} emoji url(s)")
                    return@before
                }
            }.result {
                onConductFailure { param, throwable ->
                    YLog.error("$TAG: Error during download callback hook: ${throwable.message}")
                    param.result = param.callOriginal()
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: Failed to install download callback hook: ${throwable.message}")
                }
                onHooked {
                    YLog.info("$TAG: Download callback hook installed, emoji download function is active")
                }.also {
                    ret = it
                }
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, emoji download function is not active")
        }

        return ret
    }
}