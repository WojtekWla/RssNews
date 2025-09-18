package com.example.rssnews.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.rssnews.R
import com.example.rssnews.viewModel.FavoriteViewModel
import com.example.rssnews.viewModel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoriteScreen(
    favoriteViewModel: FavoriteViewModel,
    navigateToPreview: (String) -> Unit,
    onNavigationUp: () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = stringResource(R.string.favoriteNews),
                            color = Color.Black
                        )
                    }
                },
                navigationIcon = {
                    Icon(
                        modifier = Modifier
                            .clickable(onClick = onNavigationUp)
                            .padding(12.dp),
                        imageVector = Icons.Default.Home,
                        contentDescription = stringResource(R.string.go_back_home)
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFF0D665),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black,
                    actionIconContentColor = Color.Black
                )
            )
        }
    ){ innerPaddings ->
        val systemPaddings = WindowInsets.systemBars.asPaddingValues()
        val news = favoriteViewModel.state.collectAsStateWithLifecycle()
        println("NEWS_LEN ${news.value.size}")
        Column (
            modifier = Modifier
                .padding(innerPaddings)
                .padding(horizontal = 8.dp)
                .padding(bottom = systemPaddings.calculateBottomPadding())
                .fillMaxSize()

        ) {

            DisposableEffect(Unit) {
                favoriteViewModel.addListener()

                onDispose {
                    favoriteViewModel.removeListener()
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = innerPaddings,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(news.value, key = { it.id }) {
                    NewsElement(it) { navigateToPreview(it.id) }
                }
            }
        }
    }
}