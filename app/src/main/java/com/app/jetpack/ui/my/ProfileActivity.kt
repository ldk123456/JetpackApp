package com.app.jetpack.ui.my

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.app.jetpack.R
import com.app.jetpack.core.KEY_TAB_TYPE
import com.app.jetpack.core.TAB_TYPE_ALL
import com.app.jetpack.core.TAB_TYPE_COMMENT
import com.app.jetpack.core.TAB_TYPE_FEED
import com.app.jetpack.databinding.ActivityProfileBinding
import com.app.jetpack.ui.login.UserManager
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.absoluteValue

class ProfileActivity : AppCompatActivity() {
    companion object {
        fun startActivity(context: Context, tabType: String) {
            val intent = Intent(context, ProfileActivity::class.java).apply {
                putExtra(KEY_TAB_TYPE, tabType)
            }
            context.startActivity(intent)
        }
    }

    private lateinit var mBinding: ActivityProfileBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_profile)
        mBinding.user = UserManager.getUser()
        mBinding.actionBack.setOnClickListener {
            finish()
        }
        initTab()
        initListener()
    }

    private fun initTab() {
        val tabs = resources.getStringArray(R.array.user_profile_tabs)
        val mediator = TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager) { tab, position ->
            tab.text = tabs[position]
        }
        val initTab = when (intent.getStringExtra(KEY_TAB_TYPE)) {
            TAB_TYPE_ALL -> 0
            TAB_TYPE_FEED -> 1
            TAB_TYPE_COMMENT -> 2
            else -> 0
        }
        if (initTab != 0) {
            mBinding.viewPager.post {
                mBinding.viewPager.currentItem = initTab
            }
        }
        mBinding.viewPager.adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return tabs.size
            }

            override fun createFragment(position: Int): Fragment {
                val tabType: String = when (position) {
                    0 -> TAB_TYPE_ALL
                    1 -> TAB_TYPE_FEED
                    2 -> TAB_TYPE_COMMENT
                    else -> TAB_TYPE_ALL
                }
                return ProfileFeedFragment.newInstance(tabType)
            }
        }
        mediator.attach()
    }

    private fun initListener() {
        mBinding.appbar.addOnOffsetChangedListener { appBarLayout, verticalOffset ->
            mBinding.expand = verticalOffset.absoluteValue < appBarLayout.top
        }
    }
}