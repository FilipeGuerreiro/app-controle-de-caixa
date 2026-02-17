package filipe.guerreiro.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import filipe.guerreiro.domain.model.toCurrencyStringWithoutPrefix

@Composable
fun CurrencyAmountInput(
    amountInCents: Long,
    onAmountChange: (Long) -> Unit,
    modifier: Modifier = Modifier,
    prefix: String = "R$ ",
    textStyle: TextStyle = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    maxDigits: Int = 11 // Max value safely fits in Long and typical currency limits
) {
    val formattedAmount = amountInCents.toCurrencyStringWithoutPrefix()
    
    // Create TextFieldValue with cursor always at the end
    val textFieldValue = TextFieldValue(
        text = formattedAmount,
        selection = TextRange(formattedAmount.length)
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.headlineMedium,
            color = textColor.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp, end = 4.dp)
        )

        Box(contentAlignment = Alignment.Center) {
            if (amountInCents == 0L) {
                Text(
                    text = "0,00",
                    style = textStyle,
                    color = textColor.copy(alpha = 0.3f),
                    textAlign = TextAlign.Center
                )
            }
            
            BasicTextField(
                value = textFieldValue,
                onValueChange = { newValue ->
                    // Logic to handle input:
                    // 1. Filter only digits
                    // 2. Convert to Long
                    // 3. Update state
                    val digitsOnly = newValue.text.filter { it.isDigit() }
                    
                    // Prevent overflow or excessive digits
                    if (digitsOnly.length <= maxDigits) {
                        val newAmount = digitsOnly.toLongOrNull() ?: 0L
                        onAmountChange(newAmount)
                    }
                },
                textStyle = textStyle.copy(
                    color = if (amountInCents == 0L) Color.Transparent else textColor,
                    textAlign = TextAlign.Center
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                cursorBrush = SolidColor(textColor)
            )
        }
    }
}
