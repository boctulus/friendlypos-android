package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.model.TicketLine

/**
 * Previsualización en pantalla de un [Ticket] (etapa 1, sin impresión física).
 * Simula un comprobante térmico: tipografía monoespaciada y ancho acotado.
 */
@Composable
fun TicketPreviewScreen(
    ticket: Ticket,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFECEFF1))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Vista previa del comprobante",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!ticket.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ticket.logoUrl,
                        contentDescription = "Logo",
                        modifier = Modifier
                            .height(64.dp)
                            .widthIn(max = 200.dp)
                            .padding(bottom = 8.dp),
                        contentScale = ContentScale.Fit
                    )
                }

                CenteredText(ticket.storeName, bold = true, size = 16)
                Spacer(Modifier.height(2.dp))
                CenteredText(ticket.title, bold = true, size = 14)
                CenteredText(ticket.generatedAt, size = 12, color = Color(0xFF666666))

                Dashed()
                ticket.headerLines.forEach { Row(it) }

                if (ticket.bodyLines.isNotEmpty()) {
                    Dashed()
                    ticket.bodyLines.forEach { Row(it) }
                }

                if (ticket.totalLines.isNotEmpty()) {
                    Dashed()
                    ticket.totalLines.forEach { Row(it) }
                }

                ticket.footer?.let {
                    Dashed()
                    CenteredText(it, size = 12, color = Color(0xFF666666))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {},
                enabled = false,
                modifier = Modifier.weight(1f)
            ) {
                Text("Imprimir (próximamente)", fontSize = 13.sp)
            }
            Button(
                onClick = onClose,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cerrar", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun Row(line: TicketLine) {
    val weight = if (line.emphasize) FontWeight.Bold else FontWeight.Normal
    if (line.value == null) {
        Text(
            text = line.label,
            fontFamily = FontFamily.Monospace,
            fontSize = 13.sp,
            fontWeight = weight,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
        )
    } else {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(line.label, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = weight)
            Text(line.value, fontFamily = FontFamily.Monospace, fontSize = 13.sp, fontWeight = weight)
        }
    }
}

@Composable
private fun CenteredText(text: String, bold: Boolean = false, size: Int = 13, color: Color = Color.Black) {
    Text(
        text = text,
        fontFamily = FontFamily.Monospace,
        fontSize = size.sp,
        fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
        color = color,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun Dashed() {
    Text(
        text = "--------------------------------",
        fontFamily = FontFamily.Monospace,
        fontSize = 13.sp,
        color = Color(0xFF999999),
        maxLines = 1,
        modifier = Modifier.padding(vertical = 6.dp)
    )
}
