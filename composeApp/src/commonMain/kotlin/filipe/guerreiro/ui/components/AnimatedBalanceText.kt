package filipe.guerreiro.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.TwoWayConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyString

@Composable
fun AnimatedBalanceText(
    isLoading: Boolean,
    value: Long?,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    ),
    skeletonBoxWidth: Dp = 100.dp,
    skeletonBoxHeight: Dp = 24.dp,
    boxWidth: Dp = 120.dp

) {
    Box(
        modifier = modifier.width(boxWidth)
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "BalanceTransition"
        ) { loading ->

            if (loading) {
                SkeletonBox(width = skeletonBoxWidth, height = skeletonBoxHeight)
            } else {
                val balanceAnimatable = remember { Animatable(0L, LongToVector) }
                
                LaunchedEffect(value) {
                    balanceAnimatable.animateTo(
                        targetValue = value ?: 0L,
                        animationSpec = tween(500)
                    )
                }

                Text(
                    text = balanceAnimatable.value.toCurrencyString(),
                    style = style
                )
            }
        }
    }
}

val LongToVector: TwoWayConverter<Long, AnimationVector1D> = TwoWayConverter(
    convertToVector = { AnimationVector1D(it.toFloat()) },
    convertFromVector = { it.value.toLong() }
)