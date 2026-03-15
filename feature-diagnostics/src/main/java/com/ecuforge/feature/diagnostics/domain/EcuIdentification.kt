package com.ecuforge.feature.diagnostics.domain

data class EcuIdentification(
    val model: String,
    val firmwareVersion: String,
    val serialNumber: String
)

data class IdentificationRequest(
    val ecuFamily: String,
    val endpointHint: String
)
