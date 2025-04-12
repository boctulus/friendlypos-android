package cl.friendlypos.mypos

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import cl.friendlypos.mypos.ui.payments.PaymentCancellationDialog
import java.text.NumberFormat
import java.util.Locale

class CashPaymentActivity : AppCompatActivity() {

    private lateinit var tvTotal: TextView
    private lateinit var tvAmountEntered: TextView
    private lateinit var tvChange: TextView
    private lateinit var btnCancel: Button
    private lateinit var btnAccept: Button

    // Numeric buttons
    private lateinit var btnOne: Button
    private lateinit var btnTwo: Button
    private lateinit var btnThree: Button
    private lateinit var btnFour: Button
    private lateinit var btnFive: Button
    private lateinit var btnSix: Button
    private lateinit var btnSeven: Button
    private lateinit var btnEight: Button
    private lateinit var btnNine: Button
    private lateinit var btnZero: Button
    private lateinit var btnZeroZero: Button
    private lateinit var btnBackspace: ImageButton
    private lateinit var btnBack: ImageButton

    private var totalAmount: Double = 0.0
    private var amountEntered: Int = 0
    private var formatter = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cash_payment)

        // Initialize views
        tvTotal = findViewById(R.id.tvTotal)
        tvAmountEntered = findViewById(R.id.tvAmountEntered)
        tvChange = findViewById(R.id.tvChange)
        btnCancel = findViewById(R.id.btnCancel)
        btnAccept = findViewById(R.id.btnAccept)
        btnBack = findViewById(R.id.btnBack)

        // Initialize numeric buttons
        btnOne = findViewById(R.id.btn1)
        btnTwo = findViewById(R.id.btn2)
        btnThree = findViewById(R.id.btn3)
        btnFour = findViewById(R.id.btn4)
        btnFive = findViewById(R.id.btn5)
        btnSix = findViewById(R.id.btn6)
        btnSeven = findViewById(R.id.btn7)
        btnEight = findViewById(R.id.btn8)
        btnNine = findViewById(R.id.btn9)
        btnZero = findViewById(R.id.btn0)
        btnZeroZero = findViewById(R.id.btn00)
        btnBackspace = findViewById(R.id.btn_delete)
        // btnClear = findViewById(R.id.btnClear)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        tvTitle.text = "Ingreso efectivo"

        // Get totalAmount from intent
        totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        // Format and display the total amount
        tvTotal.text = formatter.format(totalAmount)
        updateAmountDisplay()

        // Setup numeric buttons click listeners
        setupNumericButtonsListeners()

        // Setup other click listeners

        btnBack.setOnClickListener {
            goBack()
        }

        // Configurar OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this) {
            goBack()
        }

        btnAccept.setOnClickListener {
            completeTransaction()
        }

        btnCancel.setOnClickListener {
            showCancellationDialog()
        }
    }

    private fun setupNumericButtonsListeners() {
        btnOne.setOnClickListener { appendDigit(1) }
        btnTwo.setOnClickListener { appendDigit(2) }
        btnThree.setOnClickListener { appendDigit(3) }
        btnFour.setOnClickListener { appendDigit(4) }
        btnFive.setOnClickListener { appendDigit(5) }
        btnSix.setOnClickListener { appendDigit(6) }
        btnSeven.setOnClickListener { appendDigit(7) }
        btnEight.setOnClickListener { appendDigit(8) }
        btnNine.setOnClickListener { appendDigit(9) }
        btnZero.setOnClickListener { appendDigit(0) }
        btnZeroZero.setOnClickListener {
            appendDigit(0)
            appendDigit(0)
        }

        btnBackspace.setOnClickListener {
            if (amountEntered > 0) {
                amountEntered /= 10
                updateAmountDisplay()
                updateChangeAmount()
            }
        }
    }

    private fun appendDigit(digit: Int) {
        // Avoid overflow by checking if result will be less than Int.MAX_VALUE
        if (amountEntered <= Int.MAX_VALUE / 10) {
            amountEntered = amountEntered * 10 + digit
            updateAmountDisplay()
            updateChangeAmount()
            updateSkipButton()
        }
    }

    private fun updateAmountDisplay() {
        tvAmountEntered.text = formatter.format(amountEntered)
    }

    private fun updateChangeAmount() {
        val change = amountEntered - totalAmount
        if (change >= 0) {
            tvChange.visibility = View.VISIBLE
            tvChange.text = "Cambio: ${formatter.format(change)}"

            // If amount entered is sufficient, change skip button text to "Finalizar"
            btnAccept.text = "Finalizar"
        } else {
            tvChange.visibility = View.GONE
            btnAccept.text = "Omitir"
        }
    }

    private fun updateSkipButton() {
        btnAccept.text = if (amountEntered > 0) "Aceptar" else "Omitir"
    }

    private fun completeTransaction() {
        // Here you would handle the payment processing and update the sale
        // Create a local service call that would be replaced by API call later
        val serviceResult = processSalePayment(totalAmount, amountEntered)

        if (serviceResult) {
            // Return success
            setResult(Activity.RESULT_OK)
            finish()
        } else {
            // Handle error (in a real app)
            // For now just return success anyway
            setResult(Activity.RESULT_CANCELED)
            finish()
        }
    }

    private fun processSalePayment(total: Double, paymentAmount: Int): Boolean {
        // Dummy method to simulate API call
        // This would be replaced by an actual API call in the future
        return true
    }

    private fun showCancellationDialog() {
        val dialog = PaymentCancellationDialog()
        dialog.setOnCancelTransactionListener(object : PaymentCancellationDialog.OnCancelTransactionListener {
            override fun onCancel() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })
        dialog.show(supportFragmentManager, "PaymentCancellationDialog")
    }

    /*
      Deberia regresar a PaymentActivity
    */
    private fun goBack() {
        setResult(RESULT_OK, intent)
        finish()
    }
}