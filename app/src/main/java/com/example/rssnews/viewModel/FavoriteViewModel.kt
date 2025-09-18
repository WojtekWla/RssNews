package com.example.rssnews.viewModel

import androidx.lifecycle.ViewModel
import com.example.rssnews.model.News
import com.example.rssnews.model.NewsHolder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

val SEEN_ARTICLE = "seen_articles"

class FavoriteViewModel : ViewModel() {
    var state = MutableStateFlow(mutableListOf<NewsHolder>())
    private var favoriteList: List<String> = mutableListOf()
    private var seenList: List<String> = mutableListOf()

    private var newsDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("news")
    private var userDb = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/").getReference("users")

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val uid = Firebase.auth.currentUser?.uid ?: return
            val favoriteRef = userDb.child(uid).child(FAVORITE_ARTICLE)
            val seenRef = userDb.child(uid).child(SEEN_ARTICLE)

            favoriteRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    favoriteList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error updating $FAVORITE_ARTICLE: $error")
                }
            })

            seenRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    seenList = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                }

                override fun onCancelled(error: DatabaseError) {
                    println("Error updating $FAVORITE_ARTICLE: $error")
                }
            })

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

    suspend fun loadData() {
        val uid = Firebase.auth.currentUser?.uid ?: return

        favoriteList = readListFromFirebase(userDb.child(uid).child(FAVORITE_ARTICLE))
        seenList = readListFromFirebase(userDb.child(uid).child(SEEN_ARTICLE))

        addListener()
    }

    private fun isFavorite(articleId: String): Boolean {
        return favoriteList.contains(articleId)
    }

    private fun isSeen(articleId: String): Boolean {
        return seenList.contains(articleId)
    }

    private fun mapData(pairs: List<Pair<String, News>>) {
        println("Map data to domain")
        state.value.clear()
        val holder = pairs.map {
            println("Element $it")
            NewsHolder(it.first, false, isFavorite(it.first), it.second)
        }
        println("State ${state.value.size}")

        state.value = holder.filter { it.favorite } as MutableList<NewsHolder>
    }

    fun addListener() {
        newsDb.addValueEventListener(listener)
    }


    fun removeListener() {
        newsDb.removeEventListener(listener)
    }

}

@OptIn(ExperimentalCoroutinesApi::class)
suspend fun readListFromFirebase(ref: DatabaseReference): List<String> =
    suspendCancellableCoroutine { cont ->
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = snapshot.getValue(object : GenericTypeIndicator<List<String>>() {}) ?: emptyList()
                cont.resume(list, null)
            }

            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        })
    }