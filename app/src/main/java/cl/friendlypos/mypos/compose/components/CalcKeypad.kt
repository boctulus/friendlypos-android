package cl.friendlypos.mypos.compose.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val POS_BLUE = Color(0xFF2196F3)
private val KEY_GRAY = Color(0xFF9E9E9E)

// Keypad for SalesCalculatorScreen
// Layout: 7 8 9 C / 4 5 6 × / 1 2 3 [+tall] / , 0 ⌫ [+tall]
// The + button spans rows 3 and 4 vertically (including the gap between them).
@Composable
fun SalesCalcKeypad(
    modifier: Modifier = Modifier,
    onDigit: (String) -> Unit,
    onDecimal: () -> Unit,
    onClear: () -> Unit,
    onDelete: () -> Unit,
    onAdd: () -> Unit,
    onMultiply: () -> Unit
) {
    val gap = 6.dp
    BoxWithConstraints(modifier = modifier) {
        val totalHeight = maxHeight
        val rowHeight = (totalHeight - gap * 3) / 4

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(gap)
        ) {
            // Left 3×4 grid
            Column(
                modifier = Modifier.weight(3f),
                verticalArrangement = Arrangement.spacedBy(gap)
            ) {
                listOf(
                    listOf(KeyDef("7") { onDigit("7") }, KeyDef("8") { onDigit("8") }, KeyDef("9") { onDigit("9") }),
                    listOf(KeyDef("4") { onDigit("4") }, KeyDef("5") { onDigit("5") }, KeyDef("6") { onDigit("6") }),
                    listOf(KeyDef("1") { onDigit("1") }, KeyDef("2") { onDigit("2") }, KeyDef("3") { onDigit("3") }),
                    listOf(KeyDef(",") { onDecimal() }, KeyDef("0") { onDigit("0") }, null)
                ).forEach { row ->
                    Row(
                        modifier = Modifier.height(rowHeight),
                        horizontalArrangement = Arrangement.spacedBy(gap)
                    ) {
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

            // Right column: C, ×, tall +
            Column(modifier = Modifier.weight(1f)) {
                CalcKey("C", modifier = Modifier.height(rowHeight).fillMaxWidth(), onClick = onClear)
                Spacer(Modifier.height(gap))
                CalcKey("×", modifier = Modifier.height(rowHeight).fillMaxWidth(), onClick = onMultiply)
                Spacer(Modifier.height(gap))
                CalcKey(
                    label = "+",
                    color = KEY_GRAY,
                    modifier = Modifier.height(rowHeight * 2 + gap).fillMaxWidth(),
                    onClick = onAdd
                )
            }
        }
    }
}

// Keypad for CashPaymentScreen
// Layout: 7 8 9 / 4 5 6 / 1 2 3 / , 0 ⌫
@Composable
fun CashPaymentKeypad(
    modifier: Modifier = Modifier,
    onDigit: (Int) -> Unit,
    onDecimal: () -> Unit = {},
    onDelete: () -> Unit
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        listOf(
            listOf(KeyDef("7") { onDigit(7) }, KeyDef("8") { onDigit(8) }, KeyDef("9") { onDigit(9) }),
            listOf(KeyDef("4") { onDigit(4) }, KeyDef("5") { onDigit(5) }, KeyDef("6") { onDigit(6) }),
            listOf(KeyDef("1") { onDigit(1) }, KeyDef("2") { onDigit(2) }, KeyDef("3") { onDigit(3) }),
            listOf(KeyDef(",") { onDecimal() }, KeyDef("0") { onDigit(0) }, null)
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
