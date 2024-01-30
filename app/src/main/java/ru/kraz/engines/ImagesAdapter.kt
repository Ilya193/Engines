package ru.kraz.engines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import ru.kraz.engines.databinding.ItemImageBinding

class ImagesAdapter(
    private var images: List<String> = mutableListOf(),
    private val imageLoader: ImageLoader,
) : RecyclerView.Adapter<ImagesAdapter.ViewHolder>() {
    inner class ViewHolder(private val view: ItemImageBinding) :
        RecyclerView.ViewHolder(view.root) {
        fun bind(item: String) {
            view.imgEngine.load(item, imageLoader) {
                listener(onStart = {
                    view.loading.visibility = View.VISIBLE
                }, onSuccess = {_, _ ->
                    view.loading.visibility = View.GONE
                }, onError = {_, _ ->
                    view.loading.visibility = View.GONE
                }, onCancel = {
                    view.loading.visibility = View.GONE
                })
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            ItemImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int = images.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(images[position])
    }

    fun submitList(images: List<String>) {
        this.images = images.toList()
    }
}