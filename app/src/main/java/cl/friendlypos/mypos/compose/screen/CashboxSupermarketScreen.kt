package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
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
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto

@Composable
fun CashboxSupermarketScreen(
    availability: List<CashboxAvailabilityItemDto>,
    isLoading: Boolean,
    errorMessage: String?,
    onCloseSession: (sessionId: String, finalAmount: Double, notes: String?) -> Unit,
    onClearMessages: () -> Unit
) {
    var closingSessionId by remember { mutableStateOf<String?>(null) }
    var finalAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cash),
            contentDescription = "Cajas",
            tint = Color(0xFF009688),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Estado de Cajas",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Todas las cajas de tu tienda",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(20.dp))

        if (availability.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cash),
                        contentDescription = null,
                        tint = Color(0xFF388E3C),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        "No hay cajas configuradas",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF388E3C)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(availability, key = { it.cashboxId }) { item ->
                    CashboxStatusCard(
                        item = item,
                        isClosing = closingSessionId == item.sessionId,
                        isLoading = isLoading && closingSessionId == item.sessionId,
                        finalAmountText = if (closingSessionId == item.sessionId) finalAmountText else "",
                        notes = if (closingSessionId == item.sessionId) notes else "",
                        errorMessage = if (closingSessionId == item.sessionId) errorMessage else null,
                        onStartClose = {
                            closingSessionId = item.sessionId
                            finalAmountText = ""
                            notes = ""
                            onClearMessages()
                        },
                        onCancelClose = { closingSessionId = null },
                        onFinalAmountChange = { finalAmountText = it },
                        onNotesChange = { notes = it },
                        onConfirmClose = {
                            item.sessionId?.let { sessionId ->
                                val amount = finalAmountText.toDoubleOrNull() ?: 0.0
                                onCloseSession(sessionId, amount, notes.ifBlank { null })
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CashboxStatusCard(
    item: CashboxAvailabilityItemDto,
    isClosing: Boolean,
    isLoading: Boolean,
    finalAmountText: String,
    notes: String,
    errorMessage: String?,
    onStartClose: () -> Unit,
    onCancelClose: () -> Unit,
    onFinalAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirmClose: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Caja Nº ${item.cashboxLabel ?: ""}",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall
                    )
                    if (item.isOccupied && !item.cashierName.isNullOrBlank()) {
                        Text(
                            item.cashierName,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF666666)
                        )
                    }
                }
                if (item.isOccupied) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFFFF5722).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Abierta",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFBF360C),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                    ) {
                        Text(
                            "Disponible",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (item.isOccupied) {
                if (isClosing) {
                    OutlinedTextField(
                        value = finalAmountText,
                        onValueChange = onFinalAmountChange,
                        label = { Text("Monto final *") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        prefix = { Text("$") },
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = notes,
                        onValueChange = onNotesChange,
                        label = { Text("Notas (opcional)") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        enabled = !isLoading,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    if (errorMessage != null) {
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelClose,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onConfirmClose,
                            enabled = !isLoading && finalAmountText.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Cerrar Caja", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onStartClose,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Cerrar Caja", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}
