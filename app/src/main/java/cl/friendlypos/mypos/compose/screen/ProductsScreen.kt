package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.friendlypos.mypos.compose.viewmodel.ProductsViewModel
import cl.friendlypos.mypos.model.Product

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductsScreen(
    viewModel: ProductsViewModel = viewModel()
) {
    val products by viewModel.filteredProducts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Productos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Buscador
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.updateSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Buscar productos...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Buscar",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { viewModel.clearSearch() }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Limpiar búsqueda",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(products) { product ->
                    ProductItemCard(product = product)
                }
            }
        }
    }
}

@Composable
fun ProductItemCard(product: Product) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = product.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Precio: $${String.format("%.2f", product.price)}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}