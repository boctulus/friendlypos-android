package cl.friendlypos.mypos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import com.zcs.sdk.DriverManager
import com.zcs.sdk.SdkResult
import com.zcs.sdk.Sys
//import com.zcs.sdk.barcode.BarcodeManager
//import com.zcs.sdk.barcode.BarcodeManager.OnBarCodeResultListener

import cl.friendlypos.mypos.utils.SystemUtils
// import cl.friendlypos.mypos.compose.screen.BarcodeScannerScreen
import cl.friendlypos.mypos.compose.screen.BarcodeScannerDemoScreen
import cl.friendlypos.mypos.compose.viewmodel.BarcodeScannerViewModel

class ScannerActivity : ComponentActivity() {
    private lateinit var mDriverManager: DriverManager
    private lateinit var mSys: Sys

    private val viewModel: BarcodeScannerViewModel by viewModels()
//    private lateinit var mBarcodeManager: BarcodeManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!SystemUtils.isEmulator()){
            // Inicializa el SDK
            initSdk()
        }

        setContent {
            BarcodeScannerDemoScreen(
                onBackPressed = { finish() }, // Cierra esta actividad al presionar "atrás"
                viewModel = viewModel
            )
        }
    }

    private fun initSdk() {
        // Inicializa el DriverManager y el sistema base
        mDriverManager = DriverManager.getInstance()
        mSys = mDriverManager.baseSysDevice
        val status = mSys.sdkInit()

        if (status != SdkResult.SDK_OK) {
            Log.e("ScannerActivity", "Error al inicializar el SDK: $status")
            return
        }

        // Inicializa el BarcodeManager para manejar el escáner
//        mBarcodeManager = mDriverManager.barcodeDevice
//        if (mBarcodeManager == null) {
//            Log.e("ScannerActivity", "No se pudo obtener el BarcodeManager")
//            return
//        }
//
//        // Configura el listener para recibir códigos de barras escaneados
//        mBarcodeManager.setOnBarCodeResultListener(object : OnBarCodeResultListener {
//            override fun onBarCodeResult(barcode: String?) {
//                barcode?.let {
//                    Log.d("ScannerActivity", "Código escaneado: $it")
//                    viewModel.addBarcode(it) // Agrega el código al ViewModel
//                }
//            }
//        })
    }

    override fun onResume() {
        super.onResume()
        // Inicia el escáner
//        val result = mBarcodeManager.open()
//        if (result == SdkResult.SDK_OK) {
//            mBarcodeManager.startScan()
//            Log.d("ScannerActivity", "Escaneo iniciado.")
//        } else {
//            Log.e("ScannerActivity", "Error al abrir el escáner: $result")
//        }
    }

    override fun onPause() {
        super.onPause()
        // Detiene el escaneo para ahorrar batería
//        mBarcodeManager.stopScan()
//        mBarcodeManager.close()
        Log.d("ScannerActivity", "Escaneo detenido.")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Libera los recursos del escáner
//        mBarcodeManager.release()
        Log.d("ScannerActivity", "Recursos del escáner liberados.")
    }
}