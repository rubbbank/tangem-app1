package com.tangem.tap.features.onboarding.products.wallet.redux

import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 09/09/2021.
 */
sealed class OnboardingWalletAction : Action {
    object Init : OnboardingWalletAction()
    object CreateWallet : OnboardingWalletAction()
    object TopUp : OnboardingWalletAction()
    object Done : OnboardingWalletAction()

    object ProceedBackup : OnboardingWalletAction()

    sealed class Backup : Action {

    }
}