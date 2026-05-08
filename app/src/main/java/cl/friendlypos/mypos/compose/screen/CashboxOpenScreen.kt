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
import cl.friendlypos.mypos.api.dto.CashboxItemDto
import cl.friendlypos.mypos.api.dto.StoreDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashboxOpenScreen(
    storeId: String,
    stores: List<StoreDto>,
    cashboxes: List<CashboxItemDto>,
    isLoading: Boolean,
    errorMessage: String?,
    onLoadCashboxes: (storeId: String) -> Unit,
    onOpenSession: (storeId: String, cashboxNumber: Int, initialAmount: Double, notes: String?) -> Unit,
    onSessionOpened: () -> Unit,
    successMessage: String?,
    onClearMessages: () -> Unit
) {
    var selectedStoreId by remember { mutableStateOf(storeId) }
    var selectedCashboxNumber by remember { mutableIntStateOf(-1) }
    var initialAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var storeDropdownExpanded by remember { mutableStateOf(false) }
    var cashboxDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(selectedStoreId) {
        if (selectedStoreId.isNotBlank()) {
            onLoadCashboxes(selectedStoreId)
        }
    }

    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            onSessionOpened()
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
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Abrir Caja",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Registra el monto inicial para comenzar tu turno",
            fontSize = 13.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Store selector (only shown if storeId is empty or there are multiple stores)
        if (storeId.isBlank() && stores.isNotEmpty()) {
            val selectedStore = stores.find { it.id == selectedStoreId }
            ExposedDropdownMenuBox(
                expanded = storeDropdownExpanded,
                onExpandedChange = { storeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedStore?.name ?: selectedStore?.displayName ?: selectedStoreId,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tienda") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(storeDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = storeDropdownExpanded,
                    onDismissRequest = { storeDropdownExpanded = false }
                ) {
                    stores.forEach { store ->
                        DropdownMenuItem(
                            text = { Text(store.name ?: store.displayName ?: store.id) },
                            onClick = {
                                selectedStoreId = store.id
                                selectedCashboxNumber = -1
                                storeDropdownExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Cashbox selector
        val selectedCashbox = cashboxes.find { it.serialNumber == selectedCashboxNumber }
        ExposedDropdownMenuBox(
            expanded = cashboxDropdownExpanded,
            onExpandedChange = { cashboxDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = selectedCashbox?.let { "${it.displayName ?: "Caja"} (Nº ${it.serialNumber})" } ?: "",
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
                if (cashboxes.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay cajas activas") },
                        onClick = { cashboxDropdownExpanded = false }
                    )
                } else {
                    cashboxes.forEach { box ->
                        DropdownMenuItem(
                            text = { Text("${box.displayName ?: "Caja"} (Nº ${box.serialNumber})") },
                            onClick = {
                                selectedCashboxNumber = box.serialNumber
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
            prefix = { Text("$") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas de apertura (opcional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 2,
            enabled = !isLoading
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val amount = initialAmountText.toDoubleOrNull() ?: 0.0
                onOpenSession(
                    selectedStoreId,
                    selectedCashboxNumber,
                    amount,
                    notes.ifBlank { null }
                )
            },
            enabled = !isLoading && selectedCashboxNumber > 0 && initialAmountText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Abrir Caja", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
