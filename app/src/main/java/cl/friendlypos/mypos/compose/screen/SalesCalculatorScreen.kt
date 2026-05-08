package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.components.SalesCalcKeypad
import cl.friendlypos.mypos.data.DummyDataRepository
import cl.friendlypos.mypos.model.Product
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

private val POS_BLUE = Color(0xFF2196F3)

@Composable
fun SalesCalculatorScreen(
    viewModel: SalesCalculatorViewModel,
    onNavigateToCart: () -> Unit,
    onNavigateToPay: (totalAmount: Double) -> Unit,
    onPendingOperationError: () -> Unit
) {
    val currentAmount by viewModel.currentAmount.observeAsState("0")
    val totalAmount by viewModel.totalAmount.observeAsState("0")
    val currentItemName by viewModel.currentItemName.observeAsState("Nombre item 1")
    val cartItemCount by viewModel.cartItemCount.observeAsState(0)

    var showSearchModal by remember { mutableStateOf(false) }

    if (showSearchModal) {
        ProductSearchModal(
            onDismiss = { showSearchModal = false },
            onSelectProduct = { name, price ->
                viewModel.updateItemName(name)
                viewModel.setAmount(price.toString())
                showSearchModal = false
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Barcode button — top right, alone
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(
                onClick = { showSearchModal = true },
                modifier = Modifier
                    .padding(end = 8.dp)
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_barcode_scanner),
                    contentDescription = "Buscar producto",
                    tint = POS_BLUE,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Amount display — below the barcode button
        Text(
            text = "$$currentAmount",
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF333333),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )

        // Item name row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = null,
                tint = Color(0xFF666666),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = currentItemName,
                onValueChange = { viewModel.updateItemName(it) },
                placeholder = { Text("Item") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    color = Color(0xFF666666),
                    fontSize = 16.sp
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Keypad — 4 columns, fills remaining vertical space
        SalesCalcKeypad(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            onDigit = { viewModel.appendDigit(it) },
            onDoubleZero = {
                viewModel.appendDigit("0")
                viewModel.appendDigit("0")
            },
            onDecimal = { viewModel.appendDecimal(",") },
            onClear = { viewModel.clearEntry() },
            onDelete = { viewModel.deleteLastDigit() },
            onAdd = {
                viewModel.addItemToSale()
                viewModel.updateItemName(viewModel.getGenericItemName())
            },
            onMultiply = { viewModel.appendOperator("x") }
        )

        // Bottom bar — aligned with keypad (same horizontal padding + spacing)
        // Cart = weight(1f) matches one keypad column; PAGAR = weight(3f) matches three
        // Height 72dp = 50% more than the original ~48dp
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            // Cart button — same width as one numeric key
            Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                Button(
                    onClick = onNavigateToCart,
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE),
                    contentPadding = PaddingValues(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Carrito",
                        tint = Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                if (cartItemCount > 0) {
                    Badge(
                        modifier = Modifier.align(Alignment.TopEnd),
                        containerColor = Color(0xFFD32F2F)
                    ) {
                        Text(
                            text = if (cartItemCount > 99) "99" else cartItemCount.toString(),
                            color = Color.White,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // PAGAR button — spans three keypad columns
            Button(
                onClick = {
                    if (currentAmount != "0") {
                        onPendingOperationError()
                    } else {
                        val total = totalAmount.toDoubleOrNull() ?: 0.0
                        if (total > 0) onNavigateToPay(total)
                    }
                },
                modifier = Modifier.weight(3f).fillMaxHeight(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE),
                contentPadding = PaddingValues(horizontal = 20.dp)
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "PAGAR",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                    Text(
                        text = "$$totalAmount",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ProductSearchModal(
    onDismiss: () -> Unit,
    onSelectProduct: (name: String, price: Int) -> Unit
) {
    var query by remember { mutableStateOf("") }

    val allProducts by produceState(initialValue = emptyList<Product>()) {
        DummyDataRepository.getProducts().collect { value = it }
    }

    val filtered = remember(allProducts, query) {
        if (query.isBlank()) allProducts
        else allProducts.filter {
            it.name.contains(query, ignoreCase = true) ||
            it.description.contains(query, ignoreCase = true) ||
            it.category.contains(query, ignoreCase = true)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Buscar producto",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    placeholder = { Text("Nombre, categoría...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = Color(0xFF757575)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { query = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = Color(0xFF757575)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = POS_BLUE,
                        unfocusedBorderColor = Color(0xFFBDBDBD)
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    items(filtered, key = { it.id }) { product ->
                        ProductSearchRow(
                            product = product,
                            onClick = {
                                onSelectProduct(product.name, product.price.toInt())
                            }
                        )
                        HorizontalDivider(color = Color(0xFFE0E0E0))
                    }

                    if (filtered.isEmpty() && query.isNotBlank()) {
                        item {
                            Text(
                                "Sin resultados para \"$query\"",
                                color = Color(0xFF757575),
                                fontSize = 14.sp,
                                modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Cerrar", color = POS_BLUE)
                }
            }
        }
    }
}

@Composable
private fun ProductSearchRow(product: Product, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 4.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = product.name,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = Color(0xFF212121)
            )
            Text(
                text = product.category,
                fontSize = 12.sp,
                color = Color(0xFF757575)
            )
        }
        Text(
            text = "$${product.price.toInt()}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = POS_BLUE
        )
    }
}

@Composable
internal fun ProductSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.border(1.dp, Color(0xFFBDBDBD), RoundedCornerShape(8.dp)),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color(0xFF757575)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Buscar productos", fontSize = 14.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                )
            )
            if (query.isNotEmpty()) {
                IconButton(
                    onClick = { onQueryChange("") },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Limpiar",
                        tint = Color(0xFF757575),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
