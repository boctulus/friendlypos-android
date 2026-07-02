package cl.friendlypos.mypos

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import cl.friendlypos.mypos.checkout.CheckoutFlow
import cl.friendlypos.mypos.checkout.CheckoutHolder
import cl.friendlypos.mypos.compose.screen.DteRetryScreen
import cl.friendlypos.mypos.compose.screen.MockPaymentScreen
import cl.friendlypos.mypos.compose.screen.TicketPreviewScreen
import cl.friendlypos.mypos.compose.screen.TicketWebScreen
import cl.friendlypos.mypos.model.Ticket
import cl.friendlypos.mypos.repository.SaleRepository
import kotlinx.coroutines.launch

class MockPaymentActivity : AppCompatActivity() {

    private val saleRepository = SaleRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)
        val method = intent.getStringExtra("method") ?: "card"

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        setContent {
            var ticketHtml by remember { mutableStateOf<String?>(null) }
            var ticket by remember { mutableStateOf<Ticket?>(null) }
            var dteError by remember { mutableStateOf<String?>(null) }
            var lastSale by remember { mutableStateOf<SaleRepository.SaleResult?>(null) }
            var isProcessing by remember { mutableStateOf(false) }

            val storeName = SessionManager.get(this@MockPaymentActivity)?.storeId ?: "FriendlyPOS"

            val onCloseTicket = {
                CheckoutHolder.clear()
                setResult(Activity.RESULT_OK)
                finish()
            }

            fun applyOutcome(outcome: CheckoutFlow.Outcome) {
                when (outcome) {
                    is CheckoutFlow.Outcome.ShowHtml -> ticketHtml = outcome.html
                    is CheckoutFlow.Outcome.ShowTicket -> ticket = outcome.ticket
                    is CheckoutFlow.Outcome.DteError -> dteError = outcome.message
                }
            }

            when {
                ticketHtml != null -> TicketWebScreen(html = ticketHtml!!, onClose = onCloseTicket)
                ticket != null -> TicketPreviewScreen(ticket = ticket!!, onClose = onCloseTicket)
                dteError != null -> DteRetryScreen(
                    message = dteError!!,
                    isProcessing = isProcessing,
                    onRetry = {
                        val sale = lastSale ?: return@DteRetryScreen
                        isProcessing = true
                        lifecycleScope.launch {
                            val outcome = CheckoutFlow.retryDte(
                                context = this@MockPaymentActivity,
                                repo = saleRepository,
                                sale = sale,
                                documentType = CheckoutHolder.documentType,
                                receptor = CheckoutHolder.facturaReceptor,
                                storeName = storeName
                            )
                            isProcessing = false
                            dteError = null
                            applyOutcome(outcome)
                        }
                    },
                    onContinueWithout = {
                        val sale = lastSale ?: return@DteRetryScreen
                        isProcessing = true
                        lifecycleScope.launch {
                            val outcome = CheckoutFlow.continueWithoutDocument(
                                context = this@MockPaymentActivity,
                                repo = saleRepository,
                                sale = sale,
                                storeName = storeName
                            )
                            isProcessing = false
                            dteError = null
                            applyOutcome(outcome)
                        }
                    }
                )
                else -> MockPaymentScreen(
                    totalAmount = totalAmount,
                    method = method,
                    isProcessing = isProcessing,
                    onConfirm = { transferDetails ->
                        if (isProcessing) return@MockPaymentScreen
                        isProcessing = true
                        lifecycleScope.launch {
                            val result = saleRepository.createSale(
                                lines = CheckoutHolder.lines,
                                paymentMethod = method,
                                amountPaid = totalAmount,
                                tipoDocumento = CheckoutHolder.documentType.label,
                                transferBank = transferDetails?.bank,
                                transferAccountType = transferDetails?.accountType,
                                transferAccountNumber = transferDetails?.accountNumber
                            )
                            result.onSuccess { r ->
                                lastSale = r
                                val outcome = CheckoutFlow.completeSale(
                                    context = this@MockPaymentActivity,
                                    repo = saleRepository,
                                    sale = r,
                                    documentType = CheckoutHolder.documentType,
                                    receptor = CheckoutHolder.facturaReceptor,
                                    storeName = storeName
                                )
                                isProcessing = false
                                applyOutcome(outcome)
                            }.onFailure { e ->
                                isProcessing = false
                                Toast.makeText(
                                    this@MockPaymentActivity,
                                    e.message ?: "Error al registrar la venta",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    },
                    onCancel = {
                        setResult(Activity.RESULT_CANCELED)
                        finish()
                    }
                )
            }
        }
    }
}
