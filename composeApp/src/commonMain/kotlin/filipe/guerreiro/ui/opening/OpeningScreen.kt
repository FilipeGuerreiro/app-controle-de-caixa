package filipe.guerreiro.ui.opening

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun OpeningScreen(
    viewModel: OpeningViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    OpeningScreenContent(
        state = state,
        onAmountChange = viewModel::onAmountChange,
        onSuggestedAmountClick = viewModel::resetToSuggestedAmount,
        onOpenSession = viewModel::openSession
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpeningScreenContent(
    state: OpeningUiState,
    onAmountChange: (String) -> Unit,
    onSuggestedAmountClick: () -> Unit,
    onOpenSession: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Abrir Novo Caixa") }) }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Informe o valor em caixa para iniciar as vendas",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = state.initialAmountInput,
                onValueChange = onAmountChange,
                label = { Text("Valor Inicial (R$)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                prefix = { Text("R$ ") }
            )

            // Botão para resetar para a sugestão do caixa anterior
            TextButton(onClick = onSuggestedAmountClick) {
                Text("Usar saldo do caixa anterior: ${state.suggestedAmount.toCurrencyString()}")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onOpenSession,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    Text("Iniciar Expediente")
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun OpeningScreenPreview() {
    ControleDeCaixaTheme {
        OpeningScreenContent(
            state = OpeningUiState(
                initialAmountInput = "R$ 150,00",
                suggestedAmount = 12000L,
                isLoading = false
            ),
            onAmountChange = {},
            onSuggestedAmountClick = {},
            onOpenSession = {}
        )
    }
}