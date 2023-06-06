package com.app.jetpack.navigator

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.fragment.FragmentNavigator

@Navigator.Name("main_fragment")
class MainFragmentNavigator(
    private val context: Context,
    private val fragmentManager: FragmentManager,
    private val containerId: Int
) : FragmentNavigator(context, fragmentManager, containerId) {
    companion object {
        private const val TAG = "MainFragmentNavigator"
        private const val KEY_SAVED_IDS = "androidx-nav-fragment:navigator:savedIds"
    }

    private val savedIds = mutableSetOf<String>()

    override fun popBackStack(popUpTo: NavBackStackEntry, savedState: Boolean) {
        super.popBackStack(popUpTo, savedState)
        if (fragmentManager.isStateSaved) {
            Log.i(TAG, "Ignoring popBackStack() call: FragmentManager has already saved its state")
            return
        }
        if (savedState) {
            val beforePopList = state.backStack.value
            val initialEntry = beforePopList.first()
            // Get the set of entries that are going to be popped
            val poppedList = beforePopList.subList(
                beforePopList.indexOf(popUpTo),
                beforePopList.size
            )
            // Now go through the list in reversed order (i.e., started from the most added)
            // and save the back stack state of each.
            for (entry in poppedList.reversed()) {
                if (entry == initialEntry) {
                    Log.i(TAG, "FragmentManager cannot save the state of the initial destination $entry")
                } else {
                    fragmentManager.saveBackStack(entry.id)
                    savedIds += entry.id
                }
            }
        } else {
            fragmentManager.popBackStack(
                popUpTo.id,
                FragmentManager.POP_BACK_STACK_INCLUSIVE
            )
        }
        state.pop(popUpTo, savedState)
    }

    override fun navigate(
        entries: List<NavBackStackEntry>,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        if (fragmentManager.isStateSaved) {
            Log.i(
                TAG, "Ignoring navigate() call: FragmentManager has already saved its state"
            )
            return
        }
        for (entry in entries) {
            navigate(entry, navOptions, navigatorExtras)
        }
    }

    private fun navigate(
        entry: NavBackStackEntry,
        navOptions: NavOptions?,
        navigatorExtras: Navigator.Extras?
    ) {
        val initialNavigation = state.backStack.value.isEmpty()
        val restoreState = (
                navOptions != null && !initialNavigation &&
                        navOptions.shouldRestoreState() &&
                        savedIds.remove(entry.id)
                )
        if (restoreState) {
            // Restore back stack does all the work to restore the entry
            fragmentManager.restoreBackStack(entry.id)
            state.push(entry)
            return
        }
        val ft = createFragmentTransaction(entry, navOptions)

        if (!initialNavigation) {
            ft.addToBackStack(entry.id)
        }

        if (navigatorExtras is Extras) {
            for ((key, value) in navigatorExtras.sharedElements) {
                ft.addSharedElement(key, value)
            }
        }
        ft.commit()
        // The commit succeeded, update our view of the world
        state.push(entry)
    }

    private fun createFragmentTransaction(
        entry: NavBackStackEntry,
        navOptions: NavOptions?
    ): FragmentTransaction {
        val destination = entry.destination as Destination
//        val frag = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
//        frag.arguments = args
        val ft = fragmentManager.beginTransaction()
        var enterAnim = navOptions?.enterAnim ?: -1
        var exitAnim = navOptions?.exitAnim ?: -1
        var popEnterAnim = navOptions?.popEnterAnim ?: -1
        var popExitAnim = navOptions?.popExitAnim ?: -1
        if (enterAnim != -1 || exitAnim != -1 || popEnterAnim != -1 || popExitAnim != -1) {
            enterAnim = if (enterAnim != -1) enterAnim else 0
            exitAnim = if (exitAnim != -1) exitAnim else 0
            popEnterAnim = if (popEnterAnim != -1) popEnterAnim else 0
            popExitAnim = if (popExitAnim != -1) popExitAnim else 0
            ft.setCustomAnimations(enterAnim, exitAnim, popEnterAnim, popExitAnim)
        }

        val fragment = fragmentManager.primaryNavigationFragment
        if (fragment != null) {
            ft.hide(fragment)
        }
        var frag: Fragment?
        val tag = destination.id.toString()
        frag = fragmentManager.findFragmentByTag(tag)
        if (frag != null) {
            ft.show(frag)
        } else {
            val args = entry.arguments
            var className = destination.className
            if (className[0] == '.') {
                className = context.packageName + className
            }
            frag = fragmentManager.fragmentFactory.instantiate(context.classLoader, className)
            frag.arguments = args
            ft.add(containerId, frag, tag)
        }
//        ft.replace(containerId, frag)
        ft.setPrimaryNavigationFragment(frag)
        ft.setReorderingAllowed(true)
        return ft
    }

    override fun onSaveState(): Bundle? {
        super.onSaveState()
        if (savedIds.isEmpty()) {
            return null
        }
        return bundleOf(KEY_SAVED_IDS to ArrayList(savedIds))
    }

    override fun onRestoreState(savedState: Bundle) {
        super.onRestoreState(savedState)
        val savedIds = savedState.getStringArrayList(KEY_SAVED_IDS)
        if (savedIds != null) {
            this.savedIds.clear()
            this.savedIds += savedIds
        }
    }
}