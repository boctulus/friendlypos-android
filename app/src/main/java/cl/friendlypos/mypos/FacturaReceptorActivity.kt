package cl.friendlypos.mypos

import android.app.Activity
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import cl.friendlypos.mypos.checkout.CheckoutHolder
import cl.friendlypos.mypos.compose.screen.FacturaReceptorScreen
import cl.friendlypos.mypos.repository.SaleRepository

/**
 * Captura los datos del receptor para Factura Electrónica. Guarda el resultado en
 * [CheckoutHolder.facturaReceptor] y devuelve `RESULT_OK`. Si se cancela, no modifica el holder.
 */
class FacturaReceptorActivity : AppCompatActivity() {

    private val saleRepository = SaleRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            FacturaReceptorScreen(
                initial = CheckoutHolder.facturaReceptor,
                onLookupRut = { rut -> saleRepository.getTaxpayer(rut).getOrNull() },
                onConfirm = { receptor ->
                    CheckoutHolder.facturaReceptor = receptor
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onBack = {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            )
        }
    }
}
