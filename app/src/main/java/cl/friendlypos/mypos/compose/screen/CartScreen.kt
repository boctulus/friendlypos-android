package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.ui.sales.SaleItem
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

private val POS_BLUE = Color(0xFF2196F3)

@Composable
fun CartScreen(
    viewModel: SalesCalculatorViewModel,
    onNavigateBack: () -> Unit,
    onProceedToPayment: (totalAmount: Double) -> Unit
) {
    val allItems by viewModel.saleItems.observeAsState(emptyList())
    val searchQuery by viewModel.cartSearchQuery.observeAsState("")
    var showClearDialog by remember { mutableStateOf(false) }
    var editingItem by remember { mutableStateOf<SaleItem?>(null) }

    val filteredItems = remember(allItems, searchQuery) {
        if (searchQuery.isBlank()) allItems
        else allItems.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    val subtotal = remember(allItems) { allItems.sumOf { it.unitPrice * it.quantity } }
    val itemCount = remember(allItems) { allItems.sumOf { it.quantity } }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Limpiar Carrito") },
            text = { Text("¿Está seguro de que desea limpiar el carrito?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearCart()
                    showClearDialog = false
                }) { Text("Sí") }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("No") }
            }
        )
    }

    editingItem?.let { item ->
        EditItemDialog(
            item = item,
            onDismiss = { editingItem = null },
            onConfirm = { updated ->
                viewModel.updateSaleItem(updated)
                editingItem = null
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {

        // Document info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Doc.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("Boleta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(32.dp))
                    Text(
                        "39 Boleta Electronica",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Cliente:", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text("CLIENTE GENÉRICO SPyme", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }

        // Search bar (filters cart items)
        ProductSearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateCartSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
        )

        // Cart content
        if (allItems.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    painter = painterResource(R.drawable.logo_sq_empty),
                    contentDescription = null,
                    modifier = Modifier.size(200.dp),
                    tint = Color.Unspecified
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Canasta vacía",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(filteredItems, key = { it.id }) { item ->
                    SaleItemRow(
                        item = item,
                        onLongPress = { viewModel.removeSaleItem(item) },
                        onEdit = { editingItem = item }
                    )
                }
            }
        }

        // Bottom bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Card(
                modifier = Modifier.size(50.dp),
                shape = RoundedCornerShape(4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                onClick = { showClearDialog = true }
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar carrito",
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = POS_BLUE),
                onClick = {
                    if (subtotal > 0) onProceedToPayment(subtotal.toDouble())
                }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(itemCount.toString(), color = Color.White, fontSize = 12.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Subtotal", color = Color.White, fontSize = 16.sp)
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        "$$subtotal",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Pagar",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun SaleItemRow(
    item: SaleItem,
    onLongPress: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .combinedClickable(onLongClick = onLongPress, onClick = {}),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF212121)
                )
                Text(
                    text = "$${item.unitPrice}",
                    fontSize = 14.sp,
                    color = Color(0xFF2E2E33)
                )
                Text(
                    text = "Cantidad: ${item.quantity}",
                    fontSize = 14.sp,
                    color = Color(0xFF2E2E33)
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Editar",
                        tint = POS_BLUE,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "$${item.unitPrice * item.quantity}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF2E2E33)
                )
            }
        }
    }
}

@Composable
private fun EditItemDialog(
    item: SaleItem,
    onDismiss: () -> Unit,
    onConfirm: (SaleItem) -> Unit
) {
    var name by remember { mutableStateOf(item.name) }
    var unitPrice by remember { mutableStateOf(item.unitPrice.toString()) }
    var quantity by remember { mutableStateOf(item.quantity.toString()) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Editar item",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = unitPrice,
                    onValueChange = { unitPrice = it.filter { c -> c.isDigit() } },
                    label = { Text("Precio unitario") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it.filter { c -> c.isDigit() } },
                    label = { Text("Cantidad") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    "¿Guardar cambios?",
                    fontSize = 16.sp,
                    color = Color(0xFF2E2E33),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = POS_BLUE
                        )
                    ) { Text("No") }

                    Button(
                        onClick = {
                            val price = unitPrice.toIntOrNull() ?: item.unitPrice
                            val qty = quantity.toIntOrNull()?.coerceAtLeast(1) ?: item.quantity
                            onConfirm(item.copy(name = name.ifBlank { item.name }, unitPrice = price, quantity = qty))
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE)
                    ) { Text("Sí") }
                }
            }
        }
    }
}
