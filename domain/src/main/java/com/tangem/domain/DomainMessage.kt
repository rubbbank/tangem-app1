package com.tangem.domain

/**
 * Created by Anton Zhilenkov on 14/04/2022.
 */
sealed interface DomainMessage

sealed interface DomainNotification : DomainMessage {
    interface Toast : DomainNotification {}

    interface Snackbar : DomainNotification {}

    interface Dialog : DomainNotification {}

}