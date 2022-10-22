package com.tangem.tap.common.extensions

import android.widget.ImageView
import androidx.annotation.DrawableRes

/**
 * Created by Anton Zhilenkov on 23.10.2022.
 */
fun ImageView.setDrawable(@DrawableRes resId: Int) {
    setImageDrawable(context.getDrawableCompat(resId))
}
