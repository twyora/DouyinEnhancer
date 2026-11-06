package io.github.twyora.douyinenhancer.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object AppRoutes {
    @Serializable
    data object Home : NavKey

    @Serializable
    data object Verification : NavKey

    @Serializable
    sealed interface Settings : NavKey {
        @Serializable
        data object Main : Settings

        @Serializable
        data object RecommendedFeedFilter : Settings

        @Serializable
        data object PlaybackComponent : Settings

        @Serializable
        data object HomeTab : Settings
    }
}
