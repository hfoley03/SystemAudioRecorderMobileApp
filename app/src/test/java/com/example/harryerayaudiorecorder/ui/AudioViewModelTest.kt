
import android.media.MediaPlayer
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.example.harryerayaudiorecorder.RecorderControl
import com.example.harryerayaudiorecorder.data.AudioRecordDatabase
import com.example.harryerayaudiorecorder.data.MyAudioRepository
import com.example.harryerayaudiorecorder.data.SoundCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class AudioViewModelTest {

    @Mock
    private lateinit var mediaPlayerWrapper: MediaPlayerWrapper

    @Mock
    private lateinit var recorderControl: RecorderControl

    @Mock
    private lateinit var db: AudioRecordDatabase

    private lateinit var audioViewModel: AudioViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val audioCapturesDirectory = File("/harryerayaudiorecorder/resources/")

    @Mock
    private lateinit var audioRepository : MyAudioRepository

    private lateinit var closeable: AutoCloseable

    @Before
    fun setUp() {
        closeable = MockitoAnnotations.openMocks(this)
        Dispatchers.setMain(testDispatcher)
        audioRepository = MyAudioRepository(db, audioCapturesDirectory)
        audioViewModel = AudioViewModel(mediaPlayerWrapper, recorderControl, audioRepository)
    }

    @After
    fun tearDown() {
        closeable.close()
        Dispatchers.resetMain()
       // testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun testPlayAudio() {
        val testFile = File("/harryerayaudiorecorder/resources/rooster.mp3")
        audioViewModel.playAudio(testFile)
        verify(mediaPlayerWrapper).setDataSource("/harryerayaudiorecorder/resources/rooster.mp3")
        verify(mediaPlayerWrapper).prepare()
        verify(mediaPlayerWrapper).start()
    }

    @Test
    fun testStopAudio() {
        `when`(mediaPlayerWrapper.isPlaying()).thenReturn(true)
        audioViewModel.stopAudio()
        verify(mediaPlayerWrapper).stop()
        verify(mediaPlayerWrapper).onCleared()
        assert(!audioViewModel.recorderRunning.value)
    }

    @Test
    fun testStartRecording() {
        audioViewModel.startRecording()
        verify(recorderControl).startRecorder()
        assert(audioViewModel.recorderRunning.value)
    }

    @Test
    fun testStopRecording() {
        val defaultFileName = "rooster"
        audioViewModel.stopRecording(defaultFileName)
        verify(recorderControl).stopRecorder()
        assert(!audioViewModel.recorderRunning.value)
    }

    @Test
    fun testStopWithoutSavingRecording() {
        audioViewModel.startRecording()
        audioViewModel.stopWithoutSavingRecording()
        verify(recorderControl).stopRecorder()
        assert(!audioViewModel.recorderRunning.value)
    }
    @Test
    fun testSetTemporaryFileName() {
        audioViewModel.setTemporaryFileName("temp")
        assert(audioViewModel.currentFileName.value == "temp")
    }
    @Test
    fun testGetCurrentPosition() {
        `when`(mediaPlayerWrapper.getCurrentPosition()).thenReturn(100)
        assert(audioViewModel.getCurrentPosition() == 100)
        verify(mediaPlayerWrapper).getCurrentPosition()
    }

    @Test
    fun testGetAudioDuration() {
        val testFile = File("/harryerayaudiorecorder/resources/rooster.mp3")
        `when`(mediaPlayerWrapper.getDuration()).thenReturn(5000)
        audioViewModel.getAudioDuration(testFile)
        verify(mediaPlayerWrapper).setDataSource("/harryerayaudiorecorder/resources/rooster.mp3")
        verify(mediaPlayerWrapper).prepare()
        assert(audioViewModel.getAudioDuration(testFile) == 5000)
    }

    @Test
    fun testSeekTo() {
        audioViewModel.seekTo(1000)
        verify(mediaPlayerWrapper).seekTo(1000, 3)
    }

    @Test
    fun testSetLooping() {
        audioViewModel.setLooping(true)
        verify(mediaPlayerWrapper).setLooping(true)
    }

    @Test
    fun testSetPlaybackSpeed() {
        `when`(mediaPlayerWrapper.isPlaying()).thenReturn(true)
        audioViewModel.setPlaybackSpeed(1.5f)
        verify(mediaPlayerWrapper).setPlaybackSpeed(1.5f)
    }

    @Test
    fun testAdjustPlaybackSpeed() {
        audioViewModel.adjustPlaybackSpeed(2.0f)
        verify(mediaPlayerWrapper).setPlaybackSpeed(2.0f)
    }

    @Test
    fun testFastForward() {
        `when`(mediaPlayerWrapper.getCurrentPosition()).thenReturn(1000)
        audioViewModel.fastForward(5000)
        verify(mediaPlayerWrapper).seekTo(6000, MediaPlayer.SEEK_CLOSEST)
    }

    @Test
    fun testFastRewind() {
        `when`(mediaPlayerWrapper.getCurrentPosition()).thenReturn(5000)
        audioViewModel.fastRewind(3000)
        verify(mediaPlayerWrapper).seekTo(2000, MediaPlayer.SEEK_CLOSEST)
    }

    @Test
    fun testRenameFile() {
        val newName = "newFile.wav"
        audioViewModel.renameFile(newName)
        assert(audioViewModel.currentFileName.value == newName)
    }

    @Test
    fun testSave() {
        val testFileName = "testSave.wav"
        audioViewModel.save(testFileName)
    }

    @Test
    fun testDeleteSoundCard() {
        val soundCard = SoundCard(100, "Test Title")
        val soundCardList = SnapshotStateList<MutableState<SoundCard>>()
        audioViewModel.deleteSoundCard(soundCard, soundCardList)
    }

    @Test
    fun testRenameSoundCard() {
        val soundCard = SoundCard(100, "Old Title")
        val newFileName = "newFile.wav"
        val soundCardList = SnapshotStateList<MutableState<SoundCard>>()
        audioViewModel.renameSoundCard(soundCard, newFileName, soundCardList)
    }

//    @Test
//    fun testShowUploadDialog() {
//        audioViewModel.showUploadDialog()
//        assert(audioViewModel.showUploadDialog.value)
//    }
//
//    @Test
//    fun testHideUploadDialog() {
//        audioViewModel.hideUploadDialog()
//        assert(!audioViewModel.showUploadDialog.value)
//    }
}
