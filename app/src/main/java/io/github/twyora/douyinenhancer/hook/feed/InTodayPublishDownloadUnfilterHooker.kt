package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.getStaticFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setFieldQuietly

/**
 * Fix #255: In-today-publish feed media does not allow download
 */
@HookOnMainProcess
object InTodayPublishDownloadUnfilterHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    val downloadId by lazy {
        with(packageInstance.lppConfigModuleId) {
            selfClass?.getStaticFieldOrNull(
                download()
            )?.invokeMethodOrNull<String>(
                getId()
            )
        }
    }

    override fun onHook() {
        if (!ConfigManager.save.unfilterTodayPubDownload.value) {
            YLog.info("$TAG: unfilter in-today-publish download is disabled, skipping hook")
            return
        }

        if (packageInstance.hostVersionCode() < 400301) {
            // The old story panel function group list implementation
            //  is completely removed starting from host version 40.3.0.
            // Filtering here is appropriate.
            installDownloadIntoLegacyStoryPanelFunctionGroupListHook()
        }

        // Ensure download exists in the story panel config
        installDownloadIntoSharePanelConfigHook()
        // Also confirm download is included in the display allowlist
        installDownloadIntoStoryPanelAllowlistHook()
    }

    private fun installDownloadIntoLegacyStoryPanelFunctionGroupListHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        check(packageInstance.hostVersionCode() < 400301)

        // In general, hardcoding a host-sourced feature list is unacceptable.
        // The correct approach is to fetch the list from the host’s data source.
        // But the host inlines this hardcoded list at the call site, which prevents us from extracting its source.
        // Since the relevant host implementation is removed in later versions, this list does not need ongoing maintenance.
        // Having reached our so‑called maintainability objective, we tolerate this imperfect implementation for now.
        val defaultFunctionGroupList by lazy {
            with(packageInstance.lppConfigModuleId) {
                val toStoryId = selfClass?.getStaticFieldOrNull(
                    toStory()
                )?.invokeMethodOrNull<String>(getId())

                val dislikeId = selfClass?.getStaticFieldOrNull(
                    dislike()
                )?.invokeMethodOrNull<String>(getId())

                val reportId = selfClass?.getStaticFieldOrNull(
                    report()
                )?.invokeMethodOrNull<String>(getId())

                val blockPostId = selfClass?.getStaticFieldOrNull(
                    blockPost()
                )?.invokeMethodOrNull<String>(getId())

                val authId = selfClass?.getStaticFieldOrNull(
                    auth()
                )?.invokeMethodOrNull<String>(getId())

                val timingCloseId = selfClass?.getStaticFieldOrNull(
                    timingClose()
                )?.invokeMethodOrNull<String>(getId())

                val autoFeedId = selfClass?.getStaticFieldOrNull(
                    autoFeed()
                )?.invokeMethodOrNull<String>(getId())

                val autoFeedPadId = selfClass?.getStaticFieldOrNull(
                    autoFeedPad()
                )?.invokeMethodOrNull<String>(getId())

                listOfNotNull(
                    listOfNotNull(toStoryId),
                    listOfNotNull(dislikeId, downloadId, reportId),
                    listOfNotNull(timingCloseId, autoFeedId, autoFeedPadId),
                    listOfNotNull(blockPostId, authId)
                ).filter {
                    it.isNotEmpty()
                }.takeIf {
                    it.isNotEmpty()
                }
            }
        }

        YLog.info(
            "$TAG: resolved legacy story panel function default group list by module:" +
                " $defaultFunctionGroupList"
        )

        return packageInstance.story25LongPressPanelConfig.selfClass?.resolveMethodOrNull(
            packageInstance.story25LongPressPanelConfig.getFunctionGroupList()
        )?.intercept {
            val functionGroupList = proceed<List<List<*>?>?>()

            if (functionGroupList.isNullOrEmpty()) {
                YLog.warn("$TAG: legacy story panel has no function groups, using defaults: $defaultFunctionGroupList")

                defaultFunctionGroupList
            } else if (downloadId.isNullOrBlank()) {
                YLog.error("$TAG: save-to-album id unresolved, legacy story panel groups left unchanged")

                functionGroupList
            } else {
                val allowDownload = functionGroupList.any { group ->
                    group?.contains(downloadId) ?: false
                }

                if (!allowDownload) {
                    YLog.info("$TAG: save-to-album missing from legacy story panel groups, adding to first group")

                    functionGroupList.map {
                        it?.toMutableList()
                    }.apply {
                        firstOrNull()?.add(downloadId)?.also {
                            YLog.info("$TAG: save-to-album added to first legacy story panel group")
                        }
                    }
                } else {
                    if (verbose) {
                        YLog.debug("$TAG: save-to-album already present in legacy story panel groups")
                    }
                    functionGroupList
                }
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook legacy story panel function group list getter", throwable)
            }
        }
    }

    private fun installDownloadIntoSharePanelConfigHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.sharePanelFuncConfig.selfClass?.resolveMethodOrNull(
            packageInstance.sharePanelFuncConfig.getFuncConfig()
        )?.intercept {
            // A list of lines; each item represents a button rendered in a distinct region on the panel.
            val shareLines = proceed<MutableList<Any?>>() ?: run {
                YLog.warn("$TAG: story panel button config is null, leaving it unchanged")
                return@intercept null
            }

            // Look for a download button among these lines
            val allowDownload = shareLines.any { shareLine ->
                val shareBtns = shareLine?.getFieldOrNull<List<*>>(
                    packageInstance.sharePanelLine.line()
                )
                shareBtns?.any { btn ->
                    btn?.getFieldOrNull(
                        packageInstance.sharePanelBtn.key()
                    ) == downloadId
                } ?: false
            }

            run addDownloadLine@{
                if (!allowDownload) {
                    // If none is found, create a download button, wrap it as a list,
                    //  add it to the region line, then append to the final result.

                    val downloadBtn = packageInstance.sharePanelBtn.selfClass?.createInstance()?.apply {
                        this.setFieldQuietly(
                            packageInstance.sharePanelBtn.key(),
                            downloadId
                        )
                    }
                    if (downloadBtn == null) {
                        YLog.error("$TAG: failed to create save-to-album button for story panel")
                        return@addDownloadLine
                    }

                    val downloadLine = packageInstance.sharePanelLine.selfClass?.createInstance()?.apply {
                        this.setFieldQuietly(
                            packageInstance.sharePanelLine.line(),
                            listOf(downloadBtn)
                        )
                    }
                    if (downloadLine == null) {
                        YLog.error("$TAG: failed to create save-to-album row for story panel")
                        return@addDownloadLine
                    }

                    shareLines.add(downloadLine).also {
                        YLog.info("$TAG: save-to-album added to story panel button config")
                    }
                }
            }

            shareLines
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook story panel button config getter", throwable)
            }
        }
    }

    private fun installDownloadIntoStoryPanelAllowlistHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        // We also need to handle whitelist validation for buttons shown on the story panel.
        return packageInstance.story25SharePanelConfig.selfClass?.resolveMethodOrNull(
            packageInstance.story25SharePanelConfig.getOtherFunctionWhiteList()
        )?.intercept {
            val whiteList = proceed<List<*>>()

            whiteList?.let { list ->
                if (downloadId in list) {
                    list
                } else {
                    list.toMutableList().apply {
                        add(downloadId)
                        YLog.info("$TAG: save-to-album added to story panel button allowlist")
                    }
                }
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to hook story panel button allowlist getter", throwable)
            }
        }
    }
}
