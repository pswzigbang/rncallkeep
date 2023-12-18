package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.PowerManager
import com.facebook.react.bridge.LifecycleEventListener
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter

@SuppressLint("InvalidWakeLockTag")
class BackgroundTimer(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var powerManager: PowerManager? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private val listener: LifecycleEventListener = object : LifecycleEventListener {
        override fun onHostResume() {}
        override fun onHostPause() {}
        override fun onHostDestroy() {
            if (wakeLock!!.isHeld) wakeLock!!.release()
        }
    }

   init {
        try {
            powerManager = reactContext.getSystemService(Context.POWER_SERVICE) as PowerManager
            wakeLock = powerManager!!.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "rohit_bg_wakelock")
            reactContext.addLifecycleEventListener(listener)
        } catch (_: Exception) {
        }
    }

    override fun getName() = "BackgroundTimer"

    @ReactMethod
    fun setTimeout(id: Int, timeout: Double) {
        try {
            val handler = Handler()
            handler.postDelayed({
                if (reactApplicationContext.hasActiveReactInstance()) {
                    reactApplicationContext
                        .getJSModule(RCTDeviceEventEmitter::class.java)
                        .emit("backgroundTimer.timeout", id)
                }
            }, timeout.toLong())
        } catch (_: Exception) {
        }
    }
}
