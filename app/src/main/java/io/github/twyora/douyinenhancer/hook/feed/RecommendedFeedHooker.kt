package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object RecommendedFeedHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.moduleConfig.verboseDisabled

    private val kwdFilterTitleRegexes by lazy {
        val titleList = ConfigManager.recommendedFeedFilterConfig.titleKeywords
        val regexMode = ConfigManager.recommendedFeedFilterConfig.titleRegexMode
        if (regexMode) {
            titleList.map {
                it.toRegex()
            }
        } else {
            titleList.map {
                Regex.escape(it).toRegex()
            }
        }
    }

    private val kwdFilterAuthorNicknameRegexes by lazy {
        val nicknameList = ConfigManager.recommendedFeedFilterConfig.authorNicknameKeywords
        val regexMode = ConfigManager.recommendedFeedFilterConfig.authorNicknameRegexMode
        if (regexMode) {
            nicknameList.map {
                it.toRegex()
            }
        } else {
            nicknameList.map {
                Regex.escape(it).toRegex()
            }
        }
    }

    private val kwdFilterDescRegexes by lazy {
        val descList = ConfigManager.recommendedFeedFilterConfig.descKeywords
        val regexMode = ConfigManager.recommendedFeedFilterConfig.descRegexMode
        if (regexMode) {
            descList.map {
                it.toRegex()
            }
        } else {
            descList.map {
                Regex.escape(it).toRegex()
            }
        }
    }

    override fun onHook() {
        if (!ConfigManager.recommendedFeedFilterConfig.mainSwitch) {
            if (verbose) {
                YLog.debug("$TAG: recommended feed filter master switch disabled, skip feed filter hook")
            }
            return
        }

        packageInstance.feedResponseHandler.selfClass?.resolveMethod(
            packageInstance.feedResponseHandler.processAwemeList()
        )?.hook {
            before {
                val awemeList = args[2] as? MutableList<*> ?: return@before

                val iter = awemeList.iterator()
                while (iter.hasNext()) {
                    val awemeObj = iter.next() ?: continue

                    if (ConfigManager.recommendedFeedFilterConfig.blockAd && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.getAd()) == true) {
                        if (verbose) {
                            YLog.debug("$TAG: filtered by ad")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilterConfig.blockEcom && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isEcomAweme()) == true) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by ecom aweme")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilterConfig.blockGrouponLargeCard && awemeObj.getField<Any?>(
                            packageInstance.aweme.grouponLargeCard()
                        ) != null
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by groupon large card")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilterConfig.blockLive && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isLive()) == true) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by live")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilterConfig.blockMultiImage &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isMultiImage()) == true
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by multi image")
                        }
                        iter.remove()
                        continue
                    } else if (run {
                            if (ConfigManager.recommendedFeedFilterConfig.shortDurationLimit > ConfigManager.recommendedFeedFilterConfig.longDurationLimit) {
                                return@run false
                            }

                            if (awemeObj.invokeMethod<Boolean?>(
                                    packageInstance.aweme.isNormalVideo()
                                ) == false
                            ) {
                                return@run false
                            }

                            val duration = awemeObj.getField<Int?>(
                                packageInstance.aweme.duration()
                            ) ?: return@run false

                            return@run duration != 0 && (duration !in ConfigManager.recommendedFeedFilterConfig.shortDurationLimit..ConfigManager.recommendedFeedFilterConfig.longDurationLimit)
                        }
                    ) {
                        if (verbose) {
                            YLog.debug("$TAG: filtered by duration")
                        }
                        iter.remove()
                        continue
                    } else if (shouldFilterByInteractionStats(awemeObj)) {
                        iter.remove()
                        continue
                    } else if (shouldFilterByKeyword(awemeObj)) {
                        iter.remove()
                        continue
                    }
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to filter recommended feed aweme", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for filtering recommended feed aweme", throwable)
            }
        }
    }

    private fun shouldFilterByInteractionStats(aweme: Any): Boolean {
        val statsMinLEMax =
            ConfigManager.recommendedFeedFilterConfig.collectCountMin <= ConfigManager.recommendedFeedFilterConfig.collectCountMax ||
                    ConfigManager.recommendedFeedFilterConfig.commentCountMin <= ConfigManager.recommendedFeedFilterConfig.commentCountMax ||
                    ConfigManager.recommendedFeedFilterConfig.diggCountMin <= ConfigManager.recommendedFeedFilterConfig.diggCountMax ||
                    ConfigManager.recommendedFeedFilterConfig.shareCountMin <= ConfigManager.recommendedFeedFilterConfig.shareCountMax
        if (!statsMinLEMax) {
            return false
        }

        val statsObj = aweme.getField<Any?>(packageInstance.aweme.statistics())
            ?: return false

        if (ConfigManager.recommendedFeedFilterConfig.collectCountMin <= ConfigManager.recommendedFeedFilterConfig.collectCountMax) {
            val collectCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.collectCount())
            if (collectCount != null && (collectCount !in ConfigManager.recommendedFeedFilterConfig.collectCountMin..ConfigManager.recommendedFeedFilterConfig.collectCountMax)) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by collect count: $collectCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilterConfig.commentCountMin <= ConfigManager.recommendedFeedFilterConfig.commentCountMax) {
            val commentCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.commentCount())
            if (commentCount != null && (commentCount !in ConfigManager.recommendedFeedFilterConfig.commentCountMin..ConfigManager.recommendedFeedFilterConfig.commentCountMax)) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by comment count: $commentCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilterConfig.diggCountMin <= ConfigManager.recommendedFeedFilterConfig.diggCountMax) {
            val diggCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.diggCount())
            if (diggCount != null && (diggCount !in ConfigManager.recommendedFeedFilterConfig.diggCountMin..ConfigManager.recommendedFeedFilterConfig.diggCountMax)) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by digg count: $diggCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilterConfig.shareCountMin <= ConfigManager.recommendedFeedFilterConfig.shareCountMax) {
            val shareCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.shareCount())
            if (shareCount != null && (shareCount !in ConfigManager.recommendedFeedFilterConfig.shareCountMin..ConfigManager.recommendedFeedFilterConfig.shareCountMax)) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by share count: $shareCount")
                }
                return true
            }
        }

        return false
    }

    private fun shouldFilterByKeyword(aweme: Any): Boolean {
        val titleRegexes = kwdFilterTitleRegexes
        if (titleRegexes.isNotEmpty()) {
            val title = aweme.getField<String?>(
                packageInstance.aweme.itemTitle()
            )
            if (!title.isNullOrBlank() && titleRegexes.any {
                    title.contains(it)
                }
            ) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by title: $title")
                }
                return true
            }
        }

        val uidFilters = ConfigManager.recommendedFeedFilterConfig.authorUidKeywords
        if (uidFilters.isNotEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val uid = authorObj.getField<String?>(packageInstance.user.uid())
                if (uid != null && uid in uidFilters) {
                    if (verbose) {
                        YLog.debug("$TAG: filtered by author uid: $uid")
                    }
                    return true
                }
            }
        }

        val nicknameRegexes = kwdFilterAuthorNicknameRegexes
        if (nicknameRegexes.isNotEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val nickname = authorObj.getField<String?>(packageInstance.user.nickname())
                if (!nickname.isNullOrBlank() && nicknameRegexes.any {
                        nickname.contains(it)
                    }
                ) {
                    if (verbose) {
                        YLog.debug("$TAG: filtered by author nickname: $nickname")
                    }
                    return true
                }
            }
        }

        val descRegexes = kwdFilterDescRegexes
        if (descRegexes.isNotEmpty()) {
            val desc = aweme.getField<String?>(packageInstance.aweme.desc())
            if (!desc.isNullOrBlank() && descRegexes.any {
                    desc.contains(it)
                }
            ) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by desc: $desc")
                }
                return true
            }
        }

        return false
    }
}
