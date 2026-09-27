package com.anidesk.tv.player

data class Video(
    val url: String,
    val seek: Long,
    val title: String,
    val subtitle: String,
    val headers: Map<String, String>,
)