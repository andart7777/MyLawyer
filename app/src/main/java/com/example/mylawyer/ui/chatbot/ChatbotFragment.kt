package com.example.mylawyer.ui.chatbot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.mylawyer.utils.UserIdManager
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator


class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(requireContext())
    }
    private lateinit var adapter: MessageAdapter
    private val localMessages = mutableListOf<Message>()
    private val args: ChatbotFragmentArgs by navArgs()

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
        setupChatHistoryButton()
        setupNewChatButton()
        setupSignOutButton()
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
                Log.e("ChatbotFragment", "Ошибка: $error")
                android.widget.Toast.makeText(
                    context,
                    "Ошибка: $error",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
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

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val text = binding.edTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
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
        }
    }

    private fun setupSignOutButton() {
        binding.signOutButton.setOnClickListener {
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_chatbotFragment_to_authFragment)
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
        _binding = null
    }
}