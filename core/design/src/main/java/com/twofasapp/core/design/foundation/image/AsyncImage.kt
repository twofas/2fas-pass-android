/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.core.design.foundation.image

import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.DefaultAlpha
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.twofasapp.core.design.R
import java.nio.ByteBuffer

@Composable
fun AsyncImage(
    modifier: Modifier = Modifier,
    url: String? = null,
    model: Any? = null,
    svgIcon: String? = null,
    crossfade: Boolean = true,
    contentDescription: String? = null,
    placeholder: Painter? = null,
    error: Painter? = null,
    fallback: Painter? = error,
    alignment: Alignment = Alignment.Center,
    contentScale: ContentScale = ContentScale.Fit,
    alpha: Float = DefaultAlpha,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = DrawScope.DefaultFilterQuality,
) {
    AsyncImage(
        model = model
            ?: svgIcon?.let {
                ImageRequest.Builder(LocalContext.current)
                    .data(ByteBuffer.wrap(svgIcon.toByteArray()))
                    .build()
            }
            ?: url?.let { imageUrl ->
                ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(crossfade)
                    .build()
            },
        contentDescription = contentDescription,
        modifier = modifier,
        placeholder = if (LocalInspectionMode.current) {
            painterResource(id = R.drawable.img_placeholder_preview)
        } else {
            placeholder
        },
        error = error,
        fallback = fallback,
        alignment = alignment,
        contentScale = contentScale,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
    )
}