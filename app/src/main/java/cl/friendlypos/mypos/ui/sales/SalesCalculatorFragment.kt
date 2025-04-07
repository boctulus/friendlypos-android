// SalesCalculatorFragment.kt
package cl.friendlypos.mypos.ui.sales

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.databinding.ScreenSalesCalcBinding


class SalesCalculatorFragment : Fragment() {

    private var _binding: ScreenSalesCalcBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SalesCalculatorViewModel

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

        // Payment button
        binding.btnPay.setOnClickListener { viewModel.processSale() }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}