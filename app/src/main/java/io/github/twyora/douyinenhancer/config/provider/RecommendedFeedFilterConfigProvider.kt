package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.gate.ConfigValueMode
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.gate.RuleGate
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.HiddenFeatureEnabledRule
import io.github.twyora.douyinenhancer.config.rule.IRule

class RecommendedFeedFilterConfigProvider(
    kvConfig: IKVStorage,
    ruleContextProvider: () -> IRule.Context = {
        IRule.Context(hiddenFeatureEnabled = true)
    }
) : AbsConfigProvider(kvConfig, ruleContextProvider) {
    var mainSwitch = configItem(MAIN_SWITCH, false)

    var blockAd = configItem(BLOCK_AD, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var blockEcom =
        configItem(BLOCK_ECOM, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var blockGrouponLargeCard =
        configItem(BLOCK_GROUPON, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var blockLive =
        configItem(BLOCK_LIVE, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var blockMultiImage =
        configItem(BLOCK_MULTI_IMAGE, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))

    var shortDurationLimit = configItem(SHORT_DURATION_LIMIT, 0)
    var longDurationLimit = configItem(LONG_DURATION_LIMIT, Int.MAX_VALUE)
    var collectCountMin = configItem(COLLECT_COUNT_MIN, 0)
    var collectCountMax = configItem(COLLECT_COUNT_MAX, Int.MAX_VALUE)
    var commentCountMin = configItem(COMMENT_COUNT_MIN, 0)
    var commentCountMax = configItem(COMMENT_COUNT_MAX, Int.MAX_VALUE)
    var diggCountMin = configItem(DIGG_COUNT_MIN, 0)
    var diggCountMax = configItem(DIGG_COUNT_MAX, Int.MAX_VALUE)
    var shareCountMin = configItem(SHARE_COUNT_MIN, 0)
    var shareCountMax = configItem(SHARE_COUNT_MAX, Int.MAX_VALUE)

    var titleRegexMode = configItem(TITLE_REGEX_MODE, false)
    var titleKeywords = configItem(TITLE_KEYWORDS, emptySet<String>())
    var authorUidKeywords = configItem(AUTHOR_UID_KEYWORDS, emptySet<String>())
    var authorNicknameRegexMode = configItem(AUTHOR_NICKNAME_REGEX_MODE, false)
    var authorNicknameKeywords = configItem(AUTHOR_NICKNAME_KEYWORDS, emptySet<String>())
    var descRegexMode = configItem(DESC_REGEX_MODE, false)
    var descKeywords = configItem(DESC_KEYWORDS, emptySet<String>())

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
