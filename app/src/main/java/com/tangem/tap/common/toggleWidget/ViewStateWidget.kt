package com.tangem.tap.common.toggleWidget


/**
 * Created by Anton Zhilenkov on 07/07/2020.
 */
interface WidgetState

interface ViewStateWidget {
    fun changeState(state: WidgetState)
}

sealed class ProgressState : WidgetState {
    object None : ProgressState()
    data class Progress(val progress: Int = 0) : ProgressState()
}