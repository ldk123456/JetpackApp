package com.app.jetpack.utils

import android.content.ComponentName
import androidx.navigation.ActivityNavigator
import androidx.navigation.NavController
import androidx.navigation.NavGraph
import androidx.navigation.NavGraphNavigator
import androidx.navigation.fragment.FragmentNavigator

object NavGraphBuilder {
    fun build(navController: NavController) {
        val provider = navController.navigatorProvider
        val navGraph = NavGraph(NavGraphNavigator(provider))

        val fragmentNavigator = provider.getNavigator(FragmentNavigator::class.java)
        val activityNavigator = provider.getNavigator(ActivityNavigator::class.java)

        AppConfig.getDestConfig().values.forEach { node ->
            if (node.isFragment) {
                fragmentNavigator.createDestination().apply {
                    setClassName(node.className)
                }
            } else {
                activityNavigator.createDestination().apply {
                    setComponentName(ComponentName(AppGlobals.context.packageName, node.className))
                }
            }.let {
                it.id = node.id
                it.addDeepLink(node.pageUrl)
                navGraph.addDestination(it)
            }
            if (node.asStarter) {
                navGraph.setStartDestination(node.id)
            }
        }
        navController.setGraph(navGraph, null)
    }
}