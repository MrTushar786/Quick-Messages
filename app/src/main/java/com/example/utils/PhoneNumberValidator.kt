package com.example.utils

object PhoneNumberValidator {

    /**
     * Cleans and validates a phone number.
     * Country codes should start with + or digits only.
     * International phone numbers have between 7 and 15 digits total.
     */
    fun validate(countryCode: String, phoneNumber: String): ValidationResult {
        val cleanCountry = countryCode.trim().filter { it.isDigit() }
        val cleanPhone = phoneNumber.trim().filter { it.isDigit() }

        if (cleanCountry.isEmpty()) {
            return ValidationResult.Invalid("Country code cannot be empty")
        }
        if (cleanCountry.length > 4) {
            return ValidationResult.Invalid("Country code is too long")
        }
        if (cleanPhone.isEmpty()) {
            return ValidationResult.Invalid("Phone number cannot be empty")
        }
        val totalDigits = cleanCountry.length + cleanPhone.length
        if (totalDigits < 7) {
            return ValidationResult.Invalid("Phone number is too short (min 7 digits)")
        }
        if (totalDigits > 15) {
            return ValidationResult.Invalid("Phone number is too long (max 15 digits)")
        }

        return ValidationResult.Valid("$cleanCountry$cleanPhone")
    }

    sealed class ValidationResult {
        data class Valid(val sanitizedE164: String) : ValidationResult()
        data class Invalid(val errorMessage: String) : ValidationResult()
    }
}
