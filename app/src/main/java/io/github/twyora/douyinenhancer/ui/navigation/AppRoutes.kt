package io.github.twyora.douyinenhancer.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object AppRoutes {
    @Serializable
    data object Home : NavKey

    object Settings {
        @Serializable
        data object Main : NavKey

        @Serializable
        data object RecommendedFeedFilter : NavKey

        @Serializable
        data object PlaybackComponent : NavKey

        @Serializable
        data object HomeTab : NavKey
    }
}
