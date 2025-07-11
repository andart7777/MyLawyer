package com.example.mylawyer.ui.settings

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
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
import android.content.IntentSender
import android.content.SharedPreferences
import android.widget.Toast
import androidx.fragment.app.viewModels
import com.example.mylawyer.ui.chatbot.ChatViewModel
import com.example.mylawyer.viewmodel.ChatViewModelFactory
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val viewModel: ChatViewModel by viewModels {
        ChatViewModelFactory(requireContext())
    }
    private lateinit var appUpdateManager: AppUpdateManager
    private val REQUEST_CODE_UPDATE = 999

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireContext().getSharedPreferences("MyLawyerPrefs", Context.MODE_PRIVATE)
        // Применяем сохраненную тему в onCreateView
        val currentTheme = sharedPreferences.getString("theme", "system") ?: "system"
        applyTheme(currentTheme)
        // Инициализация AppUpdateManager
        appUpdateManager = AppUpdateManagerFactory.create(requireContext())
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
        setupAppearanceButton() // Новый метод для настройки кнопки выбора темы
        setupCheckUpdatesButton()
        setupTermsButton()
        bannerAdsSettings()
        updateLanguageDisplay()
        updateAppearanceDisplay() // Новый метод для отображения текущей темы
    }

    private fun setupCheckUpdatesButton() {
        binding.cardCheckUpdates.setOnClickListener {
            Log.d("SettingsFragment", "Проверка обновлений через Google Play")
            checkForInAppUpdates()
        }
    }

    private fun checkForInAppUpdates() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Немедленное обновление доступно
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        requireActivity(),
                        REQUEST_CODE_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("SettingsFragment", "Ошибка запуска обновления: ${e.message}", e)
                    Toast.makeText(
                        requireContext(),
                        getString(R.string.update_error),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_NOT_AVAILABLE) {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.no_updates_available),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.update_check_failed),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }.addOnFailureListener { exception ->
            Log.e("SettingsFragment", "Ошибка проверки обновлений: ${exception.message}", exception)
            Toast.makeText(
                requireContext(),
                "${getString(R.string.update_check_error)}: ${exception.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onResume() {
        super.onResume()
        // Проверяем, возобновляется ли немедленное обновление
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                try {
                    appUpdateManager.startUpdateFlowForResult(
                        appUpdateInfo,
                        AppUpdateType.IMMEDIATE,
                        requireActivity(),
                        REQUEST_CODE_UPDATE
                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e("SettingsFragment", "Ошибка возобновления обновления: ${e.message}", e)
                }
            }
        }
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

    private fun setupAppearanceButton() {
        binding.cardAppearanceLinear.setOnClickListener {
            showAppearanceDialog()
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

    private fun showAppearanceDialog() {
        val themes = arrayOf(
            getString(R.string.system_theme),
            getString(R.string.light_theme),
            getString(R.string.dark_theme)
        )
        val themeValues = arrayOf("system", "light", "dark")
        val currentTheme = sharedPreferences.getString("theme", "system") ?: "system"
        val selectedIndex = themeValues.indexOf(currentTheme)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.appearance))
        builder.setSingleChoiceItems(themes, selectedIndex) { dialog, which ->
            val selectedTheme = themeValues[which]
            sharedPreferences.edit().putString("theme", selectedTheme).apply()
            applyTheme(selectedTheme)
            updateAppearanceDisplay()
            dialog.dismiss()
        }
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun applyTheme(theme: String) {
        when (theme) {
            "system" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            "light" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            "dark" -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        }
    }

    private fun updateAppearanceDisplay() {
        val currentTheme = sharedPreferences.getString("theme", "system") ?: "system"
        val displayText = when (currentTheme) {
            "system" -> getString(R.string.system_theme)
            "light" -> getString(R.string.light_theme)
            "dark" -> getString(R.string.dark_theme)
            else -> getString(R.string.system_theme)
        }
        binding.tvSystemTheme.text = displayText
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

    private fun setupTermsButton() {
        binding.cardTermsLinear.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_termsFragment)
        }
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