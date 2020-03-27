package com.tangem.tangemtest._arch.structure

/**
 * Created by Anton Zhilenkov on 22/03/2020.
 */
interface DataHolder<D> {
    var viewModel: D
}

typealias Payload = MutableMap<String, Any?>

interface PayloadHolder {
    val payload: Payload
}

interface ItemListHolder<I> {
    fun setItems(list: MutableList<I>)
    fun getItems(): MutableList<I>
    fun addItem(item: I)
    fun removeItem(item: I)
    fun clear()
}