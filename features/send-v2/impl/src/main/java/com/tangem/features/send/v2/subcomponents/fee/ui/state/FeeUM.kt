package com.tangem.features.send.v2.subcomponents.fee.ui.state

import androidx.compose.runtime.Stable
import com.tangem.common.ui.notifications.NotificationUM
import com.tangem.domain.appcurrency.model.AppCurrency
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.math.BigDecimal

@Stable
internal sealed class FeeUM {

    abstract val isPrimaryButtonEnabled: Boolean

    data class Content(
        override val isPrimaryButtonEnabled: Boolean,
        val feeSelectorUM: FeeSelectorUM,
        val rate: BigDecimal?,
        val appCurrency: AppCurrency,
        val isFeeConvertibleToFiat: Boolean,
        val isFeeApproximate: Boolean,
        val isCustomSelected: Boolean,
        val isTronToken: Boolean,
        val customValues: ImmutableList<CustomFeeFieldUM> = persistentListOf(),
        val notifications: ImmutableList<NotificationUM>,
        val isEditingDisabled: Boolean = false,
    ) : FeeUM()

    data class Empty(
        override val isPrimaryButtonEnabled: Boolean = false,
    ) : FeeUM()
}
