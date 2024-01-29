package ru.kraz.engines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.kraz.engines.databinding.ItemMessageReceivingBinding
import ru.kraz.engines.databinding.ItemMessageSenderBinding

class CommentsAdapter(
    private val read: (Int) -> Unit
) : ListAdapter<CommentUi, CommentsAdapter.ViewHolder>(DiffComments()) {

    abstract class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        abstract fun bind(item: CommentUi)
        open fun bindRead(item: CommentUi) {}
    }

    class SenderViewHolder(private val view: ItemMessageSenderBinding) : ViewHolder(view.root) {
        override fun bind(item: CommentUi) {
            view.tvMessage.text = item.message
            view.tvCreatedDate.text = item.createdDate
            bindRead(item)
        }

        override fun bindRead(item: CommentUi) {
            view.stateMessage.setImageResource(if (item.messageRead) R.drawable.done_all else R.drawable.done)
        }
    }

    class ReceivingViewHolder(private val view: ItemMessageReceivingBinding) : ViewHolder(view.root) {
        override fun bind(item: CommentUi) {
            view.tvMessage.text = item.message
            view.tvCreatedDate.text = item.createdDate
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position).iSendThis) {
            true -> SENDER_VIEW_TYPE
            false -> RECEIVING_VIEW_TYPE
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return when (viewType) {
            SENDER_VIEW_TYPE -> SenderViewHolder(ItemMessageSenderBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            RECEIVING_VIEW_TYPE -> ReceivingViewHolder(ItemMessageReceivingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
            else -> throw IllegalStateException()
        }
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if (!getItem(position).iSendThis && !getItem(position).messageRead) read(position)
        holder.bind(getItem(position))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int, payloads: MutableList<Any>) {
        if (payloads.isEmpty()) super.onBindViewHolder(holder, position, payloads)
        else {
            val action = payloads[0] as Boolean
            if (action) holder.bindRead(getItem(position))
        }
    }
    
    companion object {
        private const val SENDER_VIEW_TYPE = 0
        private const val RECEIVING_VIEW_TYPE = 1
    }
}

class DiffComments : DiffUtil.ItemCallback<CommentUi>() {
    override fun areItemsTheSame(oldItem: CommentUi, newItem: CommentUi): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: CommentUi, newItem: CommentUi): Boolean =
        oldItem == newItem

    override fun getChangePayload(oldItem: CommentUi, newItem: CommentUi): Any? =
        oldItem.messageRead != newItem.messageRead

}