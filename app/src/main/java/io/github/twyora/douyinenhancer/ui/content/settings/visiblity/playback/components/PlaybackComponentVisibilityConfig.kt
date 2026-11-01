package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components


data class PlaybackComponentVisibilityItem(
    val key: String,
    val title: Int,
    val checked: Boolean
)

data class PlaybackComponentVisibilityConfig(
    val mainSwitch: Boolean,
    val switches: List<PlaybackComponentVisibilityItem>
)
