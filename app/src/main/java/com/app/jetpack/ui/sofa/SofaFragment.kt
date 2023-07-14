package com.app.jetpack.ui.sofa

import android.content.res.ColorStateList
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.app.jetpack.core.PATH_MAIN_SOFA
import com.app.jetpack.databinding.FragmentSofaBinding
import com.app.jetpack.model.SofaTab
import com.app.jetpack.model.Tab
import com.app.jetpack.ui.home.HomeFragment
import com.app.jetpack.utils.AppConfig
import com.app.lib_common.ext.color
import com.app.lib_common.ext.safeAs
import com.app.lib_nav_annotation.annotation.FragmentDestination
import com.google.android.material.tabs.TabLayoutMediator

@FragmentDestination(PATH_MAIN_SOFA)
class SofaFragment : Fragment() {

    private lateinit var mBinding: FragmentSofaBinding
    private lateinit var mMediator: TabLayoutMediator

    private var mTabConfig: SofaTab? = null
    private val mTabs = ArrayList<Tab>()
    private val mFragmentMap = HashMap<Int, Fragment>()

    private val mOnPageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            super.onPageSelected(position)
            mBinding.tabLayout.apply {
                for (i in 0 until tabCount) {
                    val tab = getTabAt(i)
                    val textView = tab?.customView?.safeAs<TextView>()
                    if (position == tab?.position) {
                        textView?.textSize = mTabConfig?.activeSize?.toFloat() ?: 12F
                        textView?.typeface = Typeface.DEFAULT_BOLD
                    } else {
                        textView?.textSize = mTabConfig?.normalSize?.toFloat() ?: 12F
                        textView?.typeface = Typeface.DEFAULT
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = FragmentSofaBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mTabConfig = AppConfig.getSofaTabConfig()
        mTabConfig?.tabs?.forEach {
            if (it?.enable == true) {
                mTabs.add(it)
            }
        }

        mBinding.viewPager.apply {
            offscreenPageLimit = ViewPager2.OFFSCREEN_PAGE_LIMIT_DEFAULT
            adapter = object : FragmentStateAdapter(childFragmentManager, lifecycle) {
                override fun getItemCount(): Int = mTabs.size

                override fun createFragment(position: Int): Fragment {
                    var fragment = mFragmentMap[position]
                    if (fragment == null) {
                        fragment = HomeFragment.newInstance(mTabs[position].tag.orEmpty())
                        mFragmentMap[position] = fragment
                    }
                    return fragment
                }
            }
        }
        mTabConfig?.tabGravity?.let {
            mBinding.tabLayout.tabGravity = it
        }
        mMediator = TabLayoutMediator(mBinding.tabLayout, mBinding.viewPager, false) { tab, position ->
            tab.customView = makeTabView(position)
        }
        mMediator.attach()

        mBinding.viewPager.apply {
            registerOnPageChangeCallback(mOnPageChangeCallback)
            post {
                currentItem = mTabConfig?.select ?: 0
            }
        }
    }

    private fun makeTabView(position: Int): View {
        return TextView(context).apply {
            val states = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            )
            val colors = intArrayOf(
                mTabConfig?.activeColor?.color ?: 0,
                mTabConfig?.normalColor?.color ?: 0
            )
            val stateList = ColorStateList(states, colors)
            setTextColor(stateList)
            text = mTabs[position].title
            textSize = mTabConfig?.normalSize?.toFloat() ?: 12F
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mBinding.viewPager.unregisterOnPageChangeCallback(mOnPageChangeCallback)
    }
}