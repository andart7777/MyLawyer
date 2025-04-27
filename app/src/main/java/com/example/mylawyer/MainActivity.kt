package com.example.mylawyer

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mylawyer.databinding.ActivityMainBinding
import com.example.mylawyer.ui.chatbot.ChatbotFragment

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        // Привязка навигации к нижнему меню
        binding.bottomNav.setupWithNavController(navController)

        // Обработка повторного нажатия
        binding.bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.chatbotFragment -> {
                    val currentFragment = supportFragmentManager
                        .findFragmentById(R.id.fragmentContainerView)
                        ?.childFragmentManager
                        ?.fragments
                        ?.firstOrNull()

                    if (currentFragment is ChatbotFragment) {
                        currentFragment.scrollToBottom()
                    } else {
                        navController.navigate(R.id.chatbotFragment)
                    }
                }
            }
        }
    }
}
