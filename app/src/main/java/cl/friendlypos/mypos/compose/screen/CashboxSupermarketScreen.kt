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
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto

@Composable
fun CashboxSupermarketScreen(
    currentSession: CashboxSessionItemDto?,
    availability: List<CashboxAvailabilityItemDto>,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onOpenSession: (cashboxId: String, cashboxLabel: String, initialAmount: Double, notes: String?) -> Unit,
    onCloseSession: (sessionId: String, finalAmount: Double, notes: String?) -> Unit,
    onClearMessages: () -> Unit
) {
    val terminalHasOpenSession = currentSession?.status == "open"

    var closingSessionId by remember { mutableStateOf<String?>(null) }
    var openingCashboxId by remember { mutableStateOf<String?>(null) }
    var finalAmountText by remember { mutableStateOf("") }
    var initialAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onClearMessages,
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = onClearMessages) { Text("Aceptar") }
            }
        )
    }

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

        if (terminalHasOpenSession) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_cash),
                        contentDescription = null,
                        tint = Color(0xFFE65100),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Caja ya abierta en esta terminal",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFE65100)
                    )
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

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
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(availability, key = { it.cashboxId }) { item ->
                    val isCurrentTerminalSession = terminalHasOpenSession &&
                        currentSession != null &&
                        item.sessionId == currentSession.id
                    CashboxStatusCard(
                        item = item,
                        terminalHasOpenSession = terminalHasOpenSession,
                        isCurrentTerminalSession = isCurrentTerminalSession,
                        currentSession = if (isCurrentTerminalSession) currentSession else null,
                        isClosing = closingSessionId == item.sessionId,
                        isOpening = openingCashboxId == item.cashboxId,
                        isLoading = isLoading && (closingSessionId == item.sessionId || openingCashboxId == item.cashboxId),
                        finalAmountText = if (closingSessionId == item.sessionId) finalAmountText else "",
                        initialAmountText = if (openingCashboxId == item.cashboxId) initialAmountText else "",
                        notes = if (closingSessionId == item.sessionId || openingCashboxId == item.cashboxId) notes else "",
                        onStartClose = {
                            closingSessionId = item.sessionId
                            openingCashboxId = null
                            finalAmountText = ""
                            notes = ""
                            onClearMessages()
                        },
                        onCancelClose = { closingSessionId = null },
                        onFinalAmountChange = { finalAmountText = it },
                        onStartOpen = {
                            openingCashboxId = item.cashboxId
                            closingSessionId = null
                            initialAmountText = ""
                            notes = ""
                            onClearMessages()
                        },
                        onCancelOpen = { openingCashboxId = null },
                        onInitialAmountChange = { initialAmountText = it },
                        onNotesChange = { notes = it },
                        onConfirmClose = {
                            item.sessionId?.let { sessionId ->
                                val amount = finalAmountText.toDoubleOrNull() ?: 0.0
                                onCloseSession(sessionId, amount, notes.ifBlank { null })
                            }
                        },
                        onConfirmOpen = {
                            val amount = initialAmountText.toDoubleOrNull() ?: 0.0
                            val label = item.cashboxLabel.toString()
                            onOpenSession(item.cashboxId, label, amount, notes.ifBlank { null })
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
    terminalHasOpenSession: Boolean,
    isCurrentTerminalSession: Boolean,
    currentSession: CashboxSessionItemDto?,
    isClosing: Boolean,
    isOpening: Boolean,
    isLoading: Boolean,
    finalAmountText: String,
    initialAmountText: String,
    notes: String,
    onStartClose: () -> Unit,
    onCancelClose: () -> Unit,
    onFinalAmountChange: (String) -> Unit,
    onStartOpen: () -> Unit,
    onCancelOpen: () -> Unit,
    onInitialAmountChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirmClose: () -> Unit,
    onConfirmOpen: () -> Unit
) {
    var showDetails by remember { mutableStateOf(false) }
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
                        "Caja Nº ${item.cashboxLabel}",
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

            if (item.isAvailable && !terminalHasOpenSession) {
                if (isOpening) {
                    OutlinedTextField(
                        value = initialAmountText,
                        onValueChange = onInitialAmountChange,
                        label = { Text("Monto inicial *") },
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onCancelOpen,
                            enabled = !isLoading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancelar", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onConfirmOpen,
                            enabled = !isLoading && initialAmountText.isNotBlank(),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text("Abrir Caja", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Button(
                        onClick = onStartOpen,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF009688))
                    ) {
                        Text("Abrir Caja", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (isCurrentTerminalSession && currentSession != null && !isClosing) {
                if (showDetails) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "Sesión activa (esta terminal)",
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF2E7D32)
                            )
                            if (!currentSession.cashierName.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cajero", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
                                    Text(currentSession.cashierName, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Monto inicial", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
                                Text("$ ${currentSession.initialAmount}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                            if (!currentSession.openedAt.isNullOrBlank()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Abierta", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
                                    Text(currentSession.openedAt.take(19).replace("T", " "), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (showDetails) "Ocultar" else "Ver detalles", fontSize = 13.sp)
                    }
                    Button(
                        onClick = onStartClose,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                    ) {
                        Text("Cerrar caja", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (item.isOccupied && (isClosing || !isCurrentTerminalSession)) {
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
