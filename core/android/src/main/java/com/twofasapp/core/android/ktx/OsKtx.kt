package com.twofasapp.core.android.ktx

import android.os.Build

fun isRunningCustomOs(): Boolean {
    return try {
        isRunningCalyxOs() || isRunningGrapheneOs() || isRunningLineageOs()
    } catch (e: Exception) {
        false
    }
}

private fun isRunningCalyxOs(): Boolean {
    val systemProp = getSystemProperty("ro.calyx.version")?.lowercase()
    return buildProps.contains("calyx") || systemProp.isNullOrEmpty().not()
}

private fun isRunningGrapheneOs(): Boolean {
    val systemProp = getSystemProperty("ro.vendor.build.fingerprint")?.lowercase()
    return buildProps.contains("graphene") || systemProp.orEmpty().contains("graphene")
}

private fun isRunningLineageOs(): Boolean {
    return buildProps.contains("lineage")
}

private val buildProps: String
    get() = listOf(Build.PRODUCT, Build.DISPLAY, Build.FINGERPRINT).joinToString(" ").lowercase()

private fun getSystemProperty(propName: String): String? {
    return try {
        val process = Runtime.getRuntime().exec("getprop $propName")
        process.inputStream.bufferedReader().readLine()?.trim()
    } catch (e: Exception) {
        null
    }
}