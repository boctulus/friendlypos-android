package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.theme.AppColors

@Composable
fun HomeScreen(
    role: String,
    isSessionOpen: Boolean?,
    onNavigateToNewSale: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit,
    onNavigateToCashbox: () -> Unit,
    onNavigateToOpenCashbox: () -> Unit,
    onNavigateToCloseCashbox: () -> Unit,
    onNavigateToTickets: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val isAdmin = role == "admin"

    val tiles = if (isAdmin) {
        listOf(
            HomeTile(R.drawable.ic_reports, "Reportes", Color(0xFFFF9800), onNavigateToReports),
            HomeTile(R.drawable.ic_settings, "Configuración", Color(0xFF607D8B), onNavigateToSettings)
        )
    } else {
        listOf(
            HomeTile(R.drawable.ic_new_sale, "Nueva Venta", AppColors.BrandPrimary, onNavigateToNewSale),
            HomeTile(R.drawable.ic_inventory, "Inventario", Color(0xFF2196F3), onNavigateToInventory),
            HomeTile(R.drawable.ic_cash, "Caja", Color(0xFF009688), onNavigateToCashbox),
            HomeTile(R.drawable.ic_sales, "Tickets", Color(0xFFE91E63), onNavigateToTickets),
            HomeTile(R.drawable.ic_customers, "Clientes", Color(0xFF9C27B0), onNavigateToCustomers),
            HomeTile(R.drawable.ic_reports, "Reportes", Color(0xFFFF9800), onNavigateToReports)
        )
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(tiles.size) { index ->
            DashboardTile(tile = tiles[index])
        }

        if (!isAdmin) {
            item(span = { GridItemSpan(2) }) {
                Text(
                    text = "Acciones rápidas",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF444444),
                    modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                )
            }

            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onNavigateToOpenCashbox,
                        enabled = isSessionOpen == false,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.BrandPrimary,
                            disabledContainerColor = AppColors.BrandPrimary.copy(alpha = 0.38f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Text("Abrir Caja", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                    Button(
                        onClick = onNavigateToCloseCashbox,
                        enabled = isSessionOpen == true,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE53935),
                            disabledContainerColor = Color(0xFFE53935).copy(alpha = 0.38f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        Text("Cerrar Caja", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

private data class HomeTile(
    val iconRes: Int,
    val title: String,
    val color: Color,
    val onClick: () -> Unit
)

@Composable
private fun DashboardTile(tile: HomeTile) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { tile.onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = tile.color)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = tile.iconRes),
                contentDescription = tile.title,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = tile.title,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
