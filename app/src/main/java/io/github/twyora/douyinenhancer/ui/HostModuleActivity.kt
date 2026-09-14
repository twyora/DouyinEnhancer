package io.github.twyora.douyinenhancer.ui

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import com.highcapable.yukihookapi.hook.xposed.parasitic.activity.proxy.ModuleActivity
import io.github.twyora.douyinenhancer.R

abstract class HostModuleActivity : ComponentActivity(), ModuleActivity {
    override val moduleTheme get() = R.style.Theme_MyModule_Compose

    override fun getClassLoader() = delegate.getClassLoader()

    override fun onCreate(savedInstanceState: Bundle?) {
        delegate.onCreate(savedInstanceState)
        super.onCreate(savedInstanceState)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        delegate.onConfigurationChanged(newConfig)
        super.onConfigurationChanged(newConfig)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        delegate.onRestoreInstanceState(savedInstanceState)
        super.onRestoreInstanceState(savedInstanceState)
    }
}