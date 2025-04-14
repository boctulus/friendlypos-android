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

        // Configurar clic en el contenedor del carrito
        binding.cartButtonContainer.setOnClickListener {
            findNavController().navigate(R.id.action_salesCalculatorFragment_to_cartFragment)
        }
    }

    private fun setupNumericPad() {
        // Obtener referencia al layout incluido
        val keypad = binding.keypadInclude

        // Configurar botones numéricos
        binding.root.findViewById<Button>(R.id.btn0).setOnClickListener { viewModel.appendDigit("0") }
        binding.root.findViewById<Button>(R.id.btn1).setOnClickListener { viewModel.appendDigit("1") }
        binding.root.findViewById<Button>(R.id.btn2).setOnClickListener { viewModel.appendDigit("2") }
        binding.root.findViewById<Button>(R.id.btn3).setOnClickListener { viewModel.appendDigit("3") }
        binding.root.findViewById<Button>(R.id.btn4).setOnClickListener { viewModel.appendDigit("4") }
        binding.root.findViewById<Button>(R.id.btn5).setOnClickListener { viewModel.appendDigit("5") }
        binding.root.findViewById<Button>(R.id.btn6).setOnClickListener { viewModel.appendDigit("6") }
        binding.root.findViewById<Button>(R.id.btn7).setOnClickListener { viewModel.appendDigit("7") }
        binding.root.findViewById<Button>(R.id.btn8).setOnClickListener { viewModel.appendDigit("8") }
        binding.root.findViewById<Button>(R.id.btn9).setOnClickListener { viewModel.appendDigit("9") }
        binding.root.findViewById<Button>(R.id.btnDecimal).setOnClickListener { viewModel.appendDecimal(".") }

        // Botones de función
        binding.root.findViewById<Button>(R.id.btnClear).setOnClickListener { viewModel.clearEntry() }
        binding.root.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { viewModel.deleteLastDigit() }

        // Operadores
        binding.root.findViewById<Button>(R.id.btnPlus).setOnClickListener {
            Log.d("Calc", "Add button clicked")
            viewModel.addItemToSale()
        }
        binding.root.findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            Log.d("Calc", "Multiply button clicked")
            viewModel.appendOperator("x")
        }

        // Botón de pago (este no está en el include, sigue usando binding directamente)
        binding.btnPay.setOnClickListener {
            val total = viewModel.totalAmount.value?.toDoubleOrNull() ?: 0.0
            if (total > 0) {
                val intent = Intent(requireContext(), PaymentActivity::class.java)
                intent.putExtra("totalAmount", total)
                startActivityForResult(intent, PAYMENT_REQUEST_CODE)
            } else {
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