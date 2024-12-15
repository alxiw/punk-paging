package io.github.alxiw.punkpaging.util

import android.os.Build

private const val GOLDFISH = "goldfish"
private const val RANCHU = "ranchu"
private const val SDK = "sdk"


object DeviceUtil {
    fun isEmulator(): Boolean {
        return Build.PRODUCT.contains(SDK)
                || Build.HARDWARE.contains(GOLDFISH)
                || Build.HARDWARE.contains(RANCHU)
    }
}
