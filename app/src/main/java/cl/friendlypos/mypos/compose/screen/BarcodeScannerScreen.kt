package cl.friendlypos.mypos.compose.screen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.friendlypos.mypos.compose.viewmodel.BarcodeScannerViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    onBackPressed: () -> Unit,
    viewModel: BarcodeScannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val scannedBarcodes by viewModel.scannedBarcodes.collectAsState()
    val isScanning by viewModel.isScanning.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Códigos de Barra") },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleScanning() }
                    ) {
                        Icon(
                            imageVector = if (isScanning) Icons.Filled.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isScanning) "Pausar escaneo" else "Reanudar escaneo"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Zona del escáner (simula la cámara)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                // Simular vista de cámara con un color de fondo
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Marco de escaneo
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
                
                // Indicador de estado
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(20.dp)
                        .background(
                            color = if (isScanning) Color.Green else Color.Red,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (isScanning) "ESCANEANDO" else "PAUSADO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Divider(color = Color.Gray.copy(alpha = 0.3f), thickness = 1.dp)
            
            // Lista de códigos escaneados
            Column(
                modifier = Modifier.weight(2f)
            ) {
                // Header de la lista
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Códigos escaneados (${scannedBarcodes.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (scannedBarcodes.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearAllBarcodes() }
                            ) {
                                Text("Limpiar todo")
                            }
                        }
                    }
                }
                
                // Lista de códigos
                if (scannedBarcodes.isEmpty()) {
                    // Estado vacío
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "No hay códigos escaneados",
                                fontSize = 16.sp,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Apunta el código de barras hacia la cámara",
                                fontSize = 14.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    // Lista de códigos
                    LazyColumn {
                        itemsIndexed(scannedBarcodes) { index, barcode ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 4.dp)
                                    .clickable {
                                        // Copiar código individual
                                        copyToClipboard(context, barcode)
                                        showSnackbar(context, "Código copiado al portapapeles")
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Número del código
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${index + 1}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.width(16.dp))
                                    
                                    // Código de barras
                                    Text(
                                        text = barcode,
                                        modifier = Modifier.weight(1f),
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 16.sp
                                    )
                                    
                                    // Botón eliminar
                                    IconButton(
                                        onClick = { viewModel.removeBarcode(index) }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.DeleteOutline,
                                            contentDescription = "Eliminar código"
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Botones de acción
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (scannedBarcodes.isEmpty()) {
                                showSnackbar(context, "No hay códigos para copiar")
                            } else {
                                val allCodes = viewModel.getAllBarcodesAsText()
                                copyToClipboard(context, allCodes)
                                showSnackbar(context, "${scannedBarcodes.size} código(s) copiado(s) al portapapeles")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ContentCopy,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copiar")
                    }
                    
                    Button(
                        onClick = onBackPressed,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cerrar")
                    }
                }
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Códigos de barras", text)
    clipboardManager.setPrimaryClip(clip)
}

private fun showSnackbar(context: Context, message: String) {
    // Aquí podrías implementar tu sistema de notificaciones personalizado
    // Por ahora, esto es un placeholder para la funcionalidad de snackbar
    // Puedes usar el TopSnackbar que ya tienes en tu proyecto
}

// Función para simular la vibración cuando se escanea un código
private fun vibrateDevice(context: Context) {
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(100)
    }
}