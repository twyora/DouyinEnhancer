package io.github.twyora.douyinenhancer.config

class PlaybackComponentBlockConfigManager(
    kvConfig: IKVStorage,
    gates: Map<FeatureGate, () -> Boolean> = emptyMap()
) : ConfigProvider(kvConfig, gates) {
    override val gatedKeys = mapOf(
        FeatureGate.HIDDEN to setOf(
            BUTTON_UNFOLLOW_FAMILIAR_REC,
            NEARBY_HOT_COMMENT,
            ECOM_STORE,
            ECOM_TAG_FRIEND,
            JX_PICK,
            FLOW,
            JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG,
            SOCIAL_NEW_COMMENT_GUIDE_BUBBLE,
            AI_CO_CREATORS_THREE,
            AIGC_COCREATE_STATUS_TITLE
        )
    )

    var mainSwitch by property(MAIN_SWITCH, false)

    var musicCoverBlock by property(MUSIC_COVER_BLOCK, false)
    var musicListenCover by property(MUSIC_LISTEN_COVER, false)
    var digg by property(DIGG, false)
    var title by property(TITLE, false)
    var musicCover by property(MUSIC_COVER, false)
    var generalLabel by property(GENERAL_LABEL, false)
    var feedLabelContainer by property(FEED_LABEL_CONTAINER, false)
    var musicTitle by property(MUSIC_TITLE, false)
    var story25DiverseDigg by property(STORY_25_DIVERSE_DIGG, false)
    var ecomStore by property(ECOM_STORE, false)
    var buttonImQuickShare by property(BUTTON_IM_QUICK_SHARE, false)
    var buttonFeedImShareGuideV2 by property(BUTTON_FEED_IM_SHARE_GUIDE_V2, false)
    var buttonForceFeedImShareGuide by property(BUTTON_FORCE_FEED_IM_SHARE_GUIDE, false)
    var socialNewCommentGuideBubble by property(SOCIAL_NEW_COMMENT_GUIDE_BUBBLE, false)
    var commentBottomAnimation by property(COMMENT_BOTTOM_ANIMATION, false)
    var nearbyIdentityTag by property(NEARBY_IDENTITY_TAG, false)
    var livePhotoTag by property(LIVE_PHOTO_TAG, false)
    var photosTag by property(PHOTOS_TAG, false)
    var story24Tag by property(STORY24_TAG, false)
    var socialNewStyleStoryTag by property(SOCIAL_NEW_STYLE_STORY_TAG, false)
    var longVideoHighlightTag by property(LONG_VIDEO_HIGHLIGHT_TAG, false)
    var danmakuVertical by property(DANMAKU_VERTICAL, false)
    var avatar by property(AVATAR, false)
    var nickname by property(NICKNAME, false)
    var postTime by property(POST_TIME, false)
    var bellowDescTime by property(BELLOW_DESC_TIME, false)
    var comment by property(COMMENT, false)
    var reply by property(REPLY, false)
    var share by property(SHARE, false)
    var collect by property(COLLECT, false)
    var anchorFramework by property(ANCHOR_FRAMEWORK, false)
    var bottomBarCommon by property(BOTTOM_BAR_COMMON, false)
    var commonButton by property(COMMON_BUTTON, false)
    var sticker by property(STICKER, false)
    var aiSearch by property(AI_SEARCH, false)
    var c2Feed by property(C2_FEED, false)
    var flow by property(FLOW, false)
    var nearbyHotComment by property(NEARBY_HOT_COMMENT, false)
    var buttonUnfollowFamiliar by property(BUTTON_UNFOLLOW_FAMILIAR, false)
    var buttonUnfollowFamiliarRec by property(BUTTON_UNFOLLOW_FAMILIAR_REC, false)
    var coCreatorAuthor by property(CO_CREATOR_AUTHOR, false)
    var chapterTag by property(CHAPTER_TAG, false)
    var ecomTagFriend by property(ECOM_TAG_FRIEND, false)
    var socialNewStylePostTimeBottom by property(SOCIAL_NEW_STYLE_POST_TIME_BOTTOM, false)
    var socialNewStyleMusicBelow by property(SOCIAL_NEW_STYLE_MUSIC_BELOW, false)
    var chapterDetail by property(CHAPTER_DETAIL, false)
    var titleTagContainer by property(TITLE_TAG_CONTAINER, false)
    var rightMenuLl by property(RIGHT_MENU_LL, false)
    var musicMuteCover by property(MUSIC_MUTE_COVER, false)
    var jxLeftBottomLongVideoPlusTitleTag by property(JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG, false)
    var bottomBarMix by property(BOTTOM_BAR_MIX, false)
    var bottomBarNormalSearch by property(BOTTOM_BAR_NORMAL_SEARCH, false)
    var bottomBarCommonPrioritySearch by property(BOTTOM_BAR_COMMON_PRIORITY_SEARCH, false)
    var jxPick by property(JX_PICK, false)
    var bottomBarContainer by property(BOTTOM_BAR_CONTAINER, false)
    var aiCoCreatorsThree by property(AI_CO_CREATORS_THREE, false)
    var aigcCocreateStatusTitle by property(AIGC_COCREATE_STATUS_TITLE, false)

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
        const val JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG = "playback_component_block_jx_left_bottom_long_video_plus_title_tag"
        const val BOTTOM_BAR_MIX = "playback_component_block_bottom_bar_mix"
        const val BOTTOM_BAR_NORMAL_SEARCH = "playback_component_block_bottom_bar_normal_search"
        const val BOTTOM_BAR_COMMON_PRIORITY_SEARCH = "playback_component_block_bottom_bar_common_priority_search"
        const val JX_PICK = "playback_component_block_jx_pick"
        const val BOTTOM_BAR_CONTAINER = "playback_component_block_bottom_bar_container"
        const val AI_CO_CREATORS_THREE = "playback_component_block_ai_co_creators_three"
        const val AIGC_COCREATE_STATUS_TITLE = "playback_component_block_aigc_cocreate_status_title"
    }
}
