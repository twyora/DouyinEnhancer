package io.github.twyora.douyinenhancer.config

class FeedConfigManager(kvConfig: KVStorage) : ConfigProvider(kvConfig) {
    var bypassListenAwemeRestriction by property(BYPASS_LISTEN_AWEME_RESTRICTION, false)
    var interceptDoubleTapDigg by property(FEED_DOUBLE_TAP_DIGG, false)
    var doubleTapOpenComment by property(FEED_DOUBLE_TAP_OPEN_COMMENT, false)
    var blockAutoReplay by property(FEED_BLOCK_AUTO_REPLAY, false)
    var blockResumePlayback by property(FEED_BLOCK_RESUME_PLAYBACK, false)

    companion object {
        const val BYPASS_LISTEN_AWEME_RESTRICTION = "bypass_listen_aweme_restriction"
        const val FEED_DOUBLE_TAP_DIGG = "disable_feed_double_tap_digg"
        const val FEED_DOUBLE_TAP_OPEN_COMMENT = "feed_double_tap_open_comment"
        const val FEED_BLOCK_AUTO_REPLAY = "block_feed_auto_replay"
        const val FEED_BLOCK_RESUME_PLAYBACK = "block_feed_resume_playback"
    }
}
