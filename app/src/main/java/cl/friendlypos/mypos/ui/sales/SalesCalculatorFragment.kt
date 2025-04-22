package cl.friendlypos.mypos.ui.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
        viewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)
        _binding = ScreenSalesCalcBinding.inflate(inflater, container, false)

        setupNumericPad()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // val topNavBar: LinearLayout = view.findViewById(R.id.top_nav_bar)
        // ..

        setupObservers()

        // Flag to prevent observer/TextWatcher loops
        var isUpdatingFromViewModel = false

        // Observer for item name updates
        viewModel.currentItemName.observe(viewLifecycleOwner) { name ->
            if (binding.etItemName.text.toString() != name) {
                isUpdatingFromViewModel = true
                binding.etItemName.setText(name)
                isUpdatingFromViewModel = false
            }
        }

        // Configurar clic en el contenedor del carrito
        binding.btnCartBadge.setOnClickListener {
            try {
                Log.d("CartNavigation", "Cart button image clicked!")

                // Depuración: verificar datos antes de navegar
                Log.d("CartNavigation", "Items en carrito antes de navegar: ${viewModel.saleItems.value?.size}")
                Log.d("CartNavigation", "Cart item count: ${viewModel.cartItemCount.value}")

                findNavController().navigate(R.id.action_sales_calc_to_cart)
                Log.d("Navigation", "Navigation action executed")
            } catch (e: Exception) {
                Log.e("Navigation", "Navigation failed", e)
            }
        }

        binding.etItemName.apply {
            // Establecer placeholder en lugar de prefijo
            hint = "Item"

            // Manejar evento de focus
            setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    // Si el texto comienza con "Item " lo eliminamos al recibir el foco
                    val currentText = text.toString()
                    if (currentText.startsWith("Item ")) {
                        setText(currentText.substring(5))
                    }
                } else {
                    // Al perder el foco, si está vacío, usamos el valor por defecto
                    if (text.toString().isBlank()) {
                        viewModel.updateItemName(viewModel.getGenericItemName())
                    }
                }
            }

            // TextWatcher for user input
            addTextChangedListener(object : android.text.TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: android.text.Editable?) {
                    // Only update if change wasn't from the ViewModel
                    if (!isUpdatingFromViewModel && !s.isNullOrBlank()) {
                        viewModel.updateItemName(s.toString())
                    }
                }
            })
        }

        binding.etAmount.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                // Remover el observer temporalmente para evitar recursión infinita
                viewModel.currentAmount.removeObservers(viewLifecycleOwner)

                // Actualizar el valor en el ViewModel sin el símbolo $
                val amountText = s.toString().replace("$", "")
                if (amountText != viewModel.currentAmount.value) {
                    viewModel.setAmount(amountText)
                }

                // Volver a observar
                observeCurrentAmount()
            }
        })
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
        binding.root.findViewById<Button>(R.id.btnDecimal).setOnClickListener { viewModel.appendDecimal(",") }

        // Botones de función
        binding.root.findViewById<Button>(R.id.btnClear).setOnClickListener { viewModel.clearEntry() }
        binding.root.findViewById<ImageButton>(R.id.btnDelete).setOnClickListener { viewModel.deleteLastDigit() }

        // Operadores
        binding.root.findViewById<Button>(R.id.btnPlus).setOnClickListener {
            Log.d("Calc", "Add button clicked")
            viewModel.addItemToSale()
            viewModel.updateItemName(viewModel.getGenericItemName())
            binding.etAmount.requestFocus()
        }

        binding.root.findViewById<Button>(R.id.btnMultiply).setOnClickListener {
            Log.d("Calc", "Multiply button clicked")
            viewModel.appendOperator("x")
        }

        binding.btnPay.setOnClickListener {
            if (viewModel.currentAmount.value != "0") {
                AlertDialog.Builder(requireContext())
                    .setTitle("Operación Pendiente")
                    .setMessage("Hay una operación pendiente. Por favor, complete la operación antes de pagar.")
                    .setPositiveButton("Regresar", null)
                    .show()
            } else {
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
    }

    // Función para observar el monto actual
    private fun observeCurrentAmount() {
        viewModel.currentAmount.observe(viewLifecycleOwner) { amount ->
            val currentText = binding.etAmount.text.toString().replace("$", "")
            if (currentText != amount) {
                binding.etAmount.setText("$${amount}")
                // Opcional: posicionar el cursor al final
                binding.etAmount.setSelection(binding.etAmount.text.length)
            }
        }
    }

    private fun setupObservers() {
        // Asegúrate de que los observadores estén configurados correctamente
        viewModel.currentAmount.observe(viewLifecycleOwner) { amount ->
            Log.d("Calc", "Display updated: $amount")
            binding.etAmount.setText("$$amount")
        }

        viewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            Log.d("Calc", "Total updated: $total")
            binding.tvTotalAmount.text = "$$total"
        }

        // Actualizar el nombre del item al valor defecto para cada ronda
        viewModel.currentAmount.observe(viewLifecycleOwner) { amount ->
            if (amount == "0") {
                binding.etItemName.setText(viewModel.getGenericItemName())
            }
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