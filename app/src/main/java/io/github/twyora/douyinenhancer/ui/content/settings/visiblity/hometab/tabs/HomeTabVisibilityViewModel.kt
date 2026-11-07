package io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.config.provider.ConfigItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class HomeTabVisibilityViewModel : ViewModel() {
    private val topTabCatalog: List<Pair<ConfigItem<Boolean>, Int>> = with(ConfigManager.homeTab) {
        listOf(
            hideHomepageHotContainer to R.string.top_tab_hide_homepage_hot_container,
            hideHomepageFollow to R.string.top_tab_hide_homepage_follow,
            hideHomepageFamiliarTop to R.string.top_tab_hide_homepage_familiar,
            hideHomepageMallTop to R.string.top_tab_hide_homepage_mall,
            hideHomepageGroupon to R.string.top_tab_hide_homepage_groupon,
            hideHomepageTablive to R.string.top_tab_hide_homepage_tablive,
            hideHomepagePadHot to R.string.top_tab_hide_homepage_pad_hot,
            hideHomepageNearby to R.string.top_tab_hide_homepage_nearby,
            hideHomepageHangout to R.string.top_tab_hide_homepage_hangout,
            hideHomepageMediumvideo to R.string.top_tab_hide_homepage_mediumvideo
        )
    }

    private val hiddenFeatureEnabled: Flow<Boolean> =
        ConfigManager.misc.hiddenFeatureEnabled.observe()

    private val topTabVisibilityItems: Flow<List<HomeTabVisibilityItem>> = combine(
        combine(
            topTabCatalog.map { (item, _) ->
                item.observe()
            }
        ) { values ->
            values
        },
        hiddenFeatureEnabled
    ) { checkedValues, _ ->
        // visibility is decided solely by ConfigItem.status. The hidden-feature flow
        // is only a recompute trigger; it isn't used in the decision.
        topTabCatalog.mapIndexedNotNull { index, (item, title) ->
            if (item.status == ConfigStateMode.NORMAL) {
                HomeTabVisibilityItem(item.key, title, checkedValues[index])
            } else {
                null
            }
        }
    }

    private val bottomCatalog: List<Pair<ConfigItem<Boolean>, Int>> = with(ConfigManager.homeTab) {
        listOf(
            hideHomepageHome to R.string.bottom_tab_hide_homepage_home,
            hideHomepageMallBottom to R.string.bottom_tab_hide_homepage_mall,
            hideHomepageFamiliarBottom to R.string.bottom_tab_hide_homepage_familiar,
            hideHomepagePublish to R.string.bottom_tab_hide_homepage_publish,
            hideHomepageNotification to R.string.bottom_tab_hide_homepage_notification,
            hideHomepageProfile to R.string.bottom_tab_hide_homepage_profile
        )
    }

    private val bottomTabVisibilityItems: Flow<List<HomeTabVisibilityItem>> = combine(
        combine(
            bottomCatalog.map { (item, _) ->
                item.observe()
            }
        ) { values ->
            values
        },
        hiddenFeatureEnabled
    ) { checkedValues, _ ->
        // Visibility is decided solely by ConfigItem.status. The hidden-feature flow
        // is only a recompute trigger; it isn't used in the decision.
        bottomCatalog.mapIndexedNotNull { index, (item, title) ->
            if (item.status == ConfigStateMode.NORMAL) {
                HomeTabVisibilityItem(item.key, title, checkedValues[index])
            } else {
                null
            }
        }
    }

    private val topTabConfig = combine(
        ConfigManager.homeTab.topMainSwitch.observe(),
        topTabVisibilityItems
    ) { topMain, topItems ->
        HomeTopTabVisibilityConfig(topMain, topItems)
    }

    private val bottomTabConfig = combine(
        ConfigManager.homeTab.bottomMainSwitch.observe(),
        bottomTabVisibilityItems
    ) { bottomMain, bottomItems ->
        HomeBottomTabVisibilityConfig(bottomMain, bottomItems)
    }

    // TODO: Interim solution — non-NORMAL items are dropped while hidden features are off, with
    // GRAYED intentionally unhandled for now (no configItem refactor intended). The gating layer was
    // over-engineered ahead of any real usage and will need its own rework; when that happens this site
    // must receive the full, correct implementation instead of this shortcut.
    val tabConfig: StateFlow<HomeTabVisibilityConfig> = combine(
        topTabConfig,
        bottomTabConfig
    ) { topTabConfig, bottomTabConfig ->
        HomeTabVisibilityConfig(
            top = topTabConfig,
            bottom = bottomTabConfig
        )
    }.stateIn(
        viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeTabVisibilityConfig(
            top = HomeTopTabVisibilityConfig(
                ConfigManager.homeTab.topMainSwitch.value,
                topTabCatalog.mapNotNull { (item, title) ->
                    if (item.status == ConfigStateMode.NORMAL) {
                        HomeTabVisibilityItem(item.key, title, item.value)
                    } else {
                        null
                    }
                }
            ),
            bottom = HomeBottomTabVisibilityConfig(
                ConfigManager.homeTab.bottomMainSwitch.value,
                bottomCatalog.mapNotNull { (item, title) ->
                    if (item.status == ConfigStateMode.NORMAL) {
                        HomeTabVisibilityItem(item.key, title, item.value)
                    } else {
                        null
                    }
                }
            )
        )
    )

    fun onTopMainSwitchChange(checked: Boolean) {
        ConfigManager.homeTab.topMainSwitch.value = checked
    }

    fun onBottomMainSwitchChange(checked: Boolean) {
        ConfigManager.homeTab.bottomMainSwitch.value = checked
    }

    fun onSwitchChange(key: String, checked: Boolean) {
        ConfigManager.homeTab.kvConfig.put(key, checked)
    }
}
