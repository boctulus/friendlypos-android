package cl.friendlypos.mypos.ui.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.friendlypos.mypos.databinding.ItemCartProductBinding
import cl.friendlypos.mypos.ui.sales.SaleItem
import android.util.Log

class SaleItemAdapter(
    private val onItemDelete: (SaleItem) -> Unit
) : ListAdapter<SaleItem, SaleItemAdapter.ViewHolder>(SaleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("SaleItemAdapter", "Mostrando item: $item")
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SaleItem) {
            // Mapear los datos de SaleItem a los TextView del layout
            binding.productName.text = item.name
            binding.productPrice.text = "$${item.unitPrice}"
            binding.productQuantity.text = "Cantidad: ${item.quantity}"
            binding.productTotal.text = "$${item.unitPrice * item.quantity}"

            // Configurar el botón de edición (opcional)
            binding.editButton.setOnClickListener {
                // Aquí puedes agregar lógica de edición si es necesaria
            }

            // Long press para eliminar el item
            binding.root.setOnLongClickListener {
                onItemDelete(item)
                true
            }
        }
    }
}

class SaleItemDiffCallback : DiffUtil.ItemCallback<SaleItem>() {
    override fun areItemsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: SaleItem, newItem: SaleItem): Boolean {
        return oldItem == newItem
    }
}