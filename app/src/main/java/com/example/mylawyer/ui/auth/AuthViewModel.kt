package com.example.mylawyer.ui.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _authState = MutableLiveData<FirebaseUser?>()
    val authState: LiveData<FirebaseUser?> get() = _authState
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error
    private val auth: FirebaseAuth = Firebase.auth

    init {
        _authState.value = auth.currentUser
    }

    fun signInWithGoogle(activity: Activity, credentialManager: CredentialManager) {
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
                firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
            } catch (e: GetCredentialException) {
                _error.postValue("Google Sign-In failed: ${e.message}")
            } catch (e: Exception) {
                _error.postValue("Unexpected error: ${e.message}")
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    _authState.postValue(auth.currentUser)
                } else {
                    _error.postValue("Firebase Auth failed: ${task.exception?.message}")
                }
            }
    }
}