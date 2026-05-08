package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
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

@Composable
fun HomeScreen(
    onNavigateToNewSale: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToCustomers: () -> Unit,
    onNavigateToReports: () -> Unit
) {
    val tiles = listOf(
        HomeTile(R.drawable.ic_new_sale, "Nueva Venta", Color(0xFF4CAF50), onNavigateToNewSale),
        HomeTile(R.drawable.ic_inventory, "Inventario", Color(0xFF2196F3), onNavigateToInventory),
        HomeTile(R.drawable.ic_customers, "Clientes", Color(0xFF9C27B0), onNavigateToCustomers),
        HomeTile(R.drawable.ic_reports, "Reportes", Color(0xFFFF9800), onNavigateToReports),
    )

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
