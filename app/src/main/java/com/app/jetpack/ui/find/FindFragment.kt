package com.app.jetpack.ui.find

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.app.jetpack.core.KEY_TAG_TYPE
import com.app.jetpack.core.PATH_MAIN_FIND
import com.app.jetpack.model.SofaTab
import com.app.jetpack.ui.sofa.SofaFragment
import com.app.jetpack.utils.AppConfig
import com.app.lib_nav_annotation.annotation.FragmentDestination

@FragmentDestination(PATH_MAIN_FIND)
class FindFragment : SofaFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        childFragmentManager.addFragmentOnAttachListener { _, fragment ->
            val tagType = fragment.arguments?.getString(KEY_TAG_TYPE)
            if ("onlyFollow" == tagType) {
                ViewModelProvider(fragment)[TagListViewModel::class.java].switchTabLiveData.observe(viewLifecycleOwner) {
                    mBinding.viewPager.setCurrentItem(1, true)
                }
            }
        }
    }

    override fun getTabFragment(position: Int): Fragment {
        val tab = getTabConfig()?.tabs?.getOrNull(position)
        return TagListFragment.newInstance(tab?.tag.orEmpty())
    }

    override fun getTabConfig(): SofaTab? {
        return AppConfig.getFindTabConfig()
    }
}