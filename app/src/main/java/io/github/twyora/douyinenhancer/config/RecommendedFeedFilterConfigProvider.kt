package io.github.twyora.douyinenhancer.config

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import kotlin.collections.setOf
import kotlin.to

class RecommendedFeedFilterConfigProvider(
    kvConfig: IKVStorage,
    gates: Map<FeatureGate, () -> Boolean> = emptyMap()
) : AbsConfigProvider(kvConfig, gates) {
    override val gatedKeys = mapOf(
        FeatureGate.HIDDEN to setOf(
            BLOCK_AD, BLOCK_ECOM, BLOCK_GROUPON, BLOCK_LIVE, BLOCK_MULTI_IMAGE
        )
    )

    var mainSwitch by property(MAIN_SWITCH, false)

    var blockAd by property(BLOCK_AD, false)
    var blockEcom by property(BLOCK_ECOM, false)
    var blockGrouponLargeCard by property(BLOCK_GROUPON, false)
    var blockLive by property(BLOCK_LIVE, false)
    var blockMultiImage by property(BLOCK_MULTI_IMAGE, false)

    var shortDurationLimit by property(SHORT_DURATION_LIMIT, 0)
    var longDurationLimit by property(LONG_DURATION_LIMIT, Int.MAX_VALUE)
    var collectCountMin by property(COLLECT_COUNT_MIN, 0)
    var collectCountMax by property(COLLECT_COUNT_MAX, Int.MAX_VALUE)
    var commentCountMin by property(COMMENT_COUNT_MIN, 0)
    var commentCountMax by property(COMMENT_COUNT_MAX, Int.MAX_VALUE)
    var diggCountMin by property(DIGG_COUNT_MIN, 0)
    var diggCountMax by property(DIGG_COUNT_MAX, Int.MAX_VALUE)
    var shareCountMin by property(SHARE_COUNT_MIN, 0)
    var shareCountMax by property(SHARE_COUNT_MAX, Int.MAX_VALUE)

    var titleRegexMode by property(TITLE_REGEX_MODE, false)
    var titleKeywords by property(TITLE_KEYWORDS, emptySet<String>())
    var authorUidKeywords by property(AUTHOR_UID_KEYWORDS, emptySet<String>())
    var authorNicknameRegexMode by property(AUTHOR_NICKNAME_REGEX_MODE, false)
    var authorNicknameKeywords by property(AUTHOR_NICKNAME_KEYWORDS, emptySet<String>())
    var descRegexMode by property(DESC_REGEX_MODE, false)
    var descKeywords by property(DESC_KEYWORDS, emptySet<String>())

    companion object {
        const val MAIN_SWITCH = "recommended_feed_filter_main_switch"
        const val BLOCK_AD = "recommended_feed_filter_block_ad"
        const val BLOCK_ECOM = "recommended_feed_filter_block_ecom_aweme"
        const val BLOCK_GROUPON = "recommended_feed_filter_block_groupon_large_card"
        const val BLOCK_LIVE = "recommended_feed_filter_block_live"
        const val BLOCK_MULTI_IMAGE = "recommended_feed_filter_block_multi_image"
        const val SHORT_DURATION_LIMIT = "recommended_feed_filter_hide_short_duration_limit"
        const val LONG_DURATION_LIMIT = "recommended_feed_filter_hide_long_duration_limit"
        const val COLLECT_COUNT_MIN = "recommended_feed_filter_collect_count_min"
        const val COLLECT_COUNT_MAX = "recommended_feed_filter_collect_count_max"
        const val COMMENT_COUNT_MIN = "recommended_feed_filter_comment_count_min"
        const val COMMENT_COUNT_MAX = "recommended_feed_filter_comment_count_max"
        const val DIGG_COUNT_MIN = "recommended_feed_filter_digg_count_min"
        const val DIGG_COUNT_MAX = "recommended_feed_filter_digg_count_max"
        const val SHARE_COUNT_MIN = "recommended_feed_filter_share_count_min"
        const val SHARE_COUNT_MAX = "recommended_feed_filter_share_count_max"
        const val TITLE_REGEX_MODE = "recommended_feed_filter_title_regex_mode"
        const val TITLE_KEYWORDS = "recommended_feed_filter_group_aweme_title"
        const val AUTHOR_UID_KEYWORDS = "recommended_feed_filter_group_author_uid"
        const val AUTHOR_NICKNAME_REGEX_MODE = "recommended_feed_filter_author_nickname_regex_mode"
        const val AUTHOR_NICKNAME_KEYWORDS = "recommended_feed_filter_group_author_nickname"
        const val DESC_REGEX_MODE = "recommended_feed_filter_desc_regex_mode"
        const val DESC_KEYWORDS = "recommended_feed_filter_group_aweme_desc"
    }
}