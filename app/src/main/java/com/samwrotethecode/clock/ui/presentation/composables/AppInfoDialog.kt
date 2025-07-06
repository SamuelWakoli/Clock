package com.samwrotethecode.clock.ui.presentation.composables

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.style.TextAlign

@Composable
fun AppInfoDialog(onDismissRequest: () -> Unit) {
    AlertDialog(
        icon = {
            Icon(Icons.Outlined.Info, contentDescription = null)
        },
        title = {
            Text("Important Information")
        },
        text = {
            Text(
                "This application demonstrates the UI and functionality of a local Room database. Audio configuration features may be added in future updates.",
                textAlign = TextAlign.Justify,
            )
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {},
        dismissButton = {
            TextButton(onDismissRequest) {
                Text("Close")
            }
        },
    )
}