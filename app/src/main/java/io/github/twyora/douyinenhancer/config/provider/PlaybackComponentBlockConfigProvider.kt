package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.gate.ConfigValueMode
import io.github.twyora.douyinenhancer.config.gate.RuleGate
import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.HiddenFeatureEnabledRule
import io.github.twyora.douyinenhancer.config.rule.IRule

class PlaybackComponentBlockConfigProvider(
    kvConfig: IKVStorage,
    ruleContextProvider: () -> IRule.Context = {
        IRule.Context(hiddenFeatureEnabled = true)
    }
) : AbsConfigProvider(kvConfig, ruleContextProvider) {
    var mainSwitch = configItem(MAIN_SWITCH, false)

    var musicCoverBlock = configItem(MUSIC_COVER_BLOCK, false)
    var musicListenCover = configItem(MUSIC_LISTEN_COVER, false)
    var digg = configItem(DIGG, false)
    var title = configItem(TITLE, false)
    var musicCover = configItem(MUSIC_COVER, false)
    var generalLabel = configItem(GENERAL_LABEL, false)
    var feedLabelContainer = configItem(FEED_LABEL_CONTAINER, false)
    var musicTitle = configItem(MUSIC_TITLE, false)
    var story25DiverseDigg = configItem(STORY_25_DIVERSE_DIGG, false)
    var ecomStore =
        configItem(ECOM_STORE, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var buttonImQuickShare = configItem(BUTTON_IM_QUICK_SHARE, false)
    var buttonFeedImShareGuideV2 = configItem(BUTTON_FEED_IM_SHARE_GUIDE_V2, false)
    var buttonForceFeedImShareGuide = configItem(BUTTON_FORCE_FEED_IM_SHARE_GUIDE, false)
    var socialNewCommentGuideBubble = configItem(
        SOCIAL_NEW_COMMENT_GUIDE_BUBBLE,
        false,
        RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN)
    )
    var commentBottomAnimation = configItem(COMMENT_BOTTOM_ANIMATION, false)
    var nearbyIdentityTag = configItem(NEARBY_IDENTITY_TAG, false)
    var livePhotoTag = configItem(LIVE_PHOTO_TAG, false)
    var photosTag = configItem(PHOTOS_TAG, false)
    var story24Tag = configItem(STORY24_TAG, false)
    var socialNewStyleStoryTag = configItem(SOCIAL_NEW_STYLE_STORY_TAG, false)
    var longVideoHighlightTag = configItem(LONG_VIDEO_HIGHLIGHT_TAG, false)
    var danmakuVertical = configItem(DANMAKU_VERTICAL, false)
    var avatar = configItem(AVATAR, false)
    var nickname = configItem(NICKNAME, false)
    var postTime = configItem(POST_TIME, false)
    var bellowDescTime = configItem(BELLOW_DESC_TIME, false)
    var comment = configItem(COMMENT, false)
    var reply = configItem(REPLY, false)
    var share = configItem(SHARE, false)
    var collect = configItem(COLLECT, false)
    var anchorFramework = configItem(ANCHOR_FRAMEWORK, false)
    var bottomBarCommon = configItem(BOTTOM_BAR_COMMON, false)
    var commonButton = configItem(COMMON_BUTTON, false)
    var sticker = configItem(STICKER, false)
    var aiSearch = configItem(AI_SEARCH, false)
    var c2Feed = configItem(C2_FEED, false)
    var flow = configItem(FLOW, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var nearbyHotComment =
        configItem(NEARBY_HOT_COMMENT, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var buttonUnfollowFamiliar = configItem(BUTTON_UNFOLLOW_FAMILIAR, false)
    var buttonUnfollowFamiliarRec =
        configItem(
            BUTTON_UNFOLLOW_FAMILIAR_REC,
            false,
            RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN)
        )
    var coCreatorAuthor = configItem(CO_CREATOR_AUTHOR, false)
    var chapterTag = configItem(CHAPTER_TAG, false)
    var ecomTagFriend =
        configItem(ECOM_TAG_FRIEND, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var socialNewStylePostTimeBottom = configItem(SOCIAL_NEW_STYLE_POST_TIME_BOTTOM, false)
    var socialNewStyleMusicBelow = configItem(SOCIAL_NEW_STYLE_MUSIC_BELOW, false)
    var chapterDetail = configItem(CHAPTER_DETAIL, false)
    var titleTagContainer = configItem(TITLE_TAG_CONTAINER, false)
    var rightMenuLl = configItem(RIGHT_MENU_LL, false)
    var musicMuteCover = configItem(MUSIC_MUTE_COVER, false)
    var jxLeftBottomLongVideoPlusTitleTag =
        configItem(
            JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG,
            false,
            RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN)
        )
    var bottomBarMix = configItem(BOTTOM_BAR_MIX, false)
    var bottomBarNormalSearch = configItem(BOTTOM_BAR_NORMAL_SEARCH, false)
    var bottomBarCommonPrioritySearch = configItem(BOTTOM_BAR_COMMON_PRIORITY_SEARCH, false)
    var jxPick = configItem(JX_PICK, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var bottomBarContainer = configItem(BOTTOM_BAR_CONTAINER, false)
    var aiCoCreatorsThree =
        configItem(AI_CO_CREATORS_THREE, false, RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN))
    var aigcCocreateStatusTitle =
        configItem(
            AIGC_COCREATE_STATUS_TITLE,
            false,
            RuleGate(HiddenFeatureEnabledRule, ConfigValueMode.FORCE_DEFAULT, ConfigStateMode.HIDDEN)
        )

    companion object {
        const val MAIN_SWITCH = "playback_component_block_main_switch"
        const val MUSIC_COVER_BLOCK = "playback_component_block_music_cover_block"
        const val MUSIC_LISTEN_COVER = "playback_component_block_music_listen_cover"
        const val DIGG = "playback_component_block_digg"
        const val TITLE = "playback_component_block_title"
        const val MUSIC_COVER = "playback_component_block_music_cover"
        const val GENERAL_LABEL = "playback_component_block_general_label"
        const val FEED_LABEL_CONTAINER = "playback_component_block_feed_label_container"
        const val MUSIC_TITLE = "playback_component_block_music_title"
        const val STORY_25_DIVERSE_DIGG = "playback_component_block_story_25_diverse_digg"
        const val ECOM_STORE = "playback_component_block_ecom_store"
        const val BUTTON_IM_QUICK_SHARE = "playback_component_block_button_im_quick_share"
        const val BUTTON_FEED_IM_SHARE_GUIDE_V2 = "playback_component_block_button_feed_im_share_guide_v2"
        const val BUTTON_FORCE_FEED_IM_SHARE_GUIDE = "playback_component_block_button_force_feed_im_share_guide"
        const val SOCIAL_NEW_COMMENT_GUIDE_BUBBLE = "playback_component_block_social_new_comment_guide_bubble"
        const val COMMENT_BOTTOM_ANIMATION = "playback_component_block_comment_bottom_animation"
        const val NEARBY_IDENTITY_TAG = "playback_component_block_nearby_identity_tag"
        const val LIVE_PHOTO_TAG = "playback_component_block_live_photo_tag"
        const val PHOTOS_TAG = "playback_component_block_photos_tag"
        const val STORY24_TAG = "playback_component_block_story24_tag"
        const val SOCIAL_NEW_STYLE_STORY_TAG = "playback_component_block_social_new_style_story_tag"
        const val LONG_VIDEO_HIGHLIGHT_TAG = "playback_component_block_long_video_highlight_tag"
        const val DANMAKU_VERTICAL = "playback_component_block_danmaku_vertical"
        const val AVATAR = "playback_component_block_avatar"
        const val NICKNAME = "playback_component_block_nickname"
        const val POST_TIME = "playback_component_block_post_time"
        const val BELLOW_DESC_TIME = "playback_component_block_bellow_desc_time"
        const val COMMENT = "playback_component_block_comment"
        const val REPLY = "playback_component_block_reply"
        const val SHARE = "playback_component_block_share"
        const val COLLECT = "playback_component_block_collect"
        const val ANCHOR_FRAMEWORK = "playback_component_block_anchor_framework"
        const val BOTTOM_BAR_COMMON = "playback_component_block_bottom_bar_common"
        const val COMMON_BUTTON = "playback_component_block_common_button"
        const val STICKER = "playback_component_block_sticker"
        const val AI_SEARCH = "playback_component_block_ai_search"
        const val C2_FEED = "playback_component_block_c2_feed"
        const val FLOW = "playback_component_block_flow"
        const val NEARBY_HOT_COMMENT = "playback_component_block_nearby_hot_comment"
        const val BUTTON_UNFOLLOW_FAMILIAR = "playback_component_block_button_unfollow_familiar"
        const val BUTTON_UNFOLLOW_FAMILIAR_REC = "playback_component_block_button_unfollow_familiar_rec"
        const val CO_CREATOR_AUTHOR = "playback_component_block_co_creator_author"
        const val CHAPTER_TAG = "playback_component_block_chapter_tag"
        const val ECOM_TAG_FRIEND = "playback_component_block_ecom_tag_friend"
        const val SOCIAL_NEW_STYLE_POST_TIME_BOTTOM = "playback_component_block_social_new_style_post_time_bottom"
        const val SOCIAL_NEW_STYLE_MUSIC_BELOW = "playback_component_block_social_new_style_music_below"
        const val CHAPTER_DETAIL = "playback_component_block_chapter_detail"
        const val TITLE_TAG_CONTAINER = "playback_component_block_title_tag_container"
        const val RIGHT_MENU_LL = "playback_component_block_right_menu_ll"
        const val MUSIC_MUTE_COVER = "playback_component_block_music_mute_cover"
        const val JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG =
            "playback_component_block_jx_left_bottom_long_video_plus_title_tag"
        const val BOTTOM_BAR_MIX = "playback_component_block_bottom_bar_mix"
        const val BOTTOM_BAR_NORMAL_SEARCH = "playback_component_block_bottom_bar_normal_search"
        const val BOTTOM_BAR_COMMON_PRIORITY_SEARCH = "playback_component_block_bottom_bar_common_priority_search"
        const val JX_PICK = "playback_component_block_jx_pick"
        const val BOTTOM_BAR_CONTAINER = "playback_component_block_bottom_bar_container"
        const val AI_CO_CREATORS_THREE = "playback_component_block_ai_co_creators_three"
        const val AIGC_COCREATE_STATUS_TITLE = "playback_component_block_aigc_cocreate_status_title"
    }
}
