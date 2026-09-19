package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.IRule

class SaveConfigProvider(
    kvConfig: IKVStorage,
    ruleContextProvider: () -> IRule.Context = {
        IRule.Context(hiddenFeatureEnabled = true)
    }
) : AbsConfigProvider(kvConfig, ruleContextProvider) {
    var purifyCommentImage = configItem(PURIFY_COMMENT_IMAGE, false)
    var unlockCommentEmoji = configItem(UNLOCK_COMMENT_EMOJI, false)
    var downloadCommentAudio = configItem(DOWNLOAD_COMMENT_AUDIO, false)
    var feedVideoRemoveWatermark = configItem(FEED_VIDEO_REMOVE_WATERMARK, false)
    var feedMultiImageRemoveWatermark = configItem(FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)
    var feedDownloadBypass = configItem(FEED_DOWNLOAD_BYPASS, false)

    companion object {
        const val PURIFY_COMMENT_IMAGE = "purify_comment_image"
        const val UNLOCK_COMMENT_EMOJI = "unlock_comment_emoji"
        const val DOWNLOAD_COMMENT_AUDIO = "download_comment_audio"
        const val FEED_VIDEO_REMOVE_WATERMARK = "feed_video_remove_watermark"
        const val FEED_MULTI_IMAGE_REMOVE_WATERMARK = "feed_multi_image_remove_watermark"
        const val FEED_DOWNLOAD_BYPASS = "feed_download_bypass"
    }
}
