/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.main

import com.twofasapp.core.android.ktx.runSafely
import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.Device
import com.twofasapp.core.common.coroutines.Dispatchers
import com.twofasapp.core.common.crypto.AndroidKeyStore
import com.twofasapp.core.common.crypto.SignatureVerifier
import com.twofasapp.core.common.ktx.decodeBase64
import com.twofasapp.core.common.ktx.encodeHex
import com.twofasapp.core.common.time.TimeProvider
import com.twofasapp.data.main.domain.BrowserRequestData
import com.twofasapp.data.main.domain.ConnectData
import com.twofasapp.data.main.domain.ConnectedBrowser
import com.twofasapp.data.main.domain.UpdateAppException
import com.twofasapp.data.main.local.ConnectedBrowsersLocalSource
import com.twofasapp.data.main.mapper.ConnectedBrowserMapper
import com.twofasapp.data.main.remote.BrowserRequestsRemoteSource
import com.twofasapp.data.main.remote.model.NotificationsJson
import com.twofasapp.data.settings.SessionRepository
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class BrowserExtensionRepositoryImpl(
    private val appBuild: AppBuild,
    private val dispatchers: Dispatchers,
    private val device: Device,
    private val androidKeyStore: AndroidKeyStore,
    private val timeProvider: TimeProvider,
    private val browserRequestsRemoteSource: BrowserRequestsRemoteSource,
    private val connectedBrowsersLocalSource: ConnectedBrowsersLocalSource,
    private val connectedBrowserMapper: ConnectedBrowserMapper,
    private val sessionRepository: SessionRepository,
) : BrowserExtensionRepository {

    private val browserConnectFlow: MutableSharedFlow<ConnectData?> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val browserRequestsFlow: MutableSharedFlow<BrowserRequestData?> = MutableSharedFlow(
        replay = 0,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    override fun observeConnect(): Flow<ConnectData> {
        return browserConnectFlow.filterNotNull()
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun publishConnect(connectData: ConnectData) {
        GlobalScope.launch {
            browserConnectFlow.emit(connectData)
        }
    }

    override fun observeRequests(): Flow<BrowserRequestData> {
        return browserRequestsFlow.filterNotNull()
    }

    override suspend fun fetchRequests() {
        withContext(dispatchers.io) {
            val response = browserRequestsRemoteSource.fetchNotifications(deviceId = device.uniqueId())

            if (shouldShowAppUpdate(response)) {
                throw UpdateAppException("You’re using an old app version. Update now to get the latest features and improvements.")
            }

            val notifications = response.notifications.orEmpty()
            val notification = notifications
                .maxByOrNull { it.data.timestamp }

            if (notification?.data?.messageType != "be_request") {
                return@withContext null
            }

            val browser = getConnectedBrowser(notification.data.pkPersBe.decodeBase64()) ?: return@withContext null

            val browserRequestData = BrowserRequestData(
                browser = browser,
                deviceId = device.uniqueId(),
                notificationId = notification.id,
                timestamp = notification.data.timestamp.toLong(),
                pkPersBe = notification.data.pkPersBe.decodeBase64(),
                pkEpheBe = notification.data.pkEpheBe.decodeBase64(),
                signature = notification.data.sigPush.decodeBase64(),
            )

            publishRequest(browserRequestData)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun deleteRequest(notificationId: String) {
        GlobalScope.launch {
            withContext(dispatchers.io) {
                runSafely {
                    browserRequestsRemoteSource.deleteNotification(
                        deviceId = device.uniqueId(),
                        notificationId = notificationId,
                    )
                }
            }
        }
    }

    override suspend fun publishRequest(requestData: BrowserRequestData) {
        if (checkIsRequestValid(requestData)) {
            browserRequestsFlow.emit(requestData)
        }
    }

    override suspend fun checkIsRequestValid(requestData: BrowserRequestData): Boolean {
        return withContext(dispatchers.io) {
            try {
                val verified = SignatureVerifier.verify(
                    key = requestData.pkPersBe,
                    data = requestData.data,
                    signature = requestData.signature,
                )

                if (verified.not()) {
                    Timber.d("Browser request has invalid signature (sessionId=${requestData.sessionId.encodeHex()})")
                }

                val expired = requestData.isExpired(timeProvider.currentTimeUtcInstant())

                if (expired) {
                    Timber.d("Browser request has expired")
                }

                verified && expired.not()
            } catch (e: Exception) {
                Timber.d("Browser request could not be validated: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    private suspend fun getConnectedBrowser(publicKey: ByteArray): ConnectedBrowser? {
        return connectedBrowsersLocalSource.getConnectedBrowsers()
            .map { connectedBrowserMapper.mapToDomain(entity = it, appKey = androidKeyStore.appKey) }
            .firstOrNull { it.publicKey.contentEquals(publicKey) }
    }

    private suspend fun shouldShowAppUpdate(response: NotificationsJson): Boolean {
        return response.notifications.isNullOrEmpty() &&
            (response.compatibility?.minimalAndroidVersion ?: 0) > appBuild.versionCode &&
            sessionRepository.getAppUpdatePrompted().not()
    }
}