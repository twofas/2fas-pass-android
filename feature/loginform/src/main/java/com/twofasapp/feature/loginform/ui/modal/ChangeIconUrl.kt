/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.loginform.ui.modal

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.common.domain.LoginUri
import com.twofasapp.core.common.domain.LoginUriMatcher
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.R
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.foundation.checked.CheckIcon
import com.twofasapp.core.design.foundation.image.AsyncImage
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewColumn

@Composable
internal fun ChangeIconUrl(
    uris: List<LoginUri>,
    iconUriIndex: Int?,
    onIndexChange: (Int) -> Unit = {},
) {
    OptionHeader(text = "Select Icon")

    if (uris.all { it.text.isBlank() }) {
        Text(
            text = "No URIs have been defined. Add one to fetch the icon automatically.",
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 16.dp),
        )

        return
    }

    uris
        .filter { it.host.isNullOrBlank().not() && it.iconUrl.isNullOrBlank().not() }
        .distinctBy { it.host }
        .forEach { loginUri ->
            IconOption(
                text = loginUri.host.orEmpty(),
                imageUrl = loginUri.iconUrl!!,
                selected = uris.indexOfFirst { it == loginUri } == iconUriIndex,
                onSelect = { onIndexChange(uris.indexOfFirst { it == loginUri }) },
            )
        }
}

@Composable
private fun IconOption(
    text: String,
    imageUrl: String,
    selected: Boolean,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect(imageUrl) }
            .padding(horizontal = 16.dp)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CheckIcon(
            checked = selected,
            size = 24.dp,
        )

        Space(12.dp)

        Text(
            text = text,
            style = MdtTheme.typo.bodyLarge,
            modifier = Modifier.weight(1f),
        )

        Space(12.dp)

        AsyncImage(
            url = imageUrl,
            modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(2.dp)),
            contentScale = ContentScale.Crop,
            error = painterResource(id = R.drawable.img_placeholder),
        )
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewColumn {
        ChangeIconUrl(
            uris = listOf(
                LoginUri(text = " ", matcher = LoginUriMatcher.Host),
                LoginUri(text = " ", matcher = LoginUriMatcher.Host),
            ),
            iconUriIndex = 1,
        )

        HorizontalDivider()

        ChangeIconUrl(
            uris = listOf(
                LoginUri(text = "https://test.com", matcher = LoginUriMatcher.Host),
                LoginUri(text = "", matcher = LoginUriMatcher.Host),
                LoginUri(text = "https://google.com", matcher = LoginUriMatcher.Host),
            ),
            iconUriIndex = 2,
        )
    }
}