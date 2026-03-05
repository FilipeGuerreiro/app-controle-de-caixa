package filipe.guerreiro.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.TransactionType
import filipe.guerreiro.ui.components.ControleDeCaixaTextField

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OnboardingCategoriesScreen(
    viewModel: OnboardingViewModel,
    onContinue: () -> Unit,
    onSkip: () -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val progress by animateFloatAsState(
        targetValue = 0.5f,
        animationSpec = tween(durationMillis = 600)
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Configuração inicial") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
                actions = {
                    TextButton(onClick = onSkip) {
                        Text("Pular", color = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            // ── Progress ─────────────────────────────────
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
            )

            Text(
                text = "Etapa 1 de 2",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                textAlign = TextAlign.End
            )

            Spacer(Modifier.height(20.dp))

            // ── Header ───────────────────────────────────
            Text(
                text = "Quais categorias você usa?",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Selecione as categorias que fazem sentido para o seu negócio. Você poderá editar depois.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Entradas ─────────────────────────────────
            SectionHeader(
                title = "💰 Entradas",
                subtitle = "Dinheiro que entra no caixa"
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.incomeCategories.forEach { cat ->
                    SuggestionChip(
                        label = cat.name,
                        isSelected = cat.isSelected,
                        onClick = { viewModel.toggleCategory(cat.name, TransactionType.INCOME) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.onShowCustomCategoryDialog(TransactionType.INCOME) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Adicionar entrada", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.height(24.dp))

            // ── Saídas ───────────────────────────────────
            SectionHeader(
                title = "📦 Saídas",
                subtitle = "Dinheiro que sai do caixa"
            )

            Spacer(Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                state.expenseCategories.forEach { cat ->
                    SuggestionChip(
                        label = cat.name,
                        isSelected = cat.isSelected,
                        onClick = { viewModel.toggleCategory(cat.name, TransactionType.EXPENSE) }
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedButton(
                onClick = { viewModel.onShowCustomCategoryDialog(TransactionType.EXPENSE) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(" Adicionar saída", style = MaterialTheme.typography.labelMedium)
            }

            Spacer(Modifier.weight(1f))

            // ── Continue Button ──────────────────────────
            Button(
                onClick = {
                    viewModel.saveCategories { onContinue() }
                },
                enabled = !state.isSaving,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(vertical = 16.dp)
                    .height(52.dp)
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Continuar",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                    )
                }
            }
        }
    }

    // ── Dialog: Categoria Personalizada ──────────────────
    if (state.showCustomCategoryDialog) {
        CustomItemDialog(
            title = if (state.customCategoryType == TransactionType.INCOME) "Nova entrada" else "Nova saída",
            name = state.customCategoryName,
            onNameChange = viewModel::onCustomCategoryNameChange,
            errorMessage = state.customCategoryError,
            onDismiss = viewModel::onDismissCustomCategoryDialog,
            onConfirm = viewModel::onConfirmCustomCategory
        )
    }
}

// ── Composables reutilizáveis ────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String, subtitle: String) {
    Row {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onBackground,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun SuggestionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(label, style = MaterialTheme.typography.bodyMedium)
        },
        leadingIcon = if (isSelected) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(FilterChipDefaults.IconSize)
                )
            }
        } else null,
        shape = RoundedCornerShape(12.dp),
    )
}

@Composable
fun CustomItemDialog(
    title: String,
    name: String,
    onNameChange: (String) -> Unit,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            ControleDeCaixaTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nome") },
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let {
                    { Text(it) }
                },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
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
