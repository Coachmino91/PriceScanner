package com.price_scanner.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import androidx.camera.core.ImageProxy
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.price_scanner.activities.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class PriceScanningService : android.app.Service() {

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private var currentPrice: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground()
        processCameraInput()
        return START_NOT_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Price Scanner",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Scans product prices" }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun startForeground() {
        val pendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Price Scanner")
            .setContentText("Scanning product prices...")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun processCameraInput() {
        serviceScope.launch {
            try {
                val bitmap = getCameraInput()
                if (bitmap != null) {
                    currentPrice = extractPriceFromImage(bitmap)
                    sendPriceToActivity(currentPrice)
                }
            } catch (e: Exception) {
                // Errore loggato server-side, non esposto all'utente
                android.util.Log.e(TAG, "Errore durante la scansione", e)
            }
        }
    }

    /**
     * Restituisce il bitmap corrente dalla fotocamera.
     * L'integrazione CameraX avviene tramite [CameraController] nell'Activity;
     * questa funzione riceve il bitmap attraverso [setLatestBitmap].
     */
    private suspend fun getCameraInput(): Bitmap? = latestBitmap

    private suspend fun extractPriceFromImage(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val result = recognizer.process(image).await()
            val priceRegex = Regex("""[\d]+[.,]\d{2}""")
            result.textBlocks
                .flatMap { it.lines }
                .flatMap { it.elements }
                .mapNotNull { priceRegex.find(it.text)?.value }
                .firstOrNull() ?: "0.00"
        } catch (e: Exception) {
            android.util.Log.e(TAG, "Errore OCR", e)
            "0.00"
        }
    }

    private fun sendPriceToActivity(price: String) {
        val intent = Intent(ACTION_PRICE_SCANNED).apply {
            putExtra(EXTRA_PRICE, price)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        recognizer.close()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "PriceScanningService"
        private const val CHANNEL_ID = "price_scanner_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_PRICE_SCANNED = "com.price_scanner.PRICE_SCANNED"
        const val EXTRA_PRICE = "price"

        /** Bitmap più recente fornito dalla CameraX dell'Activity. */
        @Volatile
        var latestBitmap: Bitmap? = null
    }
}
