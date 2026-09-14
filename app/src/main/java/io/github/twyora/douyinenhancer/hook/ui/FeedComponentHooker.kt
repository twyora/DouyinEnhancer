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
        get() = !ConfigManager.moduleConfig.verboseDisabled

    private val blockComponentIds by lazy {
        val config = ConfigManager.playbackComponentBlockConfig
        listOf(
            config.musicCoverBlock to packageInstance.fluxComponentId.musicCoverBlock(),
            config.musicListenCover to packageInstance.fluxComponentId.musicListenCover(),
            config.digg to packageInstance.fluxComponentId.digg(),
            config.title to packageInstance.fluxComponentId.title(),
            config.musicCover to packageInstance.fluxComponentId.musicCover(),
            config.generalLabel to packageInstance.fluxComponentId.generalLabel(),
            config.feedLabelContainer to packageInstance.fluxComponentId.feedLabelContainer(),
            config.musicTitle to packageInstance.fluxComponentId.musicTitle(),
            config.story25DiverseDigg to packageInstance.fluxComponentId.story25DiverseDigg(),
            config.ecomStore to packageInstance.fluxComponentId.ecomStore(),
            config.buttonImQuickShare to packageInstance.fluxComponentId.buttonImQuickShare(),
            config.buttonFeedImShareGuideV2 to packageInstance.fluxComponentId.buttonFeedImShareGuideV2(),
            config.buttonForceFeedImShareGuide to packageInstance.fluxComponentId.buttonForceFeedImShareGuide(),
            config.socialNewCommentGuideBubble to packageInstance.fluxComponentId.socialNewCommentGuideBubble(),
            config.commentBottomAnimation to packageInstance.fluxComponentId.commentBottomAnimation(),
            config.nearbyIdentityTag to packageInstance.fluxComponentId.nearbyIdentityTag(),
            config.livePhotoTag to packageInstance.fluxComponentId.livePhotoTag(),
            config.photosTag to packageInstance.fluxComponentId.photosTag(),
            config.story24Tag to packageInstance.fluxComponentId.story24Tag(),
            config.socialNewStyleStoryTag to packageInstance.fluxComponentId.socialNewStyleStoryTag(),
            config.longVideoHighlightTag to packageInstance.fluxComponentId.longVideoHighlightTag(),
            config.danmakuVertical to packageInstance.fluxComponentId.danmakuVertical(),
            config.avatar to packageInstance.fluxComponentId.avatar(),
            config.nickname to packageInstance.fluxComponentId.nickname(),
            config.postTime to packageInstance.fluxComponentId.postTime(),
            config.bellowDescTime to packageInstance.fluxComponentId.bellowDescTime(),
            config.comment to packageInstance.fluxComponentId.comment(),
            config.reply to packageInstance.fluxComponentId.reply(),
            config.share to packageInstance.fluxComponentId.share(),
            config.collect to packageInstance.fluxComponentId.collect(),
            config.anchorFramework to packageInstance.fluxComponentId.anchorFramework(),
            config.bottomBarCommon to packageInstance.fluxComponentId.bottomBarCommon(),
            config.commonButton to packageInstance.fluxComponentId.commonButton(),
            config.sticker to packageInstance.fluxComponentId.sticker(),
            config.aiSearch to packageInstance.fluxComponentId.aiSearch(),
            config.c2Feed to packageInstance.fluxComponentId.c2Feed(),
            config.flow to packageInstance.fluxComponentId.flow(),
            config.nearbyHotComment to packageInstance.fluxComponentId.nearbyHotComment(),
            config.buttonUnfollowFamiliar to packageInstance.fluxComponentId.buttonUnfollowFamiliar(),
            config.buttonUnfollowFamiliarRec to packageInstance.fluxComponentId.buttonUnfollowFamiliarRec(),
            config.coCreatorAuthor to packageInstance.fluxComponentId.coCreatorAuthor(),
            config.chapterTag to packageInstance.fluxComponentId.chapterTag(),
            config.ecomTagFriend to packageInstance.fluxComponentId.ecomTagFriend(),
            config.socialNewStylePostTimeBottom to packageInstance.fluxComponentId.socialNewStylePostTimeBottom(),
            config.socialNewStyleMusicBelow to packageInstance.fluxComponentId.socialNewStyleMusicBelow(),
            config.chapterDetail to packageInstance.fluxComponentId.chapterDetail(),
            config.titleTagContainer to packageInstance.fluxComponentId.titleTagContainer(),
            config.rightMenuLl to packageInstance.fluxComponentId.rightMenuLl(),
            config.musicMuteCover to packageInstance.fluxComponentId.musicMuteCover(),
            config.jxLeftBottomLongVideoPlusTitleTag to packageInstance.fluxComponentId.jxLeftBottomLongVideoPlusTitleTag(),
            config.bottomBarMix to packageInstance.fluxComponentId.bottomBarMix(),
            config.bottomBarNormalSearch to packageInstance.fluxComponentId.bottomBarNormalSearch(),
            config.bottomBarCommonPrioritySearch to packageInstance.fluxComponentId.bottomBarCommonPrioritySearch(),
            config.jxPick to packageInstance.fluxComponentId.jxPick(),
            config.bottomBarContainer to packageInstance.fluxComponentId.bottomBarContainer(),
            config.aiCoCreatorsThree to packageInstance.fluxComponentId.aiCoCreatorsThree(),
            config.aigcCocreateStatusTitle to packageInstance.fluxComponentId.aigcCocreateStatusTitle()
        ).filter {
            it.first
        }.map {
            it.second
        }.toSet()
    }

    override fun onHook() {
        if (ConfigManager.playbackComponentBlockConfig.mainSwitch) {
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
