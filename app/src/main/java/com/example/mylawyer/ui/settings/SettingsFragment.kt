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

    private fun bannerAdsSettings() {
        BannerAds.initializeBanner(binding.bannerSettings, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}