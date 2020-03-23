package com.tangem.tangemtest._arch.structure.impl

import com.tangem.tangemtest._arch.structure.base.BaseUnitViewModel

/**
 * Created by Anton Zhilenkov on 22/03/2020.
 */
class StringViewModel(value: String?) : BaseUnitViewModel<String>(value)
class NumberViewModel(value: Number?) : BaseUnitViewModel<Number>(value)
class BoolViewModel(value: Boolean?) : BaseUnitViewModel<Boolean>(value)
class ListViewModel(value: ModelHelper?) : BaseUnitViewModel<ModelHelper>(value)

class KeyValue(val key: String, val value: Any)
class ModelHelper(var selectedItem: Any, val itemList: List<KeyValue>)
