package cl.friendlypos.mypos.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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

        // Obtener instancia compartida del ViewModel (a nivel de actividad)
        viewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)
        Log.d("ViewModelDebug", "CartFragment - ViewModel instance: ${viewModel.hashCode()}")

        // Asignar el viewModel al binding
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        setupToolbar()
        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Depuración
        val initialItems = viewModel.saleItems.value
        Log.d("CartFragment", "Items iniciales: ${initialItems?.size ?: 0}")

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
    }

    private fun setupToolbar() {
        binding.toolbarCustom.tvTitle?.text = "Carrito"
        binding.toolbarCustom.btnBack?.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun setupObservers() {
        viewModel.saleItems.observe(viewLifecycleOwner) { items ->
            Log.d("CartFragment", "Observer activado. Items recibidos: ${items.size}")
            updateCartVisibility(items)
            adapter.submitList(items)

            // Actualizar el subtotal y contador de items
            binding.subtotalAmount.text = "$${viewModel.totalAmount.value}"
            binding.itemCountBadge.text = "${viewModel.cartItemCount.value}"
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

        binding.subtotalCard.setOnClickListener {
            // Aquí iría la lógica para proceder al pago
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}