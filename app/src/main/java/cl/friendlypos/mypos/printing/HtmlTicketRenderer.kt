package cl.friendlypos.mypos.printing

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.ceil

class HtmlTicketRenderer(private val context: Context) {

    @Suppress("DEPRECATION")
    suspend fun capture(webView: WebView): Bitmap = withContext(Dispatchers.Main.immediate) {
        withTimeout(RENDER_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val completed = AtomicBoolean(false)

                fun complete(result: Result<Bitmap>) {
                    if (!completed.compareAndSet(false, true) || !continuation.isActive) {
                        result.getOrNull()?.recycle()
                        return
                    }
                    result.fold(
                        onSuccess = { continuation.resume(it) },
                        onFailure = { continuation.resumeWithException(it) }
                    )
                }

                continuation.invokeOnCancellation {
                    completed.compareAndSet(false, true)
                }

                if (!webView.isAttachedToWindow) {
                    complete(
                        Result.failure(
                            IllegalStateException("La vista previa del ticket no esta adjunta")
                        )
                    )
                    return@suspendCancellableCoroutine
                }

                webView.postVisualStateCallback(
                    VISIBLE_WEB_VIEW_REQUEST_ID,
                    object : WebView.VisualStateCallback() {
                        override fun onComplete(requestId: Long) {
                            webView.post {
                                complete(
                                    runCatching {
                                        val picture = webView.capturePicture()
                                        require(picture.width > 0 && picture.height > 0) {
                                            "La vista previa no produjo contenido capturable"
                                        }
                                        val scale = PRINT_WIDTH_PX.toFloat() / picture.width
                                        val height = ceil(picture.height * scale).toInt()
                                            .coerceAtLeast(1)
                                        require(height <= MAX_RENDER_HEIGHT_PX) {
                                            "El ticket es demasiado largo para renderizarse ($height px)"
                                        }

                                        Bitmap.createBitmap(
                                            PRINT_WIDTH_PX,
                                            height,
                                            Bitmap.Config.RGB_565
                                        ).also { bitmap ->
                                            Canvas(bitmap).apply {
                                                drawColor(Color.WHITE)
                                                scale(scale, scale)
                                                picture.draw(this)
                                            }
                                            require(bitmap.hasPrintableContent()) {
                                                bitmap.recycle()
                                                "La vista previa produjo un bitmap completamente blanco"
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                )
            }
        }
    }

    suspend fun render(html: String): Bitmap = withContext(Dispatchers.Main.immediate) {
        withTimeout(RENDER_TIMEOUT_MILLIS) {
            renderOnMain(prepareHtml(html))
        }
    }

    private suspend fun renderOnMain(html: String): Bitmap =
        suspendCancellableCoroutine { continuation ->
            val completed = AtomicBoolean(false)
            val webView = WebView(context)
            val activity = context as? Activity
                ?: return@suspendCancellableCoroutine continuation.resumeWithException(
                    IllegalStateException("No se pudo adjuntar el renderer HTML a la pantalla")
                )

            activity.addContentView(
                webView,
                ViewGroup.LayoutParams(PRINT_WIDTH_PX, ATTACHED_RENDER_HEIGHT_PX)
            )
            webView.translationX = -PRINT_WIDTH_PX.toFloat()

            fun destroyWebView() {
                (webView.parent as? ViewGroup)?.removeView(webView)
                webView.stopLoading()
                webView.webViewClient = WebViewClient()
                webView.loadUrl("about:blank")
                webView.clearHistory()
                webView.removeAllViews()
                webView.destroy()
            }

            fun fail(error: Throwable) {
                if (!completed.compareAndSet(false, true)) return
                destroyWebView()
                if (continuation.isActive) continuation.resumeWithException(error)
            }

            fun layoutForCapture(): Int {
                val widthSpec = View.MeasureSpec.makeMeasureSpec(
                    PRINT_WIDTH_PX,
                    View.MeasureSpec.EXACTLY
                )
                val unspecifiedHeightSpec = View.MeasureSpec.makeMeasureSpec(
                    0,
                    View.MeasureSpec.UNSPECIFIED
                )

                webView.measure(widthSpec, unspecifiedHeightSpec)
                val contentHeight = ceil(webView.contentHeight * webView.scale).toInt()
                // contentHeight subestima la cola (recuadros/bordes con mucho contenido
                // final, como el cierre de caja) y corta abajo. capturePicture() reporta
                // la altura realmente dibujada, que es la fuente confiable. Se toma el
                // mayor de las tres medidas mas un margen de seguridad en blanco.
                val pictureHeight = runCatching { webView.capturePicture().height }.getOrDefault(0)
                val height = maxOf(webView.measuredHeight, contentHeight, pictureHeight, 1) +
                    BOTTOM_SAFETY_PX

                require(height <= MAX_RENDER_HEIGHT_PX) {
                    "El ticket es demasiado largo para renderizarse ($height px)"
                }

                webView.layoutParams = webView.layoutParams.apply {
                    width = PRINT_WIDTH_PX
                    this.height = height
                }
                val exactHeightSpec = View.MeasureSpec.makeMeasureSpec(
                    height,
                    View.MeasureSpec.EXACTLY
                )
                webView.measure(widthSpec, exactHeightSpec)
                webView.layout(0, 0, PRINT_WIDTH_PX, height)
                return height
            }

            fun capture(height: Int) {
                if (!completed.compareAndSet(false, true)) return

                runCatching {
                    Bitmap.createBitmap(
                        PRINT_WIDTH_PX,
                        height,
                        Bitmap.Config.RGB_565
                    ).also { bitmap ->
                        Canvas(bitmap).apply {
                            drawColor(Color.WHITE)
                            webView.draw(this)
                        }
                        require(bitmap.hasPrintableContent()) {
                            bitmap.recycle()
                            "El renderer HTML produjo un bitmap completamente blanco"
                        }
                    }
                }.fold(
                    onSuccess = { bitmap ->
                        destroyWebView()
                        if (continuation.isActive) {
                            continuation.resume(bitmap)
                        } else {
                            bitmap.recycle()
                        }
                    },
                    onFailure = { error ->
                        destroyWebView()
                        if (continuation.isActive) continuation.resumeWithException(error)
                    }
                )
            }

            continuation.invokeOnCancellation {
                if (completed.compareAndSet(false, true)) {
                    webView.post { destroyWebView() }
                }
            }

            webView.apply {
                setBackgroundColor(Color.WHITE)
                setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                isHorizontalScrollBarEnabled = false
                isVerticalScrollBarEnabled = false

                settings.apply {
                    javaScriptEnabled = false
                    useWideViewPort = false
                    loadWithOverviewMode = false
                    textZoom = 100
                    setSupportZoom(false)
                    builtInZoomControls = false
                    displayZoomControls = false
                    allowFileAccess = true
                    allowContentAccess = true
                    blockNetworkImage = false
                    offscreenPreRaster = true
                }

                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, url: String?) {
                        super.onPageFinished(view, url)
                        view.post {
                            runCatching { layoutForCapture() }.fold(
                                onSuccess = { height ->
                                    view.postOnAnimation {
                                        view.postVisualStateCallback(
                                            VISUAL_STATE_REQUEST_ID,
                                            object : WebView.VisualStateCallback() {
                                                override fun onComplete(requestId: Long) {
                                                    view.post { capture(height) }
                                                }
                                            }
                                        )
                                    }
                                },
                                onFailure = { fail(it) }
                            )
                        }
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        super.onReceivedError(view, request, error)
                        if (request.isForMainFrame) {
                            val description = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                error.description?.toString().orEmpty()
                            } else {
                                ""
                            }
                            fail(
                                IllegalStateException(
                                    "No se pudo renderizar el ticket HTML ${description}".trim()
                                )
                            )
                        }
                    }
                }

                loadDataWithBaseURL(
                    ASSET_BASE_URL,
                    html,
                    "text/html",
                    "UTF-8",
                    null
                )
            }
        }

    private fun prepareHtml(html: String): String {
        val printStyle = """
            <style id="friendlypos-thermal-print">
                html, body {
                    width: 100% !important;
                    max-width: 100% !important;
                    margin: 0 !important;
                    overflow: hidden !important;
                    background: #fff !important;
                }
                img, table {
                    max-width: 100% !important;
                }
            </style>
        """.trimIndent()

        return if (html.contains("</head>", ignoreCase = true)) {
            html.replaceFirst(
                Regex("(?i)</head>"),
                "$printStyle\n</head>"
            )
        } else {
            "$printStyle\n$html"
        }
    }

    private fun Bitmap.hasPrintableContent(): Boolean {
        val row = IntArray(width)
        for (y in 0 until height step PIXEL_SAMPLE_STEP) {
            getPixels(row, 0, width, 0, y, width, 1)
            if (row.any { pixel ->
                    Color.red(pixel) < WHITE_THRESHOLD ||
                        Color.green(pixel) < WHITE_THRESHOLD ||
                        Color.blue(pixel) < WHITE_THRESHOLD
                }
            ) {
                return true
            }
        }
        return false
    }

    private companion object {
        private const val PRINT_WIDTH_PX = 384
        private const val BOTTOM_SAFETY_PX = 120
        private const val ATTACHED_RENDER_HEIGHT_PX = 1
        private const val MAX_RENDER_HEIGHT_PX = 16_000
        private const val RENDER_TIMEOUT_MILLIS = 15_000L
        private const val VISUAL_STATE_REQUEST_ID = 1L
        private const val VISIBLE_WEB_VIEW_REQUEST_ID = 2L
        private const val PIXEL_SAMPLE_STEP = 2
        private const val WHITE_THRESHOLD = 245
        private const val ASSET_BASE_URL = "file:///android_asset/"
    }
}
