package com.app.jetpack.ui

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.jetpack.R
import com.app.lib_common.ext.dp
import com.app.lib_common.ext.getScreenHeight
import com.app.lib_common.util.ViewHelper
import com.app.lib_common.util.ViewHelper.setViewOutline
import com.app.lib_common.view.RoundFragmentLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShareDialog(context: Context) : AlertDialog(context) {

    private lateinit var mRootLayout: RoundFragmentLayout

    private lateinit var mAdapter: ShareAdapter
    private val mShareItems = ArrayList<ResolveInfo>()

    private var mShareContent: String = ""
    private var mOnClick: View.OnClickListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.onCreate(savedInstanceState)

        mRootLayout = RoundFragmentLayout(context).apply {
            setBackgroundColor(Color.WHITE)
            setViewOutline(20.dp, ViewHelper.RADIUS_TOP)
        }

        mAdapter = ShareAdapter()
        val gridView = RecyclerView(context).apply {
            layoutManager = GridLayoutManager(context, 4)
            adapter = mAdapter
        }

        val layoutParams =
            FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                .apply {
                    leftMargin = 20.dp
                    topMargin = 20.dp
                    rightMargin = 20.dp
                    bottomMargin = 20.dp
                    height = getScreenHeight() / 3
                }
        mRootLayout.addView(gridView, layoutParams)

        setContentView(mRootLayout)

        window?.apply {
            setGravity(Gravity.BOTTOM)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }

        queryShareItems()
    }

    private fun queryShareItems() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                }
                context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_ALL).forEach {
                    mShareItems.add(it)
                }
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    fun setShareContent(shareContent: String): ShareDialog {
        return this.apply { mShareContent = shareContent }
    }

    fun setShareItemClickListener(listener: View.OnClickListener): ShareDialog {
        return this.apply { mOnClick = listener }
    }

    private inner class ShareAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            val itemView = LayoutInflater.from(context).inflate(R.layout.layout_share_item, parent, false)
            return object : RecyclerView.ViewHolder(itemView) {}
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val resolveInfo = mShareItems.getOrNull(position) ?: return
            holder.itemView.findViewById<ImageView>(R.id.iv_share_icon)
                .setImageDrawable(resolveInfo.loadIcon(context.packageManager))
            holder.itemView.findViewById<TextView>(R.id.tv_share_title).text =
                resolveInfo.loadLabel(context.packageManager)

            holder.itemView.setOnClickListener {
                val packageName = resolveInfo.activityInfo?.packageName.orEmpty()
                val className = resolveInfo.activityInfo?.name.orEmpty()
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    type = "text/plain"
                    component = ComponentName(packageName, className)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    putExtra(Intent.EXTRA_TEXT, mShareContent)
                }
                context.startActivity(intent)
                mOnClick?.onClick(it)
                dismiss()
            }
        }

        override fun getItemCount(): Int {
            return mShareItems.size
        }
    }
}