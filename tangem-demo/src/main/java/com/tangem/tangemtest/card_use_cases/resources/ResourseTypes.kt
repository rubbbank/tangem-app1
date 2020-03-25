package com.tangem.tangemtest.card_use_cases.resources

/**
 * Created by Anton Zhilenkov on 25/03/2020.
 */
open class Resources(
        val resName: Int,
        val resDescription: Int? = null
)

class ActionRes(
        resName: Int,
        resDescription: Int? = null,
        val resNavigation: Int? = null
) : Resources(resName, resDescription)