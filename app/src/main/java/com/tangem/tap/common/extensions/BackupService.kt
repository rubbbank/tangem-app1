package com.tangem.tap.common.extensions

import com.tangem.domain.common.SaltPayWorkaround
import com.tangem.operations.backup.BackupService

/**
 * Created by Anton Zhilenkov on 26.10.2022.
 */
fun BackupService.primaryCardIsSaltPayVisa(): Boolean {
    return primaryCardId?.slice(0..3)?.let {
        SaltPayWorkaround.isVisaBatchId(it)
    } ?: false
}
