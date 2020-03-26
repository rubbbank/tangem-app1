package com.tangem.tangemtest.ucase.variants.personalize.converter

import com.tangem.tangemtest._arch.structure.abstraction.Block
import com.tangem.tangemtest.ucase.variants.personalize.dto.TestJsonDto
import ru.dev.gbixahue.eu4d.lib.kotlin.common.Converter

/**
 * Created by Anton Zhilenkov on 19/03/2020.
 */
class BlockToJsonConverter : Converter<List<Block>, TestJsonDto> {
    override fun convert(from: List<Block>): TestJsonDto = TestJsonDto()
}