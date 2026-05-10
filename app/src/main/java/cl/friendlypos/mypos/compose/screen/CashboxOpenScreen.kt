package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.theme.AppColors
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashboxOpenScreen(
    availability: List<CashboxAvailabilityItemDto>,
    isLoadingAvailability: Boolean = false,
    isLoading: Boolean,
    errorMessage: String?,
    onOpenSession: (cashboxId: String, initialAmount: Double, notes: String?) -> Unit,
    successMessage: String?,
    onClearMessages: () -> Unit
) {
    var selectedCashboxId by remember { mutableStateOf<String?>(null) }
    var initialAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var cashboxDropdownExpanded by remember { mutableStateOf(false) }

    val availableCashboxes = availability.filter { it.isAvailable }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onClearMessages()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cash),
            contentDescription = "Caja",
            tint = AppColors.BrandPrimary,
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Abrir Caja",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Registra el monto inicial para comenzar tu turno",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(28.dp))

        if (isLoadingAvailability) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(28.dp))
            }
        } else if (availableCashboxes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cash),
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            "Todas las cajas están ocupadas",
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE65100)
                        )
                        Text(
                            "Un supervisor debe cerrar una caja para continuar.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF795548)
                        )
                    }
                }
            }
        } else {
            val selectedCashbox = availableCashboxes.find { it.cashboxId == selectedCashboxId }
            ExposedDropdownMenuBox(
                expanded = cashboxDropdownExpanded,
                onExpandedChange = { cashboxDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedCashbox?.let { "${it.displayName ?: "Caja"} (Nº ${it.cashboxLabel ?: ""})" } ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Número de caja *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(cashboxDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    placeholder = { Text("Seleccionar caja") }
                )
                ExposedDropdownMenu(
                    expanded = cashboxDropdownExpanded,
                    onDismissRequest = { cashboxDropdownExpanded = false }
                ) {
                    availableCashboxes.forEach { box ->
                        DropdownMenuItem(
                            text = { Text("${box.displayName ?: "Caja"} (Nº ${box.cashboxLabel ?: ""})") },
                            onClick = {
                                selectedCashboxId = box.cashboxId
                                cashboxDropdownExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = initialAmountText,
            onValueChange = { initialAmountText = it },
            label = { Text("Monto inicial *") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading,
            prefix = { Text("$") },
            textStyle = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas de apertura (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            enabled = !isLoading,
            textStyle = MaterialTheme.typography.bodyMedium
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val cashboxId = selectedCashboxId ?: return@Button
                val amount = initialAmountText.toDoubleOrNull() ?: 0.0
                onOpenSession(cashboxId, amount, notes.ifBlank { null })
            },
            enabled = !isLoading && selectedCashboxId != null && initialAmountText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AppColors.BrandPrimary,
                disabledContainerColor = AppColors.BrandPrimary.copy(alpha = 0.5f),
                disabledContentColor = Color.White.copy(alpha = 0.7f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Abrir Caja", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
