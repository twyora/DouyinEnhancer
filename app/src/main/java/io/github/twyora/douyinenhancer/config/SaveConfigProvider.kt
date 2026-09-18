package io.github.twyora.douyinenhancer.config

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage

class SaveConfigProvider(kvConfig: IKVStorage) : AbsConfigProvider(kvConfig) {
    var purifyCommentImage by property(PURIFY_COMMENT_IMAGE, false)
    var unlockCommentEmoji by property(UNLOCK_COMMENT_EMOJI, false)
    var downloadCommentAudio by property(DOWNLOAD_COMMENT_AUDIO, false)
    var feedVideoRemoveWatermark by property(FEED_VIDEO_REMOVE_WATERMARK, false)
    var feedMultiImageRemoveWatermark by property(FEED_MULTI_IMAGE_REMOVE_WATERMARK, false)
    var feedDownloadBypass by property(FEED_DOWNLOAD_BYPASS, false)

    companion object {
        const val PURIFY_COMMENT_IMAGE = "purify_comment_image"
        const val UNLOCK_COMMENT_EMOJI = "unlock_comment_emoji"
        const val DOWNLOAD_COMMENT_AUDIO = "download_comment_audio"
        const val FEED_VIDEO_REMOVE_WATERMARK = "feed_video_remove_watermark"
        const val FEED_MULTI_IMAGE_REMOVE_WATERMARK = "feed_multi_image_remove_watermark"
        const val FEED_DOWNLOAD_BYPASS = "feed_download_bypass"
    }
}
