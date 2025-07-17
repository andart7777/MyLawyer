package com.example.mylawyer.ui.auth

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mylawyer.data.api.RetrofitInstance
import com.example.mylawyer.utils.UserIdManager
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val _authState = MutableLiveData<FirebaseUser?>()
    val authState: LiveData<FirebaseUser?> get() = _authState
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error
    private val auth: FirebaseAuth = Firebase.auth

    init {
        _authState.value = auth.currentUser
    }

    fun signInWithGoogle(activity: Activity, credentialManager: CredentialManager, context: Context) {
        viewModelScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setServerClientId("982601782418-5l7o5vg4alj5r0s7j0m3d1h1id5fkra1.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(activity, request)
                val credential = result.credential
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken, context)
            } catch (e: GetCredentialException) {
                _error.postValue("Google Sign-In failed: ${e.message}")
            } catch (e: Exception) {
                _error.postValue("Unexpected error: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String, context: Context) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.postValue(auth.currentUser)
                    updateUserData(context) // Вызываем updateUserData после успешной авторизации
                } else {
                    _error.postValue("Firebase Auth failed: ${task.exception?.message}")
                }
            }
    }

private fun updateUserData(context: Context) {
    viewModelScope.launch {
        val user = Firebase.auth.currentUser
        if (user != null) {
            val userId = user.uid
            val tokenResult = user.getIdToken(false).await()
            val idToken = tokenResult.token
            if (idToken != null) {
                Log.d("AuthViewModel", "Сохранение userId: $userId, idToken: $idToken")
                UserIdManager.saveUserId(context, userId)
                UserIdManager.saveIdToken(context, idToken)
                RetrofitInstance.setToken(idToken)
            } else {
                Log.e("AuthViewModel", "Не удалось получить токен")
                _error.postValue("Не удалось получить токен")
            }
        } else {
            Log.e("AuthViewModel", "Пользователь не авторизован")
            _error.postValue("Пользователь не авторизован")
        }
    }
}
}