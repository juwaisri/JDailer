package com.jdailer.feature.dialer.data

object T9Query {
    private val t9Map = mapOf(
        '2' to "abc",
        '3' to "def",
        '4' to "ghi",
        '5' to "jkl",
        '6' to "mno",
        '7' to "pqrs",
        '8' to "tuv",
        '9' to "wxyz"
    )

    fun toDigits(input: String): String {
        val normalized = input.trim().lowercase()
        if (normalized.isBlank()) return ""
        return buildString {
            normalized.forEach { c ->
                when {
                    c.isDigit() -> append(c)
                    c.isLetter() -> {
                        val digit = t9Map.entries.firstOrNull { entry ->
                            entry.value.contains(c)
                        }?.key
                        if (digit != null) append(digit)
                    }
                    else -> Unit
                }
            }
        }
    }

    fun normalizeName(input: String): String =
        input.trim()
            .lowercase()
            .replace(Regex("[^a-z0-9]+"), "")
}
