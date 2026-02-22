package filipe.guerreiro.ui.paymentmethod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.PaymentMethod
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentMethodScreen(
    onBackClick: () -> Unit,
    onNavigateToUserSelection: () -> Unit,
    viewModel: PaymentMethodViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isLogged) {
        if (!uiState.isLogged) {
            onNavigateToUserSelection()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Métodos de Pagamento") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = viewModel::onShowAddDialog,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Método")
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.paymentMethods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhum método de pagamento cadastrado.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.paymentMethods, key = { it.id }) { method ->
                        PaymentMethodItem(
                            paymentMethod = method,
                            onDeleteClick = { viewModel.onShowDeleteDialog(method) },
                            onEditClick = { viewModel.onShowEditDialog(method) }
                        )
                    }
                }
            }

            if (uiState.showAddDialog) {
                AddPaymentMethodDialog(
                    name = uiState.dialogNameInput,
                    onNameChange = viewModel::onDialogNameChange,
                    onDismiss = viewModel::onDismissAddDialog,
                    onConfirm = viewModel::onConfirmAddDialog,
                    errorMessage = uiState.dialogError
                )
            }

            if (uiState.showEditDialog) {
                EditPaymentMethodDialog(
                    name = uiState.editDialogNameInput,
                    onNameChange = viewModel::onEditDialogNameChange,
                    onDismiss = viewModel::onDismissEditDialog,
                    onConfirm = viewModel::onConfirmEditDialog,
                    errorMessage = uiState.editDialogError
                )
            }

            if (uiState.showDeleteDialog) {
                ConfirmDeletePaymentMethodDialog(
                    name = uiState.deleteDialogName,
                    onConfirm = viewModel::onConfirmDeleteDialog,
                    onDismiss = viewModel::onDismissDeleteDialog
                )
            }
        }
    }
}

@Composable
fun PaymentMethodItem(
    paymentMethod: PaymentMethod,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = paymentMethod.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Deletar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
fun AddPaymentMethodDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }

    LaunchedEffect(name) {
        if (name != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = name, selection = TextRange(name.length))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Novo Método de Pagamento") },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        onNameChange(it.text)
                    },
                    label = { Text("Nome do método") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditPaymentMethodDialog(
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = name, selection = TextRange(name.length)))
    }

    LaunchedEffect(name) {
        if (name != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = name, selection = TextRange(name.length))
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Método de Pagamento") },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        onNameChange(it.text)
                    },
                    label = { Text("Nome do método") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth(),
                    supportingText = {
                        if (errorMessage != null) {
                            Text(text = errorMessage)
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun ConfirmDeletePaymentMethodDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir método de pagamento") },
        text = {
            Text("Tem certeza que deseja excluir \"$name\"?")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Excluir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
