package cl.friendlypos.mypos.compose.screen

import android.graphics.Color
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import cl.friendlypos.mypos.hardware.PrinterManager
import cl.friendlypos.mypos.printing.HtmlTicketRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val SCROLL_STEP_MILLIS = 45L

@Composable
fun TicketWebScreen(
    html: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val renderer = remember(context) { HtmlTicketRenderer(context) }
    val printer = remember(context) { PrinterManager(context) }
    var isPrinting by remember { mutableStateOf(false) }
    var previewWebView by remember { mutableStateOf<WebView?>(null) }

    // Efecto tipo POS: mientras imprime, la preview se desplaza sola hacia arriba
    // (el contenido sube, como si el papel fuera saliendo). Al terminar vuelve arriba.
    LaunchedEffect(isPrinting) {
        val webView = previewWebView ?: return@LaunchedEffect
        if (!isPrinting) {
            webView.scrollTo(0, 0)
            return@LaunchedEffect
        }
        webView.scrollTo(0, 0)
        val target = ((webView.contentHeight * webView.scale).toInt() - webView.height)
            .coerceAtLeast(0)
        val steps = 80
        for (i in 1..steps) {
            webView.scrollTo(0, target * i / steps)
            delay(SCROLL_STEP_MILLIS)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(top = 8.dp, bottom = 16.dp)
    ) {
        AndroidView(
            // La preview es solo visual (la impresion rasteriza offscreen a 384 px).
            // Se angosta al 75% y se centra para que se parezca al ticket impreso.
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(0.75f)
                .align(Alignment.CenterHorizontally),
            factory = { ctx ->
                WebView(ctx).apply {
                    previewWebView = this
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
                    setBackgroundColor(Color.WHITE)
                }
            },
            update = { webView ->
                previewWebView = webView
                webView.loadDataWithBaseURL(
                    "file:///android_asset/",
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
                        val result = runCatching {
                            // Rasteriza offscreen a exactamente 384 px (sin reescalar el
                            // WebView visible, que se renderiza al ancho de pantalla y
                            // encogia la fuente y adelgazaba los trazos al bajar a 384 px).
                            val bitmap = renderer.render(html)
                            try {
                                withContext(Dispatchers.IO) {
                                    printer.printBitmap(bitmap).getOrThrow()
                                }
                            } finally {
                                bitmap.recycle()
                            }
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
