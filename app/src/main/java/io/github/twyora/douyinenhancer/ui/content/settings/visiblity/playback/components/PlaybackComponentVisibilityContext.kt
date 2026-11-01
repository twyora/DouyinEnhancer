package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun PlaybackComponentVisibilityContext(
    config: PlaybackComponentVisibilityConfig,
    actions: PlaybackComponentVisibilityActions,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            SwitchPreferenceItem(
                title = stringResource(R.string.playback_component_block_main_switch_title),
                summary = stringResource(R.string.playback_component_block_main_switch_summary),
                checked = config.mainSwitch
            ) {
                actions.onMainSwitchChange(it)
            }
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        items(
            items = config.switches,
            key = {
                it.key
            }
        ) { item ->
            SwitchPreferenceItem(
                title = stringResource(item.title),
                checked = item.checked
            ) {
                actions.onSwitchChange(item.key, it)
            }
        }
    }
}

@Preview
@Composable
private fun PlaybackComponentVisibilityContextPreview() {
    val playbackComponentVisibilityItems: List<PlaybackComponentVisibilityItem> = listOf(
        PlaybackComponentVisibilityItem("playback_component_block_avatar", R.string.playback_component_block_avatar, false),
        PlaybackComponentVisibilityItem("playback_component_block_nickname", R.string.playback_component_block_nickname, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_co_creator_author",
            R.string.playback_component_block_co_creator_author,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_digg", R.string.playback_component_block_digg, false),
        PlaybackComponentVisibilityItem("playback_component_block_comment", R.string.playback_component_block_comment, false),
        PlaybackComponentVisibilityItem("playback_component_block_share", R.string.playback_component_block_share, false),
        PlaybackComponentVisibilityItem("playback_component_block_collect", R.string.playback_component_block_collect, false),
        PlaybackComponentVisibilityItem("playback_component_block_right_menu_ll", R.string.playback_component_block_right_menu_ll, false),
        PlaybackComponentVisibilityItem("playback_component_block_music_cover", R.string.playback_component_block_music_cover, false),
        PlaybackComponentVisibilityItem("playback_component_block_title", R.string.playback_component_block_title, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_title_tag_container",
            R.string.playback_component_block_title_tag_container,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_chapter_tag", R.string.playback_component_block_chapter_tag, false),
        PlaybackComponentVisibilityItem("playback_component_block_chapter_detail", R.string.playback_component_block_chapter_detail, false),
        PlaybackComponentVisibilityItem("playback_component_block_music_title", R.string.playback_component_block_music_title, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_music_listen_cover",
            R.string.playback_component_block_music_listen_cover,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_music_cover_block",
            R.string.playback_component_block_music_cover_block,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_music_mute_cover",
            R.string.playback_component_block_music_mute_cover,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_social_new_style_music_below",
            R.string.playback_component_block_social_new_style_music_below,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_post_time", R.string.playback_component_block_post_time, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_bellow_desc_time",
            R.string.playback_component_block_bellow_desc_time,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_social_new_style_post_time_bottom",
            R.string.playback_component_block_social_new_style_post_time_bottom,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_reply", R.string.playback_component_block_reply, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_button_unfollow_familiar",
            R.string.playback_component_block_button_unfollow_familiar,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_danmaku_vertical",
            R.string.playback_component_block_danmaku_vertical,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_ai_search", R.string.playback_component_block_ai_search, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_nearby_identity_tag",
            R.string.playback_component_block_nearby_identity_tag,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_button_im_quick_share",
            R.string.playback_component_block_button_im_quick_share,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_button_feed_im_share_guide_v2",
            R.string.playback_component_block_button_feed_im_share_guide_v2,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_button_force_feed_im_share_guide",
            R.string.playback_component_block_button_force_feed_im_share_guide,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_bottom_bar_common",
            R.string.playback_component_block_bottom_bar_common,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_bottom_bar_mix", R.string.playback_component_block_bottom_bar_mix, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_bottom_bar_normal_search",
            R.string.playback_component_block_bottom_bar_normal_search,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_bottom_bar_common_priority_search",
            R.string.playback_component_block_bottom_bar_common_priority_search,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_bottom_bar_container",
            R.string.playback_component_block_bottom_bar_container,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_common_button", R.string.playback_component_block_common_button, false),
        PlaybackComponentVisibilityItem("playback_component_block_general_label", R.string.playback_component_block_general_label, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_feed_label_container",
            R.string.playback_component_block_feed_label_container,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_sticker", R.string.playback_component_block_sticker, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_anchor_framework",
            R.string.playback_component_block_anchor_framework,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_c2_feed", R.string.playback_component_block_c2_feed, false),
        PlaybackComponentVisibilityItem("playback_component_block_live_photo_tag", R.string.playback_component_block_live_photo_tag, false),
        PlaybackComponentVisibilityItem("playback_component_block_photos_tag", R.string.playback_component_block_photos_tag, false),
        PlaybackComponentVisibilityItem("playback_component_block_story24_tag", R.string.playback_component_block_story24_tag, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_story_25_diverse_digg",
            R.string.playback_component_block_story_25_diverse_digg,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_social_new_style_story_tag",
            R.string.playback_component_block_social_new_style_story_tag,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_long_video_highlight_tag",
            R.string.playback_component_block_long_video_highlight_tag,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_comment_bottom_animation",
            R.string.playback_component_block_comment_bottom_animation,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_button_unfollow_familiar_rec",
            R.string.playback_component_block_button_unfollow_familiar_rec,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_nearby_hot_comment",
            R.string.playback_component_block_nearby_hot_comment,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_ecom_store", R.string.playback_component_block_ecom_store, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_ecom_tag_friend",
            R.string.playback_component_block_ecom_tag_friend,
            false
        ),
        PlaybackComponentVisibilityItem("playback_component_block_jx_pick", R.string.playback_component_block_jx_pick, false),
        PlaybackComponentVisibilityItem("playback_component_block_flow", R.string.playback_component_block_flow, false),
        PlaybackComponentVisibilityItem(
            "playback_component_block_jx_left_bottom_long_video_plus_title_tag",
            R.string.playback_component_block_jx_left_bottom_long_video_plus_title_tag,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_social_new_comment_guide_bubble",
            R.string.playback_component_block_social_new_comment_guide_bubble,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_ai_co_creators_three",
            R.string.playback_component_block_ai_co_creators_three,
            false
        ),
        PlaybackComponentVisibilityItem(
            "playback_component_block_aigc_cocreate_status_title",
            R.string.playback_component_block_aigc_cocreate_status_title,
            false
        )
    )

    PlaybackComponentVisibilityContext(
        config = PlaybackComponentVisibilityConfig(
            mainSwitch = false,
            switches = playbackComponentVisibilityItems
        ),
        actions = PlaybackComponentVisibilityActions(
            onMainSwitchChange = {},
            onSwitchChange = { _, _ -> }
        )
    )
}
