/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.opensource

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.core.text.parseAsHtml
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.android.rememberLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.util.htmlReadyLicenseContent
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.locale.MdtLocale
import org.koin.compose.koinInject

@Composable
internal fun OpenSourceLibrariesScreen(
    openSourceLibrariesProvider: OpenSourceLibrariesProvider = koinInject(),
) {
    val libraries by rememberLibraries(openSourceLibrariesProvider.aboutLibrariesResId)
    Scaffold(
        topBar = { TopAppBar(title = MdtLocale.strings.aboutOpenSourceLicenses) },
    ) { padding ->
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            libraries = libraries,
            licenseDialogBody = { library, modifier -> LicenseBody(library = library, modifier = modifier) },
        )
    }
}

@Composable
private fun LicenseBody(
    library: Library,
    modifier: Modifier,
) {
    val license = remember(library) {
        library.htmlReadyLicenseContent
            .takeIf { it.isNotEmpty() }
            ?.let { AnnotatedString(library.htmlReadyLicenseContent.parseAsHtml().toString()) }
    }
    if (license != null) {
        Text(
            text = license,
            modifier = modifier,
            color = MdtTheme.color.onSurface,
        )
    }
}