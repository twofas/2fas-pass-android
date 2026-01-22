/*
 * SPDX-License-Identifier: BUSL-1.1
 *
 * Copyright © 2025 Two Factor Authentication Service, Inc.
 * Licensed under the Business Source License 1.1
 * See LICENSE file for full terms
 */

package com.twofasapp.feature.settings.ui.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.twofasapp.core.android.deeplinks.Deeplinks
import com.twofasapp.core.android.ktx.currentActivity
import com.twofasapp.core.android.ktx.openSafely
import com.twofasapp.core.android.navigation.Screen
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.feature.settings.OptionHeader
import com.twofasapp.core.design.feature.settings.OptionSwitch
import com.twofasapp.core.design.foundation.other.Space
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.foundation.topbar.TopAppBar
import com.twofasapp.core.design.theme.ScreenPadding
import com.twofasapp.core.locale.MdtLocale
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun AboutScreen(
    viewModel: AboutViewModel = koinViewModel(),
    deeplinks: Deeplinks = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Content(
        uiState = uiState,
        deeplinks = deeplinks,
        onCrashLogsToggle = { viewModel.toggleCrashLogs() },
    )
}

@Composable
private fun Content(
    uiState: AboutUiState,
    deeplinks: Deeplinks,
    onCrashLogsToggle: () -> Unit = {},
) {
    val strings = MdtLocale.strings
    val links = MdtLocale.links
    val uriHandler = LocalUriHandler.current
    val activity = LocalContext.currentActivity
    val shareText = MdtLocale.strings.aboutInviteFriendsShareText

    Scaffold(
        topBar = { TopAppBar(title = strings.settingsAbout) },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MdtTheme.color.background)
                .padding(top = padding.calculateTopPadding()),
            contentPadding = PaddingValues(bottom = ScreenPadding),
        ) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Image(
                        painter = painterResource(com.twofasapp.core.design.R.drawable.brand_logo),
                        contentDescription = null,
                        modifier = Modifier.size(50.dp),
                    )

                    Space(16.dp)

                    Text(
                        text = strings.aboutTagline,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = MdtTheme.typo.headlineSmall,
                    )

                    Space(8.dp)

                    Text(
                        text = buildAnnotatedString {
                            append(strings.aboutVersionPrefix)
                            withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = MdtTheme.color.onSurface)) {
                                append(" ")
                                append(uiState.version)
                            }
                        },
                        textAlign = TextAlign.Center,
                        color = MdtTheme.color.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        style = MdtTheme.typo.bodyMedium,
                    )
                }
            }

            item {
                OptionHeader(
                    text = strings.aboutSectionGeneral,
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutRateUs,
                    external = true,
                    icon = MdtIcons.Star,
                    onClick = { uriHandler.openSafely(links.playStore) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutPrivacyPolicy,
                    external = true,
                    icon = MdtIcons.VisibilityOff,
                    onClick = { uriHandler.openSafely(links.privacyPolicy) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutTermsOfUse,
                    external = true,
                    icon = MdtIcons.Document,
                    onClick = { uriHandler.openSafely(links.terms) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutOpenSourceLicenses,
                    icon = MdtIcons.FolderOpen,
                    onClick = { deeplinks.openScreen(Screen.OpenSourceLibraries) },
                )
            }

            item {
                OptionHeader(
                    text = strings.aboutSectionShare,
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutInviteFriends,
                    icon = MdtIcons.Share,
                    onClick = {
                        ShareCompat.IntentBuilder(activity)
                            .setType("text/plain")
                            .setChooserTitle(strings.aboutShareTitle)
                            .setText(shareText)
                            .startChooser()
                    },
                )
            }

            item {
                OptionHeader(
                    text = strings.aboutSectionConnect,
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutDiscord,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_discord),
                    external = true,
                    onClick = { uriHandler.openSafely(links.discord) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutGithub,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_github),
                    external = true,
                    onClick = { uriHandler.openSafely(links.github) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutX,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_twitter),
                    external = true,
                    onClick = { uriHandler.openSafely(links.x) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutYoutube,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_youtube),
                    external = true,
                    onClick = { uriHandler.openSafely(links.youtube) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutLinkedin,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_linkedin),
                    external = true,
                    onClick = { uriHandler.openSafely(links.linkedin) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutReddit,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_reddit),
                    external = true,
                    onClick = { uriHandler.openSafely(links.reddit) },
                )
            }

            item {
                OptionEntry(
                    title = strings.aboutFacebook,
                    image = painterResource(id = com.twofasapp.core.design.R.drawable.ic_facebook),
                    external = true,
                    onClick = { uriHandler.openSafely(links.facebook) },
                )
            }

            item {
                OptionHeader(
                    text = strings.aboutSectionCrashReporting,
                )
            }
            item {
                OptionSwitch(
                    title = strings.aboutSendCrashReports,
                    icon = MdtIcons.Warning,
                    checked = uiState.crashLogsEnabled,
                    onToggle = { onCrashLogsToggle() },
                )
            }

            item {
                OptionEntry(
                    title = null,
                    subtitle = strings.aboutCrashReportsDescription,
                    contentPadding = PaddingValues(horizontal = 16.dp),
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    PreviewTheme {
        Content(
            uiState = AboutUiState(),
            deeplinks = Deeplinks.Empty,
        )
    }
}