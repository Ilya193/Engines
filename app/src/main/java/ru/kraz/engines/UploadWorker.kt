package ru.kraz.engines

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Context.NOTIFICATION_SERVICE
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UploadWorker(private val context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    private val storage = FirebaseStorage.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private fun createNotificationChannel() {
        val notificationManager = context.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentTitle("Идет загрузка данных на сервер")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .build()
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(NOTIFICATION_ID, createNotification())
    }
    override suspend fun doWork(): Result {
        val type = inputData.getString(KEY_TYPE)
        val description = inputData.getString(KEY_DESCRIPTION)
        val uriSound = inputData.getString(KEY_SOUND_URI)
        val uriImages = inputData.getStringArray(KEY_IMAGES_URI)?.toList()

        var downloadUrlSound = ""
        var downloadUrlImages = listOf<String>()

        if (type != null && description != null && uriSound != null && uriImages != null) {
            setForeground(getForegroundInfo())
            withContext(Dispatchers.IO) {
                val tempSound = async {
                    val ref = storage.reference.child("upload/${Uri.parse(uriSound).lastPathSegment}")
                    ref.putFile(Uri.parse(uriSound)).await()
                    val uri = ref.downloadUrl.await()
                    uri
                }

                val tempImages = async {
                    val uris = mutableListOf<Uri>()
                    for (i in uriImages.indices) {
                        val ref =
                            storage.reference.child("upload/$${Uri.parse(uriImages[i]).lastPathSegment}")
                        ref.putFile(Uri.parse(uriImages[i])).await()
                        val uri = ref.downloadUrl.await()
                        uris.add(uri)
                    }
                    uris
                }

                downloadUrlSound = tempSound.await().toString()

                downloadUrlImages = tempImages.await().map {
                    it.toString()
                }

                val document = firestore.collection("items").document()
                document.set(
                    EngineCloud(
                        id = document.id,
                        name = type,
                        description = description,
                        sound = downloadUrlSound,
                        images = downloadUrlImages
                    )
                ).await()
                val cacheFiles = context.cacheDir.listFiles()
                if (cacheFiles != null) {
                    for (file in cacheFiles) {
                        file.delete()
                    }
                }
            }
        }
        return Result.success()
    }

    companion object {
        const val KEY_TYPE = "KEY_TYPE"
        const val KEY_DESCRIPTION = "KEY_DESCRIPTION"
        const val KEY_SOUND_URI = "KEY_SOUND_URI"
        const val KEY_IMAGES_URI = "KEY_IMAGES_URI"

        private const val NOTIFICATION_ID = 149
        private const val CHANNEL_ID = "CHANNEL_ID"
        private const val CHANNEL_NAME = "CHANNEL_NAME"
    }
}