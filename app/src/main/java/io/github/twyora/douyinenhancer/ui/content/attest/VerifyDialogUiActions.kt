package io.github.twyora.douyinenhancer.ui.content.attest

import androidx.compose.runtime.Stable

@Stable
class VerifyDialogUiActions(val onInputUrlChange: (String) -> Unit, val onConfirm: (String) -> Unit, val onCancel: () -> Unit)
