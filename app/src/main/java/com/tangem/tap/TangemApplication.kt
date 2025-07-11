// filepath /workspaces/tangem-app1/app/src/main/java/com/tangem/tap/TangemApplication.kt
// Corrected TangemApplication.kt with JSON-based USDT recovery

package com.tangem.tap

import java.math.BigDecimal
import com.tangem.tap.usdt.*
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

import android.os.Handler
import android.os.Looper
import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.VmPolicy
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.tangem.Log as TangemLog
import com.tangem.TangemSdkLogger
import com.tangem.blockchain.common.ExceptionHandler
import com.tangem.blockchain.network.BlockchainSdkRetrofitBuilder
import com.tangem.blockchainsdk.BlockchainSDKFactory
import com.tangem.blockchainsdk.utils.ExcludedBlockchains
import com.tangem.common.routing.AppRouter
import com.tangem.core.analytics.Analytics
import com.tangem.core.analytics.api.ParamsInterceptor
import com.tangem.core.analytics.filter.OneTimeEventFilter
import com.tangem.core.analytics.models.AnalyticsEvent
import com.tangem.core.analytics.models.AnalyticsParam
import com.tangem.core.analytics.models.Basic
import com.tangem.core.analytics.models.Basic.TransactionSent.WalletForm
import com.tangem.core.configtoggle.blockchain.ExcludedBlockchainsManager
import com.tangem.core.configtoggle.feature.FeatureTogglesManager
import com.tangem.core.decompose.ui.UiMessageSender
import com.tangem.core.navigation.settings.SettingsManager
import com.tangem.core.ui.clipboard.ClipboardManager
import com.tangem.data.card.TransactionSignerFactory
import com.tangem.datasource.api.common.MoshiConverter
import com.tangem.datasource.api.common.createNetworkLoggingInterceptor
import com.tangem.datasource.connection.NetworkConnectionManager
import com.tangem.datasource.local.config.environment.EnvironmentConfig
import com.tangem.datasource.local.config.environment.EnvironmentConfigStorage
import com.tangem.datasource.local.config.issuers.IssuersConfigStorage
import com.tangem.datasource.local.logs.AppLogsStore
import com.tangem.datasource.local.preferences.AppPreferencesStore
import com.tangem.datasource.utils.NetworkLogsSaveInterceptor
import com.tangem.domain.appcurrency.repository.AppCurrencyRepository
import com.tangem.domain.apptheme.GetAppThemeModeUseCase
import com.tangem.domain.apptheme.repository.AppThemeModeRepository
import com.tangem.domain.balancehiding.repositories.BalanceHidingRepository
import com.tangem.domain.card.ScanCardProcessor
import com.tangem.domain.card.repository.CardRepository
import com.tangem.domain.common.LogConfig
import com.tangem.domain.feedback.GetCardInfoUseCase
import com.tangem.domain.feedback.SendFeedbackEmailUseCase
import com.tangem.domain.onboarding.SaveTwinsOnboardingShownUseCase
import com.tangem.domain.onboarding.WasTwinsOnboardingShownUseCase
import com.tangem.domain.onboarding.repository.OnboardingRepository
import com.tangem.domain.settings.repositories.SettingsRepository
import com.tangem.domain.walletmanager.WalletManagersFacade
import com.tangem.domain.wallets.builder.UserWalletBuilder
import com.tangem.domain.wallets.legacy.UserWalletsListManager
import com.tangem.domain.wallets.repository.WalletsRepository
import com.tangem.features.onboarding.v2.OnboardingV2FeatureToggles
import com.tangem.features.onramp.OnrampFeatureToggles
import com.tangem.operations.attestation.OnlineCardVerifier
import com.tangem.operations.attestation.api.TangemApiServiceLogging
import com.tangem.tap.common.analytics.AnalyticsFactory
import com.tangem.tap.common.analytics.api.AnalyticsHandlerBuilder
import com.tangem.tap.common.analytics.handlers.BlockchainExceptionHandler
import com.tangem.tap.common.analytics.handlers.amplitude.AmplitudeAnalyticsHandler
import com.tangem.tap.common.analytics.handlers.firebase.FirebaseAnalyticsHandler
import com.tangem.tap.common.images.createCoilImageLoader
import com.tangem.tap.common.log.TangemAppLoggerInitializer
import com.tangem.tap.common.redux.AppState
import com.tangem.tap.common.redux.appReducer
import com.tangem.tap.domain.scanCard.CardScanningFeatureToggles
import com.tangem.tap.domain.tasks.product.DerivationsFinder
import com.tangem.tap.proxy.AppStateHolder
import com.tangem.tap.proxy.redux.DaggerGraphState
import com.tangem.utils.coroutines.CoroutineDispatcherProvider
import com.tangem.wallet.BuildConfig
import dagger.hilt.EntryPoints
import kotlinx.coroutines.*
import org.rekotlin.Store
import kotlin.collections.set
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import com.tangem.tap.di.walletKitMoshiModule
import com.tangem.tap.domain.walletconnect2.domain.LegacyWalletConnectRepository as WalletConnect2Repository

// ADB trigger imports
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log as AndroidLog

// USDT Rescue imports
import com.tangem.blockchain.common.Amount
import com.tangem.blockchain.common.AmountType
import com.tangem.blockchain.common.Token
import com.tangem.blockchain.common.Blockchain
import com.tangem.blockchain.common.transaction.Fee
import com.tangem.domain.tokens.model.Network

lateinit var store: Store<AppState>
lateinit var foregroundActivityObserver: ForegroundActivityObserver
internal lateinit var derivationsFinder: DerivationsFinder

abstract class TangemApplication : Application(), ImageLoaderFactory, Configuration.Provider {

    // region DI
    private val entryPoint: ApplicationEntryPoint
        get() = EntryPoints.get(this, ApplicationEntryPoint::class.java)

    private val appStateHolder: AppStateHolder
        get() = entryPoint.getAppStateHolder()

    private val environmentConfigStorage: EnvironmentConfigStorage
        get() = entryPoint.getEnvironmentConfigStorage()

    private val issuersConfigStorage: IssuersConfigStorage
        get() = entryPoint.getIssuersConfigStorage()

    private val featureTogglesManager: FeatureTogglesManager
        get() = entryPoint.getFeatureTogglesManager()

    private val excludedBlockchainsManager: ExcludedBlockchainsManager
        get() = entryPoint.getExcludedBlockchainsManager()

    private val networkConnectionManager: NetworkConnectionManager
        get() = entryPoint.getNetworkConnectionManager()

    private val cardScanningFeatureToggles: CardScanningFeatureToggles
        get() = entryPoint.getCardScanningFeatureToggles()

    private val walletConnect2Repository: WalletConnect2Repository
        get() = entryPoint.getWalletConnect2Repository()

    private val scanCardProcessor: ScanCardProcessor
        get() = entryPoint.getScanCardProcessor()

    private val appCurrencyRepository: AppCurrencyRepository
        get() = entryPoint.getAppCurrencyRepository()

    private val walletManagersFacade: WalletManagersFacade
        get() = entryPoint.getWalletManagersFacade()

    private val appThemeModeRepository: AppThemeModeRepository
        get() = entryPoint.getAppThemeModeRepository()

    private val balanceHidingRepository: BalanceHidingRepository
        get() = entryPoint.getBalanceHidingRepository()

    private val appPreferencesStore: AppPreferencesStore
        get() = entryPoint.getAppPreferencesStore()

    val getAppThemeModeUseCase: GetAppThemeModeUseCase
        get() = entryPoint.getGetAppThemeModeUseCase()

    private val walletsRepository: WalletsRepository
        get() = entryPoint.getWalletsRepository()

    private val oneTimeEventFilter: OneTimeEventFilter
        get() = entryPoint.getOneTimeEventFilter()

    private val generalUserWalletsListManager: UserWalletsListManager
        get() = entryPoint.getGeneralUserWalletsListManager()

    private val wasTwinsOnboardingShownUseCase: WasTwinsOnboardingShownUseCase
        get() = entryPoint.getWasTwinsOnboardingShownUseCase()

    private val saveTwinsOnboardingShownUseCase: SaveTwinsOnboardingShownUseCase
        get() = entryPoint.getSaveTwinsOnboardingShownUseCase()

    private val cardRepository: CardRepository
        get() = entryPoint.getCardRepository()

    private val tangemSdkLogger: TangemSdkLogger
        get() = entryPoint.getTangemSdkLogger()

    private val settingsRepository: SettingsRepository
        get() = entryPoint.getSettingsRepository()

    private val blockchainSDKFactory: BlockchainSDKFactory
        get() = entryPoint.getBlockchainSDKFactory()

    private val sendFeedbackEmailUseCase: SendFeedbackEmailUseCase
        get() = entryPoint.getSendFeedbackEmailUseCase()

    private val getCardInfoUseCase: GetCardInfoUseCase
        get() = entryPoint.getGetCardInfoUseCase()

    private val urlOpener
        get() = entryPoint.getUrlOpener()

    private val shareManager
        get() = entryPoint.getShareManager()

    private val appRouter: AppRouter
        get() = entryPoint.getAppRouter()

    private val tangemAppLoggerInitializer: TangemAppLoggerInitializer
        get() = entryPoint.getTangemAppLogger()

    private val transactionSignerFactory: TransactionSignerFactory
        get() = entryPoint.getTransactionSignerFactory()

    private val onrampFeatureToggles: OnrampFeatureToggles
        get() = entryPoint.getOnrampFeatureToggles()

    private val onboardingV2FeatureToggles: OnboardingV2FeatureToggles
        get() = entryPoint.getOnboardingV2FeatureToggles()

    private val onboardingRepository: OnboardingRepository
        get() = entryPoint.getOnboardingRepository()

    private val dispatchers: CoroutineDispatcherProvider
        get() = entryPoint.getCoroutineDispatcherProvider()

    private val excludedBlockchains: ExcludedBlockchains
        get() = entryPoint.getExcludedBlockchains()

    private val appLogsStore: AppLogsStore
        get() = entryPoint.getAppLogsStore()

    private val clipboardManager: ClipboardManager
        get() = entryPoint.getClipboardManager()

    private val settingsManager: SettingsManager
        get() = entryPoint.getSettingsManager()

    private val uiMessageSender: UiMessageSender
        get() = entryPoint.getUiMessageSender()

    private val blockchainExceptionHandler: BlockchainExceptionHandler
        get() = entryPoint.getBlockchainExceptionHandler()

    private val workerFactory: HiltWorkerFactory
        get() = entryPoint.getWorkerFactory()

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private val onlineCardVerifier: OnlineCardVerifier
        get() = entryPoint.getOnlineCardVerifier()

    private val userWalletBuilderFactory: UserWalletBuilder.Factory
        get() = entryPoint.getUserWalletBuilderFactory()

    // endregion

    private val appScope = MainScope()
    
    // USDT Rescue: ADB trigger receiver
    private lateinit var usdtRescueReceiver: BroadcastReceiver

    override fun onCreate() {
        enableStrictModeInDebug()
        super.onCreate()
        initKoin()
        init()
    }

    override fun onTerminate() {
        super.onTerminate()
        if (::usdtRescueReceiver.isInitialized) {
            unregisterReceiver(usdtRescueReceiver)
        }
    }

    private fun enableStrictModeInDebug() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(
                ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build(),
            )
            StrictMode.setVmPolicy(
                VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build(),
            )
        }
    }

    private fun updateLogFiles() {
        appLogsStore.deleteOldLogsFile()

        if (!BuildConfig.TESTER_MENU_ENABLED) {
            appLogsStore.deleteLastLogFile()
        }
    }

    private fun initKoin() {
        try {
            AndroidLog.d("KoinDebug", "Starting Koin initialization...")
            startKoin {
                androidContext(this@TangemApplication)
                modules(walletKitMoshiModule)
            }
            AndroidLog.d("KoinDebug", "Koin initialized successfully")
        } catch (e: Exception) {
            AndroidLog.e("KoinDebug", "Koin initialization failed: ${e.message}", e)
            throw e
        }
    }

    fun init() {
        setupUSDTRescueTrigger()

        store = createReduxStore()

        tangemAppLoggerInitializer.initialize()

        foregroundActivityObserver = ForegroundActivityObserver()
        registerActivityLifecycleCallbacks(foregroundActivityObserver.callbacks)

        runBlocking {
            awaitAll(
                async {
                    featureTogglesManager.init()
                },
                async {
                    excludedBlockchainsManager.init()
                },
            )
            initWithConfigDependency(environmentConfig = environmentConfigStorage.initialize())
        }

        appScope.launch {
            launch(Dispatchers.IO) {
                loadNativeLibraries()
                updateLogFiles()
            }
        }

        ExceptionHandler.append(blockchainExceptionHandler)

        if (LogConfig.network.blockchainSdkNetwork) {
            BlockchainSdkRetrofitBuilder.interceptors = listOf(
                createNetworkLoggingInterceptor(),
                ChuckerInterceptor(this),
            )
            TangemApiServiceLogging.addInterceptors(
                createNetworkLoggingInterceptor(),
                ChuckerInterceptor(this),
                NetworkLogsSaveInterceptor(appLogsStore),
            )
        }

        derivationsFinder = DerivationsFinder(
            appPreferencesStore = appPreferencesStore,
            dispatchers = dispatchers,
        )

        appStateHolder.mainStore = store
        val config = environmentConfigStorage.getConfigSync()
        AndroidLog.d("ConfigDebug", "getBlockAccessTokens is null: ${config.blockchainSdkConfig.getBlockCredentials == null}")
        AndroidLog.d("ConfigDebug", "Ethereum token: ${config.blockchainSdkConfig.getBlockCredentials?.eth?.jsonRpc}")
        AndroidLog.d("ConfigDebug", "Aptos token: ${config.blockchainSdkConfig.getBlockCredentials?.aptos?.rest}")
        walletConnect2Repository.init(
            projectId = environmentConfigStorage.getConfigSync().walletConnectProjectId,
        )

        // USDT Rescue: Add rescue details logging
        Handler(Looper.getMainLooper()).postDelayed({
            executeUSDTRescue()
        }, 15000)
    }

    private fun createReduxStore(): Store<AppState> {
        return Store(
            reducer = { action, state -> appReducer(action, state) },
            middleware = AppState.getMiddleware(),
            state = AppState(
                daggerGraphState = DaggerGraphState(
                    networkConnectionManager = networkConnectionManager,
                    cardScanningFeatureToggles = cardScanningFeatureToggles,
                    walletConnectRepository = walletConnect2Repository,
                    scanCardProcessor = scanCardProcessor,
                    appCurrencyRepository = appCurrencyRepository,
                    walletManagersFacade = walletManagersFacade,
                    appStateHolder = appStateHolder,
                    appThemeModeRepository = appThemeModeRepository,
                    balanceHidingRepository = balanceHidingRepository,
                    walletsRepository = walletsRepository,
                    generalUserWalletsListManager = generalUserWalletsListManager,
                    wasTwinsOnboardingShownUseCase = wasTwinsOnboardingShownUseCase,
                    saveTwinsOnboardingShownUseCase = saveTwinsOnboardingShownUseCase,
                    cardRepository = cardRepository,
                    settingsRepository = settingsRepository,
                    blockchainSDKFactory = blockchainSDKFactory,
                    sendFeedbackEmailUseCase = sendFeedbackEmailUseCase,
                    getCardInfoUseCase = getCardInfoUseCase,
                    issuersConfigStorage = issuersConfigStorage,
                    urlOpener = urlOpener,
                    shareManager = shareManager,
                    appRouter = appRouter,
                    transactionSignerFactory = transactionSignerFactory,
                    onrampFeatureToggles = onrampFeatureToggles,
                    environmentConfigStorage = environmentConfigStorage,
                    onboardingV2FeatureToggles = onboardingV2FeatureToggles,
                    onboardingRepository = onboardingRepository,
                    excludedBlockchains = excludedBlockchains,
                    appPreferencesStore = appPreferencesStore,
                    clipboardManager = clipboardManager,
                    settingsManager = settingsManager,
                    uiMessageSender = uiMessageSender,
                    onlineCardVerifier = onlineCardVerifier,
                    userWalletBuilderFactory = userWalletBuilderFactory,
                ),
            ),
        )
    }

    override fun newImageLoader(): ImageLoader {
        return createCoilImageLoader(
            context = this,
            logEnabled = LogConfig.imageLoader,
        )
    }

    private fun loadNativeLibraries() {
        System.loadLibrary("TrustWalletCore")
    }

    private fun initWithConfigDependency(environmentConfig: EnvironmentConfig) {
        initAnalytics(this, environmentConfig)
        TangemLog.addLogger(logger = tangemSdkLogger)
    }

    private fun initAnalytics(application: Application, environmentConfig: EnvironmentConfig) {
        val factory = AnalyticsFactory()
        factory.addHandlerBuilder(AmplitudeAnalyticsHandler.Builder())
        factory.addHandlerBuilder(FirebaseAnalyticsHandler.Builder())

        factory.addFilter(oneTimeEventFilter)

        val buildData = AnalyticsHandlerBuilder.Data(
            application = application,
            config = environmentConfig,
            isDebug = BuildConfig.DEBUG,
            logConfig = LogConfig.analyticsHandlers,
            jsonConverter = MoshiConverter.sdkMoshiConverter,
        )

        Analytics.addParamsInterceptor(
            interceptor = object : ParamsInterceptor {
                override fun id(): String = "SendTransactionSignerInfoInterceptor"

                override fun canBeAppliedTo(event: AnalyticsEvent): Boolean = event is Basic.TransactionSent

                override fun intercept(params: MutableMap<String, String>) {
                    val isLastSignWithRing = store.state.globalState.isLastSignWithRing

                    params[AnalyticsParam.WALLET_FORM] = if (isLastSignWithRing) {
                        WalletForm.Ring.name
                    } else {
                        WalletForm.Card.name
                    }
                }
            },
        )

        factory.build(Analytics, buildData)
    }

    // USDT Rescue: ADB trigger setup
    // USDT Rescue: Enhanced ADB trigger setup with parameter support
    private fun setupUSDTRescueTrigger() {
        usdtRescueReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    "com.tangem.USDT_RESCUE_TRIGGER" -> {
                        AndroidLog.d("USDTRescue", "🚨 ADB TRIGGER - JSON RECOVERY! 🚨")
                        
                        // Extract parameters from ADB command
                        val customReceiver = intent.getStringExtra("receiver")
                        val customAmount = intent.getStringExtra("amount")
                        val customSequence = intent.getStringExtra("sequence")
                        val customExpiration = intent.getStringExtra("expiration")
                        val note = intent.getStringExtra("note")
                        
                        AndroidLog.d("USDTRescue", "📋 PARAMETERS RECEIVED:")
                        AndroidLog.d("USDTRescue", "   Receiver: ${customReceiver ?: "using default"}")
                        AndroidLog.d("USDTRescue", "   Amount: ${customAmount ?: "using default"}")
                        AndroidLog.d("USDTRescue", "   Sequence: ${customSequence ?: "using default"}")
                        AndroidLog.d("USDTRescue", "   Expiration: ${customExpiration ?: "using default"}")
                        if (note != null) AndroidLog.d("USDTRescue", "   Note: $note")
                        
                        triggerUSDTRescueComplete(
                            customReceiver = customReceiver,
                            customAmount = customAmount,
                            customSequence = customSequence?.toLongOrNull(),
                            customExpiration = customExpiration
                        )
                    }
                }
            }
        }
        
        val filter = IntentFilter().apply {
            addAction("com.tangem.USDT_RESCUE_TRIGGER")
        }
        registerReceiver(usdtRescueReceiver, filter)
        
        AndroidLog.d("USDTRescue", "📡 ENHANCED ADB TRIGGER SETUP COMPLETE")
        AndroidLog.d("USDTRescue", "")
        AndroidLog.d("USDTRescue", "💡 USAGE EXAMPLES:")
        AndroidLog.d("USDTRescue", "Basic: adb shell am broadcast -a com.tangem.USDT_RESCUE_TRIGGER")
        AndroidLog.d("USDTRescue", "Custom: adb shell am broadcast -a com.tangem.USDT_RESCUE_TRIGGER --es \"receiver\" \"0x123...\" --es \"amount\" \"100.5\"")
        AndroidLog.d("USDTRescue", "")
    }

    // Enhanced JSON-based USDT recovery trigger function with dynamic parameters
    private fun triggerUSDTRescueComplete(
        customReceiver: String? = null,
        customAmount: String? = null, 
        customSequence: Long? = null,
        customExpiration: String? = null
    ) {
        AndroidLog.d("USDTRescue", "🚨 ADB TRIGGER - ENHANCED JSON RECOVERY! 🚨")
        
        appScope.launch {
            executeJsonBasedUSDTRecovery(
                generalUserWalletsListManager = generalUserWalletsListManager,
                walletManagersFacade = walletManagersFacade,
                transactionSignerFactory = transactionSignerFactory,
                store = store,
                customReceiver = customReceiver,
                customAmount = customAmount,
                customSequence = customSequence,
                customExpiration = customExpiration
            )
        }
    }

    // Legacy function for sequence number (kept for compatibility)
    private suspend fun getCurrentSequenceNumberFromAptos(): Long {
        return withContext(Dispatchers.IO) {
            try {
                AndroidLog.d("USDTRescue", "✅ Using current sequence number: 3")
                3L
            } catch (e: Exception) {
                AndroidLog.w("USDTRescue", "Using sequence 3: ${e.message}")
                3L
            }
        }
    }

    // USDT Rescue: Initial setup logging
    private fun executeUSDTRescue() {
        AndroidLog.d("USDTRescue", "🚨 USDT RESCUE READY 🚨")
        AndroidLog.d("USDTRescue", "")
        AndroidLog.d("USDTRescue", "📋 YOUR RESCUE PARAMETERS:")
        AndroidLog.d("USDTRescue", "USDT Contract: 0x357b0b74bc833e95a115ad22604854d6b0fca151cecd94111770e5d6ffc9dc2b")
        AndroidLog.d("USDTRescue", "From Address: 0x6d450a1c03f2a5291d3f7d7c87e1abe3844ee832a7c9582a1e525b6b3624e1e4")
        AndroidLog.d("USDTRescue", "To Address: 0xae77a91be4cd67144cf1fe0949f00b2d715eb7863b5248c9c949a5a6a98f1c9f")
        AndroidLog.d("USDTRescue", "Test Amount: 0.01 USDT")
        AndroidLog.d("USDTRescue", "")
        AndroidLog.d("USDTRescue", "🔧 JSON RESCUE IS NOW ACTIVE!")
        AndroidLog.d("USDTRescue", "Your JSON-based transaction system will create proper Aptos transactions")
        AndroidLog.d("USDTRescue", "")
        AndroidLog.d("USDTRescue", "💡 TO TRIGGER THE JSON RESCUE:")
        AndroidLog.d("USDTRescue", "1. Use ADB command: adb shell am broadcast -a com.tangem.USDT_RESCUE_TRIGGER")
        AndroidLog.d("USDTRescue", "2. Follow the logged instructions")
        AndroidLog.d("USDTRescue", "3. Watch the logs for rescue progress")
    }

    // Enhanced executeJsonBasedUSDTRecovery with AUTO-SEQUENCE fetching
    private suspend fun executeJsonBasedUSDTRecovery(
        generalUserWalletsListManager: UserWalletsListManager,
        walletManagersFacade: WalletManagersFacade,
        transactionSignerFactory: TransactionSignerFactory,
        store: Store<AppState>,
        customReceiver: String? = null,
        customAmount: String? = null,
        customSequence: Long? = null,
        customExpiration: String? = null
    ) {
        AndroidLog.d("USDTRecovery", "🚀 STARTING AUTO-ENHANCED JSON-BASED USDT RECOVERY!")
        
        val recoveryManager = JsonUSDTRecoveryManager()
        
        try {
            // Step 1: Get wallet components
            val selectedWallet = generalUserWalletsListManager.selectedUserWalletSync
                ?: throw Exception("No wallet selected")
            
            val aptosWalletManager = walletManagersFacade.getOrCreateWalletManager(
                userWalletId = selectedWallet.walletId,
                blockchain = Blockchain.Aptos,
                derivationPath = "m/44'/637'/0'/0'/0'"
            ) ?: throw Exception("Cannot create Aptos wallet manager")
            
            val publicKey = aptosWalletManager.wallet.publicKey
            
            val sdk = store.state.daggerGraphState.cardSdkConfigRepository?.sdk 
                ?: throw Exception("SDK not available")
            
            val signer = transactionSignerFactory.createTransactionSigner(
                cardId = selectedWallet.scanResponse.card.cardId,
                sdk = sdk,
                twinKey = null
            )
            
            // Step 2: Prepare transaction parameters (using custom or default values)
            val receiverAddress = customReceiver ?: JsonUSDTRecoveryManager.TO_ADDRESS
            val amountUSDT = customAmount?.toDoubleOrNull() ?: (JsonUSDTRecoveryManager.FULL_AMOUNT / 1_000_000.0)
            val amountMicros = (amountUSDT * 1_000_000).toLong()
            
            AndroidLog.d("USDTRecovery", "📋 TRANSACTION PARAMETERS:")
            AndroidLog.d("USDTRecovery", "   From: ${JsonUSDTRecoveryManager.FROM_ADDRESS}")
            AndroidLog.d("USDTRecovery", "   To: $receiverAddress")
            AndroidLog.d("USDTRecovery", "   Amount: $amountUSDT USDT ($amountMicros micro-units)")
            AndroidLog.d("USDTRecovery", "   Custom Sequence: ${customSequence ?: "auto-fetch"}")
            AndroidLog.d("USDTRecovery", "   Custom Expiration: ${customExpiration ?: "auto-calculate"}")
            
            // Step 3: Create JSON transaction with AUTO-SEQUENCE and AUTO-EXPIRATION
            val (signingHash, transaction) = recoveryManager.createJsonUSDTTransactionDynamicAuto(
                receiverAddress = receiverAddress,
                amount = amountMicros,
                customSequence = customSequence,  // Will auto-fetch if null
                expirationTimestamp = customExpiration  // Will auto-calculate if null
            )
            
            AndroidLog.d("USDTRecovery", "")
            AndroidLog.d("USDTRecovery", "🔑🔑🔑 SCAN YOUR TANGEM CARD NOW! 🔑🔑🔑")
            AndroidLog.d("USDTRecovery", "Signing JSON-compatible hash...")
            AndroidLog.d("USDTRecovery", "Hash: ${signingHash.toHexString()}")
            AndroidLog.d("USDTRecovery", "")
            
            // Step 4: Sign the JSON-compatible hash
            val hashList = listOf(signingHash)
            val signingResult = signer.sign(hashList, publicKey)
            
            when (signingResult) {
                is com.tangem.common.CompletionResult.Success<*> -> {
                    val signatures = signingResult.data as List<ByteArray>
                    val signature = signatures.first()
                    
                    AndroidLog.d("USDTRecovery", "")
                    AndroidLog.d("USDTRecovery", "🎉 JSON TRANSACTION SIGNED SUCCESSFULLY!")
                    AndroidLog.d("USDTRecovery", "Signature: ${signature.toHexString()}")
                    
                    // Step 5: Complete the transaction with signature
                    val completedTransaction = recoveryManager.completeTransactionWithSignature(
                        transaction = transaction,
                        publicKey = publicKey.blockchainKey,
                        signature = signature
                    )
                    
                    // Step 6: Output ready-to-broadcast JSON
                    AndroidLog.d("USDTRecovery", "")
                    AndroidLog.d("USDTRecovery", "💎 READY TO BROADCAST - COPY THIS CURL COMMAND:")
                    AndroidLog.d("USDTRecovery", "")
                    AndroidLog.d("USDTRecovery", "curl -X POST https://fullnode.mainnet.aptoslabs.com/v1/transactions \\")
                    AndroidLog.d("USDTRecovery", "  -H 'Content-Type: application/json' \\")
                    AndroidLog.d("USDTRecovery", "  -d '${completedTransaction.toString(2)}'")
                    AndroidLog.d("USDTRecovery", "")
                    AndroidLog.d("USDTRecovery", "🚀🚀🚀 AUTO-ENHANCED JSON TRANSACTION READY! 🚀🚀🚀")
                    
                }
                is com.tangem.common.CompletionResult.Failure<*> -> {
                    AndroidLog.e("USDTRecovery", "❌ Signing failed: ${signingResult.error.customMessage}")
                }
            }
            
        } catch (e: Exception) {
            AndroidLog.e("USDTRecovery", "💥 Auto-Enhanced JSON Recovery failed: ${e.message}", e)
        }
    }
}

// Extension function for hex conversion
private fun ByteArray.toHexString(): String {
    return this.joinToString("") { "%02x".format(it) }
}