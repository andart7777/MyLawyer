package com.example.mylawyer.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mylawyer.R
import com.example.mylawyer.ads.BannerAds
import com.example.mylawyer.databinding.FragmentTermsBinding
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import io.noties.markwon.Markwon

class TermsFragment : Fragment() {

    private var _binding: FragmentTermsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTermsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Проверка авторизации
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_termsFragment_to_authFragment)
            return
        }

        // Настройка кнопки возврата
        binding.btnBack.setOnClickListener {
            findNavController().navigate(R.id.action_termsFragment_to_settingsFragment)
        }

        // Загрузка рекламы
        BannerAds.initializeBanner(binding.bannerTerms, requireContext())

        // Загрузка текста документов
        loadTermsContent()
    }

    private fun loadTermsContent() {
        // Загружаем текст "Условий использования" и "Политики конфиденциальности"
        val termsOfUse = requireContext().resources.openRawResource(R.raw.terms_of_use)
            .bufferedReader().use { it.readText() }
        val privacyPolicy = requireContext().resources.openRawResource(R.raw.privacy_policy)
            .bufferedReader().use { it.readText() }

        // Объединяем тексты с разделителем
        val combinedText = "$termsOfUse\n\n\n$privacyPolicy"

        // Используем Markwon для рендеринга markdown
        val markwon = Markwon.create(requireContext())
         markwon.setMarkdown(binding.tvTermsContent, combinedText)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.bannerTerms?.destroy()
        _binding = null
    }
}