package com.tangem.tap.common

import android.content.Intent

/**
 * Created by Anton Zhilenkov on 16.10.2022.
 */
interface ActivityResultCallbackHolder {
    fun addOnActivityResultCallback(callback: OnActivityResultCallback)
    fun removeOnActivityResultCallback(callback: OnActivityResultCallback)
}

typealias OnActivityResultCallback = (Int, Int, Intent?) -> Unit
