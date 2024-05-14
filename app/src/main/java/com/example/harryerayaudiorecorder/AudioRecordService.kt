package com.example.harryerayaudiorecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.util.Log
import androidx.activity.result.ActivityResult
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AudioRecordService : Service() {
    companion object {
        lateinit var activityResult: ActivityResult
        const val TAG = "AudioRecordService"
        const val NOTIFICATION_ID = 19630303
        const val NOTIFICATION_CHANNEL_ID = "com.HarryErayAudioRecorder"
        const val NOTIFICATION_CHANNEL_NAME = "com.HarryErayAudioRecorder"

        fun start(context: Context, mediaProjectionActivityResult: ActivityResult) {
            activityResult = mediaProjectionActivityResult
            val intent = Intent(context, AudioRecordService::class.java)
            context.startForegroundService(intent)
        }
    }

    val audioRecordingTask by lazy {
        val mediaProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        val mediaProjection = mediaProjectionManager.getMediaProjection(
            activityResult.resultCode,
            activityResult.data!!
        )
        AudioRecordingTask(this, mediaProjection)
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind")
        return null
    }

    private val timestamp = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.ITALY).format(Date())
    private val fileNamePCM = "SystemAudio-$timestamp.pcm" //PCM file

    private val fileOutputStream by lazy {
        val audioCapturesDirectory = File(getExternalFilesDir(null), "/AudioCaptures")
        if (!audioCapturesDirectory.exists()) {
            audioCapturesDirectory.mkdirs()
        }

        File(audioCapturesDirectory.absolutePath + "/" + fileNamePCM)
        FileOutputStream(File(audioCapturesDirectory.absolutePath + "/" + fileNamePCM))
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy")
        //stop the task
        audioRecordingTask.cancel()
        Log.d(TAG, fileOutputStream.toString())
        val fileNameWAV = fileNamePCM.dropLast(3) + "wav"

        fileOutputStream.close()
        val audioCapturesDirectory = File(getExternalFilesDir(null), "/AudioCaptures")

        val f1 = File(audioCapturesDirectory.absolutePath + "/" + fileNamePCM) // The location of your PCM file

        val f2 =
            File(audioCapturesDirectory.absolutePath + "/" + fileNameWAV) // The location where you want your WAV file

        try {
            AudioConversionUtils.rawToWave(f1, f2)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        Log.d(TAG, intent.toString())
        createNotification()
        startRecording()
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotification() {
        Log.d(TAG, "createNotification")

        createNotificationChannel(this)
        val notification = Notification.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_baseline_record_voice_over_24)
            .setContentTitle(this.getString(R.string.app_name))
            .setContentText(this.getString(R.string.recording))
            .setOngoing(true)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setShowWhen(true)
            .build()

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        notificationManager.notify(
            NOTIFICATION_ID,
            notification
        )

        startForeground(
            NOTIFICATION_ID,
            notification,
        )
    }

    private fun createNotificationChannel(context: Context) {
        Log.d(TAG, "createNotificationChannel")

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        )
        channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val manager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
        Log.d(TAG, "createNotificationChannel finished")

    }

    fun startRecording() {
        Log.d(TAG, "startRecording!!!")
        audioRecordingTask.execute(fileOutputStream)
    }
}