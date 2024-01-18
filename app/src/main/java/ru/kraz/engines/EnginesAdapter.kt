package ru.kraz.engines

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.ImageLoader
import coil.load
import ru.kraz.engines.databinding.ItemEngineBinding

class EnginesAdapter(
    private val imageLoader: ImageLoader,
    private val like: (Int) -> Unit,
    private val expand: (Int) -> Unit,
) : ListAdapter<Engine, EnginesAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val view: ItemEngineBinding) :
        RecyclerView.ViewHolder(view.root) {

        fun bind(item: Engine) {
            view.imgEngine.load(item.photo, imageLoader) {
                listener(onSuccess = { _, _ ->
                    view.tvName.text = item.name
                    view.imgLike.setImageResource(R.drawable.ic_favorite_border)
                    bindLikes(item)
                })
            }
            bindDescription(item)
        }

        fun bindLikes(item: Engine) {
            view.tvLike.text = item.countLike.toString()
            view.imgLike.setImageResource(if (item.likeIt) R.drawable.ic_favorite else R.drawable.ic_favorite_border)
        }

        fun bindDescription(item: Engine) {
            val newMaxLines = if (item.expanded) MAX_LINES
            else MIN_LINES
            ObjectAnimator.ofInt(view.tvDescription, "maxLines", newMaxLines).setDuration(250)
                .start()
            view.tvDescription.text = item.description
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemEngineBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(view).apply {
            view.imgLike.setOnClickListener {
                like(adapterPosition)
            }
            view.tvDescription.setOnClickListener {
                expand(adapterPosition)
            }
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else {
            val bundle = payloads[0] as Bundle
            when (bundle.getString(Diff.ACTION) ?: "") {
                Diff.ACTION_LIKE -> holder.bindLikes(getItem(position))
                Diff.ACTION_TEXT_EXPANDED -> holder.bindDescription(getItem(position))
            }
        }
    }

    companion object {
        const val MAX_LINES = 25
        const val MIN_LINES = 2
    }
}

class Diff : DiffUtil.ItemCallback<Engine>() {
    override fun areItemsTheSame(oldItem: Engine, newItem: Engine): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Engine, newItem: Engine): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: Engine, newItem: Engine): Any? {
        return if (oldItem.likeIt != newItem.likeIt || oldItem.countLike != newItem.countLike) bundleOf().apply {
            putString(ACTION, ACTION_LIKE)
        } else bundleOf().apply {
            putString(ACTION, ACTION_TEXT_EXPANDED)
        }
    }

    companion object {
        const val ACTION = "ACTION"
        const val ACTION_LIKE = "ACTION_LIKE"
        const val ACTION_TEXT_EXPANDED = "ACTION_TEXT_EXPANDED"
    }
}