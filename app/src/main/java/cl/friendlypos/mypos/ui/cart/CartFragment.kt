package cl.friendlypos.mypos.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import cl.friendlypos.mypos.R
import cl.friendlypos.mypos.databinding.FragmentCartBinding
import cl.friendlypos.mypos.ui.sales.SalesCalculatorViewModel

class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: CartViewModel
    private lateinit var adapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        binding.cartViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        setupRecyclerView()
        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = CartAdapter(
            onItemEdit = { item ->
                // Handle edit item
            },
            onItemDelete = { item ->
                viewModel.removeItem(item.id)
            }
        )
        binding.cartItemsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@CartFragment.adapter
        }
    }

    private fun setupObservers() {
        viewModel.cartItems.observe(viewLifecycleOwner) { items ->
            adapter.submitList(items)
            if (items.isEmpty()) {
                binding.emptyCartContainer.visibility = View.VISIBLE
                binding.cartItemsRecyclerView.visibility = View.GONE
            } else {
                binding.emptyCartContainer.visibility = View.GONE
                binding.cartItemsRecyclerView.visibility = View.VISIBLE
            }
        }

        viewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.subtotalAmount.text = viewModel.formatPrice(subtotal)
        }

        viewModel.itemCount.observe(viewLifecycleOwner) { count ->
            binding.itemCountBadge.text = count.toString()
        }

        // Sincronizar con SalesCalculatorViewModel
        val salesViewModel = ViewModelProvider(requireActivity()).get(SalesCalculatorViewModel::class.java)
        salesViewModel.saleItems.observe(viewLifecycleOwner) { saleItems ->
            val cartItems = saleItems.map { CartItem(it.id, it.name, it.unitPrice, it.quantity) }
            viewModel.setCartItems(cartItems)
        }
    }

    private fun setupListeners() {
        binding.searchProductsEditText.setOnClickListener {
            // Open product search
        }

        binding.barcodeButton.setOnClickListener {
            // Open barcode scanner
        }

        binding.calculatorButton.setOnClickListener {
            // Open calculator
        }

        binding.subtotalCard.setOnClickListener {
            // Proceed to checkout
        }

        binding.toolbarCustom.btnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.toolbarCustom.btnCancel.setOnClickListener {
            // Show confirmation dialog to cancel document
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}