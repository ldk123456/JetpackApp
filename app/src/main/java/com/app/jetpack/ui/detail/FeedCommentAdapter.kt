package com.app.jetpack.ui.detail

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.databinding.LayoutFeedCommentListItemBinding
import com.app.jetpack.model.Comment
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.base.BasePagedListAdapter
import com.app.lib_common.ext.dp

class FeedCommentAdapter(private val activity: FragmentActivity) :
    BasePagedListAdapter<Comment, FeedCommentAdapter.ViewHolder>(
        object : DiffUtil.ItemCallback<Comment>() {
            override fun areItemsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Comment, newItem: Comment): Boolean {
                return oldItem == newItem
            }
        }
    ) {

    override fun onCreateDataViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = LayoutFeedCommentListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindDataViewHolder(holder: ViewHolder, position: Int) {
        holder.bindData(activity, getItem(position))
    }

    class ViewHolder(private val binding: LayoutFeedCommentListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bindData(activity: FragmentActivity, comment: Comment?) {
            comment ?: return
            binding.owner = activity
            binding.comment = comment
            val isSelf = UserManager.getUserId() == comment.author?.userId
            binding.labelAuthor.isVisible = isSelf
            binding.commentDelete.isVisible = isSelf
            if (comment.imageUrl.isNullOrEmpty()) {
                binding.commentCover.isVisible = false
                binding.videoIcon.isVisible = false
                binding.commentExt.isVisible = false
            } else {
                binding.commentExt.isVisible = true
                binding.commentCover.isVisible = true
                binding.commentCover.bindData(comment.imageUrl, comment.width, comment.height, 0, 200.dp, 200.dp)
                binding.videoIcon.isVisible = !comment.videoUrl.isNullOrEmpty()
            }
        }
    }
}