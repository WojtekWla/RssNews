package com.example.rssnews

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.rssnews.model.News
import com.example.rssnews.ui.theme.RssNewsTheme
import com.example.rssnews.view.FavoriteScreen
import com.example.rssnews.view.ListOfNews
import com.example.rssnews.view.LoginScreen
import com.example.rssnews.view.PreviewScreen
import com.example.rssnews.view.RegisterScreen
import com.example.rssnews.viewModel.FavoriteViewModel
import com.example.rssnews.viewModel.LoginViewModel
import com.example.rssnews.viewModel.NewsViewModel
import com.example.rssnews.viewModel.PreviewViewModel
import com.example.rssnews.viewModel.RegisterViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database
import com.prof18.rssparser.RssParserBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import kotlin.coroutines.resumeWithException

class MainActivity : ComponentActivity() {

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RssNewsTheme {

                LaunchedEffect(Unit) {
                    parseData()
                }

                val navController = rememberNavController()
                NavHost(
                    navController,
                    Destinations.authentication
                ) {
                    composable (Destinations.authentication) {
                        val vm: LoginViewModel = viewModel()
                        LoginScreen(
                            vm,
                            { navController.navigate(Destinations.allNews) },
                            { navController.navigate(Destinations.register) }
                        )

                    }

                    composable (Destinations.register) {
                        val vm: RegisterViewModel = viewModel()
                        RegisterScreen(
                            vm,
                            { navController.navigate(Destinations.allNews) },
                            { navController.navigate(Destinations.authentication) }
                        )

                    }

                    composable (Destinations.allNews) {
                        val vm: NewsViewModel = viewModel()

                        LaunchedEffect(Unit) {
                            try {
                                vm.loadData()
                            } catch (e: Exception) {
                                println("Failed to load user data: $e")
                            }
                        }

                        ListOfNews(
                            vm,
                            { navController.navigate(Destinations.getRoutePreview(it)) },
                            { navController.navigate(Destinations.favoriteNews) },
                        )
                    }

                    composable (Destinations.favoriteNews) {
                        val vm: FavoriteViewModel = viewModel()
                        LaunchedEffect(Unit) {
                            try {
                                vm.loadData()
                            } catch (e: Exception) {
                                println("Failed to load user data: $e")
                            }
                        }

                        FavoriteScreen (
                            vm,
                            { navController.navigate(Destinations.getRoutePreview(it)) },
                            { navController.popBackStack() },
                        )
                    }

                    composable(
                        Destinations.newsPreview,
                        arguments = listOf(
                            navArgument (Destinations.argId) {
                                type = NavType.StringType
                            }
                        )
                    ) { navBackStackEntry ->
                        val id = navBackStackEntry.arguments?.getString(Destinations.argId) ?: ""
                        val vm: PreviewViewModel = viewModel()

                        LaunchedEffect(id) {
                            try {
                                vm.loadData(id)
                            } catch (e: Exception) {
                                println("Failed to load user data: $e")
                            }
                        }

                        val film = vm
                        if (film.state != null) {
                            PreviewScreen(
                                vm
                            ) { navController.popBackStack() }
                        }
                    }
                }
            }
        }
    }

    private suspend fun parseData() {

        val existingNews = fetchAllNews()

        val l : List<News> = getChannel()?.items?.map {
            News(it.title.orEmpty(), it.image.orEmpty(), it.description.orEmpty().substringAfter('>'),
                it.link.orEmpty()
            )
        }?.toList() ?: emptyList()

        var newElements = checkNewsElements(l, existingNews)

        if(!newElements) {
            Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("news")
                .removeValue()

            l.forEach { newsItem ->
                Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("news")
                    .push()
                    .setValue(newsItem)
                    .addOnCompleteListener { println("Complete saving ${it.exception}") }
                    .addOnSuccessListener { println("News saved") }
                    .addOnFailureListener { println("Failure saving $it") }
            }
        }
    }

    private fun checkNewsElements(newList: List<News>, oldList: List<News>): Boolean {
        newList.forEach { element ->
            if(oldList.any { element.title == it.title }) {
                return true
            }
        }
        return false
    }

    suspend fun fetchAllNews(): List<News> = suspendCancellableCoroutine { cont ->
        val newsRef = Firebase.database("https://rssnews-78eb7-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("news")

        newsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val result = snapshot.children.mapNotNull { it.getValue(News::class.java) }
                cont.resume(result, null)
            }

            override fun onCancelled(error: DatabaseError) {
                cont.resumeWithException(error.toException())
            }
        })
    }

    private suspend fun getChannel() = withContext(Dispatchers.IO) {
        try {
            val builder = RssParserBuilder(
                callFactory = OkHttpClient(),
                charset = Charsets.UTF_8
            )
            val rssParser = builder.build()
            val rssChannel =
                rssParser.getRssChannel("https://wiadomosci.gazeta.pl/pub/rss/wiadomosci_kraj.xml")
            rssChannel
        }catch(e: Exception) {
            null
        }
    }
}

object Destinations {
    val argId = "id"

    val authentication = "authenticate"
    val register = "register"
    val allNews = "allNews"
    val favoriteNews = "favoriteNews"
    val newsPreview = "news/{${argId}}"

    fun getRoutePreview(id: String): String {
        return newsPreview.replace("{$argId}", id.toString())
    }
}
