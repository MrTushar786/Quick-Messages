package com.example

import com.example.data.models.Recipient
import com.example.utils.PhoneNumberValidator
import com.example.utils.WhatsAppHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class QuickMessageUnitTest {

    @Test
    fun testPhoneNumberValidation_validNumbers() {
        val defaultValidation = PhoneNumberValidator.validate("+91", "8864846795")
        assertTrue(defaultValidation is PhoneNumberValidator.ValidationResult.Valid)
        assertEquals("918864846795", (defaultValidation as PhoneNumberValidator.ValidationResult.Valid).sanitizedE164)

        val usValidation = PhoneNumberValidator.validate("+1", "4155552671")
        assertTrue(usValidation is PhoneNumberValidator.ValidationResult.Valid)
        assertEquals("14155552671", (usValidation as PhoneNumberValidator.ValidationResult.Valid).sanitizedE164)
    }

    @Test
    fun testPhoneNumberValidation_invalidNumbers() {
        val emptyPhone = PhoneNumberValidator.validate("+91", "")
        assertTrue(emptyPhone is PhoneNumberValidator.ValidationResult.Invalid)

        val emptyCode = PhoneNumberValidator.validate("", "8864846795")
        assertTrue(emptyCode is PhoneNumberValidator.ValidationResult.Invalid)

        val tooShort = PhoneNumberValidator.validate("+91", "12")
        assertTrue(tooShort is PhoneNumberValidator.ValidationResult.Invalid)
    }

    @Test
    fun testRecipient_cleanFullNumber() {
        val recipient = Recipient(
            countryCode = "+91",
            phoneNumber = "8864-846-795"
        )
        assertEquals("918864846795", recipient.cleanFullNumber)
        assertTrue(recipient.isValid())
    }

    @Test
    fun testWhatsAppHelper_buildWhatsAppUri_encoding() {
        val uri1 = WhatsAppHelper.buildWhatsAppUri("918864846795", "Nikal raha")
        assertEquals("https://wa.me/918864846795?text=Nikal%20raha", uri1.toString())

        val uri2 = WhatsAppHelper.buildWhatsAppUri("918864846795", "Poch Gaya")
        assertEquals("https://wa.me/918864846795?text=Poch%20Gaya", uri2.toString())

        val uriSpecial = WhatsAppHelper.buildWhatsAppUri("918864846795", "I'm leaving! & on my way")
        assertEquals("wa.me", uriSpecial.authority)
        assertEquals("/918864846795", uriSpecial.path)
        assertEquals("I'm leaving! & on my way", uriSpecial.getQueryParameter("text"))
    }

    @Test
    fun testGalleryHelper_permissions() {
        val permissions = com.example.utils.GalleryHelper.getRequiredPermissions()
        assertTrue(permissions.isNotEmpty())
        assertTrue(permissions.all { it.startsWith("android.permission.") })
    }

    @Test
    fun testDispatchModes_defined() {
        val modes = com.example.data.models.DispatchMode.values()
        assertTrue(modes.contains(com.example.data.models.DispatchMode.WHATSAPP_AUTO))
        assertTrue(modes.contains(com.example.data.models.DispatchMode.DIRECT_SMS))
        assertTrue(modes.contains(com.example.data.models.DispatchMode.WHATSAPP_MANUAL))
    }
}

