package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.NumberFormat
import java.util.Locale

data class TransferDetails(
    val bank: String?,
    val accountType: String?,
    val accountNumber: String?
)

/**
 * Pantalla de cobro para tarjeta (mock) y transferencia.
 * En transferencia captura los datos del pagador (banco, tipo y N° de cuenta).
 *
 * @param method "card" o "transfer".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MockPaymentScreen(
    totalAmount: Double,
    method: String,
    isProcessing: Boolean,
    onConfirm: (TransferDetails?) -> Unit,
    onCancel: () -> Unit
) {
    val isCard = method == "card"
    val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    var banco by remember { mutableStateOf("") }
    var tipoCuenta by remember { mutableStateOf("savings") }
    var numeroCuenta by remember { mutableStateOf("") }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val accountTypeOptions = listOf(
        "savings" to "Ahorros",
        "checking" to "Cuenta Corriente",
        "rut" to "Cuenta RUT",
        "other" to "Otro"
    )
    val tipoCuentaLabel = accountTypeOptions.firstOrNull { it.first == tipoCuenta }?.second ?: "Ahorros"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = if (isCard) "Pago con tarjeta" else "Transferencia bancaria",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = if (isCard) "Simulación de cobro (sin terminal TUU)" else "Ingresa los datos del pagador",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoRow("Total a cobrar", formatter.format(totalAmount))
            }
        }

        if (!isCard) {
            Spacer(Modifier.height(20.dp))

            Text(
                text = "Detalles de transferencia",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = banco,
                onValueChange = { banco = it },
                label = { Text("Banco") },
                placeholder = { Text("Ej: Banco Estado, BCI...") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(8.dp))

            ExposedDropdownMenuBox(
                expanded = dropdownExpanded,
                onExpandedChange = { dropdownExpanded = !dropdownExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = tipoCuentaLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de cuenta") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = dropdownExpanded,
                    onDismissRequest = { dropdownExpanded = false }
                ) {
                    accountTypeOptions.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                tipoCuenta = value
                                dropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = numeroCuenta,
                onValueChange = { numeroCuenta = it },
                label = { Text("N° de cuenta") },
                placeholder = { Text("00000000") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val details = if (!isCard) TransferDetails(
                    bank = banco.takeIf { it.isNotBlank() },
                    accountType = tipoCuenta,
                    accountNumber = numeroCuenta.takeIf { it.isNotBlank() }
                ) else null
                onConfirm(details)
            },
            enabled = !isProcessing,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text(
                    if (isCard) "Simular aprobación" else "Confirmar transferencia",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onCancel,
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cancelar")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF666666), style = MaterialTheme.typography.bodyMedium)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyMedium)
    }
}
