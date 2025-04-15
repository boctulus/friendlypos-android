package cl.friendlypos.mypos.ui.cart

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cl.friendlypos.mypos.databinding.FragmentCartBinding
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private lateinit var salesViewModel: SalesCalculatorViewModel
    private lateinit var adapter: SaleItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        salesViewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        setupToolbar()
        setupListeners()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSalesDataObserver()

        val initialItems = salesViewModel.saleItems.value
        Log.d("CartFragment", "Items iniciales: ${initialItems?.size ?: 0}")

        adapter.submitList(salesViewModel.saleItems.value) // Carga inicial
    }

    private fun setupRecyclerView() {
        adapter = SaleItemAdapter(
            onItemDelete = { item ->
                salesViewModel.removeSaleItem(item)
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

    private fun setupSalesDataObserver()
    {

        salesViewModel.saleItems.observe(viewLifecycleOwner) { items ->

            Log.d("CartFragment", "Observer activado. Items recibidos: ${items.size}")
            if (items.isEmpty()) {
                Log.d("CartFragment", "Carrito vacío")
                binding.emptyCartContainer.visibility = View.VISIBLE
                binding.cartItemsRecyclerView.visibility = View.GONE
            } else {
                Log.d("CartFragment", "Carrito con items: ${items.size}")
                binding.emptyCartContainer.visibility = View.GONE
                binding.cartItemsRecyclerView.visibility = View.VISIBLE
                adapter.submitList(items)
            }

        }

        salesViewModel.totalAmount.observe(viewLifecycleOwner) { total ->
            val amount = total.toIntOrNull() ?: 0
            binding.subtotalAmount.text = "$$amount"
        }

        salesViewModel.cartItemCount.observe(viewLifecycleOwner) { count ->
            binding.itemCountBadge.text = count.toString()
        }
    }

    private fun setupListeners() {
        binding.calculatorButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.subtotalCard.setOnClickListener {
            // Proceder al pago (implementar según necesites)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}