/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.pass.oss

import com.twofasapp.feature.settings.ui.opensource.OpenSourceLibrariesProvider
import com.twofasapp.pass.R

class OpenSourceLibrariesProviderImpl : OpenSourceLibrariesProvider {
    override val aboutLibrariesResId: Int = R.raw.aboutlibraries
}