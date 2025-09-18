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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rssnews.model.News
import coil.compose.AsyncImage
import com.example.rssnews.R
import com.example.rssnews.model.NewsHolder
import com.example.rssnews.viewModel.NewsViewModel


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListOfNews(
    newsViewModel: NewsViewModel,
    navigateToPreview: (String) -> Unit,
    navigateToFavorite: () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = stringResource(R.string.newsMain),
                            color = Color.Black
                        )
                    }
                },
                actions = {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable(onClick = navigateToFavorite),
                        imageVector = Icons.Filled.Favorite,
                        contentDescription = stringResource(R.string.navigation)
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
        val news = newsViewModel.state.collectAsStateWithLifecycle()
        println("NEWS_LEN ${news.value.size}")
        Column (
            modifier = Modifier
                .padding(innerPaddings)
                .padding(horizontal = 8.dp)
                .padding(bottom = systemPaddings.calculateBottomPadding())
                .fillMaxSize()

        ) {

            LaunchedEffect(Unit) {
                newsViewModel.loadData()
                newsViewModel.addListener()
            }

            DisposableEffect(Unit) {
                onDispose {
                    newsViewModel.removeListener()
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


@Composable
fun NewsElement(element: NewsHolder, onClicked: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .clickable(onClick = onClicked)
            .alpha(if (element.seen) 0.5f else 1f),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = if(element.news.imageUri.isNotBlank()) element.news.imageUri.toUri() else R.drawable.img,
                contentDescription = stringResource(R.string.news_image),
                modifier = Modifier
                    .size(150.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = element.news.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = element.news.description,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
