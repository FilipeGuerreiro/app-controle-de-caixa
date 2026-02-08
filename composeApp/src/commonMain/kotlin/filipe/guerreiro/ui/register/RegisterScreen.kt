package filipe.guerreiro.ui.register

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun RegisterScreen(
    navController: NavController,
    viewModel: RegisterViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    RegisterScreenContent(
        state = state,
        onNameChange = viewModel::onNameChange,
        onBusinessNameChange = viewModel::onBusinessNameChange,
        onSubmit = {
            viewModel.register {
                navController.navigate("home") {
                    popUpTo("userSelection") { inclusive = true }
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreenContent(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onBusinessNameChange: (String) -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val nameError = state.showErrors && state.name.trim().isEmpty()
    val businessError = state.showErrors && state.businessName.trim().isEmpty()
    val canSubmit = !state.isLoading &&
        state.name.trim().isNotEmpty() &&
        state.businessName.trim().isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar perfil") }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cadastre seu nome e o nome do negócio para começar.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Seu nome") },
                placeholder = { Text("Ex.: Flávia Oliveira") },
                singleLine = true,
                isError = nameError,
                supportingText = {
                    if (nameError) {
                        Text("Informe seu nome.")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = state.businessName,
                onValueChange = onBusinessNameChange,
                label = { Text("Nome do negócio") },
                placeholder = { Text("Ex.: Loja Espetinhos") },
                singleLine = true,
                isError = businessError,
                supportingText = {
                    if (businessError) {
                        Text("Informe o nome do negócio.")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        if (canSubmit) onSubmit()
                    }
                ),
                modifier = Modifier.fillMaxWidth()
            )

            if (state.errorMessage != null) {
                Text(
                    text = state.errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSubmit,
                enabled = canSubmit,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Finalizar cadastro")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RegisterScreenPreview() {
    ControleDeCaixaTheme {
        RegisterScreenContent(
            state = RegisterUiState(
                name = "Flávia Oliveira",
                businessName = "Loja Espetinhos"
            ),
            onNameChange = {},
            onBusinessNameChange = {},
            onSubmit = {}
        )
    }
}
