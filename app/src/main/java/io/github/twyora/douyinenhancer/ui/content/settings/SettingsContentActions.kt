package io.github.twyora.douyinenhancer.ui.content.settings

import androidx.compose.runtime.Stable

@Stable
class SettingsContentSaveActions(
    val onPurifyCommentImageChange: (Boolean) -> Unit = {},
    val onUnlockCommentEmojiChange: (Boolean) -> Unit = {},
    val onDownloadCommentAudioChange: (Boolean) -> Unit = {},
    val onRemoveVideoWatermarkChange: (Boolean) -> Unit = {},
    val onBypassVideoDownloadChange: (Boolean) -> Unit = {},
    val onRemoveMultiImageWatermarkChange: (Boolean) -> Unit = {}
)

@Stable
class SettingsContentFeedActions(
    val onNavigateToRecommendedFeedFilter: () -> Unit = {},
    val onDisableDoubleTapDiggChange: (Boolean) -> Unit = {},
    val onDoubleTabOpenCommentChange: (Boolean) -> Unit = {},
    val onBypassListenAwemeChange: (Boolean) -> Unit = {},
    val onBlockAutoReplayChange: (Boolean) -> Unit = {},
    val onBlockResumePlaybackChange: (Boolean) -> Unit = {}
)

@Stable
class SettingsContentUiActions(
    val onNavigateToPlaybackComponent: () -> Unit = {},
    val onNavigateToHomeTab: () -> Unit = {},
    val onKeepDanmakuVisibleChange: (Boolean) -> Unit = {},
    val onHideFollowButtonChange: (Boolean) -> Unit = {},
    val onKeepCleanModeChange: (Boolean) -> Unit = {}
)

@Stable
class SettingsContentConfigActions(val onExportConfig: () -> Unit = {}, val onImportConfig: () -> Unit = {})

@Stable
class SettingsContentModuleActions(
    val onHiddenFeatureChange: (Boolean) -> Unit = {},
    val onDisableVerboseChange: (Boolean) -> Unit = {},
    val onResetMapping: () -> Unit = {},
    val onLoadCustomMapping: () -> Unit = {},
    val onClearCustomMapping: () -> Unit = {}
)

@Stable
class SettingsContentActions(
    val saveActions: SettingsContentSaveActions = SettingsContentSaveActions(),
    val feedActions: SettingsContentFeedActions = SettingsContentFeedActions(),
    val uiActions: SettingsContentUiActions = SettingsContentUiActions(),
    val configActions: SettingsContentConfigActions = SettingsContentConfigActions(),
    val moduleActions: SettingsContentModuleActions = SettingsContentModuleActions()
)
