package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R

@Composable
fun HomeTabVisibilityContent(
    config: HomeTabVisibilityConfig,
    actions: HomeTabVisibilityActions,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            HomeTopTabVisibilityContent(
                state = config.top,
                actions = actions.top
            )
        }
        item {
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
        item {
            HomeBottomTabVisibilityContent(
                state = config.bottom,
                actions = actions.bottom
            )
        }
    }
}

@Preview
@Composable
private fun HomeTabVisibilityContentPreview() {
    val homeTopTabVisibilityCatalog: List<HomeTabVisibilityItem> = listOf(
        HomeTabVisibilityItem("top_tab_hide_homepage_hot_container", R.string.top_tab_hide_homepage_hot_container, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_follow", R.string.top_tab_hide_homepage_follow, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_familiar", R.string.top_tab_hide_homepage_familiar, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_mall", R.string.top_tab_hide_homepage_mall, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_groupon", R.string.top_tab_hide_homepage_groupon, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_tablive", R.string.top_tab_hide_homepage_tablive, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_pad_hot", R.string.top_tab_hide_homepage_pad_hot, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_nearby", R.string.top_tab_hide_homepage_nearby, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_hangout", R.string.top_tab_hide_homepage_hangout, false),
        HomeTabVisibilityItem("top_tab_hide_homepage_mediumvideo", R.string.top_tab_hide_homepage_mediumvideo, false)
    )
    val homeBottomTabVisibilityCatalog: List<HomeTabVisibilityItem> = listOf(
        HomeTabVisibilityItem("bottom_tab_hide_homepage_home", R.string.bottom_tab_hide_homepage_home, false),
        HomeTabVisibilityItem("bottom_tab_hide_homepage_mall", R.string.bottom_tab_hide_homepage_mall, false),
        HomeTabVisibilityItem("bottom_tab_hide_homepage_familiar", R.string.bottom_tab_hide_homepage_familiar, false),
        HomeTabVisibilityItem("bottom_tab_hide_homepage_publish", R.string.bottom_tab_hide_homepage_publish, false),
        HomeTabVisibilityItem("bottom_tab_hide_homepage_notification", R.string.bottom_tab_hide_homepage_notification, false),
        HomeTabVisibilityItem("bottom_tab_hide_homepage_profile", R.string.bottom_tab_hide_homepage_profile, false)
    )
    HomeTabVisibilityContent(
        config = HomeTabVisibilityConfig(
            top = HomeTopTabVisibilityConfig(
                mainSwitch = false,
                switches = homeTopTabVisibilityCatalog
            ),
            bottom = HomeBottomTabVisibilityConfig(
                mainSwitch = false,
                switches = homeBottomTabVisibilityCatalog
            )
        ),
        actions = HomeTabVisibilityActions(
            top = HomeTopTabVisibilityActions(
                onMainSwitchChange = {},
                onSwitchChange = { _, _ -> }
            ),
            bottom = HomeBottomTabVisibilityActions(
                onMainSwitchChange = {},
                onSwitchChange = { _, _ -> }
            )
        )
    )
}
