package com.example.mylawyer.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mylawyer.R
import com.example.mylawyer.ads.BannerAds
import com.example.mylawyer.databinding.FragmentSettingsBinding
import com.example.mylawyer.utils.ReactionManager
import com.example.mylawyer.utils.UserIdManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Проверка авторизации
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
            return
        }
        setupSignOutButton()
        setupEmailDisplay()
        bannerAdsSettings()
    }

    private fun setupSignOutButton() {
        binding.cardLogout2.setOnClickListener {
            Log.d("SettingsFragment", "Клик по кнопке разлогинивания")
            UserIdManager.clearCurrentChatId(requireContext())
            ReactionManager.clearReactions(requireContext())
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
        }
    }

    private fun setupEmailDisplay() {
        val user = Firebase.auth.currentUser
        val email = user?.email
        if (email != null) {
            val maskedEmail = maskEmail(email)
            // Находим TextView внутри card_email
            binding.emailMask.text = maskedEmail
        } else {
            Log.w("SettingsFragment", "Email пользователя не найден")
            binding.emailMask.text = "Email не доступен"
        }
    }

    private fun maskEmail(email: String): String {
        val indexOfAt = email.indexOf('@')
        if (indexOfAt < 3) return email // Если email слишком короткий, возвращаем как есть
        val localPart = email.substring(0, indexOfAt)
        val domain = email.substring(indexOfAt)
        return when {
            localPart.length <= 4 -> "${localPart.substring(0, 2)}${localPart.drop(2)}"
            else -> "${localPart.substring(0, 2)}${"*".repeat(localPart.length - 4)}${localPart.takeLast(2)}$domain"
        }
    }

    private fun bannerAdsSettings() {
        BannerAds.initializeBanner(binding.bannerSettings, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}