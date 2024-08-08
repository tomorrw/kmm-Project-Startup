package com.tomorrow.kmmProjectStartup.domain.utils

expect class PhoneNumber(number: String?) {
    var number: String?

    fun getFormattedNumberInOriginalFormat(defaultCountryCode: String = "LB"): String?

    fun isValid(defaultCountryCode: String = "LB"): Boolean

    var region: Region?
}

data class Region(val code: Int, val alpha2Code: String)

fun PhoneNumber.isNotValid(): Boolean = !isValid()

