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
    private val CommentLongPressItemModelClass by lazyClass("com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel")
    private val CommentImageStructClass by lazyClass("com.ss.android.ugc.aweme.comment.model.CommentImageStruct")

    private val CommentClassEmojiField: Field by lazy {
        CommentClass.resolve().firstField {
            name = "emoji"
        }.self.apply {
            isAccessible = true
        }
    }

    private val EmojiClassAnimateUrlField: Field by lazy {
        EmojiClass.resolve().firstField {
            name = "animateUrl"
        }.self.apply {
            isAccessible = true
        }
    }

    private val UrlModelClassUrlListField: Field by lazy {
        UrlModelClass.resolve().firstField {
            name = "urlList"
        }.self.apply {
            isAccessible = true
        }
    }

    private val CommentClassImageListField: Field by lazy {
        CommentClass.resolve().firstField {
            name = "imageList"
        }.self.apply {
            isAccessible = true
        }
    }

    private val CommentImageStructClassDownloadUrlField: Field by lazy {
        CommentImageStructClass.resolve().firstField {
            name = "downloadUrl"
        }.self.apply {
            isAccessible = true
        }
    }

    private var commentActionParamsClassCommentField: Field? = null
    private var commentActionParamsClassImageIndexField: Field? = null
    private var commentLongPressItemModelClassCommentActionParamsField: Field? = null
    private var saveImageActionItemClassCommentActionParamsField: Field? = null

    override fun onHook() {
        withProcess(mainProcessName) {
            val transaction = HookTransaction(TAG)
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
                transaction.add(::installSaveEmojiToAlbumButtonHook.name) {
                    installSaveEmojiToAlbumButtonHook(bridge)
                }
                transaction.add(::installClickSaveEmojiToAlbumButtonCallbackHook.name) {
                    installClickSaveEmojiToAlbumButtonCallbackHook(bridge)
                }
                transaction.commit()
            }
        }
    }

    private fun extractActionParams(actionItem: Any): Any? {
        val field = commentLongPressItemModelClassCommentActionParamsField ?: CommentLongPressItemModelClass.resolve()
            .firstField {
                type = CommentActionParamsClass.name
            }.self.also {
                it.isAccessible = true
                commentLongPressItemModelClassCommentActionParamsField = it
            }
        return field.get(actionItem)
    }

    private fun extractComment(actionItem: Any): Any? {
        val params = extractActionParams(actionItem) ?: return null
        val field = commentActionParamsClassCommentField
            ?: CommentActionParamsClass.resolve().firstField {
                type = CommentClass.name
            }.self.also {
                it.isAccessible = true
                commentActionParamsClassCommentField = it
            }
        return field.get(params)
    }

    private fun extractEmojiUrls(comment: Any): List<String>? {
        return runCatching {
            val emoji = CommentClassEmojiField.get(comment) ?: return@runCatching null
            val animateUrl = EmojiClassAnimateUrlField.get(emoji) ?: return@runCatching null
            @Suppress("UNCHECKED_CAST")
            UrlModelClassUrlListField.get(animateUrl) as? List<String>
        }.getOrNull()
    }

    private fun overrideImageIndex(actionItem: Any, index: Int): Int {
        val paramsField = saveImageActionItemClassCommentActionParamsField
            ?: SaveImageActionItemClass.resolve().firstField {
                type = CommentActionParamsClass.name
            }.self.also {
                it.isAccessible = true
                saveImageActionItemClassCommentActionParamsField = it
            }

        val params = paramsField.get(actionItem) ?: return -1
        val field = commentActionParamsClassImageIndexField
            ?: CommentActionParamsClass.resolve().firstField {
                type = Int::class
            }.self.also {
                it.isAccessible = true
                commentActionParamsClassImageIndexField = it
            }

        val originImageIndex = field.get(params) as Int
        field.set(params, index)
        return originImageIndex
    }

    private fun injectEmojiUrls(comment: Any, emojiUrls: List<String>, prepend: Boolean = true) {
        var imageList = CommentClassImageListField.get(comment) as? List<*>
        if (imageList.isNullOrEmpty()) {
            val newStruct = CommentImageStructClass.getConstructor().newInstance()
            imageList = listOf(newStruct)
            CommentClassImageListField.set(comment, imageList)
        }

        val targetStruct = imageList[0]
        var urlModel = CommentImageStructClassDownloadUrlField.get(targetStruct)
        if (urlModel == null) {
            urlModel = UrlModelClass.getConstructor().newInstance()
            CommentImageStructClassDownloadUrlField.set(targetStruct, urlModel)
        }

        var finalUrls: List<String>? = if (prepend) {
            @Suppress("UNCHECKED_CAST")
            val imageUrls = UrlModelClassUrlListField.get(urlModel) as? List<String>
            if (imageUrls.isNullOrEmpty()) {
                emojiUrls
            } else {
                emojiUrls + imageUrls
            }
        } else {
            emojiUrls
        }
        UrlModelClassUrlListField.set(urlModel, finalUrls)
    }

    private fun installSaveEmojiToAlbumButtonHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
        var ret: YukiMemberHookCreator.MemberHookCreator.Result? = null

        bridge.findMethod {
            matcher {
                modifiers = Modifier.STATIC
                params {
                    add(CommentClass.name)
                    add("int")
                }
                returnType = "boolean"
                invokeMethods {
                    add {
                        declaredClass = CommentImageStructClass.name
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

            result.className.toClass().resolve().firstMethod { name = result.methodName }.hook {
                before {
                    val comment = args[0] ?: return@before
                    val emojiUrls = extractEmojiUrls(comment)
                    if (!emojiUrls.isNullOrEmpty()) {
                        resultTrue()
                    }
                }
            }.result {
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
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, save emoji to album button will not be shown")
        }
        return ret
    }

    private fun installClickSaveEmojiToAlbumButtonCallbackHook(bridge: DexKitBridge): YukiMemberHookCreator.MemberHookCreator.Result? {
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

            result.className.toClass().resolve().firstMethod { name = result.methodName }.hook {
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

                    // store original state for after
                    originImageUrlList = CommentClassImageListField.get(comment) as? List<*>
                    originImageIndex = overrideImageIndex(actionItem, 0)
                    savedActionItem = actionItem
                    savedComment = comment

                    injectEmojiUrls(comment, emojiUrls, prepend = true)

                    YLog.debug("$TAG: Injected ${emojiUrls.size} emoji url(s) into comment.")

                    YLog.warn("$TAG: HEIF to GIF conversion not yet implemented, saved file may not be viewable in gallery")
                }
                after {
                    savedComment?.let { c ->
                        CommentClassImageListField.set(c, originImageUrlList)
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
            }.result {
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
            }
        } ?: run {
            YLog.warn("$TAG: Target method not found, download callback hook will not be installed")
        }

        return ret
    }
}