package filipe.guerreiro.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Creates a shimmering brush effect for skeleton loading.
 */
@Composable
fun shimmerBrush(
    baseColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    highlightColor: Color = MaterialTheme.colorScheme.surface
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    return Brush.linearGradient(
        colors = listOf(baseColor, highlightColor, baseColor),
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * A skeleton placeholder box with shimmer effect.
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    width: Dp? = null,
    height: Dp = 16.dp,
    shape: RoundedCornerShape = RoundedCornerShape(4.dp)
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .then(if (width != null) Modifier.width(width) else Modifier.fillMaxWidth())
            .height(height)
            .clip(shape)
            .background(brush)
    )
}

/**
 * A circular skeleton placeholder with shimmer effect.
 */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val brush = shimmerBrush()
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(brush)
    )
}

/**
 * Skeleton loader for the header section.
 */
@Composable
fun HomeHeaderSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SkeletonBox(width = 200.dp, height = 24.dp)
        Spacer(modifier = Modifier.height(12.dp))
        SkeletonBox(width = 120.dp, height = 14.dp)
    }
}

/**
 * Skeleton loader for the cash status card.
 */
@Composable
fun CashStatusCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top row: icon + status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                SkeletonCircle(size = 40.dp)
                Spacer(modifier = Modifier.width(12.dp))
                SkeletonBox(width = 120.dp, height = 20.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meta diária
            SkeletonBox(width = 200.dp, height = 18.dp)

            Spacer(modifier = Modifier.height(24.dp))

            // Entradas e Saídas
            Row(modifier = Modifier.fillMaxWidth()) {
                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 18.dp
                )
                Spacer(modifier = Modifier.width(12.dp))
                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 18.dp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botões
            Row(modifier = Modifier.fillMaxWidth()) {
                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    shape = RoundedCornerShape(8.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                SkeletonBox(
                    modifier = Modifier.weight(1f),
                    height = 44.dp,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Skeleton loader for a single quick action card.
 */
@Composable
fun QuickActionCardSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 32.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                SkeletonBox(width = 60.dp, height = 14.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(width = 40.dp, height = 18.dp)
            }
        }
    }
}

/**
 * Skeleton loader for the Quick Actions section.
 */
@Composable
fun QuickActionSectionSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 4
) {
    Column(modifier = modifier) {
        SkeletonBox(width = 120.dp, height = 18.dp)
        Spacer(modifier = Modifier.height(12.dp))
        
        // Grid de 2 colunas
        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)) {
            repeat((itemCount + 1) / 2) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCardSkeleton(modifier = Modifier.weight(1f))
                    if (it * 2 + 1 < itemCount) {
                        QuickActionCardSkeleton(modifier = Modifier.weight(1f))
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/**
 * Skeleton loader for a single activity item.
 */
@Composable
fun ActivityItemSkeleton(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonCircle(size = 40.dp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBox(width = 100.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(6.dp))
                SkeletonBox(width = 80.dp, height = 12.dp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            SkeletonBox(width = 60.dp, height = 16.dp)
        }
    }
}

/**
 * Skeleton loader for the Recent Activities section.
 */
@Composable
fun RecentActivitiesSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 3
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SkeletonBox(width = 140.dp, height = 18.dp)
            SkeletonBox(width = 60.dp, height = 14.dp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)) {
            repeat(itemCount) {
                ActivityItemSkeleton()
            }
        }
    }
}
