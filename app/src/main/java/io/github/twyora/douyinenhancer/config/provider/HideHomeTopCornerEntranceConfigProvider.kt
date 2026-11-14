package io.github.twyora.douyinenhancer.config.provider

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage
import io.github.twyora.douyinenhancer.config.rule.IRule

class HideHomeTopCornerEntranceConfigProvider(
    kvConfig: IKVStorage,
    ruleContextProvider: () -> IRule.Context = { IRule.Context(hiddenFeatureEnabled = true) }
) : AbsConfigProvider(kvConfig, ruleContextProvider) {

    val rightMainSwitch = configItem(RIGHT_MAIN_SWITCH, false)
    val hideRightSearch = configItem(HIDE_RIGHT_SEARCH, false)
    val leftMainSwitch = configItem(LEFT_MAIN_SWITCH, false)
    val hideLeftScan = configItem(HIDE_LEFT_SCAN, false)
    val hideLeftSidebar = configItem(HIDE_LEFT_SIDEBAR, false)
    val hideLeftSidebarJx = configItem(HIDE_LEFT_SIDEBAR_JX, false)
    val hideLeftLogin = configItem(HIDE_LEFT_LOGIN, false)
    val hideLeftExtensionComponent = configItem(HIDE_LEFT_EXTENSION_COMPONENT, false)

    companion object {
        const val RIGHT_MAIN_SWITCH = "right_main_switch"
        const val HIDE_RIGHT_SEARCH = "hide_right_search"

        const val LEFT_MAIN_SWITCH = "left_main_switch"
        const val HIDE_LEFT_SCAN = "hide_left_scan"
        const val HIDE_LEFT_SIDEBAR = "hide_left_sidebar"
        const val HIDE_LEFT_SIDEBAR_JX = "hide_left_sidebar_jx"
        const val HIDE_LEFT_LOGIN = "hide_left_login"
        const val HIDE_LEFT_EXTENSION_COMPONENT = "hide_left_extension_component"
    }
}
