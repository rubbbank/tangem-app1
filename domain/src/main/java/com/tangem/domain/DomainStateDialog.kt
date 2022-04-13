package com.tangem.domain

import com.tangem.common.extensions.VoidCallback
import com.tangem.network.api.tangemTech.Coins

/**
 * Created by Anton Zhilenkov on 10/04/2022.
 */
interface DomainStateDialog

sealed class DomainDialog : DomainStateDialog {

    data class SelectTokenDialog(
        val items: List<Coins.CheckAddressResponse.Token.Contract>,
        val networkIdConverter: (String) -> String,
        val onSelect: (Coins.CheckAddressResponse.Token.Contract) -> Unit,
        val onClose: VoidCallback = {}
    ) : DomainDialog()
}