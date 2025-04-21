package cl.friendlypos.mypos.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import cl.friendlypos.mypos.databinding.FragmentCartBinding
import cl.friendlypos.mypos.ui.sales.SaleItem
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: SalesCalculatorViewModel
    private lateinit var adapter: SaleItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)

        viewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)
        Log.d("ViewModelDebug", "CartFragment - ViewModel instance: ${viewModel.hashCode()}")

        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val initialItems = viewModel.saleItems.value
        Log.d("CartFragment", "Items iniciales: ${initialItems?.size ?: 0}")
        initialItems?.forEach {
            Log.d("CartFragment", "Item: $it")
        }

        updateCartVisibility(initialItems ?: emptyList())
        setupObservers()
    }

    private fun setupRecyclerView() {
        adapter = SaleItemAdapter(
            onItemDelete = { item ->
                viewModel.removeSaleItem(item)
            }
        )

        binding.cartItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CartFragment.adapter
        }

        Log.d("CartFragment", "RecyclerView configurado con adaptador")
    }

    private fun setupObservers() {
        viewModel.saleItems.observe(viewLifecycleOwner) { items ->
            Log.d("CartFragment", "Observer activado. Items recibidos: ${items.size}")
            items.forEach { Log.d("CartFragment", "Item en observer: $it") }
            adapter.submitList(items.toList()) // Pasar una copia de la lista
            updateCartVisibility(items)

            // Calcular el subtotal y la cantidad total directamente desde los ítems
            val subtotal = items.sumOf { it.unitPrice * it.quantity }
            val itemCount = items.sumOf { it.quantity }
            binding.subtotalAmount.text = "$$subtotal"
            binding.itemCountBadge.text = itemCount.toString()
        }
    }

    private fun updateCartVisibility(items: List<SaleItem>) {
        if (items.isEmpty()) {
            Log.d("CartFragment", "Carrito vacío")
            binding.emptyCartContainer.visibility = View.VISIBLE
            binding.cartItemsRecyclerView.visibility = View.GONE
        } else {
            Log.d("CartFragment", "Carrito con items: ${items.size}")
            binding.emptyCartContainer.visibility = View.GONE
            binding.cartItemsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun setupListeners() {
        binding.calculatorButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.clear.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Limpiar Carrito")
                .setMessage("¿Está seguro de que desea limpiar el carrito?")
                .setPositiveButton("Sí") { _, _ ->
                    viewModel.clearCart() // Método a agregar en el ViewModel
                }
                .setNegativeButton("No", null)
                .show()
        }

        binding.subtotalCard.setOnClickListener {
            // Lógica para proceder al pago
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}