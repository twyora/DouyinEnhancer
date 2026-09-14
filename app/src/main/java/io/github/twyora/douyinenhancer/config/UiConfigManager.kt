package io.github.twyora.douyinenhancer.config

class UiConfigManager(kvConfig: KVStorage) : ConfigProvider(kvConfig) {
    var keepDanmakuVisible by property(KEEP_DANMAKU_VISIBLE, false)

    companion object {
        const val KEEP_DANMAKU_VISIBLE = "keep_danmaku_visible"
    }
}
