package com.tangem.tap.features.onboarding.products.wallet.saltPay.dialog

import com.tangem.tap.common.redux.StateDialog
import com.tangem.tap.features.onboarding.products.wallet.saltPay.message.SaltPayRegistrationError

/**
 * Created by Anton Zhilenkov on 12.10.2022.
 */
sealed class SaltPayDialog : StateDialog {
    object NoFundsForActivation : SaltPayDialog()
    data class RegistrationError(val error: SaltPayRegistrationError) : SaltPayDialog()
}
