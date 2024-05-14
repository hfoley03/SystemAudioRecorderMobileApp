
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.example.harryerayaudiorecorder.AudioUtils
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [Config.OLDEST_SDK]) // Specify an SDK version that matches your testing needs
class AudioUtilsTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = mockk(relaxed = true)
        mockkStatic(ContextCompat::class)
    }

    @Test
    fun `hasRecordAudioPermission returns true when permission is granted`() {
        every { ContextCompat.checkSelfPermission(any(), android.Manifest.permission.RECORD_AUDIO) } returns PackageManager.PERMISSION_GRANTED
        val result = AudioUtils.hasRecordAudioPermission(context)
        assert(result) { "Expected to have record audio permission" }
    }

    @Test
    fun `hasRecordAudioPermission returns false when permission is not granted`() {
        every { ContextCompat.checkSelfPermission(any(), android.Manifest.permission.RECORD_AUDIO) } returns PackageManager.PERMISSION_DENIED
        val result = AudioUtils.hasRecordAudioPermission(context)
        assert(!result) { "Expected not to have record audio permission" }
    }
}
