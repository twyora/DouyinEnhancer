package com.yst.mkga.hook.dy.hook

import java.lang.reflect.Field

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import org.luckypray.dexkit.DexKitBridge

object CommentImageHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    override fun onHook() {
        withProcess(mainProcessName) {
            DouyinPackage.instance.commentImageStructClass?.resolve()?.firstMethodOrNull {
                name = DouyinPackage.instance.getDownloadUrlMethodName()
            }?.hook {
                before {
                    val originUrl = instance.asResolver().firstFieldOrNull {
                        name = DouyinPackage.instance.originUrlFieldName()
                    }?.get()
                    if (originUrl != null) {
                        result = originUrl
                    }
                }
            }?:run{
                YLog.warn("$TAG: Target method not found, watermark-free comment image download is not active")
            }
        }
    }
}