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
import cl.friendlypos.mypos.checkout.DocumentType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillingScreen(
    initialDocumentType: DocumentType = DocumentType.DEFAULT,
    onConfirm: (DocumentType) -> Unit
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
                text = "Modelo de emisión configurado para: Comprobante válido como boleta y emitir boleta electrónica.",
                fontSize = 14.sp,
                color = Color(0xFF2E2E33),
                modifier = Modifier.padding(horizontal = 30.dp, vertical = 12.dp)
            )

            Column(modifier = Modifier.fillMaxWidth()) {
                DocumentType.entries.forEach { type ->
                    DocumentTypeRow(
                        label = type.label,
                        selected = selected == type,
                        isDefault = type == DocumentType.DEFAULT,
                        onSelect = { selected = type }
                    )
                    HorizontalDivider(color = Color(0xFFE0E0E0))
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
    isDefault: Boolean,
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
        if (isDefault) {
            Text(
                text = "Predeterminado",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
