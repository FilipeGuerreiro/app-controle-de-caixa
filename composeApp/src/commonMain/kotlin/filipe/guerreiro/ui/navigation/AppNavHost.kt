package filipe.guerreiro.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import filipe.guerreiro.ui.theme.ColorGalleryScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = "start",
        modifier = modifier,
    ) {
        // Telas de onboarding/auth (sem animação especial - usam padrão)
        composable("start") {
            AppStartScreen(navController)
        }
        composable("register") {
            RegisterScreen(navController)
        }
        composable("opening") {
            OpeningScreen()
        }
        composable(
            route = "userSelection",
            ) {
            UserSelectionScreen(navController)
        }

        // Bottom Navigation - usa Fade Through para transições suaves entre tabs
        composable(
            route = BottomNavItem.Home.route,
        ) {
            HomeScreen(
                onOpenCashClick = { navController.navigate("opening") },
                onNavigateToUserSelection = { 
                    navController.navigate("userSelection") {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = BottomNavItem.Cash.route,
        ) {
            CashScreen(
                onNavigateToUserSelection = {
                    navController.navigate("userSelection") {
                        popUpTo(BottomNavItem.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(
            route = BottomNavItem.More.route,
        ) {
            MenuScreen(navController)
        }
        
        composable("colorGallery") {
            ColorGalleryScreen()
        }
    }
}
