package com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil

import android.Manifest
import android.app.KeyguardManager
import android.app.KeyguardManager.KeyguardDismissCallback
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.zigbang.smarthome.callkeep.poc.MainApplication
import com.zigbang.smarthome.callkeep.poc.R
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.squareup.picasso.Picasso

class UnlockScreenActivity : AppCompatActivity() {
    companion object {
        var active = false
        var fa: UnlockScreenActivity? = null
    }

    private var uuid: String? = ""
    private val MULTI_PERMISSION_CODE = 1

    public override fun onStart() {
        super.onStart()
        active = true
    }

    public override fun onStop() {
        super.onStop()
        active = false
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fa = this

        val font = Typeface.createFromAsset(assets, "fonts/SpoqaHanSans.ttf")
        setContentView(R.layout.activity_call_incoming)
        val tvName: TextView = findViewById(R.id.tvName)
        val tvInfo: TextView = findViewById(R.id.tvInfo)
        val ivAvatar: ImageView = findViewById(R.id.ivAvatar)
        val tvAccept: TextView = findViewById(R.id.tvAccept)
        val tvDecline: TextView = findViewById(R.id.tvDecline)
        tvAccept.typeface = font
        tvDecline.typeface = font

        val bundle = intent.extras

        if (bundle != null) {
            // TODO. 이 변수로 다이렉트로 accept 시킬지 안시킬지 결정한다
            val isDirectAccept = bundle.getBoolean("isDirectAccept")

            if (bundle.containsKey("uuid")) {
                uuid = bundle.getString("uuid")
            }
            if (bundle.containsKey("name")) {
                val name = bundle.getString("name")
                tvName.typeface = font
                tvName.text = name
            }
            if (bundle.containsKey("info")) {
                val info = bundle.getString("info")
                tvInfo.typeface = font
                tvInfo.text = info
            }
            if (bundle.containsKey("avatar")) {
                val avatar = bundle.getString("avatar")
                if (avatar != "null") {
                    Picasso.get().load(avatar).transform(CircleTransform()).into(ivAvatar)
                } else {
                    Picasso.get().load(R.drawable.img_zigbang_vertical).transform(CircleTransform()).into(ivAvatar)
                }
            }

            if (isDirectAccept) {
                cancelNotification()

                acceptDialingOrShowNeedPermissionDialog()
            }
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        // activity navigation bar color
        window.navigationBarColor = Color.parseColor("#222222")

        // activity full screen 여부등의 세팅 (fullscreen x)
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                or WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD)

        // status bar 투명으로 만들기
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = 0x00000000
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE

        val acceptCallBtn: ImageView = findViewById(R.id.ivAcceptCall)
        acceptCallBtn.setOnClickListener {
            try {
                acceptDialingOrShowNeedPermissionDialog()
            } catch (e: Exception) {
                val params: WritableMap = Arguments.createMap()
                params.putString("message", e.message)
                sendEvent("error", params)
                dismissDialing()
            }
        }
        val rejectCallBtn: ImageView = findViewById(R.id.ivDeclineCall)
        rejectCallBtn.setOnClickListener {
            dismissDialing()
        }
    }

    override fun onBackPressed() {
        // Dont back
    }

    private fun cancelNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager?
        notificationManager?.cancel(ZigbangCallUtil.NOTIFICATION_ID)
    }

    private fun unregisterNotificationReceiver() {
        try {
            val application = this.application as MainApplication

            application.reactNativeHost.reactInstanceManager.currentReactContext?.unregisterReceiver(ZigbangCallUtil.notificationReceiver)
        } catch (e: Exception) {
        }
    }

    private fun stopRingtone() {
        try {
            ZigbangCallUtil.vibrator?.cancel()
            ZigbangCallUtil.ringtone?.stop()
            ZigbangCallUtil.alternativeRingtone?.stop()
            ZigbangCallUtil.alternativeRingtone?.prepare()
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun acceptDialingOrShowNeedPermissionDialog() {
        if (hasRecordAudioPermission() && hasBluetoothPermission() && hasReadPhoneStatePermission()) {
            acceptDialing()
        } else {
            showNeedPermissionDialog()
        }
    }

    private fun hasRecordAudioPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_DENIED

    private fun hasBluetoothPermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_DENIED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_DENIED
    }

    private fun hasReadPhoneStatePermission(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_DENIED
    }

    private fun getPermissions(): ArrayList<String> {
        var permissions = ArrayList<String>()

        if (!hasReadPhoneStatePermission()) {
            permissions.add(Manifest.permission.READ_PHONE_STATE)
        }

        if (!hasBluetoothPermission()) {
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        }

        if (!hasRecordAudioPermission()) {
            permissions.add(Manifest.permission.RECORD_AUDIO)
        }

        return permissions
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun checkPermissions(): Unit {
        if (hasRecordAudioPermission() && hasBluetoothPermission() && hasReadPhoneStatePermission()) {
            acceptDialing()
        } else {
            val permissions = getPermissions()
            ActivityCompat.requestPermissions(this, permissions.toTypedArray(), MULTI_PERMISSION_CODE)
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MULTI_PERMISSION_CODE) {
            if (grantResults.isNotEmpty()) {
                for (i in grantResults.indices) {
                    if(grantResults[i] == -1){
                        return showNeedToGoSettingDialog();
                    }
                }
            } else {
                showNeedToGoSettingDialog()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun acceptDialing() {
        try {
            cancelNotification()

            val params: WritableMap = Arguments.createMap()
            params.putBoolean("accept", true)
            params.putString("uuid", uuid)

            val application = this.application as MainApplication
            if (application.reactNativeHost.reactInstanceManager.currentReactContext?.hasCurrentActivity()
                != true
            ) {
                params.putBoolean("isHeadless", true)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
                if (keyguardManager.isDeviceLocked) {
                    keyguardManager.requestDismissKeyguard(
                        this,
                        object : KeyguardDismissCallback() {}
                    )
                }
            }

            stopRingtone()
            sendEvent("answerCall", params)
            unregisterNotificationReceiver()
            finish()
        } catch (_: Exception) {
        }
    }

    private fun dismissDialing() {
        try {
            cancelNotification()

            val params: WritableMap = Arguments.createMap()
            params.putBoolean("accept", false)
            params.putString("uuid", uuid)

            val application = this.application as MainApplication
            if (application.reactNativeHost.reactInstanceManager.currentReactContext?.hasCurrentActivity()
                != true
            ) {
                params.putBoolean("isHeadless", true)
            }

            stopRingtone()
            sendEvent("endCall", params)
            unregisterNotificationReceiver()
            finish()
        } catch (_: Exception) {
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun showNeedPermissionDialog() {
        val dialog = ZigbangAlertDialog.ZigbangAlertDialogBuilder()
                .setTitle("통화 연결 권한이 필요해요.")
                .setMessage("통화하려면 마이크 권한이 필요합니다.\n권한 요청이 뜨면 허용해주세요.")
                .setPositiveBtnText("확인")
                .setBtnClickListener(object : ZigbangAlertDialogListener {
                    override fun onClickPositiveBtn() {
                        checkPermissions()
                    }

                override fun onClickNegativeBtn() {}
            })
            .create()
        dialog.show(supportFragmentManager, dialog.tag)
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    fun showNeedToGoSettingDialog() {
        val permissionText = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) "마이크" else "\n마이크, 블루투스, 모바일 통화 상태"
        val dialog = ZigbangAlertDialog.ZigbangAlertDialogBuilder()
                .setTitle("통화 연결 권한이 필요해요.")
                .setMessage("통화하려면" + permissionText + " 권한이 필요합니다.\n권한 허용을 위해 설정을 눌러주세요.")
                .setPositiveBtnText("설정")
                .setNegativeBtnText("취소")
                .setBtnClickListener(object : ZigbangAlertDialogListener {
                    override fun onClickPositiveBtn() {
                        openAppSystemSettings()
                    }

                override fun onClickNegativeBtn() {}
            })
            .create()
        dialog.show(supportFragmentManager, dialog.tag)
    }

    fun openAppSystemSettings() {
        startActivity(Intent().apply {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            data = Uri.fromParts("package", packageName, null)
        })
    }

    private fun sendEvent(eventName: String, params: WritableMap) {
        val application = this.application as MainApplication
        application.reactNativeHost.reactInstanceManager.currentReactContext
            ?.getJSModule(RCTDeviceEventEmitter::class.java)
            ?.emit(eventName, params)
    }
}
