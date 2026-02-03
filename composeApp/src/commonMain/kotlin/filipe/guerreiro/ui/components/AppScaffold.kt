package filipe.guerreiro.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

val DEFAULT_NAV_ITEMS = listOf(
    BottomNavItem(route = "home", label = "Home", icon = Icons.Filled.Home),
    BottomNavItem(route = "cash", label = "Caixa", icon = Icons.Filled.Payments),
    BottomNavItem(route = "more", label = "Mais", icon = Icons.Filled.Menu)
)

@Composable
fun AppScaffold(
    selectedRoute: String = DEFAULT_NAV_ITEMS.first().route,
    onRouteChange: (String) -> Unit = {},
    modifier: Modifier = Modifier,
    topBar: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    var currentRoute by remember { mutableStateOf(selectedRoute) }

    Scaffold(
        modifier = modifier,
        topBar = topBar,
        bottomBar = {
            BottomNavigationBar(
                items = DEFAULT_NAV_ITEMS,
                selectedRoute = currentRoute,
                onItemSelected = { item ->
                    currentRoute = item.route
                    onRouteChange(item.route)
                }
            )
        }
    ) { paddingValues ->
        content()
    }
}
