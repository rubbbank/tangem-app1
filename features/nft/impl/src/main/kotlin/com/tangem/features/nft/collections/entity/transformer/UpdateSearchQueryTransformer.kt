package com.tangem.features.nft.collections.entity.transformer

import com.tangem.features.nft.collections.entity.NFTCollectionsStateUM
import com.tangem.features.nft.collections.entity.NFTCollectionsUM
import com.tangem.utils.transformer.Transformer

internal class UpdateSearchQueryTransformer(private val newQuery: String) : Transformer<NFTCollectionsStateUM> {

    override fun transform(prevState: NFTCollectionsStateUM): NFTCollectionsStateUM = prevState.copy(
        content = when (val content = prevState.content) {
            is NFTCollectionsUM.Content -> content.copy(
                search = content.search.copy(
                    query = newQuery,
                ),
            )
            is NFTCollectionsUM.Empty,
            is NFTCollectionsUM.Loading,
            is NFTCollectionsUM.Failed,
            -> content
        },
    )
}
