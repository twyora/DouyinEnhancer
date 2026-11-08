package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed

import androidx.compose.runtime.Stable

@Stable
class RecommendedFeedFilterTitleKeywordActions(
    val onAddKeyword: () -> Unit,
    val onKeywordTextChange: (index: Int, text: String) -> Unit,
    val onRemoveKeyword: (index: Int) -> Unit,
    val onSave: () -> Boolean,
    val onRegexChange: (Boolean) -> Unit
)

@Stable
class RecommendedFeedFilterUidKeywordActions(
    val onAddKeyword: () -> Unit,
    val onKeywordTextChange: (index: Int, text: String) -> Unit,
    val onRemoveKeyword: (index: Int) -> Unit,
    val onSave: () -> Boolean
)

@Stable
class RecommendedFeedFilterNicknameKeywordActions(
    val onAddKeyword: () -> Unit,
    val onKeywordTextChange: (index: Int, text: String) -> Unit,
    val onRemoveKeyword: (index: Int) -> Unit,
    val onSave: () -> Boolean,
    val onRegexChange: (Boolean) -> Unit
)

@Stable
class RecommendedFeedFilterDescKeywordActions(
    val onAddKeyword: () -> Unit,
    val onKeywordTextChange: (index: Int, text: String) -> Unit,
    val onRemoveKeyword: (index: Int) -> Unit,
    val onSave: () -> Boolean,
    val onRegexChange: (Boolean) -> Unit
)

@Stable
class RecommendedFeedFilterSwitchActions(
    val onMainSwitchChange: (Boolean) -> Unit,
    val onBlockFollowedAuthorChange: (Boolean) -> Unit,
    val onBlockAdChange: (Boolean) -> Unit,
    val onBlockEcomChange: (Boolean) -> Unit,
    val onBlockGrouponChange: (Boolean) -> Unit,
    val onBlockLiveChange: (Boolean) -> Unit,
    val onBlockMultiImageChange: (Boolean) -> Unit
)

@Stable
class RecommendedFeedFilterCountActions(
    val onShortDurationChange: (Int?) -> Unit,
    val onLongDurationChange: (Int?) -> Unit,
    val onCollectCountMinChange: (Int?) -> Unit,
    val onCollectCountMaxChange: (Int?) -> Unit,
    val onCommentCountMinChange: (Int?) -> Unit,
    val onCommentCountMaxChange: (Int?) -> Unit,
    val onDiggCountMinChange: (Int?) -> Unit,
    val onDiggCountMaxChange: (Int?) -> Unit,
    val onShareCountMinChange: (Int?) -> Unit,
    val onShareCountMaxChange: (Int?) -> Unit
)

@Stable
class RecommendedFeedFilterKeywordActions(
    val title: RecommendedFeedFilterTitleKeywordActions,
    val uid: RecommendedFeedFilterUidKeywordActions,
    val nickname: RecommendedFeedFilterNicknameKeywordActions,
    val desc: RecommendedFeedFilterDescKeywordActions
)

@Stable
class RecommendedFeedFilterActions(
    val switch: RecommendedFeedFilterSwitchActions,
    val count: RecommendedFeedFilterCountActions,
    val keyword: RecommendedFeedFilterKeywordActions
)
