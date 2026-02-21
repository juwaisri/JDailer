package com.jdailer.feature.quickactions.domain.usecase

import com.jdailer.feature.contacts.domain.model.UnifiedContact
import com.jdailer.feature.quickactions.domain.model.SmartActionType
import com.jdailer.feature.quickactions.domain.repository.SmartContactCardRepository
import com.jdailer.feature.quickactions.domain.model.SmartAction

class GetAdvancedQuickActionsUseCase(
    private val smartContactCardRepository: SmartContactCardRepository
) {
    suspend operator fun invoke(contact: UnifiedContact): List<SmartAction> {
        val actions = smartContactCardRepository.resolveActions(contact)
        return actions
            .sortedBy { it.sortOrder }
            .map {
                SmartAction(
                    actionId = it.actionId,
                    label = it.label,
                    type = it.type,
                    sortOrder = it.sortOrder
                )
            }
    }
}
