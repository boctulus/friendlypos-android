// SalesCalculatorFragment.kt
package cl.friendlypos.mypos.ui.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.PaymentActivity
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.databinding.ScreenSalesCalcBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SalesCalculatorFragment : Fragment()
{
    private var _binding: ScreenSalesCalcBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SalesCalculatorViewModel

    companion object {
        private const val PAYMENT_REQUEST_CODE = 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(SalesCalculatorViewModel::class.java)
        _binding = ScreenSalesCalcBinding.inflate(inflater, container, false)

        setupNumericPad()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val topNavBar: LinearLayout = view.findViewById(R.id.top_nav_bar)
        // ..

        setupObservers()
    }

    private fun setupNumericPad() {
        // Set up number buttons
        binding.btn0.setOnClickListener { viewModel.appendDigit("0") }
        binding.btn1.setOnClickListener { viewModel.appendDigit("1") }
        binding.btn2.setOnClickListener { viewModel.appendDigit("2") }
        binding.btn3.setOnClickListener { viewModel.appendDigit("3") }
        binding.btn4.setOnClickListener { viewModel.appendDigit("4") }
        binding.btn5.setOnClickListener { viewModel.appendDigit("5") }
        binding.btn6.setOnClickListener { viewModel.appendDigit("6") }
        binding.btn7.setOnClickListener { viewModel.appendDigit("7") }
        binding.btn8.setOnClickListener { viewModel.appendDigit("8") }
        binding.btn9.setOnClickListener { viewModel.appendDigit("9") }
        binding.btn00.setOnClickListener { viewModel.appendDigit("00") }
        // binding.btnDot.setOnClickListener { viewModel.appendDigit(",") }

        // Function buttons
        binding.btnClear.setOnClickListener { viewModel.clearEntry() }
        binding.btnDelete.setOnClickListener { viewModel.deleteLastDigit() }

        // Operators

        // Añade logs para todos los botones críticos
        binding.btnPlus.setOnClickListener {
            Log.d("Calc", "Add button clicked")
            viewModel.addItemToSale()
        }

        binding.btnMultiply.setOnClickListener {
            Log.d("Calc", "Multiply button clicked")
            viewModel.appendOperator("x")
        }

        // Payment button -- modificación del botón PAGAR
        binding.btnPay.setOnClickListener {
            val total = viewModel.totalAmount.value?.toDoubleOrNull() ?: 0.0
            if (total > 0) {
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                intent.putExtra("totalAmount", total)
                startActivityForResult(intent, PAYMENT_REQUEST_CODE)
            } else {
                // Mostrar mensaje de carrito vacío (puedes usar un Toast o Snackbar)
                Toast.makeText(requireContext(), "No hay productos en el carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupObservers() {
        // Inicializa el ViewModel explícitamente
        viewModel = ViewModelProvider(this).get(SalesCalculatorViewModel::class.java)

        // Asegúrate de que los observadores estén configurados correctamente
        viewModel.currentAmount.observe(viewLifecycleOwner) { amount ->
            Log.d("Calc", "Display updated: $amount")
            binding.tvAmount.text = "$$amount"
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            Log.d("Calc", "Total updated: $total")
            binding.tvTotalAmount.text = "$$total"
        }

        viewModel.currentItemName.observe(viewLifecycleOwner) { name ->
            binding.tvItemName.text = name
        }

        viewModel.cartItemCount.observe(viewLifecycleOwner) { count ->
            Log.d("Calc", "Cart count updated: $count")
            updateCartBadge(count)
        }
    }

    private fun updateCartBadge(count: Int) {
        with(binding.cartBadgeCount) {
            var countToShow = count

            if (countToShow >99){
                countToShow = 99
            }

            text = countToShow.toString()
            visibility = if (count > 0) View.VISIBLE else View.INVISIBLE
        }
        // Debug para verificar
        Log.d("Calc", "Badge updated: $count")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PAYMENT_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Pago completado, reiniciar la calculadora
            viewModel.processSale()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}