package com.app.jetpack.ui.my

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.jetpack.R
import com.app.jetpack.core.PATH_MAIN_MY
import com.app.jetpack.core.TAB_TYPE_ALL
import com.app.jetpack.core.TAB_TYPE_COMMENT
import com.app.jetpack.core.TAB_TYPE_FEED
import com.app.jetpack.databinding.FragmentMyBinding
import com.app.jetpack.ui.login.UserManager
import com.app.lib_common.util.StatusBarUtil
import com.app.lib_nav_annotation.annotation.FragmentDestination

@FragmentDestination(PATH_MAIN_MY, needLogin = true)
class MyFragment : Fragment() {
    private lateinit var mBinding: FragmentMyBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        StatusBarUtil.lightStatusBar(requireActivity(), false)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentMyBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mBinding.user = UserManager.getUser()
        UserManager.update().observe(viewLifecycleOwner) {
            mBinding.user = it
        }
        mBinding.actionLogout.setOnClickListener {
            AlertDialog.Builder(requireActivity())
                .setMessage(R.string.fragment_my_logout)
                .setPositiveButton(R.string.fragment_my_logout_ok) { dialog, _ ->
                    dialog.dismiss()
                    UserManager.logout()
                    requireActivity().onBackPressed()
                }
                .setNegativeButton(R.string.fragment_my_logout_cancel, null)
                .create()
                .show()
        }
        mBinding.goDetail.setOnClickListener {
            ProfileActivity.startActivity(requireActivity(), TAB_TYPE_ALL)
        }
        mBinding.userFeed.setOnClickListener {
            ProfileActivity.startActivity(requireActivity(), TAB_TYPE_FEED)
        }
        mBinding.userComment.setOnClickListener {
            ProfileActivity.startActivity(requireActivity(), TAB_TYPE_COMMENT)
        }
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        StatusBarUtil.lightStatusBar(requireActivity(), hidden)
    }
}