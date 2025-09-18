package com.example.rssnews.viewModel

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.rssnews.model.News
import com.example.rssnews.model.NewsHolder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.flow.MutableStateFlow

class NewsViewModel : ViewModel() {
    var state = MutableStateFlow(mutableListOf<NewsHolder>())
    private var favoriteList: List<String> = mutableListOf()
    private var seenList: List<String> = mutableListOf()

    private var newsDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("news")
    private var userDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")

    suspend fun loadData() {
        val uid = Firebase.auth.currentUser?.uid ?: return
        val favoriteRef = userDb.child(uid).child(FAVORITE_ARTICLE)
        val seenRef = userDb.child(uid).child(SEEN_ARTICLE)

        favoriteList = readListFromFirebase(favoriteRef)
        seenList = readListFromFirebase(seenRef)
    }

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            snapshot.children.mapNotNull {
                val news = it.getValue(News::class.java)
                val key = it.key
                if(key != null && news != null) {
                    key to news
                }else {
                    null
                }
            }.let {
                println("Fetched ${it.size} elements")
                mapData(it)
            }
        }
        override fun onCancelled(error: DatabaseError) {
            println("Error loading db $error")
        }
    }

    private fun mapData(pairs: List<Pair<String, News>>) {
        println("Map data to domain")
        state.value.clear()
        val holder = pairs.map {
            println("Element $it")
            NewsHolder(it.first, isSeen(it.first), isFavorite(it.first), it.second)
        }
        println("State ${state.value.size}")

        state.value = holder as MutableList<NewsHolder>
    }

    fun addListener() {
        newsDb.addValueEventListener(listener)
    }


    fun removeListener() {
        newsDb.removeEventListener(listener)
    }

    private fun isFavorite(articleId: String): Boolean {
        return favoriteList.contains(articleId)
    }

    private fun isSeen(articleId: String): Boolean {
        return seenList.contains(articleId)
    }
}