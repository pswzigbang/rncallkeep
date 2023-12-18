package com.zigbang.smarthome.callkeep.poc

import com.facebook.react.ReactActivity
import com.facebook.react.ReactActivityDelegate
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.fabricEnabled
import com.facebook.react.defaults.DefaultReactActivityDelegate
import io.wazo.callkeep.RNCallKeepModule

// Add these import lines
class MainActivity : ReactActivity() {
    /**
     * Returns the name of the main component registered from JavaScript. This is used to schedule
     * rendering of the component.
     */
    override fun getMainComponentName() = "rncallkeep"

    /**
     * Returns the instance of the [ReactActivityDelegate]. Here we use a util class [ ] which allows you to easily enable Fabric and Concurrent React
     * (aka React 18) with two boolean flags.
     */
    override fun createReactActivityDelegate(): ReactActivityDelegate {
        return DefaultReactActivityDelegate(
            this,
            mainComponentName!!,  // If you opted-in for the New Architecture, we enable the Fabric Renderer.
            fabricEnabled
        )
    }

    // Permission results
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            RNCallKeepModule.REQUEST_READ_PHONE_STATE -> RNCallKeepModule.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
        }
    }
}