package com.ecuforge.core.transport

sealed interface TransportEndpoint {
    data class Bluetooth(val macAddress: String) : TransportEndpoint
    data class Usb(val vendorId: Int, val productId: Int) : TransportEndpoint
}

enum class TransportConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR
}

enum class TransportFailureCode {
    INVALID_ENDPOINT,
    CONNECTION_FAILED,
    TIMEOUT,
    IO_ERROR,
    NOT_CONNECTED,
    UNSUPPORTED_OPERATION
}

data class TransportFailure(
    val code: TransportFailureCode,
    val message: String,
    val recoverable: Boolean
)

sealed interface TransportOperationResult<out T> {
    data class Success<T>(val value: T) : TransportOperationResult<T>
    data class Failure(val error: TransportFailure) : TransportOperationResult<Nothing>

    companion object {
        fun <T> success(value: T): TransportOperationResult<T> = Success(value)

        fun failure(
            code: TransportFailureCode,
            message: String,
            recoverable: Boolean = true
        ): TransportOperationResult<Nothing> {
            return Failure(
                error = TransportFailure(
                    code = code,
                    message = message,
                    recoverable = recoverable
                )
            )
        }
    }
}
