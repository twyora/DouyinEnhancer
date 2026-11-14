package io.github.twyora.douyinenhancer.ui.content.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.PreferenceCategory
import io.github.twyora.douyinenhancer.ui.components.PreferenceItem
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun SettingsContent(state: SettingsContentConfig, actions: SettingsContentActions, modifier: Modifier = Modifier) {
    LazyColumn(modifier) {
        item {
            SettingsContentSaveSection(state.saveState, actions.saveActions)
        }
        item {
            SettingsContentFeedSection(state.feedState, actions.feedActions)
        }
        item {
            SettingsContentUiSection(state.uiState, actions.uiActions)
        }
        item {
            SettingsContentConfigSection(actions.configActions)
        }
        item {
            SettingsContentModuleSection(state.moduleState, actions.moduleActions)
        }
    }
}

@Composable
fun SettingsContentSaveSection(
    saveState: SettingsContentSaveConfig,
    saveActions: SettingsContentSaveActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.pref_category_save))

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_comment_image_title),
            summary = stringResource(R.string.pref_comment_image_summary),
            checked = saveState.purifyCommentImage
        ) {
            saveActions.onPurifyCommentImageChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_comment_emoji_title),
            summary = stringResource(R.string.pref_comment_emoji_summary),
            checked = saveState.unlockCommentEmoji
        ) {
            saveActions.onUnlockCommentEmojiChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_comment_audio_title),
            summary = stringResource(R.string.pref_comment_audio_summary),
            checked = saveState.downloadCommentAudio
        ) {
            saveActions.onDownloadCommentAudioChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_video_remove_watermark_title),
            summary = stringResource(R.string.pref_feed_video_remove_watermark_summary),
            checked = saveState.removeVideoWatermark
        ) {
            saveActions.onRemoveVideoWatermarkChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_download_bypass_title),
            summary = stringResource(R.string.pref_feed_download_bypass_summary),
            checked = saveState.bypassVideoDownload
        ) {
            saveActions.onBypassVideoDownloadChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_multi_image_remove_watermark_title),
            summary = stringResource(R.string.pref_feed_multi_image_remove_watermark_summary),
            checked = saveState.removeMultiImageWatermark
        ) {
            saveActions.onRemoveMultiImageWatermarkChange(it)
        }
    }
}

@Composable
fun SettingsContentFeedSection(
    feedState: SettingsContentFeedConfig,
    feedActions: SettingsContentFeedActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.pref_category_feed))

        PreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_dialog_title)
        ) {
            feedActions.onNavigateToRecommendedFeedFilter()
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_disable_double_tap_digg_title),
            summary = stringResource(R.string.pref_feed_disable_double_tap_digg_summary),
            checked = feedState.disableDoubleTapDigg
        ) {
            feedActions.onDisableDoubleTapDiggChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_double_tap_open_comment_title),
            summary = stringResource(R.string.pref_feed_double_tap_open_comment_summary),
            checked = feedState.doubleTapOpenComment
        ) {
            feedActions.onDoubleTabOpenCommentChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_bypass_listen_aweme_restriction_title),
            summary = stringResource(R.string.pref_feed_bypass_listen_aweme_restriction_summary),
            checked = feedState.bypassListenAweme
        ) {
            feedActions.onBypassListenAwemeChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_block_auto_replay_title),
            summary = stringResource(R.string.pref_feed_block_auto_replay_summary),
            checked = feedState.blockAutoReplay
        ) {
            feedActions.onBlockAutoReplayChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_feed_block_resume_playback_title),
            summary = stringResource(R.string.pref_feed_block_resume_playback_summary),
            checked = feedState.blockAutoResumePlayback
        ) {
            feedActions.onBlockResumePlaybackChange(it)
        }
    }
}

@Composable
fun SettingsContentUiSection(uiState: SettingsContentUiConfig, uiActions: SettingsContentUiActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.pref_category_ui))

        PreferenceItem(
            title = stringResource(R.string.playback_component_block_dialog_title),
            summary = stringResource(R.string.playback_component_block_dialog_summary)
        ) {
            uiActions.onNavigateToPlaybackComponent()
        }

        PreferenceItem(
            title = stringResource(R.string.bottom_tab_block_dialog_title),
            summary = stringResource(R.string.home_tab_block_summary)
        ) {
            uiActions.onNavigateToHomeTab()
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_ui_keep_danmaku_visible_title),
            summary = stringResource(R.string.pref_ui_keep_danmaku_visible_summary),
            checked = uiState.keepDanmakuVisible
        ) {
            uiActions.onKeepDanmakuVisibleChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_ui_hide_follow_button_title),
            summary = stringResource(R.string.pref_ui_hide_follow_button_summary),
            checked = uiState.hideFollowButton
        ) {
            uiActions.onHideFollowButtonChange(it)
        }

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_ui_keep_clean_mode_title),
            summary = stringResource(R.string.pref_ui_keep_clean_mode_summary),
            checked = uiState.keepCleanMode
        ) {
            uiActions.onKeepCleanModeChange(it)
        }
    }
}

@Composable
fun SettingsContentConfigSection(configActions: SettingsContentConfigActions, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.pref_category_backup))

        PreferenceItem(
            title = stringResource(R.string.pref_backup_export_title)
        ) {
            configActions.onExportConfig()
        }

        PreferenceItem(
            title = stringResource(R.string.pref_backup_import_title)
        ) {
            configActions.onImportConfig()
        }
    }
}

@Composable
fun SettingsContentModuleSection(
    moduleState: SettingsContentModuleConfig,
    moduleActions: SettingsContentModuleActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (moduleState.hiddenFeature) {
            PreferenceCategory(stringResource(R.string.pref_category_misc))

            SwitchPreferenceItem(
                title = stringResource(R.string.pref_misc_enable_hidden_features_title),
                summary = stringResource(R.string.pref_misc_enable_hidden_features_summary),
                checked = true
            ) {
                moduleActions.onHiddenFeatureChange(it)
            }
        }

        PreferenceCategory(stringResource(R.string.pref_category_module))

        SwitchPreferenceItem(
            title = stringResource(R.string.pref_module_disable_verbose_logs_title),
            summary = stringResource(R.string.pref_module_disable_verbose_logs_summary),
            checked = moduleState.disableVerbose
        ) {
            moduleActions.onDisableVerboseChange(it)
        }

        PreferenceItem(
            title = stringResource(R.string.pref_module_invalid_hook_info_title),
            summary = stringResource(R.string.pref_module_invalid_hook_info_summary)
        ) {
            moduleActions.onResetMapping()
        }

        PreferenceItem(
            title = stringResource(R.string.pref_module_load_custom_hook_info_title),
            summary = stringResource(R.string.pref_module_load_custom_hook_info_summary)
        ) {
            moduleActions.onLoadCustomMapping()
        }

        PreferenceItem(
            title = stringResource(R.string.pref_module_reset_custom_hook_info_title),
            summary = stringResource(R.string.pref_module_reset_custom_hook_info_summary)
        ) {
            moduleActions.onClearCustomMapping()
        }
    }
}

@Preview
@Composable
fun SettingsContentPreview() {
    SettingsContent(
        state = SettingsContentConfig(
            SettingsContentSaveConfig(
                purifyCommentImage = false,
                unlockCommentEmoji = false,
                downloadCommentAudio = false,
                removeVideoWatermark = false,
                bypassVideoDownload = false,
                removeMultiImageWatermark = false
            ),
            SettingsContentFeedConfig(
                disableDoubleTapDigg = false,
                doubleTapOpenComment = false,
                bypassListenAweme = false,
                blockAutoReplay = false,
                blockAutoResumePlayback = false
            ),
            SettingsContentUiConfig(keepDanmakuVisible = false, hideFollowButton = false, keepCleanMode = false),
            SettingsContentModuleConfig(hiddenFeature = false, disableVerbose = false)
        ),
        actions = SettingsContentActions()
    )
}
