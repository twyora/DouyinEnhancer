package io.github.twyora.douyinenhancer.ui.content.home

import android.app.Application
import android.content.ComponentName
import android.content.pm.PackageManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import androidx.annotation.StringRes
import androidx.navigation3.runtime.NavKey
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class HomeContentViewModel(application: Application) : AndroidViewModel(application) {
    private val launcherAlias = ComponentName(
        application,
        "io.github.twyora.douyinenhancer.ui.MainActivityAlias"
    )

    val uiState: StateFlow<HomeContentUiState>
        field = MutableStateFlow(
            HomeContentUiState(
                launcherIconHidden = false,
                moduleActivationState = false,
                updateState = HomeContentUpdateState(
                    latestVersionName = BuildConfig.VERSION_NAME,
                    releaseBody = null
                )
            )
        )

    val snackFlow: SharedFlow<SnackEvent>
        field = MutableSharedFlow<SnackEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val triggerOpenUrl: SharedFlow<Int>
        field = MutableSharedFlow<Int>(extraBufferCapacity = 1)

    val triggerNavigation: SharedFlow<NavKey>
        field = MutableSharedFlow<NavKey>(extraBufferCapacity = 1)

    val versionClickCount: StateFlow<Int>
        field = MutableStateFlow(0)

    private val hiddenFeatureEnabled: StateFlow<Boolean> =
        ConfigManager.misc.hiddenFeatureEnabled.observe().stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ConfigManager.misc.hiddenFeatureEnabled.defValue
        )

    init {
        viewModelScope.launch {
            uiState.update {
                it.copy(
                    launcherIconHidden = getApplication<Application>().packageManager.getComponentEnabledSetting(
                        launcherAlias
                    ) == PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    moduleActivationState = YukiHook.Status.isModuleActive
                )
            }
            fetchUpdateInfo()?.let { updateInfo ->
                uiState.update {
                    it.copy(updateState = updateInfo)
                }

                if (BuildConfig.VERSION_NAME != updateInfo.latestVersionName) {
                    sendSnack(SnackEvent(R.string.notify_update_available))
                }
            }
        }
    }

    fun setLauncherIconHidden(hidden: Boolean) {
        runCatching {
            getApplication<Application>().packageManager.setComponentEnabledSetting(
                launcherAlias,
                if (hidden) {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                },
                PackageManager.DONT_KILL_APP
            )
        }.onSuccess {
            uiState.update {
                it.copy(launcherIconHidden = hidden)
            }
        }
    }

    private suspend fun fetchUpdateInfo(): HomeContentUpdateState? = runCatching {
        val latestReleaseJson = withContext(Dispatchers.IO) {
            JSONObject(
                URL(
                    getApplication<Application>().getString(
                        R.string.latest_release_api_url
                    )
                ).readText()
            )
        }

        val latestVersionName = latestReleaseJson.optString("name").removePrefix("v").removePrefix("V")
        val releaseBody = latestReleaseJson.optString("body")

        HomeContentUpdateState(
            latestVersionName = latestVersionName,
            releaseBody = releaseBody
        )
    }.onFailure {
        YLog.error("$TAG: fetch latest release failed", it)
    }.getOrNull()

    fun requestOpenUrl(@StringRes resId: Int) {
        triggerOpenUrl.tryEmit(resId)
    }

    fun requestNavigation(navKey: NavKey) {
        triggerNavigation.tryEmit(navKey)
    }

    fun onVersionClick() {
        if (hiddenFeatureEnabled.value) {
            sendSnack(SnackEvent(R.string.pref_misc_enable_hidden_features_already_enabled))
            return
        }
        val count = ++versionClickCount.value
        if (count == HIDDEN_FEATURE_TRIGGER_CLICK_COUNT) {
            setHiddenFeatureState(true)
            sendSnack(SnackEvent(R.string.pref_misc_enable_hidden_features_restart_required))
        } else if (count >= HIDDEN_FEATURE_HINT_FROM_CLICK_COUNT) {
            sendSnack(
                SnackEvent(
                    R.string.pref_misc_enable_hidden_features_steps_remaining,
                    listOf(HIDDEN_FEATURE_TRIGGER_CLICK_COUNT - count)
                )
            )
        }
    }

    fun setHiddenFeatureState(enabled: Boolean) {
        ConfigManager.misc.hiddenFeatureEnabled.value = enabled
    }

    fun sendSnack(event: SnackEvent) {
        snackFlow.tryEmit(event)
    }

    companion object {
        private val TAG = this::class.simpleName

        private const val HIDDEN_FEATURE_TRIGGER_CLICK_COUNT = 20
        private const val HIDDEN_FEATURE_HINT_FROM_CLICK_COUNT = 17
    }
}
