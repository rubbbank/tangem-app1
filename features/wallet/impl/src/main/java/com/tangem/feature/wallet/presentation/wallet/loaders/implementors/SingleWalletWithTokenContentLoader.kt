package com.tangem.feature.wallet.presentation.wallet.loaders.implementors

import com.tangem.core.deeplink.DeepLinksRegistry
import com.tangem.domain.appcurrency.GetSelectedAppCurrencyUseCase
import com.tangem.domain.promo.GetStoryContentUseCase
import com.tangem.domain.tokens.RunPolkadotAccountHealthCheckUseCase
import com.tangem.domain.wallets.models.UserWallet
import com.tangem.domain.wallets.usecase.ShouldSaveUserWalletsUseCase
import com.tangem.feature.wallet.child.wallet.model.intents.WalletClickIntents
import com.tangem.feature.wallet.presentation.wallet.analytics.utils.TokenListAnalyticsSender
import com.tangem.feature.wallet.presentation.wallet.analytics.utils.WalletWarningsAnalyticsSender
import com.tangem.feature.wallet.presentation.wallet.analytics.utils.WalletWarningsSingleEventSender
import com.tangem.feature.wallet.presentation.wallet.domain.GetMultiWalletWarningsFactory
import com.tangem.feature.wallet.presentation.wallet.domain.MultiWalletTokenListStore
import com.tangem.feature.wallet.presentation.wallet.domain.WalletWithFundsChecker
import com.tangem.feature.wallet.presentation.wallet.state.WalletStateController
import com.tangem.feature.wallet.presentation.wallet.subscribers.*

@Suppress("LongParameterList")
internal class SingleWalletWithTokenContentLoader(
    private val userWallet: UserWallet,
    private val clickIntents: WalletClickIntents,
    private val stateHolder: WalletStateController,
    private val tokenListAnalyticsSender: TokenListAnalyticsSender,
    private val walletWarningsAnalyticsSender: WalletWarningsAnalyticsSender,
    private val walletWarningsSingleEventSender: WalletWarningsSingleEventSender,
    private val walletWithFundsChecker: WalletWithFundsChecker,
    private val getMultiWalletWarningsFactory: GetMultiWalletWarningsFactory,
    private val tokenListStore: MultiWalletTokenListStore,
    private val getSelectedAppCurrencyUseCase: GetSelectedAppCurrencyUseCase,
    private val runPolkadotAccountHealthCheckUseCase: RunPolkadotAccountHealthCheckUseCase,
    private val shouldSaveUserWalletsUseCase: ShouldSaveUserWalletsUseCase,
    private val getStoryContentUseCase: GetStoryContentUseCase,
    private val deepLinksRegistry: DeepLinksRegistry,
) : WalletContentLoader(id = userWallet.walletId) {

    override fun create(): List<WalletSubscriber> {
        return buildList {
            SingleWalletWithTokenListSubscriber(
                userWallet = userWallet,
                stateHolder = stateHolder,
                clickIntents = clickIntents,
                tokenListAnalyticsSender = tokenListAnalyticsSender,
                walletWithFundsChecker = walletWithFundsChecker,
                tokenListStore = tokenListStore,
                getSelectedAppCurrencyUseCase = getSelectedAppCurrencyUseCase,
                runPolkadotAccountHealthCheckUseCase = runPolkadotAccountHealthCheckUseCase,
                deepLinksRegistry = deepLinksRegistry,
            ).let(::add)
            MultiWalletWarningsSubscriber(
                userWallet = userWallet,
                stateHolder = stateHolder,
                clickIntents = clickIntents,
                getMultiWalletWarningsFactory = getMultiWalletWarningsFactory,
                walletWarningsAnalyticsSender = walletWarningsAnalyticsSender,
                walletWarningsSingleEventSender = walletWarningsSingleEventSender,
            ).let(::add)
            MultiWalletActionButtonsSubscriber(
                userWallet = userWallet,
                stateHolder = stateHolder,
                getStoryContentUseCase = getStoryContentUseCase,
            ).let(::add)
            WalletDropDownItemsSubscriber(
                stateHolder = stateHolder,
                shouldSaveUserWalletsUseCase = shouldSaveUserWalletsUseCase,
                clickIntents = clickIntents,
            ).let(::add)
        }
    }
}
