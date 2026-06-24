package io.github.twyora.douyinenhancer.config

import android.content.Context
import android.content.SharedPreferences
import io.fastkv.FastKV

object FastKVConfigManager : SharedPreferences, SharedPreferences.Editor {
    lateinit var kv: FastKV

    fun init(context: Context) {
        kv = FastKV.Builder(context, "dy_settings_data").build()
    }

    // region SharedPreferences

    override fun contains(key: String?): Boolean = kv.contains(key)

    override fun edit(): SharedPreferences.Editor = this

    override fun getAll(): MutableMap<String, *> = kv.all

    override fun getBoolean(key: String?, defaultValue: Boolean): Boolean =
        kv.getBoolean(key, defaultValue)

    override fun getFloat(key: String?, defaultValue: Float): Float =
        kv.getFloat(key, defaultValue)

    override fun getInt(key: String?, defaultValue: Int): Int =
        kv.getInt(key, defaultValue)

    override fun getLong(key: String?, defaultValue: Long): Long =
        kv.getLong(key, defaultValue)

    override fun getString(key: String?, defaultValue: String?): String? =
        kv.getString(key, defaultValue)

    override fun getStringSet(key: String?, defaultValue: MutableSet<String>?): MutableSet<String>? =
        kv.getStringSet(key, defaultValue)

    override fun registerOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw UnsupportedOperationException("registerOnSharedPreferenceChangeListener is not supported")
    }

    override fun unregisterOnSharedPreferenceChangeListener(listener: SharedPreferences.OnSharedPreferenceChangeListener?) {
        throw UnsupportedOperationException("unregisterOnSharedPreferenceChangeListener is not supported")
    }

    override fun apply() {}

    override fun clear(): SharedPreferences.Editor {
        kv.clear()
        return this
    }

    override fun commit(): Boolean = true

    override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
        kv.putBoolean(key, value)
        return this
    }

    override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
        kv.putFloat(key, value)
        return this
    }

    override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
        kv.putInt(key, value)
        return this
    }

    override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
        kv.putLong(key, value)
        return this
    }

    override fun putString(key: String?, value: String?): SharedPreferences.Editor {
        kv.putString(key, value)
        return this
    }

    override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
        kv.putStringSet(key, values)
        return this
    }

    override fun remove(key: String?): SharedPreferences.Editor {
        kv.remove(key)
        return this
    }
}