/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.feature.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import coil3.compose.AsyncImagePainter
import coil3.compose.LocalPlatformContext
import coil3.compose.rememberAsyncImagePainter
import coil3.compose.rememberConstraintsSizeResolver
import coil3.request.ImageRequest
import com.twofasapp.core.android.ktx.hexToColor
import com.twofasapp.core.common.domain.IconType
import com.twofasapp.core.design.AppTheme
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.image.AsyncImage
import com.twofasapp.core.design.foundation.preview.PreviewColumn
import com.twofasapp.core.design.foundation.preview.PreviewRow
import com.twofasapp.core.design.theme.RoundedShape12

@Composable
fun LoginImage(
    modifier: Modifier = Modifier,
    iconType: IconType,
    iconUrl: String? = null,
    labelText: String? = null,
    labelColor: String? = null,
    customImageUrl: String? = null,
    size: Dp = 40.dp,
) {
    val color = labelColor?.hexToColor() ?: MdtTheme.color.surfaceContainerHigh

    Box(
        modifier = modifier.size(size),
    ) {
        when (iconType) {
            IconType.Icon -> {
                if (iconUrl.isNullOrEmpty()) {
                    // Fallback to label if icon url is empty
                    Label(
                        text = labelText,
                        color = color,
                        size = size,
                    )
                } else {
                    val iconSizeResolver = rememberConstraintsSizeResolver()
                    val iconPainter = rememberAsyncImagePainter(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(iconUrl)
                            .size(iconSizeResolver)
                            .build(),
                    )
                    val iconState by iconPainter.state.collectAsState()
                    val erred by remember {
                        derivedStateOf {
                            iconState is AsyncImagePainter.State.Error
                        }
                    }

                    if (erred) {
                        // Fallback to label if icon failed to download
                        Label(
                            text = labelText,
                            color = color,
                            size = size,
                        )
                    } else {
                        RemoteImageBox {
                            Image(
                                painter = iconPainter,
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(2.dp))
                                    .then(iconSizeResolver),
                                contentScale = ContentScale.Crop,
                            )
                        }
                    }
                }
            }

            IconType.CustomImageUrl -> {
                RemoteImageBox {
                    AsyncImage(
                        url = customImageUrl,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxSize()
                            .clip(RoundedCornerShape(2.dp)),
                    )
                }
            }

            IconType.Label -> {
                Label(
                    text = labelText,
                    color = color,
                    size = size,
                )
            }
        }
    }
}

@Composable
private fun Label(
    text: String?,
    color: Color,
    size: Dp,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedShape12)
            .background(color),
    ) {
        Text(
            text = text.orEmpty().uppercase(),
            style = MdtTheme.typo.bold.base.copy(fontSize = (size.value / 3).sp),
            modifier = Modifier.align(Alignment.Center),
            color = if (color.luminance() > 0.5f) Color.Black else Color.White,
        )
    }
}

@Composable
private fun RemoteImageBox(
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedShape12)
            .background(color = MdtTheme.color.surfaceContainerHigh),
    ) {
        content()
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewRow {
        listOf(AppTheme.Dark, AppTheme.Light).forEach {
            PreviewColumn(theme = it) {
                LoginImage(
                    iconType = IconType.Label,
                    customImageUrl = "",
                    size = 48.dp,
                )

                LoginImage(
                    iconType = IconType.Label,
                    labelText = "WW",
                    size = 36.dp,
                )

                repeat(10) { index ->
                    LoginImage(
                        iconType = IconType.Label,
                        labelText = "WW",
                        size = (index + 2) * 8.dp,
                    )
                }
            }
        }
    }
}