package com.samwrotethecode.clock.ui.presentation.composables

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.samwrotethecode.clock.data.AlarmDatabaseItem


/** TODO: Add [AlarmDatabaseItem] parameter*/
@Composable
fun EditAlarmToneDialog(
    //alarmDatabaseItem: AlarmDatabaseItem,
    onDismissRequest: () -> Unit,
) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Card(
            elevation = CardDefaults.elevatedCardElevation(
                defaultElevation = 16.dp
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
            ) {
                Text(
                    text = "Alarm Tone",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.size(8.dp))

                Column(
                    modifier = Modifier
                        .heightIn(min = 260.dp, max = 400.dp)
                        .widthIn(min = 260.dp, max = 400.dp)
                ) {
                    LazyColumn {
                        items(count = 20) {
                            AlarmToneItemUi(label = "Alarm $it", selected = true) {
                                // TODO
                                onDismissRequest()
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.size(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .clickable { }
                        .fillMaxWidth()
                        .padding(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Custom alarm tone", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditAlarmToneDialogPreview() {
    MaterialTheme {
        Surface {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                EditAlarmToneDialog(
                    onDismissRequest = {}
                )
            }
        }
    }
}

@Composable
fun AlarmToneItemUi(
    label: String,
    selected: Boolean,
    onClick: (Boolean) -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(horizontal = 4.dp, vertical = 2.dp)
            .clickable { onClick(!selected) }
            .fillMaxWidth(),
    ) {
        RadioButton(selected = selected, onClick = { onClick(!selected) })
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.size(8.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun AlarmToneItemUiPreview() {
    AlarmToneItemUi(
        label = "Nairobi",
        selected = true,
        onClick = {},
    )
}