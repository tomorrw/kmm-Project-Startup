@file:OptIn(ExperimentalForeignApi::class)

package com.tomorrow.kmmProjectStartup.domain.utils


import cocoapods.libPhoneNumber_iOS.NBEPhoneNumberFormatINTERNATIONAL
import cocoapods.libPhoneNumber_iOS.NBPhoneNumber
import cocoapods.libPhoneNumber_iOS.NBPhoneNumberUtil
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSLocale
import platform.Foundation.NSLocaleCountryCode
import platform.Foundation.NSLocaleKey
import platform.Foundation.NSNumber
import platform.Foundation.currentLocale

actual class PhoneNumber actual constructor(number: String?) {
    private val phoneUtil = NBPhoneNumberUtil()
    private var phoneNumber: NBPhoneNumber? = number?.toNBPhoneNumberOrNull()

    @OptIn(ExperimentalForeignApi::class)
    actual var number: String? = number
        get() = phoneNumber?.let {
            phoneUtil.format(it, NBEPhoneNumberFormatINTERNATIONAL, null)
        } ?: field
        set(value) {
            phoneNumber = value?.toNBPhoneNumberOrNull()
            field = value
        }

    actual fun isValid(): Boolean =
        number?.toNBPhoneNumberOrNull()?.let { phoneUtil.isValidNumber(it) } ?: false

    actual var region: Region?
        get() {
            val code = phoneNumber?.countryCode ?: return null
            val alpha2Code = phoneUtil.getRegionCodeForCountryCode(code) ?: return null
            return Region(code = code.intValue, alpha2Code = alpha2Code)
        }
        set(value) {
            if (value != null) phoneNumber?.countryCode = NSNumber(value.code)
        }

    @OptIn(ExperimentalForeignApi::class)
    private fun String.toNBPhoneNumberOrNull(): NBPhoneNumber? {
        return try {
            phoneUtil.parseAndKeepRawInput(this, "lb", null)
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun getFormattedNumberInOriginalFormat(): String? =
        phoneNumber?.let {
            phoneUtil.formatInOriginalFormat(
                it,
                region?.alpha2Code ?: "lb",
                null
            )
        }
}

actual class Regions {
    actual companion object {
        private val phoneUtil = NBPhoneNumberUtil()

        actual val supportedRegions: List<Region>
            get() = (phoneUtil.getSupportedRegions() ?: listOf<String>())
                .filterIsInstance<String>()
                .mapNotNull {
                    if (it == "ZZ" || it.length != 2) null
                    else phoneUtil.getCountryCodeForRegion(it)
                        ?.let { code -> Region(code.intValue, it) }
                }


        actual fun getCountryName(code: String): String {
            val locale = NSLocale.currentLocale
            return locale.displayNameForKey(NSLocaleCountryCode, code) ?: code
        }
    }
}