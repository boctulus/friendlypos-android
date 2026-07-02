package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.compose.components.CashPaymentKeypad
import cl.friendlypos.mypos.compose.theme.AppColors
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashPaymentScreen(
    totalAmount: Double,
    onConfirm: (amountEntered: Int) -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit
) {
    val formatter = remember { NumberFormat.getCurrencyInstance(Locale("es", "CL")) }
    var amountEntered by remember { mutableIntStateOf(0) }
    var showCancelDialog by remember { mutableStateOf(false) }

    val change = amountEntered - totalAmount
    val hasEnoughAmount = change >= 0

    val acceptLabel = when {
        amountEntered == 0 -> "Omitir"
        hasEnoughAmount -> "Finalizar"
        else -> "Aceptar"
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancelar transacción") },
            text = { Text("¿Está seguro de que desea cancelar la transacción?") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    onCancel()
                }) { Text("Sí, cancelar") }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) { Text("No") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ingreso efectivo") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Volver"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Total and entered amount display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatter.format(totalAmount),
                    fontSize = 36.sp,
                    color = Color.Black
                )
                Text(
                    text = formatter.format(amountEntered),
                    fontSize = 16.sp,
                    color = Color.Black
                )
            }

            // Change display
            if (hasEnoughAmount && amountEntered > 0) {
                Text(
                    text = "Cambio: ${formatter.format(change)}",
                    fontSize = 16.sp,
                    color = AppColors.ChartTeal,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            // Keypad
            CashPaymentKeypad(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                onDigit = { digit ->
                    if (amountEntered <= Int.MAX_VALUE / 10) {
                        amountEntered = amountEntered * 10 + digit
                    }
                },
                onDelete = {
                    if (amountEntered > 0) amountEntered /= 10
                }
            )

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showCancelDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = { onConfirm(amountEntered) },
                    enabled = amountEntered == 0 || hasEnoughAmount,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = acceptLabel,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
