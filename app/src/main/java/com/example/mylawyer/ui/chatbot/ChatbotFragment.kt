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
import com.example.mylawyer.Message
import com.example.mylawyer.MessageAdapter
import com.example.mylawyer.R
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.data.model.ChatResponse
import com.example.mylawyer.databinding.FragmentChatbotBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.ui.chatbot.ChatViewModel
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ChatbotFragment : Fragment() {

    private var _binding: FragmentChatbotBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepository(RetrofitInstance.api), requireContext())
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
        setupRecyclerView()
        setupObservers()
        setupSendButton()
        setupChatHistoryButton()
        setupNewChatButton()

        // Проверяем, открыт ли существующий чат
        args.chatId?.let { chatId ->
            Log.d("ChatbotFragment", "Loading chat with chatId: $chatId")
            // Заглушка: в будущем загрузим сообщения для chatId
            viewModel.setCurrentChatId(chatId)
        } ?: run {
            Log.d("ChatbotFragment", "No chatId provided, starting new chat")
            viewModel.createNewChat()
        }
    }

    private fun setupRecyclerView() {
        adapter = MessageAdapter()
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
            // Добавляем только последний ответ бота, если он новый
            if (responses.isNotEmpty()) {
                val lastResponse = responses.last()
                // Проверяем, нет ли уже этого ответа в localMessages
                if (!localMessages.any { it.text == lastResponse.response && !it.isUser }) {
                    localMessages.add(Message(lastResponse.response, false))
                }
            }
            Log.d("ChatbotFragment", "Messages to display: $localMessages")
            adapter.submitList(localMessages.toList())
            binding.recyclerView.scrollToPosition(localMessages.size - 1)
            binding.textView.visibility = if (localMessages.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.e("ChatbotFragment", "Error: $error")
            android.widget.Toast.makeText(context, "Error: $error", android.widget.Toast.LENGTH_SHORT).show()
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.typingAnimation.visibility = View.VISIBLE
                binding.typingAnimation.playAnimation()
            } else {
                binding.typingAnimation.pauseAnimation()
                binding.typingAnimation.visibility = View.GONE
            }
        }
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val text = binding.edTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                // Добавляем сообщение пользователя сразу в UI
                localMessages.add(Message(text, true))
                adapter.submitList(localMessages.toList())
                binding.recyclerView.scrollToPosition(localMessages.size - 1)
                binding.textView.visibility = View.GONE
                // Отправляем запрос
                viewModel.sendMessage(text)
                binding.edTextMessage.text.clear()
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
            adapter.submitList(emptyList())
            binding.textView.visibility = View.VISIBLE
        }
    }

    fun scrollToBottom() {
        binding.recyclerView.post {
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




//package com.example.mylawyer.ui.chatbot
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.EditText
//import android.widget.ImageButton
//import androidx.fragment.app.Fragment
//import androidx.fragment.app.viewModels
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.example.mylawyer.MessageTemp
//import com.example.mylawyer.MessageAdapter
//import com.example.mylawyer.R
//import com.example.mylawyer.databinding.FragmentChatbotBinding
//import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
//
//class ChatbotFragment : Fragment() {
//
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: MessageAdapter
//    private lateinit var editTextMessage: EditText
//    private lateinit var buttonSend: ImageButton
//    private val message = mutableListOf<MessageTemp>(
////        Message("Hello, how can I help you?", false),
////        Message("I need help with my case.", true),
////        Message("Sure, please provide me with the details.", false),
////        Message("I have a court date next week.", true)
//    )
//    private lateinit var binding: FragmentChatbotBinding
//    private val viewModel: ChatViewModel by viewModels()
//
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        binding = FragmentChatbotBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        recyclerView = binding.recyclerView
//        editTextMessage = binding.edTextMessage
//        buttonSend = binding.sendButton
//
//        // Плавный скролл сообщений
//        recyclerView.itemAnimator = SlideInUpAnimator().apply {
//            addDuration = 200
//        }
//
//        // Скрываем текст превью если есть сообщения
//        if (message.isNotEmpty()) {
//            binding.textView.visibility = View.GONE
//        }
//
//        val layoutManager = LinearLayoutManager(requireContext())
//        layoutManager.stackFromEnd = true // Начало списка сообщений внизу
//        recyclerView.layoutManager = layoutManager
//
//        adapter = MessageAdapter(message)
//        recyclerView.adapter = adapter
//
//        // Подписка на ответ бота
////        viewModel.botResponse.observe(viewLifecycleOwner) { event ->
////            event.getContentIfNotHandled()?.let { botReply ->
////                message.add(Message(botReply, false))
////                adapter.notifyItemInserted(message.size - 1)
////                recyclerView.scrollToPosition(message.size - 1)
////            }
////        }
//
//        viewModel.messages.observe(viewLifecycleOwner) { messages ->
//            adapter.submitList(messages)
//            binding.recyclerView.scrollToPosition(messages.size - 1)
//        }
//        viewModel.error.observe(viewLifecycleOwner) { error ->
//            // Показать ошибку, например, через Toast
//            android.widget.Toast.makeText(context, error, android.widget.Toast.LENGTH_SHORT).show()
//        }
//
//        // Прелоадер
//        val typingAnimation = binding.typingAnimation
//
////        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
////            if (isLoading) {
////                typingAnimation.visibility = View.VISIBLE
////                typingAnimation.playAnimation()
////            } else {
////                typingAnimation.pauseAnimation()
////                typingAnimation.visibility = View.GONE
////            }
////        }
//
//        buttonSend.setOnClickListener {
//            val text = editTextMessage.text.toString().trim()
//            if (text.isNotEmpty()) {
//
//                // Скрываем текст превью если есть сообщения
//                if (binding.textView.visibility == View.VISIBLE) {
//                    binding.textView.visibility = View.GONE
//                }
//
//                message.add(MessageTemp(text, true))
////                message.add(Message("Ответ бота $text", false))
////                adapter.notifyItemRangeInserted(message.size - 2, 2)
//                adapter.notifyItemInserted(message.size - 1)
//                recyclerView.scrollToPosition(message.size - 1)
//
//                // Отправляем сообщение на сервер
//                viewModel.sendMessage(text)
//
//                editTextMessage.text.clear()
//            }
//        }
//
//        binding.btChatHistory.setOnClickListener {
//            findNavController().navigate(R.id.action_chatbotFragment_to_chatHistoryFragment)
//        }
//    }
//
//    // Прокрутка вниз при двойном нажатии на "чат" в bottom menu
//    fun scrollToBottom() {
//        binding.recyclerView.post {
//            binding.recyclerView.scrollToPosition(
//                binding.recyclerView.adapter?.itemCount?.minus(1) ?: 0
//            )
//        }
//    }
//}