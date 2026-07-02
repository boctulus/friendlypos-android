package cl.friendlypos.mypos.ui.sales

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.SessionManager
import cl.friendlypos.mypos.compose.screen.ScanCartScreen

/**
 * Host de [ScanCartScreen]. Comparte el [SalesCalculatorViewModel] con la calculadora y el
 * carrito (scope de Activity), por lo que los productos escaneados quedan en el carrito al volver.
 */
class ScanCartFragment : Fragment() {

    private lateinit var viewModel: SalesCalculatorViewModel

    override fun onResume() {
        super.onResume()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.hide()
    }

    override fun onStop() {
        super.onStop()
        (requireActivity() as? AppCompatActivity)?.supportActionBar?.show()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)

        // store_id es obligatorio para el lookup por EAN (endpoint público sin JWT).
        // Sin fallback silencioso: si no hay tienda asociada, se informa y se vuelve atrás.
        val storeId = SessionManager.get(requireContext())?.storeId
        if (storeId.isNullOrBlank()) {
            Toast.makeText(
                requireContext(),
                "No hay tienda asociada a la sesión. No se puede escanear.",
                Toast.LENGTH_LONG
            ).show()
            findNavController().popBackStack()
            return ComposeView(requireContext())
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                ScanCartScreen(
                    viewModel = viewModel,
                    storeId = storeId,
                    onClose = { findNavController().popBackStack() }
                )
            }
        }
    }
}
