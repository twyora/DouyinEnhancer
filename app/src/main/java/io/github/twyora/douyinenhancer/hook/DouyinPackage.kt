/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package io.github.twyora.douyinenhancer.hook

import android.content.Context
import android.os.Build
import com.google.protobuf.Descriptors
import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.asParameterizedTypeOrNull
import com.highcapable.yukihookapi.YukiHook
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.provider.ModuleConfigProvider
import io.github.twyora.douyinenhancer.constant.HookInfoFiles
import io.github.twyora.douyinenhancer.constant.SignatureKeys
import io.github.twyora.douyinenhancer.generated.AppProperties
import io.github.twyora.douyinenhancer.utils.RequireThen.requireAllNotNullThen
import io.github.twyora.douyinenhancer.utils.RequireThen.requireNotNullThen
import io.github.twyora.douyinenhancer.utils.crypto.verifySha256RsaSignature
import io.github.twyora.douyinenhancer.utils.reflect.Field
import io.github.twyora.douyinenhancer.utils.reflect.Method
import io.github.twyora.douyinenhancer.utils.reflect.toClass
import io.github.twyora.douyinenhancer.utils.weak
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import kotlin.io.encoding.Base64
import kotlin.time.measureTimedValue
import org.erdtman.jcs.JsonCanonicalizer
import org.json.JSONObject
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.MatchType
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.enums.UsingType
import org.luckypray.dexkit.query.matchers.base.AccessFlagsMatcher
import org.luckypray.dexkit.query.matchers.base.IntRange
import org.luckypray.dexkit.query.matchers.base.OpCodesMatcher
import org.luckypray.dexkit.result.FieldUsingType

val Configs.Class.nameOrNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Field.nameOrNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Method.nameOrNull
    get() = if (hasName()) {
        name
    } else {
        null
    }

val Configs.Method.Parameters.valuesListOrNull
    get() = valuesList.ifEmpty {
        null
    }

class DouyinPackage(private val classLoader: ClassLoader, context: Context) {
    private val hookInfo: Configs.HookInfo = run {
        val (result, time) = measureTimedValue {
            readHookInfo(context)
        }

        if (verbose) {
            YLog.debug("$TAG: load hookInfo time: $time")
            YLog.debug("$TAG: hookInfo: $result")
        }

        result
    }

    /**
     * NOTE: Use this function solely as a guard for legacy code that has been removed.
     * It is NOT intended for feature specialization based on host version!
     *
     * Misuse will create fragile version coupling.
     * Any exceptional usage must have a valid justified at the call site;
     *  otherwise it will not be accepted during code review.
     */
    fun hostVersionCode() = hookInfo.hostVersionCode

    val commentImageStruct = CommentImageStructModule(hookInfo.commentImageStruct, classLoader)
    val urlModel = UrlModelModule(hookInfo.urlModel, classLoader)
    val comment = CommentModule(hookInfo.comment, classLoader)
    val commentAudioStruct = CommentAudioStructModule(hookInfo.commentAudioStruct, classLoader)
    val emoji = EmojiModule(hookInfo.emoji, classLoader)
    val commentActionParams = CommentActionParamsModule(hookInfo.commentActionParams, classLoader)
    val commentLongPressItemModel = CommentLongPressItemModelModule(hookInfo.commentLongPressItemModel, classLoader)
    val saveImageActionItem = SaveImageActionItemModule(hookInfo.saveImageActionItem, classLoader)
    val listenerProviderParam = ListenerProviderParamModule(hookInfo.listenerProviderParam, classLoader)
    val commentImageSaveDownloadListener = CommentImageSaveDownloadListenerModule(hookInfo.commentImageSaveDownloadListener, classLoader)
    val downloadInfo = DownloadInfoModule(hookInfo.downloadInfo, classLoader)
    val digestUtils = DigestUtilsModule(hookInfo.digestUtils, classLoader)
    val ugFileUtils = UGFileUtilsKtModule(hookInfo.ugFileUtils, classLoader)
    val commonItemView = CommonItemViewModule(hookInfo.commonItemView, classLoader)
    val douYinSettingNewVersionActivity = DouYinSettingNewVersionActivityModule(hookInfo.douYinSettingNewVersionActivity, classLoader)
    val user = UserModule(hookInfo.user, classLoader)
    val aweme = AwemeModule(hookInfo.aweme, classLoader)
    val video = VideoModule(hookInfo.video, classLoader)
    val imageUrlStruct = ImageUrlStructModule(hookInfo.imageUrlStruct, classLoader)
    val feedResponseHandler = FeedResponseHandlerModule(hookInfo.feedResponseHandler, classLoader)
    val commentLongPressWhiteListProvider = CommentLongPressWhiteListProviderModule(hookInfo.commentLongPressWhiteListProvider, classLoader)
    val miscDownloadAddrUtil = MiscDownloadAddrUtilModule(hookInfo.miscDownloadAddrUtil, classLoader)
    val downloadAction = DownloadActionModule(hookInfo.downloadAction, classLoader)
    val abTestServiceImpl = ABTestServiceImplModule(hookInfo.abTestServiceImpl, classLoader)
    val awemeStatistics = AwemeStatisticsModule(hookInfo.awemeStatistics, classLoader)
    val downLoadExecutor = DownLoadExecutorModule(hookInfo.downLoadExecutor, classLoader)
    val absTask = AbsTaskModule(hookInfo.absTask, classLoader)
    val downloadLivePhotoExecutor = DownloadLivePhotoExecutorModule(hookInfo.downloadLivePhotoExecutor, classLoader)
    val mainActivity = MainActivityModule(hookInfo.mainActivity, classLoader)
    val absPermissionChecker = AbsPermissionCheckerModule(hookInfo.absPermissionChecker, classLoader)
    val actionCheckResult = ActionCheckResultModule(hookInfo.actionCheckResult, classLoader)
    val actionStatus = ActionStatusModule(hookInfo.actionStatus, classLoader)
    val galleryShareHelper = GalleryShareHelperModule(hookInfo.galleryShareHelper, classLoader)
    val awemeStatus = AwemeStatusModule(hookInfo.awemeStatus, classLoader)
    val sharePrivacyVideoApi = SharePrivacyVideoApiModule(hookInfo.sharePrivacyVideoApi, classLoader)
    val rxObservable = RxObservableModule(hookInfo.rxObservable, classLoader)
    val listenAwemeFilter = ListenAwemeFilterModule(hookInfo.listenAwemeFilter, classLoader)
    val baseListFragmentPanel = BaseListFragmentPanelModule(hookInfo.baseListFragmentPanel, classLoader)
    val videoPlayerStatus = VideoPlayerStatusModule(hookInfo.videoPlayerStatus, classLoader)
    val videoEvent = VideoEventModule(hookInfo.videoEvent, classLoader)
    val cleanModePresenter = CleanModePresenterModule(hookInfo.cleanModePresenter, classLoader)
    val topCornerEntranceDataSource = TopCornerEntranceDataSourceModule(hookInfo.topCornerEntranceDataSource, classLoader)
    val topCornerEntranceData = TopCornerEntranceDataModule(hookInfo.topCornerEntranceData, classLoader)
    val compilationSpeedInheritComponent = CompilationSpeedInheritComponentModule(hookInfo.compilationSpeedInheritComponent, classLoader)
    val danmakuView = DanmakuViewModule(hookInfo.danmakuView, classLoader)
    val fluxComponentId = FluxComponentIdModule(hookInfo.fluxComponentId, classLoader)
    val fluxComponentDataAction = FluxComponentDataActionModule(hookInfo.fluxComponentDataAction, classLoader)
    val heif = HeifModule(hookInfo.heif, classLoader)
    val heifData = HeifDataModule(hookInfo.heifData, classLoader)
    val closeableReference = CloseableReferenceModule(hookInfo.closeableReference, classLoader)
    val storyServiceImpl = StoryServiceImplModule(hookInfo.storyServiceImpl, classLoader)
    val homeTabDataSourceServer = HomeTabDataSourceServerModule(hookInfo.homeTabDataSourceServer, classLoader)
    val homeTabDataSourceDefault = HomeTabDataSourceDefaultModule(hookInfo.homeTabDataSourceDefault, classLoader)
    val feedAvatarPresenter = FeedAvatarPresenterModule(hookInfo.feedAvatarPresenter, classLoader)
    val playStatusHelper = PlayStatusHelperModule(hookInfo.playStatusHelper, classLoader)
    val heifImage = HeifImageModule(hookInfo.heifImage, classLoader)
    val heifFrame = HeifFrameModule(hookInfo.heifFrame, classLoader)
    val pinchViewModel = PinchViewModelModule(hookInfo.pinchViewModel, classLoader)
    val dismissWindowEvent = DismissWindowEventModule(hookInfo.dismissWindowEvent, classLoader)
    val story25LongPressPanelConfig: Story25LongPressPanelConfigModule
        get() = run {
            check(hostVersionCode() < 400301) {
                "Story25LongPressPanelConfig has been removed since host version 40.3.0"
            }
            Story25LongPressPanelConfigModule(hookInfo.story25LongPressPanelConfig, classLoader)
        }
    val lppConfigModuleId = LPPConfigModuleIdModule(hookInfo.lppConfigModuleId, classLoader)
    val story25SharePanelConfig = Story25SharePanelConfigModule(hookInfo.story25SharePanelConfig, classLoader)
    val sharePanelFuncConfig = SharePanelFuncConfigModule(hookInfo.sharePanelFuncConfig, classLoader)
    val sharePanelLine = SharePanelLineModule(hookInfo.sharePanelLine, classLoader)
    val sharePanelBtn = SharePanelBtnModule(hookInfo.sharePanelBtn, classLoader)

    class CommentImageStructModule internal constructor(
        private val configs: Configs.CommentImageStruct,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun originUrl() = Field(configs.originUrl.nameOrNull)

        fun downloadUrl() = Field(configs.downloadUrl.nameOrNull)

        fun getDownloadUrl() = Method(
            configs.getDownloadUrl.nameOrNull,
            configs.getDownloadUrl.parameters.valuesListOrNull
        )
    }

    class UrlModelModule internal constructor(private val configs: Configs.UrlModel, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun urlList() = Field(configs.urlList.nameOrNull)
    }

    class CommentModule internal constructor(private val configs: Configs.Comment, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun emoji() = Field(configs.emoji.nameOrNull)

        fun imageList() = Field(configs.imageList.nameOrNull)

        fun commentAudio() = Field(configs.commentAudio.nameOrNull)
    }

    class CommentAudioStructModule internal constructor(
        private val configs: Configs.CommentAudioStruct,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun content() = Field(configs.content.nameOrNull)
    }

    class EmojiModule internal constructor(private val configs: Configs.Emoji, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun animateUrl() = Field(configs.animateUrl.nameOrNull)
    }

    class CommentActionParamsModule internal constructor(
        private val configs: Configs.CommentActionParams,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun comment() = Field(configs.comment.nameOrNull)
    }

    class CommentLongPressItemModelModule internal constructor(
        private val configs: Configs.CommentLongPressItemModel,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun commentActionParams() = Field(configs.commentActionParams.nameOrNull)
    }

    class SaveImageActionItemModule internal constructor(
        private val configs: Configs.SaveImageActionItem,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun isVisible() = Method(
            configs.isVisible.nameOrNull,
            configs.isVisible.parameters.valuesListOrNull
        )

        fun onClick() = Method(
            configs.onClick.nameOrNull,
            configs.onClick.parameters.valuesListOrNull
        )
    }

    class ListenerProviderParamModule internal constructor(
        private val configs: Configs.ListenerProviderParam,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun context() = Field(configs.context.nameOrNull)

        fun cert() = Field(configs.cert.nameOrNull)
    }

    class CommentImageSaveDownloadListenerModule internal constructor(
        private val configs: Configs.CommentImageSaveDownloadListener,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun onSuccessed() = Method(
            configs.onSuccessed.nameOrNull,
            configs.onSuccessed.parameters.valuesListOrNull
        )

        fun notifyResult() = Method(
            configs.notifyResult.nameOrNull,
            configs.notifyResult.parameters.valuesListOrNull
        )

        fun listenerProviderParam() = Field(configs.listenerProviderParam.nameOrNull)
    }

    class DownloadInfoModule internal constructor(private val configs: Configs.DownloadInfo, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun url() = Field(configs.url.nameOrNull)

        fun getTargetFilePath() = Method(
            configs.getTargetFilePath.nameOrNull,
            configs.getTargetFilePath.parameters.valuesListOrNull
        )
    }

    class DigestUtilsModule internal constructor(private val configs: Configs.DigestUtils, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun md5Hex() = Method(
            configs.md5Hex.nameOrNull,
            configs.md5Hex.parameters.valuesListOrNull
        )
    }

    class UGFileUtilsKtModule internal constructor(private val configs: Configs.UGFileUtilsKt, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun context() = Field(configs.context.nameOrNull)

        fun copyFile() = Method(
            configs.copyFile.nameOrNull,
            configs.copyFile.parameters.valuesListOrNull
        )

        fun getStorageDir() = Method(
            configs.getStorageDir.nameOrNull,
            configs.getStorageDir.parameters.valuesListOrNull
        )

        fun getExternalStorageDir() = Method(
            configs.getExternalStorageDir.nameOrNull,
            configs.getExternalStorageDir.parameters.valuesListOrNull
        )

        fun getImageUri() = Method(
            configs.getImageUri.nameOrNull,
            configs.getImageUri.parameters.valuesListOrNull
        )

        fun createUri() = Method(
            configs.createUri.nameOrNull,
            configs.createUri.parameters.valuesListOrNull
        )

        fun getAudioUri() = Method(
            configs.getAudioUri.nameOrNull,
            configs.getAudioUri.parameters.valuesListOrNull
        )
    }

    class CommonItemViewModule internal constructor(private val configs: Configs.CommonItemView, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun setLeftText() = Method(
            configs.setLeftText.nameOrNull,
            configs.setLeftText.parameters.valuesListOrNull
        )

        fun setRightUIMode() = Method(
            configs.setRightUiMode.nameOrNull,
            configs.setRightUiMode.parameters.valuesListOrNull
        )

        fun setLeftIcon() = Method(
            configs.setLeftIcon.nameOrNull,
            configs.setLeftIcon.parameters.valuesListOrNull
        )

        fun setRightText() = Method(
            configs.setRightText.nameOrNull,
            configs.setRightText.parameters.valuesListOrNull
        )
    }

    class DouYinSettingNewVersionActivityModule internal constructor(
        private val configs: Configs.DouYinSettingNewVersionActivity,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun settingsScrollView() = Field(
            configs.settingsScrollView.nameOrNull
        )

        fun onResume() = Method(
            configs.onResume.nameOrNull,
            configs.onResume.parameters.valuesListOrNull
        )
    }

    class MainActivityModule internal constructor(private val configs: Configs.MainActivity, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun onResume() = Method(
            configs.onResume.nameOrNull,
            configs.onResume.parameters.valuesListOrNull
        )

        fun onNewIntent() = Method(
            configs.onNewIntent.nameOrNull,
            configs.onNewIntent.parameters.valuesListOrNull
        )
    }

    class UserModule internal constructor(private val configs: Configs.User, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun nickname() = Field(configs.nickname.nameOrNull)

        fun uid() = Field(configs.uid.nameOrNull)

        companion object {
            const val FOLLOW_STATUS_UNFOLLOWED = 0
            const val FOLLOW_STATUS_FOLLOWING = 1
//            const val FOLLOW_STATUS_MUTUAL_FOLLOW = 2
        }
    }

    class AwemeModule internal constructor(private val configs: Configs.Aweme, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun desc() = Field(configs.desc.nameOrNull)

        fun author() = Field(configs.author.nameOrNull)

        fun getAd() = Method(
            configs.getAd.nameOrNull,
            configs.getAd.parameters.valuesListOrNull
        )

        fun itemTitle() = Field(configs.itemTitle.nameOrNull)

        fun duration() = Field(configs.duration.nameOrNull)

        fun isNormalVideo() = Method(
            configs.isNormalVideo.nameOrNull,
            configs.isNormalVideo.parameters.valuesListOrNull
        )

        fun isEcomAweme() = Method(
            configs.isEcomAweme.nameOrNull,
            configs.isEcomAweme.parameters.valuesListOrNull
        )

        fun grouponLargeCard() = Field(configs.grouponLargeCard.nameOrNull)

        fun isLive() = Method(
            configs.isLive.nameOrNull,
            configs.isLive.parameters.valuesListOrNull
        )

        fun isMultiImage() = Method(
            configs.isMultiImage.nameOrNull,
            configs.isMultiImage.parameters.valuesListOrNull
        )

        fun getVideo() = Method(
            configs.getVideo.nameOrNull,
            configs.getVideo.parameters.valuesListOrNull
        )

        fun images() = Field(configs.images.nameOrNull)

        fun statistics() = Field(configs.statistics.nameOrNull)

        fun getAid() = Method(
            configs.getAid.nameOrNull,
            configs.getAid.parameters.valuesListOrNull
        )

        fun getDownloadStatus() = Method(
            configs.getDownloadStatus.nameOrNull,
            configs.getDownloadStatus.parameters.valuesListOrNull
        )

        fun aid() = Field(configs.aid.nameOrNull)

        fun status() = Field(configs.status.nameOrNull)

        fun getFollowStatus() = Method(
            configs.getFollowStatus.nameOrNull,
            configs.getFollowStatus.parameters.valuesListOrNull
        )
    }

    class AwemeStatusModule internal constructor(private val configs: Configs.AwemeStatus, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun downloadStatus() = Field(configs.downloadStatus.nameOrNull)
    }

    class SharePrivacyVideoApiModule internal constructor(
        private val configs: Configs.SharePrivacyVideoApi,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getDownloadStatus() = Method(
            configs.getDownloadStatus.nameOrNull,
            configs.getDownloadStatus.parameters.valuesListOrNull
        )

        val privacyVideoResponse = SharePrivacyVideoResponseModule(configs.privacyVideoResponse, classLoader)

        class SharePrivacyVideoResponseModule internal constructor(
            private val configs: Configs.SharePrivacyVideoResponse,
            private val classLoader: ClassLoader
        ) {
            val selfClass by weak {
                configs.class_.nameOrNull?.toClass(classLoader)
            }

            fun msg() = Field(configs.msg.nameOrNull)

            fun status() = Field(configs.status.nameOrNull)
        }
    }

    class RxObservableModule internal constructor(private val configs: Configs.RxObservable, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun just() = Method(
            configs.just.nameOrNull,
            configs.just.parameters.valuesListOrNull
        )
    }

    class ListenAwemeFilterModule internal constructor(
        private val configs: Configs.ListenAwemeFilter,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun accept() = Method(
            configs.accept.nameOrNull,
            configs.accept.parameters.valuesListOrNull
        )
    }

    class BaseListFragmentPanelModule internal constructor(
        private val configs: Configs.BaseListFragmentPanel,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun handleDoubleClick() = Method(
            configs.handleDoubleClick.nameOrNull,
            configs.handleDoubleClick.parameters.valuesListOrNull
        )

        fun handleVideoEvent() = Method(
            configs.handleVideoEvent.nameOrNull,
            configs.handleVideoEvent.parameters.valuesListOrNull
        )

        fun getCurrentAweme() = Method(
            configs.getCurrentAweme.nameOrNull,
            configs.getCurrentAweme.parameters.valuesListOrNull
        )

        fun canPlayOnVideoReady() = Method(
            configs.canPlayOnVideoReady.nameOrNull,
            configs.canPlayOnVideoReady.parameters.valuesListOrNull
        )

        fun mPlayStateHelper() = Field(configs.mPlayStateHelper.nameOrNull)

        fun onVideoPlayerEvent() = Method(
            configs.onVideoPlayerEvent.nameOrNull,
            configs.onVideoPlayerEvent.parameters.valuesListOrNull
        )

        fun handleBigDiggViewClick() = Method(
            configs.handleBigDiggViewClick.nameOrNull,
            configs.handleBigDiggViewClick.parameters.valuesListOrNull
        )

        fun handlePause() = Method(
            configs.handlePause.nameOrNull,
            configs.handlePause.parameters.valuesListOrNull
        )
    }

    class VideoPlayerStatusModule internal constructor(
        private val configs: Configs.VideoPlayerStatus,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun code() = Field(configs.code.nameOrNull)

        companion object {
            const val EVENT_PLAY_COMPLETED = 7
        }
    }

    class VideoEventModule internal constructor(private val configs: Configs.VideoEvent, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun type() = Field(configs.type.nameOrNull)

        companion object {
            const val EVENT_OPEN_COMMENT_PANEL = 7
        }
    }

    class FluxComponentIdModule internal constructor(private val configs: Configs.FluxComponentId, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun musicCoverBlock() = Field(configs.musicCoverBlock.nameOrNull)

        fun musicListenCover() = Field(configs.musicListenCover.nameOrNull)

        fun digg() = Field(configs.digg.nameOrNull)

        fun title() = Field(configs.title.nameOrNull)

        fun musicCover() = Field(configs.musicCover.nameOrNull)

        fun generalLabel() = Field(configs.generalLabel.nameOrNull)

        fun feedLabelContainer() = Field(configs.feedLabelContainer.nameOrNull)

        fun musicTitle() = Field(configs.musicTitle.nameOrNull)

        fun story25DiverseDigg() = Field(configs.story25DiverseDigg.nameOrNull)

        fun ecomStore() = Field(configs.ecomStore.nameOrNull)

        fun buttonImQuickShare() = Field(configs.buttonImQuickShare.nameOrNull)

        fun buttonFeedImShareGuideV2() = Field(configs.buttonFeedImShareGuideV2.nameOrNull)

        fun buttonForceFeedImShareGuide() = Field(configs.buttonForceFeedImShareGuide.nameOrNull)

        fun socialNewCommentGuideBubble() = Field(configs.socialNewCommentGuideBubble.nameOrNull)

        fun commentBottomAnimation() = Field(configs.commentBottomAnimation.nameOrNull)

        fun nearbyIdentityTag() = Field(configs.nearbyIdentityTag.nameOrNull)

        fun livePhotoTag() = Field(configs.livePhotoTag.nameOrNull)

        fun photosTag() = Field(configs.photosTag.nameOrNull)

        fun story24Tag() = Field(configs.story24Tag.nameOrNull)

        fun socialNewStyleStoryTag() = Field(configs.socialNewStyleStoryTag.nameOrNull)

        fun longVideoHighlightTag() = Field(configs.longVideoHighlightTag.nameOrNull)

        fun danmakuVertical() = Field(configs.danmakuVertical.nameOrNull)

        fun avatar() = Field(configs.avatar.nameOrNull)

        fun nickname() = Field(configs.nickname.nameOrNull)

        fun postTime() = Field(configs.postTime.nameOrNull)

        fun bellowDescTime() = Field(configs.bellowDescTime.nameOrNull)

        fun comment() = Field(configs.comment.nameOrNull)

        fun reply() = Field(configs.reply.nameOrNull)

        fun share() = Field(configs.share.nameOrNull)

        fun collect() = Field(configs.collect.nameOrNull)

        fun anchorFramework() = Field(configs.anchorFramework.nameOrNull)

        fun bottomBarCommon() = Field(configs.bottomBarCommon.nameOrNull)

        fun commonButton() = Field(configs.commonButton.nameOrNull)

        fun sticker() = Field(configs.sticker.nameOrNull)

        fun aiSearch() = Field(configs.aiSearch.nameOrNull)

        fun c2Feed() = Field(configs.c2Feed.nameOrNull)

        fun flow() = Field(configs.flow.nameOrNull)

        fun nearbyHotComment() = Field(configs.nearbyHotComment.nameOrNull)

        fun buttonUnfollowFamiliar() = Field(configs.buttonUnfollowFamiliar.nameOrNull)

        fun buttonUnfollowFamiliarRec() = Field(configs.buttonUnfollowFamiliarRec.nameOrNull)

        fun coCreatorAuthor() = Field(configs.coCreatorAuthor.nameOrNull)

        fun chapterTag() = Field(configs.chapterTag.nameOrNull)

        fun ecomTagFriend() = Field(configs.ecomTagFriend.nameOrNull)

        fun socialNewStylePostTimeBottom() = Field(configs.socialNewStylePostTimeBottom.nameOrNull)

        fun socialNewStyleMusicBelow() = Field(configs.socialNewStyleMusicBelow.nameOrNull)

        fun chapterDetail() = Field(configs.chapterDetail.nameOrNull)

        fun titleTagContainer() = Field(configs.titleTagContainer.nameOrNull)

        fun rightMenuLl() = Field(configs.rightMenuLl.nameOrNull)

        fun musicMuteCover() = Field(configs.musicMuteCover.nameOrNull)

        fun jxLeftBottomLongVideoPlusTitleTag() = Field(configs.jxLeftBottomLongVideoPlusTitleTag.nameOrNull)

        fun bottomBarMix() = Field(configs.bottomBarMix.nameOrNull)

        fun bottomBarNormalSearch() = Field(configs.bottomBarNormalSearch.nameOrNull)

        fun bottomBarCommonPrioritySearch() = Field(configs.bottomBarCommonPrioritySearch.nameOrNull)

        fun jxPick() = Field(configs.jxPick.nameOrNull)

        fun bottomBarContainer() = Field(configs.bottomBarContainer.nameOrNull)

        fun aiCoCreatorsThree() = Field(configs.aiCoCreatorsThree.nameOrNull)

        fun aigcCocreateStatusTitle() = Field(configs.aigcCocreateStatusTitle.nameOrNull)
    }

    class FluxComponentDataActionModule internal constructor(
        private val configs: Configs.FluxComponentDataAction,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getSet() = Method(
            configs.getSet.nameOrNull,
            configs.getSet.parameters.valuesListOrNull
        )
    }

    class AwemeStatisticsModule internal constructor(private val configs: Configs.AwemeStatistics, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun collectCount() = Field(configs.collectCount.nameOrNull)

        fun commentCount() = Field(configs.commentCount.nameOrNull)

        fun diggCount() = Field(configs.diggCount.nameOrNull)

        fun shareCount() = Field(configs.shareCount.nameOrNull)
    }

    class DownLoadExecutorModule internal constructor(private val configs: Configs.DownLoadExecutor, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun execute() = Method(
            configs.execute.nameOrNull,
            configs.execute.parameters.valuesListOrNull
        )
    }

    class AbsTaskModule internal constructor(private val configs: Configs.AbsTask, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getTargetFilePaths() = Method(
            configs.getTargetFilePaths.nameOrNull,
            configs.getTargetFilePaths.parameters.valuesListOrNull
        )
    }

    class DownloadLivePhotoExecutorModule internal constructor(
        private val configs: Configs.DownloadLivePhotoExecutor,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun encodeLivePhoto() = Method(
            configs.encodeLivePhoto.nameOrNull,
            configs.encodeLivePhoto.parameters.valuesListOrNull
        )
    }

    class VideoModule internal constructor(private val configs: Configs.Video, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getPlayAddr() = Method(
            configs.getPlayAddr.nameOrNull,
            configs.getPlayAddr.parameters.valuesListOrNull
        )

        fun hasSuffixWaterMark() = Field(configs.hasSuffixWaterMark.nameOrNull)

        fun hasWaterMark() = Field(configs.hasWaterMark.nameOrNull)

        fun downloadAddr() = Field(configs.downloadAddr.nameOrNull)
    }

    class ImageUrlStructModule internal constructor(private val configs: Configs.ImageUrlStruct, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun watermarkFreeDownloadUrlList() = Field(
            configs.watermarkFreeDownloadUrlList.nameOrNull
        )

        fun urlList() = Field(
            configs.urlList.nameOrNull
        )

        fun downloadUrlList() = Field(
            configs.downloadUrlList.nameOrNull
        )

        fun video() = Field(
            configs.video.nameOrNull
        )
    }

    class FeedResponseHandlerModule internal constructor(
        private val configs: Configs.FeedResponseHandler,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun processAwemeList() = Method(
            configs.processAwemeList.nameOrNull,
            configs.processAwemeList.parameters.valuesListOrNull
        )
    }

    class CommentLongPressWhiteListProviderModule internal constructor(
        private val configs: Configs.CommentLongPressWhiteListProvider,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun buildWhiteList() = Method(
            configs.buildWhiteList.nameOrNull,
            configs.buildWhiteList.parameters.valuesListOrNull
        )
    }

    class MiscDownloadAddrUtilModule internal constructor(
        private val configs: Configs.MiscDownloadAddrUtil,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getSuffixSceneDownloadAddr() = Method(
            configs.getSuffixSceneDownloadAddr.nameOrNull,
            configs.getSuffixSceneDownloadAddr.parameters.valuesListOrNull
        )
    }

    class DownloadActionModule internal constructor(private val configs: Configs.DownloadAction, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun startDownload() = Method(
            configs.startDownload.nameOrNull,
            configs.startDownload.parameters.valuesListOrNull
        )

        fun aweme() = Field(configs.aweme.nameOrNull)
    }

    class ABTestServiceImplModule internal constructor(
        private val configs: Configs.ABTestServiceImpl,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun enableSaveImageToVideoLocalWaterMask() = Method(
            configs.enableSaveImageToVideoLocalWaterMask.nameOrNull,
            configs.enableSaveImageToVideoLocalWaterMask.parameters.valuesListOrNull
        )

        fun enableVEAddLiveVideoWaterMark() = Method(
            configs.enableVeAddLiveVideoWaterMark.nameOrNull,
            configs.enableVeAddLiveVideoWaterMark.parameters.valuesListOrNull
        )
    }

    class AbsPermissionCheckerModule internal constructor(
        private val configs: Configs.AbsPermissionChecker,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getActionCheckResult() = Method(
            configs.getActionCheckResult.nameOrNull,
            configs.getActionCheckResult.parameters.valuesListOrNull
        )
    }

    class ActionCheckResultModule internal constructor(
        private val configs: Configs.ActionCheckResult,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun actionStatus() = Field(configs.actionStatus.nameOrNull)
    }

    class ActionStatusModule internal constructor(private val configs: Configs.ActionStatus, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun grayed() = Field(configs.grayed.nameOrNull)

        fun hidden() = Field(configs.hidden.nameOrNull)

        fun normal() = Field(configs.normal.nameOrNull)
    }

    class GalleryShareHelperModule internal constructor(
        private val configs: Configs.GalleryShareHelper,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun startDownload() = Method(
            configs.startDownload.nameOrNull,
            configs.startDownload.parameters.valuesListOrNull
        )
    }

    class CleanModePresenterModule internal constructor(
        private val configs: Configs.CleanModePresenter,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun manageViewVisibility() = Method(
            configs.manageViewVisibility.nameOrNull,
            configs.manageViewVisibility.parameters.valuesListOrNull
        )
    }

    class TopCornerEntranceDataSourceModule internal constructor(
        private val configs: Configs.TopCornerEntranceDataSource,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun leftEntranceList() = Field(
            name = configs.leftEntranceList.nameOrNull
        )

        fun getLeftEntranceList() = Method(
            configs.getLeftEntranceList.nameOrNull,
            configs.getLeftEntranceList.parameters.valuesListOrNull
        )

        fun getRightEntranceList() = Method(
            configs.getRightEntranceList.nameOrNull,
            configs.getRightEntranceList.parameters.valuesListOrNull
        )
    }

    class TopCornerEntranceDataModule internal constructor(
        private val configs: Configs.TopCornerEntranceData,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun entranceId() = Field(configs.entranceId.nameOrNull)

        companion object {
            const val HOMEPAGE_RIGHT_SEARCH = "homepage_right_search"
            const val HOMEPAGE_LEFT_SCAN = "homepage_left_scan"
            const val HOMEPAGE_LEFT_SIDEBAR = "homepage_left_sidebar"
            const val HOMEPAGE_LEFT_SIDEBAR_JX = "homepage_left_sidebar_jx"
            const val HOMEPAGE_LEFT_LOGIN = "homepage_left_login"
            const val HOMEPAGE_LEFT_EXTENSION_COMPONENT = "homepage_left_extension_component"
        }
    }

    class CompilationSpeedInheritComponentModule internal constructor(
        private val configs: Configs.CompilationSpeedInheritComponent,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun needInheritSpeed() = Method(
            configs.needInheritSpeed.nameOrNull,
            configs.needInheritSpeed.parameters.valuesListOrNull
        )
    }

    class Story25LongPressPanelConfigModule internal constructor(
        private val configs: Configs.Story25LongPressPanelConfig,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getFunctionGroupList() = Method(
            configs.getFunctionGroupList.nameOrNull,
            configs.getFunctionGroupList.parameters.valuesListOrNull
        )
    }

    class LPPConfigModuleIdModule internal constructor(
        private val configs: Configs.LPPConfigModuleId,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getId() = Method(
            configs.getId.nameOrNull,
            configs.getId.parameters.valuesListOrNull
        )

        fun toStory() = Field(configs.toStory.nameOrNull)

        fun dislike() = Field(configs.dislike.nameOrNull)

        fun report() = Field(configs.report.nameOrNull)

        fun blockPost() = Field(configs.blockPost.nameOrNull)

        fun auth() = Field(configs.auth.nameOrNull)

        fun download() = Field(configs.download.nameOrNull)

        fun timingClose() = Field(configs.timingClose.nameOrNull)

        fun autoFeed() = Field(configs.autoFeed.nameOrNull)

        fun autoFeedPad() = Field(configs.autoFeedPad.nameOrNull)
    }

    class Story25SharePanelConfigModule internal constructor(
        private val configs: Configs.Story25SharePanelConfig,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getOtherFunctionWhiteList() = Method(
            configs.getOtherFunctionWhiteList.nameOrNull,
            configs.getOtherFunctionWhiteList.parameters.valuesListOrNull
        )
    }

    class SharePanelFuncConfigModule internal constructor(
        private val configs: Configs.SharePanelFuncConfig,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getFuncConfig() = Method(
            configs.getFuncConfig.nameOrNull,
            configs.getFuncConfig.parameters.valuesListOrNull
        )
    }

    class SharePanelLineModule internal constructor(private val configs: Configs.SharePanelLine, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun line() = Field(configs.line.nameOrNull)
    }

    class SharePanelBtnModule internal constructor(private val configs: Configs.SharePanelBtn, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun key() = Field(configs.key.nameOrNull)
    }

    class DanmakuViewModule internal constructor(private val configs: Configs.DanmakuView, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun onAttachedToWindow() = Method(
            configs.onAttachedToWindow.nameOrNull,
            configs.onAttachedToWindow.parameters.valuesListOrNull
        )
    }

    class HeifModule internal constructor(private val configs: Configs.Heif, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun toRgba() = Method(
            configs.toRgba.nameOrNull,
            configs.toRgba.parameters.valuesListOrNull
        )
    }

    class HeifDataModule internal constructor(private val configs: Configs.HeifData, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun newBitmap() = Method(
            configs.newBitmap.nameOrNull,
            configs.newBitmap.parameters.valuesListOrNull
        )
    }

    class CloseableReferenceModule internal constructor(
        private val configs: Configs.CloseableReference,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun get() = Method(
            configs.get.nameOrNull,
            configs.get.parameters.valuesListOrNull
        )
    }

    class StoryServiceImplModule internal constructor(private val configs: Configs.StoryServiceImpl, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun convertSingleLivePhotoToMp4UseMusicUrl() = Method(
            configs.convertSingleLivePhotoToMp4UseMusicUrl.nameOrNull,
            configs.convertSingleLivePhotoToMp4UseMusicUrl.parameters.valuesListOrNull
        )

        fun convertImgToMp4() = Method(
            configs.convertImgToMp4.nameOrNull,
            configs.convertImgToMp4.parameters.valuesListOrNull
        )
    }

    class TabNodeModule {
        companion object {
            const val TAB_ID_HOMEPAGE_HOME = "homepage_home"
            const val TAB_ID_HOMEPAGE_MALL = "homepage_mall"
            const val TAB_ID_HOMEPAGE_PUBLISH = "homepage_publish"
            const val TAB_ID_HOMEPAGE_NOTIFICATION = "homepage_notification"
            const val TAB_ID_HOMEPAGE_PROFILE = "homepage_profile"
            const val TAB_ID_HOMEPAGE_MEDIUMVIDEO = "homepage_mediumvideo"
            const val TAB_ID_HOMEPAGE_NEARBY = "homepage_nearby"
            const val TAB_ID_HOMEPAGE_HANGOUT = "homepage_hangout"
            const val TAB_ID_HOMEPAGE_PAD_HOT = "homepage_pad_hot"
            const val TAB_ID_HOMEPAGE_TABLIVE = "homepage_tablive"
            const val TAB_ID_HOMEPAGE_GROUPON = "homepage_groupon"
            const val TAB_ID_HOMEPAGE_FAMILIAR = "homepage_familiar"
            const val TAB_ID_HOMEPAGE_FOLLOW = "homepage_follow"
            const val TAB_ID_HOMEPAGE_HOT_CONTAINER = "homepage_hot_container"
        }
    }

    class HomeTabDataSourceServerModule internal constructor(
        private val configs: Configs.HomeTabDataSourceServer,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getShowingBottomTabIds() = Method(
            configs.getShowingBottomTabIds.nameOrNull,
            configs.getShowingBottomTabIds.parameters.valuesListOrNull
        )

        fun getShowingTopTabIds() = Method(
            configs.getShowingTopTabIds.nameOrNull,
            configs.getShowingTopTabIds.parameters.valuesListOrNull
        )
    }

    class HomeTabDataSourceDefaultModule internal constructor(
        private val configs: Configs.HomeTabDataSourceDefault,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun getShowingBottomTabIds() = Method(
            configs.getShowingBottomTabIds.nameOrNull,
            configs.getShowingBottomTabIds.parameters.valuesListOrNull
        )

        fun getShowingTopTabIds() = Method(
            configs.getShowingTopTabIds.nameOrNull,
            configs.getShowingTopTabIds.parameters.valuesListOrNull
        )
    }

    class FeedAvatarPresenterModule internal constructor(
        private val configs: Configs.FeedAvatarPresenter,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun updateFollowView() = Method(
            configs.updateFollowView.nameOrNull,
            configs.updateFollowView.parameters.valuesListOrNull
        )

        fun performFollow() = Method(
            configs.performFollow.nameOrNull,
            configs.performFollow.parameters.valuesListOrNull
        )
    }

    class PlayStatusHelperModule internal constructor(private val configs: Configs.PlayStatusHelper, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun playStatus() = Field(configs.playStatus.nameOrNull)

        companion object {
            // const val PLAY_STATUS_IDLE = 0
            // const val PLAY_STATUS_PREPARING = 1
            // const val PLAY_STATUS_PLAYING = 2
            const val PLAY_STATUS_PAUSED = 3
            // const val PLAY_STATUS_COMPLETED = 4
        }
    }

    class HeifImageModule internal constructor(private val configs: Configs.HeifImage, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun create() = Method(
            configs.create.nameOrNull,
            configs.create.parameters.valuesListOrNull
        )

        fun dispose() = Method(
            configs.dispose.nameOrNull,
            configs.dispose.parameters.valuesListOrNull
        )

        fun getWidth() = Method(
            configs.getWidth.nameOrNull,
            configs.getWidth.parameters.valuesListOrNull
        )

        fun getHeight() = Method(
            configs.getHeight.nameOrNull,
            configs.getHeight.parameters.valuesListOrNull
        )

        fun getFrame() = Method(
            configs.getFrame.nameOrNull,
            configs.getFrame.parameters.valuesListOrNull
        )

        fun getFrameCount() = Method(
            configs.getFrameCount.nameOrNull,
            configs.getFrameCount.parameters.valuesListOrNull
        )

        fun getFrameDurations() = Method(
            configs.getFrameDurations.nameOrNull,
            configs.getFrameDurations.parameters.valuesListOrNull
        )
    }

    class HeifFrameModule internal constructor(private val configs: Configs.HeifFrame, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun renderFrame() = Method(
            configs.renderFrame.nameOrNull,
            configs.renderFrame.parameters.valuesListOrNull
        )

        fun dispose() = Method(
            configs.dispose.nameOrNull,
            configs.dispose.parameters.valuesListOrNull
        )
    }

    class PinchViewModelModule internal constructor(private val configs: Configs.PinchViewModel, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun dismissWindow() = Method(
            configs.dismissWindow.nameOrNull,
            configs.dismissWindow.parameters.valuesListOrNull
        )

        fun setDismissWindowEvent() = Method(
            configs.setDismissWindowEvent.nameOrNull,
            configs.setDismissWindowEvent.parameters.valuesListOrNull
        )
    }

    class DismissWindowEventModule internal constructor(
        private val configs: Configs.DismissWindowEvent,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun dismissWithSurface() = Field(configs.dismissWithSurface.nameOrNull)

        fun enterMethod() = Field(configs.enterMethod.nameOrNull)

        companion object {
            const val ENTER_METHOD_SLIDE_UP = "slide_up"
            const val ENTER_METHOD_SLIDE_DOWN = "slide_down"
        }
    }

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        private val verbose
            get() = !ConfigManager.module.verboseDisabled.value

        @Volatile
        lateinit var instance: DouyinPackage
            private set

        fun init(classLoader: ClassLoader, context: Context) {
            instance = DouyinPackage(classLoader, context)
        }

        private fun readHookInfo(context: Context): Configs.HookInfo {
            runCatching {
                val hookInfoFile = File(context.cacheDir, HookInfoFiles.HOOK_INFO_FILE_NAME)
                if (!(hookInfoFile.isFile && hookInfoFile.canRead())) {
                    YLog.warn("$TAG: hookInfoFile is not a file or can not be read")
                    return@runCatching
                }

                val hostAppPackageInfo = context.packageManager.getPackageInfo(
                    context.packageName,
                    0
                )
                val hostAppVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    hostAppPackageInfo.longVersionCode.toInt()
                } else {
                    @Suppress("DEPRECATION")
                    hostAppPackageInfo.versionCode
                }

                val lastUpdateTime = maxOf(
                    hostAppPackageInfo.lastUpdateTime,
                    runCatching {
                        context.packageManager.getPackageInfo(
                            AppProperties.PROJECT_NAMESPACE,
                            0
                        ).lastUpdateTime
                    }.getOrDefault(hostAppPackageInfo.lastUpdateTime)
                )

                val hookInfoBuilder = FileInputStream(hookInfoFile).use {
                    runCatching {
                        Configs.HookInfo.newBuilder().mergeFrom(it)
                    }.getOrNull() ?: Configs.HookInfo.newBuilder()
                }
                if (hookInfoBuilder.generation != ConfigManager.module.hookInfoGeneration.value ||
                    hookInfoBuilder.lastUpdateTime < lastUpdateTime ||
                    hookInfoBuilder.hostVersionCode != hostAppVersionCode ||
                    hookInfoBuilder.moduleVersionCode != BuildConfig.VERSION_CODE ||
                    hookInfoBuilder.moduleVersionName != BuildConfig.VERSION_NAME
                ) {
                    YLog.info("$TAG: hookInfo is outdated, will re-generate")
                    return@runCatching
                }

                if (ConfigManager.module.hookInfoSource.value == ModuleConfigProvider.HOOK_INFO_SOURCE_UNTRUSTED_OVERLAY) {
                    YLog.warn(
                        "$TAG: Loading custom HookInfo from UNTRUSTED SOURCES may cause malfunctions." +
                            " You will LOSE the right to submit bug reports to maintainers until you clear custom HookInfo!"
                    )
                }

                return hookInfoBuilder.build()
            }.onFailure {
                YLog.error("$TAG: failed to read hookInfo: ", it)
            }

            // HookInfo rebuild is required at this stage
            val generatedHookInfo = initHookInfo(context)
            runCatching {
                if (ConfigManager.module.hookInfoSource.value == ModuleConfigProvider.HOOK_INFO_SOURCE_BUILTIN) {
                    // skip custom preset loading when not required (source BUILTIN) or not accepted;
                    // an untrusted preset may be rejected on import in the future, leaving its overlay unapplied.
                    YLog.info("$TAG: no custom preset to import, or import not accepted")
                    return@runCatching
                } else if (HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME !in YukiHook.Module.sharedFiles.list()) {
                    YLog.info("$TAG: no custom hook info present, skipping loading")
                    return@runCatching
                } else if (ConfigManager.module.hookInfoSource.value == ModuleConfigProvider.HOOK_INFO_SOURCE_UNTRUSTED_OVERLAY) {
                    YLog.warn(
                        "$TAG: Loading custom HookInfo from UNTRUSTED SOURCES may cause malfunctions." +
                            " You will LOSE the right to submit bug reports to maintainers until you clear custom HookInfo!"
                    )
                    // optional early return here for strict mode
                }

                val presetJsonFd = YukiHook.Module.sharedFiles.open(
                    HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME
                )
                if (presetJsonFd.statSize == 0L) {
                    YLog.warn("$TAG: custom hook info preset file is empty")
                    return@runCatching
                }

                val presetJsonText = YukiHook.Module.sharedFiles.readText(
                    HookInfoFiles.HOOK_INFO_PRESET_FILE_NAME,
                    Charsets.UTF_8
                )
                val hookInfoPresetJson = JSONObject(presetJsonText)
                val customHookInfoJson = hookInfoPresetJson.optJSONObject("hookInfo") ?: run {
                    YLog.warn("$TAG: custom hook info preset missing 'hookInfo' object")
                    return@runCatching
                }

                // NOTE: JSON parsing accepts both snake_case and camelCase names, but the checks
                // below only match the snake_case keys. If you author a preset using
                // camelCase, these fields fall back to defaults, and a possibly stale custom HookInfo
                // is loaded unconditionally, which may cause malfunction — that's on you.
                val customModuleVersionCode = (customHookInfoJson.opt("module_version_code") as? Int) ?: run {
                    YLog.warn("$TAG: custom hook info preset missing 'module_version_code'")
                    BuildConfig.VERSION_CODE
                }
                val customModuleVersionName = (customHookInfoJson.opt("module_version_name") as? String) ?: run {
                    YLog.warn("$TAG: custom hook info preset missing 'module_version_name'")
                    BuildConfig.VERSION_NAME
                }
                val customHostVersionCode = (customHookInfoJson.opt("host_version_code") as? Int) ?: run {
                    YLog.warn("$TAG: custom hook info preset missing 'host_version_code'")
                    generatedHookInfo.hostVersionCode
                }

                // before merging custom hook info into final hook info, verify version validity
                if (customModuleVersionCode != generatedHookInfo.moduleVersionCode ||
                    customModuleVersionName != generatedHookInfo.moduleVersionName ||
                    customHostVersionCode != generatedHookInfo.hostVersionCode
                ) {
                    YLog.warn("$TAG: custom hook info version mismatch, skipping loading")
                    return@runCatching
                }

                val declaredSignature = hookInfoPresetJson.optString("signature")
                YLog.info("$TAG: custom hook info preset signature: $declaredSignature")
                // "signature" not included for signature checking
                hookInfoPresetJson.remove("signature")
                val canonicalPresetBytes = JsonCanonicalizer(
                    hookInfoPresetJson.toString()
                ).encodedString.toByteArray(Charsets.UTF_8)

                val signatureVerified = declaredSignature.isNotBlank() && ByteArrayInputStream(canonicalPresetBytes).use { stream ->
                    verifySha256RsaSignature(
                        stream,
                        Base64.decode(declaredSignature),
                        Base64.decode(SignatureKeys.HOOK_INFO_PRESET_PUBLIC_KEY_B64)
                    )
                }
                if (signatureVerified) {
                    YLog.info("$TAG: custom hook info signature verified")
                } else {
                    if (declaredSignature.isBlank()) {
                        YLog.warn("$TAG: custom hook info has no signature")
                    } else {
                        YLog.warn("$TAG: custom hook info signature verification failed, expected: $declaredSignature")
                    }
                }

                // do not overlay lastUpdateTime and generation fields from auto-generated HookInfo
                val snapshotLastUpdateTime = generatedHookInfo.lastUpdateTime
                val snapshotGeneration = generatedHookInfo.generation

                YLog.warn("$TAG: merging custom hook info into hookInfo")
                generatedHookInfo.apply {
                    applyFrom(customHookInfoJson)
                    lastUpdateTime = snapshotLastUpdateTime
                    generation = snapshotGeneration
                }
            }.onFailure {
                YLog.error("$TAG: failed to merge custom hook info", it)
            }

            return generatedHookInfo.build().also {
                val hookInfoFile = File(context.cacheDir, HookInfoFiles.HOOK_INFO_FILE_NAME)
                if (hookInfoFile.exists()) {
                    hookInfoFile.delete()
                }
                FileOutputStream(hookInfoFile).use { o ->
                    it.writeTo(o)
                }
            }
        }

        private fun Configs.HookInfo.Builder.applyFrom(hookInfoJson: JSONObject) {
            val preset = Configs.HookInfo.newBuilder().apply {
                JsonFormat.parser().ignoringUnknownFields().merge(
                    hookInfoJson.toString(),
                    this
                )
            }.build()
            this.applyFrom(preset)
        }

        private fun Message.Builder.applyFrom(hookInfoMessage: Message) {
            hookInfoMessage.allFields.forEach { (field, value) ->
                when {
                    field.isRepeated -> this.setField(field, value)

                    field.type == Descriptors.FieldDescriptor.Type.MESSAGE ->
                        this.getFieldBuilder(field).applyFrom(value as Message)

                    else -> this.setField(field, value)
                }
            }
        }

        private fun initHookInfo(context: Context) = Configs.HookInfo.newBuilder().apply {
            val populateFailedMsg = "%s: unable to populate %s config"

            val hostAppClassLoader = context.classLoader
            val hostAppPackageInfo = context.packageManager.getPackageInfo(
                context.packageName,
                0
            )

            lastUpdateTime = maxOf(
                hostAppPackageInfo.lastUpdateTime,
                runCatching {
                    context.packageManager
                        .getPackageInfo(
                            AppProperties.PROJECT_NAMESPACE,
                            0
                        ).lastUpdateTime
                }.getOrDefault(hostAppPackageInfo.lastUpdateTime)
            )
            moduleVersionCode = BuildConfig.VERSION_CODE
            moduleVersionName = BuildConfig.VERSION_NAME
            hostVersionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                hostAppPackageInfo.longVersionCode.toInt()
            } else {
                @Suppress("DEPRECATION")
                hostAppPackageInfo.versionCode
            }
            generation = ConfigManager.module.hookInfoGeneration.value

            runCatching {
                System.loadLibrary("dexkit")
            }.onFailure {
                YLog.error("failed to load DexKit native library", it)
                return@apply
            }

            DexKitBridge.create(context.applicationInfo.sourceDir).use { bridge ->
                commentImageStruct = commentImageStruct {
                    runCatching {
                        val commentImageStructClassData = bridge.getClassData("com.ss.android.ugc.aweme.comment.model.CommentImageStruct")
                        val originUrlFieldData = commentImageStructClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("origin_url")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val downloadUrlFieldData = commentImageStructClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("download_url")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getDownloadUrlMethodData = commentImageStructClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    paramCount = 0
                                    returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                    addUsingField {
                                        downloadUrlFieldData?.descriptor?.let { dlUrlDescriptor ->
                                            descriptor = dlUrlDescriptor
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            commentImageStructClassData,
                            originUrlFieldData,
                            downloadUrlFieldData,
                            getDownloadUrlMethodData
                        ) { classData, originField, downloadField, getDownloadMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            originUrl = field {
                                name = originField.name
                            }
                            downloadUrl = field {
                                name = downloadField.name
                            }
                            getDownloadUrl = method {
                                name = getDownloadMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getDownloadMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentImageStruct.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                urlModel = urlModel {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.base.model.UrlModel"
                    }
                    urlList = field {
                        name = "urlList"
                    }
                }

                comment = comment {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.comment.model.Comment"
                    }
                    emoji = field {
                        name = "emoji"
                    }
                    imageList = field {
                        name = "imageList"
                    }
                    commentAudio = field {
                        name = "commentAudio"
                    }
                }

                commentAudioStruct = commentAudioStruct {
                    runCatching {
                        val commentAudioStructClassData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.comment.model.CommentAudioStruct"
                        )
                        val contentFieldData = commentAudioStructClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("content")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            commentAudioStructClassData,
                            contentFieldData
                        ) { classData, contentField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            content = field {
                                name = contentField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentAudioStruct.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                emoji = emoji {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.emoji.model.Emoji"
                    }
                    animateUrl = method {
                        name = "animateUrl"
                    }
                }

                commentActionParams = commentActionParams {
                    runCatching {
                        val saveImageMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                usingStrings {
                                    add("bpea-comment_save_image_to_album")
                                    add("/comment/images")
                                    add("comment_save_image")
                                }
                                usingFields {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/download/component_api/DownloadScene;->IMAGE:Lcom/ss/android/ugc/aweme/download/component_api/DownloadScene;"
                                    }
                                }
                            }
                        }.singleOrNull()
                        val commentActionParamsClassData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                fields {
                                    add {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        type = "androidx.fragment.app.FragmentActivity"
                                    }
                                    add {
                                        type = "com.ss.android.ugc.aweme.comment.model.Comment"
                                        saveImageMethodData?.let {
                                            readMethods {
                                                add {
                                                    descriptor = it.descriptor
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()
                        val commentFieldData = commentActionParamsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.comment.model.Comment"
                                    saveImageMethodData?.let { methodData ->
                                        readMethods {
                                            add {
                                                descriptor = methodData.descriptor
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            commentActionParamsClassData,
                            commentFieldData
                        ) { classData, commentField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            comment = field {
                                name = commentField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentActionParams.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                commentLongPressItemModel = commentLongPressItemModel {
                    runCatching {
                        val commentLongPressItemModelClassData =
                            bridge.getClassData("com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel")
                        val commentActionParamsFieldData = commentLongPressItemModelClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    this@apply.commentActionParams.class_.nameOrNull?.let { commentActionParamsClassTypeName ->
                                        type = commentActionParamsClassTypeName
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            commentLongPressItemModelClassData,
                            commentActionParamsFieldData
                        ) { classData, commentActionParamsField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            commentActionParams = field {
                                name = commentActionParamsField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentLongPressItemModel.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                saveImageActionItem = saveImageActionItem {
                    runCatching {
                        val saveImageActionItemClassData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem"
                        )
                        val isVisibleMethodData = saveImageActionItemClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "boolean"
                                    usingFields {
                                        add {
                                            field {
                                                this@apply.commentLongPressItemModel.commentActionParams.nameOrNull
                                                    ?.let { commentActionParamsFieldName ->
                                                        name = commentActionParamsFieldName
                                                    }
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val onClickMethodData = saveImageActionItemClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "void"
                                    params {
                                        add("int")
                                    }
                                    usingStrings {
                                        add("bpea-comment_save_image_to_album")
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            saveImageActionItemClassData,
                            isVisibleMethodData,
                            onClickMethodData
                        ) { classData, isVisibleMethod, onClickMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            isVisible = method {
                                name = isVisibleMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(isVisibleMethod.paramTypeNames)
                                }
                            }
                            onClick = method {
                                name = onClickMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(onClickMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.SaveImageActionItem.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                listenerProviderParam = listenerProviderParam {
                    runCatching {
                        val clsData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                fields {
                                    add {
                                        type {
                                            descriptor = "Landroid/content/Context;"
                                        }
                                    }
                                    add {
                                        type {
                                            descriptor = "Lcom/bytedance/bpea/cert/token/TokenCert;"
                                        }
                                    }
                                }
                                method {
                                    name = "toString"
                                    usingStrings {
                                        add("ListenerProviderParam(context=")
                                    }
                                }
                            }
                        }.singleOrNull()

                        val clsName = clsData?.name

                        val contextFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "android.content.Context"
                            }?.self?.name

                        val certFieldName = clsName
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                type = "com.bytedance.bpea.cert.token.TokenCert"
                            }?.self?.name

                        requireAllNotNullThen(
                            clsName,
                            contextFieldName,
                            certFieldName
                        ) { paramClassName, contextFieldNameVal, certFieldNameVal ->
                            class_ = class_ {
                                name = paramClassName
                            }
                            this.context = field {
                                name = contextFieldNameVal
                            }
                            cert = field {
                                name = certFieldNameVal
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.ListenerProviderParam.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                commentImageSaveDownloadListener = commentImageSaveDownloadListener {
                    runCatching {
                        val onSuccessedMethodData = bridge
                            .findMethod {
                                matcher {
                                    name = "onSuccessed"
                                    modifiers = Modifier.FINAL or Modifier.PUBLIC
                                    returnType = "void"
                                    params {
                                        add("com.ss.android.socialbase.downloader.model.DownloadInfo")
                                    }
                                    usingStrings {
                                        add("/douyin/comment")
                                        add("comment_")
                                    }
                                }
                            }.singleOrNull()

                        val clsName = onSuccessedMethodData?.declaredClassName

                        val notifyResultMethod = clsName?.toClass(hostAppClassLoader)?.resolve()
                            ?.firstMethodOrNull {
                                modifiers(Modifiers.PUBLIC, Modifiers.FINAL)
                                parameters(Context::class, Boolean::class)
                                parameterCount = 2
                                superclass()
                            }?.self
                        val listenerProviderParamFieldName = clsName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                            type = this@apply.listenerProviderParam.class_.nameOrNull
                        }?.self?.name

                        requireAllNotNullThen(
                            clsName,
                            onSuccessedMethodData,
                            notifyResultMethod,
                            listenerProviderParamFieldName
                        ) { listenerClassName, onSuccessedMethod, notifyResultMethodVal, listenerProviderParamField ->
                            class_ = class_ {
                                name = listenerClassName
                            }
                            onSuccessed = method {
                                name = onSuccessedMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(onSuccessedMethod.paramTypeNames)
                                }
                            }
                            notifyResult = method {
                                name = notifyResultMethodVal.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    notifyResultMethodVal.parameterTypes.forEach { paramType ->
                                        values.add(paramType.name)
                                    }
                                }
                            }
                            listenerProviderParam = field {
                                name = listenerProviderParamField
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentImageSaveDownloadListener.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                downloadInfo = downloadInfo {
                    class_ = class_ {
                        name = "com.ss.android.socialbase.downloader.model.DownloadInfo"
                    }
                    url = field {
                        name = "url"
                    }
                    getTargetFilePath = method {
                        name = "getTargetFilePath"
                    }
                }

                digestUtils = digestUtils {
                    runCatching {
                        val digestUtilsClsName = "com.bytedance.common.utility.DigestUtils"
                        val md5HexFieldMethod = digestUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "md5Hex"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC)
                            parameters(String::class)
                        }?.self

                        requireAllNotNullThen(
                            digestUtilsClsName,
                            md5HexFieldMethod
                        ) { className, md5HexFieldMethod ->
                            class_ = class_ {
                                name = className
                            }
                            md5Hex = method {
                                name = md5HexFieldMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    md5HexFieldMethod.parameterTypes.forEach { paramType ->
                                        values.add(paramType.name)
                                    }
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DigestUtils.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                ugFileUtils = uGFileUtilsKt {
                    runCatching {
                        val ugFileUtilsClassData = bridge.getClassData("com.bytedance.android.ug.UGFileUtilsKt")
                        val contextFieldData = ugFileUtilsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                    type = "android.content.Context"
                                }
                            }.singleOrNull()
                        }
                        val copyFileMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                    returnType = "boolean"
                                    params {
                                        add("java.lang.String")
                                        add("java.lang.String")
                                        add("boolean")
                                        add("android.net.Uri[]")
                                        add("com.bytedance.bpea.cert.token.TokenCert")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getStorageDirMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL or Modifier.STATIC
                                    returnType = "java.lang.String"
                                    params {
                                        add("java.lang.String")
                                        add("boolean")
                                    }
                                    callerMethods {
                                        add {
                                            descriptor =
                                                "Lcom/ss/android/ugc/aweme/share/dialog/BaseQRCodeShareDialog;->saveImageToFile(Ljava/lang/String;Landroid/graphics/Bitmap;)Ljava/lang/String;"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getAudioUriMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL or Modifier.STATIC
                                    returnType = "android.net.Uri"
                                    params {
                                        add("android.content.Context")
                                        add("java.lang.String")
                                        add("java.lang.String")
                                        add("java.lang.String")
                                        add("com.bytedance.bpea.cert.token.TokenCert")
                                    }
                                    usingFields {
                                        add {
                                            descriptor =
                                                $$"Landroid/provider/MediaStore$Audio$Media;->EXTERNAL_CONTENT_URI:Landroid/net/Uri;"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getExternalStorageDirectoryMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL or Modifier.STATIC
                                    returnType = "java.lang.String"
                                    params {
                                        add("java.lang.String")
                                        add("boolean")
                                        add("boolean")
                                    }
                                    usingStrings {
                                        add {
                                            value = "DCIM"
                                            matchType = StringMatchType.Equals
                                        }
                                        add {
                                            value = "Pictures"
                                            matchType = StringMatchType.Equals
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val createUriMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL or Modifier.STATIC
                                    returnType = "android.net.Uri"
                                    params {
                                        add("java.lang.String")
                                        add("boolean")
                                        add("android.net.Uri[]")
                                        add("com.bytedance.bpea.cert.token.TokenCert")
                                    }
                                    usingStrings {
                                        add {
                                            value = "image/jpeg"
                                            matchType = StringMatchType.Equals
                                        }
                                        add {
                                            value = "image/png"
                                            matchType = StringMatchType.Equals
                                        }
                                        add {
                                            value = "audio/mp3"
                                            matchType = StringMatchType.Equals
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getImageUriMethodData = ugFileUtilsClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL or Modifier.STATIC
                                    returnType = "android.net.Uri"
                                    params {
                                        add("android.content.Context")
                                        add("java.lang.String")
                                        add("java.lang.String")
                                        add("java.lang.String")
                                        add("com.bytedance.bpea.cert.token.TokenCert")
                                    }
                                    usingFields {
                                        add {
                                            descriptor =
                                                $$"Landroid/provider/MediaStore$Images$Media;->EXTERNAL_CONTENT_URI:Landroid/net/Uri;"
                                        }
                                    }
                                    invokeMethods {
                                        add {
                                            descriptor =
                                                $$"Lcom/bytedance/bpea/entry/api/content/provider/ContentResolverEntry$Companion;->insert(Landroid/content/ContentResolver;Landroid/net/Uri;Landroid/content/ContentValues;Lcom/bytedance/bpea/basics/Cert;)Landroid/net/Uri;"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            ugFileUtilsClassData,
                            contextFieldData,
                            copyFileMethodData,
                            getStorageDirMethodData,
                            getExternalStorageDirectoryMethodData,
                            getImageUriMethodData,
                            createUriMethodData,
                            getAudioUriMethodData
                        ) {
                                classData,
                                contextField,
                                copyFileMethod,
                                getStorageDirMethod,
                                getExternalStorageDirectoryMethod,
                                getImageUriMethod,
                                createUriMethod,
                                getAudioUriMethod
                            ->
                            class_ = class_ {
                                name = classData.name
                            }
                            this.context = field {
                                name = contextField.name
                            }
                            copyFile = method {
                                name = copyFileMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(copyFileMethod.paramTypeNames)
                                }
                            }
                            getStorageDir = method {
                                name = getStorageDirMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getStorageDirMethod.paramTypeNames)
                                }
                            }
                            getExternalStorageDir = method {
                                name = getExternalStorageDirectoryMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getExternalStorageDirectoryMethod.paramTypeNames)
                                }
                            }
                            getImageUri = method {
                                name = getImageUriMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getImageUriMethod.paramTypeNames)
                                }
                            }
                            createUri = method {
                                name = createUriMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(createUriMethod.paramTypeNames)
                                }
                            }
                            getAudioUri = method {
                                name = getAudioUriMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getAudioUriMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.UGFileUtilsKt.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                commonItemView = commonItemView {
                    class_ = class_ {
                        name = "com.bytedance.ies.dmt.ui.common.views.CommonItemView"
                    }
                    setLeftText = method {
                        name = "setLeftText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                    setRightUiMode = method {
                        name = "setRightUIMode"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setLeftIcon = method {
                        name = "setLeftIcon"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("int")
                        }
                    }
                    setRightText = method {
                        name = "setRightText"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("java.lang.CharSequence")
                        }
                    }
                }

                douYinSettingNewVersionActivity = douYinSettingNewVersionActivity {
                    runCatching {
                        val dySettingsNewVersionActivityClsName = "com.ss.android.ugc.aweme.setting.ui.DouYinSettingNewVersionActivity"
                        val settingsScrollViewFieldName = dySettingsNewVersionActivityClsName.toClass(
                            hostAppClassLoader
                        ).resolve().firstFieldOrNull {
                            type = "com.ss.android.ugc.aweme.setting.ui.SettingNestedScrollView"
                        }?.self?.name

                        requireAllNotNullThen(
                            dySettingsNewVersionActivityClsName,
                            settingsScrollViewFieldName
                        ) { className, settingsScrollViewFieldNameVal ->
                            class_ = class_ {
                                name = className
                            }
                            settingsScrollView = field {
                                name = settingsScrollViewFieldNameVal
                            }
                            onResume = method {
                                name = "onResume"
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DouYinSettingNewVersionActivity.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                user = user {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.profile.model.User"
                    }
                    nickname = field {
                        name = "nickname"
                    }
                    uid = field {
                        name = "uid"
                    }
                }

                aweme = aweme {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.Aweme"
                    }
                    desc = field {
                        name = "desc"
                    }
                    author = field {
                        name = "author"
                    }
                    getAd = method {
                        name = "getAd"
                    }
                    itemTitle = field {
                        name = "itemTitle"
                    }
                    duration = field {
                        name = "duration"
                    }
                    isNormalVideo = method {
                        name = "isNormalVideo"
                    }
                    isEcomAweme = method {
                        name = "isEcomAweme"
                    }
                    grouponLargeCard = field {
                        name = "grouponLargeCard"
                    }
                    isLive = method {
                        name = "isLive"
                    }
                    isMultiImage = method {
                        name = "isMultiImage"
                    }
                    getVideo = method {
                        name = "getVideo"
                    }
                    images = field {
                        name = "images"
                    }
                    statistics = field {
                        name = "statistics"
                    }
                    getAid = method {
                        name = "getAid"
                    }
                    getDownloadStatus = method {
                        name = "getDownloadStatus"
                    }
                    aid = field {
                        name = "aid"
                    }
                    status = field {
                        name = "status"
                    }
                    getFollowStatus = method {
                        name = "getFollowStatus"
                    }
                }

                awemeStatistics = awemeStatistics {
                    runCatching {
                        val awemeStatisticsClassData = bridge.getClassData("com.ss.android.ugc.aweme.feed.model.AwemeStatistics")
                        val collectCountFieldData = awemeStatisticsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "collect_count",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val commentCountFieldData = awemeStatisticsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "comment_count",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val diggCountFieldData = awemeStatisticsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "digg_count",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val shareCountFieldData = awemeStatisticsClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "share_count",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            awemeStatisticsClassData,
                            collectCountFieldData,
                            commentCountFieldData,
                            diggCountFieldData,
                            shareCountFieldData
                        ) { classData, collectCountField, commentCountField, diggCountField, shareCountField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            collectCount = field {
                                name = collectCountField.name
                            }
                            commentCount = field {
                                name = commentCountField.name
                            }
                            diggCount = field {
                                name = diggCountField.name
                            }
                            shareCount = field {
                                name = shareCountField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.AwemeStatistics.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                downLoadExecutor = downLoadExecutor {
                    runCatching {
                        val executeMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "boolean"
                                paramCount = 1
                                usingStrings {
                                    add("/douyin")
                                    add("share_")
                                    add(".png")
                                    add("DownLoadExecutor")
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            executeMethodData
                        ) { executeMethod ->
                            class_ = class_ {
                                name = executeMethod.className
                            }
                            execute = method {
                                name = executeMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(executeMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DownLoadExecutor.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                absTask = absTask {
                    runCatching {
                        val absTaskClassData = this@apply.downLoadExecutor.execute.parameters.valuesListOrNull?.firstOrNull()?.let {
                            bridge.getClassData(it)
                        }
                        val getTargetFilePathsMethodData = absTaskClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    returnType = "java.util.List"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            absTaskClassData,
                            getTargetFilePathsMethodData
                        ) { classData, getTargetFilePathsMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getTargetFilePaths = method {
                                name = getTargetFilePathsMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getTargetFilePathsMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.AbsTask.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                downloadLivePhotoExecutor = downloadLivePhotoExecutor {
                    runCatching {
                        val encodeLivePhotoMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "boolean"
                                usingStrings {
                                    add("DownloadLiveExecutor")
                                    add("encode live photo isFinish: ")
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            encodeLivePhotoMethodData
                        ) { encodeLivePhotoMethod ->
                            class_ = class_ {
                                name = encodeLivePhotoMethod.className
                            }
                            encodeLivePhoto = method {
                                name = encodeLivePhotoMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(encodeLivePhotoMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DownloadLivePhotoExecutor.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                video = video {
                    runCatching {
                        val videoClassData = bridge.getClassData("com.ss.android.ugc.aweme.feed.model.Video")
                        val hasSuffixWaterMarkFieldData = videoClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "boolean"
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "has_download_suffix_logo_addr",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val hasWaterMarkFieldData = videoClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "boolean"
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "has_watermark",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val downloadAddrFieldData = videoClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue(
                                                    value = "download_addr",
                                                    matchType = StringMatchType.Equals
                                                )
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getPlayAddrMethodData = videoClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    returnType = "com.ss.android.ugc.aweme.feed.model.VideoUrlModel"
                                    usingFields {
                                        add {
                                            declaredClass = it.name
                                            type = "com.ss.android.ugc.aweme.feed.model.VideoUrlModel"
                                            annotations {
                                                add {
                                                    type = "com.google.gson.annotations.SerializedName"
                                                    addElement {
                                                        name = "value"
                                                        stringValue("play_addr")
                                                    }
                                                }
                                            }
                                        }
                                        add {
                                            declaredClass = it.name
                                            type = "com.ss.android.ugc.aweme.feed.model.VideoUrlModel"
                                            annotations {
                                                add {
                                                    type = "com.google.gson.annotations.SerializedName"
                                                    addElement {
                                                        name = "value"
                                                        stringValue("play_addr_265")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull { methodData ->
                                methodData.usingFields.flatMap { fieldUsage ->
                                    fieldUsage.field.annotations
                                }.filter { annotation ->
                                    annotation.typeName == "com.google.gson.annotations.SerializedName"
                                }.flatMap { annotation ->
                                    annotation.elements
                                }.none { element ->
                                    element.value.stringValue() == "ratio"
                                }
                            }
                        }

                        requireAllNotNullThen(
                            videoClassData,
                            hasSuffixWaterMarkFieldData,
                            hasWaterMarkFieldData,
                            downloadAddrFieldData,
                            getPlayAddrMethodData
                        ) { classData, hasSuffixWaterMarkField, hasWaterMarkField, downloadAddrField, getPlayAddrMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getPlayAddr = method {
                                name = getPlayAddrMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getPlayAddrMethod.paramTypeNames)
                                }
                            }
                            hasSuffixWaterMark = field {
                                name = hasSuffixWaterMarkField.name
                            }
                            hasWaterMark = field {
                                name = hasWaterMarkField.name
                            }
                            downloadAddr = field {
                                name = downloadAddrField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.Video.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                feedResponseHandler = feedResponseHandler {
                    runCatching {
                        val processAwemeListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC + Modifier.STATIC
                                returnType = "void"
                                params {
                                    add("int")
                                    add("java.lang.String")
                                    add("java.util.List")
                                }
                                invokeMethods {
                                    add {
                                        descriptor = "Ljava/util/List;->size()I"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->setRequestId(Ljava/lang/String;)V"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAd()Z"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/awemeservice/api/IAwemeService;->updateAweme(Lcom/ss/android/ugc/aweme/feed/model/Aweme;I)Lcom/ss/android/ugc/aweme/feed/model/Aweme;"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->isLive()Z"
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            processAwemeListMethodData
                        ) { processAwemeListMethod ->
                            class_ = class_ {
                                name = processAwemeListMethod.className
                            }
                            processAwemeList = method {
                                name = processAwemeListMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(processAwemeListMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.FeedResponseHandler.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                commentLongPressWhiteListProvider = commentLongPressWhiteListProvider {
                    runCatching {
                        val buildWhiteListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC
                                returnType = "java.util.Set"
                                params {
                                    this@apply.commentActionParams.class_.nameOrNull?.let {
                                        add(it)
                                    }
                                }
                                usingStrings {
                                    add("custom")
                                    add("default")
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            buildWhiteListMethodData
                        ) { buildWhiteListMethod ->
                            class_ = class_ {
                                name = buildWhiteListMethod.className
                            }
                            buildWhiteList = method {
                                name = buildWhiteListMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(buildWhiteListMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CommentLongPressWhiteListProvider.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                miscDownloadAddrUtil = miscDownloadAddrUtil {
                    runCatching {
                        val getSuffixSceneDownloadAddrMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                returnType {
                                    descriptor = "Lcom/ss/android/ugc/aweme/feed/model/VideoUrlModel;"
                                }
                                params {
                                    add("com.ss.android.ugc.aweme.feed.model.Aweme")
                                }
                                opCodes(
                                    OpCodesMatcher().opNames(
                                        listOf("const-class")
                                    )
                                )
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getVideo()Lcom/ss/android/ugc/aweme/feed/model/Video;"
                                    }
                                    add {
                                        descriptor = "Lcom/google/gson/Gson;-><init>()V"
                                    }
                                }

                                bridge.findMethod {
                                    matcher {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        params {
                                            add("boolean")
                                        }
                                        invokeMethods {
                                            add {
                                                descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getDownloadStatus()I"
                                            }
                                        }
                                        usingStrings {
                                            add("download_time")
                                            add("is_ug_can_re_download")
                                            add("download_start")
                                        }
                                    }
                                }.singleOrNull()?.let {
                                    callerMethods {
                                        method {
                                            add {
                                                descriptor = it.descriptor
                                            }
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            getSuffixSceneDownloadAddrMethodData
                        ) { getSuffixSceneDownloadAddrMethod ->
                            class_ = class_ {
                                name = getSuffixSceneDownloadAddrMethod.className
                            }
                            getSuffixSceneDownloadAddr = method {
                                name = getSuffixSceneDownloadAddrMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getSuffixSceneDownloadAddrMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.MiscDownloadAddrUtil.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                downloadAction = downloadAction {
                    runCatching {
                        val startDownloadMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "void"
                                params {
                                    add("com.ss.android.ugc.aweme.sharer.ui.SharePackage")
                                }
                                usingStrings {
                                    add("downloadImage")
                                }
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getVideoMuteStatus()Lcom/ss/android/ugc/aweme/feed/model/VideoMuteStruct;"
                                    }
                                }
                            }
                        }.singleOrNull()
                        val downloadActionClassData = startDownloadMethodData?.declaredClass
                        val awemeFieldData = downloadActionClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.feed.model.Aweme"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            startDownloadMethodData,
                            downloadActionClassData,
                            awemeFieldData
                        ) { startDownloadMethod, classData, awemeField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            startDownload = method {
                                name = startDownloadMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(startDownloadMethod.paramTypeNames)
                                }
                            }
                            aweme = field {
                                name = awemeField.fieldName
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DownloadAction.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                imageUrlStruct = imageUrlStruct {
                    class_ = class_ {
                        name = "com.ss.ugc.aweme.ImageUrlStruct"
                    }
                    watermarkFreeDownloadUrlList = field {
                        name = "watermarkFreeDownloadUrlList"
                    }
                    urlList = field {
                        name = "urlList"
                    }
                    downloadUrlList = field {
                        name = "downloadUrlList"
                    }
                    video = field {
                        name = "video"
                    }
                }

                abTestServiceImpl = aBTestServiceImpl {
                    runCatching {
                        val abTestServiceImplClassData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                interfaces {
                                    add {
                                        className = "com.ss.android.ugc.aweme.services.external.IABTestService"
                                    }
                                }
                            }
                        }.singleOrNull {
                            it.simpleName != "StubAllServices"
                        }
                        val enableSaveImageToVideoLocalWaterMaskMethodData = abTestServiceImplClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "boolean"
                                    usingStrings {
                                        add("save_image_to_video_local_water_mask_enable")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val enableVeAddLiveVideoWaterMarkMethodData = abTestServiceImplClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOfNotNull(it, *it.interfaces.toTypedArray())
                                matcher {
                                    paramCount = 0
                                    returnType = "boolean"
                                    callerMethods {
                                        add {
                                            usingStrings {
                                                add("_with_watermark.mp4")
                                                add("composeWaterMark")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            abTestServiceImplClassData,
                            enableSaveImageToVideoLocalWaterMaskMethodData,
                            enableVeAddLiveVideoWaterMarkMethodData
                        ) { classData, enableSaveImageToVideoLocalWaterMaskMethod, enableVeAddLiveVideoWaterMarkMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            enableSaveImageToVideoLocalWaterMask = method {
                                name = enableSaveImageToVideoLocalWaterMaskMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(enableSaveImageToVideoLocalWaterMaskMethod.paramTypeNames)
                                }
                            }
                            enableVeAddLiveVideoWaterMark = method {
                                name = enableVeAddLiveVideoWaterMarkMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(enableVeAddLiveVideoWaterMarkMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.ABTestServiceImpl.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                actionStatus = actionStatus {
                    runCatching {
                        val actionStatusClassData = bridge.findClass {
                            matcher {
                                superClass = "java.lang.Enum"
                                usingStrings {
                                    add("NORMAL")
                                    add("GRAYED")
                                    add("HIDDEN")
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            actionStatusClassData
                        ) { classData ->
                            class_ = class_ {
                                name = classData.name
                            }
                            grayed = field {
                                name = "GRAYED"
                            }
                            hidden = field {
                                name = "HIDDEN"
                            }
                            normal = field {
                                name = "NORMAL"
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.ActionStatus.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                absPermissionChecker = absPermissionChecker {
                    runCatching {
                        val getActionCheckResultMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                declaredClass {
                                    modifiers = Modifier.ABSTRACT
                                }
                                paramCount = 1
                                usingFields {
                                    // Messy code. Just keep out of sight so long as upper layers stay fine
                                    this@apply.actionStatus.class_.nameOrNull?.let { actionStatusClassName ->
                                        this@apply.actionStatus.normal.nameOrNull?.let { normalFieldName ->
                                            add {
                                                name = normalFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                        this@apply.actionStatus.grayed.nameOrNull?.let { grayedFieldName ->
                                            add {
                                                name = grayedFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                        this@apply.actionStatus.hidden.nameOrNull?.let { hiddenFieldName ->
                                            add {
                                                name = hiddenFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            getActionCheckResultMethodData
                        ) { getActionCheckResultMethod ->
                            class_ = class_ {
                                name = getActionCheckResultMethod.className
                            }
                            getActionCheckResult = method {
                                name = getActionCheckResultMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getActionCheckResultMethod.paramTypeNames)
                                }
                            }
                        }

                        this@apply.actionCheckResult = actionCheckResult {
                            runCatching {
                                val actionCheckResultClassData = getActionCheckResultMethodData.returnType
                                val actionStatusFieldData = actionCheckResultClassData?.let {
                                    bridge.findField {
                                        searchClasses = listOf(it)
                                        matcher {
                                            this@apply.actionStatus.class_.nameOrNull?.let { actionStatusClassName ->
                                                type = actionStatusClassName
                                            }
                                        }
                                    }.singleOrNull()
                                }

                                requireAllNotNullThen(
                                    actionCheckResultClassData,
                                    actionStatusFieldData
                                ) { classData, actionStatusField ->
                                    class_ = class_ {
                                        name = classData.name
                                    }
                                    actionStatus = field {
                                        name = actionStatusField.name
                                    }
                                }
                            }.onFailure {
                                YLog.error(
                                    populateFailedMsg.format(
                                        TAG,
                                        Configs.ActionCheckResult.getDescriptor().name
                                    ),
                                    it
                                )
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.AbsPermissionChecker.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                galleryShareHelper = galleryShareHelper {
                    runCatching {
                        val startDownloadData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "void"
                                params {
                                    add("com.ss.android.ugc.aweme.feed.model.Aweme")
                                    add("java.lang.String")
                                }
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getVideo()Lcom/ss/android/ugc/aweme/feed/model/Video;"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->isAwemeFromXiGua()Z"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getDownloadStatus()I"
                                    }
                                    add {
                                        descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAid()Ljava/lang/String;"
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            startDownloadData
                        ) { startDownloadMethod ->
                            class_ = class_ {
                                name = startDownloadMethod.className
                            }
                            startDownload = method {
                                name = startDownloadMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(startDownloadMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.GalleryShareHelper.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                awemeStatus = awemeStatus {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.AwemeStatus"
                    }
                    downloadStatus = field {
                        name = "downloadStatus"
                    }
                }

                sharePrivacyVideoApi = sharePrivacyVideoApi {
                    runCatching {
                        val getDownloadStatusMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC or Modifier.FINAL
                                declaredClass {
                                    usingStrings {
                                        add("https://www.snssdk.com")
                                    }
                                }
                                returnType = "io.reactivex.Observable"
                                params {
                                    add("java.lang.String")
                                }
                            }
                        }.singleOrNull()
                        val videoResponseClassData = bridge.findMethod {
                            matcher {
                                returnType = "io.reactivex.Observable"
                                params {
                                    add("java.lang.String")
                                }
                                annotations {
                                    add {
                                        type = "retrofit2.http.GET"
                                        addElement {
                                            name = "value"
                                            stringValue("/aweme/privacy_platform/api/privacy/permission/download")
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()?.let { realApiGetDownloadMethodData ->
                            realApiGetDownloadMethodData.className.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                                name = realApiGetDownloadMethodData.name
                            }?.self?.genericReturnType?.asParameterizedTypeOrNull()?.actualTypeArguments?.firstOrNull()?.let {
                                (it as? Class<*>)?.name
                            }?.let {
                                bridge.getClassData(it)
                            }
                        }
                        val msgFieldData = videoResponseClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("msg")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val statusFieldData = videoResponseClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("status")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            getDownloadStatusMethodData,
                            videoResponseClassData,
                            msgFieldData,
                            statusFieldData
                        ) { getDownloadStatusMethod, videoResponseClass, msgField, statusField ->
                            class_ = class_ {
                                name = getDownloadStatusMethod.className
                            }
                            privacyVideoResponse = sharePrivacyVideoResponse {
                                class_ = class_ {
                                    name = videoResponseClass.name
                                }
                                msg = field {
                                    name = msgField.name
                                }
                                status = field {
                                    name = statusField.name
                                }
                            }
                            getDownloadStatus = method {
                                name = getDownloadStatusMethod.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.SharePrivacyVideoApi.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                rxObservable = rxObservable {
                    class_ = class_ {
                        name = "io.reactivex.Observable"
                    }
                    just = method {
                        name = "just"
                        parameters = MethodKt.parameters {
                            values.add("java.lang.Object")
                        }
                    }
                }

                listenAwemeFilter = listenAwemeFilter {
                    runCatching {
                        val acceptMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                returnType = "boolean"
                                params {
                                    add("com.ss.android.ugc.aweme.feed.model.Aweme")
                                    add("java.lang.String")
                                }
                                addUsingString("listen_video_status")
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            acceptMethodData
                        ) { acceptMethod ->
                            class_ = class_ {
                                name = acceptMethod.declaredClassName
                            }
                            accept = method {
                                name = acceptMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(acceptMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.ListenAwemeFilter.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                mainActivity = mainActivity {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.main.MainActivity"
                    }
                    onResume = method {
                        name = "onResume"
                    }
                    onNewIntent = method {
                        name = "onNewIntent"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.add("android.content.Intent")
                        }
                    }
                }

                baseListFragmentPanel = baseListFragmentPanel {
                    runCatching {
                        val baseListFragmentPanelClassData =
                            bridge.getClassData("com.ss.android.ugc.aweme.feed.panel.BaseListFragmentPanel")
                        val handleDoubleClickMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    returnType = "void"
                                    params {
                                        add("android.view.MotionEvent")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val handleVideoEventMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    paramCount = 1
                                    returnType = "void"
                                    usingStrings {
                                        add("handleVideoEvent")
                                    }
                                    invokeMethods {
                                        add {
                                            descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->getAid()Ljava/lang/String;"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getCurrentAwemeMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getCurrentAweme"
                                    paramCount = 0
                                }
                            }.singleOrNull()
                        }
                        val onVideoPlayerEventMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "onVideoPlayerEvent"
                                    paramCount = 1
                                    returnType = "void"
                                }
                            }.singleOrNull()
                        }
                        val handleBigDiggViewClickMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    params {
                                        add("android.view.MotionEvent")
                                        add("com.ss.android.ugc.aweme.feed.adapter.IFeedViewHolder")
                                        add("com.ss.android.ugc.aweme.feed.model.Aweme")
                                    }
                                    returnType = "void"
                                    usingFields {
                                        add {
                                            descriptor = "Lcom/ss/android/ugc/aweme/feed/model/Aweme;->userDigg:I"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val handlePauseMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    returnType = "void"
                                    params {
                                        add("boolean")
                                    }
                                    usingStrings {
                                        add("handlePause")
                                    }
                                }
                            }.singleOrNull()
                        }

                        val canPlayOnVideoReadyMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers(
                                        AccessFlagsMatcher().apply {
                                            modifiers = Modifier.PUBLIC
                                            matchType = MatchType.Equals
                                        }
                                    )
                                    returnType = "boolean"
                                    usingNumbers {
                                        add(1)
                                    }
                                    opNames(listOf("const/4", "return"), opCodeSize = IntRange(2))
                                    callerMethods {
                                        add {
                                            usingStrings {
                                                add("handleTextureAvailable", StringMatchType.Equals)
                                                add("BaseListFragmentPanel", StringMatchType.Equals)
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        val mPlayStateHelperFieldData = baseListFragmentPanelClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    type {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        fields {
                                            add {
                                                modifiers = Modifier.PUBLIC
                                                type = "int"
                                                readMethods {
                                                    add {
                                                        declaredClass = it.name
                                                        usingStrings {
                                                            add("onNetStateChangeEvent")
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            baseListFragmentPanelClassData,
                            handleDoubleClickMethodData,
                            handleVideoEventMethodData,
                            getCurrentAwemeMethodData,
                            onVideoPlayerEventMethodData,
                            handleBigDiggViewClickMethodData,
                            handlePauseMethodData,
                            canPlayOnVideoReadyMethodData,
                            mPlayStateHelperFieldData
                        ) {
                                classData,
                                handleDoubleClickMethod,
                                handleVideoEventMethod,
                                getCurrentAwemeMethod,
                                onVideoPlayerEventMethod,
                                handleBigDiggViewClickMethod,
                                handlePauseMethod,
                                canPlayOnVideoReadyMethod,
                                mPlayStateHelperField
                            ->
                            class_ = class_ {
                                name = classData.name
                            }
                            handleDoubleClick = method {
                                name = handleDoubleClickMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(handleDoubleClickMethod.paramTypeNames)
                                }
                            }
                            handleVideoEvent = method {
                                name = handleVideoEventMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(handleVideoEventMethod.paramTypeNames)
                                }
                            }
                            getCurrentAweme = method {
                                name = getCurrentAwemeMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getCurrentAwemeMethod.paramTypeNames)
                                }
                            }
                            onVideoPlayerEvent = method {
                                name = onVideoPlayerEventMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(onVideoPlayerEventMethod.paramTypeNames)
                                }
                            }
                            handleBigDiggViewClick = method {
                                name = handleBigDiggViewClickMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(handleBigDiggViewClickMethod.paramTypeNames)
                                }
                            }
                            handlePause = method {
                                name = handlePauseMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(handlePauseMethod.paramTypeNames)
                                }
                            }
                            canPlayOnVideoReady = method {
                                name = canPlayOnVideoReadyMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(canPlayOnVideoReadyMethod.paramTypeNames)
                                }
                            }
                            mPlayStateHelper = field {
                                name = mPlayStateHelperField.name
                            }
                        }

                        this@apply.videoPlayerStatus = videoPlayerStatus {
                            runCatching {
                                val videoPlayerStatusClassData = onVideoPlayerEventMethodData.paramTypes.singleOrNull()
                                val codeFieldData = videoPlayerStatusClassData?.let {
                                    bridge.findField {
                                        searchClasses = listOf(it)
                                        matcher {
                                            modifiers = Modifier.PUBLIC or Modifier.FINAL
                                            type = "int"
                                        }
                                    }.singleOrNull()
                                }

                                requireAllNotNullThen(
                                    videoPlayerStatusClassData,
                                    codeFieldData
                                ) { classData, codeField ->
                                    class_ = class_ {
                                        name = classData.name
                                    }
                                    code = field {
                                        name = codeField.name
                                    }
                                }
                            }.onFailure {
                                YLog.error(
                                    populateFailedMsg.format(
                                        TAG,
                                        Configs.VideoPlayerStatus.getDescriptor().name
                                    ),
                                    it
                                )
                            }
                        }
                        this@apply.playStatusHelper = playStatusHelper {
                            runCatching {
                                val playStatusHelperClassData = mPlayStateHelperFieldData.type
                                val playStatusFieldData = bridge.findField {
                                    searchClasses = listOf(playStatusHelperClassData)
                                    matcher {
                                        modifiers = Modifier.PUBLIC
                                        type = "int"
                                    }
                                }.singleOrNull()

                                requireAllNotNullThen(
                                    playStatusHelperClassData,
                                    playStatusFieldData
                                ) { classData, playStatusField ->
                                    class_ = class_ {
                                        name = classData.name
                                    }
                                    playStatus = field {
                                        name = playStatusField.name
                                    }
                                }
                            }.onFailure {
                                YLog.error(
                                    populateFailedMsg.format(
                                        TAG,
                                        Configs.PlayStatusHelper.getDescriptor().name
                                    ),
                                    it
                                )
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.BaseListFragmentPanel.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                videoEvent = videoEvent {
                    runCatching {
                        val videoEventClassData = bridge.findClass {
                            matcher {
                                usingStrings {
                                    add {
                                        value = "VideoEvent"
                                        matchType = StringMatchType.Contains
                                    }
                                    add {
                                        value = "param"
                                        matchType = StringMatchType.Contains
                                    }
                                    add {
                                        value = "videoType"
                                        matchType = StringMatchType.Contains
                                    }
                                    add {
                                        value = "isPlaying"
                                        matchType = StringMatchType.Contains
                                    }
                                }
                                methods {
                                    add {
                                        name = "toString"
                                    }
                                }
                            }
                        }.singleOrNull()
                        val videTypeFieldData = videoEventClassData?.let {
                            bridge.findField {
                                searchInClass(listOf(it))
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    type = "int"
                                    writeMethods {
                                        add {
                                            name = "<init>"
                                            paramTypes("int")
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            videoEventClassData,
                            videTypeFieldData
                        ) { classData, videTypeField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            type = field {
                                name = videTypeField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.VideoEvent.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                cleanModePresenter = cleanModePresenter {
                    runCatching {
                        val cleanModePresenterClassData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.feed.plato.business.contentconsumption.cleanmode.CleanModePresenter"
                        )
                        val manageViewVisibilityMethodData = cleanModePresenterClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    returnType = "void"
                                    params {
                                        add("android.view.View")
                                        add("int")
                                        add("int")
                                        add("boolean")
                                    }
                                    usingStrings {
                                        add("curMarPriority")
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            cleanModePresenterClassData,
                            manageViewVisibilityMethodData
                        ) { classData, manageViewVisibilityMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            manageViewVisibility = method {
                                name = manageViewVisibilityMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(manageViewVisibilityMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CleanModePresenter.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                danmakuView = danmakuView {
                    class_ = class_ {
                        name = "com.bytedance.common.ultra.danmaku.view.DanmakuView"
                    }
                    onAttachedToWindow = method {
                        name = "onAttachedToWindow"
                    }
                }

                fluxComponentId = fluxComponentId {
                    runCatching {
                        val fluxComponentClassData = bridge.findClass {
                            matcher {
                                superClass = "java.lang.Enum"
                                fields {
                                    add {
                                        name = "MUSIC_COVER"
                                    }
                                    add {
                                        name = "REPORT_WARN"
                                    }
                                    add {
                                        name = "DANMAKU_VERTICAL"
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            fluxComponentClassData
                        ) { classData ->
                            class_ = class_ {
                                name = classData.name
                            }
                            musicCoverBlock = field {
                                name = "MUSIC_COVER_BLOCK"
                            }
                            musicListenCover = field {
                                name = "MUSIC_LISTEN_COVER"
                            }
                            digg = field {
                                name = "DIGG"
                            }
                            title = field {
                                name = "TITLE"
                            }
                            musicCover = field {
                                name = "MUSIC_COVER"
                            }
                            generalLabel = field {
                                name = "GENERAL_LABEL"
                            }
                            feedLabelContainer = field {
                                name = "FEED_LABEL_CONTAINER"
                            }
                            musicTitle = field {
                                name = "MUSIC_TITLE"
                            }
                            story25DiverseDigg = field {
                                name = "STORY_25_DIVERSE_DIGG"
                            }
                            ecomStore = field {
                                name = "ECOM_STORE"
                            }
                            buttonImQuickShare = field {
                                name = "BUTTON_IM_QUICK_SHARE"
                            }
                            buttonFeedImShareGuideV2 = field {
                                name = "BUTTON_FEED_IM_SHARE_GUIDE_V2"
                            }
                            buttonForceFeedImShareGuide = field {
                                name = "BUTTON_FORCE_FEED_IM_SHARE_GUIDE"
                            }
                            socialNewCommentGuideBubble = field {
                                name = "SOCIAL_NEW_COMMENT_GUIDE_BUBBLE"
                            }
                            commentBottomAnimation = field {
                                name = "COMMENT_BOTTOM_ANIMATION"
                            }
                            nearbyIdentityTag = field {
                                name = "NEARBY_IDENTITY_TAG"
                            }
                            livePhotoTag = field {
                                name = "LIVE_PHOTO_TAG"
                            }
                            photosTag = field {
                                name = "PHOTOS_TAG"
                            }
                            story24Tag = field {
                                name = "STORY24_TAG"
                            }
                            socialNewStyleStoryTag = field {
                                name = "SOCIAL_NEW_STYLE_STORY_TAG"
                            }
                            longVideoHighlightTag = field {
                                name = "LONG_VIDEO_HIGHLIGHT_TAG"
                            }
                            danmakuVertical = field {
                                name = "DANMAKU_VERTICAL"
                            }
                            avatar = field {
                                name = "AVATAR"
                            }
                            nickname = field {
                                name = "NICKNAME"
                            }
                            postTime = field {
                                name = "POST_TIME"
                            }
                            bellowDescTime = field {
                                name = "BELLOW_DESC_TIME"
                            }
                            this@fluxComponentId.comment = field {
                                name = "COMMENT"
                            }
                            reply = field {
                                name = "REPLY"
                            }
                            share = field {
                                name = "SHARE"
                            }
                            collect = field {
                                name = "COLLECT"
                            }
                            anchorFramework = field {
                                name = "ANCHOR_FRAMEWORK"
                            }
                            bottomBarCommon = field {
                                name = "BOTTOM_BAR_COMMON"
                            }
                            commonButton = field {
                                name = "COMMON_BUTTON"
                            }
                            sticker = field {
                                name = "STICKER"
                            }
                            aiSearch = field {
                                name = "AI_SEARCH"
                            }
                            c2Feed = field {
                                name = "C2_FEED"
                            }
                            flow = field {
                                name = "FLOW"
                            }
                            nearbyHotComment = field {
                                name = "NEARBY_HOT_COMMENT"
                            }
                            buttonUnfollowFamiliar = field {
                                name = "BUTTON_UNFOLLOW_FAMILIAR"
                            }
                            buttonUnfollowFamiliarRec = field {
                                name = "BUTTON_UNFOLLOW_FAMILIAR_REC"
                            }
                            coCreatorAuthor = field {
                                name = "CO_CREATOR_AUTHOR"
                            }
                            chapterTag = field {
                                name = "CHAPTER_TAG"
                            }
                            ecomTagFriend = field {
                                name = "ECOM_TAG_FRIEND"
                            }
                            socialNewStylePostTimeBottom = field {
                                name = "SOCIAL_NEW_STYLE_POST_TIME_BOTTOM"
                            }
                            socialNewStyleMusicBelow = field {
                                name = "SOCIAL_NEW_STYLE_MUSIC_BELOW"
                            }
                            chapterDetail = field {
                                name = "CHAPTER_DETAIL"
                            }
                            titleTagContainer = field {
                                name = "TITLE_TAG_CONTAINER"
                            }
                            rightMenuLl = field {
                                name = "RIGHT_MENU_LL"
                            }
                            musicMuteCover = field {
                                name = "MUSIC_MUTE_COVER"
                            }
                            jxLeftBottomLongVideoPlusTitleTag = field {
                                name = "JX_LEFT_BOTTOM_LONG_VIDEO_PLUS_TITLE_TAG"
                            }
                            bottomBarMix = field {
                                name = "BOTTOM_BAR_MIX"
                            }
                            bottomBarNormalSearch = field {
                                name = "BOTTOM_BAR_NORMAL_SEARCH"
                            }
                            bottomBarCommonPrioritySearch = field {
                                name = "BOTTOM_BAR_COMMON_PRIORITY_SEARCH"
                            }
                            jxPick = field {
                                name = "JX_PICK"
                            }
                            bottomBarContainer = field {
                                name = "BOTTOM_BAR_CONTAINER"
                            }
                            aiCoCreatorsThree = field {
                                name = "AI_CO_CREATORS_THREE"
                            }
                            aigcCocreateStatusTitle = field {
                                name = "AIGC_COCREATE_STATUS_TITLE"
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.FluxComponentId.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                fluxComponentDataAction = fluxComponentDataAction {
                    runCatching {
                        val fluxComponentDataActionClassData =
                            bridge.getClassData("com.ss.android.ugc.aweme.flux.core.data.FluxComponentDataAction")
                        val getSetMethodData = fluxComponentDataActionClassData?.let {
                            bridge.findMethod {
                                searchInClass(listOf(it))
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "java.util.Set"
                                    paramCount = 0
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            fluxComponentDataActionClassData,
                            getSetMethodData
                        ) { classData, getSetMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getSet = method {
                                name = getSetMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getSetMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.FluxComponentDataAction.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                heif = heif {
                    runCatching {
                        val heifClassData = bridge.getClassData("com.bytedance.fresco.nativeheif.Heif")
                        val toRgbaMethodData = heifClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "toRgba"
                                    params {
                                        add("byte[]") // vvicBytes
                                        add("boolean") // ttheifOpt
                                        add("int") // length
                                        add("boolean") // vvicDecOpt
                                        add("int") // vvicOptMode
                                        add("boolean") // heicUseWpp
                                        add("int") // heicDecodeThreads
                                        add("boolean") // vvicUseWpp
                                        add("int") // vvicDecodeThreads
                                        add("int") // sampleSize
                                        add("int") // cropLeft
                                        add("int") // cropTop
                                        add("int") // cropHeight
                                        add("int") // cropWidth
                                        add("boolean") // fixVvicDecode
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            heifClassData,
                            toRgbaMethodData
                        ) { classData, toRgbaMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            toRgba = method {
                                name = toRgbaMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(toRgbaMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.Heif.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                heifData = heifData {
                    runCatching {
                        val heifDataClassData = bridge.getClassData("com.bytedance.fresco.nativeheif.HeifData")
                        val newBitmapMethodData = heifDataClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "newBitmap"
                                    paramCount = 2
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            heifDataClassData,
                            newBitmapMethodData
                        ) { classData, newBitmapMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            newBitmap = method {
                                name = newBitmapMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(newBitmapMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.HeifData.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                closeableReference = closeableReference {
                    runCatching {
                        val closeableReferenceClassData = bridge.getClassData("com.facebook.common.references.CloseableReference")
                        val getMethodData = closeableReferenceClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "get"
                                    paramCount = 0
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            closeableReferenceClassData,
                            getMethodData
                        ) { classData, getMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            get = method {
                                name = getMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CloseableReference.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                storyServiceImpl = storyServiceImpl {
                    runCatching {
                        val storyServiceImplClassData = bridge.findClass {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                interfaces {
                                    add {
                                        className = "com.ss.android.ugc.aweme.services.external.ui.IStoryService"
                                        modifiers = Modifier.PUBLIC or Modifier.INTERFACE or Modifier.ABSTRACT
                                    }
                                }
                            }
                        }.singleOrNull {
                            it.simpleName != "StubAllServices"
                        }
                        val singleLivePhotoToMp4MethodData = storyServiceImplClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "void"
                                    params {
                                        add("android.app.Activity")
                                        add("androidx.lifecycle.LifecycleOwner")
                                        add("java.util.List")
                                        add("com.ss.android.ugc.aweme.music.model.Music")
                                        add("boolean")
                                        add("boolean")
                                        add("java.lang.String")
                                        add("java.lang.Integer")
                                        add("kotlin.jvm.functions.Function1")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val convertImgToMp4MethodData = storyServiceImplClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "void"
                                    paramCount = 8
                                    usingStrings {
                                        add("asve")
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            storyServiceImplClassData,
                            singleLivePhotoToMp4MethodData,
                            convertImgToMp4MethodData
                        ) { classData, singleLivePhotoToMp4Method, convertImgToMp4Method ->
                            class_ = class_ {
                                name = classData.name
                            }
                            convertSingleLivePhotoToMp4UseMusicUrl = method {
                                name = singleLivePhotoToMp4Method.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(singleLivePhotoToMp4Method.paramTypeNames)
                                }
                            }

                            convertImgToMp4 = method {
                                name = convertImgToMp4Method.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(convertImgToMp4Method.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.StoryServiceImpl.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                homeTabDataSourceServer = homeTabDataSourceServer {
                    runCatching {
                        val homeTabDataSourceServerClassData = bridge.findClass {
                            matcher {
                                usingStrings {
                                    add("HomeTabDataSourceServer", StringMatchType.Equals)
                                    add("buildShowingTopTabIds")
                                }
                            }
                        }.singleOrNull()
                        val getShowingTopTabIdsMethodData = homeTabDataSourceServerClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "java.util.List"
                                    usingStrings {
                                        add("buildShowingTopTabIds")
                                    }
                                    invokeMethods {
                                        add {
                                            modifiers = Modifier.PUBLIC
                                            returnType = "boolean"
                                            usingFields {
                                                add {
                                                    annotations {
                                                        add {
                                                            type = "com.google.gson.annotations.SerializedName"
                                                            addElement {
                                                                name = "value"
                                                                stringValue("is_show", StringMatchType.Equals)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            declaredClass {
                                                fields {
                                                    add {
                                                        annotations {
                                                            add {
                                                                type = "com.google.gson.annotations.SerializedName"
                                                                addElement {
                                                                    name = "value"
                                                                    stringValue("movable", StringMatchType.Equals)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    add {
                                                        annotations {
                                                            add {
                                                                type = "com.google.gson.annotations.SerializedName"
                                                                addElement {
                                                                    name = "value"
                                                                    stringValue("drag_type", StringMatchType.Equals)
                                                                }
                                                            }
                                                        }
                                                    }
                                                    add {
                                                        annotations {
                                                            add {
                                                                type = "com.google.gson.annotations.SerializedName"
                                                                addElement {
                                                                    name = "value"
                                                                    stringValue("extra", StringMatchType.Equals)
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getShowingBottomTabIdsMethodData = homeTabDataSourceServerClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "java.util.List"
                                    usingStrings {
                                        add("buildShowingTopTabIds")
                                    }
                                }
                            }.singleOrNull { methodData ->
                                methodData.name != getShowingTopTabIdsMethodData?.name
                            }
                        }

                        requireAllNotNullThen(
                            homeTabDataSourceServerClassData,
                            getShowingBottomTabIdsMethodData,
                            getShowingTopTabIdsMethodData
                        ) { classData, getShowingBottomTabIdsMethod, getShowingTopTabIdsMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getShowingBottomTabIds = method {
                                name = getShowingBottomTabIdsMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getShowingBottomTabIdsMethod.paramTypeNames)
                                }
                            }
                            getShowingTopTabIds = method {
                                name = getShowingTopTabIdsMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getShowingTopTabIdsMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.HomeTabDataSourceServer.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                homeTabDataSourceDefault = homeTabDataSourceDefault {
                    runCatching {
                        val homeTabDataSourceDefaultClassData = bridge.findClass {
                            matcher {
                                usingStrings {
                                    add("HomeTabDataSourceDefault", StringMatchType.Equals)
                                    add("buildEditedTopTabs")
                                }
                            }
                        }.singleOrNull()
                        val getShowingTopTabIdsMethodData = homeTabDataSourceDefaultClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    this@apply.homeTabDataSourceServer.getShowingTopTabIds.nameOrNull?.let { methodName ->
                                        name = methodName
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getShowingBottomTabIdsMethodData = homeTabDataSourceDefaultClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    this@apply.homeTabDataSourceServer.getShowingBottomTabIds.nameOrNull?.let { methodName ->
                                        name = methodName
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            homeTabDataSourceDefaultClassData,
                            getShowingBottomTabIdsMethodData,
                            getShowingTopTabIdsMethodData
                        ) { classData, getShowingBottomTabIdsMethod, getShowingTopTabIdsMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getShowingBottomTabIds = method {
                                name = getShowingBottomTabIdsMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getShowingBottomTabIdsMethod.paramTypeNames)
                                }
                            }
                            getShowingTopTabIds = method {
                                name = getShowingTopTabIdsMethod.name
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getShowingTopTabIdsMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.HomeTabDataSourceDefault.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                feedAvatarPresenter = feedAvatarPresenter {
                    runCatching {
                        val feedAvatarPresenterClassData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.feed.quick.presenter.FeedAvatarPresenter"
                        )
                        val updateFollowViewMethodData = feedAvatarPresenterClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    params {
                                        add("int")
                                        add("boolean")
                                    }
                                    returnType = "void"
                                    usingStrings {
                                        add("updateFollowView")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val performFollowMethodData = feedAvatarPresenterClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC
                                    params {
                                        add("java.lang.String")
                                    }
                                    returnType = "boolean"
                                    usingStrings {
                                        add("plus_sign")
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            feedAvatarPresenterClassData,
                            updateFollowViewMethodData,
                            performFollowMethodData
                        ) { classData, updateFollowViewMethod, performFollowMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            updateFollowView = method {
                                name = updateFollowViewMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(updateFollowViewMethod.paramTypeNames)
                                }
                            }
                            performFollow = method {
                                name = performFollowMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(performFollowMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.FeedAvatarPresenter.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                heifImage = heifImage {
                    runCatching {
                        val heifImageClassData = bridge.getClassData(
                            "com.bytedance.fresco.animatedheif.HeifImage"
                        )
                        val createMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "create"
                                    modifiers = Modifier.PUBLIC or Modifier.STATIC
                                    returnType = "com.bytedance.fresco.animatedheif.HeifImage"
                                    params {
                                        add("byte[]")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val disposeMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "dispose"
                                    returnType = "void"
                                }
                            }.singleOrNull()
                        }
                        val getWidthMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getWidth"
                                    returnType = "int"
                                }
                            }.singleOrNull()
                        }
                        val getHeightMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getHeight"
                                    returnType = "int"
                                }
                            }.singleOrNull()
                        }
                        val getFrameMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getFrame"
                                    returnType = "com.bytedance.fresco.animatedheif.HeifFrame"
                                    params {
                                        add("int")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val getFrameCountMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getFrameCount"
                                    returnType = "int"
                                }
                            }.singleOrNull()
                        }
                        val getFrameDurationsMethodData = heifImageClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "getFrameDurations"
                                    returnType = "int[]"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            heifImageClassData,
                            createMethodData,
                            disposeMethodData,
                            getWidthMethodData,
                            getHeightMethodData,
                            getFrameMethodData,
                            getFrameCountMethodData,
                            getFrameDurationsMethodData
                        ) {
                                classData,
                                createMethod,
                                disposeMethod,
                                getWidthMethod,
                                getHeightMethod,
                                getFrameMethod,
                                getFrameCountMethod,
                                getFrameDurationsMethod
                            ->
                            class_ = class_ {
                                name = classData.name
                            }
                            create = method {
                                name = createMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(createMethod.paramTypeNames)
                                }
                            }
                            dispose = method {
                                name = disposeMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(disposeMethod.paramTypeNames)
                                }
                            }
                            getWidth = method {
                                name = getWidthMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getWidthMethod.paramTypeNames)
                                }
                            }
                            getHeight = method {
                                name = getHeightMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getHeightMethod.paramTypeNames)
                                }
                            }
                            getFrame = method {
                                name = getFrameMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getFrameMethod.paramTypeNames)
                                }
                            }
                            getFrameCount = method {
                                name = getFrameCountMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getFrameCountMethod.paramTypeNames)
                                }
                            }
                            getFrameDurations = method {
                                name = getFrameDurationsMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getFrameDurationsMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.HeifImage.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                heifFrame = heifFrame {
                    runCatching {
                        val heifFrameClassData = bridge.getClassData(
                            "com.bytedance.fresco.animatedheif.HeifFrame"
                        )
                        val renderFrameMethodData = heifFrameClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "renderFrame"
                                    returnType = "void"
                                    params {
                                        add("int")
                                        add("int")
                                        add("android.graphics.Bitmap")
                                    }
                                }
                            }.singleOrNull()
                        }
                        val disposeMethodData = heifFrameClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "dispose"
                                    returnType = "void"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            heifFrameClassData,
                            renderFrameMethodData,
                            disposeMethodData
                        ) { classData, renderFrameMethod, disposeMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            renderFrame = method {
                                name = renderFrameMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(renderFrameMethod.paramTypeNames)
                                }
                            }
                            dispose = method {
                                name = disposeMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(disposeMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.HeifFrame.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                dismissWindowEvent = dismissWindowEvent {
                    runCatching {
                        val dismissWindowEventClassData = bridge.findClass {
                            matcher {
                                method {
                                    name = "toString"
                                    usingStrings {
                                        add("DismissWindowEvent", matchType = StringMatchType.StartsWith)
                                    }
                                }
                            }
                        }.singleOrNull()
                        val dismissWithSurfaceFieldData = dismissWindowEventClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "java.lang.Boolean"
                                }
                            }.singleOrNull()
                        }
                        val enterMethodFieldData = dismissWindowEventClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "java.lang.String"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            dismissWindowEventClassData,
                            dismissWithSurfaceFieldData,
                            enterMethodFieldData
                        ) { classData, dismissWithSurfaceField, enterMethodField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            dismissWithSurface = field {
                                name = dismissWithSurfaceField.name
                            }
                            enterMethod = field {
                                name = enterMethodField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.DismissWindowEvent.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                pinchViewModel = pinchViewModel {
                    runCatching {
                        val pinchViewModelClassData = bridge.findClass {
                            matcher {
                                superClass = "androidx.lifecycle.ViewModel"
                                usingStrings {
                                    add("livesdk_co_play")
                                }
                            }
                        }.singleOrNull()
                        val dismissWindowMethodData = pinchViewModelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    params {
                                        add("java.lang.Boolean")
                                    }
                                    returnType = "void"
                                    usingStrings {
                                        add("", matchType = StringMatchType.Equals)
                                    }
                                    invokeMethods {
                                        add {
                                            descriptor = "Landroidx/lifecycle/LiveData;->setValue(Ljava/lang/Object;)V"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val setDismissWindowEventMethodData = pinchViewModelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    params {
                                        this@apply.dismissWindowEvent.class_.nameOrNull?.let { eventClassName ->
                                            add(eventClassName)
                                        }
                                    }
                                    returnType = "void"
                                    invokeMethods {
                                        add {
                                            descriptor = "Landroidx/lifecycle/LiveData;->setValue(Ljava/lang/Object;)V"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            pinchViewModelClassData,
                            dismissWindowMethodData,
                            setDismissWindowEventMethodData
                        ) { classData, dismissWindowMethod, setDismissWindowEventMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            dismissWindow = method {
                                name = dismissWindowMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(dismissWindowMethod.paramTypeNames)
                                }
                            }
                            setDismissWindowEvent = method {
                                name = setDismissWindowEventMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(setDismissWindowEventMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.PinchViewModel.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                topCornerEntranceData = topCornerEntranceData {
                    runCatching {
                        val topCornerEntranceDataClassData = bridge.findClass {
                            matcher {
                                method {
                                    name = "toString"
                                    usingStrings {
                                        add("TopCornerEntranceData", matchType = StringMatchType.StartsWith)
                                    }
                                }
                            }
                        }.singleOrNull()
                        val entranceIdFieldData = topCornerEntranceDataClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("entrance_id")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            topCornerEntranceDataClassData,
                            entranceIdFieldData
                        ) { classData, entranceIdField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            entranceId = field {
                                name = entranceIdField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.TopCornerEntranceData.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                topCornerEntranceDataSource = topCornerEntranceDataSource {
                    runCatching {
                        val getRightEntranceListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.SYNCHRONIZED
                                paramCount = 0
                                returnType = "java.util.List"
                                usingStrings {
                                    add("homepage_right_search")
                                }
                            }
                        }.singleOrNull()
                        val topCornerEntranceDataSourceClassData = getRightEntranceListMethodData?.declaredClass
                        val getLeftEntranceListMethodData = topCornerEntranceDataSourceClassData?.methods?.singleOrNull { method ->
                            method.returnType?.name == "java.util.List" && method.name != getRightEntranceListMethodData.name
                        }
                        val leftEntranceListFieldData = getLeftEntranceListMethodData?.let {
                            it.usingFields.filter { usingFieldData ->
                                usingFieldData.usingType == FieldUsingType.Write
                            }.map { usingFieldData ->
                                usingFieldData.field
                            }.singleOrNull { field ->
                                field.declaredClass == it.declaredClass && field.typeName == "java.util.List"
                            }
                        }

                        requireAllNotNullThen(
                            topCornerEntranceDataSourceClassData,
                            getRightEntranceListMethodData,
                            getLeftEntranceListMethodData,
                            leftEntranceListFieldData
                        ) { classData, getRightEntranceListMethod, getLeftEntranceListMethod, leftEntranceListField ->
                            class_ = class_ {
                                name = classData.name
                            }
                            leftEntranceList = field {
                                name = leftEntranceListField.name
                            }
                            getLeftEntranceList = method {
                                name = getLeftEntranceListMethod.methodName
                            }
                            getRightEntranceList = method {
                                name = getRightEntranceListMethod.methodName
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.TopCornerEntranceDataSource.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                compilationSpeedInheritComponent = compilationSpeedInheritComponent {
                    runCatching {
                        val classData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.feed.plato.business.contentconsumption.speed.CompilationSpeedInheritComponent"
                        )
                        val needInheritSpeedMethodData = classData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    returnType = "boolean"
                                    paramCount = 0
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            classData,
                            needInheritSpeedMethodData
                        ) { classData, needInheritSpeedMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            needInheritSpeed = method {
                                name = needInheritSpeedMethod.methodName
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.CompilationSpeedInheritComponent.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                if (hostVersionCode < 400301) {
                    story25LongPressPanelConfig = story25LongPressPanelConfig {
                        runCatching {
                            val functionGroupListFieldData = bridge.findField {
                                matcher {
                                    annotations {
                                        add {
                                            type = "com.google.gson.annotations.SerializedName"
                                            addElement {
                                                name = "value"
                                                stringValue("function_group_list")
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                            val story25LongPressPanelConfigClassData = functionGroupListFieldData?.declaredClass
                            val getFunctionGroupListMethodData = story25LongPressPanelConfigClassData?.let {
                                bridge.findMethod {
                                    searchClasses = listOf(it)
                                    matcher {
                                        returnType = "java.util.List"
                                        paramCount = 0
                                    }
                                }.singleOrNull()
                            }

                            requireAllNotNullThen(
                                story25LongPressPanelConfigClassData,
                                getFunctionGroupListMethodData
                            ) { classData, getFunctionGroupListMethod ->
                                class_ = class_ {
                                    name = classData.name
                                }
                                getFunctionGroupList = method {
                                    name = getFunctionGroupListMethod.methodName
                                    parameters = MethodKt.parameters {
                                        values.clear()
                                        values.addAll(getFunctionGroupListMethod.paramTypeNames)
                                    }
                                }
                            }
                        }.onFailure {
                            YLog.error(
                                populateFailedMsg.format(
                                    TAG,
                                    Configs.Story25LongPressPanelConfig.getDescriptor().name
                                ),
                                it
                            )
                        }
                    }
                } else {
                    YLog.warn(
                        "$TAG: ${Configs.Story25LongPressPanelConfig.getDescriptor().name} has been removed since host version 40.3.0," +
                            " fields are intentionally left unfilled"
                    )
                }

                lppConfigModuleId = lPPConfigModuleId {
                    runCatching {
                        val lppConfigModuleIdClassData = bridge.findClass {
                            matcher {
                                superClass = "java.lang.Enum"
                                usingStrings {
                                    add("block_post", StringMatchType.Equals)
                                    add("dislike", StringMatchType.Equals)
                                    add("download", StringMatchType.Equals)
                                    add("2_story", StringMatchType.Equals)
                                }
                            }
                        }.singleOrNull()
                        val getIdMethodData = lppConfigModuleIdClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    paramCount = 0
                                    returnType = "java.lang.String"
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            lppConfigModuleIdClassData,
                            getIdMethodData
                        ) { classData, getIdMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getId = method {
                                name = getIdMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getIdMethod.paramTypeNames)
                                }
                            }
                        }
                        // In fact, enum fields may be renamed by R8.
                        // As the fields are hard to reference, keep hardcoding.
                        toStory = field {
                            name = "TO_STORY"
                        }
                        dislike = field {
                            name = "DISLIKE"
                        }
                        report = field {
                            name = "REPORT"
                        }
                        blockPost = field {
                            name = "BLOCK_POST"
                        }
                        auth = field {
                            name = "AUTH"
                        }
                        download = field {
                            name = "DOWNLOAD"
                        }
                        timingClose = field {
                            name = "TIMING_CLOSE"
                        }
                        autoFeed = field {
                            name = "AUTO_FEED"
                        }
                        autoFeedPad = field {
                            name = "AUTO_FEED_PAD"
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.LPPConfigModuleId.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                story25SharePanelConfig = story25SharePanelConfig {
                    runCatching {
                        val otherFunctionWhiteListFieldData = bridge.findField {
                            matcher {
                                annotations {
                                    add {
                                        type = "com.google.gson.annotations.SerializedName"
                                        addElement {
                                            name = "value"
                                            stringValue("other_function_white_list")
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()
                        val story25SharePanelConfigClassData = otherFunctionWhiteListFieldData?.declaredClass
                        val getOtherFunctionWhiteListMethodData = story25SharePanelConfigClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    returnType = "java.util.List"
                                    paramCount = 0
                                    addUsingField {
                                        descriptor = otherFunctionWhiteListFieldData.descriptor
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            story25SharePanelConfigClassData,
                            getOtherFunctionWhiteListMethodData
                        ) { classData, getOtherFunctionWhiteListMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getOtherFunctionWhiteList = method {
                                name = getOtherFunctionWhiteListMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getOtherFunctionWhiteListMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.Story25SharePanelConfig.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                sharePanelFuncConfig = sharePanelFuncConfig {
                    runCatching {
                        val funcConfigFieldData = bridge.findField {
                            matcher {
                                type = "java.util.List"
                                declaredClass {
                                    // Another `Diff` class holds a field annotated base_conf;
                                    // this is not our target, filter it out.
                                    noneOf {
                                        add {
                                            fields {
                                                add {
                                                    annotations {
                                                        add {
                                                            type = "com.google.gson.annotations.SerializedName"
                                                            addElement {
                                                                name = "value"
                                                                stringValue("base_conf")
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                annotations {
                                    add {
                                        type = "com.google.gson.annotations.SerializedName"
                                        addElement {
                                            name = "value"
                                            stringValue("func_config")
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()
                        val funcConfigClassData = funcConfigFieldData?.declaredClass
                        val getFuncConfigMethodData = funcConfigClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    returnType = "java.util.List"
                                    addUsingField {
                                        usingType = UsingType.Read
                                        descriptor = funcConfigFieldData.descriptor
                                    }
                                }
                            }.singleOrNull()
                        }

                        requireAllNotNullThen(
                            funcConfigClassData,
                            getFuncConfigMethodData
                        ) { classData, getFuncConfigMethod ->
                            class_ = class_ {
                                name = classData.name
                            }
                            getFuncConfig = method {
                                name = getFuncConfigMethod.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(getFuncConfigMethod.paramTypeNames)
                                }
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.SharePanelFuncConfig.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                sharePanelLine = sharePanelLine {
                    runCatching {
                        val lineFieldData = bridge.findField {
                            matcher {
                                declaredClass {
                                    interfaces {
                                        add {
                                            className = "android.os.Parcelable"
                                        }
                                    }
                                }
                                type = "java.util.List"
                                annotations {
                                    add {
                                        type = "com.google.gson.annotations.SerializedName"
                                        addElement {
                                            name = "value"
                                            stringValue("line", matchType = StringMatchType.Equals)
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            lineFieldData
                        ) { lineField ->
                            class_ = class_ {
                                name = lineField.className
                            }
                            line = field {
                                name = lineField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.SharePanelLine.getDescriptor().name
                            ),
                            it
                        )
                    }
                }

                sharePanelBtn = sharePanelBtn {
                    runCatching {
                        // Target field annotation value is "key";
                        // its declaring class needs a field annotated with "light_icon_url".
                        val keyFieldData = bridge.findField {
                            matcher {
                                declaredClass {
                                    interfaces {
                                        add {
                                            className = "android.os.Parcelable"
                                        }
                                    }
                                    fields {
                                        add {
                                            annotations {
                                                add {
                                                    type = "com.google.gson.annotations.SerializedName"
                                                    addElement {
                                                        name = "value"
                                                        stringValue("light_icon_url")
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                type = "java.lang.String"
                                annotations {
                                    add {
                                        type = "com.google.gson.annotations.SerializedName"
                                        addElement {
                                            name = "value"
                                            stringValue("key")
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()

                        requireNotNullThen(
                            keyFieldData
                        ) { keyField ->
                            class_ = class_ {
                                name = keyField.className
                            }
                            key = field {
                                name = keyField.name
                            }
                        }
                    }.onFailure {
                        YLog.error(
                            populateFailedMsg.format(
                                TAG,
                                Configs.SharePanelBtn.getDescriptor().name
                            ),
                            it
                        )
                    }
                }
            }
        }
    }
}
