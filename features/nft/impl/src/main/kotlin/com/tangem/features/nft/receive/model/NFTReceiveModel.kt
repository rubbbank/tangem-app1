package com.tangem.features.nft.receive.model

import com.tangem.core.analytics.api.AnalyticsEventHandler
import com.tangem.core.decompose.di.ModelScoped
import com.tangem.core.decompose.model.Model
import com.tangem.core.decompose.model.ParamsContainer
import com.tangem.core.decompose.ui.UiMessageSender
import com.tangem.core.navigation.share.ShareManager
import com.tangem.core.ui.clipboard.ClipboardManager
import com.tangem.core.ui.components.fields.InputManager
import com.tangem.core.ui.components.fields.entity.SearchBarUM
import com.tangem.core.ui.extensions.resourceReference
import com.tangem.core.ui.extensions.wrappedList
import com.tangem.core.ui.message.DialogMessage
import com.tangem.domain.nft.FilterNFTAvailableNetworksUseCase
import com.tangem.domain.nft.GetNFTNetworksUseCase
import com.tangem.domain.nft.GetNFTNetworkStatusUseCase
import com.tangem.domain.nft.analytics.NFTAnalyticsEvent
import com.tangem.domain.tokens.model.Network
import com.tangem.domain.tokens.model.NetworkStatus
import com.tangem.features.nft.impl.R
import com.tangem.features.nft.receive.NFTReceiveComponent
import com.tangem.features.nft.receive.entity.NFTReceiveUM
import com.tangem.features.nft.receive.entity.transformer.ShowReceiveBottomSheetTransformer
import com.tangem.features.nft.receive.entity.transformer.ToggleSearchBarTransformer
import com.tangem.features.nft.receive.entity.transformer.UpdateDataStateTransformer
import com.tangem.features.nft.receive.entity.transformer.UpdateSearchQueryTransformer
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@Suppress("LongParameterList")
@ModelScoped
internal class NFTReceiveModel @Inject constructor(
    override val dispatchers: CoroutineDispatcherProvider,
    private val searchManager: InputManager,
    private val getNFTNetworksUseCase: GetNFTNetworksUseCase,
    private val filterNFTAvailableNetworksUseCase: FilterNFTAvailableNetworksUseCase,
    private val getNFTNetworkStatusUseCase: GetNFTNetworkStatusUseCase,
    private val clipboardManager: ClipboardManager,
    private val shareManager: ShareManager,
    private val analyticsEventHandler: AnalyticsEventHandler,
    private val messageSender: UiMessageSender,
    paramsContainer: ParamsContainer,
) : Model() {

    private val params: NFTReceiveComponent.Params = paramsContainer.require()

    private val _state = MutableStateFlow(
        value = NFTReceiveUM(
            onBackClick = params.onBackClick,
            appBarSubtitle = resourceReference(
                R.string.hot_crypto_add_token_subtitle,
                formatArgs = wrappedList(params.walletName),
            ),
            search = getInitialSearchBar(),
            networks = NFTReceiveUM.Networks.Content(
                availableItems = persistentListOf(),
                unavailableItems = persistentListOf(),
            ),
            bottomSheetConfig = null,
        ),
    )

    val state: StateFlow<NFTReceiveUM> get() = _state

    init {
        analyticsEventHandler.send(NFTAnalyticsEvent.Receive.ScreenOpened)
        subscribeToNFTAvailableNetworks()
    }

    private fun subscribeToNFTAvailableNetworks() {
        combine(
            flow = getNFTNetworksUseCase(params.userWalletId),
            flow2 = searchManager.query.distinctUntilChanged(),
        ) { networks, query ->
            filterNFTAvailableNetworksUseCase(networks, query)
        }
            .onEach { filteredNetworks ->
                _state.update {
                    UpdateDataStateTransformer(
                        networks = filteredNetworks,
                        onNetworkClick = ::onNetworkClick,
                    ).transform(it)
                }
            }
            .launchIn(modelScope)
    }

    private fun onSearchQueryChange(newQuery: String) {
        modelScope.launch {
            _state.update { UpdateSearchQueryTransformer(newQuery).transform(it) }

            searchManager.update(newQuery)
        }
    }

    private fun getInitialSearchBar(): SearchBarUM = SearchBarUM(
        placeholderText = resourceReference(R.string.common_search),
        query = "",
        isActive = false,
        onQueryChange = ::onSearchQueryChange,
        onActiveChange = ::toggleSearchBar,
    )

    private fun toggleSearchBar(isActive: Boolean) {
        _state.update {
            ToggleSearchBarTransformer(isActive).transform(it)
        }
    }

    private fun onReceiveBottomSheetDismiss() {
        _state.update {
            it.copy(
                bottomSheetConfig = it.bottomSheetConfig?.copy(isShown = false),
            )
        }
    }

    private fun onNetworkClick(network: Network, enabled: Boolean) {
        if (!enabled) {
            val message = DialogMessage(
                title = resourceReference(R.string.nft_receive_unavailable_asset_warning_title),
                message = resourceReference(R.string.nft_receive_unavailable_asset_warning_message),
            )

            messageSender.send(message)
        } else {
            modelScope.launch {
                analyticsEventHandler.send(NFTAnalyticsEvent.Receive.BlockchainChosen(network.name))

                val networkStatus = getNFTNetworkStatusUseCase.invoke(
                    userWalletId = params.userWalletId,
                    network = network,
                ) ?: return@launch

                when (val value = networkStatus.value) {
                    is NetworkStatus.Verified -> {
                        _state.update {
                            ShowReceiveBottomSheetTransformer(
                                network = network,
                                networkAddress = value.address,
                                onDismissBottomSheet = ::onReceiveBottomSheetDismiss,
                                onCopyClick = { text -> onCopyClick(text, network) },
                                onShareClick = { text -> onShareClick(text, network) },
                            ).transform(it)
                        }
                    }
                    is NetworkStatus.MissedDerivation,
                    is NetworkStatus.NoAccount,
                    is NetworkStatus.Unreachable,
                    -> Unit
                }
            }
        }
    }

    private fun onCopyClick(text: String, network: Network) {
        analyticsEventHandler.send(NFTAnalyticsEvent.Receive.CopyAddress(network.name))
        clipboardManager.setText(text = text, isSensitive = true)
    }

    private fun onShareClick(text: String, network: Network) {
        analyticsEventHandler.send(NFTAnalyticsEvent.Receive.ShareAddress(network.name))
        shareManager.shareText(text = text)
    }
}
