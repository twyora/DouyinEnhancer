package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.provider.ConfigItem
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlaybackComponentVisibilityViewModel : ViewModel() {
    private val catalog: List<Pair<ConfigItem<Boolean>, Int>> = with(ConfigManager.playbackComponentBlock) {
        listOf(
            avatar to R.string.playback_component_block_avatar,
            nickname to R.string.playback_component_block_nickname,
            coCreatorAuthor to R.string.playback_component_block_co_creator_author,
            digg to R.string.playback_component_block_digg,
            comment to R.string.playback_component_block_comment,
            share to R.string.playback_component_block_share,
            collect to R.string.playback_component_block_collect,
            rightMenuLl to R.string.playback_component_block_right_menu_ll,
            musicCover to R.string.playback_component_block_music_cover,
            title to R.string.playback_component_block_title,
            titleTagContainer to R.string.playback_component_block_title_tag_container,
            chapterTag to R.string.playback_component_block_chapter_tag,
            chapterDetail to R.string.playback_component_block_chapter_detail,
            musicTitle to R.string.playback_component_block_music_title,
            musicListenCover to R.string.playback_component_block_music_listen_cover,
            musicCoverBlock to R.string.playback_component_block_music_cover_block,
            musicMuteCover to R.string.playback_component_block_music_mute_cover,
            socialNewStyleMusicBelow to R.string.playback_component_block_social_new_style_music_below,
            postTime to R.string.playback_component_block_post_time,
            bellowDescTime to R.string.playback_component_block_bellow_desc_time,
            socialNewStylePostTimeBottom to R.string.playback_component_block_social_new_style_post_time_bottom,
            reply to R.string.playback_component_block_reply,
            buttonUnfollowFamiliar to R.string.playback_component_block_button_unfollow_familiar,
            danmakuVertical to R.string.playback_component_block_danmaku_vertical,
            aiSearch to R.string.playback_component_block_ai_search,
            nearbyIdentityTag to R.string.playback_component_block_nearby_identity_tag,
            buttonImQuickShare to R.string.playback_component_block_button_im_quick_share,
            buttonFeedImShareGuideV2 to R.string.playback_component_block_button_feed_im_share_guide_v2,
            buttonForceFeedImShareGuide to R.string.playback_component_block_button_force_feed_im_share_guide,
            bottomBarCommon to R.string.playback_component_block_bottom_bar_common,
            bottomBarMix to R.string.playback_component_block_bottom_bar_mix,
            bottomBarNormalSearch to R.string.playback_component_block_bottom_bar_normal_search,
            bottomBarCommonPrioritySearch to R.string.playback_component_block_bottom_bar_common_priority_search,
            bottomBarContainer to R.string.playback_component_block_bottom_bar_container,
            commonButton to R.string.playback_component_block_common_button,
            generalLabel to R.string.playback_component_block_general_label,
            feedLabelContainer to R.string.playback_component_block_feed_label_container,
            sticker to R.string.playback_component_block_sticker,
            anchorFramework to R.string.playback_component_block_anchor_framework,
            c2Feed to R.string.playback_component_block_c2_feed,
            livePhotoTag to R.string.playback_component_block_live_photo_tag,
            photosTag to R.string.playback_component_block_photos_tag,
            story24Tag to R.string.playback_component_block_story24_tag,
            story25DiverseDigg to R.string.playback_component_block_story_25_diverse_digg,
            socialNewStyleStoryTag to R.string.playback_component_block_social_new_style_story_tag,
            longVideoHighlightTag to R.string.playback_component_block_long_video_highlight_tag,
            commentBottomAnimation to R.string.playback_component_block_comment_bottom_animation,
            buttonUnfollowFamiliarRec to R.string.playback_component_block_button_unfollow_familiar_rec,
            nearbyHotComment to R.string.playback_component_block_nearby_hot_comment,
            ecomStore to R.string.playback_component_block_ecom_store,
            ecomTagFriend to R.string.playback_component_block_ecom_tag_friend,
            jxPick to R.string.playback_component_block_jx_pick,
            flow to R.string.playback_component_block_flow,
            jxLeftBottomLongVideoPlusTitleTag to R.string.playback_component_block_jx_left_bottom_long_video_plus_title_tag,
            socialNewCommentGuideBubble to R.string.playback_component_block_social_new_comment_guide_bubble,
            aiCoCreatorsThree to R.string.playback_component_block_ai_co_creators_three,
            aigcCocreateStatusTitle to R.string.playback_component_block_aigc_cocreate_status_title
        )
    }

    private val hiddenFeatureEnabled: Flow<Boolean> = ConfigManager.misc.hiddenFeatureEnabled.observe()

    // TODO: Interim solution — non-NORMAL items are dropped while hidden features are off, with
    // GRAYED intentionally unhandled for now (no configItem refactor intended). The gating layer was
    // over-engineered ahead of any real usage and will need its own rework; when that happens this site
    // must receive the full, correct implementation instead of this shortcut.
    val playbackComponentConfig: StateFlow<PlaybackComponentVisibilityConfig> = combine(
        ConfigManager.playbackComponentBlock.mainSwitch.observe(),
        combine(
            catalog.map { (item, _) ->
                item.observe()
            }
        ) { values ->
            values
        },
        hiddenFeatureEnabled
    ) { mainSwitch, checkedValues, _ ->
        // visibility is decided solely by ConfigItem.status. The hidden-feature flow
        // is only a recompute trigger; it isn't used in the decision.
        val switches = catalog.mapIndexedNotNull { index, (item, title) ->
            if (item.status == ConfigStateMode.NORMAL) {
                PlaybackComponentVisibilityItem(item.key, title, checkedValues[index])
            } else {
                null
            }
        }
        PlaybackComponentVisibilityConfig(mainSwitch, switches)
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = run {
            PlaybackComponentVisibilityConfig(
                mainSwitch = ConfigManager.playbackComponentBlock.mainSwitch.value,
                switches = catalog.mapNotNull { (item, title) ->
                    if (item.status == ConfigStateMode.NORMAL) {
                        PlaybackComponentVisibilityItem(item.key, title, item.value)
                    } else {
                        null
                    }
                }
            )
        }
    )

    fun onMainSwitchChange(checked: Boolean) {
        ConfigManager.playbackComponentBlock.mainSwitch.value = checked
    }

    fun onSwitchChange(key: String, checked: Boolean) {
        ConfigManager.playbackComponentBlock.kvConfig.put(key, checked)
    }
}
