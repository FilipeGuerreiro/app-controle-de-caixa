package filipe.guerreiro.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import filipe.guerreiro.ui.cash.CashScreen
import filipe.guerreiro.ui.menu.MenuScreen
import filipe.guerreiro.ui.home.HomeScreen
import filipe.guerreiro.ui.opening.OpeningScreen
import filipe.guerreiro.ui.register.RegisterScreen
import filipe.guerreiro.ui.start.AppStartScreen
import filipe.guerreiro.ui.userselection.UserSelectionScreen


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
          composable("opening") {
              OpeningScreen()
          }
          composable("userSelection") {
              UserSelectionScreen(navController)
          }

          composable(BottomNavItem.Home.route) {
              HomeScreen(
                  onOpenCashClick = { navController.navigate("opening") }
              )
          }
          composable(BottomNavItem.Cash.route) {
              CashScreen()
          }
          composable(BottomNavItem.More.route) {
              MenuScreen()
          }
      }
}
