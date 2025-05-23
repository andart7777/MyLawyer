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
import com.example.mylawyer.ChatAdapter
import com.example.mylawyer.R
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.data.model.ChatListItem
import com.example.mylawyer.databinding.FragmentChatHistoryBinding
import com.example.mylawyer.repository.ChatRepository
import com.example.mylawyer.viewmodel.ChatHistoryViewModel
import com.example.mylawyer.viewmodel.ChatHistoryViewModelFactory
import jp.wasabeef.recyclerview.animators.SlideInUpAnimator
import java.util.UUID
import com.example.mylawyer.data.model.ChatListResponse

class ChatHistoryFragment : Fragment() {

    private var _binding: FragmentChatHistoryBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChatHistoryViewModel by viewModels {
        ChatHistoryViewModelFactory(ChatRepository(RetrofitInstance.api), requireContext())
    }
    private lateinit var adapter: ChatAdapter

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
        viewModel.fetchChats()
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter { chat ->
            // Переход к чату
            val action = ChatHistoryFragmentDirections.actionChatHistoryFragmentToChatbotFragment(chat.chatId)
            findNavController().navigate(action)
        }
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ChatHistoryFragment.adapter
            itemAnimator = SlideInUpAnimator().apply { addDuration = 200 }
        }
    }

    private fun setupObservers() {
        viewModel.chats.observe(viewLifecycleOwner) { chats: List<ChatListResponse.ChatListItem> ->
            Log.d("ChatHistoryFragment", "Received ${chats.size} chats")
            adapter.submitList(chats)
            binding.textView.visibility = if (chats.isEmpty()) View.VISIBLE else View.GONE
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            Log.e("ChatHistoryFragment", "Error: $error")
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



//package com.example.mylawyer.ui.chathistory
//
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import androidx.fragment.app.Fragment
//import androidx.navigation.fragment.findNavController
//import androidx.recyclerview.widget.LinearLayoutManager
//import com.example.mylawyer.data.model.ChatHistoryItem
//import com.example.mylawyer.databinding.FragmentChatHistoryBinding
//import java.text.SimpleDateFormat
//import java.util.Calendar
//import java.util.Date
//import java.util.Locale
//
//class ChatHistoryFragment : Fragment() {
//
//    private var _binding: FragmentChatHistoryBinding? = null
//    private val binding get() = _binding!!
//    private lateinit var chatHistoryAdapter: ChatHistoryAdapter
//
//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View {
//        _binding = FragmentChatHistoryBinding.inflate(inflater, container, false)
//        return binding.root
//    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        chatHistoryAdapter = ChatHistoryAdapter { selectedItem ->
//            findNavController().popBackStack()
//        }
//
//        binding.recyclerView.apply {
//            layoutManager = LinearLayoutManager(requireContext())
//            adapter = chatHistoryAdapter
//        }
//
//        val fakeData = listOf(
//            ChatHistoryItem(1, "Что делать если не выдали чек?", "Часть сообщения мелким текстом", Date()),
//            ChatHistoryItem(2, "Должен быть сертификат качества на мыло?", "Часть сообщения мелким текстом", hoursAgo(1)),
//            ChatHistoryItem(3, "Сделать возврат товаров возможно через сколько дней?", "Часть сообщения мелким текстом", daysAgo(2))
//        )
//
//        chatHistoryAdapter.submitList(fakeData)
//    }
//
//    private fun formatDate(date: Date): String {
//        val calendar = Calendar.getInstance()
//        val today = Calendar.getInstance()
//        val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
//
//        calendar.time = date
//
//        return when {
//            isSameDay(calendar, today) -> SimpleDateFormat("HH:mm", Locale.getDefault()).format(date)
//            isSameDay(calendar, yesterday) -> "Вчера"
//            else -> SimpleDateFormat("d MMMM", Locale("ru")).format(date)
//        }
//    }
//
//    private fun isSameDay(cal1: Calendar, cal2: Calendar): Boolean {
//        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
//                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
//    }
//
//    private fun hoursAgo(hours: Int): Date {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.HOUR_OF_DAY, -hours)
//        return calendar.time
//    }
//
//    private fun daysAgo(days: Int): Date {
//        val calendar = Calendar.getInstance()
//        calendar.add(Calendar.DAY_OF_YEAR, -days)
//        return calendar.time
//    }
//
//    override fun onDestroyView() {
//        super.onDestroyView()
//        _binding = null
//    }
//}
