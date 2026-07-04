package io.github.twyora.douyinenhancer.hook

import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.hook.utils.getField
import io.github.twyora.douyinenhancer.hook.utils.invokeMethod
import io.github.twyora.douyinenhancer.hook.utils.resolveMethod

object FeedHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val blockAdEnabled by lazy {
        FastKVConfigManager.settings.getBoolean("recommended_feed_filter_block_ad", false)
    }

    private val blockEcomEnabled by lazy {
        FastKVConfigManager.settings.getBoolean("recommended_feed_filter_block_ecom_aweme", false)
    }

    private val blockGrouponLargeCardEnabled by lazy {
        FastKVConfigManager.settings.getBoolean("recommended_feed_filter_block_groupon_large_card", false)
    }

    private val hideShortDurationLimit by lazy {
        FastKVConfigManager.settings.getInt("recommended_feed_filter_hide_short_duration_limit", 0)
    }
    private val hideLongDurationLimit by lazy {
        FastKVConfigManager.settings.getInt("recommended_feed_filter_hide_long_duration_limit", Int.MAX_VALUE)
    }

    private val kwdFilterTitleRegexMode by lazy {
        FastKVConfigManager.settings.getBoolean("recommended_feed_filter_title_regex_mode", false)
    }
    private val kwdFilterTitleRegexes by lazy {
        val titleList = FastKVConfigManager.settings.getStringSet("recommended_feed_filter_group_aweme_title", null)
        if (kwdFilterTitleRegexMode) {
            titleList?.map {
                it.toRegex()
            }
        } else {
            titleList?.map {
                Regex.escape(it).toRegex()
            }
        }
    }

    private val kwdFilterAuthorUid by lazy {
        FastKVConfigManager.settings.getStringSet("recommended_feed_filter_group_author_uid", null)
    }

    private val kwdFilterAuthorNicknames by lazy {
        FastKVConfigManager.settings.getStringSet("recommended_feed_filter_group_author_nickname", null)
    }

    private val kwdFilterDescRegexMode by lazy {
        FastKVConfigManager.settings.getBoolean("recommended_feed_filter_desc_regex_mode", false)
    }
    private val kwdFilterDescRegexes by lazy {
        val descList = FastKVConfigManager.settings.getStringSet("recommended_feed_filter_group_aweme_desc", null)
        if (kwdFilterDescRegexMode) {
            descList?.map { it.toRegex() }
        } else {
            descList?.map { Regex.escape(it).toRegex() }
        }
    }

    override fun onHook() {
        val packageInstance = DouyinPackage.instance

        withProcess(mainProcessName) {
            packageInstance.feedResponseHandler.selfClass?.resolveMethod(
                packageInstance.feedResponseHandler.processAwemeList()
            )?.hook {
                before {
                    val awemeList = args[2] as? MutableList<*> ?: return@before

                    val iter = awemeList.iterator()
                    while (iter.hasNext()) {
                        val awemeObj = iter.next() ?: continue

                        if (blockAdEnabled && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.getAd()) == true) {
                            YLog.debug("$TAG: filtered by ad")
                            iter.remove()
                            continue
                        } else if (blockEcomEnabled && awemeObj.invokeMethod<Boolean?>(packageInstance.aweme.isEcomAweme()) == true) {
                            // NOTE: this filter logic has not been rigorously verified
                            YLog.debug("$TAG: filtered by ecom aweme")
                            iter.remove()
                            continue
                        } else if (blockGrouponLargeCardEnabled && awemeObj.getField<Any?>(
                                packageInstance.aweme.grouponLargeCard()
                            ) != null
                        ) {
                            // NOTE: this filter logic has not been rigorously verified
                            YLog.debug("$TAG: filtered by groupon large card")
                            iter.remove()
                            continue
                        } else if (run {
                                if (hideShortDurationLimit > hideLongDurationLimit) {
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

                                return@run duration != 0 && (duration !in hideShortDurationLimit..hideLongDurationLimit)
                            }
                        ) {
                            YLog.debug("$TAG: filtered by duration")
                            iter.remove()
                            continue
                        } else if (isContainsBlockKwd(awemeObj)) {
                            iter.remove()
                            continue
                        }
                    }
                }
            }?.result {
                onConductFailure { param, throwable ->
                    YLog.error("$TAG: Feed hook runtime error", throwable)
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: Failed to hook feed method", throwable)
                }
                onHooked {
                    YLog.info("$TAG: Feed hook installed")
                }
            }
        }
    }

    private fun isContainsBlockKwd(aweme: Any): Boolean {
        val packageInstance = DouyinPackage.instance

        val titleRegexes = kwdFilterTitleRegexes
        if (!titleRegexes.isNullOrEmpty()) {
            val title = aweme.getField<String?>(
                packageInstance.aweme.itemTitle()
            )
            if (!title.isNullOrBlank() && titleRegexes.any {
                    title.contains(it)
                }
            ) {
                YLog.debug("$TAG: filtered by title: $title")
                return true
            }
        }

        val uidFilters = kwdFilterAuthorUid
        if (!uidFilters.isNullOrEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val uid = authorObj.getField<String?>(packageInstance.user.uid())
                if (uid != null && uid in uidFilters) {
                    YLog.debug("$TAG: filtered by author uid: $uid")
                    return true
                }
            }
        }

        val nicknameFilters = kwdFilterAuthorNicknames
        if (!nicknameFilters.isNullOrEmpty()) {
            val authorObj = aweme.getField<Any?>(packageInstance.aweme.author())
            if (authorObj != null) {
                val nickname = authorObj.getField<String?>(packageInstance.user.nickname())
                if (!nickname.isNullOrBlank() && nickname in nicknameFilters) {
                    YLog.debug("$TAG: filtered by author nickname: $nickname")
                    return true
                }
            }
        }

        val descRegexes = kwdFilterDescRegexes
        if (!descRegexes.isNullOrEmpty()) {
            val desc = aweme.getField<String?>(packageInstance.aweme.desc())
            if (!desc.isNullOrBlank() && descRegexes.any {
                    desc.contains(it)
                }
            ) {
                YLog.debug("$TAG: filtered by desc: $desc")
                return true
            }
        }

        return false
    }
}
