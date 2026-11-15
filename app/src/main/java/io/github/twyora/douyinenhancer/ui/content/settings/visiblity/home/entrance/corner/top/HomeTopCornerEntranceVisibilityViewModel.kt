package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.home.entrance.corner.top

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.provider.ConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeTopCornerEntranceVisibilityViewModel : ViewModel() {

    private val rightMainFlow: Flow<Boolean> =
        ConfigManager.hideHomeTopCornerEntrance.rightMainSwitch.observe()
    private val leftMainFlow: Flow<Boolean> =
        ConfigManager.hideHomeTopCornerEntrance.leftMainSwitch.observe()

    private val rightCatalog: List<Pair<ConfigItem<Boolean>, Int>> =
        with(ConfigManager.hideHomeTopCornerEntrance) {
            listOf(
                hideRightSearch to R.string.home_top_corner_entrance_hide_right_search
            )
        }

    private val leftCatalog: List<Pair<ConfigItem<Boolean>, Int>> =
        with(ConfigManager.hideHomeTopCornerEntrance) {
            listOf(
                hideLeftScan to R.string.home_top_corner_entrance_hide_left_scan,
                hideLeftSidebar to R.string.home_top_corner_entrance_hide_left_sidebar,
                hideLeftSidebarJx to R.string.home_top_corner_entrance_hide_left_sidebar_jx,
                hideLeftLogin to R.string.home_top_corner_entrance_hide_left_login,
                hideLeftExtensionComponent to R.string.home_top_corner_entrance_hide_left_extension_component
            )
        }

    private val rightFlow: Flow<List<HomeTopCornerEntranceVisibilityItem>> =
        combine(
            rightCatalog.map { item ->
                item.first.observe()
            }
        ) { values ->
            rightCatalog.mapIndexed { index, (item, title) ->
                HomeTopCornerEntranceVisibilityItem(item.key, title, values[index])
            }
        }

    private val leftFlow: Flow<List<HomeTopCornerEntranceVisibilityItem>> =
        combine(
            leftCatalog.map { item ->
                item.first.observe()
            }
        ) { values ->
            leftCatalog.mapIndexed { index, (item, title) ->
                HomeTopCornerEntranceVisibilityItem(item.key, title, values[index])
            }
        }

    val homeTopCornerEntranceConfig: StateFlow<HomeTopCornerEntranceVisibilityConfig> =
        combine(rightMainFlow, rightFlow, leftMainFlow, leftFlow) { rightMain, right, leftMain, left ->
            HomeTopCornerEntranceVisibilityConfig(rightMain, right, leftMain, left)
        }.stateIn(
            viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeTopCornerEntranceVisibilityConfig(false, emptyList(), false, emptyList())
        )

    fun onRightMainSwitchChange(checked: Boolean) {
        ConfigManager.hideHomeTopCornerEntrance.kvConfig.put(
            ConfigManager.hideHomeTopCornerEntrance.rightMainSwitch.key,
            checked
        )
    }

    fun onLeftMainSwitchChange(checked: Boolean) {
        ConfigManager.hideHomeTopCornerEntrance.kvConfig.put(
            ConfigManager.hideHomeTopCornerEntrance.leftMainSwitch.key,
            checked
        )
    }

    fun onSwitchChange(key: String, checked: Boolean) {
        ConfigManager.hideHomeTopCornerEntrance.kvConfig.put(key, checked)
    }
}
