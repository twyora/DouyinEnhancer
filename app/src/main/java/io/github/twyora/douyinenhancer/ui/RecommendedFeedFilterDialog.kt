package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.children
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.databinding.ItemInputWithDeleteBinding
import io.github.twyora.douyinenhancer.databinding.RecommendedFeedFilterDialogBinding

class RecommendedFeedFilterDialog(context: Context) : AlertDialog.Builder(ContextThemeWrapper(context, R.style.MainTheme)) {
    init {
        val recommendedFeedFilterDialogBinding = RecommendedFeedFilterDialogBinding.inflate(
            LayoutInflater.from(ContextThemeWrapper(context, R.style.MainTheme))
        )
        val cfg = ConfigManager.recommendedFeedFilterConfig

        // Show hidden block options when feature is enabled
        val showBlockGroups = ConfigManager.miscConfig.hiddenFeatureEnabled
        if (showBlockGroups) {
            recommendedFeedFilterDialogBinding.groupBlockAd.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockEcomAweme.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockGrouponLargeCard.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockLive.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockMultiImage.visibility = View.VISIBLE
        }

        // restore state
        recommendedFeedFilterDialogBinding.switchMainSwitch.isChecked = cfg.mainSwitch
        recommendedFeedFilterDialogBinding.switchBlockAd.isChecked = cfg.blockAd
        recommendedFeedFilterDialogBinding.switchBlockEcomAweme.isChecked = cfg.blockEcom
        recommendedFeedFilterDialogBinding.switchBlockGrouponLargeCard.isChecked = cfg.blockGrouponLargeCard
        recommendedFeedFilterDialogBinding.switchBlockLive.isChecked = cfg.blockLive
        recommendedFeedFilterDialogBinding.switchBlockMultiImage.isChecked = cfg.blockMultiImage
        cfg.shortDurationLimit.let {
            recommendedFeedFilterDialogBinding.editShortDuration.setText(it.toString())
        }
        cfg.longDurationLimit.let {
            recommendedFeedFilterDialogBinding.editLongDuration.setText(it.toString())
        }
        cfg.collectCountMin.let {
            recommendedFeedFilterDialogBinding.editCollectCountMin.setText(it.toString())
        }
        cfg.collectCountMax.let {
            recommendedFeedFilterDialogBinding.editCollectCountMax.setText(it.toString())
        }
        cfg.commentCountMin.let {
            recommendedFeedFilterDialogBinding.editCommentCountMin.setText(it.toString())
        }
        cfg.commentCountMax.let {
            recommendedFeedFilterDialogBinding.editCommentCountMax.setText(it.toString())
        }
        cfg.diggCountMin.let {
            recommendedFeedFilterDialogBinding.editDiggCountMin.setText(it.toString())
        }
        cfg.diggCountMax.let {
            recommendedFeedFilterDialogBinding.editDiggCountMax.setText(it.toString())
        }
        cfg.shareCountMin.let {
            recommendedFeedFilterDialogBinding.editShareCountMin.setText(it.toString())
        }
        cfg.shareCountMax.let {
            recommendedFeedFilterDialogBinding.editShareCountMax.setText(it.toString())
        }
        recommendedFeedFilterDialogBinding.switchTitleRegex.isChecked = cfg.titleRegexMode
        cfg.titleKeywords.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeTitle).apply {
                editInput.setText(it)
            }
        }
        cfg.authorUidKeywords.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorUid).apply {
                editInput.inputType = InputType.TYPE_CLASS_NUMBER
                editInput.setText(it)
            }
        }
        cfg.authorNicknameKeywords.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorNickname).apply {
                editInput.setText(it)
            }
        }
        recommendedFeedFilterDialogBinding.switchAuthorNicknameRegex.isChecked = cfg.authorNicknameRegexMode
        recommendedFeedFilterDialogBinding.switchDescRegex.isChecked = cfg.descRegexMode
        cfg.descKeywords.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeDesc).apply {
                editInput.setText(it)
            }
        }

        // setup click listener
        recommendedFeedFilterDialogBinding.btnAddTitle.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeTitle)
        }
        recommendedFeedFilterDialogBinding.btnAddUid.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorUid).apply {
                editInput.inputType = InputType.TYPE_CLASS_NUMBER
            }
        }
        recommendedFeedFilterDialogBinding.btnAddAuthorNickname.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorNickname)
        }
        recommendedFeedFilterDialogBinding.btnAddDesc.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeDesc)
        }

        setView(recommendedFeedFilterDialogBinding.root)
        setTitle(context.getString(R.string.recommended_feed_filter_dialog_title))
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            val mainSwitch = recommendedFeedFilterDialogBinding.switchMainSwitch.isChecked
            val blockAd = recommendedFeedFilterDialogBinding.switchBlockAd.isChecked
            val blockEcomAweme = recommendedFeedFilterDialogBinding.switchBlockEcomAweme.isChecked
            val blockGrouponLargeCard = recommendedFeedFilterDialogBinding.switchBlockGrouponLargeCard.isChecked
            val blockLive = recommendedFeedFilterDialogBinding.switchBlockLive.isChecked
            val blockMultiImage = recommendedFeedFilterDialogBinding.switchBlockMultiImage.isChecked

            val hideShortDurationLimit = recommendedFeedFilterDialogBinding.editShortDuration.text.toString().toIntOrNull() ?: 0
            val hideLongDurationLimit = recommendedFeedFilterDialogBinding.editLongDuration.text.toString().toIntOrNull() ?: Int.MAX_VALUE
            if (hideShortDurationLimit > hideLongDurationLimit) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_bounds), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val hideCollectCountMin = recommendedFeedFilterDialogBinding.editCollectCountMin.text.toString().toIntOrNull() ?: 0
            val hideCollectCountMax = recommendedFeedFilterDialogBinding.editCollectCountMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
            if (hideCollectCountMin > hideCollectCountMax) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_bounds), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val hideCommentCountMin = recommendedFeedFilterDialogBinding.editCommentCountMin.text.toString().toIntOrNull() ?: 0
            val hideCommentCountMax = recommendedFeedFilterDialogBinding.editCommentCountMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
            if (hideCommentCountMin > hideCommentCountMax) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_bounds), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val hideDiggCountMin = recommendedFeedFilterDialogBinding.editDiggCountMin.text.toString().toIntOrNull() ?: 0
            val hideDiggCountMax = recommendedFeedFilterDialogBinding.editDiggCountMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
            if (hideDiggCountMin > hideDiggCountMax) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_bounds), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val hideShareCountMin = recommendedFeedFilterDialogBinding.editShareCountMin.text.toString().toIntOrNull() ?: 0
            val hideShareCountMax = recommendedFeedFilterDialogBinding.editShareCountMax.text.toString().toIntOrNull() ?: Int.MAX_VALUE
            if (hideShareCountMin > hideShareCountMax) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_bounds), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val titleRegexMode = recommendedFeedFilterDialogBinding.switchTitleRegex.isChecked
            val titleKeywords = recommendedFeedFilterDialogBinding.groupAwemeTitle.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.filter {
                it.isNotBlank()
            }.toSet()
            if (titleRegexMode && runCatching {
                    titleKeywords.forEach {
                        it.toRegex()
                    }
                }.isFailure
            ) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_regex), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val uidKeywords = recommendedFeedFilterDialogBinding.groupAuthorUid.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.filter {
                it.isNotBlank()
            }.toSet()

            val upKeywords = recommendedFeedFilterDialogBinding.groupAuthorNickname.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.filter {
                it.isNotBlank()
            }.toSet()

            val descRegexMode = recommendedFeedFilterDialogBinding.switchDescRegex.isChecked
            val descKeywords = recommendedFeedFilterDialogBinding.groupAwemeDesc.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.filter {
                it.isNotBlank()
            }.toSet()
            if (descRegexMode && runCatching {
                    descKeywords.forEach {
                        it.toRegex()
                    }
                }.isFailure
            ) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_regex), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            val authorNicknameRegexMode = recommendedFeedFilterDialogBinding.switchAuthorNicknameRegex.isChecked
            if (authorNicknameRegexMode && runCatching {
                    upKeywords.forEach {
                        it.toRegex()
                    }
                }.isFailure
            ) {
                (context as? Activity)?.runOnUiThread {
                    Toast.makeText(context, context.getString(R.string.save_failed_invalid_regex), Toast.LENGTH_SHORT).show()
                }
                return@setPositiveButton
            }

            cfg.mainSwitch = mainSwitch
            cfg.blockAd = blockAd
            cfg.blockEcom = blockEcomAweme
            cfg.blockGrouponLargeCard = blockGrouponLargeCard
            cfg.blockLive = blockLive
            cfg.blockMultiImage = blockMultiImage
            cfg.shortDurationLimit = hideShortDurationLimit
            cfg.longDurationLimit = hideLongDurationLimit
            cfg.collectCountMin = hideCollectCountMin
            cfg.collectCountMax = hideCollectCountMax
            cfg.commentCountMin = hideCommentCountMin
            cfg.commentCountMax = hideCommentCountMax
            cfg.diggCountMin = hideDiggCountMin
            cfg.diggCountMax = hideDiggCountMax
            cfg.shareCountMin = hideShareCountMin
            cfg.shareCountMax = hideShareCountMax
            cfg.titleRegexMode = titleRegexMode
            cfg.titleKeywords = titleKeywords
            cfg.authorUidKeywords = uidKeywords
            cfg.authorNicknameRegexMode = authorNicknameRegexMode
            cfg.authorNicknameKeywords = upKeywords
            cfg.descRegexMode = descRegexMode
            cfg.descKeywords = descKeywords

            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, context.getString(R.string.save_success_restart_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private fun pushKeywordItem(context: Context, container: ViewGroup): ItemInputWithDeleteBinding {
            val itemBinding = ItemInputWithDeleteBinding.inflate(
                LayoutInflater.from(ContextThemeWrapper(context, R.style.MainTheme)),
                container,
                false
            )
            itemBinding.btnDelete.setOnClickListener {
                container.removeView(itemBinding.root)
            }
            itemBinding.root.tag = itemBinding

            container.addView(itemBinding.root)

            return itemBinding
        }
    }
}
