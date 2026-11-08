package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.PreferenceCategory
import io.github.twyora.douyinenhancer.ui.components.PreferenceItem
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun RecommendedFeedFilterContent(state: RecommendedFeedFilterConfig, actions: RecommendedFeedFilterActions, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier
            .fillMaxSize()
            .imePadding()
    ) {
        item {
            RecommendedFeedFilterSwitchSection(state.switch, actions.switch)
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        item {
            RecommendedFeedFilterCountSection(state.count, actions.count)
        }
        item {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        }
        item {
            RecommendedFeedFilterKeywordSection(state.keyword, actions.keyword)
        }
    }
}

@Composable
private fun RecommendedFeedFilterSwitchSection(
    state: RecommendedFeedFilterSwitchConfig,
    actions: RecommendedFeedFilterSwitchActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_main_switch_title),
            summary = stringResource(R.string.recommended_feed_filter_main_switch_summary),
            checked = state.mainSwitch
        ) {
            actions.onMainSwitchChange(it)
        }
        if (state.blockFollowedAuthorVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_followed_author_title),
                checked = state.blockFollowedAuthor
            ) {
                actions.onBlockFollowedAuthorChange(it)
            }
        }
        if (state.blockAdVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_ad_title),
                summary = stringResource(R.string.recommended_feed_filter_block_ad_summary),
                checked = state.blockAd
            ) {
                actions.onBlockAdChange(it)
            }
        }
        if (state.blockEcomVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_ecom_aweme_title),
                summary = stringResource(R.string.recommended_feed_filter_block_ecom_aweme_summary),
                checked = state.blockEcom
            ) {
                actions.onBlockEcomChange(it)
            }
        }
        if (state.blockGrouponLargeCardVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_groupon_large_card_title),
                summary = stringResource(R.string.recommended_feed_filter_block_groupon_large_card_summary),
                checked = state.blockGrouponLargeCard
            ) {
                actions.onBlockGrouponChange(it)
            }
        }
        if (state.blockLiveVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_live_title),
                summary = stringResource(R.string.recommended_feed_filter_block_live_summary),
                checked = state.blockLive
            ) {
                actions.onBlockLiveChange(it)
            }
        }
        if (state.blockMultiImageVisible) {
            SwitchPreferenceItem(
                title = stringResource(R.string.recommended_feed_filter_block_multi_image_title),
                summary = stringResource(R.string.recommended_feed_filter_block_multi_image_summary),
                checked = state.blockMultiImage
            ) {
                actions.onBlockMultiImageChange(it)
            }
        }
    }
}

@Composable
private fun RecommendedFeedFilterCountSection(
    state: RecommendedFeedFilterCountConfig,
    actions: RecommendedFeedFilterCountActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.recommended_feed_filter_hide_by_duration_title))
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_short_duration),
            value = state.shortDuration,
            onValueChange = {
                actions.onShortDurationChange(it)
            }
        )
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_long_duration),
            value = state.longDuration,
            onValueChange = {
                actions.onLongDurationChange(it)
            }
        )

        PreferenceCategory(stringResource(R.string.recommended_feed_filter_hide_by_collect_count_title))
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_collect_count_min),
            value = state.collectCountMin,
            onValueChange = {
                actions.onCollectCountMinChange(it)
            }
        )
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_collect_count_max),
            value = state.collectCountMax,
            onValueChange = {
                actions.onCollectCountMaxChange(it)
            }
        )

        PreferenceCategory(stringResource(R.string.recommended_feed_filter_hide_by_comment_count_title))
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_comment_count_min),
            value = state.commentCountMin,
            onValueChange = {
                actions.onCommentCountMinChange(it)
            }
        )
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_comment_count_max),
            value = state.commentCountMax,
            onValueChange = {
                actions.onCommentCountMaxChange(it)
            }
        )

        PreferenceCategory(stringResource(R.string.recommended_feed_filter_hide_by_digg_count_title))
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_digg_count_min),
            value = state.diggCountMin,
            onValueChange = {
                actions.onDiggCountMinChange(it)
            }
        )
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_digg_count_max),
            value = state.diggCountMax,
            onValueChange = {
                actions.onDiggCountMaxChange(it)
            }
        )

        PreferenceCategory(stringResource(R.string.recommended_feed_filter_hide_by_share_count_title))
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_share_count_min),
            value = state.shareCountMin,
            onValueChange = {
                actions.onShareCountMinChange(it)
            }
        )
        NumberInputRow(
            label = stringResource(R.string.recommended_feed_filter_hide_by_share_count_max),
            value = state.shareCountMax,
            onValueChange = {
                actions.onShareCountMaxChange(it)
            }
        )
    }
}

@Composable
private fun RecommendedFeedFilterKeywordSection(
    state: RecommendedFeedFilterKeywordConfig,
    actions: RecommendedFeedFilterKeywordActions,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        PreferenceCategory(stringResource(R.string.recommended_feed_filter_keywords_title))
        RecommendedFeedFilterTitleKeywordGroup(state = state.title, actions = actions.title)
        RecommendedFeedFilterUidKeywordGroup(state = state.uid, actions = actions.uid)
        RecommendedFeedFilterNicknameKeywordGroup(state = state.nickname, actions = actions.nickname)
        RecommendedFeedFilterDescKeywordGroup(state = state.desc, actions = actions.desc)
    }
}

@Composable
private fun RecommendedFeedFilterTitleKeywordGroup(
    state: RecommendedFeedFilterTitleKeywordsConfig,
    actions: RecommendedFeedFilterTitleKeywordActions,
    modifier: Modifier = Modifier
) {
    var showEditorDialog by remember {
        mutableStateOf(false)
    }

    PreferenceItem(
        title = stringResource(R.string.recommended_feed_filter_group_title),
        modifier = modifier,
        onClick = {
            showEditorDialog = true
        }
    )

    if (showEditorDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditorDialog = false
            },
            title = {
                Text(text = stringResource(R.string.recommended_feed_filter_group_title))
            },
            text = {
                Column {
                    SwitchPreferenceItem(
                        title = stringResource(R.string.regex_mode),
                        checked = state.regexMode,
                        onCheckedChange = {
                            actions.onRegexChange(it)
                        }
                    )
                    KeywordEditorContent(
                        keywords = state.keywords,
                        onAddKeyword = actions.onAddKeyword,
                        onKeywordTextChange = actions.onKeywordTextChange,
                        onRemoveKeyword = actions.onRemoveKeyword
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actions.onSave()) {
                            showEditorDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditorDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RecommendedFeedFilterUidKeywordGroup(
    state: RecommendedFeedFilterUidKeywordsConfig,
    actions: RecommendedFeedFilterUidKeywordActions,
    modifier: Modifier = Modifier
) {
    var showEditorDialog by remember {
        mutableStateOf(false)
    }

    PreferenceItem(
        title = stringResource(R.string.recommended_feed_filter_group_uid),
        modifier = modifier,
        onClick = {
            showEditorDialog = true
        }
    )

    if (showEditorDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditorDialog = false
            },
            title = {
                Text(text = stringResource(R.string.recommended_feed_filter_group_uid))
            },
            text = {
                KeywordEditorContent(
                    keywords = state.keywords,
                    onAddKeyword = actions.onAddKeyword,
                    onKeywordTextChange = actions.onKeywordTextChange,
                    onRemoveKeyword = actions.onRemoveKeyword
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actions.onSave()) {
                            showEditorDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditorDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RecommendedFeedFilterNicknameKeywordGroup(
    state: RecommendedFeedFilterNicknameKeywordsConfig,
    actions: RecommendedFeedFilterNicknameKeywordActions,
    modifier: Modifier = Modifier
) {
    var showEditorDialog by remember {
        mutableStateOf(false)
    }

    PreferenceItem(
        title = stringResource(R.string.recommended_feed_filter_group_author_nickname),
        modifier = modifier,
        onClick = {
            showEditorDialog = true
        }
    )

    if (showEditorDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditorDialog = false
            },
            title = {
                Text(text = stringResource(R.string.recommended_feed_filter_group_author_nickname))
            },
            text = {
                Column {
                    SwitchPreferenceItem(
                        title = stringResource(R.string.regex_mode),
                        checked = state.regexMode,
                        onCheckedChange = {
                            actions.onRegexChange(it)
                        }
                    )
                    KeywordEditorContent(
                        keywords = state.keywords,
                        onAddKeyword = actions.onAddKeyword,
                        onKeywordTextChange = actions.onKeywordTextChange,
                        onRemoveKeyword = actions.onRemoveKeyword
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actions.onSave()) {
                            showEditorDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditorDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun RecommendedFeedFilterDescKeywordGroup(
    state: RecommendedFeedFilterDescKeywordsConfig,
    actions: RecommendedFeedFilterDescKeywordActions,
    modifier: Modifier = Modifier
) {
    var showEditorDialog by remember {
        mutableStateOf(false)
    }

    PreferenceItem(
        title = stringResource(R.string.recommended_feed_filter_group_desc),
        modifier = modifier,
        onClick = {
            showEditorDialog = true
        }
    )

    if (showEditorDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditorDialog = false
            },
            title = {
                Text(text = stringResource(R.string.recommended_feed_filter_group_desc))
            },
            text = {
                Column {
                    SwitchPreferenceItem(
                        title = stringResource(R.string.regex_mode),
                        checked = state.regexMode,
                        onCheckedChange = {
                            actions.onRegexChange(it)
                        }
                    )
                    KeywordEditorContent(
                        keywords = state.keywords,
                        onAddKeyword = actions.onAddKeyword,
                        onKeywordTextChange = actions.onKeywordTextChange,
                        onRemoveKeyword = actions.onRemoveKeyword
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (actions.onSave()) {
                            showEditorDialog = false
                        }
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditorDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            }
        )
    }
}

@Composable
private fun KeywordEditorContent(
    modifier: Modifier = Modifier,
    keywords: List<String>,
    onAddKeyword: () -> Unit,
    onKeywordTextChange: (index: Int, text: String) -> Unit,
    onRemoveKeyword: (index: Int) -> Unit
) {
    var newKeyword by remember {
        mutableStateOf("")
    }

    Column(
        modifier = modifier
            .heightIn(max = 320.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = newKeyword,
                onValueChange = {
                    newKeyword = it
                },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            TextButton(
                enabled = newKeyword.isNotBlank(),
                onClick = {
                    onAddKeyword()
                    if (newKeyword.isNotBlank()) {
                        onKeywordTextChange(keywords.size, newKeyword)
                    }
                    newKeyword = ""
                }
            ) {
                Text(text = stringResource(R.string.add))
            }
        }
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        keywords.forEachIndexed { index, text ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = {
                        onKeywordTextChange(index, it)
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                TextButton(
                    onClick = {
                        onRemoveKeyword(index)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun NumberInputRow(label: String, value: Int?, onValueChange: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) {
        mutableStateOf(value?.toString() ?: "")
    }
    var hadFocus by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, modifier = Modifier.weight(2f))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onValueChange(text.toIntOrNull())
                }
            ),
            modifier = Modifier
                .weight(3f)
                .onFocusChanged { state ->
                    if (hadFocus && !state.isFocused) {
                        onValueChange(text.toIntOrNull())
                    }
                    hadFocus = state.isFocused
                }
        )
    }
}

@Preview
@Composable
private fun RecommendedFeedFilterContentPreview() {
    RecommendedFeedFilterContent(
        state = RecommendedFeedFilterConfig(
            switch = RecommendedFeedFilterSwitchConfig(
                mainSwitch = false,
                blockFollowedAuthor = false,
                blockFollowedAuthorVisible = true,
                blockAd = false,
                blockAdVisible = true,
                blockEcom = false,
                blockEcomVisible = true,
                blockGrouponLargeCard = false,
                blockGrouponLargeCardVisible = true,
                blockLive = false,
                blockLiveVisible = true,
                blockMultiImage = false,
                blockMultiImageVisible = true
            ),
            count = RecommendedFeedFilterCountConfig(
                shortDuration = 0,
                longDuration = Int.MAX_VALUE,
                collectCountMin = 0,
                collectCountMax = Int.MAX_VALUE,
                commentCountMin = 0,
                commentCountMax = Int.MAX_VALUE,
                diggCountMin = 0,
                diggCountMax = Int.MAX_VALUE,
                shareCountMin = 0,
                shareCountMax = Int.MAX_VALUE
            ),
            keyword = RecommendedFeedFilterKeywordConfig(
                title = RecommendedFeedFilterTitleKeywordsConfig(emptyList(), regexMode = false),
                uid = RecommendedFeedFilterUidKeywordsConfig(emptyList()),
                nickname = RecommendedFeedFilterNicknameKeywordsConfig(emptyList(), regexMode = false),
                desc = RecommendedFeedFilterDescKeywordsConfig(emptyList(), regexMode = false)
            )
        ),
        actions = RecommendedFeedFilterActions(
            switch = RecommendedFeedFilterSwitchActions(
                onMainSwitchChange = {},
                onBlockFollowedAuthorChange = {},
                onBlockAdChange = {},
                onBlockEcomChange = {},
                onBlockGrouponChange = {},
                onBlockLiveChange = {},
                onBlockMultiImageChange = {}
            ),
            count = RecommendedFeedFilterCountActions(
                onShortDurationChange = {},
                onLongDurationChange = {},
                onCollectCountMinChange = {},
                onCollectCountMaxChange = {},
                onCommentCountMinChange = {},
                onCommentCountMaxChange = {},
                onDiggCountMinChange = {},
                onDiggCountMaxChange = {},
                onShareCountMinChange = {},
                onShareCountMaxChange = {}
            ),
            keyword = RecommendedFeedFilterKeywordActions(
                title = RecommendedFeedFilterTitleKeywordActions(
                    onAddKeyword = {},
                    onKeywordTextChange = { _, _ -> },
                    onRemoveKeyword = {},
                    onSave = { true },
                    onRegexChange = {}
                ),
                uid = RecommendedFeedFilterUidKeywordActions(
                    onAddKeyword = {},
                    onKeywordTextChange = { _, _ -> },
                    onRemoveKeyword = {},
                    onSave = { true }
                ),
                nickname = RecommendedFeedFilterNicknameKeywordActions(
                    onAddKeyword = {},
                    onKeywordTextChange = { _, _ -> },
                    onRemoveKeyword = { _ -> },
                    onSave = { true },
                    onRegexChange = { _ -> }
                ),
                desc = RecommendedFeedFilterDescKeywordActions(
                    onAddKeyword = {},
                    onKeywordTextChange = { _, _ -> },
                    onRemoveKeyword = {},
                    onSave = { true },
                    onRegexChange = {}
                )
            )
        )
    )
}
