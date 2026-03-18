package com.ecuforge.core.transport

/**
 * Transport endpoints supported by the ECU communication layer.
 */
sealed interface TransportEndpoint {
    /**
     * Bluetooth transport endpoint identified by a MAC address.
     *
     * @property macAddress Bluetooth adapter MAC address used for transport connection.
     */
    data class Bluetooth(val macAddress: String) : TransportEndpoint

    /**
     * USB transport endpoint identified by vendor and product IDs.
     *
     * @property vendorId USB vendor identifier.
     * @property productId USB product identifier.
     */
    data class Usb(val vendorId: Int, val productId: Int) : TransportEndpoint
}

/**
 * High-level connection state for transport gateways.
 *
 * @property IDLE Gateway is idle with no active connection attempt.
 * @property CONNECTING Gateway is currently establishing connection.
 * @property CONNECTED Gateway is connected and ready for I/O.
 * @property DISCONNECTING Gateway is currently closing connection.
 * @property DISCONNECTED Gateway was disconnected after an active session.
 * @property ERROR Gateway entered error state due to transport failure.
 */
enum class TransportConnectionState {
    /** Gateway is idle with no active connection attempt. */
    IDLE,

    /** Gateway is currently establishing connection. */
    CONNECTING,

    /** Gateway is connected and ready for I/O. */
    CONNECTED,

    /** Gateway is currently closing connection. */
    DISCONNECTING,

    /** Gateway was disconnected after an active session. */
    DISCONNECTED,

    /** Gateway entered error state due to transport failure. */
    ERROR,
}

/**
 * Canonical transport error codes used across modules.
 *
 * @property INVALID_ENDPOINT Provided endpoint data is invalid.
 * @property CONNECTION_FAILED Connection could not be established.
 * @property TIMEOUT Operation timed out.
 * @property IO_ERROR Generic transport input/output failure.
 * @property NOT_CONNECTED Operation requires an active connection.
 * @property UNSUPPORTED_OPERATION Operation is not supported by adapter or scenario.
 */
enum class TransportFailureCode {
    /** Provided endpoint data is invalid. */
    INVALID_ENDPOINT,

    /** Connection could not be established. */
    CONNECTION_FAILED,

    /** Operation timed out. */
    TIMEOUT,

    /** Generic transport input/output failure. */
    IO_ERROR,

    /** Operation requires an active connection. */
    NOT_CONNECTED,

    /** Operation is not supported by adapter or scenario. */
    UNSUPPORTED_OPERATION,
}

/**
 * Detailed transport failure payload.
 *
 * @property code Canonical transport failure code.
 * @property message Human-readable failure reason.
 * @property recoverable True when caller can retry safely.
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
     *
     * @property value Typed operation result payload.
     */
    data class Success<T>(val value: T) : TransportOperationResult<T>

    /**
     * Failed transport operation with structured failure payload.
     *
     * @property error Structured transport failure payload.
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
