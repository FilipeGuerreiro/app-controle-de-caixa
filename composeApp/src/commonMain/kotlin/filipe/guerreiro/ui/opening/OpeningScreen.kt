package filipe.guerreiro.ui.opening

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import filipe.guerreiro.ui.components.BottomNavigationBar
import filipe.guerreiro.ui.components.BottomNavItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningScreen(
    viewModel: OpeningViewModel = koinViewModel()
) {
    val navItems = remember {
        listOf(
            BottomNavItem(route = "home", label = "Home", icon = Icons.Filled.Home),
            BottomNavItem(route = "cash", label = "Caixa", icon = Icons.Filled.Payments),
            BottomNavItem(route = "more", label = "Mais", icon = Icons.Filled.Menu)
        )
    }

    var selectedRoute by remember { mutableStateOf(navItems.first().route) }

    Scaffold (
        topBar = {
            TopAppBar(
                title = { Text("Abertura de Caixa") }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                items = navItems,
                selectedRoute = selectedRoute,
                onItemSelected = { selectedRoute = it.route }
            )
        }
    ){
        paddingValues ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp), // Valor de margem confortável para o uso na rua
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Quanto você tem de troco inicial?",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            //Campo de entrada de dinheiro

            OutlinedTextField(
                value = viewModel.initialAmount,
                onValueChange = { viewModel.onAmountChange(it) },
                label = { Text("Valor inical") },
                prefix = { Text("R$ ") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = MaterialTheme.shapes.medium
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = { viewModel.openCash() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), // Botão alto para facilitar o clique
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("ABRIR CAIXA AGORA", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}