package com.example.harryerayaudiorecorder.ui

import AudioViewModel
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.harryerayaudiorecorder.R
import com.linc.audiowaveform.AudioWaveform
import kotlinx.coroutines.delay
import linc.com.amplituda.Amplituda
import linc.com.amplituda.AmplitudaResult
import linc.com.amplituda.exceptions.AmplitudaException
import linc.com.amplituda.exceptions.io.AmplitudaIOException
import java.io.File


@Composable
fun EditRecordingScreen(
    audioViewModel: AudioViewModel,
    durationSample: Int,
    fileName: String,
    fileSize: Double,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isPlaying = remember { mutableStateOf(false) }
    val audioCapturesDirectory = File(context.getExternalFilesDir(null), "/AudioCaptures")
    val audioFile = File(audioCapturesDirectory.absolutePath, fileName)
    val scope = rememberCoroutineScope()
    var waveformProgress by remember { mutableStateOf(0F) }
    var amplitudesData: List<Int> = listOf()
    var currentPosition by remember { mutableStateOf(0) }
    val startPosition = remember { mutableStateOf(0.0f) }
    val endPosition = remember { mutableStateOf(1.0f) }
    var sliderPosition by remember { mutableStateOf(0.0f..1.0f) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val amplituda = Amplituda(context)
    var boxWidth by remember { mutableStateOf(0f) }


    // Process audio and handle result
    amplituda.processAudio(audioFile.path)[
        { result: AmplitudaResult<String?> ->
            amplitudesData = result.amplitudesAsList()
        }, { exception: AmplitudaException? ->
            if (exception is AmplitudaIOException) {
                Log.e("AmplitudaException", "IO Exception!")
            }
        }
    ]

    LaunchedEffect(isPlaying.value) {
        while (isPlaying.value) {
            val maxPosition = ((endPosition.value * durationSample)).toInt()
            if(currentPosition >= maxPosition){
                val newCurrent = startPosition.value * durationSample
                audioViewModel.playAudio(
                    audioFile,
                    newCurrent.toLong()
                )
            }

            currentPosition = audioViewModel.getCurrentPosition()
            waveformProgress = (audioViewModel.getCurrentPosition() / durationSample.toFloat())
            delay(20)
        }
    }
    if(isLandscape){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(
                            PaddingValues(
                                start = 16.dp,
                                top = 8.dp,
                                end = 8.dp,
                                bottom = 8.dp
                            )
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            EvenlySpacedText2(text = formatTime(currentPosition))
                        }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxSize()
                                .onGloballyPositioned { coordinates: LayoutCoordinates ->
                                    boxWidth = coordinates.size.width.toFloat()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            AudioWaveform(
                                amplitudes = amplitudesData,
                                progress = waveformProgress,
                                progressBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                                waveformBrush = SolidColor(MaterialTheme.colorScheme.primary),
                                onProgressChange = { newProgress ->
                                    waveformProgress = newProgress
                                    val newPosition = (newProgress * audioViewModel.getAudioDuration(audioFile)).toLong()
                                    val minPosition = (startPosition.value * durationSample).toLong()
                                    val maxPosition = ((endPosition.value * durationSample) - 10).toLong()
                                    //audioViewModel.seekTo(newPosition.coerceIn(minPosition, maxPosition))
                                    audioViewModel.playAudio(audioFile, newPosition.coerceIn(minPosition, maxPosition))
                                    Log.d(
                                        "playbackscreen",
                                        audioViewModel.getCurrentPosition().toString()
                                    )
                                    isPlaying.value = true
                                }
                            )
                            DrawVerticalLines(
                                modifier = Modifier.fillMaxSize(),
                                startPos = startPosition,
                                endPos = endPosition,
                                boxWidth = boxWidth
                            )
                        }
                    }

                }
                Box(
                    modifier = Modifier
                        .weight(3f)
                        .fillMaxHeight()
                        .padding(
                            PaddingValues(
                                start = 8.dp,
                                top = 8.dp,
                                end = 16.dp,
                                bottom = 8.dp
                            )
                        )
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxHeight()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Set Trim Points",
                                //modifier = Modifier.padding(8.dp)
                            )
                            RangeSlider(
                                value = sliderPosition,
                                onValueChange = { range -> sliderPosition = range
                                    startPosition.value = sliderPosition.start
                                    endPosition.value = sliderPosition.endInclusive
                                },
                                valueRange = 0.0f..1.0f,
                                onValueChangeFinished = {

                                },
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Button(onClick = {
                                    if (isPlaying.value) {
                                        audioViewModel.pauseAudio()
                                        isPlaying.value = false
                                    } else {
                                        if (audioFile.exists()) {

                                            if(audioViewModel.getCurrentPosition() < startPosition.value * durationSample)
                                            {
                                                val newCurrent = startPosition.value * durationSample
                                                audioViewModel.playAudio(
                                                    audioFile,
                                                    newCurrent.toLong()
                                                )
                                            }
                                            else {
                                                audioViewModel.playAudio(
                                                    audioFile,
                                                    audioViewModel.getCurrentPosition().toLong()
                                                )
                                            }
                                            isPlaying.value = true
                                        }
                                    }
                                }) {
                                    Icon(
                                        painter = painterResource(id = if (isPlaying.value) R.drawable.ic_pause2 else R.drawable.round_play_circle),
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp),
                                        contentDescription = if (isPlaying.value) "Pause" else "Play",
                                    )
                                }

                                Button(onClick = {
                                    val startMillis = (startPosition.value * durationSample).toInt()
                                    val endMillis = (endPosition.value * durationSample).toInt()
                                    audioViewModel.trimAudio(audioFile, startMillis, endMillis) { trimmedFile ->
                                    }
                                },
                                ) {
                                    Text("Trim")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    else
    {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(3f)
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.SpaceEvenly,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        EvenlySpacedText2(text = formatTime(currentPosition))
                    }
                    Box(
                        modifier = Modifier.weight(1f)
                            .onGloballyPositioned { coordinates: LayoutCoordinates ->
                            boxWidth = coordinates.size.width.toFloat()
                        },
                        contentAlignment = Alignment.Center
                    ) {
                        AudioWaveform(
                            amplitudes = amplitudesData,
                            progress = waveformProgress,
                            progressBrush = SolidColor(MaterialTheme.colorScheme.onPrimary),
                            waveformBrush = SolidColor(MaterialTheme.colorScheme.primary),
                            onProgressChange = { newProgress ->
                                waveformProgress = newProgress
                                val newPosition = (newProgress * audioViewModel.getAudioDuration(audioFile)).toLong()
                                val minPosition = (startPosition.value * durationSample).toLong()
                                val maxPosition = ((endPosition.value * durationSample) - 10).toLong()
                                //audioViewModel.seekTo(newPosition.coerceIn(minPosition, maxPosition))
                                audioViewModel.playAudio(audioFile, newPosition.coerceIn(minPosition, maxPosition))
                                isPlaying.value = true
                            }
                        )
                        DrawVerticalLines(
                            modifier = Modifier.fillMaxSize(),
                            startPos = startPosition,
                            endPos = endPosition,
                            boxWidth = boxWidth
                        )
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .weight(2f)
                    .fillMaxWidth()
                    .padding(16.dp, 8.dp, 16.dp, 8.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Set Trim Points", modifier = Modifier.padding(8.dp))
//                    Text(
//                        text = "Start Position: ${formatTime((startPosition.value * durationSample).toInt())}",
//                        modifier = Modifier.padding(8.dp)
//                    )
//                    Text(
//                        text = "End Position: ${formatTime((endPosition.value * durationSample).toInt())}",
//                        modifier = Modifier.padding(8.dp)
//                    )
                    RangeSlider(
                        value = sliderPosition,
                        onValueChange = { range -> sliderPosition = range
                                        startPosition.value = sliderPosition.start
                                        endPosition.value = sliderPosition.endInclusive
                        },
                        valueRange = 0.0f..1.0f,
                        onValueChangeFinished = {
                            //startPosition = sliderPosition.start
                            //endPosition = sliderPosition.endInclusive
                            // launch some business logic update with the state you hold
                            // viewModel.updateSelectedSliderValue(sliderPosition)
                        },
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Button(onClick = {
                            if (isPlaying.value) {
                                audioViewModel.pauseAudio()
                                isPlaying.value = false
                            } else {
                                if (audioFile.exists()) {

                                    if(audioViewModel.getCurrentPosition() < startPosition.value * durationSample)
                                    {
                                        val newCurrent = startPosition.value * durationSample
                                        audioViewModel.playAudio(
                                            audioFile,
                                            newCurrent.toLong()
                                        )
                                    }
                                    else {
                                        audioViewModel.playAudio(
                                            audioFile,
                                            audioViewModel.getCurrentPosition().toLong()
                                        )
                                    }
                                    isPlaying.value = true
                                }
                            }
                        }) {
                            Icon(
                                painter = painterResource(id = if (isPlaying.value) R.drawable.ic_pause2 else R.drawable.round_play_circle),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp),
                                contentDescription = if (isPlaying.value) "Pause" else "Play",
                            )
                        }

                        Button(onClick = {
                            val startMillis = (startPosition.value * durationSample).toInt()
                            val endMillis = (endPosition.value * durationSample).toInt()
                            audioViewModel.trimAudio(audioFile, startMillis, endMillis) { trimmedFile ->
                                // Handle the trimmed file, e.g., play it, save it, etc.
                            }
                        }) {
                            Text("Trim")
                        }
                    }
                }
                }
            }
        }

}

@Composable
fun DrawVerticalLines(
    modifier: Modifier = Modifier,
    boxWidth: Float,
    startPos: MutableState<Float>,
    endPos: MutableState<Float>,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    lineWidth: Float = 5f,
    cornerRadius: Float = 16f
) {
    Canvas(modifier = modifier) {
        val halfHeight = size.height / 2

        drawRoundRect(
            color = lineColor,
            topLeft = androidx.compose.ui.geometry.Offset(x = 0f, y = halfHeight / 2),
            size = androidx.compose.ui.geometry.Size(width = startPos.value * boxWidth, height = halfHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )

//        drawLine(
//            color = lineColor,
//            start = androidx.compose.ui.geometry.Offset(x = startPos.value *  boxWidth, y = halfHeight / 2),
//            end = androidx.compose.ui.geometry.Offset(x = startPos.value * boxWidth, y = halfHeight * 1.5f),
//            strokeWidth = lineWidth
//        )
        drawRoundRect(
            color = lineColor,
            topLeft = androidx.compose.ui.geometry.Offset(x = endPos.value * boxWidth, y = halfHeight/2),
            size = androidx.compose.ui.geometry.Size(width = (1 - endPos.value) * boxWidth, height = halfHeight),
            cornerRadius = CornerRadius(cornerRadius, cornerRadius)
        )
//        drawLine(
//            color = lineColor,
//            start = androidx.compose.ui.geometry.Offset(x = endPos.value *  boxWidth, y = halfHeight / 2),
//            end = androidx.compose.ui.geometry.Offset(x = endPos.value * boxWidth, y = halfHeight * 1.5f),
//            strokeWidth = lineWidth
//        )
    }
}











