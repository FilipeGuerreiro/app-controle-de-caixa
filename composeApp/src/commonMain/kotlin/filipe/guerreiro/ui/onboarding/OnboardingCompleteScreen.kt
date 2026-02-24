package filipe.guerreiro.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

private const val AUTO_REDIRECT_DELAY_MS = 1000L

@Composable
fun OnboardingCompleteScreen(
    onGoToHome: () -> Unit,
) {
    // Animação: 1) círculo escala  2) check desenha  3) conteúdo aparece
    val circleProgress = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }
    var showContent by remember { mutableStateOf(false) }
    var showProgress by remember { mutableStateOf(false) }

    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(Unit) {
        circleProgress.animateTo(
            1f, animationSpec = tween(400, easing = FastOutSlowInEasing)
        )
        checkProgress.animateTo(
            1f, animationSpec = tween(500, easing = FastOutSlowInEasing)
        )
        delay(200)
        showContent = true
        delay(500)
        showProgress = true
        delay(AUTO_REDIRECT_DELAY_MS)
        onGoToHome()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ── Checkmark animado via Canvas ─────────
            AnimatedCheckmark(
                circleProgress = circleProgress.value,
                checkProgress = checkProgress.value,
                color = primaryColor,
                trackColor = surfaceVariantColor,
                modifier = Modifier.size(96.dp)
            )

            Spacer(Modifier.height(24.dp))

            // ── Texto principal ──────────────────────
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(tween(500)) + slideInVertically(
                    initialOffsetY = { it / 4 },
                    animationSpec = tween(500)
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tudo pronto! 🎉",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center,
                    )
                    Text(
                        text = "Seu caixa está configurado e pronto para usar.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Spacer(Modifier.height(40.dp))

        }
    }
}

// ── Checkmark desenhado com Canvas ─────────────────────────────────────────

@Composable
private fun AnimatedCheckmark(
    circleProgress: Float,
    checkProgress: Float,
    color: Color,
    trackColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val strokeWidth = size.minDimension * 0.07f
        val radius = (size.minDimension / 2f) - strokeWidth
        val center = Offset(size.width / 2f, size.height / 2f)

        // Fundo do círculo (track)
        drawCircle(
            color = trackColor,
            radius = radius,
            center = center,
            style = Stroke(width = strokeWidth)
        )

        // Círculo animado (sweep)
        val sweepAngle = 360f * circleProgress
        drawArc(
            color = color,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
        )

        // Checkmark (✓) desenhado progressivamente
        if (checkProgress > 0f) {
            val checkPath = Path().apply {
                // Pontos do check proporcionais ao tamanho
                val startX = size.width * 0.28f
                val startY = size.height * 0.52f
                val midX = size.width * 0.44f
                val midY = size.height * 0.66f
                val endX = size.width * 0.72f
                val endY = size.height * 0.38f

                moveTo(startX, startY)
                lineTo(midX, midY)
                lineTo(endX, endY)
            }

            // Medir o path e recortar com base no progresso
            val pathMeasure = PathMeasure()
            pathMeasure.setPath(checkPath, false)
            val totalLength = pathMeasure.length

            val partialPath = Path()
            pathMeasure.getSegment(
                startDistance = 0f,
                stopDistance = totalLength * checkProgress,
                destination = partialPath,
                startWithMoveTo = true
            )

            drawPath(
                path = partialPath,
                color = color,
                style = Stroke(
                    width = strokeWidth * 1.2f,
                    cap = StrokeCap.Round
                )
            )
        }
    }
}
