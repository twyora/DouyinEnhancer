package io.github.twyora.douyinenhancer.config

import io.github.twyora.douyinenhancer.config.kvstorage.IKVStorage

class ModuleConfigProvider(kvConfig: IKVStorage) : AbsConfigProvider(kvConfig) {
    var verboseDisabled by property(DISABLE_VERBOSE_LOGS, false)
    var notifyUpdateCooldown by property(NOTIFY_UPDATE_COOLDOWN, NOTIFY_UPDATE_COOLDOWN_PERIOD)
    var lastVerifiedVersion by property(LAST_VERIFIED_VERSION, 0)

    companion object {
        const val DISABLE_VERBOSE_LOGS = "disable_verbose_logs"
        const val NOTIFY_UPDATE_COOLDOWN = "notify_update_cooldown"
        const val LAST_VERIFIED_VERSION = "last_verified_version"

        const val NOTIFY_UPDATE_COOLDOWN_PERIOD = 3
    }
}
