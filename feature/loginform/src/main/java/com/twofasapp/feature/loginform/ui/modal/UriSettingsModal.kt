/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui.modal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.LoginUriMatcher
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.lazy.forEachIndexed
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.core.locale.MdtLocale

@Composable
fun UriSettingsModal(
    onDismissRequest: () -> Unit,
    loginUri: LoginUri,
    onSelectMatcher: (LoginUriMatcher) -> Unit = {},
) {
    Modal(
        onDismissRequest = onDismissRequest,
        headerText = MdtLocale.strings.uriSettingsModalHeader,
    ) { dismissAction ->
        Content(
            loginUri = loginUri,
            onSelect = { dismissAction { onSelectMatcher(it) } },
        )
    }
}

@Composable
private fun Content(
    loginUri: LoginUri,
    onSelect: (LoginUriMatcher) -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        if (loginUri.text.isNotBlank()) {
            SelectionContainer {
                Text(
                    text = loginUri.text,
                    style = if (loginUri.text.length > 100) MdtTheme.typo.regular.xs else MdtTheme.typo.regular.sm,
                    color = MdtTheme.color.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedShape12)
                        .padding(horizontal = 12.dp),
                )
            }
        }

        OptionHeader(
            text = MdtLocale.strings.uriSettingsMatchingRuleHeader,
            contentPadding = PaddingValues(start = 4.dp, bottom = 16.dp, top = if (loginUri.text.isNotBlank()) 24.dp else 0.dp),
        )

        LoginUriMatcher.entries.forEachIndexed { _, isFirst, isLast, item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedShapeIndexed(isFirst, isLast))
                    .background(MdtTheme.color.surfaceContainerHigh)
                    .clickable { onSelect(item) }
                    .padding(start = 16.dp, end = 12.dp, top = 16.dp, bottom = 16.dp),
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.asTitle(),
                        style = MdtTheme.typo.medium.base,
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = item.asDescription(),
                        style = MdtTheme.typo.regular.sm,
                        color = MdtTheme.color.onSurfaceVariant,
                    )
                }

                CheckIcon(
                    checked = loginUri.matcher == item,
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = MdtLocale.strings.uriSettingsModalDescription,
            style = MdtTheme.typo.regular.xs,
            color = MdtTheme.color.secondary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )
    }
}

@Composable
fun LoginUriMatcher.asTitle(): String {
    return when (this) {
        LoginUriMatcher.Domain -> MdtLocale.strings.loginUriMatcherDomainTitle
        LoginUriMatcher.Host -> MdtLocale.strings.loginUriMatcherHostTitle
        LoginUriMatcher.StartsWith -> MdtLocale.strings.loginUriMatcherStartsWithTitle
        LoginUriMatcher.Exact -> MdtLocale.strings.loginUriMatcherExactTitle
    }
}

@Composable
fun LoginUriMatcher.asDescription(): String {
    return when (this) {
        LoginUriMatcher.Domain -> MdtLocale.strings.loginUriMatcherDomainDescription
        LoginUriMatcher.Host -> MdtLocale.strings.loginUriMatcherHostDescription
        LoginUriMatcher.StartsWith -> MdtLocale.strings.loginUriMatcherStartsWithDescription
        LoginUriMatcher.Exact -> MdtLocale.strings.loginUriMatcherExactDescription
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        Content(
            loginUri = LoginUri("https://www.google.com/search?q=asd3.&rlz=1C5CHFA_enPL1054PL1054&oq=asd3.&gs_lcrp=EgZjaHJvbWUyBggAEEUYOTIKCAEQABixAxiABDIVCAIQLhhDGMcBGLEDGNEDGIAEGIoFMgoIAxAuGLEDGIAEMhIIBBAAGEMYgwEYsQMYgAQYigUyCggFEAAYsQMYgAQyBwgGEAAYgAQyDQgHEC4YrwEYxwEYgAQyBwgIEAAYgAQyBwgJEAAYgATSAQc1MzNqMGoxqAIAsAIA&sourceid=chrome&ie=UTF-8"),
        )
    }
}