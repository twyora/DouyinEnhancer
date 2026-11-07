package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components

import androidx.compose.runtime.Stable

@Stable
class PlaybackComponentVisibilityActions(
    val onMainSwitchChange: (Boolean) -> Unit,
    val onSwitchChange: (key: String, checked: Boolean) -> Unit
)
