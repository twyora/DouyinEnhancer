package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun HomeTopTabVisibilityContent(
    state: HomeTopTabVisibilityConfig,
    actions: HomeTopTabVisibilityActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SwitchPreferenceItem(
            title = stringResource(R.string.top_tab_main_switch_title),
            summary = stringResource(R.string.top_tab_main_switch_summary),
            checked = state.mainSwitch
        ) {
            actions.onMainSwitchChange(it)
        }
        state.switches.forEach { item ->
            SwitchPreferenceItem(
                title = stringResource(item.title),
                checked = item.checked
            ) {
                actions.onSwitchChange(item.key, it)
            }
        }
    }
}
