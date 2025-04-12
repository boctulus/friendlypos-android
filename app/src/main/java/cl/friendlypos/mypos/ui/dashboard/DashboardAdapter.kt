package cl.friendlypos.mypos.ui.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import cl.friendlypos.mypos.databinding.ItemDashboardTileBinding
import cl.friendlypos.mypos.model.DashboardItem

class DashboardAdapter(
    private val items: List<DashboardItem>,
    private val onItemClick: (DashboardItem) -> Unit
) : RecyclerView.Adapter<DashboardAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemDashboardTileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemDashboardTileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        
        holder.binding.apply {
            tileTitle.text = item.title
            tileIcon.setImageResource(item.icon)
            tileCard.setCardBackgroundColor(item.color)
            tileCard.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}