package filipe.guerreiro.ui.menu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import filipe.guerreiro.domain.session.SessionManager
import filipe.guerreiro.ui.navigation.BottomNavItem
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun MenuScreen(
    navController: NavController,
    sessionManager: SessionManager = koinInject()
) {
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Menu",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        // Galeria de cores
        MenuItem(
            icon = Icons.Default.Palette,
            text = "Galeria de cores",
            onClick = { navController.navigate("colorGallery") }
        )

        // Métodos de Pagamento
        MenuItem(
            icon = Icons.Default.CreditCard,
            text = "Métodos de Pagamento",
            onClick = { navController.navigate("paymentMethods") }
        )

        // Categorias
        MenuItem(
            icon = Icons.Default.Category,
            text = "Categorias",
            onClick = { navController.navigate("categories") }
        )

        // Logout / Trocar de perfil
        MenuItem(
            icon = Icons.AutoMirrored.Filled.Logout,
            text = "Trocar de perfil",
            onClick = {
                scope.launch {
                    sessionManager.logout()
                    navController.navigate("userSelection") {
                        // Limpa todo o back stack para evitar voltar a telas autenticadas
                        popUpTo(0) { inclusive = true }
                    }
                }
            },
            tintColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
private fun MenuItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    tintColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        tonalElevation = 1.dp,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tintColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyLarge,
                color = tintColor
            )
        }
    }
}