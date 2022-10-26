package com.tangem.common

/**
 * Created by Anton Zhilenkov on 08.10.2022.
 */
interface Filter<T> {
    fun filter(value: T): Boolean
}
