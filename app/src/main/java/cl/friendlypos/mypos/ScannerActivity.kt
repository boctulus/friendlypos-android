package cl.friendlypos.mypos

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

import cl.friendlypos.mypos.compose.screen.BarcodeScannerDemoScreen
import cl.friendlypos.mypos.compose.viewmodel.BarcodeScannerViewModel

/**
 * Pantalla de escaneo de códigos de barra.
 *
 * El SDK propietario (ZCS/SmartPos) fue removido en la rama `remove-sdk`. La lectura real
 * de EAN13 por cámara (CameraX + ML Kit) se implementa en la Fase 3 del plan
 * (docs/to-do/plan-etapa1-ventas-caja-tickets.md). Por ahora muestra la pantalla demo.
 */
class ScannerActivity : ComponentActivity() {

    private val viewModel: BarcodeScannerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("ScannerActivity", "Scanner sin SDK propietario — pendiente CameraX/ML Kit (Fase 3)")

        setContent {
            BarcodeScannerDemoScreen(
                onBackPressed = { finish() },
                viewModel = viewModel
            )
        }
    }
}
