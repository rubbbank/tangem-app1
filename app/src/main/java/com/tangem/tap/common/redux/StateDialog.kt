package com.tangem.tap.common.redux

/**
 * Created by Anton Zhilenkov on 25/09/2021.
 */
interface StateDialog

sealed class AppDialog : StateDialog {
    object ScanFailsDialog : AppDialog()
}