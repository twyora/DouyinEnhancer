package io.github.twyora.douyinenhancer.ui.content.attest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.github.twyora.douyinenhancer.R

@Composable
fun VerifyDialogContent(state: VerifyDialogUiState, actions: VerifyDialogUiActions, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .imePadding(),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .widthIn(max = 560.dp)
                .heightIn(max = 560.dp)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.verify_dialog_title),
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = stringResource(R.string.verify_dialog_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    TextField(
                        value = state.inputUrl,
                        onValueChange = {
                            actions.onInputUrlChange(it)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(stringResource(R.string.verify_dialog_input_hint))
                        }
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = actions.onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(android.R.string.cancel))
                    }
                    TextButton(
                        onClick = {
                            actions.onConfirm(state.inputUrl)
                        },
                        enabled = state.inputUrl.isNotBlank(),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(android.R.string.ok))
                    }
                }
            }
        }
    }
}
