package com.example.harryerayaudiorecorder.data

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

data class FreesoundSoundCard(
    val id: Int,
    val name: String,
    val tags: List<String>,
    val description: String,
    val created: String,
    val license: String,
    val channels: Int,
    val filesize: Int,
    val bitrate: Int,
    val bitdepth: Int,
    val duration: Float,
    val sampleRate: Int,
    val username: String,
    val download: String,
    val previews: Map<String, String>,
    val avgRating: Double,
    val isPlaying: MutableState<Boolean> = mutableStateOf(false)

)