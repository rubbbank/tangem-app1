package com.tangem.domain.common

/**
 * Created by Anton Zhilenkov on 29/03/2022.
 */
interface DomainError {
    val code: Int
    val message: String
    val data: Any?
}

open class AnyError(
    override val code: Int,
    override val message: String,
    override val data: Any? = null,
) : DomainError

interface ErrorConverter<T> {
    fun convertError(error: DomainError): T
}

interface Validator<Data, Error> {
    fun validate(data: Data? = null): Error?
}