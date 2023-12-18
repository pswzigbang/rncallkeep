package com.zigbang.smarthome.callkeep.poc.nativemodules

import com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil.ZigbangCallUtil
import com.zigbang.smarthome.callkeep.poc.nativemodules.zigbangCallUtil.BackgroundTimer
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ViewManager

class NativeModulePackage : ReactPackage {
    override fun createNativeModules(reactContext: ReactApplicationContext) = listOf(
        ZigbangCallUtil(reactContext),
        BackgroundTimer(reactContext),
    )

    override fun createViewManagers(reactContext: ReactApplicationContext) = emptyList<ViewManager<*, *>>()
}
