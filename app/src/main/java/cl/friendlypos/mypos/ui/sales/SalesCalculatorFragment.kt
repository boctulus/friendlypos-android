package cl.friendlypos.mypos.ui.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.PaymentActivity
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.compose.screen.SalesCalculatorScreen

class SalesCalculatorFragment : Fragment() {

    private lateinit var viewModel: SalesCalculatorViewModel

    companion object {
        private const val PAYMENT_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                SalesCalculatorScreen(
                    viewModel = viewModel,
                    onNavigateToCart = {
                        findNavController().navigate(R.id.action_sales_calc_to_cart)
                    },
                    onNavigateToPay = { totalAmount ->
                        val intent = Intent(requireContext(), PaymentActivity::class.java)
                        intent.putExtra("totalAmount", totalAmount)
                        @Suppress("DEPRECATION")
                        startActivityForResult(intent, PAYMENT_REQUEST_CODE)
                    },
                    onPendingOperationError = {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Operación Pendiente")
                            .setMessage("Hay una operación pendiente. Por favor, complete la operación antes de pagar.")
                            .setPositiveButton("Regresar", null)
                            .show()
                    }
                )
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            viewModel.processSale()
        }
    }
}
