package io.github.twyora.douyinenhancer.ui.content.home

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.res.stringResource
import io.github.twyora.douyinenhancer.BuildConfig
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.ui.components.PreferenceCategory
import io.github.twyora.douyinenhancer.ui.components.PreferenceItem
import io.github.twyora.douyinenhancer.ui.components.SwitchPreferenceItem
import java.text.SimpleDateFormat

@Composable
fun HomeContent(uiState: HomeContentUiState, modifier: Modifier = Modifier, uiActions: HomeContentUiActions = HomeContentUiActions()) {
    LazyColumn(modifier = modifier) {
        item {
            PreferenceCategory(title = stringResource(R.string.pref_category_settings))
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_settings_open_module_settings_title),
                summary = stringResource(R.string.pref_settings_open_module_settings_summary)
            ) {
                uiActions.onNavigateToSettings()
            }
        }
        item {
            SwitchPreferenceItem(
                title = stringResource(R.string.pref_settings_hide_launcher_icon_title),
                summary = stringResource(R.string.pref_settings_hide_launcher_icon_summary),
                checked = uiState.launcherIconHidden
            ) {
                uiActions.onLauncherIconChange(it)
            }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_settings_main_help_title),
                summary = stringResource(R.string.pref_settings_main_help_summary)
            ) {
                uiActions.onOpenHelp()
            }
        }
        item {
            PreferenceCategory(title = stringResource(R.string.pref_category_about))
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_version_title),
                summary = BuildConfig.VERSION_NAME + if (uiState.updateState.latestVersionName != BuildConfig.VERSION_NAME) {
                    " (${uiState.updateState.latestVersionName})"
                } else {
                    ""
                }
            )
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_author_title),
                summary = stringResource(R.string.pref_about_author_summary)
            ) {
                uiActions.onOpenAuthor()
            }
        }
        item {
            PreferenceItem(
                title = if (BuildConfig.VERSION_NAME == uiState.updateState.latestVersionName) {
                    stringResource(R.string.pref_about_up_to_date_title)
                } else {
                    stringResource(R.string.pref_about_update_available_title)
                },
                summary = uiState.updateState.releaseBody?.takeIf {
                    it.isNotBlank()
                }?.let {
                    if (it.length > 80) {
                        it.take(80) + "..."
                    } else {
                        it
                    }
                } ?: stringResource(R.string.pref_about_update_available_summary)
            ) {
                uiActions.onViewRelease()
            }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_repository_title),
                summary = stringResource(R.string.pref_about_repository_summary)
            ) {
                uiActions.onOpenRepository()
            }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_telegram_title),
                summary = stringResource(R.string.pref_about_telegram_summary)
            ) {
                uiActions.onJoinTelegramGroup()
            }
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_build_time_title),
                summary = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss",
                    LocalLocale.current.platformLocale
                ).format(BuildConfig.BUILD_TIMESTAMP)
            )
        }
        item {
            PreferenceItem(
                title = stringResource(R.string.pref_about_back_then_title),
                summary = stringResource(R.string.pref_about_back_then_summary)
            )
        }
        item {
            PreferenceItem(
                title = if (uiState.moduleActivationState) {
                    stringResource(R.string.pref_about_activation_status_activated_title)
                } else {
                    stringResource(R.string.pref_about_activation_status_deactivated_title)
                },
                summary = if (uiState.moduleActivationState) {
                    stringResource(R.string.pref_about_activation_status_activated_summary)
                } else {
                    stringResource(R.string.pref_about_activation_status_deactivated_summary)
                }
            )
        }
    }
}
