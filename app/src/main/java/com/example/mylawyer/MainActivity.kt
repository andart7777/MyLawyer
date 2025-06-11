package com.example.mylawyer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.databinding.ActivityMainBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.ui.chatbot.ChatViewModel
import com.example.mylawyer.ui.chatbot.ChatbotFragment
import com.example.mylawyer.viewmodel.ChatViewModelFactory

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var chatViewModel: ChatViewModel
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Инициализация ViewModel
        val repository = ChatRepository(RetrofitInstance.api)
        val viewModelFactory = ChatViewModelFactory(repository, this)
        chatViewModel = ViewModelProvider(this, viewModelFactory).get(ChatViewModel::class.java)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        navController = navHostFragment.navController

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
}