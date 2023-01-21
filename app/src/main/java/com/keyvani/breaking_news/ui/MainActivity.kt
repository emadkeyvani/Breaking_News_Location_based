package com.keyvani.breaking_news.ui


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.keyvani.breaking_news.R
import com.keyvani.breaking_news.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //handle bottom navigation via navigation component
        binding.apply {
            navController = findNavController(R.id.navContainer)
            appBarConfiguration = AppBarConfiguration(setOf(R.id.LastNewsFragment, R.id.searchFragment, R.id.favoriteFragment))
            setupActionBarWithNavController(navController, appBarConfiguration)
            bottomNav.setupWithNavController(navController)
        }
    }

    //handle press back on navigation component
    override fun onNavigateUp(): Boolean {
        return navController.navigateUp() || super.onNavigateUp()
    }

}

