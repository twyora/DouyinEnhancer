package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.tab

import androidx.compose.runtime.Stable

@Stable
class HomeBottomTabVisibilityActions(val onMainSwitchChange: (Boolean) -> Unit, val onSwitchChange: (key: String, checked: Boolean) -> Unit)
