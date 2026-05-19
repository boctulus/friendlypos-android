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
import cl.friendlypos.mypos.api.dto.MovementTypeDto

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashboxMovementScreen(
    sessionId: String,
    movementTypes: List<MovementTypeDto>,
    isLoadingTypes: Boolean,
    isLoading: Boolean,
    errorMessage: String?,
    onRegister: (movementCode: String, amount: Double, description: String, paymentMethod: String) -> Unit,
    onClearError: () -> Unit,
    onBack: () -> Unit
) {
    val incomeTypes = movementTypes.filter { it.sign == "+" && it.code != "opening" && it.code != "sale" }
    val expenseTypes = movementTypes.filter { it.sign == "-" }

    var selectedType by remember { mutableStateOf<MovementTypeDto?>(null) }
    var amountText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var hasAttempted by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }

    val amountError = hasAttempted && (amountText.isBlank() || amountText.toDoubleOrNull() == null || amountText.toDouble() <= 0)
    val descriptionError = hasAttempted && description.isBlank()
    val typeError = hasAttempted && selectedType == null

    if (errorMessage != null) {
        AlertDialog(
            onDismissRequest = onClearError,
            title = { Text("Error al registrar") },
            text = { Text(errorMessage) },
            confirmButton = { TextButton(onClick = onClearError) { Text("Aceptar") } }
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
            contentDescription = "Movimiento",
            tint = Color(0xFF607D8B),
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Registrar Movimiento",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Registra un ingreso o egreso en la caja activa",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF666666)
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoadingTypes) {
            CircularProgressIndicator(modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            ExposedDropdownMenuBox(
                expanded = typeExpanded,
                onExpandedChange = { typeExpanded = !typeExpanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedType?.label ?: "",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de movimiento *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    isError = typeError,
                    supportingText = if (typeError) {
                        { Text("Selecciona un tipo de movimiento", color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                ExposedDropdownMenu(
                    expanded = typeExpanded,
                    onDismissRequest = { typeExpanded = false }
                ) {
                    if (incomeTypes.isNotEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "— Ingresos —",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF388E3C),
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        incomeTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(type.label, fontWeight = FontWeight.Medium)
                                        if (!type.description.isNullOrBlank()) {
                                            Text(
                                                type.description,
                                                fontSize = 11.sp,
                                                color = Color(0xFF888888)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }

                    if (expenseTypes.isNotEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    "— Egresos —",
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFFE53935),
                                    fontSize = 12.sp
                                )
                            },
                            onClick = {},
                            enabled = false
                        )
                        expenseTypes.forEach { type ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(type.label, fontWeight = FontWeight.Medium)
                                        if (!type.description.isNullOrBlank()) {
                                            Text(
                                                type.description,
                                                fontSize = 11.sp,
                                                color = Color(0xFF888888)
                                            )
                                        }
                                    }
                                },
                                onClick = {
                                    selectedType = type
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            selectedType?.let { type ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (type.sign == "+") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (type.sign == "+") "+" else "-",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = if (type.sign == "+") Color(0xFF2E7D32) else Color(0xFFE53935),
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        Column {
                            Text(type.label, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            if (!type.description.isNullOrBlank()) {
                                Text(type.description, fontSize = 12.sp, color = Color(0xFF666666))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            OutlinedTextField(
                value = amountText,
                onValueChange = { amountText = it },
                label = { Text("Monto *") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading,
                prefix = { Text("$") },
                isError = amountError,
                supportingText = if (amountError) {
                    { Text("Ingresa un monto válido mayor a cero", color = MaterialTheme.colorScheme.error) }
                } else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            val minDescLength = selectedType?.requiresJustification == true
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = {
                    Text(
                        if (minDescLength) "Descripción * (mínimo 10 caracteres)"
                        else "Descripción *"
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3,
                enabled = !isLoading,
                isError = descriptionError || (hasAttempted && minDescLength && description.trim().length < 10),
                supportingText = if (descriptionError) {
                    { Text("La descripción es obligatoria", color = MaterialTheme.colorScheme.error) }
                } else if (hasAttempted && minDescLength && description.trim().length < 10) {
                    { Text("Este tipo requiere mínimo 10 caracteres", color = MaterialTheme.colorScheme.error) }
                } else null
            )

            Spacer(modifier = Modifier.height(28.dp))

            val isExpense = selectedType?.sign == "-"
            Button(
                onClick = {
                    hasAttempted = true
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    val type = selectedType ?: return@Button
                    if (amount <= 0 || description.isBlank()) return@Button
                    if (type.requiresJustification && description.trim().length < 10) return@Button
                    onRegister(type.code, amount, description.trim(), "cash")
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isExpense) Color(0xFFE53935) else Color(0xFF388E3C)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                } else {
                    Text("Registrar Movimiento", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading
            ) {
                Text("Cancelar")
            }
        }
    }
}
