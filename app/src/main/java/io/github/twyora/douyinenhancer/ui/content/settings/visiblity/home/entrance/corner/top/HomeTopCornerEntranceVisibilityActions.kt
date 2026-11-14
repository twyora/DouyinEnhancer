package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.entrance.corner.top

import androidx.compose.runtime.Stable

@Stable
class HomeTopCornerEntranceVisibilityActions(
    val onRightMainSwitchChange: (checked: Boolean) -> Unit,
    val onLeftMainSwitchChange: (checked: Boolean) -> Unit,
    val onSwitchChange: (key: String, checked: Boolean) -> Unit
)
