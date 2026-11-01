package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed


data class RecommendedFeedFilterSwitchConfig(
    val mainSwitch: Boolean,
    val blockFollowedAuthor: Boolean,
    val blockAd: Boolean,
    val blockEcom: Boolean,
    val blockGrouponLargeCard: Boolean,
    val blockLive: Boolean,
    val blockMultiImage: Boolean,
)

data class RecommendedFeedFilterCountConfig(
    val shortDuration: Int,
    val longDuration: Int,
    val collectCountMin: Int,
    val collectCountMax: Int,
    val commentCountMin: Int,
    val commentCountMax: Int,
    val diggCountMin: Int,
    val diggCountMax: Int,
    val shareCountMin: Int,
    val shareCountMax: Int,
)

data class RecommendedFeedFilterTitleKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterUidKeywordsConfig(val keywords: List<String>)

data class RecommendedFeedFilterNicknameKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterDescKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterKeywordConfig(
    val title: RecommendedFeedFilterTitleKeywordsConfig,
    val uid: RecommendedFeedFilterUidKeywordsConfig,
    val nickname: RecommendedFeedFilterNicknameKeywordsConfig,
    val desc: RecommendedFeedFilterDescKeywordsConfig,
)

data class RecommendedFeedFilterConfig(
    val switch: RecommendedFeedFilterSwitchConfig,
    val count: RecommendedFeedFilterCountConfig,
    val keyword: RecommendedFeedFilterKeywordConfig,
)
