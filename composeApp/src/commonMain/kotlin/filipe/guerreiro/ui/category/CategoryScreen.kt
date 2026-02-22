package filipe.guerreiro.ui.category

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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.TransactionType
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(
    onBackClick: () -> Unit,
    viewModel: CategoryViewModel = koinViewModel()
) {

    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Entradas", "Saídas")
    val selectedType = if (selectedTab == 0) TransactionType.INCOME else TransactionType.EXPENSE


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Categorias") },
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
                Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Categoria")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val filteredCategories = uiState.categories.filter { it.type == selectedType }

            if (filteredCategories.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Nenhuma categoria de ${tabs[selectedTab].lowercase()} cadastrada.",
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
                    items(filteredCategories, key = { it.id }) { category ->
                        CategoryItem(
                            category = category,
                            onDeleteClick = { viewModel.onShowDeleteDialog(category) },
                            onEditClick = { viewModel.onShowEditDialog(category) }
                        )
                    }
                }
            }
        }

        if (uiState.showAddDialog) {
            AddCategoryDialog(
                type = selectedType,
                name = uiState.dialogNameInput,
                onNameChange = viewModel::onDialogNameChange,
                onDismiss = viewModel::onDismissAddDialog,
                onConfirm = { viewModel.onConfirmAddDialog(selectedType) },
                errorMessage = uiState.dialogError
            )
        }

        if (uiState.showEditDialog) {
            EditCategoryDialog(
                type = selectedType,
                name = uiState.editDialogNameInput,
                onNameChange = viewModel::onEditDialogNameChange,
                onDismiss = viewModel::onDismissEditDialog,
                onConfirm = { viewModel.onConfirmEditDialog(selectedType) },
                errorMessage = uiState.editDialogError
            )
        }

        if (uiState.showDeleteDialog) {
            ConfirmDeleteCategoryDialog(
                name = uiState.deleteDialogName,
                onConfirm = viewModel::onConfirmDeleteDialog,
                onDismiss = viewModel::onDismissDeleteDialog
            )
        }
    }
}

@Composable
fun CategoryItem(
    category: Category,
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
                text = category.name,
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
fun AddCategoryDialog(
    type: TransactionType,
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?
) {
    val typeName = if (type == TransactionType.INCOME) "entrada" else "saída"

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
        title = { Text("Nova Categoria de $typeName") },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        onNameChange(it.text)
                    },
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
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
fun EditCategoryDialog(
    type: TransactionType,
    name: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    errorMessage: String?
) {
    val typeName = if (type == TransactionType.INCOME) "entrada" else "saída"

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
        title = { Text("Editar Categoria de $typeName") },
        text = {
            Column {
                OutlinedTextField(
                    value = textFieldValue,
                    onValueChange = { 
                        textFieldValue = it
                        onNameChange(it.text)
                    },
                    label = { Text("Nome da categoria") },
                    singleLine = true,
                    isError = errorMessage != null,
                    modifier = Modifier.fillMaxWidth()
                )
                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
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
fun ConfirmDeleteCategoryDialog(
    name: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Excluir categoria") },
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
