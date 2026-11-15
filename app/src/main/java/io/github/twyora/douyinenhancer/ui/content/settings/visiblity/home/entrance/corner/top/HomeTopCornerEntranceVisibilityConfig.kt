package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.entrance.corner.top

data class HomeTopCornerEntranceVisibilityItem(val key: String, val title: Int, val checked: Boolean)

data class HomeTopCornerEntranceVisibilityConfig(
    val rightMainSwitch: Boolean,
    val rightSwitches: List<HomeTopCornerEntranceVisibilityItem>,
    val leftMainSwitch: Boolean,
    val leftSwitches: List<HomeTopCornerEntranceVisibilityItem>
)
