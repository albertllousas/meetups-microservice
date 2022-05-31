package alo.meetups.infrastructure.adapters.output.http

data class HttpClientFailureException(
    val httpClient: String,
    val method: String,
    val path: String,
    val errorBody: String?,
    val httpStatus: Int,
) : RuntimeException("Http call with '$httpClient' to '$method $path' failed with status '$httpStatus' and body '$errorBody' ")
