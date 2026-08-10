package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess

@HookOnMainProcess
object FeedVideoHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_VIDEO_REMOVE_WATERMARK, false)) {
            if (verbose) {
                YLog.debug("$TAG: remove watermark disabled, skip feed video hook")
            }
            return
        }

        // 39.6.0+: miscDownloadAddrUtil anchor (is_ug_can_re_download string) is gone.
        // UrlModel.getUrlList() is the single choke point every downloader uses to
        // obtain video URLs, and UrlModel is a stable class name across 39.6.0/39.9.0.
        val urlModelClass = "com.ss.android.ugc.aweme.base.model.UrlModel".toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: UrlModel not found")
            return
        }
        val getUrlList = urlModelClass.declaredMethods.firstOrNull {
            it.name == "getUrlList" && it.parameterTypes.isEmpty()
        } ?: run {
            YLog.error("$TAG: UrlModel.getUrlList() not found")
            return
        }
        getUrlList.hook {
            after {
                val list = result as? List<*> ?: return@after
                var changed = false
                val cleaned = java.util.ArrayList<Any>(list.size)
                for (url in list) {
                    val s = url as? String ?: continue
                    val c = s.replace("watermark=1", "watermark=0")
                    if (c != s) changed = true
                    cleaned.add(c)
                }
                // The downloader uses the FIRST URL. douyinvod CDN direct links have
                // the watermark baked into the mp4 (no URL param to strip) — only
                // api-play/aweme/v1/play/ URLs honor watermark=0.
                val apiPlay = cleaned.filter { it.toString().contains("aweme/v1/play") }
                val others = cleaned.filter { !it.toString().contains("aweme/v1/play") }
                val cdnDirect = others.filter { it.toString().contains("douyinvod.com") }
                if (apiPlay.isNotEmpty() && cdnDirect.isNotEmpty()) {
                    val firstClean = apiPlay.first()
                    val reordered = java.util.ArrayList<Any>(cleaned.size)
                    for (url in cleaned) {
                        if (url.toString().contains("douyinvod.com") && !url.toString().contains("aweme/v1/play")) {
                            reordered.add(firstClean)
                        } else {
                            reordered.add(url)
                        }
                    }
                    result = reordered
                    YLog.info("$TAG: getUrlList() replaced ${cdnDirect.size} cdn-direct -> api-play (clean)")
                } else if (apiPlay.isNotEmpty() && others.isNotEmpty() && cleaned.isNotEmpty() && cleaned[0] !in apiPlay) {
                    val reordered = java.util.ArrayList<Any>(apiPlay.size + others.size)
                    reordered.addAll(apiPlay)
                    reordered.addAll(others)
                    result = reordered
                    YLog.info("$TAG: getUrlList() reordered: api-play first (${apiPlay.size} clean + ${others.size} cdn)")
                } else if (changed) {
                    result = cleaned
                    YLog.info("$TAG: getUrlList() cleaned ${list.size} URLs (watermark=1 -> 0)")
                }
            }
        }
    }
}
