package filipe.guerreiro.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavController

@Composable
fun NavigateOnce(
    navController: NavController,
    route: String
) {
    LaunchedEffect(route) {
        navController.navigate(route) {
            popUpTo("start") { inclusive = true }
        }
    }
}