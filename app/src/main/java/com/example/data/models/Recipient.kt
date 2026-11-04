package com.example.data.models

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Recipient(
    val id: String = "default_recipient",
    val name: String = "Default Recipient",
    val countryCode: String = "+91",
    val phoneNumber: String = "8864846795"
) {
    /**
     * Sanitized international phone number without '+' or special characters,
     * suitable for https://wa.me/{number} format.
     */
    val cleanFullNumber: String
        get() {
            val cleanCountryCode = countryCode.filter { it.isDigit() }
            val cleanNumber = phoneNumber.filter { it.isDigit() }
            return "$cleanCountryCode$cleanNumber"
        }

    val displayFormatted: String
        get() {
            val code = if (countryCode.startsWith("+")) countryCode else "+$countryCode"
            return "$code $phoneNumber"
        }

    fun isValid(): Boolean {
        val clean = cleanFullNumber
        // International E.164 phone numbers have 7 to 15 digits
        return clean.length in 7..15
    }
}
