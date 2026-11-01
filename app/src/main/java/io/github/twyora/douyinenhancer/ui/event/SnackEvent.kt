package io.github.twyora.douyinenhancer.ui.event

import androidx.annotation.StringRes

data class SnackEvent(
    @StringRes val resId: Int,
    val args: List<Any> = emptyList()
)
