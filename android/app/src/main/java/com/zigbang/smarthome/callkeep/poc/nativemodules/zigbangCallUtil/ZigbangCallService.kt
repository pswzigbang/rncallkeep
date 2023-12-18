package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.os.IBinder
import android.os.Process
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import com.zigbang.smarthome.callkeep.poc.MainActivity
import com.zigbang.smarthome.callkeep.poc.R

class ZigbangCallService : Service() {
    companion object {
        private const val NOTIFICATION_ID = 2
        private const val CHANNEL_ID = "START_CALL_CHANNEL"
        private const val CHANNEL_NAME = "start call channel"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            createNotificationChannel()

            val pendingIntent = PendingIntent.getActivity(
                this, 0,
                Intent(this, MainActivity::class.java),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
            )
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_LOW) // Head Up Display를 위해 PRIORITY_HIGH 설정
                .setSmallIcon(R.drawable.img_zigbang_vertical) // 알림시 보여지는 아이콘. 반드시 필요
                .setContentText("음성 통화 중입니다")
                .setContentTitle(intent?.getStringExtra("name"))
                .setLargeIcon(CircleTransform().transform(BitmapFactory.decodeResource(resources, R.drawable.img_zigbang_vertical)))
                .setContentIntent(pendingIntent)

            val notification = builder.build()
            startForeground(NOTIFICATION_ID, notification)
        } catch (_: Exception) {
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(AppCompatActivity.NOTIFICATION_SERVICE) as NotificationManager?
            val startCallChannel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_LOW)
            notificationManager?.createNotificationChannel(startCallChannel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        try {
            stopSelf()
            Process.killProcess(Process.myPid())
        } catch (_: Exception) {
        }
    }
}
