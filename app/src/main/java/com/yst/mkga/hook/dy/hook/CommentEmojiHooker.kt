package com.yst.mkga.hook.dy.hook

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
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
    private var saveImageActionItemClassCommentActionParamsField: Field? = null
    private var commentActionParamsClassFragmentActivityField: Field? = null
    private var commentActionParamsClassCommentField: Field? = null

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

    override fun onHook() {
        withProcess(mainProcessName) {
            DexKitBridge.create(this.appInfo.sourceDir).use { bridge ->
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
                                declaredClass =
                                    "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
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
                        }
                    }
                } ?: run {
                    YLog.warn("$TAG: Target method not found, save to album feature is not active")
                }

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

                            val commentActionParamsField =
                                saveImageActionItemClassCommentActionParamsField ?: runCatching {
                                    SaveImageActionItemClass.resolve().firstField {
                                        type = CommentActionParamsClass.name
                                    }.self.also {
                                        saveImageActionItemClassCommentActionParamsField = it
                                    }
                                }.onFailure {
                                    YLog.error("$TAG: Failed to find CommentActionParams field in SaveImageActionItem: ${it.message}")
                                }.getOrNull()

                            val commentActionParams =
                                commentActionParamsField?.get(saveImageActionItem) ?: run {
                                    YLog.debug("$TAG: Failed to get CommentActionParams from SaveImageActionItem")
                                    return@before
                                }

                            val commentField = commentActionParamsClassCommentField ?: runCatching {
                                CommentActionParamsClass.resolve().firstField {
                                    type = CommentClass.name
                                }.self.also {
                                    commentActionParamsClassCommentField = it
                                }
                            }.onFailure {
                                YLog.error("$TAG: Failed to find Comment field in CommentActionParams: ${it.message}")
                            }.getOrNull()

                            val comment = commentField?.get(commentActionParams) ?: run {
                                YLog.debug("$TAG: Failed to get Comment from CommentActionParams")
                                return@before
                            }

                            val emojiUrlList = getEmojiUrlListFromCommentOrNull(comment)?.takeIf {
                                it.isNotEmpty()
                            } ?: return@before

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
                        }
                    }
                } ?: run {
                    YLog.warn("$TAG: Target method not found, emoji download function is not active")
                }
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
}