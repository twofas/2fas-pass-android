/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.connect.ui.requestmodal

import androidx.lifecycle.ViewModel
import com.twofasapp.core.android.ktx.launchScoped
import com.twofasapp.core.android.ktx.resumeIfActive
import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.locale.Strings
import com.twofasapp.data.main.BrowserExtensionRepository
import com.twofasapp.data.main.LoginsRepository
import com.twofasapp.data.main.TrashRepository
import com.twofasapp.data.main.VaultCryptoScope
import com.twofasapp.data.main.VaultsRepository
import com.twofasapp.data.main.domain.BrowserRequestAction
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.BrowserRequestResponse
import com.twofasapp.data.main.domain.RequestWebSocketResult
import com.twofasapp.data.main.mapper.ItemEncryptionMapper
import com.twofasapp.data.main.websocket.RequestWebSocket
import com.twofasapp.data.purchases.PurchasesRepository
import com.twofasapp.feature.connect.ui.requestmodal.states.AddLoginState
import com.twofasapp.feature.connect.ui.requestmodal.states.DeleteLoginState
import com.twofasapp.feature.connect.ui.requestmodal.states.PasswordRequestState
import com.twofasapp.feature.connect.ui.requestmodal.states.UpdateLoginState
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.suspendCancellableCoroutine

internal class RequestModalViewModel(
    private val strings: Strings,
    private val browserExtensionRepository: BrowserExtensionRepository,
    private val loginsRepository: LoginsRepository,
    private val vaultsRepository: VaultsRepository,
    private val trashRepository: TrashRepository,
    private val purchasesRepository: PurchasesRepository,
    private val vaultCryptoScope: VaultCryptoScope,
    private val requestWebSocket: RequestWebSocket,
    private val itemEncryptionMapper: ItemEncryptionMapper,
) : ViewModel() {

    val uiState = MutableStateFlow(RequestModalUiState())
    val passwordRequestState = MutableStateFlow(PasswordRequestState())
    val deleteLoginState = MutableStateFlow(DeleteLoginState())
    val addLoginState = MutableStateFlow(AddLoginState())
    val updateLoginState = MutableStateFlow(UpdateLoginState())

    fun connect(requestData: BrowserRequestData) {
        uiState.update {
            it.copy(
                notificationId = requestData.notificationId,
                browserExtensionName = requestData.browser.extensionName,
                browserIdenticon = requestData.browser.identicon,
                requestState = RequestState.InsideFrame.Loading,
            )
        }

        launchScoped {
            val result = requestWebSocket.open(
                requestData = requestData,
                onBrowserRequestAction = ::onBrowserRequestAction,
            )

            when (result) {
                is RequestWebSocketResult.Success -> {
                    uiState.update {
                        it.copy(
                            finishWithSuccess = true,
                        )
                    }
                }

                is RequestWebSocketResult.Failure -> {
                    launchScoped {
                        runSafely {
                            browserExtensionRepository.deleteRequest(notificationId = requestData.notificationId)
                        }

                        updateState(
                            RequestState.InsideFrame.Error(
                                title = strings.requestModalErrorGenericTitle,
                                subtitle = result.errorMessage.ifEmpty { strings.requestModalErrorGenericSubtitle },
                                cta = strings.requestModalErrorGenericCta,
                            ),
                        )
                    }
                }
            }
        }
    }

    private suspend fun onBrowserRequestAction(request: BrowserRequestAction): BrowserRequestResponse {
        updateState(
            when (request) {
                is BrowserRequestAction.PasswordRequest -> RequestState.InsideFrame.PasswordRequest
                is BrowserRequestAction.DeleteLogin -> RequestState.InsideFrame.DeleteLogin
                is BrowserRequestAction.AddLogin -> {
                    val maxItems = purchasesRepository.getSubscriptionPlan().entitlements.itemsLimit
                    val currentItems = loginsRepository.getLoginsCount()

                    if (currentItems >= maxItems) {
                        RequestState.InsideFrame.UpgradePlan(maxItems = maxItems)
                    } else {
                        RequestState.InsideFrame.AddLogin
                    }
                }

                is BrowserRequestAction.UpdateLogin -> RequestState.InsideFrame.UpdateLogin
            },
        )

        return suspendCancellableCoroutine { continuation ->
            when (request) {
                is BrowserRequestAction.PasswordRequest -> {
                    launchScoped {
                        passwordRequestState.update { state ->
                            state.copy(
                                login = request.login,
                                onSendPasswordClick = { password ->
                                    continuation.sendResponse(BrowserRequestResponse.PasswordRequestAccept(password))
                                },
                                onCancelClick = {
                                    continuation.sendResponse(BrowserRequestResponse.Cancel)
                                },
                            )
                        }
                    }
                }

                is BrowserRequestAction.DeleteLogin -> {
                    launchScoped {
                        deleteLoginState.update { state ->
                            state.copy(
                                login = request.login,
                                onDeleteClick = {
                                    launchScoped {
                                        updateState(RequestState.InsideFrame.Loading)

                                        trashRepository.trash(request.login.id)

                                        continuation.sendResponse(BrowserRequestResponse.DeleteLoginAccept)
                                    }
                                },
                                onCancelClick = {
                                    continuation.sendResponse(BrowserRequestResponse.Cancel)
                                },
                            )
                        }
                    }
                }

                is BrowserRequestAction.AddLogin -> {
                    launchScoped {
                        addLoginState.update { state ->
                            state.copy(
                                login = request.login,
                                onContinueClick = {
                                    updateState(
                                        RequestState.FullSize.LoginForm(
                                            login = request.login,
                                            onCancel = {
                                                updateState(RequestState.InsideFrame.AddLogin)
                                            },
                                            onSaveClick = { login ->
                                                launchScoped {
                                                    val vaultId = vaultsRepository.getVault().id

                                                    vaultCryptoScope.withVaultCipher(vaultId) {
                                                        val loginId = loginsRepository.saveLogin(
                                                            login
                                                                .copy(vaultId = vaultId)
                                                                .let { itemEncryptionMapper.encryptLogin(it, this) },
                                                        )

                                                        updateState(RequestState.InsideFrame.Loading)

                                                        val updatedLogin = loginsRepository.getLogin(loginId).let {
                                                            itemEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                                                        }

                                                        continuation.sendResponse(BrowserRequestResponse.AddLoginAccept(updatedLogin!!))
                                                    }
                                                }
                                            },
                                        ),
                                    )
                                },
                                onCancelClick = {
                                    continuation.sendResponse(BrowserRequestResponse.Cancel)
                                },
                            )
                        }
                    }
                }

                is BrowserRequestAction.UpdateLogin -> {
                    launchScoped {
                        updateLoginState.update { state ->
                            state.copy(
                                login = request.login,
                                onContinueClick = {
                                    updateState(
                                        RequestState.FullSize.LoginForm(
                                            login = request.updatedLogin,
                                            onCancel = {
                                                updateState(RequestState.InsideFrame.UpdateLogin)
                                            },
                                            onSaveClick = { login ->
                                                launchScoped {
                                                    vaultCryptoScope.withVaultCipher(login.vaultId) {
                                                        val loginId = loginsRepository.saveLogin(
                                                            itemEncryptionMapper.encryptLogin(login, this),
                                                        )

                                                        updateState(RequestState.InsideFrame.Loading)

                                                        val updatedLogin = loginsRepository.getLogin(loginId).let {
                                                            itemEncryptionMapper.decryptLogin(it, this, decryptPassword = true)
                                                        }

                                                        continuation.sendResponse(BrowserRequestResponse.UpdateLoginAccept(updatedLogin!!))
                                                    }
                                                }
                                            },
                                        ),
                                    )
                                },
                                onCancelClick = {
                                    continuation.sendResponse(BrowserRequestResponse.Cancel)
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    fun deleteRequest() {
        uiState.value.notificationId?.let {
            browserExtensionRepository.deleteRequest(notificationId = it)
        }
    }

    private fun updateState(state: RequestState) {
        uiState.update { it.copy(requestState = state) }
    }

    private fun CancellableContinuation<BrowserRequestResponse>.sendResponse(response: BrowserRequestResponse) {
        uiState.update { it.copy(selectedResponse = response) }
        resumeIfActive(response)
    }
}