package com.jdailer.feature.voip.domain.usecase

import com.jdailer.feature.voip.data.telecom.TelecomRoleManager

class ResolveTelecomRoleUseCase(
    private val roleManager: TelecomRoleManager
) {
    operator fun invoke(): Boolean = roleManager.isDefaultDialerRoleActive()
}
