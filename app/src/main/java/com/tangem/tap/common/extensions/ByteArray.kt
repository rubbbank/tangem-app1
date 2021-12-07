package com.tangem.tap.common.extensions

/**
 * Created by Anton Zhilenkov on 05/12/2021.
 */

data class ByteArrayKey(val bytes: ByteArray) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ByteArrayKey && this.bytes.contentEquals(other.bytes)
    }

    override fun hashCode(): Int = bytes.contentHashCode()
}

fun ByteArray.toMapKey(): ByteArrayKey = ByteArrayKey(this)

fun ByteArrayKey.bytes(): ByteArray = this.bytes