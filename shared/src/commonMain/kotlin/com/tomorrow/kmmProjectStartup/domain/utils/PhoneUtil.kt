package com.tomorrow.kmmProjectStartup.domain.utils

expect class PhoneUtil {
    companion object {
        val supportedRegions: List<Region>
        fun getExamplePhoneNumberFor(region: Region): PhoneNumber?
    }
}