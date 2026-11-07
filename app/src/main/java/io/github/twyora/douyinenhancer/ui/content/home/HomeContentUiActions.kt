package io.github.twyora.douyinenhancer.ui.content.home

import androidx.compose.runtime.Stable

@Stable
class HomeContentUiActions(
    val onLauncherIconChange: (Boolean) -> Unit = {},
    val onOpenHelp: () -> Unit = {},
    val onOpenAuthor: () -> Unit = {},
    val onOpenRepository: () -> Unit = {},
    val onViewRelease: () -> Unit = {},
    val onVersionClick: () -> Unit = {},
    val onJoinTelegramGroup: () -> Unit = {},
    val onNavigateToSettings: () -> Unit = {}
)
