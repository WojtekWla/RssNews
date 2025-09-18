package com.example.rssnews.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class LoginViewModel: ViewModel() {
    private val auth = Firebase.auth
    suspend fun authenticate(email: String, password: String): Boolean{
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}