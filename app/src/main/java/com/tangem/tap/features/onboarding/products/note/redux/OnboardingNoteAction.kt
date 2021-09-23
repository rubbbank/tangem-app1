package com.tangem.tap.features.onboarding.products.note.redux

import com.tangem.blockchain.common.WalletManager
import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 23/09/2021.
 */
sealed class OnboardingNoteAction : Action {
    object Init : OnboardingNoteAction()
    object CreateWallet : OnboardingNoteAction()
    object TopUp : OnboardingNoteAction()
    object Done : OnboardingNoteAction()

    data class SetWalletManager(val walletManager: WalletManager) : OnboardingNoteAction()
}