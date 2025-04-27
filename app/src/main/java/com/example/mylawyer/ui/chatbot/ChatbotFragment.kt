package com.example.mylawyer.ui.chatbot

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mylawyer.Message
import com.example.mylawyer.MessageAdapter
import com.example.mylawyer.R
import com.example.mylawyer.databinding.FragmentChatbotBinding
import com.facebook.shimmer.ShimmerFrameLayout
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ChatbotFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MessageAdapter
    private lateinit var editTextMessage: EditText
    private lateinit var buttonSend: ImageButton
    private val message = mutableListOf<Message>(
//        Message("Hello, how can I help you?", false),
//        Message("I need help with my case.", true),
//        Message("Sure, please provide me with the details.", false),
//        Message("I have a court date next week.", true)
    )
    private lateinit var binding: FragmentChatbotBinding
    private val viewModel: ChatViewModel by viewModels()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatbotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.recyclerView
        editTextMessage = binding.edTextMessage
        buttonSend = binding.sendButton

        // Плавный скролл сообщений
        recyclerView.itemAnimator = SlideInUpAnimator().apply {
            addDuration = 200
        }

        // Скрываем текст превью если есть сообщения
        if (message.isNotEmpty()) {
            binding.textView.visibility = View.GONE
        }

        val layoutManager = LinearLayoutManager(requireContext())
        layoutManager.stackFromEnd = true // Начало списка сообщений внизу
        recyclerView.layoutManager = layoutManager

        adapter = MessageAdapter(message)
        recyclerView.adapter = adapter

        // Подписка на ответ бота
        viewModel.botResponse.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { botReply ->
                message.add(Message(botReply, false))
                adapter.notifyItemInserted(message.size - 1)
                recyclerView.scrollToPosition(message.size - 1)
            }
        }

        // Прелоадер
        val typingAnimation = binding.typingAnimation

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                typingAnimation.visibility = View.VISIBLE
                typingAnimation.playAnimation()
            } else {
                typingAnimation.pauseAnimation()
                typingAnimation.visibility = View.GONE
            }
        }

        buttonSend.setOnClickListener {
            val text = editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {

                // Скрываем текст превью если есть сообщения
                if (binding.textView.visibility == View.VISIBLE) {
                    binding.textView.visibility = View.GONE
                }

                message.add(Message(text, true))
//                message.add(Message("Ответ бота $text", false))
//                adapter.notifyItemRangeInserted(message.size - 2, 2)
                adapter.notifyItemInserted(message.size - 1)
                recyclerView.scrollToPosition(message.size - 1)

                // Отправляем сообщение на сервер
                viewModel.sendMessage(text)

                editTextMessage.text.clear()
            }
        }

        binding.btChatHistory.setOnClickListener {
            findNavController().navigate(R.id.action_chatbotFragment_to_chatHistoryFragment)
        }
    }

    // Прокрутка вниз при двойном нажатии на "чат" в bottom menu
    fun scrollToBottom() {
        binding.recyclerView.post {
            binding.recyclerView.scrollToPosition(
                binding.recyclerView.adapter?.itemCount?.minus(1) ?: 0
            )
        }
    }
}