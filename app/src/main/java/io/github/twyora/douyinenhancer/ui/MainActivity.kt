package io.github.twyora.douyinenhancer.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.scene.DialogSceneStrategy
import androidx.navigation3.ui.NavDisplay
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.ui.content.attest.VerifyDialogContent
import io.github.twyora.douyinenhancer.ui.content.attest.VerifyDialogUiActions
import io.github.twyora.douyinenhancer.ui.content.attest.VerifyDialogViewModel
import io.github.twyora.douyinenhancer.ui.content.attest.VerifyDialogViewModelFactory
import io.github.twyora.douyinenhancer.ui.content.home.HomeContent
import io.github.twyora.douyinenhancer.ui.content.home.HomeContentUiActions
import io.github.twyora.douyinenhancer.ui.content.home.HomeContentViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContent
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentConfigActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentFeedActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentModuleActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentSaveActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentUiActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterContent
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterCountActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterDescKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterNicknameKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterSwitchActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterTitleKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterUidKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed.RecommendedFeedFilterViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeBottomTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityContent
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTopTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityContext
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityViewModel
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import io.github.twyora.douyinenhancer.ui.navigation.AppRoutes
import io.github.twyora.douyinenhancer.utils.android.openUrl
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.serializer

class MainActivity : ComponentActivity() {
    private val navFlow = MutableSharedFlow<NavKey>(
        extraBufferCapacity = 1,
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    private var pendingNavKey: NavKey? = null

    private val uriDeepLinkMatchers = listOf(
        UriDeepLinkMatcher(
            DeepLinkUri("douyinenhancer://io.github.twyora.douyinenhancer/home"),
            serializer = serializer<AppRoutes.Home>()
        ),
        UriDeepLinkMatcher(
            DeepLinkUri("douyinenhancer://io.github.twyora.douyinenhancer/settings/main"),
            serializer = serializer<AppRoutes.Settings.Main>()
        ),
        UriDeepLinkMatcher(
            DeepLinkUri("douyinenhancer://io.github.twyora.douyinenhancer/settings/recommended-feed-filter"),
            serializer = serializer<AppRoutes.Settings.RecommendedFeedFilter>()
        ),
        UriDeepLinkMatcher(
            DeepLinkUri("douyinenhancer://io.github.twyora.douyinenhancer/settings/hometab"),
            serializer = serializer<AppRoutes.Settings.HomeTab>()
        ),
        UriDeepLinkMatcher(
            DeepLinkUri("douyinenhancer://io.github.twyora.douyinenhancer/settings/playback-component"),
            serializer = serializer<AppRoutes.Settings.PlaybackComponent>()
        )
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        intent.data?.let {
            val req = DeepLinkRequest(it)
            uriDeepLinkMatchers.mapNotNull { matcher ->
                matcher.match(req)
            }.maxOrNull()?.let { matchResult ->
                navFlow.tryEmit(matchResult.key)
            }
        }

        setContent {
            val currLocalContext = LocalContext.current
            val dark = isSystemInDarkTheme()
            MaterialTheme(
                colorScheme = remember(dark) {
                    val dynamic = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                    when {
                        dynamic && dark -> dynamicDarkColorScheme(currLocalContext)
                        dynamic -> dynamicLightColorScheme(currLocalContext)
                        dark -> darkColorScheme()
                        else -> lightColorScheme()
                    }
                }
            ) {
                val backstack = rememberNavBackStack(AppRoutes.Home)

                val snackbarHostState = remember {
                    SnackbarHostState()
                }
                val snackbarResources = LocalResources.current
                val snackbarScope = rememberCoroutineScope()
                val onShowSnackbar: (SnackEvent) -> Unit = remember(snackbarHostState, snackbarResources, snackbarScope) {
                    { event ->
                        val message = if (event.args.isEmpty()) {
                            snackbarResources.getString(event.resId)
                        } else {
                            snackbarResources.getString(event.resId, *event.args.toTypedArray())
                        }
                        snackbarScope.launch {
                            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
                        }
                    }
                }

                val langCode = LocalConfiguration.current.locales[0].language
                LaunchedEffect(langCode) {
                    navFlow.collect { navKey ->
                        if (backstack.lastOrNull() is AppRoutes.Verification) {
                            return@collect
                        }

                        val shouldVerify = !(
                            BuildConfig.DEBUG ||
                                (langCode != Locale.CHINESE.language) ||
                                ConfigManager.module.lastVerifiedVersion.value == BuildConfig.VERSION_CODE
                            )

                        val target = if (navKey is AppRoutes.Settings && shouldVerify) {
                            YLog.info("verification required, redirecting for verification")

                            pendingNavKey = navKey
                            AppRoutes.Verification
                        } else {
                            navKey
                        }
                        backstack.add(target)
                        navFlow.resetReplayCache()
                    }
                }

                NavDisplay(
                    backStack = backstack,
                    entryProvider = { key ->
                        when (key) {
                            AppRoutes.Home,
                            AppRoutes.Settings.Main,
                            AppRoutes.Settings.RecommendedFeedFilter,
                            AppRoutes.Settings.PlaybackComponent,
                            AppRoutes.Settings.HomeTab -> NavEntry(key) {
                                Scaffold(
                                    snackbarHost = {
                                        SnackbarHost(snackbarHostState)
                                    }
                                ) { innerPadding ->
                                    when (key) {
                                        AppRoutes.Home -> HomeScreen(
                                            onNavigate = {
                                                navFlow.tryEmit(it)
                                            },
                                            innerPadding = innerPadding,
                                            onShowSnackbar = onShowSnackbar
                                        )

                                        AppRoutes.Settings.Main -> SettingsScreen(
                                            onNavigate = {
                                                navFlow.tryEmit(it)
                                            },
                                            innerPadding = innerPadding,
                                            onShowSnackbar = onShowSnackbar
                                        )

                                        AppRoutes.Settings.RecommendedFeedFilter -> RecommendedFeedFilterScreen(
                                            innerPadding = innerPadding,
                                            onShowSnackbar = onShowSnackbar
                                        )

                                        AppRoutes.Settings.PlaybackComponent -> PlaybackComponentVisibilityScreen(
                                            innerPadding = innerPadding
                                        )

                                        AppRoutes.Settings.HomeTab -> HomeTabVisibilityScreen(
                                            innerPadding = innerPadding
                                        )

                                        else -> {
                                        }
                                    }
                                }
                            }

                            AppRoutes.Verification -> NavEntry(
                                key,
                                metadata = DialogSceneStrategy.dialog() +
                                    NavDisplay.transitionSpec {
                                        fadeIn(tween(220)) togetherWith ExitTransition.None
                                    } +
                                    NavDisplay.popTransitionSpec {
                                        EnterTransition.None togetherWith fadeOut(tween(220))
                                    }
                            ) {
                                VerifyDialogScreen(
                                    onVerified = {
                                        pendingNavKey?.let {
                                            pendingNavKey = null
                                            lifecycleScope.launch(Dispatchers.IO) {
                                                ConfigManager.module.lastVerifiedVersion.value = BuildConfig.VERSION_CODE
                                                withContext(Dispatchers.Main) {
                                                    backstack.remove(AppRoutes.Verification)
                                                    navFlow.tryEmit(it)
                                                }
                                            }
                                        }
                                    },
                                    onDismiss = {
                                        pendingNavKey = null
                                        backstack.remove(AppRoutes.Verification)
                                    },
                                    onShowSnackbar = onShowSnackbar
                                )
                            }

                            else -> NavEntry(key) {
                            }
                        }
                    },
                    sceneStrategies = listOf(DialogSceneStrategy()),
                    // Animation referenced from:
                    // [fdroidclient](https://github.com/f-droid/fdroidclient/blob/master/app/src/main/kotlin/org/fdroid/ui/MainContent.kt)
                    transitionSpec = {
                        slideInHorizontally {
                            it
                        } togetherWith
                            slideOutHorizontally {
                                -it / 3
                            } + scaleOut(targetScale = 0.9f) + fadeOut(tween())
                    },
                    popTransitionSpec = {
                        slideInHorizontally {
                            -it / 3
                        } + scaleIn(
                            initialScale = 0.9f
                        ) + fadeIn(
                            tween()
                        ) togetherWith slideOutHorizontally {
                            it / 3
                        } + fadeOut(tween())
                    },
                    predictivePopTransitionSpec = {
                        slideInHorizontally {
                            -it / 3
                        } + scaleIn(
                            initialScale = 0.9f
                        ) + fadeIn(
                            tween()
                        ) togetherWith slideOutHorizontally {
                            it / 3
                        } + fadeOut(tween())
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.data?.let {
            val req = DeepLinkRequest(it)
            uriDeepLinkMatchers.mapNotNull { matcher ->
                matcher.match(req)
            }.maxOrNull()?.let { matchResult ->
                navFlow.tryEmit(matchResult.key)
            }
        }
    }

    @Composable
    fun HomeScreen(
        modifier: Modifier = Modifier,
        onNavigate: (NavKey) -> Unit,
        onShowSnackbar: (SnackEvent) -> Unit,
        innerPadding: PaddingValues
    ) {
        val mainVm: HomeContentViewModel = viewModel()
        val mainUiState by mainVm.uiState.collectAsStateWithLifecycle()
        val currLocalContext = LocalContext.current
        val currLocalResources = LocalResources.current

        LaunchedEffect(Unit) {
            mainVm.snackFlow.conflate().collect { event ->
                onShowSnackbar(event)
            }
        }

        LaunchedEffect(Unit) {
            mainVm.triggerOpenUrl.collect { resId ->
                currLocalContext.openUrl(currLocalResources.getString(resId))
            }
        }

        LaunchedEffect(Unit) {
            mainVm.triggerNavigation.collect {
                onNavigate(it)
            }
        }

        val uiActions = remember {
            HomeContentUiActions(
                onLauncherIconChange = {
                    mainVm.setLauncherIconHidden(it)
                },
                onOpenHelp = {
                    mainVm.requestOpenUrl(R.string.docs_url)
                },
                onOpenAuthor = {
                    mainVm.requestOpenUrl(R.string.author_url)
                },
                onOpenRepository = {
                    mainVm.requestOpenUrl(R.string.repository_url)
                },
                onViewRelease = {
                    mainVm.requestOpenUrl(R.string.latest_release_page_url)
                },
                onVersionClick = {
                    mainVm.onVersionClick()
                },
                onJoinTelegramGroup = {
                    mainVm.requestOpenUrl(R.string.telegram_url)
                },
                onNavigateToSettings = {
                    if (mainUiState.moduleActivationState) {
                        mainVm.requestNavigation(AppRoutes.Settings.Main)
                    } else {
                        mainVm.sendSnack(
                            SnackEvent(R.string.pref_about_activation_status_deactivated_summary)
                        )
                    }
                }
            )
        }
        HomeContent(
            uiState = mainUiState,
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            uiActions = uiActions
        )
    }

    @Composable
    private fun SettingsScreen(
        modifier: Modifier = Modifier,
        onNavigate: (NavKey) -> Unit,
        onShowSnackbar: (SnackEvent) -> Unit,
        innerPadding: PaddingValues
    ) {
        val vm: SettingsContentViewModel = viewModel()
        val settingsConfig by vm.settingsConfig.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            vm.triggerNavigation.collect {
                onNavigate(it)
            }
        }

        LaunchedEffect(Unit) {
            vm.snackFlow.conflate().collect { event ->
                onShowSnackbar(event)
            }
        }

        val scope = rememberCoroutineScope()

        val exportKVConfigLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.CreateDocument("application/zip")
        ) { uri ->
            uri?.let { uri ->
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        val exportStream = requireNotNull(applicationContext.contentResolver.openOutputStream(uri)) {
                            "cannot open output uri"
                        }
                        exportStream.use { stream ->
                            val storages = listOf(ConfigManager.settingsStorage)
                            val tempZip = File.createTempFile("douyinenhancer_kv_export_", "", applicationContext.cacheDir)
                            try {
                                vm.exportConfigZip(storages, tempZip.outputStream())
                                tempZip.inputStream().use { tempZipOut ->
                                    tempZipOut.copyTo(stream)
                                }
                            } finally {
                                tempZip.delete()
                            }
                        }
                    }.onFailure { e ->
                        YLog.error("$TAG: export config failed", e)
                        vm.sendSnack(SnackEvent(R.string.config_export_failed, listOf(e.message ?: e.toString())))
                    }.onSuccess {
                        vm.sendSnack(SnackEvent(R.string.config_export_success))
                    }
                }
            }
        }

        val importKVConfigLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { uri ->
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        val zipInputStream = requireNotNull(applicationContext.contentResolver.openInputStream(uri)) {
                            "empty import"
                        }
                        zipInputStream.use { zipIn ->
                            val tempDir =
                                Files.createTempDirectory(applicationContext.cacheDir.toPath(), "douyinenhancer_kv_import_").toFile()
                            try {
                                ZipInputStream(zipIn).use { zip ->
                                    generateSequence {
                                        zip.nextEntry
                                    }.forEach { entry ->
                                        File(tempDir, entry.name).writeBytes(zip.readBytes())
                                        zip.closeEntry()
                                    }
                                }

                                val extractedFiles = tempDir.listFiles()?.toList().orEmpty()
                                when {
                                    extractedFiles.any { file ->
                                        file.name == "checksum"
                                    } -> {
                                        vm.importV1ConfigZip(
                                            storages = listOf(ConfigManager.settingsStorage),
                                            checksumFile = extractedFiles.first { file ->
                                                file.name == "checksum"
                                            },
                                            storageDir = tempDir
                                        )
                                    }

                                    extractedFiles.any { file ->
                                        file.name == "manifest.json"
                                    } -> {
                                        vm.importConfigZip(
                                            listOf(ConfigManager.settingsStorage),
                                            inputFiles = extractedFiles.filter { file ->
                                                file.name != "manifest.json"
                                            },
                                            manifestFile = extractedFiles.first { file ->
                                                file.name == "manifest.json"
                                            }
                                        )
                                    }

                                    else -> {
                                        YLog.error("$TAG: unrecognized config archive")
                                        vm.sendSnack(
                                            SnackEvent(
                                                R.string.config_import_failed,
                                                listOf("unrecognized config archive format")
                                            )
                                        )
                                        return@launch
                                    }
                                }
                            } finally {
                                tempDir.deleteRecursively()
                            }
                        }
                    }.onFailure { e ->
                        YLog.error("$TAG: import config failed", e)
                        vm.sendSnack(SnackEvent(R.string.config_import_failed, listOf(e.message ?: e.toString())))
                    }.onSuccess {
                        vm.sendSnack(SnackEvent(R.string.config_import_success))
                    }
                }
            }
        }

        val loadMappingLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            uri?.let { uri ->
                scope.launch(Dispatchers.IO) {
                    runCatching {
                        val hookInfoInputStream = requireNotNull(applicationContext.contentResolver.openInputStream(uri)) {
                            "custom hook info file input stream is null"
                        }
                        hookInfoInputStream.use { hookInfoStream ->
                            vm.loadCustomHookInfo(hookInfoStream)
                        }
                    }.onFailure { e ->
                        YLog.error("$TAG: load custom hook info failed", e)
                        vm.sendSnack(SnackEvent(R.string.import_failed, listOf(e.message ?: e.toString())))
                    }
                }
            }
        }

        LaunchedEffect(Unit) {
            vm.triggerExportConfig.collect {
                exportKVConfigLauncher.launch(
                    "douyinenhancer_backup_${
                        SimpleDateFormat("yyMMdd-HHmmss", Locale.getDefault()).format(Date())
                    }.zip"
                )
            }
        }

        LaunchedEffect(Unit) {
            vm.triggerImportConfig.collect {
                importKVConfigLauncher.launch("application/zip")
            }
        }

        LaunchedEffect(Unit) {
            vm.triggerLoadCustomMapping.collect {
                loadMappingLauncher.launch("application/json")
            }
        }

        val saveActions = remember {
            SettingsContentSaveActions(
                onPurifyCommentImageChange = {
                    vm.setPurifyCommentImage(it)
                },
                onUnlockCommentEmojiChange = {
                    vm.setUnlockCommentEmoji(it)
                },
                onDownloadCommentAudioChange = {
                    vm.setDownloadCommentAudio(it)
                },
                onRemoveVideoWatermarkChange = {
                    vm.setRemoveVideoWatermark(it)
                },
                onBypassVideoDownloadChange = {
                    vm.setBypassVideoDownload(it)
                },
                onRemoveMultiImageWatermarkChange = {
                    vm.setRemoveMultiImageWatermark(it)
                }
            )
        }
        val feedActions = remember {
            SettingsContentFeedActions(
                onNavigateToRecommendedFeedFilter = {
                    vm.requestNavigation(AppRoutes.Settings.RecommendedFeedFilter)
                },
                onDisableDoubleTapDiggChange = {
                    vm.setDisableDoubleTapDigg(it)
                },
                onDoubleTabOpenCommentChange = {
                    vm.setDoubleTapOpenComment(it)
                },
                onBypassListenAwemeChange = {
                    vm.setBypassListenAweme(it)
                },
                onBlockAutoReplayChange = {
                    vm.setBlockAutoReplay(it)
                },
                onBlockResumePlaybackChange = {
                    vm.setBlockResumePlayback(it)
                }
            )
        }
        val uiActions = remember {
            SettingsContentUiActions(
                onNavigateToPlaybackComponent = {
                    vm.requestNavigation(AppRoutes.Settings.PlaybackComponent)
                },
                onNavigateToHomeTab = {
                    vm.requestNavigation(AppRoutes.Settings.HomeTab)
                },
                onKeepDanmakuVisibleChange = {
                    vm.setKeepDanmakuVisible(it)
                },
                onHideFollowButtonChange = {
                    vm.setHideFollowButton(it)
                },
                onKeepCleanModeChange = {
                    vm.setKeepCleanMode(it)
                }
            )
        }
        val configActions = remember {
            SettingsContentConfigActions(
                onExportConfig = {
                    vm.requestExportConfig()
                },
                onImportConfig = {
                    vm.requestImportConfig()
                }
            )
        }
        val moduleActions = remember {
            SettingsContentModuleActions(
                onHiddenFeatureChange = {
                    vm.setHiddenFeature(it)
                },
                onDisableVerboseChange = {
                    vm.setVerboseDisabled(it)
                },
                onResetMapping = {
                    vm.resetMapping()
                },
                onLoadCustomMapping = {
                    vm.requestLoadCustomMapping()
                },
                onClearCustomMapping = {
                    vm.clearCustomMapping()
                }
            )
        }

        SettingsContent(
            state = settingsConfig,
            actions = remember(saveActions, feedActions, uiActions, configActions, moduleActions) {
                SettingsContentActions(saveActions, feedActions, uiActions, configActions, moduleActions)
            },
            modifier = modifier.padding(innerPadding)
        )
    }

    @Composable
    private fun RecommendedFeedFilterScreen(
        modifier: Modifier = Modifier,
        onShowSnackbar: (SnackEvent) -> Unit,
        innerPadding: PaddingValues
    ) {
        val vm: RecommendedFeedFilterViewModel = viewModel()

        LaunchedEffect(Unit) {
            vm.snackFlow.conflate().collect { event ->
                onShowSnackbar(event)
            }
        }

        val switchActions = remember {
            RecommendedFeedFilterSwitchActions(
                onMainSwitchChange = {
                    vm.setMainSwitch(it)
                },
                onBlockFollowedAuthorChange = {
                    vm.setBlockFollowedAuthor(it)
                },
                onBlockAdChange = {
                    vm.setBlockAd(it)
                },
                onBlockEcomChange = {
                    vm.setBlockEcom(it)
                },
                onBlockGrouponChange = {
                    vm.setBlockGrouponLargeCard(it)
                },
                onBlockLiveChange = {
                    vm.setBlockLive(it)
                },
                onBlockMultiImageChange = {
                    vm.setBlockMultiImage(it)
                }
            )
        }
        val countActions = remember {
            RecommendedFeedFilterCountActions(
                onShortDurationChange = {
                    vm.setShortDuration(it)
                },
                onLongDurationChange = {
                    vm.setLongDuration(it)
                },
                onCollectCountMinChange = {
                    vm.setCollectCountMin(it)
                },
                onCollectCountMaxChange = {
                    vm.setCollectCountMax(it)
                },
                onCommentCountMinChange = {
                    vm.setCommentCountMin(it)
                },
                onCommentCountMaxChange = {
                    vm.setCommentCountMax(it)
                },
                onDiggCountMinChange = {
                    vm.setDiggCountMin(it)
                },
                onDiggCountMaxChange = {
                    vm.setDiggCountMax(it)
                },
                onShareCountMinChange = {
                    vm.setShareCountMin(it)
                },
                onShareCountMaxChange = {
                    vm.setShareCountMax(it)
                }
            )
        }
        val keywordActions = remember {
            RecommendedFeedFilterKeywordActions(
                title = RecommendedFeedFilterTitleKeywordActions(
                    onAddKeyword = {
                        vm.addTitleKeyword()
                    },
                    onKeywordTextChange = { index, text ->
                        vm.setTitleKeywordText(index, text)
                    },
                    onRemoveKeyword = { index ->
                        vm.removeTitleKeyword(index)
                    },
                    onSave = {
                        vm.saveTitleKeywords()
                    },
                    onRegexChange = {
                        vm.setTitleRegex(it)
                    }
                ),
                uid = RecommendedFeedFilterUidKeywordActions(
                    onAddKeyword = {
                        vm.addUidKeyword()
                    },
                    onKeywordTextChange = { index, text ->
                        vm.setUidKeywordText(index, text)
                    },
                    onRemoveKeyword = { index ->
                        vm.removeUidKeyword(index)
                    },
                    onSave = {
                        vm.saveUidKeywords()
                    }
                ),
                nickname = RecommendedFeedFilterNicknameKeywordActions(
                    onAddKeyword = {
                        vm.addNicknameKeyword()
                    },
                    onKeywordTextChange = { index, text ->
                        vm.setNicknameKeywordText(index, text)
                    },
                    onRemoveKeyword = { index ->
                        vm.removeNicknameKeyword(index)
                    },
                    onSave = {
                        vm.saveNicknameKeywords()
                    },
                    onRegexChange = {
                        vm.setNicknameRegex(it)
                    }
                ),
                desc = RecommendedFeedFilterDescKeywordActions(
                    onAddKeyword = {
                        vm.addDescKeyword()
                    },
                    onKeywordTextChange = { index, text ->
                        vm.setDescKeywordText(index, text)
                    },
                    onRemoveKeyword = { index ->
                        vm.removeDescKeyword(index)
                    },
                    onSave = {
                        vm.saveDescKeywords()
                    },
                    onRegexChange = {
                        vm.setDescRegex(it)
                    }
                )
            )
        }

        val filterConfig by vm.filterConfig.collectAsStateWithLifecycle()

        RecommendedFeedFilterContent(
            state = filterConfig,
            actions = remember(switchActions, countActions, keywordActions) {
                RecommendedFeedFilterActions(
                    switch = switchActions,
                    count = countActions,
                    keyword = keywordActions
                )
            },
            modifier = modifier.padding(innerPadding)
        )
    }

    @Composable
    private fun HomeTabVisibilityScreen(modifier: Modifier = Modifier, innerPadding: PaddingValues) {
        val vm: HomeTabVisibilityViewModel = viewModel()
        val tabConfig by vm.tabConfig.collectAsStateWithLifecycle()
        val actions = remember {
            HomeTabVisibilityActions(
                top = HomeTopTabVisibilityActions(
                    onMainSwitchChange = {
                        vm.onTopMainSwitchChange(it)
                    },
                    onSwitchChange = { key, checked ->
                        vm.onSwitchChange(key, checked)
                    }
                ),
                bottom = HomeBottomTabVisibilityActions(
                    onMainSwitchChange = {
                        vm.onBottomMainSwitchChange(it)
                    },
                    onSwitchChange = { key, checked ->
                        vm.onSwitchChange(key, checked)
                    }
                )
            )
        }

        HomeTabVisibilityContent(
            config = tabConfig,
            actions = actions,
            modifier = modifier.padding(innerPadding)
        )
    }

    @Composable
    private fun PlaybackComponentVisibilityScreen(modifier: Modifier = Modifier, innerPadding: PaddingValues) {
        val vm: PlaybackComponentVisibilityViewModel = viewModel()
        val configs by vm.playbackComponentConfig.collectAsStateWithLifecycle()
        val actions = remember {
            PlaybackComponentVisibilityActions(
                onMainSwitchChange = {
                    vm.onMainSwitchChange(it)
                },
                onSwitchChange = { key, checked ->
                    vm.onSwitchChange(key, checked)
                }
            )
        }

        PlaybackComponentVisibilityContext(
            config = configs,
            actions = actions,
            modifier = modifier.padding(innerPadding)
        )
    }

    @Composable
    private fun VerifyDialogScreen(
        modifier: Modifier = Modifier,
        onVerified: () -> Unit,
        onDismiss: () -> Unit,
        onShowSnackbar: (SnackEvent) -> Unit
    ) {
        val currLocalResources = LocalResources.current
        val vm: VerifyDialogViewModel = viewModel(
            factory = VerifyDialogViewModelFactory(
                currLocalResources.getStringArray(R.array.valid_verification_urls)
            )
        )
        val uiState by vm.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(Unit) {
            vm.triggerVerifySuccess.collect {
                onVerified()
            }
        }

        LaunchedEffect(Unit) {
            vm.triggerVerifyFailure.collect {
                // do nothing
            }
        }

        LaunchedEffect(Unit) {
            vm.snackFlow.conflate().collect { event ->
                onShowSnackbar(event)
            }
        }

        val actions = remember {
            VerifyDialogUiActions(
                onInputUrlChange = {
                    vm.onInputTextChanged(it)
                },
                onConfirm = {
                    vm.onConfirm(it)
                },
                onCancel = {
                    vm.onCancel()
                }
            )
        }

        VerifyDialogContent(
            state = uiState,
            actions = actions,
            modifier = modifier
        )
    }

    companion object {
        val TAG = this::class.simpleName
    }
}
