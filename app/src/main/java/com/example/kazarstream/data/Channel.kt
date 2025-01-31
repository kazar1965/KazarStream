package com.example.kazarstream.data

import kotlinx.serialization.Serializable

@Serializable
data class Channel(
    val name: String,
    val logoUrl: String?,
    val streamUrl: String,
    val groupTitle: String?
)

@Serializable
data class Playlist(
    val name: String,
    val url: String,
    val lastUpdated: Long = System.currentTimeMillis(),
    val channels: List<Channel> = emptyList()
) 