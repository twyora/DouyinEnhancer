package io.github.twyora.douyinenhancer.config

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage

class UiConfigProvider(kvConfig: IKVStorage) : AbsConfigProvider(kvConfig) {
    var keepDanmakuVisible by property(KEEP_DANMAKU_VISIBLE, false)

    companion object {
        const val KEEP_DANMAKU_VISIBLE = "keep_danmaku_visible"
    }
}
