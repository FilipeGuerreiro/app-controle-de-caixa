package filipe.guerreiro.ui.cash.detail

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// ----- Model -----

enum class DetailSection(
    val label: String,
    val icon: ImageVector
) {
    SUMMARY(
        label = "Resumos",
        icon = Icons.Default.Category
    ),
    HISTORY(
        label = "Lançamentos",
        icon = Icons.AutoMirrored.Filled.List
    ),
    GOALS(
        label = "Metas",
        icon = Icons.Default.Flag
    )
}

// ----- Composables -----

@Composable
fun SectionPickerGrid(
    sections: List<DetailSection> = DetailSection.entries,
    selectedSection: DetailSection,
    onSectionSelected: (DetailSection) -> Unit
) {
    // Two columns; wrap to next "row" for every pair
    val rows = sections.chunked(2)

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowItems.forEach { section ->
                    SectionPickerCard(
                        modifier = Modifier.weight(1f),
                        section = section,
                        isSelected = section == selectedSection,
                        onClick = { onSectionSelected(section) }
                    )
                }
                // Fill remaining slot if row has odd count
                if (rowItems.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun SectionPickerCard(
    modifier: Modifier = Modifier,
    section: DetailSection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) primary.copy(alpha = 0.15f)
                      else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        animationSpec = tween(durationMillis = 200),
        label = "containerColor"
    )
    val iconTint by animateColorAsState(
        targetValue = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "iconTint"
    )
    val textColor by animateColorAsState(
        targetValue = if (isSelected) primary else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(durationMillis = 200),
        label = "textColor"
    )

    Card(
        modifier = modifier
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (isSelected) BorderStroke(1.dp, primary.copy(alpha = 0.5f)) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = section.icon,
                contentDescription = section.label,
                tint = iconTint,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = section.label,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textColor
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}
