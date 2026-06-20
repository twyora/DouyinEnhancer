package com.yst.mkga.hook.dy.hook

import java.lang.reflect.Field

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog

import com.yst.mkga.hook.dy.hook.utils.getField

object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            val packageInstance = DouyinPackage.instance

            packageInstance.commentImageStruct.selfClass?.resolve()?.firstMethodOrNull {
                name = packageInstance.commentImageStruct.getDownloadUrl().name
            }?.hook {
                before {
                    val originUrl =
                        instance.getField<Any?>(packageInstance.commentImageStruct.originUrl())
                    if (originUrl != null) {
                        result = originUrl
                    }
                }
            } ?: run {
                YLog.warn("$TAG: Target method not found, watermark-free comment image download is not active")
            }
        }
    }
}