package cl.friendlypos.mypos

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.friendlypos.PaymentCancellationDialog
import java.text.NumberFormat
import java.util.Locale

class PaymentActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var tvDocumentType: TextView
    private lateinit var btnEditDocument: ImageButton
    private lateinit var cardCash: CardView
    private lateinit var btnCancel: Button
    private lateinit var btnBack: ImageButton

    private var totalAmount: Double = 0.0
    private var selectedDocumentType: String = "Documento afecto"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        // Initialize views
        tvTotal = findViewById(R.id.tvTotal)
        tvDocumentType = findViewById(R.id.tvDocumentType)
        btnEditDocument = findViewById(R.id.btnEditDocument)
        cardCash = findViewById(R.id.cardCash)
        btnCancel = findViewById(R.id.btnCancel)
        btnBack = findViewById(R.id.btnBack)

        // Get totalAmount from intent
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        // Format and display the total amount
        val formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
        tvTotal.text = formatter.format(totalAmount)

        // Set initial document type
        tvDocumentType.text = selectedDocumentType

        // Setup click listeners
        btnEditDocument.setOnClickListener {
            navigateToBillingActivity()
        }

        cardCash.setOnClickListener {
            navigateToCashPaymentActivity()
        }

        btnCancel.setOnClickListener {
            showCancellationDialog()
        }

        btnBack.setOnClickListener {
            onBackPressed()
        }
    }

    private fun navigateToBillingActivity() {
        val intent = Intent(this, BillingActivity::class.java)
        intent.putExtra("selectedDocumentType", selectedDocumentType)
        startActivityForResult(intent, BILLING_REQUEST_CODE)
    }

    private fun navigateToCashPaymentActivity() {
        val intent = Intent(this, CashPaymentActivity::class.java)
        intent.putExtra("totalAmount", totalAmount)
        startActivityForResult(intent, CASH_PAYMENT_REQUEST_CODE)
    }

    private fun showCancellationDialog() {
        val dialog = PaymentCancellationDialog()
        dialog.setOnCancelTransactionListener(object : PaymentCancellationDialog.OnCancelTransactionListener {
            override fun onCancel() {
                finish()
            }
        })
        dialog.show(supportFragmentManager, "PaymentCancellationDialog")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == BILLING_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            selectedDocumentType = data.getStringExtra("selectedDocumentType") ?: selectedDocumentType
            tvDocumentType.text = selectedDocumentType
        } else if (requestCode == CASH_PAYMENT_REQUEST_CODE && resultCode == RESULT_OK) {
            // Sale completed, return to calculator
            setResult(RESULT_OK)
            finish()
        }
    }

    companion object {
        private const val BILLING_REQUEST_CODE = 100
        private const val CASH_PAYMENT_REQUEST_CODE = 101
    }
}