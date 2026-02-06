package filipe.guerreiro.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Home: BottomNavItem("home", "Home", Icons.Filled.Home)
    object Cash : BottomNavItem("cash", "Caixa", Icons.Filled.Payments)
    object More : BottomNavItem("more", "Mais", Icons.Filled.Menu)

}