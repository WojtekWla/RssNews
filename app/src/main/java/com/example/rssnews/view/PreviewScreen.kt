package com.example.rssnews.view

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rssnews.R
import com.example.rssnews.viewModel.PreviewViewModel


@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreviewScreen(
    previewViewModel: PreviewViewModel,
    onNavigationUp: () -> Unit
) {
    Scaffold (
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row (verticalAlignment = Alignment.CenterVertically){
                        Text(
                            text = stringResource(R.string.newsDetails),
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
                actions = {
                    Icon(
                        modifier = Modifier
                            .padding(12.dp)
                            .clickable(onClick = {
                                if(previewViewModel.state?.favorite == false) {
                                    previewViewModel.addToFavorite()
                                }else {
                                    previewViewModel.removeFromFavorite()
                                }
                            } ),
                        imageVector = if(previewViewModel.state?.favorite == false) Icons.Rounded.FavoriteBorder else Icons.Filled.Favorite,
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
        val scroll = rememberScrollState()

        val context = LocalContext.current
        val onBackPressDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
        val webView = remember {
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams (
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                settings.javaScriptEnabled = true
                webViewClient = WebViewClient()

                loadUrl(previewViewModel.state?.news?.link.toString())
            }
        }

        DisposableEffect(webView) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(webView.canGoBack()) {
                        webView.goBack()
                    }
                }
            }

            onBackPressDispatcher?.addCallback(callback)
            onDispose {
                callback.remove()
            }
        }


        AndroidView(
            factory = {webView},
            modifier = Modifier.fillMaxSize()
                .padding(innerPaddings)
                .padding(bottom = systemPaddings.calculateBottomPadding())
        )

    }
}
