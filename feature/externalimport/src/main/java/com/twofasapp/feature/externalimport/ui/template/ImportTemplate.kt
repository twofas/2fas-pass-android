/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.externalimport.ui.template

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.foundation.button.Button
import com.twofasapp.core.design.foundation.lazy.forEachIndexed
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.text.richText
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShapeIndexed
import com.twofasapp.feature.externalimport.import.ImportSpec

@Composable
internal fun ImportTemplate(
    modifier: Modifier = Modifier,
    importSpec: ImportSpec,
    loading: Boolean,
    onFilePicked: (Uri) -> Unit = {},
) {
    val filePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let(onFilePicked)
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Image(
                painter = painterResource(id = importSpec.image),
                contentDescription = null,
                modifier = Modifier.size(56.dp),
            )

            Space(16.dp)

            Text(
                text = "Transfer from ${importSpec.name}",
                style = MdtTheme.typo.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Space(16.dp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            importSpec.instructions.split("\n\n").forEachIndexed { index, isFirst, isLast, textLine ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedShapeIndexed(isFirst, isLast))
                        .background(MdtTheme.color.surfaceContainer)
                        .padding(horizontal = 16.dp),
                ) {
                    Space(16.dp)

                    Text(
                        text = richText("${index + 1}. $textLine"),
                        style = MdtTheme.typo.bodyMedium,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Space(16.dp)
                    if (isLast.not()) {
                        HorizontalDivider(
                            color = MdtTheme.color.surfaceContainerHigh,
                        )
                    }
                }
            }

            importSpec.additionalInfo?.let {
                Space(8.dp)

                Text(
                    text = richText(it),
                    color = MdtTheme.color.onSurfaceVariant,
                    style = MdtTheme.typo.bodySmall,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedShape12)
                        .background(MdtTheme.color.surfaceContainer)
                        .padding(16.dp),
                )
            }

            Space(8.dp)
        }

        importSpec.cta.forEach { cta ->
            when (cta) {
                is ImportSpec.Cta.Primary -> {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        text = cta.text,
                        loading = loading,
                        onClick = {
                            when (cta.action) {
                                is ImportSpec.CtaAction.ChooseFile -> {
                                    filePicker.launch("*/*")
                                }
                            }
                        },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        ImportTemplate(
            modifier = Modifier.fillMaxSize(),
            importSpec = ImportSpec.Empty,
            loading = false,
        )
    }
}