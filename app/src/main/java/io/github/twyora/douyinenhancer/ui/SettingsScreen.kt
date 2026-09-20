package io.github.twyora.douyinenhancer.ui

import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.twyora.douyinenhancer.R
import io.github.twyora.douyinenhancer.bridge.ModuleApp
import io.github.twyora.douyinenhancer.config.ConfigManager

class SettingsScreen : AbsHostModuleActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ModuleApp.refreshResources()
        val appContext = object : ContextWrapper(this.applicationContext) {
            // Accessibility scans views and resolves module resources, which crashes
            override fun getResources() = ModuleApp.resources
        }
        val activityContext = object : ContextWrapper(this) {
            // Compose resolves module resources via the activity context and crashes
            override fun getResources() = ModuleApp.resources
            override fun getApplicationContext() = appContext
        }

        setContentView(
            ComposeView(activityContext).apply {
                setContent {
                    SettingsPage()
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Preview
    @Composable
    private fun SettingsPage() {
        MaterialTheme {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = {
                            Text(stringResource(R.string.settings_dialog_title))
                        }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    SettingsContent()
                }
            }
        }
    }

    @Composable
    private fun SettingsContent() {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SaveConfigsCategory()
            }
        }
    }

    @Composable
    private fun SaveConfigsCategory() {
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.pref_category_save),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_comment_image_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_comment_image_summary))
                    },
                    trailingContent = {
                        val purifyCommentImage by remember {
                            ConfigManager.save.purifyCommentImage.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.purifyCommentImage.value
                        )
                        Switch(
                            checked = purifyCommentImage,
                            onCheckedChange = {
                                ConfigManager.save.purifyCommentImage.value = it
                            }
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_comment_emoji_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_comment_emoji_summary))
                    },
                    trailingContent = {
                        val unlockCommentEmoji by remember {
                            ConfigManager.save.unlockCommentEmoji.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.unlockCommentEmoji.value
                        )
                        Switch(
                            checked = unlockCommentEmoji,
                            onCheckedChange = {
                                ConfigManager.save.unlockCommentEmoji.value = it
                            }
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_comment_audio_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_comment_audio_summary))
                    },
                    trailingContent = {
                        val downloadCommentAudio by remember {
                            ConfigManager.save.downloadCommentAudio.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.downloadCommentAudio.value
                        )
                        Switch(
                            checked = downloadCommentAudio,
                            onCheckedChange = {
                                ConfigManager.save.downloadCommentAudio.value = it
                            }
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_feed_video_remove_watermark_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_feed_video_remove_watermark_summary))
                    },
                    trailingContent = {
                        val feedVideoRemoveWatermark by remember {
                            ConfigManager.save.feedVideoRemoveWatermark.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.feedVideoRemoveWatermark.value
                        )
                        Switch(
                            checked = feedVideoRemoveWatermark,
                            onCheckedChange = {
                                ConfigManager.save.feedVideoRemoveWatermark.value = it
                            }
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_feed_download_bypass_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_feed_download_bypass_summary))
                    },
                    trailingContent = {
                        val feedDownloadBypass by remember {
                            ConfigManager.save.feedDownloadBypass.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.feedDownloadBypass.value
                        )
                        Switch(
                            checked = feedDownloadBypass,
                            onCheckedChange = {
                                ConfigManager.save.feedDownloadBypass.value = it
                            }
                        )
                    }
                )
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.pref_feed_multi_image_remove_watermark_title))
                    },
                    supportingContent = {
                        Text(stringResource(R.string.pref_feed_multi_image_remove_watermark_summary))
                    },
                    trailingContent = {
                        val feedMultiImageRemoveWatermark by remember {
                            ConfigManager.save.feedMultiImageRemoveWatermark.observe()
                        }.collectAsStateWithLifecycle(
                            initialValue = ConfigManager.save.feedMultiImageRemoveWatermark.value
                        )
                        Switch(
                            checked = feedMultiImageRemoveWatermark,
                            onCheckedChange = {
                                ConfigManager.save.feedMultiImageRemoveWatermark.value = it
                            }
                        )
                    }
                )
            }
        }
    }
}
