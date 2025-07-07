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
import com.example.mylawyer.databinding.FragmentSettingsDeleteDataBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class SettingsDataFragment : Fragment() {

    private var _binding: FragmentSettingsDeleteDataBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsDeleteDataBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Проверка авторизации
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
            return
        }
        setupBackButton()
        setupDeleteAllChats()
        setupDeleteAccount()
        bannerAdsSettings()
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_settingsDataFragment_to_settingsFragment)
        }
    }

    private fun setupDeleteAllChats() {
        binding.cardDeleteChatsLinear.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Удаление чатов")
                .setMessage("Вы уверены, что хотите удалить все чаты?")
                .setPositiveButton("Удалить") { _, _ ->
                    Log.d("ChatHistoryFragment", "Удаление чатов")
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun setupDeleteAccount() {
        binding.cardAccountLinear.setOnClickListener {
            android.app.AlertDialog.Builder(requireContext())
                .setTitle("Удаление учетной записи")
                .setMessage("Вы уверены, что хотите удалить учетку?")
                .setPositiveButton("Удалить") { _, _ ->
                    Log.d("ChatHistoryFragment", "Удаление учетной записи")
                }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun bannerAdsSettings() {
        BannerAds.initializeBanner(binding.bannerSettingsData, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}