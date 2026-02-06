package filipe.guerreiro.ui.cash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme



@Composable
fun CashScreen() {
    Scaffold(
        topBar = {
            CashTopBar()
        }
    ) {
        paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
            Text("Tela do Caixa (vazia)", style = MaterialTheme.typography.titleMedium)
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun CashScreenPreview() {
    ControleDeCaixaTheme {
        Column {
            CashTopBar()
            CashScreen()
        }
    }
}
