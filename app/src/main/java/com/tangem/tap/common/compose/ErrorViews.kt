package com.tangem.tap.common.compose

import androidx.compose.material.LocalTextStyle
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * Created by Anton Zhilenkov on 09/04/2022.
 */
@Composable
fun ErrorView(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text,
        color = MaterialTheme.colors.error,
        modifier = modifier,
        style = style
    )
}