package io.github.twyora.douyinenhancer.ui

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.edit
import androidx.core.view.children
import io.github.twyora.douyinenhancer.config.FastKVConfigManager
import io.github.twyora.douyinenhancer.databinding.ItemInputWithDeleteBinding
import io.github.twyora.douyinenhancer.databinding.RecommendedFeedFilterDialogBinding

class RecommendedFeedFilterDialog(context: Context) : AlertDialog.Builder(context) {
    init {
        val recommendedFeedFilterDialogBinding = RecommendedFeedFilterDialogBinding.inflate(
            LayoutInflater.from(context)
        )
        val prefs = FastKVConfigManager.settings

        // restore state
        recommendedFeedFilterDialogBinding.switchHideAd.isChecked = prefs.getBoolean("recommended_feed_filter_hide_ad", false)
        recommendedFeedFilterDialogBinding.switchHideEcomAweme.isChecked = prefs.getBoolean("recommended_feed_filter_hide_ecom_aweme", false)
        recommendedFeedFilterDialogBinding.switchHideGrouponLargeCard.isChecked = prefs.getBoolean("recommended_feed_filter_hide_groupon_large_card", false)
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
        recommendedFeedFilterDialogBinding.btnAddUp.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAuthorNickname)
        }
        recommendedFeedFilterDialogBinding.btnAddDesc.setOnClickListener {
            pushKeywordItem(context, recommendedFeedFilterDialogBinding.groupAwemeDesc)
        }

        setView(recommendedFeedFilterDialogBinding.root)
        setTitle("首页推荐过滤器")
        setNegativeButton(android.R.string.cancel, null)
        setPositiveButton(android.R.string.ok) { _, _ ->
            val hideAd = recommendedFeedFilterDialogBinding.switchHideAd.isChecked
            val hideEcomAweme = recommendedFeedFilterDialogBinding.switchHideEcomAweme.isChecked
            val hideGrouponLargeCard = recommendedFeedFilterDialogBinding.switchHideGrouponLargeCard.isChecked
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
                return@setPositiveButton
            }

            val uidKeywords = recommendedFeedFilterDialogBinding.groupAuthorUid.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.filter {
                it.isNotBlank()
            }.toSet()

            val upKeywords = recommendedFeedFilterDialogBinding.groupAuthorNickname.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.toSet()

            val descRegexMode = recommendedFeedFilterDialogBinding.switchDescRegex.isChecked
            val descKeywords = recommendedFeedFilterDialogBinding.groupAwemeDesc.children.map {
                (it.tag as ItemInputWithDeleteBinding).editInput.text.toString()
            }.toSet()
            if (descRegexMode && runCatching {
                    descKeywords.forEach {
                        it.toRegex()
                    }
                }.isFailure
            ) {
                return@setPositiveButton
            }

            prefs.edit(commit = true) {
                putBoolean("recommended_feed_filter_hide_ad", hideAd)
                putBoolean("recommended_feed_filter_hide_ecom_aweme", hideEcomAweme)
                putBoolean("recommended_feed_filter_hide_groupon_large_card", hideGrouponLargeCard)
                putInt("recommended_feed_filter_hide_short_duration_limit", hideShortDurationLimit)
                putInt("recommended_feed_filter_hide_long_duration_limit", hideLongDurationLimit)
                putBoolean("recommended_feed_filter_title_regex_mode", titleRegexMode)
                putStringSet("recommended_feed_filter_group_aweme_title", titleKeywords)
                putStringSet("recommended_feed_filter_group_author_uid", uidKeywords)
                putStringSet("recommended_feed_filter_group_author_nickname", upKeywords)
                putBoolean("recommended_feed_filter_desc_regex_mode", descRegexMode)
                putStringSet("recommended_feed_filter_group_aweme_desc", descKeywords)
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
