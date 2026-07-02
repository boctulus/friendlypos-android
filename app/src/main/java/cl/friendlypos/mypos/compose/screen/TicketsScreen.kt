package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.api.dto.SaleDto
import cl.friendlypos.mypos.compose.viewmodel.TicketsViewModel

@Composable
fun TicketsScreen(viewModel: TicketsViewModel = viewModel()) {
    val sales by viewModel.sales.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isLoadingTicket by viewModel.isLoadingTicket.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val selectedTicket by viewModel.selectedTicket.collectAsState()
    val context = LocalContext.current

    val selectedTicketHtml by viewModel.selectedTicketHtml.collectAsState()
    val storeName = remember { SessionManager.get(context)?.storeId ?: "FriendlyPOS" }

    if (selectedTicketHtml != null) {
        TicketWebScreen(html = selectedTicketHtml!!, onClose = { viewModel.clearSelectedTicket() })
        return
    }

    if (selectedTicket != null) {
        TicketPreviewScreen(ticket = selectedTicket!!, onClose = { viewModel.clearSelectedTicket() })
        return
    }

    if (isLoadingTicket) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Tickets de Venta",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = { viewModel.loadSales() }) {
                Icon(Icons.Default.Refresh, contentDescription = "Recargar")
            }
        }

        if (errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    errorMessage ?: "",
                    modifier = Modifier.padding(12.dp),
                    color = Color(0xFFB71C1C),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return
        }

        if (sales.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Receipt,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = Color(0xFFBBBBBB)
                    )
                    Spacer(Modifier.height(12.dp))
                    Text("Sin tickets registrados", color = Color(0xFF888888))
                }
            }
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sales) { sale ->
                SaleTicketCard(
                    sale = sale,
                    onClick = { viewModel.loadSaleTicket(sale, storeName) }
                )
            }
        }
    }
}

@Composable
private fun SaleTicketCard(sale: SaleDto, onClick: () -> Unit) {
    val ticketLabel = sale.ticketNumber ?: "#${sale.id.takeLast(6)}"
    val dateLabel = sale.createdAt?.take(10) ?: "—"
    val total = sale.total?.toDoubleOrNull() ?: 0.0
    val paymentLabel = when (sale.paymentMethod?.lowercase()) {
        "cash" -> "Efectivo"
        "transfer" -> "Transferencia"
        "card" -> "Tarjeta"
        "mixed" -> "Mixto"
        else -> sale.paymentMethod ?: sale.tipoDocumento ?: "—"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_sales),
                contentDescription = null,
                tint = Color(0xFFE91E63),
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    ticketLabel,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "${sale.customer?.name ?: sale.customer?.rut ?: "Sin cliente"} · $dateLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF888888)
                )
                Text(
                    paymentLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF666666)
                )
            }
            Text(
                "$ ${String.format("%,.0f", total)}",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
