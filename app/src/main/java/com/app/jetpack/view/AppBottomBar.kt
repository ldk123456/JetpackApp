package com.app.jetpack.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.app.jetpack.R
import com.app.jetpack.utils.AppConfig
import com.app.lib_common.ext.color
import com.app.lib_common.ext.dp
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import com.google.android.material.bottomnavigation.BottomNavigationMenuView
import com.google.android.material.bottomnavigation.BottomNavigationView

class AppBottomBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.bottomNavigationStyle,
    defStyleRes: Int = com.google.android.material.R.style.Widget_Design_BottomNavigationView
) : BottomNavigationView(context, attrs, defStyleAttr, defStyleRes) {

    private val sIcons = intArrayOf(
        R.drawable.icon_tab_home,
        R.drawable.icon_tab_sofa,
        R.drawable.icon_tab_publish,
        R.drawable.icon_tab_find,
        R.drawable.icon_tab_mine
    )

    init {
        init()
    }

    @SuppressLint("RestrictedApi")
    private fun init() {
        AppConfig.getBottomBarConfig()?.apply {
            val state = arrayOf(
                intArrayOf(android.R.attr.state_selected),
                intArrayOf()
            )
            val colors = intArrayOf(activeColor.color, inActiveColor.color)
            val stateList = ColorStateList(state, colors)
            itemTextColor = stateList
            itemIconTintList = stateList
            labelVisibilityMode = LABEL_VISIBILITY_LABELED
            for (tab in tabs) {
                if (tab.enable.not()) {
                    continue
                }
                val itemId = tab.pageUrl.itemId()
                if (itemId < 0) {
                    continue
                }
                menu.add(0, itemId, tab.index, tab.title).apply {
                    setIcon(sIcons[tab.index])
                }
            }

            // 给按钮icon设置大小
            var index = 0
            for (tab in tabs) {
                if (tab.enable.not()) {
                    continue
                }
                val itemId = tab.pageUrl.itemId()
                if (itemId < 0) {
                    continue
                }
                val itemView =
                    (getChildAt(0) as? BottomNavigationMenuView)?.getChildAt(index) as? BottomNavigationItemView
                itemView?.setIconSize(tab.size.dp)
                if (tab.title.isEmpty()) {
                    tab.tintColor.takeIf { it.isNotEmpty() }?.let {
                        itemView?.setIconTintList(ColorStateList.valueOf(it.color))
                    }
                    itemView?.setShifting(false)
                }
                index++
            }

            // 底部导航栏默认选中项
            if (selectTab != 0) {
                val select = tabs.getOrNull(selectTab)
                if (select?.enable == true) {
                    val itemId = select.pageUrl.itemId()
                    //这里需要延迟一下 再定位到默认选中的tab
                    //因为需要等待内容区域,也就NavGraphBuilder解析数据并初始化完成，
                    //否则会出现 底部按钮切换过去了，但内容区域还没切换过去
                    post { selectedItemId = itemId }
                }
            }
        }
    }

    private fun String.itemId(): Int {
        return AppConfig.getDestConfig()[this]?.id ?: -1
    }
}