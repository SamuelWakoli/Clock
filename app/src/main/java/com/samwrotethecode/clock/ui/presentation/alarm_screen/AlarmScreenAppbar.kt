package com.samwrotethecode.clock.ui.presentation.alarm_screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAlarm
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.PlainTooltipBox
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.samwrotethecode.clock.AppViewModelProvider
import com.samwrotethecode.clock.ui.navigation.Screens
import com.samwrotethecode.clock.ui.presentation.app_composables.CircularCheckbox
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreenAppbar(
    scrollBehavior: TopAppBarScrollBehavior,
    navController: NavController,
    viewModel: AlarmViewModel,
    is24HourFormat: Boolean,
    onClickUpdate24Hour: () -> Unit,
) {
    var showDropMenu by remember {
        mutableStateOf(false)
    }

    TopAppBar(
        title = { Text(text = "Alarm") },
        actions = {
            PlainTooltipBox(tooltip = { Text(text = "Add alarm") }) {
                IconButton(
                    onClick = {
                        navController.navigate(Screens.AddAlarmScreen.route) {
                            viewModel.setCurrentAlarm(null)
                        }
                    }) {
                    Icon(imageVector = Icons.Default.AddAlarm, contentDescription = null)
                }
            }
            PlainTooltipBox(tooltip = { Text(text = "More") }) {
                IconButton(onClick = { showDropMenu = true }) {
                    Icon(imageVector = Icons.Default.AddAlarm, contentDescription = null)
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
                    }
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
            navController = rememberNavController(),
            viewModel = viewModel(factory = AppViewModelProvider.Factory),
            is24HourFormat = false,
            onClickUpdate24Hour = {}
        )
    }) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues)
        ) {

        }
    }

}