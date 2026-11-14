package io.github.twyora.douyinenhancer.ui.content.settings

data class SettingsContentSaveConfig(
    val purifyCommentImage: Boolean,
    val unlockCommentEmoji: Boolean,
    val downloadCommentAudio: Boolean,
    val removeVideoWatermark: Boolean,
    val bypassVideoDownload: Boolean,
    val removeMultiImageWatermark: Boolean
)

data class SettingsContentFeedConfig(
    val disableDoubleTapDigg: Boolean,
    val doubleTapOpenComment: Boolean,
    val bypassListenAweme: Boolean,
    val blockAutoReplay: Boolean,
    val blockAutoResumePlayback: Boolean
)

data class SettingsContentUiConfig(val keepDanmakuVisible: Boolean, val hideFollowButton: Boolean, val keepCleanMode: Boolean)

data class SettingsContentModuleConfig(val hiddenFeature: Boolean, val disableVerbose: Boolean)

data class SettingsContentConfig(
    val saveState: SettingsContentSaveConfig,
    val feedState: SettingsContentFeedConfig,
    val uiState: SettingsContentUiConfig,
    val moduleState: SettingsContentModuleConfig
)
