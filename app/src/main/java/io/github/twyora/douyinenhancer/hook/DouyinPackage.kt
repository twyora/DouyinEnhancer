/*
 * Referenced from [BiliRoaming](https://github.com/yujincheng08/BiliRoaming/blob/master/app/src/main/java/me/iacn/biliroaming/BiliBiliPackage.kt)
 */

package io.github.twyora.douyinenhancer.hook

import android.app.AndroidAppHelper
import android.content.Context
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.condition.matcher.extension.parameterizedBy
import com.highcapable.kavaref.condition.matcher.extension.toTypeMatcher
import com.highcapable.kavaref.condition.type.Modifiers
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.ModuleKey
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
    val heifDecoder = HeifDecoderModule(hookInfo.heifDecoder, classLoader)
    val heifBitmapFactoryImpl = HeifBitmapFactoryImplModule(hookInfo.heifBitmapFactoryImpl, classLoader)
    val downLoadExecutor = DownLoadExecutorModule(hookInfo.downLoadExecutor, classLoader)
    val downLoadTask = DownLoadTaskModule(hookInfo.downLoadTask, classLoader)
    val downloadLivePhotoExecutor = DownloadLivePhotoExecutorModule(hookInfo.downloadLivePhotoExecutor, classLoader)
    val singleImageToMp4Composer = SingleImageToMp4ComposerModule(hookInfo.singleImageToMp4Composer, classLoader)
    val multiImageToMp4Composer = MultiImageToMp4ComposerModule(hookInfo.multiImageToMp4Composer, classLoader)
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
    val videoPlayerEvent = VideoPlayerEventModule(hookInfo.videoPlayerEvent, classLoader)
    val videoEvent = VideoEventModule(hookInfo.videoEvent, classLoader)
    val cleanModePresenter = CleanModePresenterModule(hookInfo.cleanModePresenter, classLoader)
    val danmakuView = DanmakuViewModule(hookInfo.danmakuView, classLoader)
    val fluxComponentId = FluxComponentIdModule(hookInfo.fluxComponentId, classLoader)
    val fluxComponentDataAction = FluxComponentDataActionModule(hookInfo.fluxComponentDataAction, classLoader)

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

        fun imageIndex() = Field(configs.imageIndex.nameOrNull)
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

        fun commentActionParams() = Field(configs.cmtActionParams.nameOrNull)

        fun saveImageActionParams() = Field(configs.saveImgActionParams.nameOrNull)

        class OnClickExecutorModule internal constructor(
            private val configs: Configs.SaveImageActionItemOnClickExecutor,
            private val classLoader: ClassLoader
        ) {
            val selfClass by weak {
                configs.class_.nameOrNull?.toClass(classLoader)
            }

            fun onClick() = Method(
                configs.onClick.nameOrNull,
                configs.onClick.parameters.valuesListOrNull
            )

            fun hostItem() = Field(configs.hostItem.nameOrNull)
        }

        val onClickExecutor = OnClickExecutorModule(configs.onClickExecutor, classLoader)

        fun isVisible() = Method(
            configs.isVisible.nameOrNull,
            configs.isVisible.parameters.valuesListOrNull
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

        fun pauseCurrentPlayerWithListener() = Method(
            configs.pauseCurrentPlayerWithListener.nameOrNull,
            configs.pauseCurrentPlayerWithListener.parameters.valuesListOrNull
        )

        fun showIvWhenPause() = Method(
            configs.showIvWhenPause.nameOrNull,
            configs.showIvWhenPause.parameters.valuesListOrNull
        )

        fun onVideoPlayerEvent() = Method(
            configs.onVideoPlayerEvent.nameOrNull,
            configs.onVideoPlayerEvent.parameters.valuesListOrNull
        )
    }

    class VideoPlayerEventModule internal constructor(private val configs: Configs.VideoPlayerEvent, private val classLoader: ClassLoader) {
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

        fun videoType() = Field(configs.videoType.nameOrNull)

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

    class HeifDecoderModule internal constructor(private val configs: Configs.HeifDecoder, private val classLoader: ClassLoader) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun sBitmapFactory() = Field(configs.sBitmapFactory.nameOrNull)
    }

    class HeifBitmapFactoryImplModule internal constructor(
        private val configs: Configs.HeifBitmapFactoryImpl,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun decodeByteArray() = Method(
            configs.decodeByteArray.nameOrNull,
            configs.decodeByteArray.parameters.valuesListOrNull
        )
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

    class DownLoadTaskModule internal constructor(private val configs: Configs.DownLoadTask, private val classLoader: ClassLoader) {
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

    class SingleImageToMp4ComposerModule internal constructor(
        private val configs: Configs.SingleImageToMp4Composer,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun onLoad() = Method(
            configs.onLoad.nameOrNull,
            configs.onLoad.parameters.valuesListOrNull
        )
    }

    class MultiImageToMp4ComposerModule internal constructor(
        private val configs: Configs.MultiImageToMp4Composer,
        private val classLoader: ClassLoader
    ) {
        val selfClass by weak {
            configs.class_.nameOrNull?.toClass(classLoader)
        }

        fun onLoad() = Method(
            configs.onLoad.nameOrNull,
            configs.onLoad.parameters.valuesListOrNull
        )

        fun imagePathList() = Field(
            configs.imagePathList.nameOrNull
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

    companion object {
        private val TAG = DouyinPackage::class.simpleName

        private val verbose
            get() = !FastKVConfigManager.module.getBoolean(ModuleKey.DISABLE_VERBOSE_LOGS, false)

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
                        val cmtImgClsName = "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                        val originUrlFieldName = "originUrl"
                        val downloadUrlFieldName = "downloadUrl"
                        val getDownloadUrlMethodData = bridge.findMethod {
                            matcher {
                                declaredClass = "com.ss.android.ugc.aweme.comment.model.CommentImageStruct"
                                returnType = "com.ss.android.ugc.aweme.base.model.UrlModel"
                                paramCount = 0
                                addUsingField {
                                    name = "downloadUrl"
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentImageStruct
                        }

                        class_ = class_ {
                            name = cmtImgClsName
                        }
                        originUrl = field {
                            name = originUrlFieldName
                        }
                        downloadUrl = field {
                            name = downloadUrlFieldName
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
                        val cmtActionParamsClsName = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                        val commentFieldName = cmtActionParamsClsName
                            .toClass(hostAppClassLoader)
                            .resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.model.Comment"
                            }?.self
                            ?.name
                        val imageFieldName = cmtActionParamsClsName
                            .toClass(hostAppClassLoader)
                            .resolve()
                            .firstFieldOrNull {
                                type = Int::class
                            }?.self
                            ?.name
                        if (commentFieldName == null || imageFieldName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentActionParams
                        }

                        class_ = class_ {
                            name = cmtActionParamsClsName
                        }
                        comment = field {
                            name = commentFieldName
                        }
                        imageIndex = field {
                            name = imageFieldName
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                commentLongPressItemModel = commentLongPressItemModel {
                    runCatching {
                        val commentLongPressItemModelClsName = "com.ss.android.ugc.aweme.comment.ui.longpress.CommentLongPressItemModel"
                        val commentActionParamsFieldName = commentLongPressItemModelClsName
                            .toClass(hostAppClassLoader)
                            .resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            }?.self
                            ?.name

                        if (commentActionParamsFieldName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@commentLongPressItemModel
                        }

                        class_ = class_ {
                            name = commentLongPressItemModelClsName
                        }
                        commentActionParams = field {
                            name = commentActionParamsFieldName
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                saveImageActionItem = saveImageActionItem {
                    runCatching {
                        val saveImageActionItemClsName =
                            "com.ss.android.ugc.aweme.comment.manager.longclickaction.actions.SaveImageActionItem"
                        // SaveImageActionItem extends CommentLongPressItemModel, ensure commentLongPressItemModel is populated first!
                        val cmtActionParamsFieldName = this@hookInfo.commentLongPressItemModel.commentActionParams?.name
                        val saveImageActionParamsFieldName = saveImageActionItemClsName
                            .toClass(hostAppClassLoader)
                            .resolve()
                            .firstFieldOrNull {
                                type = "com.ss.android.ugc.aweme.comment.CommentActionParams"
                            }?.self
                            ?.name
                        val onClickMethodData = bridge
                            .findMethod {
                                matcher {
                                    modifiers = Modifier.STATIC + Modifier.FINAL + Modifier.PUBLIC
                                    returnType = "java.lang.Object"
                                    params {
                                        count = 1
                                    }
                                    addUsingString("bpea-comment_save_image_to_album")
                                }
                            }.singleOrNull()
                        val onClickHostItemFieldName =
                            onClickMethodData?.declaredClassName?.toClass(hostAppClassLoader)?.resolve()?.firstFieldOrNull {
                                type = Object::class
                            }?.self?.name
                        val isVisibleMethodData = bridge
                            .findMethod {
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    declaredClass = saveImageActionItemClsName
                                    returnType = "boolean"
                                    usingFields {
                                        add {
                                            field {
                                                cmtActionParamsFieldName?.let {
                                                    name = it
                                                }
                                            }
                                        }
                                    }
                                }
                            }.singleOrNull()
                        if (cmtActionParamsFieldName == null || saveImageActionParamsFieldName == null || onClickMethodData == null ||
                            onClickHostItemFieldName == null ||
                            isVisibleMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@saveImageActionItem
                        }

                        class_ = class_ {
                            name = saveImageActionItemClsName
                        }
                        cmtActionParams = field {
                            name = cmtActionParamsFieldName
                        }
                        saveImgActionParams = field {
                            name = saveImageActionParamsFieldName
                        }
                        onClickExecutor = saveImageActionItemOnClickExecutor {
                            class_ = class_ {
                                name = onClickMethodData.className
                            }
                            onClick = method {
                                name = onClickMethodData.methodName
                                parameters = MethodKt.parameters {
                                    values.clear()
                                    values.addAll(onClickMethodData.paramTypeNames)
                                }
                            }
                            hostItem = field {
                                name = onClickHostItemFieldName
                            }
                        }
                        isVisible = method {
                            name = isVisibleMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(isVisibleMethodData.paramTypeNames)
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
                        val ugFileUtilsClsName = "com.bytedance.android.ug.UGFileUtilsKt"
                        val copyFileMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "copyFile"
                            returnType = Boolean::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(
                                String::class,
                                String::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                            parameterCount = 3
                        }?.self
                        val getStorageDirMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getStorageDir"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(String::class, Boolean::class)
                            parameterCount = 2
                        }?.self
                        val getExternalStorageDirectoryMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getExternalStorageDirectory"
                            returnType = String::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(String::class, Boolean::class)
                            parameterCount = 2
                        }?.self
                        val getImageUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getImageUri"
                            returnType = android.net.Uri::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(
                                Context::class,
                                String::class,
                                String::class,
                                String::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                            parameterCount = 5
                        }?.self
                        val createUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "createUri"
                            returnType = android.net.Uri::class
                            modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                            parameters(
                                String::class,
                                Boolean::class,
                                Array<android.net.Uri>::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                            parameterCount = 4
                        }?.self
                        val getAudioUriMethod = ugFileUtilsClsName.toClass(hostAppClassLoader).resolve().firstMethodOrNull {
                            name = "getAudioUri"
                            parameters(
                                Context::class,
                                String::class,
                                String::class,
                                String::class,
                                "com.bytedance.bpea.cert.token.TokenCert"
                            )
                        }?.self
                        if (copyFileMethod == null || getStorageDirMethod == null || getExternalStorageDirectoryMethod == null ||
                            getImageUriMethod == null || createUriMethod == null || getAudioUriMethod == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@uGFileUtilsKt
                        }

                        class_ = class_ {
                            name = ugFileUtilsClsName
                        }
                        this.context = field {
                            name = "context"
                        }
                        copyFile = method {
                            name = copyFileMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                copyFileMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        getStorageDir = method {
                            name = getStorageDirMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                getStorageDirMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        getExternalStorageDir = method {
                            name = getExternalStorageDirectoryMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                getExternalStorageDirectoryMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        getImageUri = method {
                            name = getImageUriMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                getImageUriMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        createUri = method {
                            name = createUriMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                createUriMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
                            }
                        }
                        getAudioUri = method {
                            name = getAudioUriMethod.name
                            parameters = MethodKt.parameters {
                                values.clear()
                                getAudioUriMethod.parameterTypes.forEach { paramType ->
                                    values.add(paramType.name)
                                }
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

                heifDecoder = heifDecoder {
                    class_ = class_ {
                        name = "com.bytedance.fresco.heif.HeifDecoder"
                    }
                    sBitmapFactory = field {
                        name = "sBitmapFactory"
                    }
                }

                heifBitmapFactoryImpl = heifBitmapFactoryImpl {
                    class_ = class_ {
                        name = "com.bytedance.fresco.heif.HeifBitmapFactoryImpl"
                    }
                    decodeByteArray = method {
                        name = "decodeByteArray"
                        parameters = MethodKt.parameters {
                            values.clear()
                            values.addAll(
                                listOf(
                                    "[B",
                                    "int",
                                    "int",
                                    $$"android.graphics.BitmapFactory$Options"
                                )
                            )
                        }
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
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/bytedance/android/ug/UGFileUtilsKt;->getExternalStorageDirectory(Ljava/lang/String;Z)Ljava/lang/String;"
                                    }
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

                downLoadTask = downLoadTask {
                    runCatching {
                        val downloadTaskClassName = this@hookInfo.downLoadExecutor.execute.parameters.valuesListOrNull?.firstOrNull()
                            ?: run {
                                YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                                return@downLoadTask
                            }
                        val getTargetFilePathsMethodData = bridge.findMethod {
                            matcher {
                                declaredClass = downloadTaskClassName
                                returnType = "java.util.List"
                            }
                        }.singleOrNull()

                        if (getTargetFilePathsMethodData == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@downLoadTask
                        }

                        class_ = class_ {
                            name = downloadTaskClassName
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

                singleImageToMp4Composer = singleImageToMp4Composer {
                    runCatching {
                        val onLoadMethodData = bridge.findMethod {
                            matcher {
                                name = "onLoad"
                                usingStrings {
                                    add("[onLoad] failed, cause path not exist")
                                }
                                invokeMethods {
                                    add {
                                        descriptor =
                                            $$"Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService;->convertImgToMp4(Landroid/content/Context;Landroidx/lifecycle/LifecycleOwner;Ljava/lang/String;Ljava/lang/String;ZJLjava/lang/String;Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService$OnMuxImgToMp4Callback;)V"
                                    }
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@singleImageToMp4Composer
                        }

                        class_ = class_ {
                            name = onLoadMethodData.className
                        }
                        onLoad = method {
                            name = onLoadMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onLoadMethodData.paramTypeNames)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                multiImageToMp4Composer = multiImageToMp4Composer {
                    runCatching {
                        val onLoadMethodData = bridge.findMethod {
                            matcher {
                                name = "onLoad"
                                usingStrings {
                                    add("images file not exist!")
                                }
                                invokeMethods {
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService;->convertImgListToMp4UseMusicUrl(Landroid/app/Activity;Landroidx/lifecycle/LifecycleOwner;Ljava/util/List;Lcom/ss/android/ugc/aweme/music/model/Music;ZZLjava/lang/String;ZLkotlin/jvm/functions/Function1;)V"
                                    }
                                    add {
                                        descriptor =
                                            "Lcom/ss/android/ugc/aweme/services/external/ui/IStoryService;->convertSlidesListToMp4UseMusicUrl(Landroid/app/Activity;Landroidx/lifecycle/LifecycleOwner;Ljava/util/List;Lcom/ss/android/ugc/aweme/music/model/Music;ZZLjava/lang/String;ZLkotlin/jvm/functions/Function1;)V"
                                    }
                                }
                            }
                        }.singleOrNull()

                        val imagePathListFieldName = onLoadMethodData?.className
                            ?.toClass(hostAppClassLoader)
                            ?.resolve()
                            ?.firstFieldOrNull {
                                genericType = List::class.parameterizedBy(
                                    List::class.parameterizedBy(
                                        String::class.toTypeMatcher()
                                    )
                                )
                            }?.self?.name

                        if (onLoadMethodData == null || imagePathListFieldName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@multiImageToMp4Composer
                        }

                        class_ = class_ {
                            name = onLoadMethodData.className
                        }
                        onLoad = method {
                            name = onLoadMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onLoadMethodData.paramTypeNames)
                            }
                        }
                        imagePathList = field {
                            name = imagePathListFieldName
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                video = video {
                    runCatching {
                        val getPlayAddrMethodData = bridge.findMethod {
                            matcher {
                                declaredClass = "com.ss.android.ugc.aweme.feed.model.Video"
                                returnType = "com.ss.android.ugc.aweme.feed.model.VideoUrlModel"
                                usingFields {
                                    add {
                                        name = "_playAddr"
                                    }
                                    add {
                                        name = "_playAddrH265"
                                    }
                                }
                            }
                        }.singleOrNull { methodData ->
                            methodData.usingFields.none {
                                it.field.fieldName == "ratio"
                            }
                        } ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@video
                        }

                        class_ = class_ {
                            name = getPlayAddrMethodData.className
                        }
                        getPlayAddr = method {
                            name = getPlayAddrMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getPlayAddrMethodData.paramTypeNames)
                            }
                        }
                        hasSuffixWaterMark = field {
                            name = "hasSuffixWaterMark"
                        }
                        hasWaterMark = field {
                            name = "hasWaterMark"
                        }
                        downloadAddr = field {
                            name = "downloadAddr"
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
                                    add("com.ss.android.ugc.aweme.comment.CommentActionParams")
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
                        val downloadActionClassData = bridge.findClass {
                            matcher {
                                className("DownloadAction", StringMatchType.EndsWith)
                            }
                        }.singleOrNull { classData ->
                            classData.simpleName == "DownloadAction"
                        }
                        val startDownloadMethodData = downloadActionClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    modifiers = Modifier.PUBLIC or Modifier.FINAL
                                    paramTypes("com.ss.android.ugc.aweme.sharer.ui.SharePackage")
                                    addUsingString("downloadImage")
                                }
                            }.singleOrNull()
                        }
                        val awemeFieldData = downloadActionClassData?.let {
                            bridge.findField {
                                searchClasses = listOf(it)
                                matcher {
                                    type = "com.ss.android.ugc.aweme.feed.model.Aweme"
                                }
                            }.singleOrNull()
                        }

                        if (downloadActionClassData == null || startDownloadMethodData == null || awemeFieldData == null) {
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
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.servicimpl.ABTestServiceImpl"
                    }
                    enableSaveImageToVideoLocalWaterMask = method {
                        name = "enableSaveImageToVideoLocalWaterMask"
                    }
                    enableVeAddLiveVideoWaterMark = method {
                        name = "enableVEAddLiveVideoWaterMark"
                    }
                }

                absPermissionChecker = absPermissionChecker {
                    runCatching {
                        val clsName = "com.ss.android.ugc.aweme.permission.AbsPermissionChecker"
                        val getActionCheckResultData = bridge.findMethod {
                            matcher {
                                declaredClass = clsName
                                modifiers = Modifier.PUBLIC or Modifier.FINAL
                                addUsingField {
                                    descriptor =
                                        "Lcom/ss/android/ugc/aweme/privacy/model/ActionStatus;->NORMAL:Lcom/ss/android/ugc/aweme/privacy/model/ActionStatus;"
                                }
                            }
                        }.singleOrNull() ?: run {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@absPermissionChecker
                        }

                        class_ = class_ {
                            name = clsName
                        }
                        getActionCheckResult = method {
                            name = getActionCheckResultData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(getActionCheckResultData.paramTypeNames)
                            }
                        }

                        this@hookInfo.actionCheckResult = actionCheckResult {
                            runCatching {
                                val actionCheckResultClsName = getActionCheckResultData.returnType!!.name
                                val actionCheckResultActionStatusFieldName =
                                    actionCheckResultClsName.toClass(hostAppClassLoader).resolve().firstFieldOrNull {
                                        type = "com.ss.android.ugc.aweme.privacy.model.ActionStatus"
                                    }?.self?.name ?: run {
                                        YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                                        return@actionCheckResult
                                    }

                                class_ = class_ {
                                    name = actionCheckResultClsName
                                }
                                actionStatus = field {
                                    name = actionCheckResultActionStatusFieldName
                                }
                            }.onFailure {
                                YLog.error(populateFailedMsg.format(TAG), it)
                            }
                        }
                    }.onFailure {
                        YLog.error(populateFailedMsg.format(TAG), it)
                    }
                }

                actionStatus = actionStatus {
                    class_ = class_ {
                        name = "com.ss.android.ugc.aweme.privacy.model.ActionStatus"
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
                                            "Lcom/ss/android/common/util/NetworkUtils;->isNetworkAvailable(Landroid/content/Context;)Z"
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
                        val getDownloadStatusMethodName =
                            "com.ss.android.ugc.aweme.feed.share.video.SharePrivacyVideoApi".toClass(hostAppClassLoader).resolve()
                                .firstMethodOrNull {
                                    modifiers(Modifiers.PUBLIC, Modifiers.STATIC, Modifiers.FINAL)
                                    returnType = "io.reactivex.Observable"
                                }?.self?.name
                        if (getDownloadStatusMethodName == null) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                            return@sharePrivacyVideoApi
                        }

                        class_ = class_ {
                            name = "com.ss.android.ugc.aweme.feed.share.video.SharePrivacyVideoApi"
                        }
                        privacyVideoResponse = sharePrivacyVideoResponse {
                            class_ = class_ {
                                name = $$"com.ss.android.ugc.aweme.feed.share.video.SharePrivacyVideoApi$PrivacyVideoResponse"
                            }
                            msg = field {
                                name = "msg"
                            }
                            status = field {
                                name = "status"
                            }
                        }
                        getDownloadStatus = method {
                            name = getDownloadStatusMethodName
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
                                    name = "handleDoubleClick"
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
                                    name = "handleVideoEvent"
                                    paramCount = 1
                                    returnType = "void"
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
                        val pauseCurrentPlayerWithListenerMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "pauseCurrentPlayerWithListener"
                                    paramCount = 0
                                    returnType = "void"
                                }
                            }.singleOrNull()
                        }
                        val showIvWhenPauseMethodData = baseListFragmentPanelClassData?.let {
                            bridge.findMethod {
                                searchClasses = listOf(it)
                                matcher {
                                    name = "showIvWhenPause"
                                    paramCount = 0
                                    returnType = "void"
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

                        if (baseListFragmentPanelClassData == null || handleDoubleClickMethodData == null ||
                            handleVideoEventMethodData == null ||
                            getCurrentAwemeMethodData == null ||
                            pauseCurrentPlayerWithListenerMethodData == null ||
                            showIvWhenPauseMethodData == null ||
                            onVideoPlayerEventMethodData == null
                        ) {
                            YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
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
                        pauseCurrentPlayerWithListener = method {
                            name = pauseCurrentPlayerWithListenerMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(pauseCurrentPlayerWithListenerMethodData.paramTypeNames)
                            }
                        }
                        showIvWhenPause = method {
                            name = showIvWhenPauseMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(showIvWhenPauseMethodData.paramTypeNames)
                            }
                        }
                        onVideoPlayerEvent = method {
                            name = onVideoPlayerEventMethodData.methodName
                            parameters = MethodKt.parameters {
                                values.clear()
                                values.addAll(onVideoPlayerEventMethodData.paramTypeNames)
                            }
                        }

                        this@hookInfo.videoPlayerEvent = videoPlayerEvent {
                            runCatching {
                                val videoPlayerEventCodeFieldData = bridge.findField {
                                    searchInClass(onVideoPlayerEventMethodData.paramTypes)
                                    matcher {
                                        modifiers = Modifier.PUBLIC or Modifier.FINAL
                                        readMethods {
                                            add {
                                                descriptor = onVideoPlayerEventMethodData.descriptor
                                            }
                                        }
                                    }
                                }.singleOrNull()
                                if (videoPlayerEventCodeFieldData == null) {
                                    YLog.error(symbolNotFoundMsg.format(TAG, this::class.java.enclosingClass?.simpleName))
                                    return@videoPlayerEvent
                                }

                                class_ = class_ {
                                    name = videoPlayerEventCodeFieldData.declaredClassName
                                }
                                code = field {
                                    name = videoPlayerEventCodeFieldData.name
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
                        videoType = field {
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
                                    params {
                                        add("int")
                                        add("int")
                                        add("java.lang.String")
                                        add("boolean")
                                        add("java.util.List")
                                    }
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
            }
        }
    }
}
