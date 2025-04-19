package cl.friendlypos.mypos.ui.cart

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cl.friendlypos.mypos.databinding.ItemCartProductBinding
import cl.friendlypos.mypos.ui.sales.SaleItem

class SaleItemAdapter(
    private val onItemDelete: (SaleItem) -> Unit
) : ListAdapter<SaleItem, SaleItemAdapter.ViewHolder>(SaleItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d("SaleItemAdapter", "onCreateViewHolder llamado")
        val binding = ItemCartProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        Log.d("SaleItemAdapter", "Binding item en posición $position: $item")
        holder.bind(item)
    }

    inner class ViewHolder(private val binding: ItemCartProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SaleItem) {
            Log.d("SaleItemAdapter", "Binding item: ${item.name}, precio: ${item.unitPrice}, cantidad: ${item.quantity}")
            binding.saleItem = item // Asignar el SaleItem al binding
            binding.executePendingBindings() // Forzar la actualización inmediata de los datos
            binding.editButton.setOnClickListener {
                // Lógica de edición si es necesaria
            }
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