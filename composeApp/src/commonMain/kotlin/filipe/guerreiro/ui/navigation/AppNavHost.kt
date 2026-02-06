package filipe.guerreiro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import filipe.guerreiro.ui.cash.CashScreen
import filipe.guerreiro.ui.menu.MenuScreen
import filipe.guerreiro.ui.home.HomeScreen
import filipe.guerreiro.ui.register.RegisterScreen
import filipe.guerreiro.ui.start.AppStartScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
      NavHost(
          navController = navController,
          startDestination = "start",
          modifier = modifier
      ) {
          composable("start") {
              AppStartScreen(navController)
          }
          composable("register") {
              RegisterScreen(navController)
          }
          composable(BottomNavItem.Home.route) {
              HomeScreen()
          }
          composable(BottomNavItem.Cash.route) {
              CashScreen()
          }
          composable(BottomNavItem.More.route) {
              MenuScreen()
          }
      }
}
