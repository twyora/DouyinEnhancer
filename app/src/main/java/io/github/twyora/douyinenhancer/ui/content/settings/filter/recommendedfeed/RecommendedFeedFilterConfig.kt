package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed

data class RecommendedFeedFilterSwitchConfig(
    val mainSwitch: Boolean,
    val blockFollowedAuthor: Boolean,
    val blockFollowedAuthorVisible: Boolean,
    val blockAd: Boolean,
    val blockAdVisible: Boolean,
    val blockEcom: Boolean,
    val blockEcomVisible: Boolean,
    val blockGrouponLargeCard: Boolean,
    val blockGrouponLargeCardVisible: Boolean,
    val blockLive: Boolean,
    val blockLiveVisible: Boolean,
    val blockMultiImage: Boolean,
    val blockMultiImageVisible: Boolean
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
    val shareCountMax: Int
)

data class RecommendedFeedFilterTitleKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterUidKeywordsConfig(val keywords: List<String>)

data class RecommendedFeedFilterNicknameKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterDescKeywordsConfig(val keywords: List<String>, val regexMode: Boolean)

data class RecommendedFeedFilterKeywordConfig(
    val title: RecommendedFeedFilterTitleKeywordsConfig,
    val uid: RecommendedFeedFilterUidKeywordsConfig,
    val nickname: RecommendedFeedFilterNicknameKeywordsConfig,
    val desc: RecommendedFeedFilterDescKeywordsConfig
)

data class RecommendedFeedFilterConfig(
    val switch: RecommendedFeedFilterSwitchConfig,
    val count: RecommendedFeedFilterCountConfig,
    val keyword: RecommendedFeedFilterKeywordConfig
)
