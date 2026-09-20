package io.github.twyora.douyinenhancer.bridge

import android.content.res.Resources
import android.content.res.XModuleResources
import com.highcapable.yukihookapi.hook.param.PackageParam

object ModuleApp {
    lateinit var apkPath: String
        private set

    val resources: XModuleResources by lazy {
        XModuleResources.createInstance(apkPath, null)
    }

    fun init(packageParam: PackageParam) {
        apkPath = packageParam.moduleAppFilePath
    }

    fun refreshResources() {
        resources.updateConfiguration(
            Resources.getSystem().configuration,
            Resources.getSystem().displayMetrics
        )
    }
}