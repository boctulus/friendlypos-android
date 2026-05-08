package cl.friendlypos.mypos

import android.app.Activity
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import cl.friendlypos.mypos.compose.screen.CashPaymentScreen
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class CashPaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val totalAmount = intent.getDoubleExtra("totalAmount", 0.0)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goBack()
            }
        })

        setContent {
            CashPaymentScreen(
                totalAmount = totalAmount,
                onConfirm = { _ ->
                    // processSalePayment is always true (dummy); real API call goes here
                    setResult(Activity.RESULT_OK)
                    finish()
                },
                onCancel = {
                    // Clear cart via Activity-scoped ViewModel (preserved from original behavior)
                    val viewModel = ViewModelProvider(this).get(SalesCalculatorViewModel::class.java)
                    viewModel.clearCart()
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                },
                onBack = { goBack() }
            )
        }
    }

    private fun goBack() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }
}
