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

private val DOCUMENT_TYPES = listOf(
    "Documento afecto",
    "Documento exento",
    "Factura afecta",
    "Factura exenta",
    "Sin documento"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    initialDocumentType: String = "Documento afecto",
    onConfirm: (String) -> Unit
) {
    var selected by remember { mutableStateOf(initialDocumentType) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documento") },
                navigationIcon = {
                    IconButton(onClick = { onConfirm(selected) }) {
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
            Text(
                text = "Modelo de emisión configurado para: Comprobante válido como boleta y emitir boleta electrónica solo para efectivo.",
                fontSize = 14.sp,
                color = Color(0xFF2E2E33),
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp)
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    DOCUMENT_TYPES.forEach { type ->
                        DocumentTypeRow(
                            label = type,
                            selected = selected == type,
                            onSelect = { selected = type }
                        )
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 16.dp, top = 4.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp
                ) {
                    Text(
                        text = "Predeterminado",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirm(selected) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text("Confirmar")
            }
        }
    }
}

@Composable
private fun DocumentTypeRow(
    label: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onSelect
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}
