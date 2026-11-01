package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommended_feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.PreferenceCategory
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem

@Composable
fun RecommendedFeedFilterContent(
    state: RecommendedFeedFilterConfig,
    actions: RecommendedFeedFilterActions,
    modifier: Modifier = Modifier,
) {
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
    modifier: Modifier = Modifier,
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
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_followed_author_title),
            checked = state.blockFollowedAuthor
        ) {
            actions.onBlockFollowedAuthorChange(it)
        }
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_ad_title),
            summary = stringResource(R.string.recommended_feed_filter_block_ad_summary),
            checked = state.blockAd
        ) {
            actions.onBlockAdChange(it)
        }
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_ecom_aweme_title),
            summary = stringResource(R.string.recommended_feed_filter_block_ecom_aweme_summary),
            checked = state.blockEcom
        ) {
            actions.onBlockEcomChange(it)
        }
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_groupon_large_card_title),
            summary = stringResource(R.string.recommended_feed_filter_block_groupon_large_card_summary),
            checked = state.blockGrouponLargeCard
        ) {
            actions.onBlockGrouponChange(it)
        }
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_live_title),
            summary = stringResource(R.string.recommended_feed_filter_block_live_summary),
            checked = state.blockLive
        ) {
            actions.onBlockLiveChange(it)
        }
        SwitchPreferenceItem(
            title = stringResource(R.string.recommended_feed_filter_block_multi_image_title),
            summary = stringResource(R.string.recommended_feed_filter_block_multi_image_summary),
            checked = state.blockMultiImage
        ) {
            actions.onBlockMultiImageChange(it)
        }
    }
}

@Composable
private fun RecommendedFeedFilterCountSection(
    state: RecommendedFeedFilterCountConfig,
    actions: RecommendedFeedFilterCountActions,
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
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
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.recommended_feed_filter_group_title),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = actions.onAdd) {
                Text(text = stringResource(R.string.add))
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.regex_mode)
                )
                Switch(
                    checked = state.regexMode,
                    onCheckedChange = {
                        actions.onRegexChange(it)
                    }
                )
            }
        }
        state.keywords.forEachIndexed { index, text ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeywordInputRow(
                    text = text,
                    onTextChange = {
                        actions.onTextChange(index, it)
                    },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        actions.onRemove(index)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun RecommendedFeedFilterUidKeywordGroup(
    state: RecommendedFeedFilterUidKeywordsConfig,
    actions: RecommendedFeedFilterUidKeywordActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.recommended_feed_filter_group_uid),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = actions.onAdd) {
                Text(text = stringResource(R.string.add))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
        state.keywords.forEachIndexed { index, text ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeywordInputRow(
                    text = text,
                    onTextChange = {
                        actions.onTextChange(index, it)
                    },
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                )
                TextButton(
                    onClick = {
                        actions.onRemove(index)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun RecommendedFeedFilterNicknameKeywordGroup(
    state: RecommendedFeedFilterNicknameKeywordsConfig,
    actions: RecommendedFeedFilterNicknameKeywordActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.recommended_feed_filter_group_author_nickname),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = actions.onAdd) {
                Text(text = stringResource(R.string.add))
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.regex_mode)
                )
                Switch(
                    checked = state.regexMode,
                    onCheckedChange = {
                        actions.onRegexChange(it)
                    }
                )
            }
        }
        state.keywords.forEachIndexed { index, text ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeywordInputRow(
                    text = text,
                    onTextChange = {
                        actions.onTextChange(index, it)
                    },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        actions.onRemove(index)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun RecommendedFeedFilterDescKeywordGroup(
    state: RecommendedFeedFilterDescKeywordsConfig,
    actions: RecommendedFeedFilterDescKeywordActions,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.recommended_feed_filter_group_desc),
                modifier = Modifier.weight(1f)
            )
            TextButton(onClick = actions.onAdd) {
                Text(text = stringResource(R.string.add))
            }
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.regex_mode)
                )
                Switch(
                    checked = state.regexMode,
                    onCheckedChange = {
                        actions.onRegexChange(it)
                    }
                )
            }
        }
        state.keywords.forEachIndexed { index, text ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                KeywordInputRow(
                    text = text,
                    onTextChange = {
                        actions.onTextChange(index, it)
                    },
                    modifier = Modifier.weight(1f),
                )
                TextButton(
                    onClick = {
                        actions.onRemove(index)
                    }
                ) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        }
    }
}

@Composable
private fun NumberInputRow(
    label: String,
    value: Int?,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var text by remember(value) {
        mutableStateOf(value?.toString() ?: "")
    }
    var hadFocus by remember {
        mutableStateOf(false)
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = label, modifier = Modifier.weight(2f))
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    onValueChange(text.toIntOrNull())
                },
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

@Composable
private fun KeywordInputRow(
    text: String,
    onTextChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    var localText by remember {
        mutableStateOf(text)
    }
    LaunchedEffect(text) {
        localText = text
    }
    var hadFocus by remember {
        mutableStateOf(false)
    }
    OutlinedTextField(
        value = localText,
        onValueChange = {
            localText = it
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                onTextChange(localText)
            },
        ),
        modifier = modifier.onFocusChanged { state ->
            if (hadFocus && !state.isFocused) {
                onTextChange(localText)
            }
            hadFocus = state.isFocused
        },
        singleLine = true,
    )
}

@Preview
@Composable
private fun RecommendedFeedFilterContentPreview() {
    RecommendedFeedFilterContent(
        state = RecommendedFeedFilterConfig(
            switch = RecommendedFeedFilterSwitchConfig(
                mainSwitch = false,
                blockFollowedAuthor = false,
                blockAd = false,
                blockEcom = false,
                blockGrouponLargeCard = false,
                blockLive = false,
                blockMultiImage = false,
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
                shareCountMax = Int.MAX_VALUE,
            ),
            keyword = RecommendedFeedFilterKeywordConfig(
                title = RecommendedFeedFilterTitleKeywordsConfig(emptyList(), regexMode = false),
                uid = RecommendedFeedFilterUidKeywordsConfig(emptyList()),
                nickname = RecommendedFeedFilterNicknameKeywordsConfig(emptyList(), regexMode = false),
                desc = RecommendedFeedFilterDescKeywordsConfig(emptyList(), regexMode = false),
            ),
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
                    onAdd = {},
                    onTextChange = { _, _ -> },
                    onRemove = {},
                    onRegexChange = {}
                ),
                uid = RecommendedFeedFilterUidKeywordActions(
                    onAdd = {},
                    onTextChange = { _, _ -> },
                    onRemove = {}
                ),
                nickname = RecommendedFeedFilterNicknameKeywordActions(
                    onAdd = {},
                    onTextChange = { _, _ -> },
                    onRemove = { _ -> },
                    onRegexChange = { _ -> }
                ),
                desc = RecommendedFeedFilterDescKeywordActions(
                    onAdd = {},
                    onTextChange = { _, _ -> },
                    onRemove = {},
                    onRegexChange = {}
                )
            )
        ),
    )
}
