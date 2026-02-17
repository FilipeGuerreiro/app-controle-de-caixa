package filipe.guerreiro.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ==============================================================================
// DOCUMENTAÇÃO PARA AGENTES DE UI (LLMs)
// Use este guia para escolher a cor correta em cada componente:
//
// 1. ESTRUTURA E NAVEGAÇÃO:
//    - TopAppBar, BottomNavigation, Botões de Confirmar -> Use [primary]
//    - Botões de Cancelar/Voltar, Bordas -> Use [outline] ou [secondary]
//
// 2. AÇÕES DE DESTAQUE (VENDAS):
//    - Botão Flutuante (FAB) "Nova Venda" -> Use [tertiary]
//    - Ícones de comida ou destaque visual -> Use [tertiary]
//
// 3. DADOS FINANCEIROS (IMPORTANTE):
//    - Valor monetário positivo (Entrada/Lucro) -> Use [MaterialTheme.financial.profit]
//    - Fundo de um card de lucro -> Use [MaterialTheme.financial.profitContainer]
//    - Valor monetário negativo (Saída/Prejuízo) -> Use [error]
// ==============================================================================

/**
 * Extensão de cores para o domínio financeiro.
 * Adiciona cores que não existem no Material 3 padrão.
 */
@Immutable
data class FinancialColors(
    val profit: Color = Color.Unspecified,
    val onProfit: Color = Color.Unspecified,
    val profitContainer: Color = Color.Unspecified,
    val onProfitContainer: Color = Color.Unspecified
)

// Cria o LocalProvider para as cores financeiras
val LocalFinancialColors = staticCompositionLocalOf { FinancialColors() }

private val LightColorScheme = lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
    surfaceDim = surfaceDimLight,
    surfaceBright = surfaceBrightLight,
    surfaceContainerLowest = surfaceContainerLowestLight,
    surfaceContainerLow = surfaceContainerLowLight,
    surfaceContainer = surfaceContainerLight,
    surfaceContainerHigh = surfaceContainerHighLight,
    surfaceContainerHighest = surfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
    surfaceDim = surfaceDimDark,
    surfaceBright = surfaceBrightDark,
    surfaceContainerLowest = surfaceContainerLowestDark,
    surfaceContainerLow = surfaceContainerLowDark,
    surfaceContainer = surfaceContainerDark,
    surfaceContainerHigh = surfaceContainerHighDark,
    surfaceContainerHighest = surfaceContainerHighestDark,
)

// Definição das cores financeiras Light/Dark
private val LightFinancialColors = FinancialColors(
    profit = profitLight,
    onProfit = onProfitLight,
    profitContainer = profitContainerLight,
    onProfitContainer = Color(0xFF003300) // Verde escuro para contraste no container claro
)

private val DarkFinancialColors = FinancialColors(
    profit = profitDark,
    onProfit = onProfitDark,
    profitContainer = profitContainerDark,
    onProfitContainer = Color(0xFFC8E6C9) // Verde claro para contraste no container escuro
)

@Composable
fun ControleDeCaixaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    // Seleciona as cores financeiras corretas
    val financialColors = if (darkTheme) DarkFinancialColors else LightFinancialColors

    // Injeta as cores financeiras na árvore de composição
    CompositionLocalProvider(LocalFinancialColors provides financialColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = AppTypography,
            content = content
        )
    }
}

// Atalho para acessar as cores financeiras de qualquer lugar no código
// Exemplo de uso: MaterialTheme.financial.profit
val MaterialTheme.financial: FinancialColors
    @Composable
    get() = LocalFinancialColors.current