import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import com.highcapable.yukihookapi.hook.factory.injectModuleAppResources
import com.highcapable.yukihookapi.hook.log.YLog
import io.fastkv.FastKV
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.utils.setField

/**
 * Settings dialog for DouyinEnhancer.
 *
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/SettingDialog.kt)
 */
class SettingsDialog(context: Context) : AlertDialog.Builder(context) {
    class PrefsFragment : PreferenceFragment() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val fastKVSharedPref = FastKV.adapt(this.activity, "douyinenhancer_prefs")
            preferenceManager.setField<Any?>(DouyinPackage.Field("mSharedPreferences"), fastKVSharedPref)
            preferenceManager.setField<Any?>(DouyinPackage.Field("mEditor"), null)
            addPreferencesFromResource(R.xml.prefs_setting)
        }
    }

    init {
        val activity = context as Activity
        activity.injectModuleAppResources()

        val prefsFragment = PrefsFragment()
        activity.fragmentManager.beginTransaction().add(prefsFragment, "Settings").commit()
        activity.fragmentManager.executePendingTransactions()

        setView(prefsFragment.view)
        setTitle("抖柚设置")
        setNegativeButton("返回", null)
        setPositiveButton("确定", null)
        setOnDismissListener {
            activity.fragmentManager.beginTransaction().remove(prefsFragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val TAG = this::class.simpleName

        fun show(context: Context) {
            runCatching {
                SettingsDialog(context).show()
            }.onFailure {
                YLog.error("$TAG: SettingDialog show failed", it)
            }
        }
    }
}
