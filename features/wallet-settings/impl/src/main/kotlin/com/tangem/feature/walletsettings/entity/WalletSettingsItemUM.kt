package com.tangem.feature.walletsettings.entity

import androidx.compose.runtime.Immutable
import com.tangem.core.ui.components.block.model.BlockUM
import com.tangem.core.ui.extensions.TextReference
import kotlinx.collections.immutable.ImmutableList

@Immutable
internal sealed class WalletSettingsItemUM {

    abstract val id: String

    data class WithItems(
        override val id: String,
        val description: TextReference,
        val blocks: ImmutableList<BlockUM>,
    ) : WalletSettingsItemUM()

    data class WithSwitch(
        override val id: String,
        val title: TextReference,
        val isChecked: Boolean,
        val onCheckedChange: (Boolean) -> Unit,
    ) : WalletSettingsItemUM()

    data class WithText(
        override val id: String,
        val title: TextReference,
        val text: TextReference,
        val isEnabled: Boolean,
        val onClick: () -> Unit,
    ) : WalletSettingsItemUM()
}
