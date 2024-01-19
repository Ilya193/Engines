package ru.kraz.engines

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import ru.kraz.engines.databinding.ItemImageBinding

class ImageAdapter(
    private var images: List<String> = mutableListOf(),
    private val imageLoader: ImageLoader
) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {
    inner class ViewHolder(private val view: ItemImageBinding) : RecyclerView.ViewHolder(view.root) {
        fun bind(item: String) {
            view.imgEngine.load(item, imageLoader)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemImageBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    fun submitList(images: List<String>) {
        this.images = images.toList()
    }
}