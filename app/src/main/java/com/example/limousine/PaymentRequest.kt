package com.example.limousine

data class PaymentRequest(
    val userSecretKey: String,
    val categoryCode: String,
    val billName: String,
    val billDescription: String,
    val billpaymentAmount: String
)