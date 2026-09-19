package io.github.twyora.douyinenhancer.hook.ui

import com.highcapable.yukihookapi.hook.core.YukiMemberHookCreator
import com.highcapable.yukihookapi.hook.entity.YukiBaseHooker
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.hook.DouyinPackage
import io.github.twyora.douyinenhancer.hook.HookOnMainProcess
import io.github.twyora.douyinenhancer.utils.getStaticField
import io.github.twyora.douyinenhancer.utils.resolveMethod

@HookOnMainProcess
object FeedComponentHooker : YukiBaseHooker() {
    private val TAG = this::class.simpleName

    private val packageInstance
        get() = DouyinPackage.instance

    private val verbose
        get() = !ConfigManager.module.verboseDisabled.value

    private val blockComponentIds by lazy {
        val config = ConfigManager.playbackComponentBlock
        listOf(
            config.musicCoverBlock.value to packageInstance.fluxComponentId.musicCoverBlock(),
            config.musicListenCover.value to packageInstance.fluxComponentId.musicListenCover(),
            config.digg.value to packageInstance.fluxComponentId.digg(),
            config.title.value to packageInstance.fluxComponentId.title(),
            config.musicCover.value to packageInstance.fluxComponentId.musicCover(),
            config.generalLabel.value to packageInstance.fluxComponentId.generalLabel(),
            config.feedLabelContainer.value to packageInstance.fluxComponentId.feedLabelContainer(),
            config.musicTitle.value to packageInstance.fluxComponentId.musicTitle(),
            config.story25DiverseDigg.value to packageInstance.fluxComponentId.story25DiverseDigg(),
            config.ecomStore.value to packageInstance.fluxComponentId.ecomStore(),
            config.buttonImQuickShare.value to packageInstance.fluxComponentId.buttonImQuickShare(),
            config.buttonFeedImShareGuideV2.value to packageInstance.fluxComponentId.buttonFeedImShareGuideV2(),
            config.buttonForceFeedImShareGuide.value to packageInstance.fluxComponentId.buttonForceFeedImShareGuide(),
            config.socialNewCommentGuideBubble.value to packageInstance.fluxComponentId.socialNewCommentGuideBubble(),
            config.commentBottomAnimation.value to packageInstance.fluxComponentId.commentBottomAnimation(),
            config.nearbyIdentityTag.value to packageInstance.fluxComponentId.nearbyIdentityTag(),
            config.livePhotoTag.value to packageInstance.fluxComponentId.livePhotoTag(),
            config.photosTag.value to packageInstance.fluxComponentId.photosTag(),
            config.story24Tag.value to packageInstance.fluxComponentId.story24Tag(),
            config.socialNewStyleStoryTag.value to packageInstance.fluxComponentId.socialNewStyleStoryTag(),
            config.longVideoHighlightTag.value to packageInstance.fluxComponentId.longVideoHighlightTag(),
            config.danmakuVertical.value to packageInstance.fluxComponentId.danmakuVertical(),
            config.avatar.value to packageInstance.fluxComponentId.avatar(),
            config.nickname.value to packageInstance.fluxComponentId.nickname(),
            config.postTime.value to packageInstance.fluxComponentId.postTime(),
            config.bellowDescTime.value to packageInstance.fluxComponentId.bellowDescTime(),
            config.comment.value to packageInstance.fluxComponentId.comment(),
            config.reply.value to packageInstance.fluxComponentId.reply(),
            config.share.value to packageInstance.fluxComponentId.share(),
            config.collect.value to packageInstance.fluxComponentId.collect(),
            config.anchorFramework.value to packageInstance.fluxComponentId.anchorFramework(),
            config.bottomBarCommon.value to packageInstance.fluxComponentId.bottomBarCommon(),
            config.commonButton.value to packageInstance.fluxComponentId.commonButton(),
            config.sticker.value to packageInstance.fluxComponentId.sticker(),
            config.aiSearch.value to packageInstance.fluxComponentId.aiSearch(),
            config.c2Feed.value to packageInstance.fluxComponentId.c2Feed(),
            config.flow.value to packageInstance.fluxComponentId.flow(),
            config.nearbyHotComment.value to packageInstance.fluxComponentId.nearbyHotComment(),
            config.buttonUnfollowFamiliar.value to packageInstance.fluxComponentId.buttonUnfollowFamiliar(),
            config.buttonUnfollowFamiliarRec.value to packageInstance.fluxComponentId.buttonUnfollowFamiliarRec(),
            config.coCreatorAuthor.value to packageInstance.fluxComponentId.coCreatorAuthor(),
            config.chapterTag.value to packageInstance.fluxComponentId.chapterTag(),
            config.ecomTagFriend.value to packageInstance.fluxComponentId.ecomTagFriend(),
            config.socialNewStylePostTimeBottom.value to packageInstance.fluxComponentId.socialNewStylePostTimeBottom(),
            config.socialNewStyleMusicBelow.value to packageInstance.fluxComponentId.socialNewStyleMusicBelow(),
            config.chapterDetail.value to packageInstance.fluxComponentId.chapterDetail(),
            config.titleTagContainer.value to packageInstance.fluxComponentId.titleTagContainer(),
            config.rightMenuLl.value to packageInstance.fluxComponentId.rightMenuLl(),
            config.musicMuteCover.value to packageInstance.fluxComponentId.musicMuteCover(),
            config.jxLeftBottomLongVideoPlusTitleTag.value to packageInstance.fluxComponentId.jxLeftBottomLongVideoPlusTitleTag(),
            config.bottomBarMix.value to packageInstance.fluxComponentId.bottomBarMix(),
            config.bottomBarNormalSearch.value to packageInstance.fluxComponentId.bottomBarNormalSearch(),
            config.bottomBarCommonPrioritySearch.value to packageInstance.fluxComponentId.bottomBarCommonPrioritySearch(),
            config.jxPick.value to packageInstance.fluxComponentId.jxPick(),
            config.bottomBarContainer.value to packageInstance.fluxComponentId.bottomBarContainer(),
            config.aiCoCreatorsThree.value to packageInstance.fluxComponentId.aiCoCreatorsThree(),
            config.aigcCocreateStatusTitle.value to packageInstance.fluxComponentId.aigcCocreateStatusTitle()
        ).filter {
            it.first
        }.map {
            it.second
        }.toSet()
    }

    override fun onHook() {
        if (ConfigManager.playbackComponentBlock.mainSwitch.value) {
            if (verbose) {
                YLog.debug("$TAG: playback component block is disabled, skipping hook")
            }
            return
        }
        installPlaybackComponentBlockHook()
    }

    private fun installPlaybackComponentBlockHook(): YukiMemberHookCreator.MemberHookCreator.Result? {
        return packageInstance.fluxComponentDataAction.selfClass?.resolveMethod(
            packageInstance.fluxComponentDataAction.getSet()
        )?.hook {
            after {
                val allowComponentSet = result as? MutableSet<*> ?: run {
                    YLog.error("$TAG: ${result?.javaClass?.name} is not a mutable set")
                    return@after
                }
                allowComponentSet.removeAll(
                    blockComponentIds.mapNotNull { field ->
                        packageInstance.fluxComponentId.selfClass?.getStaticField(field)
                    }.toSet()
                )
            }
        }?.result {
            onConductFailure { _, throwable ->
                YLog.error("$TAG: failed to block playback components", throwable)
            }
            onHookingFailure { throwable ->
                YLog.error("$TAG: failed to hook for blocking playback components", throwable)
            }
        }
    }
}
