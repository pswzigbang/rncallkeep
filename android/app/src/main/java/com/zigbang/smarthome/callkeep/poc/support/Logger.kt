package com.zigbang.smarthome.callkeep.poc.support

import com.zigbang.smarthome.callkeep.poc.BuildConfig
import timber.log.Timber

typealias Logger = Timber

fun initLogger() {
    if (Logger.treeCount() > 0) return
    Logger.plant(object : Timber.DebugTree() {
        override fun isLoggable(tag: String?, priority: Int) = BuildConfig.DEBUG
    })
}
