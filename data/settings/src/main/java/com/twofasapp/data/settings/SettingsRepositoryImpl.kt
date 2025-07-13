/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.data.settings

import com.twofasapp.core.common.build.AppBuild
import com.twofasapp.core.common.build.BuildVariant
import com.twofasapp.core.common.domain.PasswordGeneratorSettings
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.common.domain.SelectedTheme
import com.twofasapp.core.common.storage.DataStoreOwner
import com.twofasapp.core.common.storage.booleanPref
import com.twofasapp.core.common.storage.enumPref
import com.twofasapp.core.common.storage.intPref
import com.twofasapp.core.common.storage.serializedPrefNullable
import com.twofasapp.data.settings.domain.AppLockAttempts
import com.twofasapp.data.settings.domain.AppLockTime
import com.twofasapp.data.settings.domain.AutofillLockTime
import com.twofasapp.data.settings.domain.AutofillSettings
import com.twofasapp.data.settings.domain.LoginClickAction
import com.twofasapp.data.settings.domain.SortingMethod
import com.twofasapp.data.settings.local.model.AppLockAttemptsEntity
import com.twofasapp.data.settings.local.model.AppLockTimeEntity
import com.twofasapp.data.settings.local.model.AutofillLockTimeEntity
import com.twofasapp.data.settings.local.model.LoginClickActionEntity
import com.twofasapp.data.settings.local.model.PasswordGeneratorSettingsEntity
import com.twofasapp.data.settings.local.model.SelectedThemeEntity
import com.twofasapp.data.settings.local.model.SortingMethodEntity
import com.twofasapp.data.settings.mapper.asDomain
import com.twofasapp.data.settings.mapper.asEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

internal class SettingsRepositoryImpl(
    private val appBuild: AppBuild,
    dataStoreOwner: DataStoreOwner,
) : SettingsRepository, DataStoreOwner by dataStoreOwner {

    private val selectedTheme by enumPref(cls = SelectedThemeEntity::class.java, default = SelectedThemeEntity.Auto)
    private val dynamicColors by booleanPref(default = false)
    private val loginClickAction by enumPref(cls = LoginClickActionEntity::class.java, default = LoginClickActionEntity.View)
    private val autofillUseInline by booleanPref(default = true)
    private val sortingMethod by enumPref(cls = SortingMethodEntity::class.java, default = SortingMethodEntity.NameAsc)
    private val appLockTime by enumPref(
        cls = AppLockTimeEntity::class.java,
        default = AppLockTimeEntity.Seconds30,
        encrypted = true,
    )
    private val appLockAttempts by enumPref(
        cls = AppLockAttemptsEntity::class.java,
        default = AppLockAttemptsEntity.Count3,
        encrypted = true,
    )
    private val autofillLockTime by enumPref(
        cls = AutofillLockTimeEntity::class.java,
        default = AutofillLockTimeEntity.Hour1,
        encrypted = true,
    )
    private val defaultSecurityType by intPref(default = 2, encrypted = true)
    private val sendCrashLogs by booleanPref(true)
    private val screenCapture by booleanPref(
        default = false,
        encrypted = true,
    )
    private val passwordGeneratorSettings by serializedPrefNullable(
        serializer = PasswordGeneratorSettingsEntity.serializer(),
    )

    override fun observeSelectedTheme(): Flow<SelectedTheme> {
        return selectedTheme.asFlow().map { it.asDomain() }
    }

    override suspend fun setSelectedTheme(theme: SelectedTheme) {
        selectedTheme.set(theme.asEntity())
    }

    override fun observeDynamicColors(): Flow<Boolean> {
        return dynamicColors.asFlow()
    }

    override suspend fun setDynamicColors(enabled: Boolean) {
        dynamicColors.set(enabled)
    }

    override fun observeLoginClickAction(): Flow<LoginClickAction> {
        return loginClickAction.asFlow().map { it.asDomain() }
    }

    override suspend fun setLoginClickAction(action: LoginClickAction) {
        loginClickAction.set(action.asEntity())
    }

    override fun observeAutofillSettings(): Flow<AutofillSettings> {
        return autofillUseInline.asFlow().map {
            AutofillSettings(
                useInlinePresentation = it,
            )
        }
    }

    override suspend fun setAutofillSettings(useInline: Boolean?) {
        useInline?.let { autofillUseInline.set(it) }
    }

    override fun observeSortingMethod(): Flow<SortingMethod> {
        return sortingMethod.asFlow().map { it.asDomain() }
    }

    override suspend fun setSortingMethod(sortingMethod: SortingMethod) {
        this.sortingMethod.set(sortingMethod.asEntity())
    }

    override fun observeAppLockTime(): Flow<AppLockTime> {
        return appLockTime.asFlow().map { it.asDomain() }
    }

    override suspend fun setAppLockTime(time: AppLockTime) {
        this.appLockTime.set(time.asEntity())
    }

    override fun observeAppLockAttempts(): Flow<AppLockAttempts> {
        return appLockAttempts.asFlow().map { it.asDomain() }
    }

    override suspend fun setAppLockAttempts(attempts: AppLockAttempts) {
        this.appLockAttempts.set(attempts.asEntity())
    }

    override fun observeAutofillLockTime(): Flow<AutofillLockTime> {
        return autofillLockTime.asFlow().map { it.asDomain() }
    }

    override suspend fun setAutofillLockTime(time: AutofillLockTime) {
        this.autofillLockTime.set(time.asEntity())
    }

    override fun observeSendCrashLogs(): Flow<Boolean> {
        return sendCrashLogs.asFlow()
    }

    override suspend fun setSendCrashLogs(enabled: Boolean) {
        this.sendCrashLogs.set(enabled)
    }

    override fun observeDefaultSecurityType(): Flow<SecurityType> {
        return defaultSecurityType.asFlow().map {
            when (it) {
                0 -> SecurityType.Tier1
                1 -> SecurityType.Tier2
                2 -> SecurityType.Tier3
                else -> SecurityType.Tier3
            }
        }
    }

    override suspend fun setDefaultSecurityType(type: SecurityType) {
        defaultSecurityType.set(
            when (type) {
                SecurityType.Tier1 -> 0
                SecurityType.Tier2 -> 1
                SecurityType.Tier3 -> 2
            },
        )
    }

    override fun observeScreenCaptureEnabled(): Flow<Boolean> {
        return when (appBuild.buildVariant) {
            BuildVariant.Release -> screenCapture.asFlow()
            BuildVariant.Internal -> flowOf(true)
            BuildVariant.Debug -> flowOf(true)
        }
    }

    override suspend fun setScreenCaptureEnabled(enabled: Boolean) {
        screenCapture.set(enabled)
    }

    override fun observePasswordGeneratorSettings(): Flow<PasswordGeneratorSettings> {
        return passwordGeneratorSettings.asFlow().map { it?.asDomain() ?: PasswordGeneratorSettings() }
    }

    override suspend fun setPasswordGeneratorSettings(settings: PasswordGeneratorSettings) {
        passwordGeneratorSettings.set(settings.asEntity())
    }
}