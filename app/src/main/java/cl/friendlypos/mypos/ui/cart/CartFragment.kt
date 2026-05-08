package cl.friendlypos.mypos.ui.cart

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import cl.friendlypos.mypos.PaymentActivity
import cl.friendlypos.mypos.compose.screen.CartScreen
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class CartFragment : Fragment() {

    private lateinit var viewModel: SalesCalculatorViewModel

    companion object {
        private const val PAYMENT_REQUEST_CODE = 2
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
                CartScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        @Suppress("DEPRECATION")
                        requireActivity().onBackPressed()
                    },
                    onProceedToPayment = { totalAmount ->
                        val intent = Intent(requireContext(), PaymentActivity::class.java)
                        intent.putExtra("totalAmount", totalAmount)
                        @Suppress("DEPRECATION")
                        startActivityForResult(intent, PAYMENT_REQUEST_CODE)
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
