package io.github.twyora.douyinenhancer.config

import android.content.Context
import android.content.SharedPreferences
import io.fastkv.FastKV

object FastKVConfigManager {
    lateinit var settings: SharedPreferences

    fun init(context: Context) {
        settings = FastKV.adapt(context, "douyinenhancer_prefs")
    }
}
