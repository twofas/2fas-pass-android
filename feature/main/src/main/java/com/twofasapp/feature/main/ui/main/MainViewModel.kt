/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.main.ui.main

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.auth.AuthStatusTracker
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.data.main.BrowserExtensionRepository
import com.twofasapp.data.main.CloudRepository
import com.twofasapp.data.main.ConnectedBrowsersRepository
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.CloudSyncStatus
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.data.push.PushRepository
import com.twofasapp.data.push.domain.Push
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update

internal class MainViewModel(
    private val deeplinks: Deeplinks,
    private val device: Device,
    private val authStatusTracker: AuthStatusTracker,
    private val cloudRepository: CloudRepository,
    private val pushRepository: PushRepository,
    private val connectedBrowsersRepository: ConnectedBrowsersRepository,
    private val browserExtensionRepository: BrowserExtensionRepository,
    private val purchasesRepository: PurchasesRepository,
) : ViewModel() {

    val uiState = MutableStateFlow(MainUiState())

    private var observeLocalPushesJob: Job? = null
    private var fetchNotificationsJob: Job? = null
    private var syncJob: Job? = null

    init {
        launchScoped {
            deeplinks.observePendingDeeplink().collect {
                publishEvent(MainUiEvent.OpenDeeplink(it))
                deeplinks.clearPendingDeeplink()
            }
        }

        launchScoped {
            cloudRepository.observeSyncStatus().collect { syncStatus ->
                if (cloudRepository.getSyncInfo().enabled && syncStatus is CloudSyncStatus.Error) {
                    uiState.update { it.copy(cloudSyncError = true) }
                } else {
                    uiState.update { it.copy(cloudSyncError = false) }
                }
            }
        }

        launchScoped {
            browserExtensionRepository.observeConnect().collect { connect ->
                publishEvent(MainUiEvent.ShowBrowserConnect(connect))
            }
        }

        launchScoped {
            browserExtensionRepository.observeRequests().collect { request ->
                publishEvent(MainUiEvent.ShowBrowserRequest(request))
            }
        }
    }

    fun startObservingLocalPushes() {
        observeLocalPushesJob?.cancel()
        observeLocalPushesJob = launchScoped {
            pushRepository.observePushesOnLocalChannel().collect { push ->
                when (push) {
                    is Push.BrowserRequest -> {
                        connectedBrowsersRepository.getBrowser(push.pkPersBe.decodeBase64())?.let { browser ->
                            browserExtensionRepository.publishRequest(
                                BrowserRequestData(
                                    browser = browser,
                                    deviceId = device.uniqueId(),
                                    notificationId = push.notificationId,
                                    timestamp = push.timestamp.toEpochMilli(),
                                    pkPersBe = push.pkPersBe.decodeBase64(),
                                    pkEpheBe = push.pkEpheBe.decodeBase64(),
                                    signature = push.sigPush.decodeBase64(),
                                ),
                            )
                        }
                    }
                }
            }
        }
    }

    fun stopObservingLocalPushes() {
        observeLocalPushesJob?.cancel()
    }

    fun fetchBrowserRequests() {
        fetchNotificationsJob?.cancel()

        fetchNotificationsJob = launchScoped {
            authStatusTracker.observeIsAuthenticated().collect { isAuthenticated ->
                if (isAuthenticated) {
                    runSafely { browserExtensionRepository.fetchRequests() }
                        .onSuccess { fetchNotificationsJob?.cancel() }
                        .onFailure { fetchNotificationsJob?.cancel() }
                }
            }
        }
    }

    fun fetchSubscriptionInfo() {
        launchScoped {
            runSafely { purchasesRepository.fetchSubscriptionInfo() }
        }
    }

    fun sync() {
        syncJob?.cancel()

        syncJob = launchScoped {
            authStatusTracker.observeIsAuthenticated().distinctUntilChanged().collect { isAuthenticated ->
                if (isAuthenticated && cloudRepository.getSyncInfo().lastSuccessfulSyncTime > 0) {
                    runSafely { cloudRepository.sync() }
                        .onSuccess { syncJob?.cancel() }
                        .onFailure { syncJob?.cancel() }
                }
            }
        }
    }

    fun consumeEvent(event: MainUiEvent) {
        uiState.update { it.copy(events = it.events.minus(event)) }
    }

    private fun publishEvent(event: MainUiEvent) {
        uiState.update { it.copy(events = it.events.plus(event)) }
    }
}