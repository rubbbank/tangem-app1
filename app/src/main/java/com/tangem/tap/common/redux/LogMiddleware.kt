package com.tangem.tap.common.redux

import com.tangem.tap.store
import org.rekotlin.Middleware
import timber.log.Timber

/**
 * Created by Anton Zhilenkov on 31/08/2020.
 */
val logMiddleware: Middleware<AppState> = { dispatch, appState ->
    { nextDispatch ->
        { action ->
            if (store.state.domainState.globalState.logConfig.storeAction) {
                Timber.d("Dispatch action: $action")
            }
            nextDispatch(action)
        }
    }
}
