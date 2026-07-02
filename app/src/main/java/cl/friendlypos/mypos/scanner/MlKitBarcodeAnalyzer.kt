package cl.friendlypos.mypos.scanner

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

/**
 * Callback de un código detectado. Abstrae la fuente del escaneo: hoy lo emite
 * [MlKitBarcodeAnalyzer] (cámara + ML Kit); un futuro adapter de lector físico
 * (p.ej. terminal P8 Neo) puede emitir por esta misma interfaz sin tocar la UI.
 */
fun interface BarcodeListener {
    fun onBarcode(value: String)
}

/**
 * Analizador de frames de CameraX que detecta códigos EAN13 (y EAN8/UPC-A como apoyo)
 * con ML Kit y los entrega por [listener]. La deduplicación/anti-rebote se maneja
 * en la capa de UI.
 */
class MlKitBarcodeAnalyzer(
    private val listener: BarcodeListener
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_EAN_13,
                Barcode.FORMAT_EAN_8,
                Barcode.FORMAT_UPC_A
            )
            .build()
    )

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }
        val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(input)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull { !it.rawValue.isNullOrBlank() }
                    ?.rawValue
                    ?.let { listener.onBarcode(it) }
            }
            .addOnCompleteListener { imageProxy.close() }
    }
}
