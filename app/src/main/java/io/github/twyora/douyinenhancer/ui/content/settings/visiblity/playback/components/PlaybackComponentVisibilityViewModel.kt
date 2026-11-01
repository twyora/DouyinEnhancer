package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.provider.PlaybackComponentBlockConfigProvider
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class PlaybackComponentVisibilityViewModel : ViewModel() {
    private val catalog: List<Pair<String, Int>> = listOf(
        PlaybackComponentBlockConfigProvider.AVATAR to R.string.playback_component_block_avatar,
        PlaybackComponentBlockConfigProvider.NICKNAME to R.string.playback_component_block_nickname,
        PlaybackComponentBlockConfigProvider.CO_CREATOR_AUTHOR to R.string.playback_component_block_co_creator_author,
        PlaybackComponentBlockConfigProvider.DIGG to R.string.playback_component_block_digg,
        PlaybackComponentBlockConfigProvider.COMMENT to R.string.playback_component_block_comment,
        PlaybackComponentBlockConfigProvider.SHARE to R.string.playback_component_block_share,
        PlaybackComponentBlockConfigProvider.COLLECT to R.string.playback_component_block_collect,
        PlaybackComponentBlockConfigProvider.RIGHT_MENU_LL to R.string.playback_component_block_right_menu_ll,
        PlaybackComponentBlockConfigProvider.MUSIC_COVER to R.string.playback_component_block_music_cover,
        PlaybackComponentBlockConfigProvider.TITLE to R.string.playback_component_block_title,
        PlaybackComponentBlockConfigProvider.TITLE_TAG_CONTAINER to R.string.playback_component_block_title_tag_container,
        PlaybackComponentBlockConfigProvider.CHAPTER_TAG to R.string.playback_component_block_chapter_tag,
        PlaybackComponentBlockConfigProvider.CHAPTER_DETAIL to R.string.playback_component_block_chapter_detail,
        PlaybackComponentBlockConfigProvider.MUSIC_TITLE to R.string.playback_component_block_music_title,
        PlaybackComponentBlockConfigProvider.MUSIC_LISTEN_COVER to R.string.playback_component_block_music_listen_cover,
        PlaybackComponentBlockConfigProvider.MUSIC_COVER_BLOCK to R.string.playback_component_block_music_cover_block,
        PlaybackComponentBlockConfigProvider.MUSIC_MUTE_COVER to R.string.playback_component_block_music_mute_cover,
        PlaybackComponentBlockConfigProvider.SOCIAL_NEW_STYLE_MUSIC_BELOW to R.string.playback_component_block_social_new_style_music_below,
        PlaybackComponentBlockConfigProvider.POST_TIME to R.string.playback_component_block_post_time,
        PlaybackComponentBlockConfigProvider.BELLOW_DESC_TIME to R.string.playback_component_block_bellow_desc_time,
        PlaybackComponentBlockConfigProvider.SOCIAL_NEW_STYLE_POST_TIME_BOTTOM to R.string.playback_component_block_social_new_style_post_time_bottom,
        PlaybackComponentBlockConfigProvider.REPLY to R.string.playback_component_block_reply,
        PlaybackComponentBlockConfigProvider.BUTTON_UNFOLLOW_FAMILIAR to R.string.playback_component_block_button_unfollow_familiar,
        PlaybackComponentBlockConfigProvider.DANMAKU_VERTICAL to R.string.playback_component_block_danmaku_vertical,
        PlaybackComponentBlockConfigProvider.AI_SEARCH to R.string.playback_component_block_ai_search,
        PlaybackComponentBlockConfigProvider.NEARBY_IDENTITY_TAG to R.string.playback_component_block_nearby_identity_tag,
        PlaybackComponentBlockConfigProvider.BUTTON_IM_QUICK_SHARE to R.string.playback_component_block_button_im_quick_share,
        PlaybackComponentBlockConfigProvider.BUTTON_FEED_IM_SHARE_GUIDE_V2 to R.string.playback_component_block_button_feed_im_share_guide_v2,
        PlaybackComponentBlockConfigProvider.BUTTON_FORCE_FEED_IM_SHARE_GUIDE to R.string.playback_component_block_button_force_feed_im_share_guide,
        PlaybackComponentBlockConfigProvider.BOTTOM_BAR_COMMON to R.string.playback_component_block_bottom_bar_common,
        PlaybackComponentBlockConfigProvider.BOTTOM_BAR_MIX to R.string.playback_component_block_bottom_bar_mix,
        PlaybackComponentBlockConfigProvider.BOTTOM_BAR_NORMAL_SEARCH to R.string.playback_component_block_bottom_bar_normal_search,
        PlaybackComponentBlockConfigProvider.BOTTOM_BAR_COMMON_PRIORITY_SEARCH to R.string.playback_component_block_bottom_bar_common_priority_search,
        PlaybackComponentBlockConfigProvider.BOTTOM_BAR_CONTAINER to R.string.playback_component_block_bottom_bar_container,
        PlaybackComponentBlockConfigProvider.COMMON_BUTTON to R.string.playback_component_block_common_button,
        PlaybackComponentBlockConfigProvider.GENERAL_LABEL to R.string.playback_component_block_general_label,
        PlaybackComponentBlockConfigProvider.FEED_LABEL_CONTAINER to R.string.playback_component_block_feed_label_container,
        PlaybackComponentBlockConfigProvider.STICKER to R.string.playback_component_block_sticker,
        PlaybackComponentBlockConfigProvider.ANCHOR_FRAMEWORK to R.string.playback_component_block_anchor_framework,
        PlaybackComponentBlockConfigProvider.C2_FEED to R.string.playback_component_block_c2_feed,
        PlaybackComponentBlockConfigProvider.LIVE_PHOTO_TAG to R.string.playback_component_block_live_photo_tag,
        PlaybackComponentBlockConfigProvider.PHOTOS_TAG to R.string.playback_component_block_photos_tag,
        PlaybackComponentBlockConfigProvider.STORY24_TAG to R.string.playback_component_block_story24_tag,
        PlaybackComponentBlockConfigProvider.STORY_25_DIVERSE_DIGG to R.string.playback_component_block_story_25_diverse_digg,
        PlaybackComponentBlockConfigProvider.SOCIAL_NEW_STYLE_STORY_TAG to R.string.playback_component_block_social_new_style_story_tag,
        PlaybackComponentBlockConfigProvider.LONG_VIDEO_HIGHLIGHT_TAG to R.string.playback_component_block_long_video_highlight_tag,
        PlaybackComponentBlockConfigProvider.COMMENT_BOTTOM_ANIMATION to R.string.playback_component_block_comment_bottom_animation,
        PlaybackComponentBlockConfigProvider.BUTTON_UNFOLLOW_FAMILIAR_REC to R.string.playback_component_block_button_unfollow_familiar_rec,
        PlaybackComponentBlockConfigProvider.NEARBY_HOT_COMMENT to R.string.playback_component_block_nearby_hot_comment,
        PlaybackComponentBlockConfigProvider.ECOM_STORE to R.string.playback_component_block_ecom_store,
        PlaybackComponentBlockConfigProvider.ECOM_TAG_FRIEND to R.string.playback_component_block_ecom_tag_friend,
        PlaybackComponentBlockConfigProvider.JX_PICK to R.string.playback_component_block_jx_pick,
        PlaybackComponentBlockConfigProvider.FLOW to R.string.playback_component_block_flow,
        PlaybackComponentBlockConfigProvider.JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG to R.string.playback_component_block_jx_left_bottom_long_video_plus_title_tag,
        PlaybackComponentBlockConfigProvider.SOCIAL_NEW_COMMENT_GUIDE_BUBBLE to R.string.playback_component_block_social_new_comment_guide_bubble,
        PlaybackComponentBlockConfigProvider.AI_CO_CREATORS_THREE to R.string.playback_component_block_ai_co_creators_three,
        PlaybackComponentBlockConfigProvider.AIGC_COCREATE_STATUS_TITLE to R.string.playback_component_block_aigc_cocreate_status_title
    )

    private fun switchesState(items: List<Pair<String, Int>>) = combine(items.map { (key, _) ->
        ConfigManager.playbackComponentBlock.kvConfig.observe(key, false)
    }
    ) { checkedValues ->
        items.mapIndexed { index, (key, title) ->
            PlaybackComponentVisibilityItem(key, title, checkedValues[index])
        }
    }

    val playbackComponentConfig: StateFlow<PlaybackComponentVisibilityConfig> = combine(
        ConfigManager.playbackComponentBlock.kvConfig.observe(PlaybackComponentBlockConfigProvider.MAIN_SWITCH, false),
        switchesState(catalog)
    ) { mainSwitch, switches ->
        PlaybackComponentVisibilityConfig(mainSwitch, switches)
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = PlaybackComponentVisibilityConfig(
            ConfigManager.playbackComponentBlock.kvConfig.get(PlaybackComponentBlockConfigProvider.MAIN_SWITCH, false),
            catalog.map { (key, title) ->
                PlaybackComponentVisibilityItem(key, title, ConfigManager.playbackComponentBlock.kvConfig.get(key, false))
            }
        )
    )

    fun onMainSwitchChange(checked: Boolean) {
        ConfigManager.playbackComponentBlock.mainSwitch.value = checked
    }

    fun onSwitchChange(key: String, checked: Boolean) {
        ConfigManager.playbackComponentBlock.kvConfig.put(key, checked)
    }
}
