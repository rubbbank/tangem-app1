package com.tangem.tap.common.extensions

import android.webkit.WebView

/**
 * Created by Anton Zhilenkov on 16.10.2022.
 */
fun WebView.configureSettings() {
    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
    }
}

fun WebView.stop() {
    stopLoading()
    pauseTimers()
    destroy()
}
