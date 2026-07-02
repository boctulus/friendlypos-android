package cl.friendlypos.mypos.compose.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla de reintento cuando la emisión del DTE falla. La venta YA está registrada;
 * solo se reintenta la emisión o se cierra como ticket sin valor tributario.
 */
@Composable
fun DteRetryScreen(
    message: String,
    isProcessing: Boolean,
    onRetry: () -> Unit,
    onContinueWithout: () -> Unit
) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = Color(0xFFE65100),
                modifier = Modifier.size(56.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "No se pudo emitir el documento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color(0xFF555555),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "La venta quedó registrada. Puedes reintentar la emisión o continuar sin documento tributario.",
                fontSize = 13.sp,
                color = Color(0xFF777777),
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            if (isProcessing) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = onRetry,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Reintentar emisión")
                }
                Spacer(Modifier.height(12.dp))
                OutlinedButton(
                    onClick = onContinueWithout,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continuar sin documento")
                }
            }
        }
    }
}
