package io.github.twyora.douyinenhancer.ui.content.attest

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.highcapable.kavaref.extension.classOf
import com.highcapable.kavaref.extension.isSubclassOf
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class VerifyDialogViewModel(private val validUrls: Array<String>) : ViewModel() {
    val uiState: StateFlow<VerifyDialogUiState>
        field = MutableStateFlow(VerifyDialogUiState(inputUrl = ""))

    val snackFlow: SharedFlow<SnackEvent>
        field = MutableSharedFlow<SnackEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    val triggerVerifySuccess: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    val triggerVerifyFailure: SharedFlow<Unit>
        field = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun onInputTextChanged(text: String) {
        uiState.update {
            it.copy(inputUrl = text)
        }
    }

    fun onConfirm(inputUrl: String) {
        val valid = validUrls.any {
            inputUrl.contains(it, ignoreCase = true)
        }
        if (!valid) {
            triggerVerifyFailure.tryEmit(Unit)
            return
        }

        ConfigManager.module.lastVerifiedVersion.value = BuildConfig.VERSION_CODE
        triggerVerifySuccess.tryEmit(Unit)
        snackFlow.tryEmit(SnackEvent(R.string.verify_toast_success))
    }

    fun onCancel() {
        triggerVerifyFailure.tryEmit(Unit)
    }
}

class VerifyDialogViewModelFactory(private val validUrls: Array<String>) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(classOf<VerifyDialogViewModel>() isSubclassOf modelClass) {
            "Unknown ViewModel class: ${modelClass.name}"
        }
        @Suppress("UNCHECKED_CAST")
        return VerifyDialogViewModel(validUrls) as T
    }
}
