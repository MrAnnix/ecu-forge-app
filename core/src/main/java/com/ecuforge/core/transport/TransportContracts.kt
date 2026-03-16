package com.ecuforge.core.transport

/**
 * Transport endpoints supported by the ECU communication layer.
 */
sealed interface TransportEndpoint {
    /**
     * Bluetooth transport endpoint identified by a MAC address.
     */
    data class Bluetooth(val macAddress: String) : TransportEndpoint

    /**
     * USB transport endpoint identified by vendor and product IDs.
     */
    data class Usb(val vendorId: Int, val productId: Int) : TransportEndpoint
}

/**
 * High-level connection state for transport gateways.
 */
enum class TransportConnectionState {
    IDLE,
    CONNECTING,
    CONNECTED,
    DISCONNECTING,
    DISCONNECTED,
    ERROR,
}

/**
 * Canonical transport error codes used across modules.
 */
enum class TransportFailureCode {
    INVALID_ENDPOINT,
    CONNECTION_FAILED,
    TIMEOUT,
    IO_ERROR,
    NOT_CONNECTED,
    UNSUPPORTED_OPERATION,
}

/**
 * Detailed transport failure payload.
 */
data class TransportFailure(
    val code: TransportFailureCode,
    val message: String,
    val recoverable: Boolean,
)

/**
 * Typed result wrapper for transport operations.
 */
sealed interface TransportOperationResult<out T> {
    /**
     * Successful transport operation with typed value.
     */
    data class Success<T>(val value: T) : TransportOperationResult<T>

    /**
     * Failed transport operation with structured failure payload.
     */
    data class Failure(val error: TransportFailure) : TransportOperationResult<Nothing>

    /**
     * Factory helpers for transport operation results.
     */
    companion object {
        /**
         * Creates a successful operation result.
         */
        fun <T> success(value: T): TransportOperationResult<T> = Success(value)

        /**
         * Creates a failed operation result.
         */
        fun failure(
            code: TransportFailureCode,
            message: String,
            recoverable: Boolean = true,
        ): TransportOperationResult<Nothing> {
            return Failure(
                error =
                    TransportFailure(
                        code = code,
                        message = message,
                        recoverable = recoverable,
                    ),
            )
        }
    }
}
