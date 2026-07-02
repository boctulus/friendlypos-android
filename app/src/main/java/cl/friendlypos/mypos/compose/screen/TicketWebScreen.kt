package cl.friendlypos.mypos.compose.screen

import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cl.friendlypos.mypos.hardware.PrinterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun TicketWebScreen(
    html: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var isPrinting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        AndroidView(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            factory = { ctx ->
                WebView(ctx).apply {
                    webViewClient = WebViewClient()
                    settings.apply {
                        javaScriptEnabled = false
                        useWideViewPort = false
                        loadWithOverviewMode = false
                        textZoom = 100
                        setSupportZoom(false)
                        builtInZoomControls = false
                        displayZoomControls = false
                    }
                    setBackgroundColor(android.graphics.Color.WHITE)
                }
            },
            update = { webView ->
                webView.loadDataWithBaseURL(
                    "about:blank",
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    if (isPrinting) return@OutlinedButton
                    isPrinting = true
                    scope.launch {
                        val result = withContext(Dispatchers.IO) {
                            PrinterManager(context).printHtml(html)
                        }
                        isPrinting = false
                        val message = result.fold(
                            onSuccess = { "Ticket enviado a impresora" },
                            onFailure = { it.message ?: "No se pudo imprimir" }
                        )
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = !isPrinting,
                modifier = Modifier.weight(1f)
            ) {
                Text(if (isPrinting) "Imprimiendo..." else "Imprimir", fontSize = 13.sp)
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
