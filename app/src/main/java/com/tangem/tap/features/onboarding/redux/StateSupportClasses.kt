package com.tangem.tap.features.onboarding.redux

import com.tangem.tap.common.redux.navigation.AppScreen
import com.tangem.tap.domain.tasks.ScanNoteResponse
import com.tangem.tap.features.wallet.redux.Artwork
import java.math.BigDecimal

/**
 * Created by Anton Zhilenkov on 22/09/2021.
 */
data class OnboardingData(
    val scanNoteResponse: ScanNoteResponse,
    val cardArtwork: Artwork,
    val balance: BigDecimal?,
    val onboardingScreen: AppScreen
)

interface OnboardingStep

enum class OnboardingNoteStep : OnboardingStep {
    None, CreateWallet, TopUpWallet, Done
}

enum class OnboardingOtherStep : OnboardingStep {
    CreateWallet, Done
}