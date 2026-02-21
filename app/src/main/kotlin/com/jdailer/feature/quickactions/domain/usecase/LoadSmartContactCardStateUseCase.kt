package com.jdailer.feature.quickactions.domain.usecase

import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.quickactions.domain.model.SmartContactCardState
import com.jdailer.feature.quickactions.domain.repository.SmartContactCardRepository
import com.jdailer.feature.quickactions.domain.usecase.GetAdvancedQuickActionsUseCase

class LoadSmartContactCardStateUseCase(
    private val smartContactCardRepository: SmartContactCardRepository,
    private val resolveActionsUseCase: GetAdvancedQuickActionsUseCase
) {
    suspend operator fun invoke(contact: UnifiedContact): SmartContactCardState {
        val actions = resolveActionsUseCase(contact)
        val tags = smartContactCardRepository.resolveTags(contact.contactId)
        val latestNote = smartContactCardRepository.resolveLatestNote(contact.contactId)

        return SmartContactCardState(
            contact = contact,
            actions = actions,
            tags = tags,
            latestNote = latestNote,
            isQuickActionAvailable = actions.isNotEmpty()
        )
    }
}
