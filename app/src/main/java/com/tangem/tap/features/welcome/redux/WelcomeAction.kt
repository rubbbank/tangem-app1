package com.tangem.tap.features.welcome.redux

import android.content.Intent
import com.tangem.common.core.TangemError
import org.rekotlin.Action

internal sealed interface WelcomeAction : Action {

    data class ProceedWithBiometrics(val afterUnlockIntent: Intent? = null) : WelcomeAction {
        object Success : WelcomeAction
        data class Error(val error: TangemError) : WelcomeAction
    }

    object ProceedWithCard : WelcomeAction {
        object Success : WelcomeAction
        data class Error(val error: TangemError) : WelcomeAction
        data class ChangeProgress(val showProgress: Boolean) : WelcomeAction
    }

    data class ProceedWithIntent(val intent: Intent) : WelcomeAction

    object CloseError : WelcomeAction

    object ClearUserWallets : WelcomeAction
}
