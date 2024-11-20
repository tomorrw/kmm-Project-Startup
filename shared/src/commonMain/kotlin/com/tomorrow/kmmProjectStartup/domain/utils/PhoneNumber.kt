package com.tomorrow.kmmProjectStartup.domain.utils

expect class PhoneNumber(number: String?, regionCode: String = "LB") {
    var number: String?

    fun getFormattedNumberInOriginalFormat(): String?

    fun isValid(): Boolean

    var region: Region?
}

data class Region(val code: Int, val alpha2Code: String) {
    fun getFlagEmoji(): String = alpha2Code
        .uppercase()
        .split("")
        .filter { it.isNotBlank() }
        .map { getCodePointAt(it, 0) + 0x1F1A5 }
        .joinToString("") { codePointToString(it) }
}

fun PhoneNumber.isNotValid(): Boolean = !isValid()

expect class Regions {
    companion object {
        fun getCountryName(code: String): String

        val supportedRegions: List<Region>
    }
}

fun getCodePointAt(str: String, index: Int): Int {
    if (index < 0 || index >= str.length) throw IndexOutOfBoundsException("Invalid index")
    return str[index].code
}
fun codePointToString(codePoint: Int): String {
    return when {
        codePoint <= 0xFFFF -> codePoint.toChar().toString()
        else -> {
            // Calculate high and low surrogates manually
            val highSurrogate = ((codePoint - 0x10000) / 0x400 + 0xD800).toChar()
            val lowSurrogate = ((codePoint - 0x10000) % 0x400 + 0xDC00).toChar()
            "$highSurrogate$lowSurrogate"
        }
    }
}
