package filipe.guerreiro.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

private data class ColorItem(val name: String, val color: Color)
private data class ColorSection(val title: String, val items: List<ColorItem>)

@Composable
fun ColorGalleryScreen() {
    val sections = listOf(
        ColorSection(
            title = "Tema claro",
            items = listOf(
                ColorItem("primaryLight", primaryLight),
                ColorItem("onPrimaryLight", onPrimaryLight),
                ColorItem("primaryContainerLight", primaryContainerLight),
                ColorItem("onPrimaryContainerLight", onPrimaryContainerLight),
                ColorItem("secondaryLight", secondaryLight),
                ColorItem("onSecondaryLight", onSecondaryLight),
                ColorItem("secondaryContainerLight", secondaryContainerLight),
                ColorItem("onSecondaryContainerLight", onSecondaryContainerLight),
                ColorItem("tertiaryLight", tertiaryLight),
                ColorItem("onTertiaryLight", onTertiaryLight),
                ColorItem("tertiaryContainerLight", tertiaryContainerLight),
                ColorItem("onTertiaryContainerLight", onTertiaryContainerLight),
                ColorItem("errorLight", errorLight),
                ColorItem("onErrorLight", onErrorLight),
                ColorItem("errorContainerLight", errorContainerLight),
                ColorItem("onErrorContainerLight", onErrorContainerLight),
                ColorItem("backgroundLight", backgroundLight),
                ColorItem("onBackgroundLight", onBackgroundLight),
                ColorItem("surfaceLight", surfaceLight),
                ColorItem("onSurfaceLight", onSurfaceLight),
                ColorItem("surfaceVariantLight", surfaceVariantLight),
                ColorItem("onSurfaceVariantLight", onSurfaceVariantLight),
                ColorItem("outlineLight", outlineLight),
                ColorItem("outlineVariantLight", outlineVariantLight),
                ColorItem("scrimLight", scrimLight),
                ColorItem("inverseSurfaceLight", inverseSurfaceLight),
                ColorItem("inverseOnSurfaceLight", inverseOnSurfaceLight),
                ColorItem("inversePrimaryLight", inversePrimaryLight),
                ColorItem("surfaceDimLight", surfaceDimLight),
                ColorItem("surfaceBrightLight", surfaceBrightLight),
                ColorItem("surfaceContainerLowestLight", surfaceContainerLowestLight),
                ColorItem("surfaceContainerLowLight", surfaceContainerLowLight),
                ColorItem("surfaceContainerLight", surfaceContainerLight),
                ColorItem("surfaceContainerHighLight", surfaceContainerHighLight),
                ColorItem("surfaceContainerHighestLight", surfaceContainerHighestLight)
            )
        ),
        ColorSection(
            title = "Tema escuro",
            items = listOf(
                ColorItem("primaryDark", primaryDark),
                ColorItem("onPrimaryDark", onPrimaryDark),
                ColorItem("primaryContainerDark", primaryContainerDark),
                ColorItem("onPrimaryContainerDark", onPrimaryContainerDark),
                ColorItem("secondaryDark", secondaryDark),
                ColorItem("onSecondaryDark", onSecondaryDark),
                ColorItem("secondaryContainerDark", secondaryContainerDark),
                ColorItem("onSecondaryContainerDark", onSecondaryContainerDark),
                ColorItem("tertiaryDark", tertiaryDark),
                ColorItem("onTertiaryDark", onTertiaryDark),
                ColorItem("tertiaryContainerDark", tertiaryContainerDark),
                ColorItem("onTertiaryContainerDark", onTertiaryContainerDark),
                ColorItem("errorDark", errorDark),
                ColorItem("onErrorDark", onErrorDark),
                ColorItem("errorContainerDark", errorContainerDark),
                ColorItem("onErrorContainerDark", onErrorContainerDark),
                ColorItem("backgroundDark", backgroundDark),
                ColorItem("onBackgroundDark", onBackgroundDark),
                ColorItem("surfaceDark", surfaceDark),
                ColorItem("onSurfaceDark", onSurfaceDark),
                ColorItem("surfaceVariantDark", surfaceVariantDark),
                ColorItem("onSurfaceVariantDark", onSurfaceVariantDark),
                ColorItem("outlineDark", outlineDark),
                ColorItem("outlineVariantDark", outlineVariantDark),
                ColorItem("scrimDark", scrimDark),
                ColorItem("inverseSurfaceDark", inverseSurfaceDark),
                ColorItem("inverseOnSurfaceDark", inverseOnSurfaceDark),
                ColorItem("inversePrimaryDark", inversePrimaryDark),
                ColorItem("surfaceDimDark", surfaceDimDark),
                ColorItem("surfaceBrightDark", surfaceBrightDark),
                ColorItem("surfaceContainerLowestDark", surfaceContainerLowestDark),
                ColorItem("surfaceContainerLowDark", surfaceContainerLowDark),
                ColorItem("surfaceContainerDark", surfaceContainerDark),
                ColorItem("surfaceContainerHighDark", surfaceContainerHighDark),
                ColorItem("surfaceContainerHighestDark", surfaceContainerHighestDark)
            )
        ),
        ColorSection(
            title = "Cores estendidas",
            items = listOf(
                ColorItem("extendedColorLight", extendedColorLight),
                ColorItem("onExtendedColorLight", onExtendedColorLight),
                ColorItem("extendedColorContainerLight", extendedColorContainerLight),
                ColorItem("onExtendedColorContainerLight", onExtendedColorContainerLight),
                ColorItem("extendedColorDark", extendedColorDark),
                ColorItem("onExtendedColorDark", onExtendedColorDark),
                ColorItem("extendedColorContainerDark", extendedColorContainerDark),
                ColorItem("onExtendedColorContainerDark", onExtendedColorContainerDark)
            )
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(sections) { section ->
            SectionHeader(title = section.title)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                section.items.forEach { item ->
                    ColorRow(item)
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ColorRow(item: ColorItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(item.color, RoundedCornerShape(8.dp))
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.color.toHexString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun Color.toHexString(): String {
    val argb = toArgb()
    val hex = argb.toUInt().toString(16).uppercase().padStart(8, '0')
    return "#" + hex
}
