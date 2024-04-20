package com.tomorrow.kmmProjectStartup.domain.model

data class SocialLink(val url: String, val platform: Platform) {
    enum class Platform {
        WhatsApp,
        Instagram,
        Facebook,
        LinkedIn,
        Youtube,
        Twitter,
        Website,
        Tiktok,
        Phone
    }
}
