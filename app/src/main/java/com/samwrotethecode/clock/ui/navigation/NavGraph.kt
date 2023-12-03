package com.samwrotethecode.clock.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screens.AlarmScreen.route) {
        // TODO: Add screens here
    }
}

@Preview
@Composable
private fun NavGraphPreview() {
    NavGraph(navController = rememberNavController())
}