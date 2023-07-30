package com.app.jetpack.ui.find

import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.databinding.LayoutTagListItemBinding
import com.app.jetpack.model.TagList
import com.app.lib_common.base.BasePagedListAdapter

class TagListAdapter(private val activity: FragmentActivity) : BasePagedListAdapter<TagList, TagListAdapter.ViewHolder>(
    object : DiffUtil.ItemCallback<TagList>() {
        override fun areItemsTheSame(oldItem: TagList, newItem: TagList): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TagList, newItem: TagList): Boolean {
            return oldItem == newItem
        }

    }
) {

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutTagListItemBinding.inflate(activity.layoutInflater, parent, false))
    }

    override fun onBindDataViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position) ?: return
        holder.bindData(activity, item)
        holder.itemView.setOnClickListener {
            TagFeedListActivity.startActivity(activity, item)
        }
    }

    class ViewHolder(val binding: LayoutTagListItemBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindData(owner: LifecycleOwner, item: TagList?) {
            item?.let {
                binding.tag = it
                binding.owner = owner
            }
        }
    }
}