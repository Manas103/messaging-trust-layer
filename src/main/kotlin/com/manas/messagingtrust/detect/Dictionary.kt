package com.manas.messagingtrust.detect

/**
 * Off-platform payment services and "take this off the app" phrases, in English, French and
 * Spanish (this project's non-English simulated locales). A single flat word list, because
 * {@code AhoCorasick} does not need per-language structure to match any of them in one pass.
 */
object Dictionary {
    val patterns: List<String> = listOf(
        // off-platform payment services
        "venmo", "paypal", "zelle", "cash app", "cashapp", "western union", "wise", "revolut",
        "wire transfer", "bank transfer", "virement bancaire", "transferencia bancaria",
        // off-platform messaging/contact
        "whatsapp", "telegram", "wechat", "signal app", "line app",
        // explicit redirect phrases
        "off the app", "off platform", "off-platform", "outside the app", "outside airbnb",
        "my number is", "call me at", "text me at", "contactez-moi au", "llámame al",
        "my email is", "mi correo es", "mon email est",
        // discount-for-off-platform-booking phrases (a classic fee-avoidance pattern)
        "pay me directly", "skip the fee", "avoid the fee", "book directly"
    )
}
