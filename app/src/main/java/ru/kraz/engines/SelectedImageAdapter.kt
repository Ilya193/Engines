package ru.kraz.engines

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import ru.kraz.engines.databinding.ItemSelectedImageBinding

class SelectedImageAdapter(
    private val close: (Int) -> Unit
) : ListAdapter<SelectedImage, SelectedImageAdapter.ViewHolder>(DiffSelectedImage()) {
    inner class ViewHolder(private val view: ItemSelectedImageBinding) : RecyclerView.ViewHolder(view.root) {

        init {
            view.icClose.setOnClickListener {
                if (bindingAdapterPosition != RecyclerView.NO_POSITION)
                    close(bindingAdapterPosition)
            }
        }

        fun bind(item: SelectedImage) {
            view.imgEngine.load(item.uri)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemSelectedImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

data class SelectedImage(val uri: Uri)

class DiffSelectedImage : DiffUtil.ItemCallback<SelectedImage>() {
    override fun areItemsTheSame(oldItem: SelectedImage, newItem: SelectedImage): Boolean =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: SelectedImage, newItem: SelectedImage): Boolean =
        oldItem == newItem

}