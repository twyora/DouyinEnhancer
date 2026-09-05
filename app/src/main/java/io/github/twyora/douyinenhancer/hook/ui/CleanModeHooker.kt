package io.github.twyora.douyinenhancer.hook.ui

import android.app.Activity
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.CleanModeKey
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.toClass
import java.lang.ref.WeakReference

/**
 * 清爽模式（防烧屏模式 / OLED 救星）。
 *
 * 规则（已与需求确认）：
 * 1. 清爽开关开启期间：整个抖音 App 全程沉浸全屏（状态栏/导航栏永不出现，任何界面、任何时刻），
 *    从启动的一瞬间生效，关掉开关并重启后恢复；
 * 2. 顶栏/底栏/右侧互动列/作者文案/音乐等悬浮控件：播放时隐藏、暂停（或播放结束）恢复；
 * 3. feed 面板顶/底留白保持清零，让视频铺满全屏（不随暂停恢复）。
 *
 * 注：抖音冷启动首条视频的部分原生行为（个别控件晚出现等）不属于本模块职责范围。
 */
@HookOnMainProcess
object CleanModeHooker : YukiBaseHooker() {
    private const val TAG = "CleanModeHooker"

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    // handleVideoEvent 的 videoType（内容流播放事件，兜底）
    private const val VIDEO_EVENT_TEXTURE_AVAILABLE = 0
    private const val VIDEO_EVENT_PAUSE_CLICK = 16
    private const val VIDEO_EVENT_PAUSE_1 = 45
    private const val VIDEO_EVENT_PAUSE_2 = 47
    private const val VIDEO_EVENT_RESUME_1 = 46
    private const val VIDEO_EVENT_RESUME_2 = 48

    // onVideoPlayerEvent 的 code（播放器状态事件，兜底）
    private const val PLAYER_EVENT_PAUSED = 4
    private const val PLAYER_EVENT_COMPLETED = 7

    private val hidePlayVideoTypes = setOf(VIDEO_EVENT_TEXTURE_AVAILABLE, VIDEO_EVENT_RESUME_1, VIDEO_EVENT_RESUME_2)
    private val showPlayVideoTypes = setOf(VIDEO_EVENT_PAUSE_CLICK, VIDEO_EVENT_PAUSE_1, VIDEO_EVENT_PAUSE_2)

    /**
     * 沉浸标志位（含 layout 标志，避免隐藏系统栏时内容重排抖动）：
     * LAYOUT_STABLE(256) | LAYOUT_FULLSCREEN(1024) | LAYOUT_HIDE_NAVIGATION(512) |
     * FULLSCREEN(4) | HIDE_NAVIGATION(2) | IMMERSIVE_STICKY(4096) = 5894
     */
    private const val IMMERSIVE_FLAGS = 5894

    // 状态栏/导航栏是否被隐藏的判定位：FULLSCREEN(4) | HIDE_NAVIGATION(2)
    private const val HIDDEN_BAR_FLAGS = 6

    private const val HOST_PANEL_CLASS = "com.ss.android.ugc.aweme.feed.panel.BaseListFragmentPanel"
    private const val HOST_AWEME_CLASS = "com.ss.android.ugc.aweme.feed.model.Aweme"

    /** 播放/恢复类方法：调用即隐藏悬浮控件 */
    private val playMethods = listOf(
        Method("handlePlay", listOf(HOST_AWEME_CLASS)),
        Method("handlePlay", listOf(HOST_AWEME_CLASS, "boolean")),
        Method("handlePlay", listOf(HOST_AWEME_CLASS, "boolean", "boolean")),
        Method("tryResumePlay", emptyList()),
        Method("tryResumePlay", listOf("boolean")),
        Method("tryResumePlay", listOf(HOST_AWEME_CLASS)),
        Method("tryResumePlay", listOf(HOST_AWEME_CLASS, "boolean")),
        Method("tryResumePlayByOnResume", listOf("boolean")),
        Method("resumePlay", listOf(HOST_AWEME_CLASS)),
        Method("handleResumeP", null)
    )

    /** 暂停类方法：调用即恢复悬浮控件 */
    private val pauseMethods = listOf(
        Method("handlePause", listOf("boolean")),
        Method("pausePlayer", emptyList()),
        Method("pausePlayerWithListener", emptyList()),
        Method("pauseCurrentPlayerWithListener", emptyList())
    )

    /** 页面切换类方法：切到新页后进入隐藏（新视频会自动播放） */
    private val pageSelectedMethods = listOf(
        Method("onPageSelected", emptyList()),
        Method("onPageScrollStateChanged", listOf("int"))
    )

    /** 需要整体隐藏的悬浮控件容器（类名在抖音 38.8.0 未混淆） */
    private val hideClassNames = listOf(
        "com.ss.android.ugc.aweme.homepage.ui.titlebar.MainTitleBar",
        "com.ss.android.ugc.aweme.homepage.ui.bottombar.MainBottomTabContainer",
        "com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView",
        "com.ss.android.ugc.aweme.feed.ui.AwemeIntroInfoLayout",
        "com.ss.android.ugc.aweme.feed.ui.musiccover.MusicCoverContainerLayout",
        "com.ss.android.ugc.aweme.feed.widget.feedbottommusicanchor.FeedBottomMusicAnchorLayout",
        "com.ss.android.ugc.aweme.feed.widget.MarqueeView"
    )

    private val classCache = HashMap<String, Class<*>?>()

    // 已对哪些留白视图做过首次 requestLayout（只做一次，避免反复重排）
    private val spaceZeroedOnce = HashSet<Int>()

    // 被我们隐藏的“底部全宽纯占位 View”（dyoo 删 spacer 的等价物）
    private val hiddenBottomSpacers = HashMap<Int, WeakReference<View>>()

    // 翻页器父容器原始底部 padding（清爽时清零让 pager 长到全屏）
    private val pagerAncestorPadding = HashMap<Int, Int>()

    // RTViewPager 原始布局高度（清爽时直接撑到全屏）
    private val pagerOriginalHeight = HashMap<Int, Int>()

    // 已由我们改成“全屏”的翻页器实例（暂停时据此还原；记录即改，避免快速暂停/继续的竞态）
    private val pagerFullscreenChanged = HashSet<Int>()

    // RTViewPager 原始 layout_above/below 锚定 id（备用）
    private val pagerOriginalRuleAbove = HashMap<Int, Int>()
    private val pagerOriginalRuleBelow = HashMap<Int, Int>()

    // 首次进入清爽时录制的 pager 原生高度（用于推算暂停上移量 = 屏幕高 - 原生高）
    private var nativePagerHeight = 0

    // 暂停恢复（底栏可见、pager 保持全屏）时上移的页内悬浮控件，避免被底栏盖住
    private val pauseLiftedViews = HashMap<Int, Float>()
    private val pauseLiftClassNames = listOf(
        "com.ss.android.ugc.aweme.feed.ui.AwemeIntroInfoLayout",
        "com.ss.android.ugc.aweme.feed.ui.FeedRightScaleView",
        "com.ss.android.ugc.aweme.feed.ui.musiccover.MusicCoverContainerLayout"
    )
    private val pauseLiftReassertRunnable = object : Runnable {
        override fun run() {
            if (overlaysHidden) {
                return
            }
            val activity = mainActivityRef?.get() ?: return
            val loader = packageInstance.baseListFragmentPanel.selfClass?.classLoader ?: return
            val decor = activity.window?.decorView ?: return
            applyPauseLift(decor, loader, show = true)
        }
    }

    // 是否已做过一次“底栏 显示→隐藏”让抖音按 BottomSpace=0 重新评估（清圆角）
    private var firstEntryBarFlipDone = false

    private val mainHandler = Handler(Looper.getMainLooper())
    private val rehideRunnable = object : Runnable {
        override fun run() {
            if (overlaysHidden) {
                // 补扫只处理悬浮控件，不再清零留白，避免与抖音的布局逻辑打架
                applyOverlayVisibility(hidden = true, log = false, zeroSpaces = false)
            }
        }
    }

    private var mainActivityRef: WeakReference<Activity>? = null
    private var panelRef: WeakReference<Any>? = null
    private var immersiveDecorRef: WeakReference<View>? = null

    /** 悬浮控件是否处于隐藏状态（播放中=隐藏，暂停=恢复） */
    @Volatile
    private var overlaysHidden = false

    /** 内容流正在滚动/切页：期间忽略“恢复悬浮控件”，避免闪烁 */
    @Volatile
    private var pagerDragging = false

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(CleanModeKey.MAIN_SWITCH, false)) {
            if (verbose) {
                YLog.debug("$TAG: clean mode disabled, skip hook")
            }
            return
        }
        YLog.debug("$TAG: CleanModeHooker v7.21 active (module 0.11.1)")
        installGlobalImmersiveHook()
        installPlaybackStateHooks()
    }

    /** 任何 Activity 恢复时都强制沉浸全屏（清爽开启期间状态栏/导航栏永不出现） */
    private fun installGlobalImmersiveHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        // onCreate 之前就生效：保证 feed 首次布局即处于全屏，避免“视频铺不到底”的时机竞争
        Activity::class.java.resolveMethod(
            Method("onCreate", listOf("android.os.Bundle"))
        )?.hook {
            before {
                val activity = instance as? Activity ?: return@before
                mainActivityRef = WeakReference(activity)
                applyPersistentImmersive(activity)
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to enforce immersive before activity create", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook activity create", throwable)
            }
        }

        return Activity::class.java.resolveMethod(
            Method("onResume", emptyList())
        )?.hook {
            after {
                val activity = instance as? Activity ?: return@after
                mainActivityRef = WeakReference(activity)
                applyPersistentImmersive(activity)
                if (verbose) {
                    YLog.debug("$TAG: immersive enforced on ${activity::class.java.name}")
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to enforce immersive on activity resume", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook activity resume", throwable)
            }
        }
    }

    private fun installPlaybackStateHooks() {
        val classLoader = packageInstance.baseListFragmentPanel.selfClass?.classLoader
        if (classLoader == null) {
            YLog.error("$TAG: unable to resolve host class loader")
            return
        }
        val panelClass = HOST_PANEL_CLASS.toClass(classLoader)

        playMethods.forEach { method ->
            panelClass.resolveMethod(method)?.hook {
                after {
                    capturePanel(instance)
                    applyOverlayMode(hidden = true)
                }
            }?.result {
                onConductFailure { _, throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
            }
        }

        pauseMethods.forEach { method ->
            panelClass.resolveMethod(method)?.hook {
                after {
                    capturePanel(instance)
                    applyOverlayMode(hidden = false)
                }
            }?.result {
                onConductFailure { _, throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
            }
        }

        pageSelectedMethods.forEach { method ->
            panelClass.resolveMethod(method)?.hook {
                after {
                    capturePanel(instance)
                    if (method.name == "onPageScrollStateChanged") {
                        val state = args[0] as? Int ?: return@after
                        pagerDragging = state != 0
                        // 任何翻页状态都补扫隐藏：切页时抖音会短暂重新显示新页控件
                        applyOverlayMode(hidden = true)
                    } else {
                        applyOverlayMode(hidden = true)
                    }
                }
            }?.result {
                onConductFailure { _, throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
                onHookingFailure { throwable ->
                    YLog.error("$TAG: failed to hook ${method.name} for clean mode", throwable)
                }
            }
        }

        // 兜底：视频事件
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.handleVideoEvent()
        )?.hook {
            after {
                capturePanel(instance)
                val videoEvent = args[0] ?: return@after
                val videoType = videoEvent.getField<Int>(
                    packageInstance.videoEvent.videoType()
                ) ?: return@after
                when {
                    videoType in hidePlayVideoTypes -> applyOverlayMode(hidden = true)
                    videoType in showPlayVideoTypes -> applyOverlayMode(hidden = false)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to listen feed video events", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook feed video events", throwable)
            }
        }

        // 兜底：播放器状态事件（暂停/结束）
        packageInstance.baseListFragmentPanel.selfClass?.resolveMethod(
            packageInstance.baseListFragmentPanel.onVideoPlayerEvent()
        )?.hook {
            after {
                capturePanel(instance)
                val playerEvent = args[0] ?: return@after
                val code = playerEvent.getField<Int>(
                    packageInstance.videoPlayerEvent.code()
                ) ?: return@after
                if (code == PLAYER_EVENT_PAUSED) {
                    // 仅在真正暂停时恢复；播完自动重播不恢复，避免重播瞬间元素闪现
                    applyOverlayMode(hidden = false)
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to listen player state events", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook player state events", throwable)
            }
        }
    }

    private fun capturePanel(instance: Any?) {
        if (instance != null) {
            panelRef = WeakReference(instance)
        }
    }

    /** 调用当前 feed 项 VideoViewHolder.openCleanMode，让抖音原生清爽逻辑隐藏/恢复其全部控件 */
    private fun setNativeItemClean(hidden: Boolean) {
        val panel = panelRef?.get() ?: return
        val holder = runCatching {
            panel.javaClass.getMethod("getCurViewHolder").invoke(panel)
        }.getOrNull() ?: return
        runCatching {
            val method = holder.javaClass.getMethod("openCleanMode", Boolean::class.javaPrimitiveType)
            method.isAccessible = true
            method.invoke(holder, hidden)
            if (verbose) {
                YLog.debug("$TAG: native openCleanMode($hidden) invoked on ${holder.javaClass.name}")
            }
        }.onFailure { throwable ->
            YLog.error("$TAG: failed to invoke native openCleanMode", throwable)
        }
    }

    /** 只控制悬浮控件显隐；系统栏由全程沉浸负责，不再随暂停恢复 */
    private fun applyOverlayMode(hidden: Boolean) {
        if (!hidden && pagerDragging) {
            return
        }
        if (hidden) {
            val transition = !overlaysHidden
            overlaysHidden = true
            // 只在进入隐藏的瞬间清零留白（避免抖音重置后我们反复清零造成抖动）
            applyOverlayVisibility(hidden = true, log = transition, zeroSpaces = transition)
            // 隐藏后做几次短间隔补扫，兜住切页/重渲染后新冒出的控件
            mainHandler.removeCallbacks(rehideRunnable)
            mainHandler.postDelayed(rehideRunnable, 200L)
            mainHandler.postDelayed(rehideRunnable, 700L)
            mainHandler.postDelayed(rehideRunnable, 1800L)
            if (transition && verbose) {
                dumpLayoutStructure()
            }
            if (transition && !firstEntryBarFlipDone) {
                // BottomSpace 已清零；再让底栏经历一次真实 显示→隐藏，抖音便会按
                // “无底栏占位=全屏”重新评估 → 视频方角（等效手动暂停再继续，自动执行）
                firstEntryBarFlipDone = true
                mainHandler.postDelayed({ doFirstEntryBarFlip() }, 2000L)
            }
        } else {
            if (!overlaysHidden) {
                return
            }
            overlaysHidden = false
            applyOverlayVisibility(hidden = false, log = true, zeroSpaces = false)
        }
    }

    private fun applyOverlayVisibility(hidden: Boolean, log: Boolean, zeroSpaces: Boolean) {
        val activity = mainActivityRef?.get()
        if (activity == null) {
            if (log) {
                YLog.debug("$TAG: main activity not ready, skip applying overlay visibility")
            }
            return
        }
        val classLoader = packageInstance.baseListFragmentPanel.selfClass?.classLoader
        val decorView = activity.window?.decorView
        if (classLoader == null || decorView == null) {
            YLog.error("$TAG: unable to resolve host class loader or decor view")
            return
        }

        if (zeroSpaces) {
            zeroPanelSpaces()
        }

        val overlayViews = ArrayList<View>()
        collectOverlayViews(decorView, classLoader, overlayViews)
        val targetVisibility = if (hidden) View.GONE else View.VISIBLE
        var changed = 0
        overlayViews.forEach { view ->
            if (view.visibility != targetVisibility) {
                view.visibility = targetVisibility
                changed++
            }
        }
        adjustBottomPlainSpacer(decorView, hidden)
        adjustPagerAncestors(decorView, classLoader, hidden)
        adjustPagerHeight(decorView, classLoader, hidden)
        // 暂停恢复时把页内悬浮控件上移避开底栏（pager 保持全屏以维持方角）；播放隐藏时复位
        applyPauseLift(decorView, classLoader, show = !hidden)
        if (!hidden) {
            mainHandler.removeCallbacks(pauseLiftReassertRunnable)
            mainHandler.postDelayed(pauseLiftReassertRunnable, 150L)
            mainHandler.postDelayed(pauseLiftReassertRunnable, 450L)
        } else {
            mainHandler.removeCallbacks(pauseLiftReassertRunnable)
        }
        if (log || changed > 0) {
            YLog.debug("$TAG: overlays ${if (hidden) "hidden" else "shown"}, $changed views changed, ${overlayViews.size} total")
        }
        // 隐藏后仍可见的右列/底部疑似悬浮控件（合集等页面可能漏网），verbose 时打印类名
        if (hidden && verbose) {
            dumpLeftoverOverlays(activity)
        }
    }

    /**
     * 打印隐藏后仍可见的“疑似悬浮控件”（右列/底部区域、非通用布局、非弹幕/进度条），
     * 附最近的非通用祖先类名，用于定位合集等页面漏网的控件容器。
     */
    private fun dumpLeftoverOverlays(activity: Activity) {
        val decor = activity.window?.decorView ?: return
        val screenW = decor.right - decor.left
        val screenH = decor.bottom - decor.top
        val found = ArrayList<View>()
        val counter = intArrayOf(0)
        fun walk(view: View, depth: Int) {
            if (depth > 14 || counter[0] > 4000) {
                return
            }
            counter[0]++
            if (view.visibility == View.VISIBLE && view.width > 0 && view.height > 0) {
                val loc = IntArray(2)
                view.getLocationInWindow(loc)
                // 只看真正落在可视窗口内的视图，过滤掉水平/垂直方向离屏的兄弟页与评论面板等
                if (loc[0] in 0 until screenW && loc[1] in 0 until screenH) {
                    val rightBand = loc[0] >= screenW - 220
                    val bottomBand = loc[1] >= screenH - 560
                    if (rightBand || bottomBand) {
                        val name = view.javaClass.name
                        val isGeneric = name.startsWith("android.widget.FrameLayout") ||
                            name.startsWith("android.widget.RelativeLayout") ||
                            name.startsWith("android.widget.LinearLayout") ||
                            name.startsWith("android.widget.HorizontalScrollView") ||
                            name.startsWith("android.widget.ScrollView") ||
                            name == "android.view.View" || name.startsWith("android.view.ViewGroup") ||
                            name.startsWith("X.") || name.endsWith("ViewStub") || name.endsWith("ViewGroup")
                        val isKeep = name.contains("DanmakuView") || name.contains("SeekBar") ||
                            name.contains("VerticalViewPager") || name.contains("RTViewPager") ||
                            name.contains("SurfaceView") || name.contains("TextureView")
                        if (!isGeneric && !isKeep && found.size < 60) {
                            found.add(view)
                        }
                    }
                }
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    walk(view.getChildAt(i), depth + 1)
                }
            }
        }
        walk(decor, 0)
        if (found.isEmpty()) {
            return
        }
        YLog.debug("$TAG: leftover visible overlays in right/bottom bands (${found.size})")
        found.forEach { view ->
            val loc = IntArray(2)
            view.getLocationInWindow(loc)
            val idText = if (view.id != View.NO_ID) "id=0x${view.id.toString(16)}" else "id=no"
            val ancestors = StringBuilder()
            var parent = view.parent
            var d = 0
            while (parent is ViewGroup && d < 5) {
                val pName = parent.javaClass.name
                ancestors.append(
                    if (pName.contains("aweme") ||
                        pName.contains("bytedance")
                    ) {
                        pName
                    } else {
                        pName.substringAfterLast('.')
                    }
                ).append(" > ")
                parent = parent.parent
                d++
            }
            YLog.debug(
                "$TAG:   [leftover] ${view.javaClass.name} top=${loc[1]} bottom=${loc[1] + view.height} " +
                    "x=${loc[0]} w=${view.width} $idText ancestors=$ancestors"
            )
        }
    }

    /** 清爽隐藏时隐藏屏幕底部“全宽纯 View 占位”（dyoo 删 spacer 思路），暂停恢复 */
    private fun adjustBottomPlainSpacer(decorView: View, hidden: Boolean) {
        if (hidden) {
            val screenH = decorView.bottom - decorView.top
            val screenW = decorView.right - decorView.left
            collectPlainBottomViews(decorView, screenW, screenH, this::recordAndHideSpacer)
        } else {
            val it = hiddenBottomSpacers.entries.iterator()
            while (it.hasNext()) {
                val e = it.next()
                val v = e.value.get()
                it.remove()
                if (v != null && v.visibility != View.VISIBLE) {
                    v.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun recordAndHideSpacer(view: View) {
        hiddenBottomSpacers[System.identityHashCode(view)] = WeakReference(view)
        if (view.visibility != View.GONE) {
            view.visibility = View.GONE
            if (verbose) {
                YLog.debug("$TAG: bottom plain spacer hidden ${view.javaClass.name} top=${view.top} bottom=${view.bottom}")
            }
        }
    }

    private fun collectPlainBottomViews(view: View, screenW: Int, screenH: Int, onFound: (View) -> Unit) {
        if (view !is ViewGroup) {
            if (view.javaClass.name == "android.view.View" && view.visibility == View.VISIBLE &&
                view.bottom >= screenH - 300 && (view.right - view.left) >= screenW - 4 && (view.bottom - view.top) in 50..400
            ) {
                onFound(view)
            }
            return
        }
        for (i in 0 until view.childCount) {
            collectPlainBottomViews(view.getChildAt(i), screenW, screenH, onFound)
        }
    }

    /** 清爽开启期间：强制沉浸全屏，并挂看守，防止抖音/系统把状态栏放出来 */
    private fun applyPersistentImmersive(activity: Activity) {
        val decorView = activity.window?.decorView ?: return
        val current = decorView.systemUiVisibility
        if (current and IMMERSIVE_FLAGS != IMMERSIVE_FLAGS) {
            decorView.systemUiVisibility = current or IMMERSIVE_FLAGS
        }

        // 每次活动恢复都重新挂看守（抖音可能覆盖监听器）
        decorView.setOnSystemUiVisibilityChangeListener { visibility ->
            if (visibility and HIDDEN_BAR_FLAGS != HIDDEN_BAR_FLAGS) {
                decorView.systemUiVisibility = decorView.systemUiVisibility or IMMERSIVE_FLAGS
            }
        }
        immersiveDecorRef = WeakReference(decorView)
    }

    /** feed 面板顶/底留白清零：BottomSpace=0 时抖音把 feed 视为“全屏(无底栏)”→视频方角；
     * 保留 BottomSpace 高度则一直按“首页带底栏”画圆角（v7.19 对照：恢复 v7.13 及以前的清零） */
    private fun zeroPanelSpaces() {
        val panel = panelRef?.get() ?: return
        val panelClass = panel.javaClass
        listOf("mTopSpace", "mBottomSpace").forEach { fieldName ->
            val field = runCatching {
                panelClass.getField(fieldName)
            }.getOrNull() ?: return@forEach
            val view = runCatching {
                field.get(panel) as? View
            }.getOrNull() ?: return@forEach
            val layoutParams = view.layoutParams ?: return@forEach
            if (layoutParams.height != 0) {
                layoutParams.height = 0
                if (spaceZeroedOnce.add(System.identityHashCode(view))) {
                    // 首次清零才主动布局一次以生效，之后交给抖音自身布局，避免反复重排抖动
                    view.requestLayout()
                }
                if (verbose) {
                    YLog.debug("$TAG: panel space $fieldName height set to 0")
                }
            }
        }
    }

    /** 打印 decor 视图树（深 7 层，限量），用于定位“挡在视频底部”的视图 */
    private fun dumpDecorChildren() {
        val activity = mainActivityRef?.get() ?: return
        val decor = activity.window?.decorView ?: return
        YLog.debug("$TAG: decor children dump (screen=${activity.resources.displayMetrics.heightPixels})")
        val counter = intArrayOf(0)
        fun dumpView(view: View, prefix: String, depth: Int) {
            if (depth > 7 || counter[0] > 400) {
                return
            }
            counter[0]++
            val lp = view.layoutParams
            val lpText = if (lp is ViewGroup.MarginLayoutParams) {
                "h=${lp.height} bottomMargin=${lp.bottomMargin}"
            } else {
                "lp=${lp?.javaClass?.simpleName}"
            }
            val idText = if (view.id != View.NO_ID) "id=0x${view.id.toString(16)}" else "id=no"
            YLog.debug(
                "$TAG: $prefix ${view.javaClass.name} vis=${view.visibility} " +
                    "top=${view.top} bottom=${view.bottom} $idText $lpText"
            )
            if (view is ViewGroup) {
                val count = view.childCount
                for (i in 0 until count) {
                    dumpView(view.getChildAt(i), "$prefix$i.", depth + 1)
                }
            }
        }
        dumpView(decor, "  ", 0)
    }

    /** 打印当前 feed 项根视图子树，定位底部被遮挡的视图 */
    private fun dumpHolderTree() {
        val panel = panelRef?.get()
        if (panel == null) {
            YLog.debug("$TAG: holder dump skipped, panel not captured")
            return
        }
        var root: View? = null
        runCatching {
            val holder = panel.javaClass.getMethod("getCurViewHolder").invoke(panel)
            root = holder?.javaClass?.getField("itemView")?.get(holder) as? View
            if (root == null) {
                YLog.debug("$TAG: holder dump: holder or itemView null (holder=${holder?.javaClass?.name})")
            }
        }.onFailure { e ->
            YLog.debug("$TAG: holder dump via holder failed: $e")
        }
        if (root == null) {
            // 回退：在 decor 里找第一个 VideoViewHolderRootView 打印
            val activity = mainActivityRef?.get()
            val loader = packageInstance.baseListFragmentPanel.selfClass?.classLoader
            if (activity == null || loader == null) {
                YLog.debug("$TAG: holder dump skipped, no fallback root")
                return
            }
            val found = ArrayList<View>()
            collectByClassName(
                activity.window?.decorView ?: return,
                loader,
                "com.ss.android.ugc.aweme.ad.feed.VideoViewHolderRootView",
                found
            )
            root = found.firstOrNull()
            if (root == null) {
                YLog.debug("$TAG: holder dump skipped, no VideoViewHolderRootView found")
                return
            }
            YLog.debug("$TAG: holder dump fallback: VideoViewHolderRootView")
        }
        YLog.debug("$TAG: holder itemView dump (root=${root.javaClass.name})")
        val rootHeight = root.bottom - root.top
        YLog.debug("$TAG: bottom-strip candidates (rootHeight=$rootHeight)")
        val counter = intArrayOf(0)
        fun dumpView(view: View, prefix: String, depth: Int) {
            if (depth > 10 || counter[0] > 350) {
                return
            }
            counter[0]++
            val lp = view.layoutParams
            val lpText = if (lp is ViewGroup.MarginLayoutParams) {
                "h=${lp.height} bottomMargin=${lp.bottomMargin} topMargin=${lp.topMargin}"
            } else {
                "lp=${lp?.javaClass?.simpleName}"
            }
            val idText = if (view.id != View.NO_ID) "id=0x${view.id.toString(16)}" else "id=no"
            YLog.debug(
                "$TAG:   $prefix ${view.javaClass.name} vis=${view.visibility} " +
                    "top=${view.top} bottom=${view.bottom} $idText $lpText"
            )
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    dumpView(view.getChildAt(i), "$prefix$i.", depth + 1)
                }
            }
        }
        dumpView(root, "", 0)
        // 底部 600px 内、可见且高度有限的视图候选（可能遮挡视频底部）
        val strip = ArrayList<View>()
        collectBottomStripCandidates(root, rootHeight, strip)
        for (v in strip) {
            val lp = v.layoutParams
            val lpText = if (lp is ViewGroup.MarginLayoutParams) {
                "h=${lp.height} bottomMargin=${lp.bottomMargin}"
            } else {
                "lp=${lp?.javaClass?.simpleName}"
            }
            val idText = if (v.id != View.NO_ID) "id=0x${v.id.toString(16)}" else "id=no"
            YLog.debug(
                "$TAG:   [strip] ${v.javaClass.name} vis=${v.visibility} " +
                    "top=${v.top} bottom=${v.bottom} $idText $lpText"
            )
        }
    }

    private fun collectBottomStripCandidates(view: View, rootHeight: Int, out: MutableList<View>) {
        if (view is ViewGroup) {
            val h = view.bottom - view.top
            if (view.visibility == View.VISIBLE && view.bottom >= rootHeight - 600 && h in 1..1600) {
                out.add(view)
            }
            for (i in 0 until view.childCount) {
                collectBottomStripCandidates(view.getChildAt(i), rootHeight, out)
            }
        } else if (view.visibility == View.VISIBLE) {
            val h = view.bottom - view.top
            if (view.bottom >= rootHeight - 600 && h in 1..1600) {
                out.add(view)
            }
        }
    }

    /** 打印窗口最底部 region 内所有可见视图（类/id/边界/高度），用于定位挡住视频底部的容器 */
    private fun dumpBottomRegionViews() {
        val activity = mainActivityRef?.get() ?: return
        val decor = activity.window?.decorView ?: return
        val screenH = decor.bottom - decor.top
        YLog.debug("$TAG: bottom-region views (screenH=$screenH)")
        val counter = intArrayOf(0)
        fun walk(view: View, prefix: String, depth: Int) {
            if (depth > 12 || counter[0] > 400) {
                return
            }
            counter[0]++
            val h = view.bottom - view.top
            if (view.visibility == View.VISIBLE && view.bottom >= screenH - 320 && h in 1..1800) {
                val idText = if (view.id != View.NO_ID) "id=0x${view.id.toString(16)}" else "id=no"
                val lp = view.layoutParams
                val lpText = if (lp is ViewGroup.MarginLayoutParams) {
                    "h=${lp.height} bottomMargin=${lp.bottomMargin}"
                } else {
                    "lp=${lp?.javaClass?.simpleName}"
                }
                YLog.debug(
                    "$TAG:   $prefix ${view.javaClass.name} top=${view.top} bottom=${view.bottom} $idText $lpText"
                )
            }
            if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    walk(view.getChildAt(i), "$prefix$i.", depth + 1)
                }
            }
        }
        walk(decor, "", 0)
    }

    /** 打印底栏/翻页器及其父容器结构与高度，定位 196px 由谁占用 */
    private fun dumpLayoutStructure() {
        val activity = mainActivityRef?.get() ?: return
        val decor = activity.window?.decorView ?: return
        val loader = packageInstance.baseListFragmentPanel.selfClass?.classLoader ?: return
        YLog.debug("$TAG: layout structure probe")
        listOf(
            "com.ss.android.ugc.aweme.homepage.ui.bottombar.MainBottomTabContainer",
            "com.ss.android.ugc.aweme.common.widget.VerticalViewPager",
            "com.ss.android.ugc.aweme.homepage.ui.view.MainScrollableViewPager"
        ).forEach { clsName ->
            val found = ArrayList<View>()
            collectByClassName(decor, loader, clsName, found)
            YLog.debug("$TAG:   $clsName -> ${found.size} instance(s)")
            found.forEachIndexed { idx, v ->
                YLog.debug(
                    "$TAG:     [$idx] ${v.javaClass.name} vis=${v.visibility} top=${v.top} bottom=${v.bottom} " +
                        "w=${v.right - v.left} lp=${describeLp(v.layoutParams)}"
                )
                var parent = v.parent
                var depth = 0
                while (parent is ViewGroup && depth < 4) {
                    val pv = parent
                    YLog.debug(
                        "$TAG:        parent$depth ${pv.javaClass.name} vis=${pv.visibility} top=${pv.top} " +
                            "bottom=${pv.bottom} w=${pv.right - pv.left} lp=${describeLp(pv.layoutParams)} children=${pv.childCount}"
                    )
                    parent = pv.parent
                    depth++
                }
            }
        }
    }

    /** 清爽隐藏时清零翻页器各级父容器底部 padding，让 RTViewPager 高度=父高（页面=全屏） */
    private fun adjustPagerAncestors(decorView: View, classLoader: ClassLoader, hidden: Boolean) {
        val pagers = ArrayList<View>()
        collectByClassName(decorView, classLoader, "com.ss.android.ugc.aweme.common.widget.VerticalViewPager", pagers)
        val touched = HashSet<Int>()
        pagers.forEach { pager ->
            var parent = pager.parent
            var depth = 0
            while (parent is ViewGroup && depth < 6) {
                val key = System.identityHashCode(parent)
                if (touched.add(key)) {
                    val original = pagerAncestorPadding[key] ?: parent.paddingBottom
                    pagerAncestorPadding[key] = original
                    val target = if (hidden) 0 else original
                    if (parent.paddingBottom != target) {
                        parent.setPadding(parent.paddingLeft, parent.paddingTop, parent.paddingRight, target)
                        if (verbose) {
                            YLog.debug("$TAG: pager ancestor ${parent.javaClass.name} bottom padding -> $target (orig $original)")
                        }
                    }
                }
                parent = parent.parent
                depth++
            }
        }
        if (!hidden) {
            pagerAncestorPadding.clear()
            hiddenBottomSpacers.clear()
        }
    }

    /**
     * 清爽隐藏(play)：翻页器去锚定 + 全高 → 视频铺满全屏；暂停(show)保持全屏不动。
     * 圆角机制（v7.19 验证）：只要暂停时 pager 保持全屏 3168 且 BottomSpace 清零，
     * 抖音就判定为“无底栏=方角”。暂停时文字被底栏挡的问题交给 applyPauseLift
     * （上移固定差值=屏幕高−原生pager高）处理，不再让 pager 缩回原生（那会切回圆角）。
     */
    private fun adjustPagerHeight(decorView: View, classLoader: ClassLoader, hidden: Boolean) {
        val pagers = ArrayList<View>()
        collectByClassName(decorView, classLoader, "com.ss.android.ugc.aweme.common.widget.VerticalViewPager", pagers)
        if (pagers.isEmpty()) {
            return
        }
        val screenH = decorView.bottom - decorView.top
        pagers.forEach { pager ->
            if (!hidden) {
                return@forEach
            }
            val key = System.identityHashCode(pager)
            val lp = pager.layoutParams ?: return@forEach
            val rlp = lp as? RelativeLayout.LayoutParams
            if (!pagerFullscreenChanged.contains(key)) {
                if (nativePagerHeight <= 0) {
                    val preH = pager.bottom - pager.top
                    if (preH > 0) {
                        nativePagerHeight = preH
                    }
                }
                pagerOriginalHeight[key] = lp.height
                pagerOriginalRuleAbove[key] = rlp?.getRule(RelativeLayout.ABOVE) ?: 0
                pagerOriginalRuleBelow[key] = rlp?.getRule(RelativeLayout.BELOW) ?: 0
                pagerFullscreenChanged.add(key)
            }
            var layoutChanged = false
            var anchors = ""
            if (rlp != null) {
                val above = rlp.getRule(RelativeLayout.ABOVE)
                val below = rlp.getRule(RelativeLayout.BELOW)
                if (above != 0) {
                    rlp.removeRule(RelativeLayout.ABOVE)
                    anchors += "above=0x${above.toString(16)} "
                    layoutChanged = true
                }
                if (below != 0) {
                    rlp.removeRule(RelativeLayout.BELOW)
                    anchors += "below=0x${below.toString(16)}"
                    layoutChanged = true
                }
            } else if (verbose) {
                YLog.debug("$TAG: RTViewPager lp is ${lp.javaClass.name}, not RelativeLayout.LayoutParams; skip anchor removal")
            }
            if (lp.height != screenH) {
                lp.height = screenH
                layoutChanged = true
            }
            if (layoutChanged) {
                pager.layoutParams = lp
            }
            val laidOutHeight = pager.bottom - pager.top
            if (pagerFullscreenChanged.contains(key) && laidOutHeight < screenH - 2) {
                pager.requestLayout()
                (pager.parent as? View)?.requestLayout()
                if (verbose) {
                    YLog.debug(
                        "$TAG: RTViewPager full -> $screenH (orig ${pagerOriginalHeight[key]}, anchors ${anchors.ifEmpty {
                            "none"
                        }})"
                    )
                }
            }
        }
    }

    /**
     * 暂停(show=true)：把左下作者/描述、右侧竖列、旋转唱片整体上移
     * (屏幕高 − 原生pager高) 像素，避开恢复显示的底栏；只设固定值、不做坐标测量，
     * 150/450ms 补打两次抗抖音动画/晚渲染覆盖；播放隐藏(false)时复位。
     */
    private fun applyPauseLift(decorView: View, classLoader: ClassLoader, show: Boolean) {
        val targets = ArrayList<View>()
        pauseLiftClassNames.forEach { className -> collectByClassName(decorView, classLoader, className, targets) }
        if (!show) {
            targets.forEach { view ->
                val original = pauseLiftedViews.remove(System.identityHashCode(view))
                if (original != null && view.translationY != original) {
                    view.translationY = original
                }
            }
            return
        }
        val screenH = decorView.bottom - decorView.top
        val delta = if (nativePagerHeight > 0 && screenH > nativePagerHeight) {
            (screenH - nativePagerHeight).toFloat()
        } else {
            100f
        }
        var lifted = 0
        targets.forEach { view ->
            if (view.visibility == View.VISIBLE && view.height > 0) {
                val key = System.identityHashCode(view)
                if (!pauseLiftedViews.containsKey(key)) {
                    pauseLiftedViews[key] = view.translationY
                }
                if (view.translationY != -delta) {
                    view.translationY = -delta
                    lifted++
                }
            }
        }
        if (verbose && targets.isNotEmpty()) {
            YLog.debug("$TAG: pause lift up ${-delta.toInt()}px, matched=${targets.size} lifted=$lifted")
        }
    }

    /** 首次进入清爽约 2s：底栏 显示→隐藏 一次，让抖音按 BottomSpace=0 重估 → 视频方角 */
    private fun doFirstEntryBarFlip() {
        val activity = mainActivityRef?.get() ?: return
        val loader = packageInstance.baseListFragmentPanel.selfClass?.classLoader ?: return
        val decor = activity.window?.decorView ?: return
        val bars = ArrayList<View>()
        collectByClassName(decor, loader, "com.ss.android.ugc.aweme.homepage.ui.bottombar.MainBottomTabContainer", bars)
        val bar = bars.firstOrNull() ?: return
        bar.visibility = View.VISIBLE
        if (verbose) {
            YLog.debug("$TAG: first-entry bar flip -> shown")
        }
        mainHandler.postDelayed({
            bar.visibility = View.GONE
            if (verbose) {
                YLog.debug("$TAG: first-entry bar flip -> hidden (rounding eval refresh)")
            }
        }, 300L)
    }

    private fun describeLp(lp: ViewGroup.LayoutParams?): String {
        if (lp == null) {
            return "null"
        }
        val mlp = lp as? ViewGroup.MarginLayoutParams
        val weight = runCatching {
            lp.javaClass.getField("weight").get(lp)
        }.getOrNull()
        return "h=${lp.height} w=${lp.width} bottomMargin=${mlp?.bottomMargin} weight=$weight"
    }

    private fun collectByClassName(view: View, classLoader: ClassLoader, className: String, out: MutableList<View>) {
        val clazz = classCache.getOrPut(className) {
            runCatching {
                classLoader.loadClass(className)
            }.getOrNull()
        }
        if (clazz != null && clazz.isInstance(view)) {
            out.add(view)
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                collectByClassName(view.getChildAt(i), classLoader, className, out)
            }
        }
    }

    private fun collectOverlayViews(view: View, classLoader: ClassLoader, out: MutableList<View>) {
        if (matchesOverlayClass(view, classLoader)) {
            out.add(view)
        }
        if (view is ViewGroup) {
            val childCount = view.childCount
            for (i in 0 until childCount) {
                collectOverlayViews(view.getChildAt(i), classLoader, out)
            }
        }
    }

    private fun matchesOverlayClass(view: View, classLoader: ClassLoader): Boolean = hideClassNames.any { className ->
        val clazz = classCache.getOrPut(className) {
            runCatching {
                classLoader.loadClass(className)
            }.getOrNull()
        }
        clazz != null && clazz.isInstance(view)
    }
}
