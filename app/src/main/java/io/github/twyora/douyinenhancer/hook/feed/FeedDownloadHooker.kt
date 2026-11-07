package io.github.twyora.douyinenhancer.hook.feed

import com.highcapable.kavaref.extension.createInstance
import com.highcapable.yukihookapi.extension.kavaref.intercept
import com.highcapable.yukihookapi.hook.core.YukiHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.hook.HookTransaction
import io.github.twyora.douyinenhancer.utils.reflect.getFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.getStaticFieldOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.invokeStaticMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.resolveMethodOrNull
import io.github.twyora.douyinenhancer.utils.reflect.setFieldQuietly

@HookOnMainProcess
object FeedDownloadHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    override fun onHook() {
        if (!ConfigManager.save.feedDownloadBypass.value) {
            YLog.info("$TAG: bypass feed download is disabled, skipping hook")
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

        transaction.commit()
    }

    private fun installForceActionStatusNormalHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.absPermissionChecker.selfClass?.resolveMethodOrNull(
            packageInstance.absPermissionChecker.getActionCheckResult()
        )?.intercept {
            val actionCheckResult = proceed() ?: run {
                YLog.warn(
                    "$TAG: action permission check result is null"
                )
                return@intercept null
            }
            val actionStatus = actionCheckResult.getFieldOrNull(
                packageInstance.actionCheckResult.actionStatus()
            ) ?: run {
                YLog.warn("$TAG: action status is null")
                return@intercept actionCheckResult
            }

            val normalStatus = packageInstance.actionStatus.selfClass?.getStaticFieldOrNull(
                packageInstance.actionStatus.normal()
            )

            if (actionStatus != normalStatus) {
                if (verbose) {
                    YLog.debug("$TAG: forcing action status from $actionStatus to $normalStatus to allow download")
                }
                actionCheckResult.setFieldQuietly(
                    packageInstance.actionCheckResult.actionStatus(),
                    normalStatus
                )
            }
            actionCheckResult
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to force feed download status to normal", throwable)
            }
        }
    }

    private fun installOverrideAwemeDownloadStatusHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.galleryShareHelper.selfClass?.resolveMethodOrNull(
            packageInstance.galleryShareHelper.startDownload()
        )?.intercept {
            val aweme = args.firstOrNull {
                packageInstance.aweme.selfClass?.isInstance(it) == true
            } ?: run {
                YLog.error("$TAG: aweme not found in argument list")
                return@intercept proceed()
            }
            val downloadStatus = aweme.invokeMethodOrNull<Int>(
                packageInstance.aweme.getDownloadStatus()
            )?.also {
                if (verbose) {
                    YLog.debug("$TAG: aweme download status: $it")
                }
            }

            if (downloadStatus != 0) {
                if (verbose) {
                    YLog.debug("$TAG: resetting aweme download status from $downloadStatus to 0 to allow download")
                }
                aweme.getFieldOrNull(
                    packageInstance.aweme.status()
                )?.setFieldQuietly(
                    packageInstance.awemeStatus.downloadStatus(),
                    0
                )
            }
            proceed()
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to reset aweme download status", throwable)
            }
        }
    }

    private fun installOverridePrivacyVideoDownloadStatusHook(): YukiHookCreator.MemberHooker<*>.Handle? {
        return packageInstance.sharePrivacyVideoApi.selfClass?.resolveMethodOrNull(
            packageInstance.sharePrivacyVideoApi.getDownloadStatus()
        )?.intercept {
            val itemId = args.firstNotNullOfOrNull {
                it as? String
            }
            if (verbose) {
                YLog.debug("$TAG: privacy video download status query aweme id: $itemId")
            }

            val response = packageInstance.sharePrivacyVideoApi.privacyVideoResponse.selfClass?.createInstance()?.apply {
                setFieldQuietly(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.msg(),
                    ""
                )
                setFieldQuietly(
                    packageInstance.sharePrivacyVideoApi.privacyVideoResponse.status(),
                    0
                )
            } ?: run {
                YLog.error("$TAG: failed to build the allowed-download response")
                return@intercept proceed()
            }

            packageInstance.rxObservable.selfClass?.invokeStaticMethodOrNull(
                packageInstance.rxObservable.just(),
                response
            ) ?: run {
                YLog.error("$TAG: failed to warp the allowed-download response")
                return@intercept proceed()
            }
        }?.apply {
            onCallbackFailure { _, throwable ->
                YLog.error("$TAG: failed to bypass privacy video download status check", throwable)
            }
        }
    }
}
