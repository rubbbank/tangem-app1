package com.tangem.tap.common.redux

import com.tangem.tap.features.wallet.redux.AddressData

/**
 * Created by Anton Zhilenkov on 25/09/2021.
 */
interface StateDialog

sealed class AppDialog : StateDialog {
    object ScanFailsDialog : AppDialog()
    data class AddressInfoDialog(
        val addressData: AddressData,
        val onCopyAddress: () -> Unit,
        val onExploreAddress: () -> Unit
    ) : AppDialog()
}