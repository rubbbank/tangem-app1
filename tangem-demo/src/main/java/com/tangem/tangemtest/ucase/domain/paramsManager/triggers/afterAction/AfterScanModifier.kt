package com.tangem.tangemtest.ucase.domain.paramsManager.triggers.afterAction

import com.tangem.tangemtest._arch.structure.abstraction.Item
import com.tangem.tangemtest.ucase.domain.paramsManager.findDataParameter
import com.tangem.tangemtest.ucase.variants.TlvId
import com.tangem.tasks.ScanEvent
import com.tangem.tasks.TaskEvent

/**
 * Created by Anton Zhilenkov on 19/03/2020.
 */
class AfterScanModifier : AfterActionModification {
    override fun modify(taskEvent: TaskEvent<*>, paramsList: List<Item>): List<Item> {
        val parameter = paramsList.findDataParameter(TlvId.CardId) ?: return listOf()

        return if (taskEvent is TaskEvent.Event && taskEvent.data is ScanEvent.OnReadEvent) {
            parameter.viewModel.data = (taskEvent.data as ScanEvent.OnReadEvent).card.cardId
            listOf(parameter)
        } else listOf()
    }

}