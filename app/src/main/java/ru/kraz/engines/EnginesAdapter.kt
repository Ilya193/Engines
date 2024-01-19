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
import ru.kraz.engines.databinding.ItemEngineBinding

class EnginesAdapter(
    private val imageLoader: ImageLoader,
    private val like: (Int) -> Unit,
    private val expand: (Int) -> Unit,
    private val soundAction: (Int, Engine) -> Unit,
) : ListAdapter<Engine, EnginesAdapter.ViewHolder>(Diff()) {

    inner class ViewHolder(private val view: ItemEngineBinding) :
        RecyclerView.ViewHolder(view.root) {

        private val adapter = ImageAdapter(imageLoader = imageLoader)

        init {
            view.viewPager.adapter = adapter
        }

        fun bind(item: Engine) {
            adapter.submitList(item.images)
            view.tvName.text = item.name
            view.imgLike.setImageResource(R.drawable.ic_favorite_border)
            bindLikes(item)
            bindDescription(item)
            bindSoundAction(item)
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

        fun bindSoundAction(item: Engine) {
            view.soundAction.setImageResource(if (item.soundPlaying) R.drawable.pause else R.drawable.play)
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
            view.soundAction.setOnClickListener {
                soundAction(adapterPosition, getItem(adapterPosition))
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
                Diff.ACTION_SOUND -> holder.bindSoundAction(getItem(position))
                Diff.ACTION_TEXT_EXPANDED -> holder.bindDescription(getItem(position))
            }
        }
    }

    companion object {
        const val MAX_LINES = 100
        const val MIN_LINES = 2
    }
}

class Diff : DiffUtil.ItemCallback<Engine>() {
    override fun areItemsTheSame(oldItem: Engine, newItem: Engine): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: Engine, newItem: Engine): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: Engine, newItem: Engine): Any? {
        return if (oldItem.likeIt != newItem.likeIt || oldItem.countLike != newItem.countLike)
            bundleOf().apply {
                putString(ACTION, ACTION_LIKE)
            }
        else if (oldItem.soundPlaying != newItem.soundPlaying)
            bundleOf().apply {
                putString(ACTION, ACTION_SOUND)
            }
        else
            bundleOf().apply {
                putString(ACTION, ACTION_TEXT_EXPANDED)
            }
    }

    companion object {
        const val ACTION = "ACTION"
        const val ACTION_LIKE = "ACTION_LIKE"
        const val ACTION_SOUND = "ACTION_SOUND"
        const val ACTION_TEXT_EXPANDED = "ACTION_TEXT_EXPANDED"
    }
}