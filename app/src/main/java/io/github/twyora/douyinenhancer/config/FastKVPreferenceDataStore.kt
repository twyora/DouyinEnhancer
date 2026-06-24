package io.github.twyora.douyinenhancer.config

import androidx.preference.PreferenceDataStore

object FastKVPreferenceDataStore : PreferenceDataStore() {

    override fun getBoolean(key: String?, defValue: Boolean): Boolean =
        FastKVConfigManager.kv.getBoolean(key, defValue)

    override fun getFloat(key: String?, defValue: Float): Float =
        FastKVConfigManager.kv.getFloat(key, defValue)

    override fun getInt(key: String?, defValue: Int): Int =
        FastKVConfigManager.kv.getInt(key, defValue)

    override fun getLong(key: String?, defValue: Long): Long =
        FastKVConfigManager.kv.getLong(key, defValue)

    override fun getString(key: String?, defValue: String?): String? =
        FastKVConfigManager.kv.getString(key, defValue)

    override fun putBoolean(key: String?, value: Boolean) {
        FastKVConfigManager.kv.putBoolean(key, value)
    }

    override fun putFloat(key: String?, value: Float) {
        FastKVConfigManager.kv.putFloat(key, value)
    }

    override fun putInt(key: String?, value: Int) {
        FastKVConfigManager.kv.putInt(key, value)
    }

    override fun putLong(key: String?, value: Long) {
        FastKVConfigManager.kv.putLong(key, value)
    }

    override fun putString(key: String?, value: String?) {
        FastKVConfigManager.kv.putString(key, value)
    }
}
