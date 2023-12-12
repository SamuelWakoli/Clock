package com.samwrotethecode.clock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.samwrotethecode.clock.ui.presentation.viewmodels.AppViewModelProvider
import com.samwrotethecode.clock.ui.presentation.alarm_screen.AlarmScreen
import com.samwrotethecode.clock.ui.presentation.viewmodels.AlarmViewModel

@Composable
fun NavGraph(navController: NavHostController) {
    /**
     * This is a shared ViewModel that ensures consistency of runtime data
     */
    val alarmViewModel: AlarmViewModel = viewModel(factory = AppViewModelProvider.Factory)

    NavHost(navController = navController, startDestination = Screens.AlarmScreen.route) {
        composable(route = Screens.AlarmScreen.route) {
            AlarmScreen(
                navController = navController,
                viewModel = alarmViewModel,
            )
        }
    }
}

@Preview
@Composable
private fun NavGraphPreview() {
    NavGraph(navController = rememberNavController())
}