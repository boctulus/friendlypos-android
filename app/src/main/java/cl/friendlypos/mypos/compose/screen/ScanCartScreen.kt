package cl.friendlypos.mypos.compose.screen

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cl.friendlypos.mypos.compose.components.EditItemDialog
import cl.friendlypos.mypos.compose.components.SaleItemRow
import cl.friendlypos.mypos.scanner.CameraPreview
import cl.friendlypos.mypos.ui.sales.SaleItem
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

private val POS_BLUE = Color(0xFF2196F3)

/**
 * Pantalla "escáner + carrito": escanea EAN13 y agrega productos al carrito compartido.
 * Muestra el carrito COMPLETO con el ítem más reciente arriba (orden solo de UI).
 * Al cerrar vuelve a la calculadora con el carrito ya poblado.
 *
 * Reusa [CameraPreview] (mismo motor que "Probar Scanner") y las filas/edición del carrito.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanCartScreen(
    viewModel: SalesCalculatorViewModel,
    storeId: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val allItems by viewModel.saleItems.observeAsState(emptyList())
    val totalAmount by viewModel.totalAmount.observeAsState("0")
    val itemCount by viewModel.cartItemCount.observeAsState(0)

    val snackbarHostState = remember { SnackbarHostState() }
    var editingItem by remember { mutableStateOf<SaleItem?>(null) }

    var isScanning by remember { mutableStateOf(true) }

    // Anti-rebote local (el guard de lookups concurrentes vive en el ViewModel).
    var lastScanned by remember { mutableStateOf("") }
    var lastScanTime by remember { mutableStateOf(0L) }

    // Carrito completo, más reciente arriba (solo presentación; no se reordena la lista interna).
    val displayItems = remember(allItems) { allItems.asReversed() }

    LaunchedEffect(Unit) {
        viewModel.scanFeedback.collect { feedback ->
            when (feedback) {
                is SalesCalculatorViewModel.ScanFeedback.Added -> {
                    vibrate(context, longBuzz = false)
                    snackbarHostState.showSnackbar("Agregado: ${feedback.name}")
                }
                is SalesCalculatorViewModel.ScanFeedback.NotFound -> {
                    vibrate(context, longBuzz = true)
                    snackbarHostState.showSnackbar("Producto no encontrado: ${feedback.code}")
                }
                is SalesCalculatorViewModel.ScanFeedback.Error -> {
                    vibrate(context, longBuzz = true)
                    snackbarHostState.showSnackbar("Error: ${feedback.message}")
                }
            }
        }
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Escanear al carrito") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Zona de cámara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.1f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    enabled = isScanning,
                    onBarcodeDetected = { code ->
                        val now = System.currentTimeMillis()
                        if (code != lastScanned || now - lastScanTime > 2000) {
                            lastScanned = code
                            lastScanTime = now
                            viewModel.scanBarcode(code, storeId)
                        }
                    }
                )

                Box(
                    modifier = Modifier
                        .size(width = 250.dp, height = 150.dp)
                        .border(
                            width = 2.dp,
                            color = if (isScanning) Color.Green else Color.Red,
                            shape = RoundedCornerShape(8.dp)
                        )
                )
            }

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)

            // Header del carrito
            Surface(color = Color.Gray.copy(alpha = 0.1f)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Carrito ($itemCount)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$$totalAmount",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = POS_BLUE
                    )
                }
            }

            // Carrito completo (más reciente arriba)
            if (displayItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Escanea un producto para agregarlo",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(displayItems, key = { it.id }) { item ->
                        SaleItemRow(
                            item = item,
                            onLongPress = { viewModel.removeSaleItem(item) },
                            onEdit = { editingItem = item }
                        )
                    }
                }
            }

            // Botón cerrar (vuelve a la calculadora con el carrito poblado)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = onClose,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = POS_BLUE)
                ) {
                    Text("Listo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun vibrate(context: Context, longBuzz: Boolean) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator ?: return
    val ms = if (longBuzz) 300L else 100L
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(ms)
    }
}
