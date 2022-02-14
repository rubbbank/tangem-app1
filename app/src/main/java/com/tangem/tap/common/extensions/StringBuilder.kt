package com.tangem.tap.common.extensions

/**
 * Created by Anton Zhilenkov on 07/02/2022.
 */
fun StringBuilder.appendIf(value: String, predicate: (String) -> Boolean) {
    if (predicate(value)) this.append(value)
}