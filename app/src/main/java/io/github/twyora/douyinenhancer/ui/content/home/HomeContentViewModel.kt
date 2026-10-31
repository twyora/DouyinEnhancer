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
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
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

    val updateNotifyEvent: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

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
                    updateNotifyEvent.tryEmit(Unit)
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

    companion object {
        private val TAG = this::class.simpleName
    }
}
