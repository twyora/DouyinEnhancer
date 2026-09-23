package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.IRule

class FeedConfigProvider(
    kvConfig: IKVStorage,
    ruleContextProvider: () -> IRule.Context = {
        IRule.Context(hiddenFeatureEnabled = true)
    }
) : AbsConfigProvider(kvConfig, ruleContextProvider) {
    val bypassListenAwemeRestriction = configItem(BYPASS_LISTEN_AWEME_RESTRICTION, false)
    val interceptDoubleTapDigg = configItem(FEED_DOUBLE_TAP_DIGG, false)
    val doubleTapOpenComment = configItem(FEED_DOUBLE_TAP_OPEN_COMMENT, false)
    val blockAutoReplay = configItem(FEED_BLOCK_AUTO_REPLAY, false)
    val blockResumePlayback = configItem(FEED_BLOCK_RESUME_PLAYBACK, false)

    companion object {
        const val BYPASS_LISTEN_AWEME_RESTRICTION = "bypass_listen_aweme_restriction"
        const val FEED_DOUBLE_TAP_DIGG = "disable_feed_double_tap_digg"
        const val FEED_DOUBLE_TAP_OPEN_COMMENT = "feed_double_tap_open_comment"
        const val FEED_BLOCK_AUTO_REPLAY = "block_feed_auto_replay"
        const val FEED_BLOCK_RESUME_PLAYBACK = "block_feed_resume_playback"
    }
}
