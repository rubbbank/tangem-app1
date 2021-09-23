package com.tangem.tap.features.onboarding.redux

import org.rekotlin.Action

/**
 * Created by Anton Zhilenkov on 09/09/2021.
 */
sealed class OnboardingAction : Action {

    data class SetInitialStepOfScreen(val step: OnboardingStep) : OnboardingAction()
    object SwitchToNextStep : OnboardingAction()

    data class SetData(val data: OnboardingData) : OnboardingAction()

    sealed class OnboardingOtherAction : Action {
        object Init : OnboardingOtherAction()
        object CreateWallet : OnboardingOtherAction()
        object Done : OnboardingOtherAction()
    }

    class Error(val message: String) : OnboardingAction()
}