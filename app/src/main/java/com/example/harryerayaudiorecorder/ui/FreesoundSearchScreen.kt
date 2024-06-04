package com.example.harryerayaudiorecorder.ui

import AudioViewModel
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.harryerayaudiorecorder.data.FreesoundSoundCard
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

@Composable
fun FreesoundSearchScreen(audioViewModel: AudioViewModel,
                          windowSizeClass: WindowSizeClass) {
    var sounds = remember { mutableStateListOf<FreesoundSoundCard>() }

    // maybe put to bg?
    audioViewModel.performSearch(
        clientSecret = "DFYwiCdqrNbhB9RFGiENSXURVlF30uGFrGcLMFWy",
        "birds",
        setFreesoundSoundCards = { newSounds ->
            sounds.clear()
            sounds.addAll(newSounds)
        }
    )

    val fileNameFontSize = when {
        SamplerViewModel().isTablet() -> 32
        else -> 22
    }

    LazyColumn(modifier = Modifier.padding(top = (fileNameFontSize*2).dp)) {
        items(sounds) { item ->
            FsSoundCard(item, fileNameFontSize, audioViewModel)
        }
    }

}




@Composable
fun FsSoundCard(sound: FreesoundSoundCard, fileNameFontSize:Int, audioViewModel: AudioViewModel) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp, 8.dp, 16.dp, 8.dp)

    ) {
        Column(
            modifier = Modifier.padding((fileNameFontSize / 2.0).toInt().dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = sound.name,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = fileNameFontSize.sp,
                    lineHeight = fileNameFontSize.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    IconButton(onClick = { }) {
                        Icon(
                            Icons.Default.KeyboardArrowDown,
                            contentDescription = "Download",
                            modifier = Modifier.size((fileNameFontSize * 1.5).toInt().dp)
                        )
                    }

                }
            }

            Text(
                text = "Duration: ${audioViewModel.formatDuration((sound.duration*1000).toLong())} ",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = fileNameFontSize.sp
            )

            Text(
                text = "File Size:  ${String.format("%.2f", sound.filesize / 1_000_000.0)} MB",
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontSize = fileNameFontSize.sp
            )
        }
    }
}
