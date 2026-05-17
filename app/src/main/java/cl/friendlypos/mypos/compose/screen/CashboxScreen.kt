package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.api.dto.CashboxAvailabilityItemDto
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import kotlin.math.abs

@Composable
fun CashboxScreen(
    role: String,
    currentSession: CashboxSessionItemDto?,
    availability: List<CashboxAvailabilityItemDto>,
    isLoading: Boolean,
    isLoadingAvailability: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onOpenSession: (cashboxId: String, cashboxLabel: String, initialAmount: Double, notes: String?) -> Unit,
    onCloseSession: (sessionId: String, finalAmount: Double, notes: String?) -> Unit,
    onClearMessages: () -> Unit,
    onSaveAndLogout: () -> Unit,
    onContinue: () -> Unit,
    onLogout: () -> Unit
) {
    when {
        role == "supermarket" -> CashboxSupermarketScreen(
            currentSession = currentSession,
            availability = availability,
            isLoading = isLoading,
            errorMessage = errorMessage,
            successMessage = successMessage,
            onOpenSession = onOpenSession,
            onCloseSession = onCloseSession,
            onClearMessages = onClearMessages
        )
        currentSession != null && currentSession.status == "open" -> CashboxCloseContent(
            session = currentSession,
            isLoading = isLoading,
            errorMessage = errorMessage,
            successMessage = successMessage,
            onCloseSession = onCloseSession,
            onClearMessages = onClearMessages,
            onSaveAndLogout = onSaveAndLogout,
            onContinue = onContinue,
            onLogout = onLogout
        )
        else -> CashboxOpenScreen(
            availability = availability,
            isLoadingAvailability = isLoadingAvailability,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onOpenSession = onOpenSession,
            successMessage = successMessage,
            onClearMessages = onClearMessages
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CashboxCloseContent(
    session: CashboxSessionItemDto,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    onCloseSession: (String, Double, String?) -> Unit,
    onClearMessages: () -> Unit,
    onSaveAndLogout: () -> Unit,
    onContinue: () -> Unit,
    onLogout: () -> Unit
) {
    var finalAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var hasAttempted by remember { mutableStateOf(false) }
    var hadError by remember { mutableStateOf(false) }
    var notesRequired by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var pendingCloseAmount by remember { mutableStateOf(0.0) }
    var pendingDiff by remember { mutableStateOf(0.0) }
    var pendingDiffPercent by remember { mutableStateOf(0.0) }

    val notesFocusRequester = remember { FocusRequester() }
    val expectedAmount = session.totalCash ?: session.initialAmount

    if (errorMessage != null) hadError = true
    if (notesRequired && notes.isNotBlank()) notesRequired = false

    LaunchedEffect(notesRequired) {
        if (notesRequired) notesFocusRequester.requestFocus()
    }

    if (showConfirmDialog) {
        val isSurplus = (pendingCloseAmount - expectedAmount) > 0
        val diffWord = if (isSurplus) "Sobrante" else "Faltante"
        val (dialogBg, accentColor, titleText) = when {
            pendingDiffPercent > 5.0 -> Triple(Color(0xFFFFEBEE), Color(0xFFE53935), "Alta Diferencia Detectada")
            pendingDiffPercent >= 1.0 -> Triple(Color(0xFFFFF8E1), Color(0xFFF57C00), "Diferencia Intermedia Detectada")
            else -> Triple(Color(0xFFE3F2FD), Color(0xFF1976D2), "Diferencia Mínima Detectada")
        }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            containerColor = dialogBg,
            title = { Text(titleText, color = accentColor, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Monto esperado: ${"$"}${String.format("%.2f", expectedAmount)}")
                    Text("Monto contado:  ${"$"}${String.format("%.2f", pendingCloseAmount)}")
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Diferencia: ${"$"}${String.format("%.2f", pendingDiff)} (${String.format("%.2f", pendingDiffPercent)}%)",
                        color = accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("$diffWord en caja", style = MaterialTheme.typography.bodySmall, color = Color(0xFF666666))
                    Spacer(Modifier.height(8.dp))
                    Text("¿Confirmas cerrar la caja con esta diferencia?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDialog = false
                        onCloseSession(session.id, pendingCloseAmount, notes.ifBlank { null })
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) { Text("Sí, cerrar caja", color = Color.White) }
            },
            dismissButton = {
                OutlinedButton(onClick = { showConfirmDialog = false }) { Text("Cancelar") }
            }
        )
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
            tint = Color(0xFFE53935),
            modifier = Modifier.size(56.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Cerrar Caja",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Registra el monto final para cerrar tu turno",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sesión activa", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                SessionInfoRow("Caja Nº", session.cashboxLabel ?: "")
                SessionInfoRow("Monto inicial", "$ ${session.initialAmount}")
                if (!session.openedAt.isNullOrBlank()) {
                    SessionInfoRow("Abierta", session.openedAt.take(19).replace("T", " "))
                }
                if (!session.cashierName.isNullOrBlank()) {
                    SessionInfoRow("Cajero", session.cashierName)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        successMessage,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF2E7D32),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                onClearMessages()
                                onContinue()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Continuar", fontSize = 13.sp)
                        }
                        Button(
                            onClick = onLogout,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64))
                        ) {
                            Text("Cerrar sesión", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
            return
        }

        OutlinedTextField(
            value = finalAmountText,
            onValueChange = {
                finalAmountText = it
                onClearMessages()
            },
            label = { Text("Monto final *") },
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
            onValueChange = {
                notes = it
                if (notesRequired && it.isNotBlank()) notesRequired = false
            },
            label = { Text(if (notesRequired && notes.isBlank()) "Notas de cierre *" else "Notas de cierre (obligatorio si diferencia ≥ 1%)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(notesFocusRequester),
            maxLines = 2,
            enabled = !isLoading,
            textStyle = MaterialTheme.typography.bodyMedium,
            isError = notesRequired && notes.isBlank(),
            supportingText = if (notesRequired && notes.isBlank()) {
                { Text("Diferencia ≥ 1%: se requiere nota explicativa", color = MaterialTheme.colorScheme.error) }
            } else null
        )

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = onClearMessages,
                title = { Text("Error al cerrar caja") },
                text = { Text(errorMessage) },
                confirmButton = {
                    TextButton(onClick = onClearMessages) { Text("Aceptar") }
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                hasAttempted = true
                val finalVal = finalAmountText.toDoubleOrNull() ?: 0.0
                val diff = abs(expectedAmount - finalVal)
                val diffPct = if (expectedAmount > 0) (diff / expectedAmount) * 100
                              else if (diff > 0) 100.0 else 0.0

                if (diffPct >= 1.0 && notes.isBlank()) {
                    notesRequired = true
                    return@Button
                }

                if (diff == 0.0) {
                    onCloseSession(session.id, finalVal, notes.ifBlank { null })
                } else {
                    pendingCloseAmount = finalVal
                    pendingDiff = diff
                    pendingDiffPercent = diffPct
                    showConfirmDialog = true
                }
            },
            enabled = !isLoading && finalAmountText.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
            } else {
                Text("Cerrar Caja", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }

        if (hasAttempted && hadError) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onSaveAndLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF795548))
            ) {
                Text("Guardar y salir", fontSize = 14.sp)
            }
        }
    }
}

@Composable
private fun SessionInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF666666), style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodySmall)
    }
}
