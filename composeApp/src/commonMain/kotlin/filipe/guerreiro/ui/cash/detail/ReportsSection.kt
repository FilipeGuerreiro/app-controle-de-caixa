package filipe.guerreiro.ui.cash.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// ----- Mock Data -----

data class ReportOptionUi(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val accentColor: Color
)

// ----- Composables -----

@Composable
fun ReportsSection() {
    val options = listOf(
        ReportOptionUi(
            title = "Exportar PDF",
            subtitle = "Resumo completo do caixa em PDF",
            icon = Icons.Default.PictureAsPdf,
            accentColor = Color(0xFFE53935) // Red
        ),
        ReportOptionUi(
            title = "Exportar Planilha",
            subtitle = "Dados em formato CSV para Excel",
            icon = Icons.Default.TableChart,
            accentColor = Color(0xFF43A047) // Green
        ),
        ReportOptionUi(
            title = "Compartilhar Resumo",
            subtitle = "Enviar resumo via WhatsApp ou e-mail",
            icon = Icons.Default.Share,
            accentColor = Color(0xFF1E88E5) // Blue
        ),
        ReportOptionUi(
            title = "Imprimir Comprovante",
            subtitle = "Impressão térmica do fechamento",
            icon = Icons.Default.Print,
            accentColor = Color(0xFF8E24AA) // Purple
        )
    )

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Relatórios",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 4.dp)
        )

        options.forEach { option ->
            ReportOptionCard(option)
        }
    }
}

@Composable
private fun ReportOptionCard(option: ReportOptionUi) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* placeholder click */ },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icon badge
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = option.accentColor.copy(alpha = 0.15f),
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = option.icon,
                    contentDescription = null,
                    tint = option.accentColor,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(24.dp)
                )
            }

            // Text info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = option.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
                Text(
                    text = option.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            // Arrow
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
