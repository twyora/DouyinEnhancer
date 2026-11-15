package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.entrance.corner.top

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.PreferenceCategory
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun HomeTopCornerEntranceVisibilityContent(
    config: HomeTopCornerEntranceVisibilityConfig,
    actions: HomeTopCornerEntranceVisibilityActions,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            PreferenceCategory(stringResource(R.string.home_top_corner_entrance_right_category))
        }
        item {
            SwitchPreferenceItem(
                title = stringResource(R.string.home_top_corner_entrance_right_main_switch),
                summary = stringResource(R.string.home_top_corner_entrance_right_main_switch_summary),
                checked = config.rightMainSwitch
            ) {
                actions.onRightMainSwitchChange(it)
            }
        }
        items(config.rightSwitches, key = { it.key }) { item ->
            SwitchPreferenceItem(
                title = stringResource(item.title),
                checked = item.checked
            ) {
                actions.onSwitchChange(item.key, it)
            }
        }
        item {
            PreferenceCategory(stringResource(R.string.home_top_corner_entrance_left_category))
        }
        item {
            SwitchPreferenceItem(
                title = stringResource(R.string.home_top_corner_entrance_left_main_switch),
                summary = stringResource(R.string.home_top_corner_entrance_left_main_switch_summary),
                checked = config.leftMainSwitch
            ) {
                actions.onLeftMainSwitchChange(it)
            }
        }
        items(config.leftSwitches, key = { it.key }) { item ->
            SwitchPreferenceItem(
                title = stringResource(item.title),
                checked = item.checked
            ) {
                actions.onSwitchChange(item.key, it)
            }
        }
    }
}

@Preview
@Composable
private fun HomeTopCornerEntranceVisibilityContentPreview() {
    val rightSwitches = listOf(
        HomeTopCornerEntranceVisibilityItem(
            "hide_right_search",
            R.string.home_top_corner_entrance_hide_right_search,
            false
        )
    )
    val leftSwitches = listOf(
        HomeTopCornerEntranceVisibilityItem(
            "hide_left_scan",
            R.string.home_top_corner_entrance_hide_left_scan,
            false
        ),
        HomeTopCornerEntranceVisibilityItem(
            "hide_left_sidebar",
            R.string.home_top_corner_entrance_hide_left_sidebar,
            false
        ),
        HomeTopCornerEntranceVisibilityItem(
            "hide_left_sidebar_jx",
            R.string.home_top_corner_entrance_hide_left_sidebar_jx,
            false
        ),
        HomeTopCornerEntranceVisibilityItem(
            "hide_left_login",
            R.string.home_top_corner_entrance_hide_left_login,
            false
        ),
        HomeTopCornerEntranceVisibilityItem(
            "hide_left_extension_component",
            R.string.home_top_corner_entrance_hide_left_extension_component,
            false
        )
    )
    HomeTopCornerEntranceVisibilityContent(
        config = HomeTopCornerEntranceVisibilityConfig(
            rightMainSwitch = false,
            rightSwitches = rightSwitches,
            leftMainSwitch = false,
            leftSwitches = leftSwitches
        ),
        actions = HomeTopCornerEntranceVisibilityActions(
            onRightMainSwitchChange = {},
            onLeftMainSwitchChange = {},
            onSwitchChange = { _, _ -> }
        )
    )
}
