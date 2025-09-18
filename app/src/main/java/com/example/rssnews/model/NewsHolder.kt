package com.example.rssnews.model

data class NewsHolder(
    var id: String = "",
    var seen: Boolean = false,
    var favorite: Boolean = false,
    var news: News = News(),
)
