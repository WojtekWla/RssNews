package com.example.rssnews.viewModel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.rssnews.model.News
import com.example.rssnews.model.NewsHolder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

val FAVORITE_ARTICLE = "favorite_articles"

class PreviewViewModel : ViewModel() {
    var state by mutableStateOf<NewsHolder?>(null)
    private var favoriteList: List<String> = mutableListOf()

    private var newsDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("news")
    private var userDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")
    suspend fun loadData(id: String) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val favoriteRef = userDb.child(uid).child(FAVORITE_ARTICLE)

        favoriteList = readListFromFirebase(favoriteRef)

        newsDb.child(id).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val news = snapshot.getValue(News::class.java)
                if (news != null) {
                    println("Loaded news for ID: $id")
                    state = NewsHolder(id = id, seen = false, favorite = isFavorite(id),  news = news)
                    updateFavorite(isFavorite(id))
                } else {
                    println("No news found for ID: $id")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        addToSeen()
    }

    private fun isFavorite(articleId: String): Boolean {
        return favoriteList.contains(articleId)
    }

    fun addToFavorite() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val favoriteRef = userDb.child(uid).child(FAVORITE_ARTICLE)

        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                if (!currentList.contains(state?.id)) {
                    val updatedList = currentList + state?.id
                    updateFavorite(true)
                    favoriteRef.setValue(updatedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error updating $FAVORITE_ARTICLE: $error")
            }
        })
    }

    fun removeFromFavorite() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val favoriteRef = userDb.child(uid).child(FAVORITE_ARTICLE)

        favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                if (currentList.contains(state?.id)) {
                    val updatedList = currentList - state?.id
                    updateFavorite(false)
                    favoriteRef.setValue(updatedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error updating $FAVORITE_ARTICLE: $error")
            }
        })
    }

    private fun addToSeen() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val seenRef = userDb.child(uid).child(SEEN_ARTICLE)

        seenRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val currentList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()

                if (!currentList.contains(state?.id)) {
                    val updatedList = currentList + state?.id
                    updateFavorite(true)
                    seenRef.setValue(updatedList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                println("Error updating $SEEN_ARTICLE: $error")
            }
        })
    }


    private fun updateFavorite(favorite: Boolean) {
        state = state?.copy(
            favorite = favorite
        )
    }
}