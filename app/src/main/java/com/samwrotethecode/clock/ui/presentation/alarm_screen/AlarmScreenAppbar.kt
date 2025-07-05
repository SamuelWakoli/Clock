package com.samwrotethecode.clock.ui.presentation.alarm_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.samwrotethecode.clock.ui.presentation.app_composables.CircularCheckbox

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreenAppbar(
    scrollBehavior: TopAppBarScrollBehavior,
    is24HourFormat: Boolean,
    onClickUpdate24Hour: () -> Unit,
    useKeyboard: Boolean,
    onClickUseKeyboard: () -> Unit,
) {
    var showDropMenu by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        title = { Text(text = "Alarm", color = MaterialTheme.colorScheme.primary) },
        actions = {
            IconButton(onClick = { showDropMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                DropdownMenu(
                    expanded = showDropMenu,
                    onDismissRequest = { showDropMenu = false }) {
                    DropdownMenuItem(
                        leadingIcon = {
                            CircularCheckbox(checked = is24HourFormat) {}
                        },
                        text = { Text(text = "24-hour") }, onClick = {
                            showDropMenu = false
                            onClickUpdate24Hour()
                        })
                    DropdownMenuItem(
                        leadingIcon = {
                            CircularCheckbox(checked = useKeyboard) {}
                        },
                        text = { Text(text = "Use keyboard") }, onClick = {
                            showDropMenu = false
                            onClickUseKeyboard()
                        })
                }
            }
        },
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun AlarmScreenAppbarPreview() {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()

    Scaffold(topBar = {
        AlarmScreenAppbar(
            scrollBehavior = scrollBehavior,
            is24HourFormat = false,
            useKeyboard = false,
            onClickUpdate24Hour = {},
            onClickUseKeyboard = {},
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

        }
    }

}