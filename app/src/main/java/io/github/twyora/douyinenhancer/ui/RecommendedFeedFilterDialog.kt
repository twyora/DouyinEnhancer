package io.github.twyora.douyinenhancer.ui

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.edit
import androidx.core.view.children
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.config.key.MiscKey
import io.github.twyora.douyinenhancer.config.key.RecommendedFeedFilterKey
import io.github.twyora.douyinenhancer.databinding.ItemInputWithDeleteBinding
import io.github.twyora.douyinenhancer.databinding.RecommendedFeedFilterDialogBinding

class RecommendedFeedFilterDialog(context: Context) : AlertDialog.Builder(context) {
    init {
        val recommendedFeedFilterDialogBinding = RecommendedFeedFilterDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        val prefs = FastKVConfigManager.settings

        // Show hidden block options when feature is enabled
        val showBlockGroups = prefs.getBoolean(MiscKey.ENABLE_HIDDEN_FEATURES, false)
        if (showBlockGroups) {
            recommendedFeedFilterDialogBinding.groupBlockAd.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockEcomAweme.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockGrouponLargeCard.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockLive.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockMultiImage.visibility = View.VISIBLE
        }

        // restore state
        recommendedFeedFilterDialogBinding.switchMainSwitch.isChecked = prefs.getBoolean(RecommendedFeedFilterKey.MAIN_SWITCH, false)
        recommendedFeedFilterDialogBinding.switchBlockAd.isChecked = prefs.getBoolean(RecommendedFeedFilterKey.BLOCK_AD, false)
        recommendedFeedFilterDialogBinding.switchBlockEcomAweme.isChecked =
            prefs.getBoolean(RecommendedFeedFilterKey.BLOCK_ECOM, false)
        recommendedFeedFilterDialogBinding.switchBlockGrouponLargeCard.isChecked =
            prefs.getBoolean(RecommendedFeedFilterKey.BLOCK_GROUPON, false)
        recommendedFeedFilterDialogBinding.switchBlockLive.isChecked =
            prefs.getBoolean(RecommendedFeedFilterKey.BLOCK_LIVE, false)
        recommendedFeedFilterDialogBinding.switchBlockMultiImage.isChecked =
            prefs.getBoolean(RecommendedFeedFilterKey.BLOCK_MULTI_IMAGE, false)
        prefs.getInt(RecommendedFeedFilterKey.SHORT_DURATION_LIMIT, 0).let {
            recommendedFeedFilterDialogBinding.editShortDuration.setText(it.toString())
        }
        prefs.getInt(RecommendedFeedFilterKey.LONG_DURATION_LIMIT, Int.MAX_VALUE).let {
            recommendedFeedFilterDialogBinding.editLongDuration.setText(it.toString())
        }
        recommendedFeedFilterDialogBinding.switchTitleRegex.isChecked = prefs.getBoolean(RecommendedFeedFilterKey.TITLE_REGEX_MODE, false)
        prefs.getStringSet(RecommendedFeedFilterKey.TITLE_KEYWORDS, null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeTitle).apply {
                editInput.setText(it)
            }
        }
        prefs.getStringSet(RecommendedFeedFilterKey.AUTHOR_UID_KEYWORDS, null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorUid).apply {
                editInput.inputType = InputType.TYPE_CLASS_NUMBER
                editInput.setText(it)
            }
        }
        prefs.getStringSet(RecommendedFeedFilterKey.AUTHOR_NICKNAME_KEYWORDS, null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorNickname).apply {
                editInput.setText(it)
            }
        }
        recommendedFeedFilterDialogBinding.switchDescRegex.isChecked = prefs.getBoolean(RecommendedFeedFilterKey.DESC_REGEX_MODE, false)
        prefs.getStringSet(RecommendedFeedFilterKey.DESC_KEYWORDS, null)?.forEach {
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

            prefs.edit(commit = true) {
                putBoolean(RecommendedFeedFilterKey.MAIN_SWITCH, mainSwitch)
                putBoolean(RecommendedFeedFilterKey.BLOCK_AD, blockAd)
                putBoolean(RecommendedFeedFilterKey.BLOCK_ECOM, blockEcomAweme)
                putBoolean(RecommendedFeedFilterKey.BLOCK_GROUPON, blockGrouponLargeCard)
                putBoolean(RecommendedFeedFilterKey.BLOCK_LIVE, blockLive)
                putBoolean(RecommendedFeedFilterKey.BLOCK_MULTI_IMAGE, blockMultiImage)
                putInt(RecommendedFeedFilterKey.SHORT_DURATION_LIMIT, hideShortDurationLimit)
                putInt(RecommendedFeedFilterKey.LONG_DURATION_LIMIT, hideLongDurationLimit)
                putBoolean(RecommendedFeedFilterKey.TITLE_REGEX_MODE, titleRegexMode)
                putStringSet(RecommendedFeedFilterKey.TITLE_KEYWORDS, titleKeywords)
                putStringSet(RecommendedFeedFilterKey.AUTHOR_UID_KEYWORDS, uidKeywords)
                putStringSet(RecommendedFeedFilterKey.AUTHOR_NICKNAME_KEYWORDS, upKeywords)
                putBoolean(RecommendedFeedFilterKey.DESC_REGEX_MODE, descRegexMode)
                putStringSet(RecommendedFeedFilterKey.DESC_KEYWORDS, descKeywords)
            }

            (context as? Activity)?.runOnUiThread {
                Toast.makeText(context, context.getString(R.string.save_success_restart_required), Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private fun pushKeywordItem(context: Context, container: ViewGroup): ItemInputWithDeleteBinding {
            val itemBinding = ItemInputWithDeleteBinding.inflate(
                LayoutInflater.from(context),
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
