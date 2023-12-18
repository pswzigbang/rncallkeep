package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Notification
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadset
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.telephony.TelephonyManager
import android.view.WindowManager
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.zigbang.smarthome.callkeep.poc.R
import com.zigbang.smarthome.callkeep.poc.support.Logger
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.squareup.picasso.Picasso
import com.squareup.picasso.RequestCreator

class ZigbangCallUtil(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    companion object {
        const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "INCOMING_CALL_CHANNEL"
        private const val CHANNEL_NAME = "incoming channel"

        var vibrator: Vibrator? = null
        var ringtone: Ringtone? = null
        var alternativeRingtone: MediaPlayer? = null
        var notificationReceiver: BroadcastReceiver? = null
    }

    private val audioManager = reactContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var headlessExtras: WritableMap? = null
    private var bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var receiver: BroadcastReceiver? = null
    private val telephonyManager = reactContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

    private val pattern = longArrayOf(0, 1000, 800)

    init {
        vibrator = reactContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        alternativeRingtone = MediaPlayer.create(reactContext, Settings.System.DEFAULT_RINGTONE_URI)
    }

    override fun getName() = "ZigbangCallUtil"

    private fun stopRingtone() {
        try {
            vibrator?.cancel()
            ringtone?.stop()
            alternativeRingtone?.stop()
            alternativeRingtone?.prepare()
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    @ReactMethod
    fun display(uuid: String?, name: String?, avatar: String?, info: String?) {
        try {
            if (UnlockScreenActivity.active) {
                return
            }
            // 무조건 진동
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttributes = AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .build()
                val ve = VibrationEffect.createWaveform(pattern, 0)
                vibrator?.vibrate(ve, audioAttributes)
            } else {
                vibrator?.vibrate(pattern, 0)
            }

            if (audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
                // 벨소리 모드일 경우
                ringtone = RingtoneManager.getRingtone(reactApplicationContext, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE))
                if (ringtone != null) {
                    ringtone?.play()
                } else {
                    alternativeRingtone?.start()
                }
            }

            val bundle = Bundle()
            bundle.putString("uuid", uuid)
            bundle.putString("name", name)
            bundle.putString("avatar", avatar)
            bundle.putString("info", info)

            createNotificationChannel(reactApplicationContext)
            createNotification(reactApplicationContext, createUnlockScreenActivityIntent(bundle, false), bundle)
        } catch (_: Exception) {
        }
    }

    @Suppress("WrongConstant")
    fun createUnlockScreenActivityIntent(bundle: Bundle, isDirectAccept: Boolean): Intent {
        bundle.putBoolean("isDirectAccept", isDirectAccept)

        val intent = Intent(reactApplicationContext, UnlockScreenActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        intent.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED +
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD +
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        intent.putExtras(bundle)
        return intent
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun createNotification(context: ReactContext, intent: Intent, bundle: Bundle) {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_IMMUTABLE
        val fullScreenPendingIntent = PendingIntent.getActivity(context, 0, intent, flags)

        var notification: Notification? = null

        var builder:NotificationCompat.Builder =
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_MAX) // Head Up Display를 위해 PRIORITY_HIGH 설정
                .setFullScreenIntent(fullScreenPendingIntent, true)
                .setContentTitle(bundle.getString("info"))
                .setContentText(bundle.getString("name"))
                .setAutoCancel(true)
                .setDeleteIntent(getPendingSelfIntent(context, "dismiss"))

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder = setNotificationBuilderForUpToSDK31(builder, context, bundle)
            notification = builder.build()
        } else {
            val remoteViews = getNotificationRemoteView(context, bundle)
            builder = setNotificationBuilderForUnderSDK31(builder, remoteViews)

            notification = builder.build()

            addUIHandlerToRemoteView(notification, bundle, remoteViews)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.notify(NOTIFICATION_ID, notification)

        addNotificationActionListener(context, bundle)
    }

    private fun addUIHandlerToRemoteView(notification: Notification, bundle: Bundle, remoteViews: RemoteViews) {
        val uiHandler = Handler(Looper.getMainLooper())
        uiHandler.post {
            getAgentProfileImage(bundle.getString("avatar")).into(remoteViews, R.id.ivAvatar, NOTIFICATION_ID, notification)
        }
    }

    private fun addNotificationActionListener(context: ReactContext, bundle: Bundle) {
        val intentFilter = IntentFilter()
        intentFilter.addAction("dismiss")
        notificationReceiver = object : BroadcastReceiver() {
            override fun onReceive(receivedContext: Context, intent: Intent) {
                val action = intent.action
                if (action == "dismiss") {
                    stopRingtone()
                    val params: WritableMap = Arguments.createMap()
                    params.putBoolean("accept", false)
                    params.putString("uuid", bundle.getString("uuid"))
                    if (!context.hasCurrentActivity()) {
                        params.putBoolean("isHeadless", true)
                    }
                    sendEvent("endCall", params)
                    destroyNotification()
                    context.unregisterReceiver(notificationReceiver)
                }
            }
        }

        context.registerReceiver(notificationReceiver, intentFilter)
    }

    private fun setNotificationBuilderForUpToSDK31(builder: NotificationCompat.Builder, context: ReactContext, bundle: Bundle): NotificationCompat.Builder {
        val agentProfileImg = getAgentProfileImage(bundle.getString("avatar")).get()
        val dismissAction: NotificationCompat.Action = NotificationCompat.Action.Builder(R.drawable.ic_hangup_red4, "거절", getPendingSelfIntent(context, "dismiss")).build()
        val acceptAction: NotificationCompat.Action = NotificationCompat.Action.Builder(R.drawable.ic_call_green2, "통화하기", getAcceptPendingIntent(context, bundle)).build()
        return builder.setStyle(NotificationCompat.BigTextStyle().bigText(bundle.getString("name")))
            .setLargeIcon(agentProfileImg)
            .setColor(Color.parseColor("#FA880B"))
            .setSmallIcon(R.drawable.ic_call)
            .addAction(dismissAction)
            .addAction(acceptAction)
            .setCategory(NotificationCompat.CATEGORY_CALL)
    }

    private fun getNotificationRemoteView(context: ReactContext, bundle: Bundle): RemoteViews {
        val remoteViews = RemoteViews(context.packageName, R.layout.activity_call_incoming_headup)

        if (bundle.containsKey("name")) {
            remoteViews.setTextViewText(R.id.tvName, bundle.getString("name"))
        }
        if (bundle.containsKey("info")) {
            remoteViews.setTextViewText(R.id.tvInfo, bundle.getString("info"))
        }
        remoteViews.setOnClickPendingIntent(R.id.ivInformation, getPendingSelfIntent(context, "none"))
        remoteViews.setOnClickPendingIntent(R.id.ivAcceptCall, getAcceptPendingIntent(context, bundle))
        remoteViews.setOnClickPendingIntent(R.id.ivDeclineCall, getPendingSelfIntent(context, "dismiss"))
        return remoteViews
    }

    private fun setNotificationBuilderForUnderSDK31(builder: NotificationCompat.Builder, remoteViews: RemoteViews): NotificationCompat.Builder {
        return builder.setCustomHeadsUpContentView(remoteViews)
            .setCustomContentView(remoteViews)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setColorized(true)
            .setSmallIcon(R.drawable.ic_zigang_logo)
    }

    private fun getAgentProfileImage(avatarUrl: String?): RequestCreator {
        if (avatarUrl != "null") {
            return Picasso.get().load(avatarUrl).transform(CircleTransform()).resize(256, 256)
        } else {
            return Picasso.get().load(R.drawable.img_profile_zigbang_logo)
                .transform(CircleTransform())
        }
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)?.emit(eventName, params)
    }

    @ReactMethod
    fun destroyNotification() {
        try {
            reactApplicationContext.stopService(Intent(reactApplicationContext, ZigbangCallService::class.java))
            val notificationManager = reactApplicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
            notificationManager?.cancel(NOTIFICATION_ID)
        } catch (_: Exception) {
        }
    }

    @ReactMethod
    fun startCallNotification(name: String) {
        try {
            val intent = Intent(reactApplicationContext, ZigbangCallService::class.java)
            val bundle = Bundle()
            bundle.putString("name", name)
            ContextCompat.startForegroundService(reactApplicationContext, intent)
        } catch (_: Exception) {
        }
    }

    private fun getPendingSelfIntent(context: Context?, action: String?): PendingIntent? {
        val intent = Intent(action)
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = flags or PendingIntent.FLAG_MUTABLE
        return PendingIntent.getBroadcast(context, 0, intent, flags)
    }

     private fun getAcceptPendingIntent(context: Context, bundle: Bundle): PendingIntent? {
        var flags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) flags = PendingIntent.FLAG_MUTABLE
        return PendingIntent.getActivity(context, 0, createUnlockScreenActivityIntent(bundle, true), flags)
    }

    @ReactMethod
    fun endCall() {
        try {
            val params: WritableMap = Arguments.createMap()
            params.putBoolean("accept", false)
            if (reactApplicationContext.hasCurrentActivity()) {
                params.putBoolean("isHeadless", true)
            }
            sendEvent("endCall", params)

            destroyNotification()
            stopRingtone()
            val presenting = UnlockScreenActivity.fa ?: return
            presenting.finish()
        } catch (_: Exception) {
        }
    }

    @ReactMethod
    fun backToForeground() {
        try {
            val context = reactApplicationContext
            val packageName = context.packageName
            val focusIntent = context.packageManager.getLaunchIntentForPackage(packageName)!!.cloneFilter()
            val activity = currentActivity
            Logger.d( "backToForeground, app isOpened ? ${activity != null}")
            if (activity != null) {
                focusIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                activity.startActivity(focusIntent)
            }
        } catch (_: Exception) {
        }
    }

    @Suppress("WrongConstant")
    @ReactMethod
    fun openAppFromHeadlessMode(uuid: String?) {
        try {
            val context = reactApplicationContext
            val packageName = context.packageName
            val focusIntent = context.packageManager.getLaunchIntentForPackage(packageName)!!.cloneFilter()
            if (currentActivity == null) {
                focusIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
                val response = WritableNativeMap()
                response.putBoolean("isHeadless", true)
                response.putString("uuid", uuid)
                headlessExtras = response
                context.startActivity(focusIntent)
            }
        } catch (_: Exception) {
        }
    }

    @ReactMethod
    fun getExtrasFromHeadlessMode(promise: Promise) {
        if (headlessExtras != null) {
            promise.resolve(headlessExtras)
            headlessExtras = null
            return
        }
        promise.resolve(null)
    }

    @Suppress("MissingPermission")
    @ReactMethod
    fun checkBluetoothState(promise: Promise) {
        val connectedBluetooth =
            bluetoothAdapter?.getProfileConnectionState(BluetoothProfile.HEADSET) == BluetoothProfile.STATE_CONNECTED
        promise.resolve(connectedBluetooth)
    }

    private fun onChange(deviceName: String) {
        try {
            // Report device name (if not empty) to the host
            val payload = Arguments.createMap()
            val deviceList = Arguments.createArray()
            if (deviceName.isNotEmpty()) {
                deviceList.pushString(deviceName)
            }
            payload.putArray("devices", deviceList)
            reactApplicationContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
                .emit("onChange", payload)
        } catch (_: Exception) {
        }
    }

    @Suppress("MissingPermission")
    @ReactMethod
    fun onStateChange() {
        try {
            if (currentActivity == null) {
                return
            }
            val intentFilter = IntentFilter(BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED)
            intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    val action = intent.action
                    if (action == BluetoothHeadset.ACTION_CONNECTION_STATE_CHANGED) {
                        // Bluetooth headset connection state has changed
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        val state = intent.getIntExtra(
                            BluetoothProfile.EXTRA_STATE,
                            BluetoothProfile.STATE_DISCONNECTED
                        )
                        if (state == BluetoothProfile.STATE_CONNECTED) {
                            // Device has connected, report it
                            onChange(device?.name ?: "No Name")
                        } else if (state == BluetoothProfile.STATE_DISCONNECTED) {
                            // Device has disconnected, report it
                            onChange("")
                        }
                    } else if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                        val state = intent.getIntExtra(
                            BluetoothProfile.EXTRA_STATE,
                            BluetoothProfile.STATE_DISCONNECTED
                        )
                        if (state == BluetoothProfile.STATE_DISCONNECTED) {
                            // Bluetooth is disabled
                            onChange("")
                        }
                    }
                }
            }

            // Subscribe for intents
            currentActivity?.registerReceiver(receiver, intentFilter)
        } catch (_: Exception) {
        }
    }

    @ReactMethod
    fun setAudioRoute(audioType: String) {
        when (audioType) {
            "Bluetooth" -> {
                audioManager.startBluetoothSco()
                audioManager.isBluetoothScoOn = true
                audioManager.isSpeakerphoneOn = false
            }
            "Speaker" -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = true
            }
            else -> {
                audioManager.stopBluetoothSco()
                audioManager.isBluetoothScoOn = false
                audioManager.isSpeakerphoneOn = false
            }
        }
    }

    @ReactMethod
    fun showListDialog(selectedId: String, promise: Promise) {
        val items = arrayOf("블루투스", "휴대전화", "스피커")
        val itemsId = arrayOf("Bluetooth", "Phone", "Speaker")
        val selectedIndex = itemsId.indexOf(selectedId)
        if (currentActivity == null) {
            return
        }
        val mBuilder = AlertDialog.Builder(currentActivity)
        mBuilder.setTitle("음성출력")
        mBuilder.setSingleChoiceItems(items, selectedIndex
        ) { dialog, selected ->
            promise.resolve(itemsId[selected])
            dialog.dismiss()
        }
        mBuilder.setCancelable(true)
        mBuilder.show()
    }

    @ReactMethod
    fun getAudioMode(promise: Promise) {
        val audioMode = audioManager.mode
        promise.resolve(audioMode)
    }

    @ReactMethod
    fun getPhoneState(promise: Promise) {
        val phoneState = when (telephonyManager.callState) {
            TelephonyManager.CALL_STATE_RINGING -> "RINGING"
            TelephonyManager.CALL_STATE_OFFHOOK -> "OFFHOOK"
            else -> "IDLE"
        }
        promise.resolve(phoneState)
    }
}
