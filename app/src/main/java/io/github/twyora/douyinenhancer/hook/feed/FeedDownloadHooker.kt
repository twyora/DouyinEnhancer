package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.kavaref.extension.toClassOrNull
import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
import io.github.twyora.douyinenhancer.config.key.SaveKey
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.HookTransaction
import io.github.twyora.douyinenhancer.utils.getField
import io.github.twyora.douyinenhancer.utils.invokeMethod
import io.github.twyora.douyinenhancer.utils.invokeStaticMethod
import io.github.twyora.douyinenhancer.utils.resolveMethod
import io.github.twyora.douyinenhancer.utils.setField
import java.lang.reflect.Modifier

@HookOnMainProcess
object FeedDownloadHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private const val GALLERY_SHARE_HELPER =
        "com.ss.android.ugc.aweme.share.video.GalleryShareHelper"
    private const val AWEME_CLASS = "com.ss.android.ugc.aweme.feed.model.Aweme"

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

    override fun onHook() {
        if (!FastKVConfigManager.settings.getBoolean(SaveKey.FEED_DOWNLOAD_BYPASS, false)) {
            if (verbose) {
                YLog.debug("$TAG: feed download bypass disabled, skip feed download hooks")
            }
            return
        }
        val transaction = HookTransaction(TAG)

        transaction.add(::installForceActionStatusNormalHook.name) {
            installForceActionStatusNormalHook()
        }
        transaction.add(::installOverrideAwemeDownloadStatusHook.name) {
            installOverrideAwemeDownloadStatusHook()
        }
        transaction.add(::installOverridePrivacyVideoDownloadStatusHook.name) {
            installOverridePrivacyVideoDownloadStatusHook()
        }

        if (packageInstance.hostVersionCode() >= 390601) {
            // 39.6.0+: AbsPermissionChecker / ActionStatus are gone and the download
            // pipeline was rewritten (DownloadAction -> LX/1deN). The legacy hooks
            // above silently no-op. Install the replacement chain below. All anchors
            // are stable class names / signatures (GalleryShareHelper, Aweme,
            // UrlModel.getUrlList, MultiStateDownloadViewHolder, DownloadAction.enable)
            // so they survive the 39.9.0 obfuscation reshuffle (X.1Vkh -> X.1DM8).
            installAwemeGetDownloadStatusHook()
            installMultiStateDownloadHook()
            installLongPressSaveItemHook()
            installNeedDownloadActionForceHook()
            installShareFunctionAdapterHook()
            installPermissionResultForceNormalHook()
            installClearDownloadFailInfoHook()
            installBlockedCheckBypassHook()
            installUrlListChokePointHook()
            installDownloadActionEnableHook()
            installConsumerPermissionForceNormalHook()
            installDownloadAddrOverrideHook()
        }

        transaction.commit()
    }

    private fun installForceActionStatusNormalHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.absPermissionChecker.selfClass?.resolveMethod(
            packageInstance.absPermissionChecker.getActionCheckResult()
        )?.hook {
            after {
                val actionCheckResult = result ?: run {
                    YLog.warn(
                        "$TAG: action permission check result is null, cannot force action status to allowed, download may be blocked"
                    )
                    return@after
                }
                val actionStatus = actionCheckResult.getField<Any>(
                    packageInstance.actionCheckResult.actionStatus()
                ) ?: run {
                    YLog.warn("$TAG: action status is null, cannot decide whether to force it to allowed")
                    return@after
                }

                val normalStatus = packageInstance.actionStatus.selfClass?.invokeStaticMethod<Any>(
                    packageInstance.actionStatus.valueOf(),
                    packageInstance.actionStatus.normal().name
                )

                if (actionStatus != normalStatus) {
                    YLog.info("$TAG: forcing action status from $actionStatus to $normalStatus to allow download")
                    actionCheckResult.setField(
                        packageInstance.actionCheckResult.actionStatus(),
                        normalStatus
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook action permission check, download action may stay blocked", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, action status cannot be forced to allowed, download may be blocked")
            }
        }
    }

    private fun installOverrideAwemeDownloadStatusHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.galleryShareHelper.selfClass?.resolveMethod(
            packageInstance.galleryShareHelper.startDownload()
        )?.hook {
            before {
                val aweme = args[0] ?: return@before
                val downloadStatus = aweme.invokeMethod<Int>(
                    packageInstance.aweme.getDownloadStatus()
                )

                if (verbose) {
                    YLog.debug("$TAG: aweme download status: $downloadStatus")
                }

                if (downloadStatus != 0) {
                    YLog.info("$TAG: resetting aweme download status from $downloadStatus to 0 to allow download")
                    aweme.getField<Any>(
                        packageInstance.aweme.status()
                    )?.setField(
                        packageInstance.awemeStatus.downloadStatus(),
                        0
                    )
                }
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook gallery share download, aweme download status may stay restricted", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, aweme download status cannot be reset, download may be blocked")
            }
        }
    }

    private fun installOverridePrivacyVideoDownloadStatusHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.sharePrivacyVideoApi.selfClass?.resolveMethod(
            packageInstance.sharePrivacyVideoApi.getDownloadStatus()
        )?.hook {
            before {
                val itemId = args[0] as? String
                if (verbose) {
                    YLog.debug("$TAG: privacy video download status query aweme id: $itemId")
                }

                val response = packageInstance.sharePrivacyVideoApi.privacyVideoResponse.selfClass?.createInstance() ?: run {
                    YLog.error("$TAG: failed to build the allowed-download response, privacy video download may stay blocked")
                    return@before
                }
                response.setField(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.msg(),
                    ""
                )
                response.setField(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.status(),
                    0
                )

                val observable = packageInstance.rxObservable.selfClass?.invokeStaticMethod<Any>(
                    packageInstance.rxObservable.just(),
                    response
                ) ?: run {
                    YLog.error("$TAG: failed to return the allowed-download response, privacy video download may stay blocked")
                    return@before
                }
                result = observable
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to hook privacy video download status, host will not treat it as downloadable", throwable)
            }
            onHookingFailure {
                YLog.error("$TAG: hook failed, privacy video download status cannot be faked, download stays blocked")
            }
        }
    }

    /**
     * 39.6.0+: AwemeStatus fields are obfuscated (downloadStatus -> single-letter),
     * so the field-write path in [installOverrideAwemeDownloadStatusHook] silently
     * no-ops. Hooking the getter directly is version-agnostic: Aweme.getDownloadStatus()
     * is kept across 39.6.0/39.9.0, and forcing it to 0 covers every read site.
     */
    private fun installAwemeGetDownloadStatusHook() {
        packageInstance.aweme.selfClass?.resolveMethod(
            packageInstance.aweme.getDownloadStatus()
        )?.hook {
            after {
                result = 0
            }
        } ?: YLog.error("$TAG: Aweme.getDownloadStatus not resolved")
    }

    /**
     * 39.6.0+/39.9.0: share-panel 保存本地 greys forbidden-download awemes.
     *
     * Real grey path (smali, SocialActionsAdapter.onBindViewHolder):
     *   v7 = panelState.LIZLLL  (ShareService.LJJJJL can-download)
     *   v5 = panelState.LJFF    (AwemeControl.b && !teenMode)
     *   if (!(v7 && v5)) icon/label alpha = 0.34f  // greys the button
     * O5() is only a click-side grey-disable (f GONE / g VISIBLE / b alpha0) and
     * often never runs when just opening the panel — so ctor/O5-only force is not enough.
     *
     * Fix layers (stable class names, no X.* hardcodes):
     * 1) MultiStateDownloadViewHolder ctor after — baseline force-enabled
     * 2) O5-like zero-arg void — skip click-side grey path
     * 3) SocialActionsAdapter.onBindViewHolder after — force icon/label alpha=1
     *    every time the row is bound (covers the real panel-open grey path)
     */
    private fun installMultiStateDownloadHook() {
        val holderClassName =
            "com.ss.android.ugc.aweme.share.socialpanel.viewholder.MultiStateDownloadViewHolder"
        val clazz = holderClassName.toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: MultiStateDownloadViewHolder not found")
            return
        }
        YLog.info("$TAG: installing MultiStateDownloadViewHolder grey-bypass hooks")
        clazz.declaredConstructors.firstOrNull()?.hook {
            after {
                val forced = forceAllViewFieldsEnabled(instance)
                YLog.info("$TAG: MultiStateDownloadViewHolder ctor force-enabled ($forced views)")
            }
        }
        // 39.9.0 O5()V is the only non-static zero-arg void instance method on this class.
        val greyMethods =
            clazz.declaredMethods.filter {
                !Modifier.isStatic(it.modifiers) &&
                    it.returnType == Void.TYPE &&
                    it.parameterTypes.isEmpty() &&
                    !it.name.startsWith("access$")
            }
        if (greyMethods.isEmpty()) {
            YLog.warn("$TAG: MultiStateDownloadViewHolder O5-like method not found")
        }
        greyMethods.forEach { method ->
            method.hook {
                before {
                    resultNull()
                    val forced = forceAllViewFieldsEnabled(instance)
                    YLog.info(
                        "$TAG: blocked MultiStateDownloadViewHolder.${method.name}() " +
                            "grey-disable (forced $forced views)"
                    )
                }
            }
        }

        // Bind-time alpha grey (primary path for panel open). 39.9.0 has TWO
        // social-panel adapters that both bind download rows:
        //   SocialActionsPanelContentAdapter (main share-panel content grid)
        //   SocialActionsAdapter (action-bar row, MultiStateDownloadViewHolder)
        // Hook both — force every bound row's View fields bright.
        val adapterClasses = listOf(
            "com.ss.android.ugc.aweme.share.socialpanel.adapter.SocialActionsPanelContentAdapter",
            "com.ss.android.ugc.aweme.share.socialpanel.adapter.SocialActionsAdapter"
        )
        for (adapterName in adapterClasses) {
            val adapterClass = adapterName.toClassOrNull(appClassLoader) ?: continue
            val bindMethod =
                adapterClass.declaredMethods.firstOrNull {
                    it.name.contains("onBindViewHolder") && it.parameterTypes.size == 2
                } ?: run {
                    YLog.error("$TAG: $adapterName.onBindViewHolder not found")
                    continue
                }
            bindMethod.hook {
                after {
                    val holder = args.getOrNull(0) ?: return@after
                    val forced = forceAllViewFieldsEnabled(holder)
                    if (forced > 0) {
                        YLog.info(
                            "$TAG: ${adapterName.substringAfterLast('.')}.bind " +
                                "${holder.javaClass.simpleName} force-enabled ($forced views)"
                        )
                    }
                }
            }
            YLog.info("$TAG: hooked $adapterName.onBindViewHolder")
        }
    }

    /**
     * 39.9.0 分享面板功能按钮真实渲染 adapter：
     *   im/share/aweme/only/sharepanel/ui/ShareFunctionAdapter
     * 「保存本地」= DownloadFunction。onBindViewHolder 可用分支条件：
     *   holder.a(LX/1DCl).LJJI == true 且 X.1DTv.LJ(...)==true
     * 条件不满足 → 只画灰图标、不绑 OnClickListener → 点了无反应。
     * before-hook：在原生绑定逻辑跑之前把 LJJI 设 true，让原生代码自己绑点击；
     * after-hook：强制 alpha=1 去掉灰色。未混淆稳定类。
     */
    private fun installShareFunctionAdapterHook() {
        val clazz =
            "com.ss.android.ugc.aweme.im.share.aweme.only.sharepanel.ui.ShareFunctionAdapter"
                .toClassOrNull(appClassLoader) ?: run {
                YLog.error("$TAG: ShareFunctionAdapter not found")
                return
            }
        val bindMethod = clazz.declaredMethods.firstOrNull {
            it.name.contains("onBindViewHolder") && it.parameterTypes.size == 2
        } ?: run {
            YLog.error("$TAG: ShareFunctionAdapter.onBindViewHolder not found")
            return
        }
        // BEFORE: force LJJI=true so the native "available" branch runs.
        // AFTER: force alpha + manually bind click on the SaveLocalFunction row.
        // The native available branch also requires X.1DTv.LJ==true (removed —
        // it broke the friend row), so the click listener is never bound natively;
        // we attach our own listener that calls SaveLocalFunction.LJIILIIL().
        bindMethod.hook {
            before {
                val holder = args.getOrNull(0) ?: return@before
                forceSaveAvailableFlag(holder)
            }
            after {
                val holder = args.getOrNull(0) ?: return@after
                val forced = forceAllViewFieldsEnabled(holder)
                if (forced > 0 && verbose) {
                    YLog.info(
                        "$TAG: ShareFunctionAdapter.bind ${holder.javaClass.simpleName} " +
                            "force-enabled ($forced views)"
                    )
                }
                bindSaveLocalClick(instance, holder, args.getOrNull(1))
            }
        }
        YLog.info("$TAG: hooked ShareFunctionAdapter.onBindViewHolder")
    }

    /** Lazily resolve the download function with the host's own class loader.
     *  Runtime log proved the 保存本地 button on 39.9.0 is DownloadFunction,
     *  not SaveLocalFunction (which never appears in the bound function list). */
    private var saveLocalClassCache: Class<*>? = null
    private val boundFunctionTypes = mutableSetOf<String>()
    private fun resolveSaveLocalFunctionClass(host: Any): Class<*>? {
        saveLocalClassCache?.let { return it }
        val cl = host.javaClass.classLoader ?: return null
        val c = runCatching {
            cl.loadClass("com.ss.android.ugc.aweme.im.share.aweme.only.sharepanel.function.DownloadFunction")
        }.getOrNull()
        if (c != null) saveLocalClassCache = c
        else YLog.warn("$TAG: DownloadFunction class not loadable via host loader")
        return c
    }

    /**
     * On the row whose bound IShareFunction is a SaveLocalFunction, attach our own
     * click listener that invokes LJIILIIL(). Every failure path logs a reason.
     */
    private fun bindSaveLocalClick(adapter: Any, holder: Any, position: Any?) {
        val clazz = resolveSaveLocalFunctionClass(holder) ?: return
        runCatching {
            val pos = position as? Int
            if (pos == null) {
                YLog.warn("$TAG: bindSaveLocalClick: position arg is ${position?.javaClass}")
                return
            }
            val func = getBoundShareFunction(adapter, pos)
            if (func == null) {
                YLog.warn("$TAG: bindSaveLocalClick: no function resolved at pos $pos")
                return
            }
            val funcName = func.javaClass.name
            if (boundFunctionTypes.add(funcName)) {
                YLog.info("$TAG: bindSaveLocalClick: function class seen: $funcName")
            }
            if (!clazz.isInstance(func)) {
                // not the save row — expected for most rows, stay quiet
                return
            }
            val itemViewField = findFieldByName(holder, "itemView")
            if (itemViewField == null) {
                YLog.warn("$TAG: bindSaveLocalClick: itemView field not found on ${holder.javaClass.name}")
                return
            }
            itemViewField.isAccessible = true
            val itemView = itemViewField.get(holder) as? android.view.View
            if (itemView == null) {
                YLog.warn("$TAG: bindSaveLocalClick: itemView is null at pos $pos")
                return
            }
            YLog.info(
                "$TAG: bindSaveLocalClick: pos $pos itemView=${itemView.javaClass.name} " +
                    "clickable=${itemView.isClickable} hasListener=${itemView.hasOnClickListeners()}"
            )
            // Diagnostic touch listener: fires before click dispatch so we can tell
            // whether touch events reach this view at all.
            itemView.setOnTouchListener { v, ev ->
                if (ev.actionMasked == android.view.MotionEvent.ACTION_DOWN) {
                    YLog.info("$TAG: DownloadFunction row touched (view=${v.javaClass.simpleName})")
                }
                false // don't consume — let normal click dispatch continue
            }
            val lastClick = longArrayOf(0)
            val clickListener = android.view.View.OnClickListener {
                val now = System.currentTimeMillis()
                if (now - lastClick[0] < 800) return@OnClickListener
                lastClick[0] = now
                YLog.info("$TAG: DownloadFunction click intercepted")
                runCatching {
                    val m = clazz.getMethod("LJIILIIL")
                    val r = m.invoke(func)
                    YLog.info("$TAG: DownloadFunction.LJIILIIL() -> $r")
                }.onFailure {
                    YLog.error("$TAG: DownloadFunction click failed: $it")
                }
            }
            // Bind on the whole row subtree so no matter which child receives the
            // touch, the action fires (itemView itself may only be a container).
            bindClickRecursive(itemView, clickListener)
            YLog.info("$TAG: bound manual click on DownloadFunction row (pos $pos)")
        }.onFailure {
            YLog.warn("$TAG: bindSaveLocalClick failed: $it")
        }
    }

    private fun bindClickRecursive(root: android.view.View, listener: android.view.View.OnClickListener) {
        root.setOnClickListener(listener)
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                bindClickRecursive(root.getChildAt(i), listener)
            }
        }
    }

    /**
     * Get the IShareFunction bound at [position] from the adapter's item list.
     * adapter.f = List<LX/1DU0>, LX/1DU0.LIZ = IShareFunction.
     */
    private fun getBoundShareFunction(adapter: Any?, position: Any?): Any? {
        val pos = position as? Int ?: return null
        val listField = findFieldByName(adapter ?: return null, "f") ?: return null
        return runCatching {
            listField.isAccessible = true
            val list = listField.get(adapter) as? List<*> ?: return null
            val item = list.getOrNull(pos) ?: return null
            val funcField = findFieldByName(item, "LIZ") ?: return null
            funcField.isAccessible = true
            funcField.get(item)
        }.getOrNull()
    }

    /**
     * Locate holder.a (LX/1DCl) and set its boolean "save available" gate to true.
     * The gate field is LJJI in the 39.9.0 APK.
     */
    private fun forceSaveAvailableFlag(holder: Any) {
        runCatching {
            // 1) find the X.1DCl field on the holder (by type, fallback by name "a")
            val dclField = findFieldByType(holder, "X.1DCl") ?: findFieldByName(holder, "a")
            if (dclField == null) {
                YLog.warn("$TAG: holder(${holder.javaClass.name}) has no X.1DCl field")
                return
            }
            dclField.isAccessible = true
            val dclObj = dclField.get(holder)
            if (dclObj == null) {
                YLog.warn("$TAG: holder.a (X.1DCl) value is null at bind time")
                return
            }
            // 2) find the LJJI boolean gate on the X.1DCl instance
            val gate = findFieldByName(dclObj, "LJJI")
            if (gate == null) {
                YLog.warn("$TAG: LJJI not found on ${dclObj.javaClass.name}")
                return
            }
            gate.isAccessible = true
            if (!gate.getBoolean(dclObj)) {
                gate.setBoolean(dclObj, true)
                YLog.info("$TAG: ShareFunctionAdapter LJJI forced true (save click enabled)")
            }
        }.onFailure {
            YLog.error("$TAG: forceSaveAvailableFlag failed: ${it.javaClass.simpleName} ${it.message}")
        }
    }

    /** Find first field (own + superclasses) whose type name matches [typeName].
     *  typeName is a Java-style class name (dots), e.g. "X.1DCl". */
    private fun findFieldByType(obj: Any, typeName: String): java.lang.reflect.Field? {
        var clz: Class<*>? = obj.javaClass
        while (clz != null && clz != Any::class.java) {
            for (field in clz.declaredFields) {
                val t = field.type.name
                if (t == typeName) return field
            }
            clz = clz.superclass
        }
        return null
    }

    /** Find first field (own + superclasses) with the exact [name]. */
    private fun findFieldByName(obj: Any, name: String): java.lang.reflect.Field? {
        var clz: Class<*>? = obj.javaClass
        while (clz != null && clz != Any::class.java) {
            for (field in clz.declaredFields) {
                if (field.name == name) return field
            }
            clz = clz.superclass
        }
        return null
    }

    /**
     * 39.9.0 失败视频（作者关闭下载）的根因：下载执行器 UGFiles 保存链读
     *   aweme.videoControl.downloadInfo.failInfo.reason（=「作者已关闭下载功能」）
     * 来决定是否中止保存。外层权限 hook（LIZLLL→NORMAL）已让下载启动，但执行器
     * 内部直接读视频元数据。修复：hook LX/1DM8.execute()（DownloadAction）before，
     * 反射清空 this.b(Aweme).videoControl.downloadInfo.failInfo.reason，
     * 让执行器认为可下载。字段全部未混淆（downloadInfo/failInfo/reason）。
     */
    private fun installClearDownloadFailInfoHook() {
        val clazz = "X.1DM8".toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: X.1DM8 (DownloadAction) not found")
            return
        }
        clazz.declaredMethods
            .filter {
                it.name == "execute" && it.parameterTypes.size == 2 &&
                    it.parameterTypes[1].name.contains("SharePackage")
            }
            .forEach { method ->
                method.hook {
                    before {
                        runCatching {
                            val awemeField = findFieldByName(instance, "b") ?: return@runCatching
                            awemeField.isAccessible = true
                            val aweme = awemeField.get(instance) ?: return@runCatching
                            clearFailInfoReason(aweme)
                        }
                    }
                }
            }
        YLog.info("$TAG: installed LX/1DM8.execute failInfo-clear hook")
    }

    /** Rewrite aweme download-blocked markers so downloader proceeds.
     *  Runtime correlation proved the real gate:
     *    video.K == true  → 作者关闭下载 → 下载失败
     *    video.K == false → 全部下载成功
     *  Set video.K=false and null downloadInfo on execute. */
    private fun clearFailInfoReason(aweme: Any) {
        // 1) video.K = false (the author-download-blocked flag)
        val video = runCatching {
            aweme.javaClass.getMethod("getVideo").invoke(aweme)
        }.getOrNull()
        if (video != null) {
            val kField = findFieldByName(video, "K")
            if (kField != null) {
                runCatching {
                    kField.isAccessible = true
                    if (kField.getBoolean(video)) {
                        kField.setBoolean(video, false)
                        YLog.info("$TAG: video.K true -> false (author-download-blocked removed)")
                    }
                }
            }
        }
        // 2) videoControl.downloadInfo = null (failInfo gate)
        val vc = getFieldPath(aweme, "videoControl") ?: return
        val diField = findFieldByName(vc, "downloadInfo") ?: return
        diField.isAccessible = true
        if (diField.get(vc) != null) {
            diField.set(vc, null)
            YLog.info("$TAG: cleared videoControl.downloadInfo -> null (failInfo gate removed)")
        } else {
            YLog.info("$TAG: downloadInfo already null")
        }
    }

    private fun getFieldPath(obj: Any, name: String): Any? {
        val f = findFieldByName(obj, name) ?: return null
        return runCatching {
            f.isAccessible = true
            f.get(obj)
        }.getOrNull()
    }

    /**
     * 39.9.0 分享面板「保存本地」下载权限链（多层）：
     *   LX/1DM8.execute() 调 IConsumerPermissionService.LIZLLL(Aweme)（外层）
     *      → ConsumerPermissionServiceImp.LIZLLL → LX/1Dic.LIZLLL(LX/1Dio)（底层规则遍历）
     *   UGFiles 保存链还可能独立再查一次权限。
     * 实测分发（LX/1DF6.LIZ 映射表）：NORMAL(0)→:cond_ef 真下载，GRAYED→复制链接，HIDDEN→return。
     * 已 hook ConsumerPermissionServiceImp.LIZLLL → NORMAL（下载能启动），但失败视频在
     * 下载执行器内部又弹「作者已关闭下载功能」——说明底层 LX/1Dic.LIZLLL 还有独立消费点。
     * 补 hook LX/1Dic.LIZLLL(LX/1Dio)→NORMAL，覆盖所有底层权限判定。
     */
    private fun installPermissionResultForceNormalHook() {
        val targets = listOf(
            "com.ss.android.ugc.aweme.spi.ConsumerPermissionServiceImp",
            "X.1Dic",
        )
        for (t in targets) {
            val clazz = t.toClassOrNull(appClassLoader) ?: continue
            val method = clazz.declaredMethods.firstOrNull {
                it.name == "LIZLLL" && it.parameterTypes.size == 1
            } ?: run {
                YLog.error("$TAG: $t.LIZLLL not found")
                continue
            }
            var normalEnum: Any? = null
            var dmgCtor: java.lang.reflect.Constructor<*>? = null
            method.hook {
                before {
                    runCatching {
                        if (normalEnum == null) {
                            val df5 = "X.1DF5".toClassOrNull(appClassLoader) ?: return@runCatching
                            normalEnum = df5.enumConstants?.firstOrNull {
                                (it as? Enum<*>)?.name == "NORMAL"
                            }
                            val dmg = "X.1DMG".toClassOrNull(appClassLoader) ?: return@runCatching
                            dmgCtor = dmg.declaredConstructors.firstOrNull()
                            YLog.info("$TAG: LIZLLL force prep ($t): NORMAL=${normalEnum != null} dmgCtor=${dmgCtor != null}")
                        }
                        val df7 = "X.1DF7".toClassOrNull(appClassLoader) ?: return@runCatching
                        val dmg = dmgCtor?.newInstance("", 0)
                        val ok = df7.declaredConstructors.firstOrNull()?.newInstance(normalEnum, dmg)
                        if (ok != null) {
                            result = ok
                            YLog.info("$TAG: forced $t.LIZLLL -> NORMAL (download branch)")
                        }
                    }
                }
            }
            YLog.info("$TAG: installed $t.LIZLLL -> NORMAL hook")
        }
    }

    /**
     * 39.9.0 长按面板/分享面板统一判定点：ShareExtServiceImpl.needDownloadAction()。
     * 未混淆稳定类，被 LPPSaveVideoItem / LPPSaveVideoModule / ActionsManager 等多处调用。
     * Force to true so 保存视频 entry always renders enabled.
     */
    private fun installNeedDownloadActionForceHook() {
        val impClass =
            "com.ss.android.ugc.aweme.share.ShareExtServiceImpl"
                .toClassOrNull(appClassLoader)
        if (impClass != null) {
            val needMethod = impClass.declaredMethods.firstOrNull {
                it.name == "needDownloadAction" &&
                    it.returnType == Boolean::class.java &&
                    it.parameterTypes.size == 2
            }
            if (needMethod != null) {
                needMethod.hook {
                    after {
                        if (result == false) {
                            result = true
                            YLog.info(
                                "$TAG: forced ShareExtServiceImpl.needDownloadAction " +
                                    "-> true (allow download action)"
                            )
                        }
                    }
                }
                YLog.info("$TAG: hooked ShareExtServiceImpl.needDownloadAction")
            } else {
                YLog.error("$TAG: ShareExtServiceImpl.needDownloadAction not found")
            }
        } else {
            YLog.error("$TAG: ShareExtServiceImpl not found")
        }

        // Permission-service unified entry (spi 包稳定类). It is the final arbiter
        // behind needDownloadAction on 39.9.0:
        //   ConsumerPermissionServiceImp.LJFF(request) -> LX/1Dic.LJIIJ(request)
        val clazz =
            "com.ss.android.ugc.aweme.spi.ConsumerPermissionServiceImp"
                .toClassOrNull(appClassLoader) ?: run {
                YLog.error("$TAG: ConsumerPermissionServiceImp not found")
                return
            }
        val methods =
            clazz.declaredMethods
                .filter {
                    it.returnType == Boolean::class.java &&
                        it.parameterTypes.size == 1 &&
                        it.parameterTypes[0].name == "X.1DJ4"
                }
                .toMutableList()
        if (methods.isEmpty()) {
            // fallback: match by name if not obfuscated (LJFF may survive)
            methods.addAll(
                clazz.declaredMethods.filter {
                    it.name == "LJFF" && it.returnType == Boolean::class.java &&
                        it.parameterTypes.size == 1
                }
            )
        }
        if (methods.isEmpty()) {
            YLog.error("$TAG: ConsumerPermissionServiceImp.LJFF not found")
            return
        }
        methods.forEach { method ->
            method.hook {
                after {
                    if (result == false) {
                        result = true
                        YLog.info(
                            "$TAG: forced ConsumerPermissionServiceImp.${method.name}() " +
                                "-> true (allow download action)"
                        )
                    }
                }
            }
        }
        YLog.info("$TAG: installed ConsumerPermissionServiceImp permission-force hooks (${methods.size})")
    }

    /**
     * 39.9.0 long-press panel (feed/long_press_panel) save-video entry:
     * LPPSaveVideoItem.LIZ() (instance) and LIZLLL(params) (static) both return
     * Visibility (0 = VISIBLE, 8 = GONE). Blocked-download awemes get GONE or the
     * row is greyed via the same permission checks. Force both to VISIBLE.
     * Class name stable; method names obfuscated — match by signature (int return,
     * zero/one-arg).
     */
    private fun installLongPressSaveItemHook() {
        val clazz =
            "com.ss.android.ugc.aweme.feed.long_press_panel.item.bussiness.LPPSaveVideoItem"
                .toClassOrNull(appClassLoader) ?: run {
                YLog.error("$TAG: LPPSaveVideoItem not found")
                return
            }
        clazz.declaredMethods
            .filter {
                it.returnType == Integer.TYPE &&
                    (it.parameterTypes.isEmpty() || it.parameterTypes.size == 1) &&
                    !Modifier.isStatic(it.modifiers) ||
                    (it.returnType == Integer.TYPE &&
                        it.parameterTypes.size == 1 &&
                        Modifier.isStatic(it.modifiers))
            }
            .forEach { method ->
                method.hook {
                    before {
                        resultNull()
                        result = 0 // VISIBLE
                        YLog.info("$TAG: LPPSaveVideoItem.${method.name}() -> VISIBLE")
                    }
                }
            }
        YLog.info("$TAG: installed LPPSaveVideoItem visibility hooks")
    }

    /** Force every View field on the instance (own + superclasses) enabled/opaque/clickable. */
    private fun forceAllViewFieldsEnabled(instance: Any): Int {
        var forced = 0
        var clz: Class<*>? = instance.javaClass
        while (clz != null && clz != Any::class.java) {
            for (field in clz.declaredFields) {
                if (!android.view.View::class.java.isAssignableFrom(field.type)) continue
                runCatching {
                    field.isAccessible = true
                    val view = field.get(instance) as? android.view.View ?: return@runCatching
                    forceViewEnabled(view)
                    forced++
                }
            }
            clz = clz.superclass
        }
        return forced
    }

    private fun forceViewEnabled(root: android.view.View) {
        root.isEnabled = true
        root.alpha = 1.0f
        root.isClickable = true
        if (root is android.view.ViewGroup) {
            for (i in 0 until root.childCount) {
                forceViewEnabled(root.getChildAt(i))
            }
        }
    }

    /**
     * 39.6.0+: the download executor uses Video.downloadAddr (obfuscated to "v",
     * watermarked) instead of playAddr. Swap downloadAddr to playAddr before the
     * download starts. Field names differ across versions (39.6: a/v, older:
     * playAddr/downloadAddr), selected by host versionCode.
     */
    private fun installDownloadAddrOverrideHook() {
        val clazz = GALLERY_SHARE_HELPER.toClassOrNull(appClassLoader) ?: return
        val newObfuscated = packageInstance.hostVersionCode() >= 390601
        val playAddrField = if (newObfuscated) "a" else "playAddr"
        val downloadAddrField = if (newObfuscated) "v" else "downloadAddr"

        clazz.declaredMethods
            .filter {
                !Modifier.isAbstract(it.modifiers) &&
                    it.returnType == Void.TYPE &&
                    it.parameterTypes.size == 2 &&
                    it.parameterTypes[0].name == AWEME_CLASS &&
                    it.parameterTypes[1] == String::class.java
            }
            .forEach { method ->
                method.hook {
                    before {
                        val aweme = args[0] ?: return@before
                        val video = aweme.invokeMethod<Any>(
                            packageInstance.aweme.getVideo()
                        ) ?: return@before
                        val playAddr = video.getField<Any>(Field(playAddrField)) ?: return@before
                        video.setField(Field(downloadAddrField), playAddr)
                        YLog.info("$TAG: replaced video.$downloadAddrField with playAddr (watermark-free)")
                    }
                }
            }
    }

    /**
     * 39.6.0+: GalleryShareHelper.LJIILLIIL(Aweme) returns TRUE only for
     * normally-downloadable videos, FALSE for author-blocked videos. In the
     * download source selection, false -> downloadAddr (watermarked) and
     * true -> playAddr (clean). Force the check to TRUE so blocked videos take
     * the clean playAddr branch.
     */
    private fun installBlockedCheckBypassHook() {
        val helperClass = GALLERY_SHARE_HELPER.toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: GalleryShareHelper not found for blocked-check bypass")
            return
        }
        val blockedMethod = helperClass.declaredMethods.firstOrNull {
            it.returnType == Boolean::class.java &&
                it.parameterTypes.size == 1 &&
                it.parameterTypes[0].name == AWEME_CLASS
        } ?: run {
            YLog.error("$TAG: GalleryShareHelper blocked-check method not found")
            return
        }
        blockedMethod.hook {
            after {
                if (result == false) {
                    YLog.info("$TAG: GalleryShareHelper blocked-check false -> true (use clean playAddr branch)")
                    result = true
                }
            }
        }
    }

    /**
     * 39.6.0+: UrlModel.getUrlList() is the single choke point every downloader
     * uses to obtain video URLs (all routes: share panel, long-press, multi-image).
     * Hook it to strip watermark=1 -> watermark=0 AND replace douyinvod CDN direct
     * links with the first api-play URL (CDN mp4s bake the watermark into the source).
     * UrlModel is a stable class name — zero changes for 39.9.0.
     */
    private fun installUrlListChokePointHook() {
        val urlModelClass = "com.ss.android.ugc.aweme.base.model.UrlModel".toClassOrNull(appClassLoader) ?: run {
            YLog.error("$TAG: UrlModel not found for choke-point hook")
            return
        }
        val getUrlList = urlModelClass.declaredMethods.firstOrNull {
            it.name == "getUrlList" && it.parameterTypes.isEmpty()
        } ?: run {
            YLog.error("$TAG: UrlModel.getUrlList() not found")
            return
        }
        getUrlList.hook {
            after {
                val list = result as? List<*> ?: return@after
                var changed = false
                val cleaned = java.util.ArrayList<Any>(list.size)
                for (url in list) {
                    val s = url as? String ?: continue
                    val c = s.replace("watermark=1", "watermark=0")
                    if (c != s) changed = true
                    cleaned.add(c)
                }
                // The downloader uses the FIRST URL in the list. For author-blocked
                // videos the first entries are douyinvod.com CDN direct links that
                // CARRY the watermark baked into the mp4 (no URL param to strip) —
                // only api-play/aweme/v1/play/ URLs honor watermark=0.
                val apiPlay = cleaned.filter { it.toString().contains("aweme/v1/play") }
                val others = cleaned.filter { !it.toString().contains("aweme/v1/play") }
                val cdnDirect = others.filter { it.toString().contains("douyinvod.com") }
                if (apiPlay.isNotEmpty() && cdnDirect.isNotEmpty()) {
                    val firstClean = apiPlay.first()
                    val reordered = java.util.ArrayList<Any>(cleaned.size)
                    for (url in cleaned) {
                        if (url.toString().contains("douyinvod.com") && !url.toString().contains("aweme/v1/play")) {
                            reordered.add(firstClean)
                        } else {
                            reordered.add(url)
                        }
                    }
                    result = reordered
                    changed = true
                    YLog.info("$TAG: getUrlList() replaced ${cdnDirect.size} cdn-direct -> api-play (clean)")
                } else if (apiPlay.isNotEmpty() && others.isNotEmpty() && cleaned.isNotEmpty() && cleaned[0] !in apiPlay) {
                    val reordered = java.util.ArrayList<Any>(apiPlay.size + others.size)
                    reordered.addAll(apiPlay)
                    reordered.addAll(others)
                    result = reordered
                    changed = true
                    YLog.info("$TAG: getUrlList() reordered: api-play first (${apiPlay.size} clean + ${others.size} cdn)")
                } else if (changed) {
                    result = cleaned
                    YLog.info("$TAG: getUrlList() cleaned ${list.size} URLs (watermark=1 -> 0)")
                }
            }
        }
    }

    /**
     * 39.6.0+: the share-panel 保存本地 button's grey state is decided by
     * DownloadAction (implements SheetAction) enable(). It checks downloadStatus /
     * paidSeries / videoControl conditions. Force enable() to true so the button is
     * always enabled. The DownloadAction class is located by DexKit feature rule
     * (SheetAction + downloadImage) so it survives the 39.9.0 rename (X.1Vkh -> X.1DM8).
     */
    private fun installDownloadActionEnableHook() {
        val clazz = packageInstance.downloadAction.selfClass ?: run {
            YLog.error("$TAG: DownloadAction class not resolved")
            return
        }
        val enableMethod = clazz.declaredMethods.firstOrNull {
            it.name == "enable" && it.parameterTypes.isEmpty() && it.returnType == Boolean::class.java
        } ?: run {
            YLog.error("$TAG: DownloadAction.enable() not found")
            return
        }
        enableMethod.hook {
            after {
                result = true
                YLog.info("$TAG: forced DownloadAction.enable() -> true")
            }
        }
    }

    /**
     * 39.6.0: DownloadAction.execute() gets LX/1Vf7 (status enum LX/12cv +
     * reason LX/1Vkr) from IConsumerPermissionService.LIZIZ(aweme), then:
     *   NORMAL -> real download; GRAYED -> copy-link toast; HIDDEN -> toast only.
     * Force the status to NORMAL at the LX/1Vf7 constructor so every read site
     * sees a downloadable video. On 39.9.0 this hook harmlessly no-ops (enable()
     * reads getDownloadStatus() directly, X.1Vf7 no longer referenced) — kept for
     * 39.6.0 compatibility.
     */
    private fun installConsumerPermissionForceNormalHook() {
        val normalClass =
            "X.12cv".toClassOrNull(appClassLoader) ?: run {
                YLog.error("$TAG: X.12cv (permission status enum) not found")
                return
            }
        val normalEnum = runCatching {
            normalClass.enumConstants?.firstOrNull { (it as? Enum<*>)?.name == "NORMAL" }
        }.getOrNull() ?: run {
            YLog.error("$TAG: X.12cv.NORMAL not found")
            return
        }
        val resultClass =
            "X.1Vf7".toClassOrNull(appClassLoader) ?: run {
                YLog.error("$TAG: X.1Vf7 (permission result) not found")
                return
            }
        val ctor = resultClass.declaredConstructors.firstOrNull() ?: run {
            YLog.error("$TAG: X.1Vf7 constructor not found")
            return
        }
        ctor.hook {
            before {
                val status = args.getOrNull(0)
                if (status != null && status != normalEnum) {
                    YLog.info("$TAG: forced X.1Vf7 status $status -> NORMAL")
                    args[0] = normalEnum
                }
            }
        }
    }
}
