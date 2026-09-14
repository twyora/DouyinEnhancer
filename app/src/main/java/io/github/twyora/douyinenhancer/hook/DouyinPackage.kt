/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package io.github.twyora.douyinenhancer.hook

import android.app.AndroidAppHelper
import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.kavaref.extension.asParameterizedTypeOrNull
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.generated.AppProperties
import io.github.twyora.douyinenhancer.utils.Field
import io.github.twyora.douyinenhancer.utils.Method
import io.github.twyora.douyinenhancer.utils.toClass
import io.github.twyora.douyinenhancer.utils.weak
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.reflect.Modifier
import kotlin.time.measureTimedValue
import org.luckypray.dexkit.DexKitBridge
import org.luckypray.dexkit.query.enums.StringMatchType
import org.luckypray.dexkit.query.matchers.base.OpCodesMatcher

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

class DouyinPackage(classLoader: ClassLoader, context: Context) {
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
    val danmakuView = DanmakuViewModule(hookInfo.danmakuView, classLoader)
    val fluxComponentId = FluxComponentIdModule(hookInfo.fluxComponentId, classLoader)
    val fluxComponentDataAction = FluxComponentDataActionModule(hookInfo.fluxComponentDataAction, classLoader)
    val heif = HeifModule(hookInfo.heif, classLoader)
    val heifData = HeifDataModule(hookInfo.heifData, classLoader)
    val closeableReference = CloseableReferenceModule(hookInfo.closeableReference, classLoader)
    val storyServiceImpl = StoryServiceImplModule(hookInfo.storyServiceImpl, classLoader)

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
            const val EVENT_TEXTURE_AVAILABLE = 0
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

        fun enterCleanMode() = Method(
            configs.enterCleanMode.nameOrNull,
            configs.enterCleanMode.parameters.valuesListOrNull
        )

        fun setVisibility() = Method(
            configs.setVisibility.nameOrNull,
            configs.setVisibility.parameters.valuesListOrNull
        )
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

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        private val verbose
            get() = !ConfigManager.moduleConfig.verboseDisabled

        @Volatile
        lateinit var instance: DouyinPackage

        fun init(classLoader: ClassLoader, context: Context) {
            instance = DouyinPackage(classLoader, context)
        }

        private fun readHookInfo(context: Context): Configs.HookInfo {
            val hookInfoFileName = "douyinenhancer_hookInfo"

            runCatching {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (!(hookInfoFile.isFile && hookInfoFile.canRead())) {
                    YLog.warn("$TAG: hookInfoFile is not a file or can not be read")
                    return@runCatching null
                }

                val hostAppPackageInfo = context.packageManager.getPackageInfo(
                    AndroidAppHelper.currentPackageName(),
                    0
                )
                val hostAppLastUpdateTime = hostAppPackageInfo.lastUpdateTime
                val hostAppVersionCode = hostAppPackageInfo.versionCode

                val moduleLastUpdateTime = runCatching {
                    context.packageManager
                        .getPackageInfo(
                            AppProperties.PROJECT_APPLICATION_ID,
                            0
                        ).lastUpdateTime
                }.getOrDefault(hostAppLastUpdateTime)

                val hookInfo = FileInputStream(hookInfoFile).use {
                    runCatching {
                        Configs.HookInfo.parseFrom(it)
                    }.getOrNull() ?: Configs.HookInfo.newBuilder().build()
                }

                if (hookInfo.lastUpdateTime >= moduleLastUpdateTime &&
                    hookInfo.lastUpdateTime >= hostAppLastUpdateTime &&
                    hookInfo.hostVersionCode == hostAppVersionCode &&
                    hookInfo.moduleVersionCode == BuildConfig.VERSION_CODE &&
                    hookInfo.moduleVersionName == BuildConfig.VERSION_NAME
                ) {
                    return hookInfo
                } else {
                    YLog.debug("$TAG: hookInfo is outdated, will re-generate")
                }
            }.onFailure {
                YLog.error("$TAG: failed to read hookInfo: ", it)
            }

            return initHookInfo(context).also {
                val hookInfoFile = File(context.cacheDir, hookInfoFileName)
                if (hookInfoFile.exists()) {
                    hookInfoFile.delete()
                }
                FileOutputStream(hookInfoFile).use { o ->
                    it.writeTo(o)
                }
            }
        }

        private fun initHookInfo(context: Context) = hookInfo {
            val symbolNotFoundMsg = "%s: unable to populate %s config, possibly due to unfound obfuscated symbols"
            val populateFailedMsg = "%s: unable to populate config"

            val hostAppClassLoader = context.classLoader
            val hostAppPackageInfo = context.packageManager.getPackageInfo(
                AndroidAppHelper.currentPackageName(),
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
            hostVersionCode = hostAppPackageInfo.versionCode
            generation = 0

            runCatching {
                System.loadLibrary("dexkit")
            }.onFailure {
                YLog.error("failed to load DexKit native library", it)
                return@hookInfo
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
                        if (commentImageStructClassData == null || originUrlFieldData == null ||
                            downloadUrlFieldData == null || getDownloadUrlMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentImageStruct
                        }

                        class_ = class_ {
                            name = commentImageStructClassData.name
                        }
                        originUrl = field {
                            name = originUrlFieldData.name
                        }
                        downloadUrl = field {
                            name = downloadUrlFieldData.name
                        }
                        getDownloadUrl = method {
                            name = getDownloadUrlMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getDownloadUrlMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.comment.model.CommentAudioStruct"
                    }
                    content = field {
                        name = "content"
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
                                    saveImageMethodData?.let {
                                        readMethods {
                                            add {
                                                descriptor = it.descriptor
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        if (commentActionParamsClassData == null || commentFieldData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentActionParams
                        }

                        class_ = class_ {
                            name = commentActionParamsClassData.name
                        }
                        comment = field {
                            name = commentFieldData.name
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                                    this@hookInfo.commentActionParams.class_.nameOrNull?.let { commentActionParamsClassTypeName ->
                                        type = commentActionParamsClassTypeName
                                    }
                                }
                            }.singleOrNull()
                        }

                        if (commentLongPressItemModelClassData == null || commentActionParamsFieldData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentLongPressItemModel
                        }

                        class_ = class_ {
                            name = commentLongPressItemModelClassData.name
                        }
                        commentActionParams = field {
                            name = commentActionParamsFieldData.name
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                                                this@hookInfo.commentLongPressItemModel.commentActionParams.nameOrNull
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
                        if (saveImageActionItemClassData == null || isVisibleMethodData == null || onClickMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@saveImageActionItem
                        }

                        class_ = class_ {
                            name = saveImageActionItemClassData.name
                        }
                        isVisible = method {
                            name = isVisibleMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(isVisibleMethodData.paramTypeNames)
                            }
                        }
                        onClick = method {
                            name = onClickMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onClickMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (clsName == null || contextFieldName == null || certFieldName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@listenerProviderParam
                        }

                        class_ = class_ {
                            name = clsName
                        }
                        this.context = field {
                            name = contextFieldName
                        }
                        cert = field {
                            name = certFieldName
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                commentImageSaveDownloadListener = commentImageSaveDownloadListener {
                    runCatching {
                        val onSuccessedMethodData = bridge
                            .findMethod {
                                matcher {
                                    name = "onSuccessed"
                                    modifiers = Modifier.FINAL + Modifier.PUBLIC
                                    returnType = "void"
                                    params {
                                        add("com.ss.android.socialbase.downloader.model.DownloadInfo")
                                    }
                                    usingStrings {
                                        add("/douyin/comment")
                                        add("comment_")
                                    }
                                    invokeMethods {
                                        add {
                                            descriptor =
                                                "Lcom/bytedance/android/ug/UGFileUtilsKt;->copyFile(Ljava/lang/String;Ljava/lang/String;Lcom/bytedance/bpea/cert/token/TokenCert;)Z"
                                        }
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
                            type = this@hookInfo.listenerProviderParam.class_.nameOrNull
                        }?.self?.name
                        if (onSuccessedMethodData == null || notifyResultMethod == null || listenerProviderParamFieldName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentImageSaveDownloadListener
                        }

                        class_ = class_ {
                            name = clsName
                        }
                        onSuccessed = method {
                            name = onSuccessedMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onSuccessedMethodData.paramTypeNames)
                            }
                        }
                        notifyResult = method {
                            name = notifyResultMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                notifyResultMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        listenerProviderParam = field {
                            name = listenerProviderParamFieldName
                        }
                        return@commentImageSaveDownloadListener
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (md5HexFieldMethod == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@digestUtils
                        }

                        class_ = class_ {
                            name = digestUtilsClsName
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
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (contextFieldData == null || copyFileMethodData == null || getStorageDirMethodData == null ||
                            getExternalStorageDirectoryMethodData == null ||
                            getImageUriMethodData == null || createUriMethodData == null || getAudioUriMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@uGFileUtilsKt
                        }

                        class_ = class_ {
                            name = ugFileUtilsClassData.name
                        }
                        this.context = field {
                            name = contextFieldData.name
                        }
                        copyFile = method {
                            name = copyFileMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(copyFileMethodData.paramTypeNames)
                            }
                        }
                        getStorageDir = method {
                            name = getStorageDirMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getStorageDirMethodData.paramTypeNames)
                            }
                        }
                        getExternalStorageDir = method {
                            name = getExternalStorageDirectoryMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getExternalStorageDirectoryMethodData.paramTypeNames)
                            }
                        }
                        getImageUri = method {
                            name = getImageUriMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getImageUriMethodData.paramTypeNames)
                            }
                        }
                        createUri = method {
                            name = createUriMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(createUriMethodData.paramTypeNames)
                            }
                        }
                        getAudioUri = method {
                            name = getAudioUriMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getAudioUriMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (settingsScrollViewFieldName == null) {
                            YLog.error("$TAG: unable to populate config, settingsScrollViewFieldName is null")
                            return@runCatching
                        }

                        class_ = class_ {
                            name = dySettingsNewVersionActivityClsName
                        }
                        settingsScrollView = field {
                            name = settingsScrollViewFieldName
                        }
                        onResume = method {
                            name = "onResume"
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                }

                awemeStatistics = awemeStatistics {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.feed.model.AwemeStatistics"
                    }
                    collectCount = field {
                        name = "collectCount"
                    }
                    commentCount = field {
                        name = "commentCount"
                    }
                    diggCount = field {
                        name = "diggCount"
                    }
                    shareCount = field {
                        name = "shareCount"
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
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@downLoadExecutor
                        }

                        class_ = class_ {
                            name = executeMethodData.className
                        }
                        execute = method {
                            name = executeMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(executeMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                absTask = absTask {
                    runCatching {
                        val absTaskClassData = this@hookInfo.downLoadExecutor.execute.parameters.valuesListOrNull?.firstOrNull()?.let {
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

                        if (absTaskClassData == null || getTargetFilePathsMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@absTask
                        }

                        class_ = class_ {
                            name = absTaskClassData.name
                        }
                        getTargetFilePaths = method {
                            name = getTargetFilePathsMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getTargetFilePathsMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@downloadLivePhotoExecutor
                        }

                        class_ = class_ {
                            name = encodeLivePhotoMethodData.className
                        }
                        encodeLivePhoto = method {
                            name = encodeLivePhotoMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(encodeLivePhotoMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (videoClassData == null || hasSuffixWaterMarkFieldData == null ||
                            hasWaterMarkFieldData == null || downloadAddrFieldData == null ||
                            getPlayAddrMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@video
                        }

                        class_ = class_ {
                            name = videoClassData.name
                        }
                        getPlayAddr = method {
                            name = getPlayAddrMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getPlayAddrMethodData.paramTypeNames)
                            }
                        }
                        hasSuffixWaterMark = field {
                            name = hasSuffixWaterMarkFieldData.name
                        }
                        hasWaterMark = field {
                            name = hasWaterMarkFieldData.name
                        }
                        downloadAddr = field {
                            name = downloadAddrFieldData.name
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                feedResponseHandler = feedResponseHandler {
                    runCatching {
                        bridge.findMethod {
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
                        }.singleOrNull()?.also { match ->
                            class_ = class_ {
                                name = match.className
                            }
                            processAwemeList = method {
                                name = match.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(match.paramTypeNames)
                                }
                            }
                        } ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@feedResponseHandler
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                commentLongPressWhiteListProvider = commentLongPressWhiteListProvider {
                    runCatching {
                        val buildWhiteListMethodData = bridge.findMethod {
                            matcher {
                                modifiers = Modifier.PUBLIC or Modifier.STATIC
                                returnType = "java.util.Set"
                                params {
                                    this@hookInfo.commentActionParams.class_.nameOrNull?.let {
                                        add(it)
                                    }
                                }
                                usingStrings {
                                    add("custom")
                                    add("default")
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentLongPressWhiteListProvider
                        }

                        class_ = class_ {
                            name = buildWhiteListMethodData.className
                        }
                        buildWhiteList = method {
                            name = buildWhiteListMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(buildWhiteListMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@miscDownloadAddrUtil
                        }

                        class_ = class_ {
                            name = getSuffixSceneDownloadAddrMethodData.className
                        }
                        getSuffixSceneDownloadAddr = method {
                            name = getSuffixSceneDownloadAddrMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getSuffixSceneDownloadAddrMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (startDownloadMethodData == null || downloadActionClassData == null || awemeFieldData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@downloadAction
                        }

                        class_ = class_ {
                            name = downloadActionClassData.name
                        }
                        startDownload = method {
                            name = startDownloadMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(startDownloadMethodData.paramTypeNames)
                            }
                        }
                        aweme = field {
                            name = awemeFieldData.fieldName
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                    if (abTestServiceImplClassData == null || enableSaveImageToVideoLocalWaterMaskMethodData == null ||
                        enableVeAddLiveVideoWaterMarkMethodData == null
                    ) {
                        YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                        return@aBTestServiceImpl
                    }

                    class_ = class_ {
                        name = abTestServiceImplClassData.name
                    }
                    enableSaveImageToVideoLocalWaterMask = method {
                        name = enableSaveImageToVideoLocalWaterMaskMethodData.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.addAll(enableSaveImageToVideoLocalWaterMaskMethodData.paramTypeNames)
                        }
                    }
                    enableVeAddLiveVideoWaterMark = method {
                        name = enableVeAddLiveVideoWaterMarkMethodData.name
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.addAll(enableVeAddLiveVideoWaterMarkMethodData.paramTypeNames)
                        }
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
                        if (actionStatusClassData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@actionStatus
                        }

                        class_ = class_ {
                            name = actionStatusClassData.name
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
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                                    this@hookInfo.actionStatus.class_.nameOrNull?.let { actionStatusClassName ->
                                        this@hookInfo.actionStatus.normal.nameOrNull?.let { normalFieldName ->
                                            add {
                                                name = normalFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                        this@hookInfo.actionStatus.grayed.nameOrNull?.let { grayedFieldName ->
                                            add {
                                                name = grayedFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                        this@hookInfo.actionStatus.hidden.nameOrNull?.let { hiddenFieldName ->
                                            add {
                                                name = hiddenFieldName
                                                type = actionStatusClassName
                                            }
                                        }
                                    }
                                }
                            }
                        }.singleOrNull()
                        if (getActionCheckResultMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@absPermissionChecker
                        }

                        class_ = class_ {
                            name = getActionCheckResultMethodData.className
                        }
                        getActionCheckResult = method {
                            name = getActionCheckResultMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getActionCheckResultMethodData.paramTypeNames)
                            }
                        }

                        this@hookInfo.actionCheckResult = actionCheckResult {
                            runCatching {
                                val actionCheckResultClassData = getActionCheckResultMethodData.returnType
                                val actionStatusFieldData = actionCheckResultClassData?.let {
                                    bridge.findField {
                                        searchClasses = listOf(it)
                                        matcher {
                                            this@hookInfo.actionStatus.class_.nameOrNull?.let { actionStatusClassName ->
                                                type = actionStatusClassName
                                            }
                                        }
                                    }.singleOrNull()
                                }

                                if (actionCheckResultClassData == null || actionStatusFieldData == null) {
                                    YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                                    return@actionCheckResult
                                }

                                class_ = class_ {
                                    name = actionCheckResultClassData.name
                                }
                                actionStatus = field {
                                    name = actionStatusFieldData.name
                                }
                            }.onFailure {
                                YLog.error(populateFailedMsg.format(TAG), it)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass.simpleName))
                            return@galleryShareHelper
                        }

                        class_ = class_ {
                            name = startDownloadData.className
                        }
                        startDownload = method {
                            name = startDownloadData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(startDownloadData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (getDownloadStatusMethodData == null || videoResponseClassData == null ||
                            msgFieldData == null || statusFieldData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@sharePrivacyVideoApi
                        }

                        class_ = class_ {
                            name = getDownloadStatusMethodData.className
                        }
                        privacyVideoResponse = sharePrivacyVideoResponse {
                            class_ = class_ {
                                name = videoResponseClassData.name
                            }
                            msg = field {
                                name = msgFieldData.name
                            }
                            status = field {
                                name = statusFieldData.name
                            }
                        }
                        getDownloadStatus = method {
                            name = getDownloadStatusMethodData.name
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (acceptMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@listenAwemeFilter
                        }

                        class_ = class_ {
                            name = acceptMethodData.declaredClassName
                        }
                        accept = method {
                            name = acceptMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(acceptMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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

                        if (baseListFragmentPanelClassData == null || handleDoubleClickMethodData == null ||
                            handleVideoEventMethodData == null ||
                            getCurrentAwemeMethodData == null ||
                            onVideoPlayerEventMethodData == null ||
                            handleBigDiggViewClickMethodData == null ||
                            handlePauseMethodData == null
                        ) {
                            if (baseListFragmentPanelClassData == null) YLog.error("$TAG: BaseListFragmentPanel class not found")
                            if (handleDoubleClickMethodData == null) YLog.error("$TAG: handleDoubleClick not found")
                            if (handleVideoEventMethodData == null) YLog.error("$TAG: handleVideoEvent not found")
                            if (getCurrentAwemeMethodData == null) YLog.error("$TAG: getCurrentAweme not found")
                            if (onVideoPlayerEventMethodData == null) YLog.error("$TAG: onVideoPlayerEvent not found")
                            if (handleBigDiggViewClickMethodData == null) YLog.error("$TAG: handleBigDiggViewClick not found")
                            if (handlePauseMethodData == null) YLog.error("$TAG: handlePause not found")
                            return@baseListFragmentPanel
                        }

                        class_ = class_ {
                            name = baseListFragmentPanelClassData.name
                        }
                        handleDoubleClick = method {
                            name = handleDoubleClickMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(handleDoubleClickMethodData.paramTypeNames)
                            }
                        }
                        handleVideoEvent = method {
                            name = handleVideoEventMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(handleVideoEventMethodData.paramTypeNames)
                            }
                        }
                        getCurrentAweme = method {
                            name = getCurrentAwemeMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getCurrentAwemeMethodData.paramTypeNames)
                            }
                        }
                        onVideoPlayerEvent = method {
                            name = onVideoPlayerEventMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onVideoPlayerEventMethodData.paramTypeNames)
                            }
                        }
                        handleBigDiggViewClick = method {
                            name = handleBigDiggViewClickMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(handleBigDiggViewClickMethodData.paramTypeNames)
                            }
                        }
                        handlePause = method {
                            name = handlePauseMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(handlePauseMethodData.paramTypeNames)
                            }
                        }

                        this@hookInfo.videoPlayerStatus = videoPlayerStatus {
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
                                if (videoPlayerStatusClassData == null || codeFieldData == null) {
                                    YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                                    return@videoPlayerStatus
                                }

                                class_ = class_ {
                                    name = videoPlayerStatusClassData.name
                                }
                                code = field {
                                    name = codeFieldData.name
                                }
                            }.onFailure {
                                YLog.error(populateFailedMsg.format(TAG), it)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
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

                        if (videoEventClassData == null || videTypeFieldData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@videoEvent
                        }

                        class_ = class_ {
                            name = videoEventClassData.name
                        }
                        type = field {
                            name = videTypeFieldData.name
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                cleanModePresenter = cleanModePresenter {
                    runCatching {
                        val cleanModePresenterClassData = bridge.getClassData(
                            "com.ss.android.ugc.aweme.feed.plato.business.contentconsumption.cleanmode.CleanModePresenter"
                        )
                        val enterCleanModeMethodData = cleanModePresenterClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    // in some of the newer versions, the parameter order has changed
                                    paramCount = 5
                                    invokeMethods {
                                        add {
                                            descriptor =
                                                "Lcom/ss/android/ugc/aweme/feed/adapter/IFeedViewHolder;->blockCommonCleanModeEvent(Z)Z"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }
                        val setVisibilityMethodData = cleanModePresenterClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    returnType = "void"
                                    params {
                                        add("android.view.View")
                                        add("int")
                                    }
                                    invokeMethods {
                                        add {
                                            descriptor = "Landroid/view/View;->setVisibility(I)V"
                                        }
                                    }
                                }
                            }.singleOrNull()
                        }

                        if (cleanModePresenterClassData == null ||
                            enterCleanModeMethodData == null || setVisibilityMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@cleanModePresenter
                        }

                        class_ = class_ {
                            name = cleanModePresenterClassData.name
                        }
                        enterCleanMode = method {
                            name = enterCleanModeMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(enterCleanModeMethodData.paramTypeNames)
                            }
                        }
                        setVisibility = method {
                            name = setVisibilityMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(setVisibilityMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.flux.register.FluxComponentId"
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
                        if (fluxComponentDataActionClassData == null || getSetMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@fluxComponentDataAction
                        }

                        class_ = class_ {
                            name = fluxComponentDataActionClassData.name
                        }
                        getSet = method {
                            name = getSetMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getSetMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (heifClassData == null || toRgbaMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@heif
                        }

                        class_ = class_ {
                            name = heifClassData.name
                        }
                        toRgba = method {
                            name = toRgbaMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(toRgbaMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (heifDataClassData == null || newBitmapMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@heifData
                        }

                        class_ = class_ {
                            name = heifDataClassData.name
                        }
                        newBitmap = method {
                            name = newBitmapMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(newBitmapMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (closeableReferenceClassData == null || getMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@closeableReference
                        }

                        class_ = class_ {
                            name = closeableReferenceClassData.name
                        }
                        get = method {
                            name = getMethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
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
                        if (storyServiceImplClassData == null || singleLivePhotoToMp4MethodData == null ||
                            convertImgToMp4MethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@storyServiceImpl
                        }

                        class_ = class_ {
                            name = storyServiceImplClassData.name
                        }
                        convertSingleLivePhotoToMp4UseMusicUrl = method {
                            name = singleLivePhotoToMp4MethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(singleLivePhotoToMp4MethodData.paramTypeNames)
                            }
                        }

                        convertImgToMp4 = method {
                            name = convertImgToMp4MethodData.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(convertImgToMp4MethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }
            }
        }
    }
}
