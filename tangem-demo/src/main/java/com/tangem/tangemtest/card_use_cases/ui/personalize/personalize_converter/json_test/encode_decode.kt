package com.tangem.tangemtest.card_use_cases.ui.personalize.personalize_converter.json_test

import com.tangem.tangemtest._arch.structure.base.Block

/**
 * Created by Anton Zhilenkov on 20/03/2020.
 */
interface EncodeDecode<A, B> {
    fun decode(from: A): B
    fun encode(from: B): A
}

class JsonBlockEnDe(
        private val jsonToBlock: JsonToBlockConverter,
        private val blockToJson: BlockToJsonConverter
) : EncodeDecode<TestJsonDto, List<Block>> {

    override fun encode(from: List<Block>): TestJsonDto = blockToJson.convert(from)

    override fun decode(from: TestJsonDto): List<Block> = jsonToBlock.convert(from)
}