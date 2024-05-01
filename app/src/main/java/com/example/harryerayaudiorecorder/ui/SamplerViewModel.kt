package com.example.harryerayaudiorecorder.ui

import androidx.lifecycle.ViewModel
import com.example.harryerayaudiorecorder.data.SamplerUiState
import com.example.harryerayaudiorecorder.data.SoundCard
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update


class SamplerViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SoundCard())
    val uiState: StateFlow<SoundCard> = _uiState.asStateFlow()

//    fun setSoundCardTitle(fileName: String){
//        _uiState.update { currentState -> currentState.copy(
//            title = fileName
//        ) }
//    }
    fun setSoundCard(soundCard: SoundCard){
        _uiState.update { currentState -> currentState.copy(
            title = soundCard.title,
            duration = soundCard.duration,
            filePath = soundCard.filePath,
            fileSize = soundCard.fileSize
        ) }
    }

    //TODO : Implement this functions (first calculate or save)
//    fun setDuration(someNumber: Double) {
//        _uiState.update { currentState ->
//            currentState.copy(
//                duration = someNumber,
//            )
//        }
//    }
//    fun setFileSize(someNumber: Double) {}
}
