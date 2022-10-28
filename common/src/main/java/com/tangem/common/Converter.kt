package com.tangem.common

/**
 * Created by Anton Zhilenkov on 22/09/2022.
 */
interface Converter<I, O> {
    fun convert(value: I): O
}
