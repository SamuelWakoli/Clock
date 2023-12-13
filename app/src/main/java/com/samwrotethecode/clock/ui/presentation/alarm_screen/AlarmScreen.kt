package com.samwrotethecode.clock.ui.presentation.alarm_screen

import android.text.format.DateFormat
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.AlarmOff
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.samwrotethecode.clock.data.AlarmDatabaseItem
import com.samwrotethecode.clock.ui.presentation.app_composables.AlarmListItem
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmScreen(
    navController: NavController,
    viewModel: AlarmViewModel,
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val alarmsList = viewModel.alarmsUiState.collectAsState().value.alarms
    val context = LocalContext.current
    var is24HourFormat by rememberSaveable {
        mutableStateOf(DateFormat.is24HourFormat(context))
    }
    var showAddAlarmDialog by rememberSaveable {
        mutableStateOf(false)
    }


    Scaffold(
        topBar = {
            AlarmScreenAppbar(
                scrollBehavior = scrollBehavior,
                is24HourFormat = is24HourFormat,
                onClickUpdate24Hour = {
                    is24HourFormat = !is24HourFormat
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAlarmDialog = true }) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
            }
        }
    ) { paddingValues: PaddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
        ) {
            if (alarmsList.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.AlarmOff,
                        contentDescription = null,
                        modifier = Modifier.size(92.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(
                        text = "No alarm added",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = buildAnnotatedString {
                            append("Click ")
                            withStyle(style = SpanStyle(fontSize = MaterialTheme.typography.titleMedium.fontSize)) {
                                append("+")
                            }
                            append(" to add an alarm")
                        },
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                }
            } else {
                LazyColumn {
                    items(alarmsList) { alarm: AlarmDatabaseItem ->
                        AlarmListItem(
                            alarm = alarm,
                            viewModel = viewModel,
                            is24HourFormat = is24HourFormat
                        )
                    }
                }
            }

            if (showAddAlarmDialog) AddAlarmDialog(
                is24Hour = is24HourFormat,
                onDismissRequest = { showAddAlarmDialog = false },
                viewModel = viewModel,
            )
        }
    }
}

@Preview
@Composable
private fun AlarmScreenPreview() {
    AlarmScreen(
        navController = rememberNavController(),
        viewModel = viewModel(factory = AppViewModelProvider.Factory)
    )
}