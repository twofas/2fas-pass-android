/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.autofill.service.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class SaveLoginData(
    val uri: String?,
    val username: String?,
    val password: String?,
) : Parcelable