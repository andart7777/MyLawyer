package com.example.mylawyer.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mylawyer.R
import com.example.mylawyer.ads.BannerAds
import com.example.mylawyer.databinding.FragmentSettingsBinding
import com.example.mylawyer.utils.ReactionManager
import com.example.mylawyer.utils.UserIdManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.content.Context
import android.content.SharedPreferences
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireContext().getSharedPreferences("MyLawyerPrefs", Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Проверка авторизации
        if (Firebase.auth.currentUser == null) {
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
            return
        }
        setupDataManagementButton()
        setupEmailDisplay()
        setupSignOutButton()
        setupLanguageButton()
        bannerAdsSettings()
        updateLanguageDisplay()
    }

    private fun setupEmailDisplay() {
        val user = Firebase.auth.currentUser
        val email = user?.email
        if (email != null) {
            val maskedEmail = maskEmail(email)
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
            else -> "${
                localPart.substring(
                    0,
                    2
                )
            }${"*".repeat(localPart.length - 4)}${localPart.takeLast(2)}$domain"
        }
    }

    private fun setupDataManagementButton() {
        binding.cardDataManagementLinear.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_settingsDataFragment)
        }
    }

    private fun setupSignOutButton() {
        binding.cardLogoutLinear.setOnClickListener {
            Log.d("SettingsFragment", "Клик по кнопке разлогинивания")
            UserIdManager.clearCurrentChatId(requireContext())
            ReactionManager.clearReactions(requireContext())
            Firebase.auth.signOut()
            findNavController().navigate(R.id.action_settingsFragment_to_authFragment)
        }
    }

    private fun setupLanguageButton() {
        binding.cardLanguageLinear.setOnClickListener {
            showLanguageDialog()
        }
    }

    private fun showLanguageDialog() {
        val languages = arrayOf("Русский", "English")
        val selectedLanguage = sharedPreferences.getString("language", "Русский")
        val selectedIndex = languages.indexOf(selectedLanguage)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Выберите язык")
        builder.setSingleChoiceItems(languages, selectedIndex) { dialog, which ->
            val chosenLanguage = languages[which]
            sharedPreferences.edit().putString("language", chosenLanguage).apply()
            updateLanguageDisplay()
            dialog.dismiss()
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun setLocale(language: String) {
        val locale = when (language) {
            "Русский" -> "ru"
            "English" -> "en"
            else -> "ru"
        }
        val config = resources.configuration
        config.setLocale(Locale(locale))
        val updatedContext = requireContext().createConfigurationContext(config)
        resources.updateConfiguration(config, updatedContext.resources.displayMetrics)
//        requireActivity().recreate() // Перезапуск активности для применения изменений (мигает экран)
//        findNavController().navigate(R.id.action_settingsFragment_self) Не работает...
        // Обновляем UI вручную
        binding.languageDisplay.text = language
        binding.tvTitle.text = updatedContext.getString(R.string.settings)
        binding.tvProfileSection.text = updatedContext.getString(R.string.profile)
        binding.tvEmail.text = updatedContext.getString(R.string.email)
        binding.tvGoogle.text = updatedContext.getString(R.string.google)
        binding.tvConnected.text = updatedContext.getString(R.string.connected)
        binding.tvDataManagement.text = updatedContext.getString(R.string.data_management)
        binding.tvAppSection.text = updatedContext.getString(R.string.application)
        binding.tvLanguage.text = updatedContext.getString(R.string.language)
        binding.tvAppearance.text = updatedContext.getString(R.string.appearance)
        binding.tvSystemTheme.text = updatedContext.getString(R.string.system_theme)
        binding.tvAboutSection.text = updatedContext.getString(R.string.about_app)
        binding.tvCheckUpdates.text = updatedContext.getString(R.string.check_updates)
        binding.tvVersion.text = updatedContext.getString(R.string.version)
        binding.tvTerms.text = updatedContext.getString(R.string.terms_of_service)
        binding.tvContactUs.text = updatedContext.getString(R.string.contact_us)
        binding.tvLogout.text = updatedContext.getString(R.string.logout)
        binding.tvFooter.text = updatedContext.getString(R.string.footer_text)
    }

    private fun updateLanguageDisplay() {
        val currentLanguage = sharedPreferences.getString("language", "Русский")
        binding.languageDisplay.text = currentLanguage
        currentLanguage?.let { setLocale(it) }
    }

    private fun bannerAdsSettings() {
        BannerAds.initializeBanner(binding.bannerSettings, requireContext())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.let {
            it.bannerSettings.destroy()
        }
        _binding = null
    }
}