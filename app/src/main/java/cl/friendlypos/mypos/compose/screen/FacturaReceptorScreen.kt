package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cl.friendlypos.mypos.api.dto.TaxpayerDataDto
import cl.friendlypos.mypos.checkout.CheckoutHolder
import cl.friendlypos.mypos.util.RutValidator
import kotlinx.coroutines.delay

/**
 * Formulario de datos del receptor para Factura Electrónica (DTE tipo 33).
 * Al ingresar un RUT válido, autocompleta los campos consultando el backend (OpenFactura)
 * mediante [onLookupRut] (debounce ~500 ms, equivalente al módulo web).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturaReceptorScreen(
    initial: CheckoutHolder.ReceptorData? = null,
    onLookupRut: suspend (String) -> TaxpayerDataDto?,
    onConfirm: (CheckoutHolder.ReceptorData) -> Unit,
    onBack: () -> Unit
) {
    var rut by remember { mutableStateOf(initial?.rut ?: "") }
    var razonSocial by remember { mutableStateOf(initial?.razonSocial ?: "") }
    var giro by remember { mutableStateOf(initial?.giro ?: "") }
    var direccion by remember { mutableStateOf(initial?.direccion ?: "") }
    var comuna by remember { mutableStateOf(initial?.comuna ?: "") }
    var email by remember { mutableStateOf(initial?.email ?: "") }

    var isLooking by remember { mutableStateOf(false) }
    var lookupError by remember { mutableStateOf<String?>(null) }

    val rutValid = RutValidator.isValid(rut)

    // Autocompletado con debounce al cambiar el RUT.
    LaunchedEffect(rut) {
        lookupError = null
        if (!rutValid) return@LaunchedEffect
        delay(500)
        isLooking = true
        val data = runCatching { onLookupRut(rut) }.getOrNull()
        isLooking = false
        if (data != null) {
            data.resolveRazonSocial()?.let { if (razonSocial.isBlank()) razonSocial = it }
            data.giro?.let { if (giro.isBlank()) giro = it }
            data.direccion?.let { if (direccion.isBlank()) direccion = it }
            data.comuna?.let { if (comuna.isBlank()) comuna = it }
            data.email?.let { if (email.isBlank()) email = it }
        } else {
            lookupError = "No se pudieron cargar los datos. Ingréselos manualmente."
        }
    }

    val canConfirm = rutValid &&
        razonSocial.isNotBlank() &&
        giro.isNotBlank() &&
        direccion.isNotBlank() &&
        comuna.isNotBlank()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Datos de la factura") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = rut,
                onValueChange = { rut = it },
                label = { Text("RUT receptor") },
                placeholder = { Text("12345678-9") },
                isError = rut.isNotBlank() && !rutValid,
                supportingText = {
                    when {
                        rut.isNotBlank() && !rutValid -> Text("RUT inválido")
                        isLooking -> Text("Buscando datos…")
                        lookupError != null -> Text(lookupError!!)
                    }
                },
                trailingIcon = { if (isLooking) CircularProgressIndicator(Modifier.size(20.dp)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            ReceptorField("Razón social", razonSocial) { razonSocial = it }
            ReceptorField("Giro", giro) { giro = it }
            ReceptorField("Dirección", direccion) { direccion = it }
            ReceptorField("Comuna", comuna) { comuna = it }
            ReceptorField("Email (opcional)", email, KeyboardType.Email) { email = it }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = {
                    onConfirm(
                        CheckoutHolder.ReceptorData(
                            rut = rut.trim(),
                            razonSocial = razonSocial.trim(),
                            giro = giro.trim(),
                            direccion = direccion.trim(),
                            comuna = comuna.trim(),
                            email = email.trim().ifBlank { null }
                        )
                    )
                },
                enabled = canConfirm,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar datos")
            }
        }
    }
}

@Composable
private fun ReceptorField(
    label: String,
    value: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    )
}
