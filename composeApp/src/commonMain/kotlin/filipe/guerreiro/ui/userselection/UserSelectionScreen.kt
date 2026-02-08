package filipe.guerreiro.ui.userselection

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import filipe.guerreiro.domain.model.User
import filipe.guerreiro.ui.theme.ControleDeCaixaTheme
import org.koin.compose.viewmodel.koinViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode

@Composable
fun UserSelectionScreen(
    navController: NavController,
    viewModel: UserSelectionViewModel = koinViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    UserSelectionContent(
        state = state,
        onUserSelected = { userId ->
            viewModel.selectUser(userId) {
                navController.navigate("home") {
                    popUpTo("userSelection") { inclusive = true }
                }
            }
        },
        onRetry = viewModel::loadUsers,
        gotoUserCreateScreen = {
            navController.navigate("register")
        }
    )
}

@Composable
fun UserSelectionContent(
    state: UserSelectionUiState,
    onUserSelected: (Long) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    gotoUserCreateScreen: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            HeaderBlock()

            Spacer(modifier = Modifier.height(18.dp))

            when {
                state.isLoading -> LoadingListPlaceholder()
                state.users.isEmpty() -> EmptyState(onRetry = onRetry, onAdd = gotoUserCreateScreen)
                else -> UserList(users = state.users, onUserSelected = onUserSelected, maxVisibleUsers = 5)
            }

            Spacer(modifier = Modifier.height(16.dp))

            AddNewAccountCard(onAdd = gotoUserCreateScreen)

            if (state.errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorMessage(message = state.errorMessage, onRetry = onRetry)
            }
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        color = MaterialTheme.colorScheme.surface,
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            UserInitialsAvatar(name = user.name)

            Column(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = user.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = user.businessName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Entrar",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.Rounded.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun UserList(users: List<User>, onUserSelected: (Long) -> Unit, maxVisibleUsers: Int = 5) {
    // Approximate item height based on paddings and content
    val itemHeight = 76.dp
    val spacing = 12.dp
    val maxHeight = itemHeight * maxVisibleUsers + spacing * (maxVisibleUsers - 1)

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = maxHeight),
        contentPadding = PaddingValues(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        items(users) { user ->
            UserItem(
                user = user,
                onClick = { onUserSelected(user.id) }
            )
        }
    }
}

@Composable
private fun HeaderBlock() {
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Bem-vindo de volta",
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Escolha um perfil para continuar.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LoadingListPlaceholder() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(3) {
            Surface(
                shape = RoundedCornerShape(18.dp),
                tonalElevation = 1.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(12.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                        Box(
                            modifier = Modifier
                                .width(110.dp)
                                .height(10.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(onRetry: () -> Unit, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Nenhum perfil encontrado",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Crie um novo perfil ou tente recarregar a lista.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onRetry, modifier = Modifier.height(44.dp)) {
                    Text("Recarregar")
                }
            }
        }
    }
}

@Composable
private fun ErrorMessage(message: String, onRetry: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Toque para tentar novamente.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            Text(
                text = "Recarregar",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.clickable { onRetry() }
            )
        }
    }
}

@Composable
private fun UserInitialsAvatar(name: String, modifier: Modifier = Modifier) {
    val initials = name
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(2.dp, color = MaterialTheme.colorScheme.surface, shape = CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials.ifEmpty { "" },
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun AddNewAccountCard(onAdd: () -> Unit) {
    val dashColor = MaterialTheme.colorScheme.outlineVariant
    val interactionSource = remember { MutableInteractionSource() }
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val strokeWidth = 2.dp.toPx()
                val cornerRadius = 18.dp.toPx()
                drawRoundRect(
                    color = dashColor,
                    style = Stroke(
                        width = strokeWidth,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f))
                    ),
                    cornerRadius = CornerRadius(cornerRadius, cornerRadius)
                )
            }
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onAdd
            ),
        shape = RoundedCornerShape(18.dp),
        color = Color.Transparent,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                tonalElevation = 2.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = "Adicionar nova conta",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UserSelectionPreview() {
    ControleDeCaixaTheme {
        UserSelectionContent(
            state = UserSelectionUiState(
                users = listOf(
                    User(id = 1, name = "Flávia Oliveira", businessName = "Loja Espetinhos"),
                    User(id = 2, name = "João Marcos", businessName = "Mercadinho Central"),
                    User(id = 3, name = "Mateus Oliveira", businessName = "Mercadinho Central"),
                    User(id = 4, name = "Roberto Carlos", businessName = "Mercadinho Central")
                ),
                isLoading = false
            ),
            onUserSelected = {},
            onRetry = {},
            gotoUserCreateScreen = {}
        )
    }
}
