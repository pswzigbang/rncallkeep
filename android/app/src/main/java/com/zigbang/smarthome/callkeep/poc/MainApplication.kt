package com.zigbang.smarthome.callkeep.poc

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.facebook.react.PackageList
import com.facebook.react.ReactApplication
import com.facebook.react.ReactNativeHost
import com.facebook.react.ReactPackage
import com.facebook.react.defaults.DefaultNewArchitectureEntryPoint.load
import com.facebook.react.defaults.DefaultReactNativeHost
import com.facebook.soloader.SoLoader
import com.zigbang.smarthome.callkeep.poc.nativemodules.NativeModulePackage
import com.zigbang.smarthome.callkeep.poc.support.Logger
import com.zigbang.smarthome.callkeep.poc.support.initLogger
import io.wazo.callkeep.RNCallKeepPackage

// Add this import line
class MainApplication : Application(), ReactApplication {

    private val mActivityLifecycleCallbacks = object : ActivityLifecycleCallbacks {
        override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityCreated = $activity")
        }

        override fun onActivityStarted(activity: Activity) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityStarted = $activity")
        }

        override fun onActivityResumed(activity: Activity) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityResumed = $activity")
        }

        override fun onActivityPaused(activity: Activity) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityPaused = $activity")
        }

        override fun onActivityStopped(activity: Activity) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityStopped = $activity")
        }

        override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivitySaveInstanceState = $activity")
        }

        override fun onActivityDestroyed(activity: Activity) {
            Logger.tag(MainApplication::class.java.simpleName).d("onActivityDestroyed = $activity")
        }
    }

    override fun onCreate() {
        super.onCreate()
        initLogger()
        SoLoader.init(this,  /* native exopackage */false)
        if (BuildConfig.IS_NEW_ARCHITECTURE_ENABLED) {
            // If you opted-in for the New Architecture, we load the native entry point for this app.
            load()
        }
        ReactNativeFlipper.initializeFlipper(this, reactNativeHost.reactInstanceManager)
        registerActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun onTerminate() {
        super.onTerminate()
        unregisterActivityLifecycleCallbacks(mActivityLifecycleCallbacks)
    }

    override fun getReactNativeHost() = mReactNativeHost

    private val mReactNativeHost = object : DefaultReactNativeHost(this) {
        override val isNewArchEnabled = BuildConfig.IS_NEW_ARCHITECTURE_ENABLED
        override val isHermesEnabled = BuildConfig.IS_HERMES_ENABLED
        override fun getUseDeveloperSupport() = BuildConfig.DEBUG
        override fun getJSMainModuleName() = "index"
        override fun getPackages(): List<ReactPackage> {
            return PackageList(this).packages.apply {
                add(NativeModulePackage())
//                add(RNCallKeepPackage())
            }
        }

    }

}