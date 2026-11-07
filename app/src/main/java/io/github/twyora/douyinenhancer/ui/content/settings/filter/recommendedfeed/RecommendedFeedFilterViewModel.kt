package io.github.twyora.douyinenhancer.ui.content.settings.filter.recommendedfeed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.highcapable.yukihookapi.hook.log.YLog
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.config.ConfigManager
import io.github.twyora.douyinenhancer.config.gate.ConfigStateMode
import io.github.twyora.douyinenhancer.ui.event.SnackEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class RecommendedFeedFilterViewModel : ViewModel() {
    val snackFlow: SharedFlow<SnackEvent>
        field = MutableSharedFlow<SnackEvent>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )

    // TODO: Interim solution — non-NORMAL items are dropped while hidden features are off, with
    // GRAYED intentionally unhandled for now (no configItem refactor intended). The gating layer was
    // over-engineered ahead of any real usage and will need its own rework; when that happens this site
    // must receive the full, correct implementation instead of this shortcut.
    private val hiddenFeatureEnabled = ConfigManager.misc.hiddenFeatureEnabled.observe()

    private val switchConfig = with(ConfigManager.recommendedFeedFilter) {
        combine(
            mainSwitch.observe(),
            blockFollowedAuthor.observe(),
            blockAd.observe(),
            blockEcom.observe(),
            blockGrouponLargeCard.observe(),
            blockLive.observe(),
            blockMultiImage.observe(),
            hiddenFeatureEnabled
        ) { values ->
            RecommendedFeedFilterSwitchConfig(
                mainSwitch = values[0],
                blockFollowedAuthor = values[1],
                blockFollowedAuthorVisible = blockFollowedAuthor.status == ConfigStateMode.NORMAL,
                blockAd = values[2],
                blockAdVisible = blockAd.status == ConfigStateMode.NORMAL,
                blockEcom = values[3],
                blockEcomVisible = blockEcom.status == ConfigStateMode.NORMAL,
                blockGrouponLargeCard = values[4],
                blockGrouponLargeCardVisible = blockGrouponLargeCard.status == ConfigStateMode.NORMAL,
                blockLive = values[5],
                blockLiveVisible = blockLive.status == ConfigStateMode.NORMAL,
                blockMultiImage = values[6],
                blockMultiImageVisible = blockMultiImage.status == ConfigStateMode.NORMAL
            )
        }
    }

    private val shortDurationLimit = MutableStateFlow(ConfigManager.recommendedFeedFilter.shortDurationLimit.defValue)
    private val longDurationLimit = MutableStateFlow(ConfigManager.recommendedFeedFilter.longDurationLimit.defValue)
    private val collectCountMin = MutableStateFlow(ConfigManager.recommendedFeedFilter.collectCountMin.defValue)
    private val collectCountMax = MutableStateFlow(ConfigManager.recommendedFeedFilter.collectCountMax.defValue)
    private val commentCountMin = MutableStateFlow(ConfigManager.recommendedFeedFilter.commentCountMin.defValue)
    private val commentCountMax = MutableStateFlow(ConfigManager.recommendedFeedFilter.commentCountMax.defValue)
    private val diggCountMin = MutableStateFlow(ConfigManager.recommendedFeedFilter.diggCountMin.defValue)
    private val diggCountMax = MutableStateFlow(ConfigManager.recommendedFeedFilter.diggCountMax.defValue)
    private val shareCountMin = MutableStateFlow(ConfigManager.recommendedFeedFilter.shareCountMin.defValue)
    private val shareCountMax = MutableStateFlow(ConfigManager.recommendedFeedFilter.shareCountMax.defValue)

    private val countConfig = combine(
        shortDurationLimit,
        longDurationLimit,
        collectCountMin,
        collectCountMax,
        commentCountMin,
        commentCountMax,
        diggCountMin,
        diggCountMax,
        shareCountMin,
        shareCountMax
    ) {
        RecommendedFeedFilterCountConfig(
            shortDuration = it[0],
            longDuration = it[1],
            collectCountMin = it[2],
            collectCountMax = it[3],
            commentCountMin = it[4],
            commentCountMax = it[5],
            diggCountMin = it[6],
            diggCountMax = it[7],
            shareCountMin = it[8],
            shareCountMax = it[9]
        )
    }

    private val titleKeywords = MutableStateFlow(emptyList<String>())
    private val uidKeywords = MutableStateFlow(emptyList<String>())
    private val nicknameKeywords = MutableStateFlow(emptyList<String>())
    private val descKeywords = MutableStateFlow(emptyList<String>())
    private val titleRegexMode = MutableStateFlow(ConfigManager.recommendedFeedFilter.titleRegexMode.defValue)
    private val nicknameRegexMode = MutableStateFlow(ConfigManager.recommendedFeedFilter.authorNicknameRegexMode.defValue)
    private val descRegexMode = MutableStateFlow(ConfigManager.recommendedFeedFilter.descRegexMode.defValue)

    init {
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.titleKeywords.observe().collect {
                titleKeywords.value = it.sorted()
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.authorUidKeywords.observe().collect {
                uidKeywords.value = it.sorted()
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.authorNicknameKeywords.observe().collect {
                nicknameKeywords.value = it.sorted()
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.descKeywords.observe().collect {
                descKeywords.value = it.sorted()
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.shortDurationLimit.observe().collect {
                shortDurationLimit.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.longDurationLimit.observe().collect {
                longDurationLimit.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.collectCountMin.observe().collect {
                collectCountMin.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.collectCountMax.observe().collect {
                collectCountMax.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.commentCountMin.observe().collect {
                commentCountMin.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.commentCountMax.observe().collect {
                commentCountMax.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.diggCountMin.observe().collect {
                diggCountMin.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.diggCountMax.observe().collect {
                diggCountMax.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.shareCountMin.observe().collect {
                shareCountMin.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.shareCountMax.observe().collect {
                shareCountMax.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.titleRegexMode.observe().collect {
                titleRegexMode.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.authorNicknameRegexMode.observe().collect {
                nicknameRegexMode.value = it
            }
        }
        viewModelScope.launch {
            ConfigManager.recommendedFeedFilter.descRegexMode.observe().collect {
                descRegexMode.value = it
            }
        }
    }

    private val titleKeywordsConfig = combine(
        titleKeywords,
        titleRegexMode
    ) { kws, regex ->
        RecommendedFeedFilterTitleKeywordsConfig(
            keywords = kws,
            regexMode = regex
        )
    }

    private val uidKeywordsConfig = uidKeywords.map { kws ->
        RecommendedFeedFilterUidKeywordsConfig(
            keywords = kws
        )
    }

    private val nicknameKeywordsConfig = combine(
        nicknameKeywords,
        nicknameRegexMode
    ) { kws, regex ->
        RecommendedFeedFilterNicknameKeywordsConfig(
            keywords = kws,
            regexMode = regex
        )
    }

    private val descKeywordsConfig = combine(
        descKeywords,
        descRegexMode
    ) { kws, regex ->
        RecommendedFeedFilterDescKeywordsConfig(
            keywords = kws,
            regexMode = regex
        )
    }

    private val keywordConfig = combine(
        titleKeywordsConfig,
        uidKeywordsConfig,
        nicknameKeywordsConfig,
        descKeywordsConfig
    ) { t, u, n, k ->
        RecommendedFeedFilterKeywordConfig(
            title = t,
            uid = u,
            nickname = n,
            desc = k
        )
    }

    val filterConfig: StateFlow<RecommendedFeedFilterConfig> = combine(
        switchConfig,
        countConfig,
        keywordConfig
    ) { switch, count, keyword ->
        RecommendedFeedFilterConfig(
            switch = switch,
            count = count,
            keyword = keyword
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = RecommendedFeedFilterConfig(
            switch = with(ConfigManager.recommendedFeedFilter) {
                RecommendedFeedFilterSwitchConfig(
                    mainSwitch = mainSwitch.defValue,
                    blockFollowedAuthor = blockFollowedAuthor.defValue,
                    blockFollowedAuthorVisible = blockFollowedAuthor.status == ConfigStateMode.NORMAL,
                    blockAd = blockAd.defValue,
                    blockAdVisible = blockAd.status == ConfigStateMode.NORMAL,
                    blockEcom = blockEcom.defValue,
                    blockEcomVisible = blockEcom.status == ConfigStateMode.NORMAL,
                    blockGrouponLargeCard = blockGrouponLargeCard.defValue,
                    blockGrouponLargeCardVisible = blockGrouponLargeCard.status == ConfigStateMode.NORMAL,
                    blockLive = blockLive.defValue,
                    blockLiveVisible = blockLive.status == ConfigStateMode.NORMAL,
                    blockMultiImage = blockMultiImage.defValue,
                    blockMultiImageVisible = blockMultiImage.status == ConfigStateMode.NORMAL
                )
            },
            count = with(ConfigManager.recommendedFeedFilter) {
                RecommendedFeedFilterCountConfig(
                    shortDuration = shortDurationLimit.defValue,
                    longDuration = longDurationLimit.defValue,
                    collectCountMin = collectCountMin.defValue,
                    collectCountMax = collectCountMax.defValue,
                    commentCountMin = commentCountMin.defValue,
                    commentCountMax = commentCountMax.defValue,
                    diggCountMin = diggCountMin.defValue,
                    diggCountMax = diggCountMax.defValue,
                    shareCountMin = shareCountMin.defValue,
                    shareCountMax = shareCountMax.defValue
                )
            },
            keyword = RecommendedFeedFilterKeywordConfig(
                title = RecommendedFeedFilterTitleKeywordsConfig(
                    keywords = ConfigManager.recommendedFeedFilter.titleKeywords.defValue.sorted(),
                    regexMode = ConfigManager.recommendedFeedFilter.titleRegexMode.defValue
                ),
                uid = RecommendedFeedFilterUidKeywordsConfig(
                    keywords = ConfigManager.recommendedFeedFilter.authorUidKeywords.defValue.sorted()
                ),
                nickname = RecommendedFeedFilterNicknameKeywordsConfig(
                    keywords = ConfigManager.recommendedFeedFilter.authorNicknameKeywords.defValue.sorted(),
                    regexMode = ConfigManager.recommendedFeedFilter.authorNicknameRegexMode.defValue
                ),
                desc = RecommendedFeedFilterDescKeywordsConfig(
                    keywords = ConfigManager.recommendedFeedFilter.descKeywords.defValue.sorted(),
                    regexMode = ConfigManager.recommendedFeedFilter.descRegexMode.defValue
                )
            )
        )
    )

    private fun sendSnack(event: SnackEvent) {
        snackFlow.tryEmit(event)
    }

    fun setMainSwitch(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.mainSwitch.value = enable
    }

    fun setBlockFollowedAuthor(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockFollowedAuthor.value = enable
    }

    fun setBlockAd(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockAd.value = enable
    }

    fun setBlockEcom(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockEcom.value = enable
    }

    fun setBlockGrouponLargeCard(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockGrouponLargeCard.value = enable
    }

    fun setBlockLive(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockLive.value = enable
    }

    fun setBlockMultiImage(enable: Boolean) {
        ConfigManager.recommendedFeedFilter.blockMultiImage.value = enable
    }

    fun setShortDuration(value: Int?) {
        val v = value ?: 0
        if (v <= longDurationLimit.value) {
            ConfigManager.recommendedFeedFilter.shortDurationLimit.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setLongDuration(value: Int?) {
        val v = value ?: Int.MAX_VALUE
        if (v >= shortDurationLimit.value) {
            ConfigManager.recommendedFeedFilter.longDurationLimit.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setCollectCountMin(value: Int?) {
        val v = value ?: 0
        if (v <= collectCountMax.value) {
            ConfigManager.recommendedFeedFilter.collectCountMin.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setCollectCountMax(value: Int?) {
        val v = value ?: Int.MAX_VALUE
        if (v >= collectCountMin.value) {
            ConfigManager.recommendedFeedFilter.collectCountMax.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setCommentCountMin(value: Int?) {
        val v = value ?: 0
        if (v <= commentCountMax.value) {
            ConfigManager.recommendedFeedFilter.commentCountMin.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setCommentCountMax(value: Int?) {
        val v = value ?: Int.MAX_VALUE
        if (v >= commentCountMin.value) {
            ConfigManager.recommendedFeedFilter.commentCountMax.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setDiggCountMin(value: Int?) {
        val v = value ?: 0
        if (v <= diggCountMax.value) {
            ConfigManager.recommendedFeedFilter.diggCountMin.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setDiggCountMax(value: Int?) {
        val v = value ?: Int.MAX_VALUE
        if (v >= diggCountMin.value) {
            ConfigManager.recommendedFeedFilter.diggCountMax.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setShareCountMin(value: Int?) {
        val v = value ?: 0
        if (v <= shareCountMax.value) {
            ConfigManager.recommendedFeedFilter.shareCountMin.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun setShareCountMax(value: Int?) {
        val v = value ?: Int.MAX_VALUE
        if (v >= shareCountMin.value) {
            ConfigManager.recommendedFeedFilter.shareCountMax.value = v
        } else {
            sendSnack(SnackEvent(R.string.save_failed_invalid_bounds))
        }
    }

    fun addTitleKeyword() {
        titleKeywords.value += ""
    }

    fun setTitleKeywordText(index: Int, text: String) {
        if (index !in titleKeywords.value.indices) {
            YLog.warn("$TAG title keyword index out of bounds: $index")
            return
        }
        titleKeywords.value.toMutableList().apply {
            this[index] = text
        }.also {
            titleKeywords.value = it
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (titleRegexMode.value && text.isNotBlank() &&
                runCatching {
                    Regex(text)
                }.isFailure
            ) {
                sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
                return@launch
            }
            persistTitleKeywords()
        }
    }

    fun removeTitleKeyword(index: Int) {
        if (index !in titleKeywords.value.indices) {
            YLog.warn("$TAG title keyword index out of bounds: $index")
            return
        }
        titleKeywords.value.toMutableList().apply {
            removeAt(index)
        }.also {
            titleKeywords.value = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            persistTitleKeywords()
        }
    }

    fun setTitleRegex(enable: Boolean) {
        if (enable &&
            titleKeywords.value.any { kw ->
                kw.isNotBlank() &&
                    runCatching {
                        Regex(kw)
                    }.isFailure
            }
        ) {
            sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
            return
        }
        ConfigManager.recommendedFeedFilter.titleRegexMode.value = enable
    }

    private fun persistTitleKeywords() {
        ConfigManager.recommendedFeedFilter.titleKeywords.value =
            titleKeywords.value.filter {
                it.isNotBlank()
            }.toSet()
    }

    fun addUidKeyword() {
        uidKeywords.value += ""
    }

    fun setUidKeywordText(index: Int, text: String) {
        if (index !in uidKeywords.value.indices) {
            YLog.warn("$TAG uid keyword index out of bounds: $index")
            return
        }
        uidKeywords.value.toMutableList().apply {
            this[index] = text
        }.also {
            uidKeywords.value = it
        }

        viewModelScope.launch(Dispatchers.IO) {
            persistUidKeywords()
        }
    }

    fun removeUidKeyword(index: Int) {
        if (index !in uidKeywords.value.indices) {
            YLog.warn("$TAG uid keyword index out of bounds: $index")
            return
        }
        uidKeywords.value.toMutableList().apply {
            removeAt(index)
        }.also {
            uidKeywords.value = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            persistUidKeywords()
        }
    }

    private fun persistUidKeywords() {
        ConfigManager.recommendedFeedFilter.authorUidKeywords.value =
            uidKeywords.value.filter {
                it.isNotBlank()
            }.toSet()
    }

    fun addNicknameKeyword() {
        nicknameKeywords.value += ""
    }

    fun setNicknameKeywordText(index: Int, text: String) {
        if (index !in nicknameKeywords.value.indices) {
            YLog.warn("$TAG nickname keyword index out of bounds: $index")
            return
        }
        nicknameKeywords.value.toMutableList().apply {
            this[index] = text
        }.also {
            nicknameKeywords.value = it
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (nicknameRegexMode.value && text.isNotBlank() &&
                runCatching {
                    Regex(text)
                }.isFailure
            ) {
                sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
                return@launch
            }
            persistNicknameKeywords()
        }
    }

    fun removeNicknameKeyword(index: Int) {
        if (index !in nicknameKeywords.value.indices) {
            YLog.warn("$TAG nickname keyword index out of bounds: $index")
            return
        }
        nicknameKeywords.value.toMutableList().apply {
            removeAt(index)
        }.also {
            nicknameKeywords.value = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            persistNicknameKeywords()
        }
    }

    fun setNicknameRegex(enable: Boolean) {
        if (enable &&
            nicknameKeywords.value.any { kw ->
                kw.isNotBlank() &&
                    runCatching {
                        Regex(kw)
                    }.isFailure
            }
        ) {
            sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
            return
        }
        ConfigManager.recommendedFeedFilter.authorNicknameRegexMode.value = enable
    }

    private fun persistNicknameKeywords() {
        ConfigManager.recommendedFeedFilter.authorNicknameKeywords.value = nicknameKeywords.value.filter {
            it.isNotBlank()
        }.toSet()
    }

    fun addDescKeyword() {
        descKeywords.value += ""
    }

    fun setDescKeywordText(index: Int, text: String) {
        if (index !in descKeywords.value.indices) {
            YLog.warn("$TAG desc keyword index out of bounds: $index")
            return
        }
        descKeywords.value.toMutableList().apply {
            this[index] = text
        }.also {
            descKeywords.value = it
        }

        viewModelScope.launch(Dispatchers.IO) {
            if (descRegexMode.value && text.isNotBlank() &&
                runCatching {
                    Regex(text)
                }.isFailure
            ) {
                sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
                return@launch
            }
            persistDescKeywords()
        }
    }

    fun removeDescKeyword(index: Int) {
        if (index !in descKeywords.value.indices) {
            YLog.warn("$TAG desc keyword index out of bounds: $index")
            return
        }
        descKeywords.value.toMutableList().apply {
            removeAt(index)
        }.also {
            descKeywords.value = it
        }
        viewModelScope.launch(Dispatchers.IO) {
            persistDescKeywords()
        }
    }

    fun setDescRegex(enable: Boolean) {
        if (enable && descKeywords.value.any { kw ->
                kw.isNotBlank() &&
                    runCatching {
                        Regex(kw)
                    }.isFailure
            }
        ) {
            sendSnack(SnackEvent(R.string.save_failed_invalid_regex))
            return
        }
        ConfigManager.recommendedFeedFilter.descRegexMode.value = enable
    }

    private fun persistDescKeywords() {
        ConfigManager.recommendedFeedFilter.descKeywords.value = descKeywords.value.filter {
            it.isNotBlank()
        }.toSet()
    }

    companion object {
        private val TAG = this::class.java.enclosingClass?.simpleName ?: "RecommendedFeedFilterViewModel"
    }
}
