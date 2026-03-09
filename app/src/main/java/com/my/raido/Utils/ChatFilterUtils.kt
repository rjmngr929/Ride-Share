package com.my.raidocaption.Utils

object ChatFilterUtils {

    // 🚫 List of abusive/offensive words (extendable)
    private val badWords = listOf(
        // English abuses
        "fuck", "shit", "bitch", "bastard", "asshole",
        "slut", "dick", "damn", "crap", "retard",
        "motherfucker", "son of a bitch", "nigga",

        // Hindi abuses (Roman script)
        "chutiya", "bhosdi", "madarchod", "behenchod", "gaand",
        "lund", "randi", "harami", "kutta", "gandu",
        "chodu", "chod", "bhen ke", "maa ke", "launda",
        "tatti", "chootia", "bkl", "bc", "mc"
    )

    // ✅ Check if message is clean
    fun isValidMessage(msg: String): Boolean {
        val trimmedMsg = msg.trim()
        return trimmedMsg.isNotEmpty()
                && trimmedMsg.length <= 500
                && badWords.none { trimmedMsg.contains(it, ignoreCase = true) }
                && trimmedMsg.matches(Regex("^[\\p{Print}\\p{Space}]+$"))
    }

    // ⭐ Sanitize bad words with ***
    fun sanitizeMessage(msg: String): String {
        var cleanMsg = msg
        badWords.forEach {
            cleanMsg = cleanMsg.replace(it.toRegex(RegexOption.IGNORE_CASE), "***")
        }
        return cleanMsg
    }
}
