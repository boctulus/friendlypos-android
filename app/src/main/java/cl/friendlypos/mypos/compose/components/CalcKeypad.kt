package cl.friendlypos.mypos.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val POS_BLUE = Color(0xFF2196F3)
private val KEY_GRAY = Color(0xFF9E9E9E)

// Keypad for SalesCalculatorScreen — right column (top→bottom): C, ×, +, ⌫
// Layout: 7 8 9 C / 4 5 6 × / 1 2 3 + / , 0 00 ⌫
@Composable
fun SalesCalcKeypad(
    modifier: Modifier = Modifier,
    onDigit: (String) -> Unit,
    onDoubleZero: () -> Unit,
    onDecimal: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit,
    onMultiply: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            listOf(
                KeyDef("7") { onDigit("7") },
                KeyDef("8") { onDigit("8") },
                KeyDef("9") { onDigit("9") },
                KeyDef("C") { onClear() }
            ),
            listOf(
                KeyDef("4") { onDigit("4") },
                KeyDef("5") { onDigit("5") },
                KeyDef("6") { onDigit("6") },
                KeyDef("×") { onMultiply() }
            ),
            listOf(
                KeyDef("1") { onDigit("1") },
                KeyDef("2") { onDigit("2") },
                KeyDef("3") { onDigit("3") },
                KeyDef("+", color = KEY_GRAY) { onAdd() }
            ),
            listOf(
                KeyDef(",") { onDecimal() },
                KeyDef("0") { onDigit("0") },
                KeyDef("00") { onDoubleZero() },
                null
            )
        ).forEach { row ->
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { key ->
                    if (key != null) {
                        CalcKey(
                            label = key.label,
                            color = key.color,
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = key.onClick
                        )
                    } else {
                        BackspaceKey(
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                            onClick = onDelete
                        )
                    }
                }
            }
        }
    }
}

// Keypad for CashPaymentScreen (3 columns: digits + backspace)
@Composable
fun CashPaymentKeypad(
    modifier: Modifier = Modifier,
    onDigit: (Int) -> Unit,
    onDoubleZero: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(listOf(1, 2, 3), listOf(4, 5, 6), listOf(7, 8, 9)).forEach { row ->
            Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                row.forEach { digit ->
                    CalcKey(
                        label = digit.toString(),
                        modifier = Modifier.weight(1f).fillMaxHeight(),
                        onClick = { onDigit(digit) }
                    )
                }
            }
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            CalcKey(
                label = "00",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onDoubleZero() }
            )
            CalcKey(
                label = "0",
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = { onDigit(0) }
            )
            BackspaceKey(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                onClick = onDelete
            )
        }
    }
}

@Composable
private fun CalcKey(
    label: String,
    modifier: Modifier = Modifier,
    color: Color = POS_BLUE,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(
            text = label,
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White
        )
    }
}

@Composable
private fun BackspaceKey(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE),
        contentPadding = PaddingValues(0.dp)
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Backspace,
            contentDescription = "Borrar",
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

private data class KeyDef(val label: String, val color: Color = POS_BLUE, val onClick: () -> Unit)
