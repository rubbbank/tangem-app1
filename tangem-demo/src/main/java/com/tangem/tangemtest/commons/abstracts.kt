package com.tangem.tangemtest.commons

/**
 * Created by Anton Zhilenkov on 10.03.2020.
 */
interface LayoutHolder {
    fun getLayoutId(): Int
}

interface Bindable<T> {
    fun bind(data: T)
}