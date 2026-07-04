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
import io.github.twyora.douyinenhancer.databinding.ItemInputWithDeleteBinding
import io.github.twyora.douyinenhancer.databinding.RecommendedFeedFilterDialogBinding

class RecommendedFeedFilterDialog(context: Context) : AlertDialog.Builder(context) {
    init {
        val recommendedFeedFilterDialogBinding = RecommendedFeedFilterDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        val prefs = FastKVConfigManager.settings

        // Show hidden block options when feature is enabled
        val showBlockGroups = prefs.getBoolean("enable_hidden_features", false)
        if (showBlockGroups) {
            recommendedFeedFilterDialogBinding.groupBlockAd.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockEcomAweme.visibility = View.VISIBLE
            recommendedFeedFilterDialogBinding.groupBlockGrouponLargeCard.visibility = View.VISIBLE
        }

        // restore state
        recommendedFeedFilterDialogBinding.switchBlockAd.isChecked = prefs.getBoolean("recommended_feed_filter_block_ad", false)
        recommendedFeedFilterDialogBinding.switchBlockEcomAweme.isChecked =
            prefs.getBoolean("recommended_feed_filter_block_ecom_aweme", false)
        recommendedFeedFilterDialogBinding.switchBlockGrouponLargeCard.isChecked =
            prefs.getBoolean("recommended_feed_filter_block_groupon_large_card", false)
        prefs.getInt("recommended_feed_filter_hide_short_duration_limit", 0).let {
            recommendedFeedFilterDialogBinding.editShortDuration.setText(it.toString())
        }
        prefs.getInt("recommended_feed_filter_hide_long_duration_limit", Int.MAX_VALUE).let {
            recommendedFeedFilterDialogBinding.editLongDuration.setText(it.toString())
        }
        recommendedFeedFilterDialogBinding.switchTitleRegex.isChecked = prefs.getBoolean("recommended_feed_filter_title_regex_mode", false)
        prefs.getStringSet("recommended_feed_filter_group_aweme_title", null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeTitle).apply {
                editInput.setText(it)
            }
        }
        prefs.getStringSet("recommended_feed_filter_group_author_uid", null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorUid).apply {
                editInput.inputType = InputType.TYPE_CLASS_NUMBER
                editInput.setText(it)
            }
        }
        prefs.getStringSet("recommended_feed_filter_group_author_nickname", null)?.forEach {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorNickname).apply {
                editInput.setText(it)
            }
        }
        recommendedFeedFilterDialogBinding.switchDescRegex.isChecked = prefs.getBoolean("recommended_feed_filter_desc_regex_mode", false)
        prefs.getStringSet("recommended_feed_filter_group_aweme_desc", null)?.forEach {
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
            val blockAd = recommendedFeedFilterDialogBinding.switchBlockAd.isChecked && showBlockGroups
            val blockEcomAweme = recommendedFeedFilterDialogBinding.switchBlockEcomAweme.isChecked && showBlockGroups
            val blockGrouponLargeCard = recommendedFeedFilterDialogBinding.switchBlockGrouponLargeCard.isChecked && showBlockGroups
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
                putBoolean("recommended_feed_filter_block_ad", blockAd)
                putBoolean("recommended_feed_filter_block_ecom_aweme", blockEcomAweme)
                putBoolean("recommended_feed_filter_block_groupon_large_card", blockGrouponLargeCard)
                putInt("recommended_feed_filter_hide_short_duration_limit", hideShortDurationLimit)
                putInt("recommended_feed_filter_hide_long_duration_limit", hideLongDurationLimit)
                putBoolean("recommended_feed_filter_title_regex_mode", titleRegexMode)
                putStringSet("recommended_feed_filter_group_aweme_title", titleKeywords)
                putStringSet("recommended_feed_filter_group_author_uid", uidKeywords)
                putStringSet("recommended_feed_filter_group_author_nickname", upKeywords)
                putBoolean("recommended_feed_filter_desc_regex_mode", descRegexMode)
                putStringSet("recommended_feed_filter_group_aweme_desc", descKeywords)
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
