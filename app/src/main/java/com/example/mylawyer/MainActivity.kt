package com.example.mylawyer

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.databinding.ActivityMainBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.ui.chatbot.ChatViewModel
import com.example.mylawyer.ui.chatbot.ChatbotFragment
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var navController: NavController
    private lateinit var auth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private val REQUEST_CODE_UPDATE = 999

    override fun onCreate(savedInstanceState: Bundle?) {
        // Применяем сохраненную тему перед установкой контента
        val sharedPreferences = getSharedPreferences("MyLawyerPrefs", MODE_PRIVATE)
        val currentTheme = sharedPreferences.getString("theme", "system") ?: "system"
        when (currentTheme) {
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
//        val repository = ChatRepository(RetrofitInstance.createApi(this))
        val viewModelFactory = ChatViewModelFactory(this)
        chatViewModel = ViewModelProvider(this, viewModelFactory).get(ChatViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

        // Настройка BottomNavigationView
        binding.bottomNav.setupWithNavController(navController)

        // Слушатель изменений авторизации
        authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                // Пользователь не авторизован: переходим к AuthFragment и скрываем меню
                binding.bottomNav.visibility = View.GONE
            } else {
                // Пользователь авторизован: показываем меню (если не AuthFragment)
                if (navController.currentDestination?.id != R.id.authFragment) {
                    binding.bottomNav.visibility = View.VISIBLE
                }
            }
        }
        auth.addAuthStateListener(authStateListener)

        // Слушатель изменений назначения для управления видимостью BottomNavigationView
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment -> binding.bottomNav.visibility = View.GONE
                else -> {
                    if (auth.currentUser != null) {
                        binding.bottomNav.visibility = View.VISIBLE
                    }
                }
            }
        }

        // Обработка повторного нажатия на элемент BottomNavigationView
        binding.bottomNav.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.chatbotFragment -> {
                    val currentFragment = supportFragmentManager
                        .findFragmentById(R.id.fragmentContainerView)
                        ?.childFragmentManager
                        ?.fragments
                        ?.firstOrNull()

                    if (currentFragment is ChatbotFragment) {
                        // Если уже в ChatbotFragment, прокручиваем вниз
                        currentFragment.scrollToBottom()
                    } else {
                        // Проверяем, есть ли ChatbotFragment в стеке
                        if (navController.popBackStack(R.id.chatbotFragment, false)) {
                            // Если ChatbotFragment найден в стеке, возвращаемся к нему
                            // Состояние (chatId) сохраняется в ChatbotFragment
                        } else {
                            // Если ChatbotFragment не в стеке, открываем новый с текущим chatId
                            chatViewModel.currentChatId.value?.let { chatId ->
                                val bundle = bundleOf("chatId" to chatId)
                                navController.navigate(R.id.chatbotFragment, bundle)
                            } ?: navController.navigate(R.id.chatbotFragment)
                        }
                    }
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_UPDATE) {
            if (resultCode != RESULT_OK) {
                Toast.makeText(
                    this,
                    getString(R.string.update_cancelled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Удаляем слушатель при уничтожении активности
        auth.removeAuthStateListener(authStateListener)
    }
}