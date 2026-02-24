package filipe.guerreiro.ui.transaction

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.Category
import filipe.guerreiro.domain.model.PaymentMethod
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import filipe.guerreiro.ui.theme.financial

import filipe.guerreiro.ui.components.CurrencyAmountInput
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun TransactionScreen(
    onBackClick: () -> Unit,
    onTransactionSaved: () -> Unit,
    viewModel: TransactionViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isSaved) {
        if (state.isSaved) {
            onTransactionSaved()
            viewModel.resetSaveState()
        }
    }

    TransactionContent(
        state = state,
        onBackClick = onBackClick,
        onSaveClick = viewModel::saveTransaction,
        onAmountChange = viewModel::onAmountChange,
        onDescriptionChange = viewModel::onDescriptionChange,
        onTypeChange = viewModel::onTypeChange,
        onCategorySelected = viewModel::onCategorySelected,
        onPaymentMethodSelected = viewModel::onPaymentMethodSelected
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionContent(
    state: TransactionUiState,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit,
    onAmountChange: (Long) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onTypeChange: (TransactionType) -> Unit,
    onCategorySelected: (Category) -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit
) {
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Novo Lançamento") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    scrolledContainerColor = Color.Unspecified,
                    navigationIconContentColor = Color.Unspecified,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    actionIconContentColor = Color.Unspecified
                )
            )
        },
        bottomBar = {
            TransactionBottomBar(onSaveClick = onSaveClick, modifier = Modifier.imePadding())
        }
    ) { paddingValues ->
        val focusManager = LocalFocusManager.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp), // Increased horizontal padding
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Transaction Type Selector
            TransactionTypeSelector(
                selectedType = state.type,
                onTypeSelected = onTypeChange
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Amount Input
            val amountColor = if (state.type == TransactionType.INCOME) {
                MaterialTheme.financial.profit
            } else {
                MaterialTheme.colorScheme.error
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Valor da transação",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                CurrencyAmountInput(
                    amountInCents = state.amountInCents,
                    onAmountChange = onAmountChange,
                    textColor = amountColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (state.showValidationErrors && state.amountInCents <= 0L) {
                    Text(
                        text = "Valor deve ser maior que zero.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Form Fields
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                
                // Description
                TransactionTextField(
                    value = state.description,
                    onValueChange = onDescriptionChange,
                    label = "Descrição (Opcional)",
                    icon = Icons.AutoMirrored.Filled.Notes,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    )
                )

                // Category Selection
                val filteredCategories = state.categories.filter { it.type == state.type }
                val isCategoryError = state.showValidationErrors && state.selectedCategory == null
                DropdownSelector(
                    label = "Categoria *",
                    icon = Icons.Default.Category,
                    options = filteredCategories,
                    selectedOption = state.selectedCategory,
                    onOptionSelected = onCategorySelected,
                    itemLabel = { it.name },
                    isError = isCategoryError,
                    supportingText = if (isCategoryError) "Selecione uma categoria" else null
                )

                // Payment Method Selection
                val isPaymentError = state.showValidationErrors && state.selectedPaymentMethod == null
                DropdownSelector(
                    label = "Método de Pagamento *",
                    icon = Icons.Default.CreditCard,
                    options = state.paymentMethods,
                    selectedOption = state.selectedPaymentMethod,
                    onOptionSelected = onPaymentMethodSelected,
                    itemLabel = { it.name },
                    isError = isPaymentError,
                    supportingText = if (isPaymentError) "Selecione um método de pagamento" else null
                )
            }

            Spacer(modifier = Modifier.height(100.dp)) // Spacing for bottom bar
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun TransactionScreenPreview() {
    ControleDeCaixaTheme {
        TransactionContent(
            state = TransactionUiState(
                amountInCents = 15000L,
                description = "Venda de Espetinhos",
                type = TransactionType.INCOME,
                categories = listOf(
                    Category(1, 1, "Vendas", TransactionType.INCOME),
                    Category(2, 1, "Insumos", TransactionType.EXPENSE)
                ),
                paymentMethods = listOf(
                    PaymentMethod(1, 1, "Dinheiro"),
                    PaymentMethod(2, 1, "Pix")
                )
            ),
            onBackClick = {},
            onSaveClick = {},
            onAmountChange = {},
            onDescriptionChange = {},
            onTypeChange = {},
            onCategorySelected = {},
            onPaymentMethodSelected = {}
        )
    }
}

@Composable
fun TransactionBottomBar(
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Button(
            onClick = onSaveClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Salvar Lançamento",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}

@Composable
fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TypeButton(
            text = "Entrada",
            isSelected = selectedType == TransactionType.INCOME,
            selectedColor = MaterialTheme.financial.profitContainer,
            selectedContentColor = MaterialTheme.financial.onProfitContainer,
            onClick = { onTypeSelected(TransactionType.INCOME) },
            modifier = Modifier.weight(1f)
        )
        TypeButton(
            text = "Saída",
            isSelected = selectedType == TransactionType.EXPENSE,
            selectedColor = MaterialTheme.colorScheme.errorContainer,
            selectedContentColor = MaterialTheme.colorScheme.onErrorContainer,
            onClick = { onTypeSelected(TransactionType.EXPENSE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun TypeButton(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    selectedContentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        if (isSelected) selectedColor else Color.Transparent
    )
    val contentColor by animateColorAsState(
        if (isSelected) selectedContentColor else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(visible = isSelected) {
                Icon(
                    imageVector = if (text == "Entrada") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.padding(end = 8.dp).size(18.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                color = contentColor
            )
        }
    }
}


@Composable
fun TransactionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    modifier: Modifier = Modifier
) {
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(text = value, selection = TextRange(value.length)))
    }

    LaunchedEffect(value) {
        if (value != textFieldValue.text) {
            textFieldValue = TextFieldValue(text = value, selection = TextRange(value.length))
        }
    }

    OutlinedTextField(
        value = textFieldValue,
        onValueChange = { 
            textFieldValue = it
            onValueChange(it.text)
        },
        label = { Text(label) },
        leadingIcon = { Icon(icon, contentDescription = null) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline
        ),
        keyboardOptions = keyboardOptions,
        singleLine = true
    )
}

@Composable
fun <T> DropdownSelector(
    label: String,
    icon: ImageVector,
    options: List<T>,
    selectedOption: T?,
    onOptionSelected: (T) -> Unit,
    itemLabel: (T) -> String,
    isError: Boolean = false,
    supportingText: String? = null
) {
    var expanded by remember { mutableStateOf(false) }
    val rotationState by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            OutlinedTextField(
                value = selectedOption?.let { itemLabel(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                leadingIcon = { Icon(icon, contentDescription = null) },
                trailingIcon = {
                    Icon(
                        Icons.Default.ArrowDropDown, 
                        null,
                        modifier = Modifier.clickable { expanded = !expanded } // Handle click on icon too
                            .rotate(rotationState)
                    )
                },
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = false,
                isError = isError,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                    disabledLeadingIconColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLabelColor = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledContainerColor = Color.Transparent
                )
            )
            
            if (isError && supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
        
        // Transparent overlay to capture clicks
        Box(
            modifier = Modifier
                .matchParentSize()
                .clip(RoundedCornerShape(12.dp))
                .clickable { expanded = !expanded }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.85f) // Adjust width relative to parent if possible, or just default
        ) {
            if (options.isEmpty()) {
                DropdownMenuItem(
                    text = { Text("Nenhuma opção disponível", fontStyle = FontStyle.Italic) },
                    onClick = { expanded = false }
                )
            } else {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = itemLabel(option),
                                style = MaterialTheme.typography.bodyLarge
                            ) 
                        },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
