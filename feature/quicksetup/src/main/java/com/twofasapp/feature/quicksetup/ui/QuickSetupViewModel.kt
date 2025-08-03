package com.twofasapp.feature.quicksetup.ui

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.settings.SessionRepository
import com.twofasapp.data.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

internal class QuickSetupViewModel(
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository,
    private val cloudRepository: CloudRepository,
) : ViewModel() {
    val uiState = MutableStateFlow(QuickSetupUiState())

    init {
        launchScoped {
            cloudRepository.observeSyncInfo().collect { syncInfo ->
                uiState.update { it.copy(syncEnabled = syncInfo.enabled) }
            }
        }

        launchScoped {
            settingsRepository.observeDefaultSecurityType().collect { securityType ->
                uiState.update { it.copy(securityType = securityType) }
            }
        }
    }

    fun markAsPrompted(onCompleted: () -> Unit) {
        launchScoped { sessionRepository.setQuickSetupPrompted(true) }
            .invokeOnCompletion { onCompleted() }
    }
}