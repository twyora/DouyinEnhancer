package io.github.twyora.douyinenhancer.ui

import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.content.home.HomeContent
import io.github.twyora.douyinenhancer.ui.content.home.HomeContentUiActions
import io.github.twyora.douyinenhancer.ui.content.home.HomeContentUiState
import io.github.twyora.douyinenhancer.ui.content.home.HomeContentViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterContent
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterCountActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterDescKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterNicknameKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterSwitchActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterTitleKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterUidKeywordActions
import io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed.RecommendedFeedFilterViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeBottomTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityContent
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTabVisibilityViewModel
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.hometab.tabs.HomeTopTabVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityActions
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityContext
import io.github.twyora.douyinenhancer.ui.content.settings.visiblity.playback.components.PlaybackComponentVisibilityViewModel
import io.github.twyora.douyinenhancer.ui.navigation.AppRoutes
import io.github.twyora.douyinenhancer.utils.android.openUrl
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.platform.LocalResources
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContent
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentViewModel
import java.io.File
import java.nio.file.Files
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipInputStream
import kotlinx.coroutines.launch
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.deeplink.DeepLinkRequest
import androidx.navigation3.runtime.deeplink.DeepLinkUri
import androidx.navigation3.runtime.deeplink.UriDeepLinkMatcher
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentConfigActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentFeedActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentModuleActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentSaveActions
import io.github.twyora.douyinenhancer.ui.content.settings.SettingsContentUiActions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.serialization.serializer

class MainActivity : ComponentActivity() {
    private val navFlow = MutableSharedFlow<NavKey>(
        extraBufferCapacity = 1,
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

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

                LaunchedEffect(Unit) {
                    navFlow.collect { navKey ->
                        backstack.add(navKey)
                        navFlow.resetReplayCache()
                    }
                }

                NavDisplay(
                    backStack = backstack,
                    entryProvider = { key ->
                        when (key) {
                            AppRoutes.Home -> NavEntry(key) {
                                HomeScreen(onNavigate = {
                                    navFlow.tryEmit(it)
                                })
                            }

                            AppRoutes.Settings.Main -> NavEntry(key) {
                                SettingsScreen(onNavigate = {
                                    navFlow.tryEmit(it)
                                })
                            }

                            AppRoutes.Settings.RecommendedFeedFilter -> NavEntry(key) {
                                RecommendedFeedFilterScreen()
                            }

                            AppRoutes.Settings.PlaybackComponent -> NavEntry(key) {
                                PlaybackComponentVisibilityScreen()
                            }

                            AppRoutes.Settings.HomeTab -> NavEntry(key) {
                                HomeTabVisibilityScreen()
                            }

                            else -> NavEntry(key) {
                            }
                        }
                    },
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
    fun HomeScreen(modifier: Modifier = Modifier, onNavigate: (NavKey) -> Unit) {
        val mainVm: HomeContentViewModel = viewModel()
        val mainUiState by mainVm.uiState.collectAsStateWithLifecycle()
        val currLocalContext = LocalContext.current

        val snackbarHostState = remember {
            SnackbarHostState()
        }

        val currLocalResources = LocalResources.current

        LaunchedEffect(currLocalResources) {
            mainVm.snackFlow.conflate().collect { event ->
                val message = if (event.args.isEmpty()) {
                    currLocalResources.getString(event.resId)
                } else {
                    currLocalResources.getString(event.resId, *event.args.toTypedArray())
                }
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
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

        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
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
                uiState = HomeContentUiState(
                    launcherIconHidden = mainUiState.launcherIconHidden,
                    moduleActivationState = mainUiState.moduleActivationState,
                    updateState = mainUiState.updateState
                ),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                uiActions = uiActions
            )
        }
    }

    @Composable
    private fun SettingsScreen(
        modifier: Modifier = Modifier,
        onNavigate: (NavKey) -> Unit
    ) {
        val vm: SettingsContentViewModel = viewModel()
        val settingsConfig by vm.settingsConfig.collectAsStateWithLifecycle()

        val currLocalResources = LocalResources.current

        val snackbarHostState = remember {
            SnackbarHostState()
        }

        LaunchedEffect(Unit) {
            vm.triggerNavigation.collect {
                onNavigate(it)
            }
        }

        LaunchedEffect(currLocalResources) {
            vm.snackFlow.conflate().collect { event ->
                val message = if (event.args.isEmpty()) {
                    currLocalResources.getString(event.resId)
                } else {
                    currLocalResources.getString(event.resId, *event.args.toTypedArray())
                }
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
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
                            val storages = listOf(ConfigManager.settingsStorage, ConfigManager.moduleStorage)
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

                                vm.importConfigZip(
                                    listOf(ConfigManager.settingsStorage, ConfigManager.moduleStorage),
                                    inputFiles = tempDir.listFiles()?.filter { file ->
                                        file.name != "manifest.json"
                                    }?.toList() ?: emptyList(),
                                    manifestFile = File(tempDir, "manifest.json")
                                )
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

        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            SettingsContent(
                state = settingsConfig,
                actions = remember(saveActions, feedActions, uiActions, configActions, moduleActions) {
                    SettingsContentActions(saveActions, feedActions, uiActions, configActions, moduleActions)
                },
                modifier = modifier.padding(paddingValues)
            )
        }
    }

    @Composable
    private fun RecommendedFeedFilterScreen(
        modifier: Modifier = Modifier
    ) {
        val vm: RecommendedFeedFilterViewModel = viewModel()

        val currLocalResources = LocalResources.current

        val snackbarHostState = remember {
            SnackbarHostState()
        }

        LaunchedEffect(currLocalResources) {
            vm.snackFlow.conflate().collect { event ->
                val message = if (event.args.isEmpty()) {
                    currLocalResources.getString(event.resId)
                } else {
                    currLocalResources.getString(event.resId, *event.args.toTypedArray())
                }
                snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
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
                },
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
                },
            )
        }
        val keywordActions = remember {
            RecommendedFeedFilterKeywordActions(
                title = RecommendedFeedFilterTitleKeywordActions(
                    onAdd = {
                        vm.addTitleKeyword()
                    },
                    onTextChange = { index, text ->
                        vm.setTitleKeywordText(index, text)
                    },
                    onRemove = { index ->
                        vm.removeTitleKeyword(index)
                    },
                    onRegexChange = {
                        vm.setTitleRegex(it)
                    },
                ),
                uid = RecommendedFeedFilterUidKeywordActions(
                    onAdd = {
                        vm.addUidKeyword()
                    },
                    onTextChange = { index, text ->
                        vm.setUidKeywordText(index, text)
                    },
                    onRemove = { index ->
                        vm.removeUidKeyword(index)
                    },
                ),
                nickname = RecommendedFeedFilterNicknameKeywordActions(
                    onAdd = {
                        vm.addNicknameKeyword()
                    },
                    onTextChange = { index, text ->
                        vm.setNicknameKeywordText(index, text)
                    },
                    onRemove = { index ->
                        vm.removeNicknameKeyword(index)
                    },
                    onRegexChange = {
                        vm.setNicknameRegex(it)
                    },
                ),
                desc = RecommendedFeedFilterDescKeywordActions(
                    onAdd = {
                        vm.addDescKeyword()
                    },
                    onTextChange = { index, text ->
                        vm.setDescKeywordText(index, text)
                    },
                    onRemove = { index ->
                        vm.removeDescKeyword(index)
                    },
                    onRegexChange = {
                        vm.setDescRegex(it)
                    },
                ),
            )
        }

        val filterConfig by vm.filterConfig.collectAsStateWithLifecycle()

        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            RecommendedFeedFilterContent(
                state = filterConfig,
                actions = remember(switchActions, countActions, keywordActions) {
                    RecommendedFeedFilterActions(
                        switch = switchActions,
                        count = countActions,
                        keyword = keywordActions,
                    )
                },
                modifier = modifier.padding(paddingValues)
            )
        }
    }

    @Composable
    private fun HomeTabVisibilityScreen(modifier: Modifier = Modifier) {
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
                    },
                ),
                bottom = HomeBottomTabVisibilityActions(
                    onMainSwitchChange = {
                        vm.onBottomMainSwitchChange(it)
                    },
                    onSwitchChange = { key, checked ->
                        vm.onSwitchChange(key, checked)
                    },
                ),
            )
        }

        val snackbarHostState = remember {
            SnackbarHostState()
        }

        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            HomeTabVisibilityContent(
                config = tabConfig,
                actions = actions,
                modifier = modifier.padding(paddingValues)
            )
        }
    }

    @Composable
    private fun PlaybackComponentVisibilityScreen(modifier: Modifier = Modifier) {
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
        val snackbarHostState = remember {
            SnackbarHostState()
        }

        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { paddingValues ->
            PlaybackComponentVisibilityContext(
                config = configs,
                actions = actions,
                modifier = modifier.padding(paddingValues)
            )
        }
    }

    companion object {
        val TAG = this::class.simpleName
    }
}

