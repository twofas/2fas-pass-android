/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.common.build

interface AppBuild {
    val packageName: String
    val debuggable: Boolean
    val os: String
    val versionName: String
    val versionCode: Long
    val buildVariant: BuildVariant
    val systemSdkVersion: Int
}