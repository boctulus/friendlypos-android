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
import cl.friendlypos.mypos.api.dto.CashboxSessionItemDto
import cl.friendlypos.mypos.api.dto.StoreDto

@Composable
fun CashboxScreen(
    currentSession: CashboxSessionItemDto?,
    stores: List<StoreDto>,
    cashboxes: List<CashboxItemDto>,
    isLoading: Boolean,
    errorMessage: String?,
    successMessage: String?,
    storeId: String,
    onLoadCashboxes: (String) -> Unit,
    onOpenSession: (String, Int, Double, String?) -> Unit,
    onSessionOpened: () -> Unit,
    onCloseSession: (String, Double, String?) -> Unit,
    onClearMessages: () -> Unit
) {
    if (currentSession != null && currentSession.status == "open") {
        CashboxCloseContent(
            session = currentSession,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onCloseSession = onCloseSession,
            onClearMessages = onClearMessages
        )
    } else {
        CashboxOpenScreen(
            storeId = storeId,
            stores = stores,
            cashboxes = cashboxes,
            isLoading = isLoading,
            errorMessage = errorMessage,
            onLoadCashboxes = onLoadCashboxes,
            onOpenSession = onOpenSession,
            onSessionOpened = onSessionOpened,
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
    onCloseSession: (String, Double, String?) -> Unit,
    onClearMessages: () -> Unit
) {
    var finalAmountText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

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
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Registra el monto final para cerrar tu turno",
            fontSize = 13.sp,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F5F5))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Sesión activa", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                SessionInfoRow("Caja Nº", session.cashboxNumber.toString())
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
            prefix = { Text("$") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notas de cierre (opcional)") },
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
                val amount = finalAmountText.toDoubleOrNull() ?: 0.0
                onCloseSession(session.id, amount, notes.ifBlank { null })
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
    }
}

@Composable
private fun SessionInfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color(0xFF666666), fontSize = 13.sp)
        Text(value, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}
