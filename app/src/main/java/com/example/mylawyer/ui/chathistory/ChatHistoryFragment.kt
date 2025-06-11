package com.example.mylawyer.ui.chatbot

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mylawyer.ChatHistoryAdapter
import com.example.mylawyer.R
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.data.model.ChatHistoryItem
import com.example.mylawyer.databinding.FragmentChatHistoryBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator

class ChatHistoryFragment : Fragment() {

    private var _binding: FragmentChatHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(ChatRepository(RetrofitInstance.api), requireContext())
    }
    private lateinit var adapter: ChatHistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
        viewModel.loadChats()
    }

    private fun setupRecyclerView() {
        adapter = ChatHistoryAdapter(
            onChatClick = { chat ->
                Log.d("ChatHistoryFragment", "Выбран чат с chatId: ${chat.chatId}")
                val action = ChatHistoryFragmentDirections.actionChatHistoryFragmentToChatbotFragment(
                    chatId = chat.chatId
                )
                findNavController().navigate(action)
            },
            onDeleteClick = { chat ->
                android.app.AlertDialog.Builder(requireContext())
                    .setTitle("Удалить чат?")
                    .setMessage("Вы уверены, что хотите удалить чат \"${chat.title}\"?")
                    .setPositiveButton("Удалить") { _, _ ->
                        Log.d("ChatHistoryFragment", "Удаление чата с chatId: ${chat.chatId}")
                        viewModel.deleteChat(chat.chatId)
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
        )
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatHistoryFragment.adapter
            itemAnimator = SlideInUpAnimator().apply { addDuration = 200 }
        }
    }

private fun setupObservers() {
    viewModel.chats.observe(viewLifecycleOwner) { chats: List<ChatHistoryItem> ->
        Log.d("ChatHistoryFragment", "Получено чатов: ${chats.size}, данные: $chats")
        if (chats.isEmpty()) {
            Log.d("ChatHistoryFragment", "Список чатов пуст, возвращаемся в ChatbotFragment")
            binding.textView.visibility = View.VISIBLE
            findNavController().navigate(R.id.action_chatHistoryFragment_to_chatbotFragment)
        } else {
            adapter.submitList(chats)
            binding.textView.visibility = View.GONE
        }
    }
    viewModel.error.observe(viewLifecycleOwner) { event ->
        event.getContentIfNotHandled()?.let { error ->
            Log.e("ChatHistoryFragment", "Ошибка: $error")
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
        }
    }
    viewModel.isLoadingMessages.observe(viewLifecycleOwner) { isLoading ->
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}