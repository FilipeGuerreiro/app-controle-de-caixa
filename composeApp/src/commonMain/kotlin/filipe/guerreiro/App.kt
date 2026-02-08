package filipe.guerreiro

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import filipe.guerreiro.ui.components.BottomNavigationBar
import filipe.guerreiro.ui.navigation.AppNavHost
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme

@Composable
@Preview
fun App() {
    val navController = rememberNavController()

    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

    val shouldHideBottomBar = when (currentRoute) {
        "start",
        "userSelection",
        "register" -> true
         else -> false
    }

    ControleDeCaixaTheme {
        Scaffold(
            bottomBar = {
                if (!shouldHideBottomBar) {
                    BottomNavigationBar(navController)
                }
            }
        ) {
            paddingValues ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}