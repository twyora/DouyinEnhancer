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
        get() = !ConfigManager.module.verboseDisabled.value

    private val kwdFilterTitleRegexes by lazy {
        val titleList = ConfigManager.recommendedFeedFilter.titleKeywords.value
        val regexMode = ConfigManager.recommendedFeedFilter.titleRegexMode.value
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
        val nicknameList = ConfigManager.recommendedFeedFilter.authorNicknameKeywords.value
        val regexMode = ConfigManager.recommendedFeedFilter.authorNicknameRegexMode.value
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
        val descList = ConfigManager.recommendedFeedFilter.descKeywords.value
        val regexMode = ConfigManager.recommendedFeedFilter.descRegexMode.value
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
        if (!ConfigManager.recommendedFeedFilter.mainSwitch.value) {
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

                    if (ConfigManager.recommendedFeedFilter.blockAd.value &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.getAd()) == true
                    ) {
                        if (verbose) {
                            YLog.debug("$TAG: filtered by ad")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilter.blockEcom.value &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isEcomAweme()) == true
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by ecom aweme")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilter.blockGrouponLargeCard.value && awemeObj.getField<Any?>(
                            packageInstance.aweme.grouponLargeCard()
                        ) != null
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by groupon large card")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilter.blockLive.value &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isLive()) == true
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by live")
                        }
                        iter.remove()
                        continue
                    } else if (ConfigManager.recommendedFeedFilter.blockMultiImage.value &&
                        awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isMultiImage()) == true
                    ) {
                        // NOTE: this filter logic has not been rigorously verified
                        if (verbose) {
                            YLog.debug("$TAG: filtered by multi image")
                        }
                        iter.remove()
                        continue
                    } else if (run {
                            if (ConfigManager.recommendedFeedFilter.shortDurationLimit.value
                                > ConfigManager.recommendedFeedFilter.longDurationLimit.value
                            ) {
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

                            return@run duration != 0 && with(ConfigManager.recommendedFeedFilter) {
                                duration !in shortDurationLimit.value..longDurationLimit.value
                            }
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
            ConfigManager.recommendedFeedFilter.collectCountMin.value <= ConfigManager.recommendedFeedFilter.collectCountMax.value ||
                ConfigManager.recommendedFeedFilter.commentCountMin.value <= ConfigManager.recommendedFeedFilter.commentCountMax.value ||
                ConfigManager.recommendedFeedFilter.diggCountMin.value <= ConfigManager.recommendedFeedFilter.diggCountMax.value ||
                ConfigManager.recommendedFeedFilter.shareCountMin.value <= ConfigManager.recommendedFeedFilter.shareCountMax.value
        if (!statsMinLEMax) {
            return false
        }

        val statsObj = aweme.getField<Any?>(packageInstance.aweme.statistics())
            ?: return false

        if (ConfigManager.recommendedFeedFilter.collectCountMin.value <= ConfigManager.recommendedFeedFilter.collectCountMax.value) {
            val collectCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.collectCount())
            if (collectCount != null &&
                (
                    collectCount !in
                        ConfigManager.recommendedFeedFilter.collectCountMin.value..ConfigManager.recommendedFeedFilter.collectCountMax.value
                    )
            ) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by collect count: $collectCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilter.commentCountMin.value <= ConfigManager.recommendedFeedFilter.commentCountMax.value) {
            val commentCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.commentCount())
            if (commentCount != null &&
                (
                    commentCount !in
                        ConfigManager.recommendedFeedFilter.commentCountMin.value..ConfigManager.recommendedFeedFilter.commentCountMax.value
                    )
            ) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by comment count: $commentCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilter.diggCountMin.value <= ConfigManager.recommendedFeedFilter.diggCountMax.value) {
            val diggCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.diggCount())
            if (diggCount != null &&
                (
                    diggCount !in
                        ConfigManager.recommendedFeedFilter.diggCountMin.value..ConfigManager.recommendedFeedFilter.diggCountMax.value
                    )
            ) {
                if (verbose) {
                    YLog.debug("$TAG: filtered by digg count: $diggCount")
                }
                return true
            }
        }

        if (ConfigManager.recommendedFeedFilter.shareCountMin.value <= ConfigManager.recommendedFeedFilter.shareCountMax.value) {
            val shareCount = statsObj.getField<Long?>(packageInstance.awemeStatistics.shareCount())
            if (shareCount != null &&
                (
                    shareCount !in
                        ConfigManager.recommendedFeedFilter.shareCountMin.value..ConfigManager.recommendedFeedFilter.shareCountMax.value
                    )
            ) {
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

        val uidFilters = ConfigManager.recommendedFeedFilter.authorUidKeywords.value
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
