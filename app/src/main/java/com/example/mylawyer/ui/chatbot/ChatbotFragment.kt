package com.example.mylawyer.ui.chatbot

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mylawyer.R
import com.example.mylawyer.ads.BannerAds
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.data.model.Message
import com.example.mylawyer.databinding.FragmentChatbotBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.utils.ReactionManager
import com.example.mylawyer.utils.UserIdManager
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(requireContext())
    }
    private lateinit var adapter: MessageAdapter
    private val localMessages = mutableListOf<Message>()
    private val args: ChatbotFragmentArgs by navArgs()
    private var retryTimer: CountDownTimer? = null
    private var lastFailedMessage: String? = null
    private var retryJob: Job? = null
    private var retryAttempts = 0
    private val maxRetryAttempts = 3

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Проверка авторизации
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_chatbotFragment_to_authFragment)
            return
        }
        setupRecyclerView()
        setupObservers()
        setupSendButton()
        setupRetryButton()
        setupChatHistoryButton()
        setupNewChatButton()
        bannerAdsChatBot()

        // Восстанавливаем chatId из savedInstanceState или SharedPreferences
        val savedChatId = savedInstanceState?.getString("chatId") ?: UserIdManager.getCurrentChatId(
            requireContext()
        )
        if (savedChatId != null && savedChatId == viewModel.currentChatId.value) {
            Log.d(
                "ChatbotFragment",
                "Восстановлен chatId: $savedChatId, используем кэшированные сообщения"
            )
            adapter.submitList(localMessages.toList())
            updateTextViewVisibility()
        } else {
            // Проверяем, открыт ли существующий чат
            args.chatId?.let { chatId ->
                Log.d("ChatbotFragment", "Загрузка чата с chatId: $chatId")
                viewModel.setCurrentChatId(chatId)
                viewModel.loadChatMessages(chatId)
            } ?: run {
                Log.d("ChatbotFragment", "chatId не предоставлен")
                viewModel.currentChatId.value?.let { currentChatId ->
                    Log.d("ChatbotFragment", "Загрузка сообщений для chatId: $currentChatId")
                    viewModel.loadChatMessages(currentChatId)
                } ?: run {
                    Log.d("ChatbotFragment", "Нет chatId, инициализируем чат")
                    viewModel.initializeDefaultChat()
                    // Скрываем индикаторы на старте для пустого чата
                    binding.typingAnimation.visibility = View.GONE
                    binding.chatLoadingProgressBar.visibility = View.GONE
                    updateTextViewVisibility()
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Сохраняем текущий chatId
        viewModel.currentChatId.value?.let { chatId ->
            outState.putString("chatId", chatId)
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter(
            onLikeClick = { message ->
                Log.d(
                    "ChatbotFragment",
                    "Лайк нажат для сообщения: id=${message.id}, reaction=${message.reaction}"
                )
                message.id?.let { id ->
                    val newReaction = if (message.reaction == 1) 0 else 1
                    // Временно обновляем локально для мгновенного UI-отклика
                    val index = localMessages.indexOfFirst { it.id == id }
                    if (index != -1) {
                        localMessages[index] = localMessages[index].copy(reaction = newReaction)
                        updateAdapter()
                    }
                    Log.d(
                        "ChatbotFragment",
                        "Вызываем sendReaction: id=$id, newReaction=$newReaction"
                    )
                    viewModel.sendReaction(id, newReaction)
                } ?: Log.d("ChatbotFragment", "ID сообщения отсутствует: ${message.text}")
            },
            onDislikeClick = { message ->
                Log.d(
                    "ChatbotFragment",
                    "Дизлайк нажат для сообщения: id=${message.id}, reaction=${message.reaction}"
                )
                message.id?.let { id ->
                    val newReaction = if (message.reaction == 2) 0 else 2
                    // Временно обновляем локально для мгновенного UI-отклика
                    val index = localMessages.indexOfFirst { it.id == id }
                    if (index != -1) {
                        localMessages[index] = localMessages[index].copy(reaction = newReaction)
                        updateAdapter()
                    }
                    Log.d(
                        "ChatbotFragment",
                        "Вызываем sendReaction: id=$id, newReaction=$newReaction"
                    )
                    viewModel.sendReaction(id, newReaction)
                } ?: Log.d("ChatbotFragment", "ID сообщения отсутствует: ${message.text}")
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                stackFromEnd = true
            }
            adapter = this@ChatbotFragment.adapter
            itemAnimator = SlideInUpAnimator().apply { addDuration = 200 }
        }
    }

    private fun setupObservers() {
        viewModel.messages.observe(viewLifecycleOwner) { responses ->
            if (responses.isNotEmpty()) {
                val lastResponse = responses.last()
                if (!localMessages.any { it.text == lastResponse.response && !it.isUser }) {
                    lastResponse.response?.let { response ->
                        localMessages.add(
                            Message(
                                text = response,
                                isUser = false,
                                botResponse = response
                            )
                        )
                        updateAdapter()
                        hideErrorCard()
                    }
                }
            }
            updateTextViewVisibility()
        }

        viewModel.chatMessages.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { messages ->
                Log.d("ChatbotFragment", "Получено сообщений из API: ${messages.size}")
                val lastUserMessage = localMessages.lastOrNull { it.isUser }
                val newMessages = mutableListOf<Message>()
                messages.forEach { message ->
                    message.userMessage?.takeIf { it.isNotEmpty() }?.let { userMsg ->
                        newMessages.add(
                            Message(
                                text = userMsg,
                                isUser = true,
                                userMessage = userMsg
                            )
                        )
                    }
                    message.botResponse?.takeIf { it.isNotEmpty() }?.let { botMsg ->
                        newMessages.add(
                            Message(
                                id = message.id,
                                text = botMsg,
                                isUser = false,
                                botResponse = botMsg,
                                reaction = message.reaction
                            )
                        )
                    }
                }
                if (lastUserMessage != null && !newMessages.any { it.text == lastUserMessage.text && it.isUser }) {
                    newMessages.add(lastUserMessage)
                }
                localMessages.clear()
                localMessages.addAll(newMessages)
                updateAdapter()
                updateTextViewVisibility()
                hideErrorCard()
            }
        }

        // Наблюдатель за обновлением реакции
        viewModel.reactionUpdate.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { reactionUpdate ->
                Log.d(
                    "ChatbotFragment",
                    "Обновление реакции: messageId=${reactionUpdate.messageId}, reaction=${reactionUpdate.reaction}"
                )
                val index = localMessages.indexOfFirst { it.id == reactionUpdate.messageId }
                if (index != -1) {
                    localMessages[index] =
                        localMessages[index].copy(reaction = reactionUpdate.reaction)
                    updateAdapter()
                } else {
                    Log.w(
                        "ChatbotFragment",
                        "Сообщение с id=${reactionUpdate.messageId} не найдено в localMessages"
                    )
                }
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { error ->
                if (error.contains("Требуется повторная авторизация")) {
                    Firebase.auth.signOut()
                    findNavController().navigate(R.id.action_chatbotFragment_to_authFragment)
                } else {
                    if (error.startsWith("timeout")) {
                        binding.errorTitle.text = "Mylawyer не смог закончить ответ"
                        binding.errorMessage.text = "Пожалуйста, попробуйте позже"
                        showErrorCard()
                        startRetryCountdown()
                    } else if (error.contains("Нет соединения с интернетом")) {
                        binding.errorTitle.text = "Нет соединения"
                        binding.errorMessage.text = "Проверьте подключение к интернету"
                        showErrorCard()
                        retryTimer?.cancel()
                        binding.retryButton.text = "Проверить"
                        binding.retryButton.isEnabled = true
                        binding.retryButton.setOnClickListener {
                            if (isNetworkAvailable()) {
                                if (error.contains("Не удалось загрузить чаты")) {
                                    viewModel.syncChats()
                                } else {
                                    retryLastMessage()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Нет интернета", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        binding.errorTitle.text = "Ошибка"
                        binding.errorMessage.text = error
                        showErrorCard()
                        retryTimer?.cancel()
                        binding.retryButton.text = getString(R.string.retry)
                        binding.retryButton.isEnabled = true
                        binding.retryButton.setOnClickListener {
                            if (error.contains("Не удалось загрузить чаты")) {
                                viewModel.syncChats()
                            } else {
                                retryLastMessage()
                            }
                        }
                    }
                    Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        viewModel.isWaitingForBotResponse.observe(viewLifecycleOwner) { isWaiting ->
            binding.typingAnimation.visibility = if (isWaiting) View.VISIBLE else View.GONE
            if (isWaiting) {
                binding.typingAnimation.playAnimation()
            } else {
                binding.typingAnimation.pauseAnimation()
            }
        }

        viewModel.isLoadingMessages.observe(viewLifecycleOwner) { isLoading ->
            binding.chatLoadingProgressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = requireContext().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val text = binding.edTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                lastFailedMessage = text
                Log.d("ChatbotFragment", "Отправка сообщения: $text")
                localMessages.add(
                    Message(
                        text = text,
                        isUser = true,
                        userMessage = text
                    )
                )
                updateAdapter()
                viewModel.sendMessage(text)
                binding.edTextMessage.text.clear()
                updateTextViewVisibility()
            }
        }
    }

    private fun setupRetryButton() {
        binding.retryButton.setOnClickListener {
            // Кнопка неактивна во время отсчета, поэтому клик возможен только после завершения таймера
            retryLastMessage()
        }
    }

    private fun setupChatHistoryButton() {
        binding.btChatHistory.setOnClickListener {
            findNavController().navigate(R.id.action_chatbotFragment_to_chatHistoryFragment)
        }
    }

    private fun setupNewChatButton() {
        binding.btNewChat.setOnClickListener {
            viewModel.createNewChat()
            localMessages.clear()
            updateAdapter()
            updateTextViewVisibility()
            hideErrorCard()
        }
    }

    private fun updateAdapter() {
        Log.d(
            "ChatbotFragment",
            "localMessages: ${localMessages.map { "id=${it.id}, text=${it.text}, reaction=${it.reaction}" }}"
        )
        adapter.submitList(localMessages.toList()) {
            Log.d("ChatbotFragment", "Адаптер обновлён, сообщений: ${localMessages.size}")
            binding.recyclerView.scrollToPosition(localMessages.size - 1)
        }
    }

    private fun updateTextViewVisibility() {
        binding.textView.visibility = if (localMessages.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun showErrorCard() {
        binding.errorCardView.visibility = View.VISIBLE
        binding.typingAnimation.visibility = View.GONE
    }

    private fun hideErrorCard() {
        binding.errorCardView.visibility = View.GONE
        retryTimer?.cancel()
        retryJob?.cancel()
        binding.retryButton.text = getString(R.string.retry)
        binding.retryButton.isEnabled = true
    }

    private fun startRetryCountdown() {
        if (retryAttempts >= maxRetryAttempts) {
            binding.retryButton.text = getString(R.string.retry)
            binding.retryButton.isEnabled = true
            return
        }
        retryTimer?.cancel()
        retryJob?.cancel()

        binding.retryButton.isEnabled = false
        retryTimer = object : CountDownTimer(5000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = (millisUntilFinished / 1000).toInt()
                binding.retryButton.text = getString(R.string.retry_with_countdown, secondsLeft)
            }

            override fun onFinish() {
                binding.retryButton.isEnabled = true
                binding.retryButton.text = getString(R.string.retry)
                retryLastMessage()
                retryAttempts++
            }
        }.start()
    }

    private fun retryLastMessage() {
        lastFailedMessage?.let { message ->
            retryJob?.cancel()
            retryJob = MainScope().launch {
                delay(500) // Задержка для предотвращения нагрузки на сервер
                viewModel.sendMessage(message)
            }
        }
    }

    fun scrollToBottom() {
        binding.recyclerView.post {
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    private fun bannerAdsChatBot() {
        BannerAds.initializeBanner(binding.bannerChatBot, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        retryTimer?.cancel()
        retryJob?.cancel()
        _binding = null
    }
}