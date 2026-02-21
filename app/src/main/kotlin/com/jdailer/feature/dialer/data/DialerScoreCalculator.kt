package com.jdailer.feature.dialer.data

import com.jdailer.core.database.entity.ContactSuggestionRow
import com.jdailer.feature.dialer.domain.model.DialerSuggestion

class DialerScoreCalculator {
    fun score(row: ContactSuggestionRow, query: String): Float {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            return if (row.isFavorite) 1f else 0f
        }

        var score = 0f
        val name = row.normalizedName.lowercase()
        val number = row.normalizedNumber
        val t9Digits = row.t9Digits
        when {
            name.startsWith(normalizedQuery) -> score += 1f
            name.contains(normalizedQuery) -> score += 0.7f
        }
        if (number.startsWith(normalizedQuery)) {
            score += 1f
        } else if (number.contains(normalizedQuery)) {
            score += 0.5f
        }
        if (t9Digits.startsWith(T9Query.toDigits(normalizedQuery))) {
            score += 0.9f
        }
        if (row.isFavorite) score += 0.6f
        return score
    }

    fun toSuggestion(row: ContactSuggestionRow, query: String): DialerSuggestion =
        DialerSuggestion(
            contactId = row.contactId,
            displayName = row.displayName,
            phoneNumber = row.phoneNumber,
            normalizedNumber = row.normalizedNumber,
            isFavorite = row.isFavorite,
            score = score(row, query),
        )
}
