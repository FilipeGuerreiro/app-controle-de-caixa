package filipe.guerreiro.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavBackStackEntry
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

// Constantes de duração (Material Motion specs)
private const val FADE_THROUGH_DURATION = 300
private const val SLIDE_DURATION = 350

// Rotas do Bottom Navigation (para detectar transições entre tabs)
private val bottomNavRoutes = setOf(
    BottomNavItem.Home.route,
    BottomNavItem.Cash.route,
    BottomNavItem.More.route,
)

// Fade Through (Material: entre destinos de mesmo nível / tabs)
private fun fadeThroughEnter(): EnterTransition =
    fadeIn(animationSpec = tween(durationMillis = FADE_THROUGH_DURATION, delayMillis = 90))

private fun fadeThroughExit(): ExitTransition =
    fadeOut(animationSpec = tween(durationMillis = 90))

// Slide Horizontal (Material: navegação forward/backward)
private fun slideInFromRight(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(SLIDE_DURATION)
    ) + fadeIn(animationSpec = tween(SLIDE_DURATION))

private fun slideOutToLeft(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(SLIDE_DURATION)
    ) + fadeOut(animationSpec = tween(SLIDE_DURATION))

private fun slideInFromLeft(): EnterTransition =
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth },
        animationSpec = tween(SLIDE_DURATION)
    ) + fadeIn(animationSpec = tween(SLIDE_DURATION))

private fun slideOutToRight(): ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(SLIDE_DURATION)
    ) + fadeOut(animationSpec = tween(SLIDE_DURATION))

// Helper: verifica se a transição é entre tabs do Bottom Nav
private fun isBottomNavTransition(
    initialRoute: String?,
    targetRoute: String?
): Boolean = initialRoute in bottomNavRoutes && targetRoute in bottomNavRoutes

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

        // Telas de onboarding/auth — Slide horizontal

        composable(
            route = "start",
            enterTransition = { fadeIn(animationSpec = tween(FADE_THROUGH_DURATION)) },
            exitTransition = { fadeOut(animationSpec = tween(FADE_THROUGH_DURATION)) },
        ) {
            AppStartScreen(navController)
        }

        composable(
            route = "register",
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            RegisterScreen(navController)
        }

        composable(
            route = "userSelection",
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            UserSelectionScreen(navController)
        }

        composable(
            route = "opening",
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            OpeningScreen()
        }

        // Bottom Navigation — Fade Through entre tabs, Slide horizontal quando vem de fora

        composable(
            route = BottomNavItem.Home.route,
            enterTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughEnter()
                } else {
                    slideInFromRight()
                }
            },
            exitTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughExit()
                } else {
                    slideOutToLeft()
                }
            },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
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
            enterTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughEnter()
                } else {
                    slideInFromRight()
                }
            },
            exitTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughExit()
                } else {
                    slideOutToLeft()
                }
            },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
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
            enterTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughEnter()
                } else {
                    slideInFromRight()
                }
            },
            exitTransition = {
                if (isBottomNavTransition(initialState.destination.route, targetState.destination.route)) {
                    fadeThroughExit()
                } else {
                    slideOutToLeft()
                }
            },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            MenuScreen(navController)
        }

        // Telas internas — Slide horizontal (push/pop)

        composable(
            route = "colorGallery",
            enterTransition = { slideInFromRight() },
            exitTransition = { slideOutToLeft() },
            popEnterTransition = { slideInFromLeft() },
            popExitTransition = { slideOutToRight() },
        ) {
            ColorGalleryScreen()
        }
    }
}
