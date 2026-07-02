package cl.friendlypos.mypos

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import cl.friendlypos.mypos.checkout.CheckoutHolder
import cl.friendlypos.mypos.checkout.DocumentType
import cl.friendlypos.mypos.ui.payments.PaymentCancellationDialog
import java.text.NumberFormat
import java.util.Locale
import android.widget.Toast
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class PaymentActivity : AppCompatActivity()
{
    private lateinit var tvTotal: TextView
    private lateinit var tvDocumentType: TextView
    private lateinit var btnEditDocument: ImageButton
    private lateinit var cardCash: CardView
    private lateinit var cardCredit: CardView
    private lateinit var cardDebit: CardView
    private lateinit var cardTransfer: CardView
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageButton

    private var totalAmount: Double = 0.0
    private var selectedDocumentType: DocumentType = DocumentType.DEFAULT

    companion object {
        private const val BILLING_REQUEST_CODE = 100
        private const val CASH_PAYMENT_REQUEST_CODE = 101
        private const val FACTURA_RECEPTOR_REQUEST_CODE = 102
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Initialize views
        tvTotal = findViewById(R.id.tvTotal)
        tvDocumentType = findViewById(R.id.tvDocumentType)
        btnEditDocument = findViewById(R.id.btnEditDocument)
        cardCash = findViewById(R.id.cardCash)
        cardCredit = findViewById(R.id.cardCredit)
        cardDebit = findViewById(R.id.cardDebit)
        cardTransfer = findViewById(R.id.cardTransfer)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Forma de pago"

        // Get totalAmount from intent
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        // Format and display the total amount
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        tvTotal.text = formatter.format(totalAmount)

        // Set initial document type
        tvDocumentType.text = selectedDocumentType.label

        // Configurar listeners
        btnEditDocument.setOnClickListener {
            navigateToBillingActivity()
        }

        cardCash.setOnClickListener {
            Log.d("PaymentActivity", "Clic en cardCash detectado, iniciando CashPaymentActivity")
            Toast.makeText(this, "Abriendo ingreso de efectivo", Toast.LENGTH_SHORT).show()
            navigateToCashPaymentActivity()
        }

        cardCredit.setOnClickListener { navigateToMockPayment("card") }
        cardDebit.setOnClickListener { navigateToMockPayment("card") }
        cardTransfer.setOnClickListener { navigateToMockPayment("transfer") }

        btnBack.setOnClickListener {
            finish()
        }

        btnCancel.setOnClickListener {
            showCancellationDialog()
        }
    }

    private fun navigateToBillingActivity() {
        val intent = Intent(this, BillingActivity::class.java)
        intent.putExtra(BillingActivity.EXTRA_DOCUMENT_TYPE, selectedDocumentType.name)
        startActivityForResult(intent, BILLING_REQUEST_CODE)
    }

    private fun navigateToCashPaymentActivity() {
        CheckoutHolder.documentType = selectedDocumentType
        val intent = Intent(this, CashPaymentActivity::class.java)
        intent.putExtra("totalAmount", totalAmount)
        startActivityForResult(intent, CASH_PAYMENT_REQUEST_CODE)
    }

    private fun navigateToMockPayment(method: String) {
        CheckoutHolder.documentType = selectedDocumentType
        val intent = Intent(this, MockPaymentActivity::class.java)
        intent.putExtra("totalAmount", totalAmount)
        intent.putExtra("method", method)
        startActivityForResult(intent, CASH_PAYMENT_REQUEST_CODE)
    }

    /** Para Factura Electrónica, captura los datos del receptor antes de pagar. */
    private fun navigateToFacturaReceptor() {
        val intent = Intent(this, FacturaReceptorActivity::class.java)
        startActivityForResult(intent, FACTURA_RECEPTOR_REQUEST_CODE)
    }

    private fun showCancellationDialog() {
        val dialog = PaymentCancellationDialog()
        dialog.setOnCancelTransactionListener(object : PaymentCancellationDialog.OnCancelTransactionListener {
            override fun onCancel() {
                val viewModel = ViewModelProvider(this@PaymentActivity).get(SalesCalculatorViewModel::class.java)
                viewModel.clearCart() // Limpiar el carrito
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })
        dialog.show(supportFragmentManager, "PaymentCancellationDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            BILLING_REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {
                selectedDocumentType = DocumentType.fromName(
                    data.getStringExtra(BillingActivity.EXTRA_DOCUMENT_TYPE)
                )
                tvDocumentType.text = selectedDocumentType.label
                // Factura requiere datos del receptor → capturarlos ahora.
                if (selectedDocumentType.requiresReceptor) {
                    navigateToFacturaReceptor()
                } else {
                    CheckoutHolder.facturaReceptor = null
                }
            }
            FACTURA_RECEPTOR_REQUEST_CODE -> if (resultCode != RESULT_OK || CheckoutHolder.facturaReceptor == null) {
                // Se canceló la captura → revertir a la opción por defecto.
                selectedDocumentType = DocumentType.DEFAULT
                tvDocumentType.text = selectedDocumentType.label
            }
            CASH_PAYMENT_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                setResult(RESULT_OK)
                finish()
            }
        }
    }
}
