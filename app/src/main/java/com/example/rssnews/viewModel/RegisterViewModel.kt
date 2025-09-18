package com.example.rssnews.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class RegisterViewModel : ViewModel() {
    var message by mutableStateOf("")
    private val auth = Firebase.auth

    suspend fun register(email: String, password: String): Boolean{
        return try {
            auth.createUserWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            message = e.message.toString()
            false
        }
    }
}