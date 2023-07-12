package com.app.jetpack

import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.jetpack.databinding.ActivityMainBinding
import com.app.jetpack.ui.login.UserManager
import com.app.jetpack.utils.AppConfig
import com.app.jetpack.utils.NavGraphBuilder
import com.app.lib_common.base.BaseActivity

class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        NavGraphBuilder.build(navController, this, R.id.nav_host_fragment_activity_main)
        navView.setOnItemSelectedListener {
            for (node in AppConfig.getDestConfig().values) {
                if (node.needLogin && UserManager.isLogin().not() && node.id == it.itemId) {
                    UserManager.login(this).observe(this) { _ ->
                        navView.selectedItemId = it.itemId
                    }
                    return@setOnItemSelectedListener false
                }
            }
            AppConfig.getDestConfig().values
            navController.navigate(it.itemId)
            it.title.isNullOrEmpty().not()
        }
    }
}