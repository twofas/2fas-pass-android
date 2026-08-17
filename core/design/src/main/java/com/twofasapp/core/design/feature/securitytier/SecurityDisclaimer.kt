package com.twofasapp.core.design.feature.securitytier

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.twofasapp.core.common.domain.SecurityType
import com.twofasapp.core.design.MdtIcons
import com.twofasapp.core.design.MdtTheme
import com.twofasapp.core.design.feature.settings.OptionEntry
import com.twofasapp.core.design.foundation.layout.ZeroPadding
import com.twofasapp.core.design.foundation.modal.Modal
import com.twofasapp.core.design.foundation.preview.PreviewTheme
import com.twofasapp.core.design.theme.RoundedShape12
import com.twofasapp.core.design.theme.RoundedShape16
import com.twofasapp.core.design.theme.RoundedShape40
import com.twofasapp.core.design.theme.RoundedShape8
import com.twofasapp.core.locale.MdtLocale
import kotlin.math.sqrt

@Composable
fun SecurityDisclaimer(modifier: Modifier = Modifier) {
    var showDisclaimerModal by remember { mutableStateOf(false) }

    if (showDisclaimerModal) {
        SecurityDisclaimerModal(
            onDismissRequest = { showDisclaimerModal = false },
        )
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = MdtLocale.strings.securityTypeModalDescription,
            style = MdtTheme.typo.regular.xs,
            color = MdtTheme.color.secondary,
            modifier = Modifier.padding(horizontal = 8.dp),
        )

        OptionEntry(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedShape12)
                .background(MdtTheme.color.surfaceContainerHigh)
                .testTag("securityDisclaimerButton")
                .clickable { showDisclaimerModal = true }
                .padding(16.dp),
            icon = MdtIcons.Lightbulb,
            title = MdtLocale.strings.settingsProtectionLevelHelp,
            contentPadding = ZeroPadding,
        )
    }
}

@Composable
private fun SecurityDisclaimerModal(
    onDismissRequest: () -> Unit,
) {
    Modal(
        onDismissRequest = onDismissRequest,
    ) {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
        ) {
            item {
                Image(
                    modifier = Modifier
                        .padding(top = 40.dp)
                        .fillMaxWidth()
                        .height(70.dp),
                    painter = painterResource(com.twofasapp.core.design.R.drawable.security_disclaimer_items),
                    contentDescription = null,
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 32.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = MdtLocale.strings.securityTiersHelpTitle,
                    style = MdtTheme.typo.headlineLarge,
                    color = MdtTheme.color.onSurface,
                )
            }
            item {
                Text(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    text = MdtLocale.strings.securityTiersHelpSubtitle,
                    style = MdtTheme.typo.bodyMedium,
                    color = MdtTheme.color.onSurface,
                )
            }
            item {
                SectionTitle(text = MdtLocale.strings.securityTiersHelpLocalFirstSectionTitle)
            }
            item {
                SectionSubtitle(text = MdtLocale.strings.securityTiersHelpLocalFirstSectionSubtitle)
            }
            item {
                TiersContent()
            }
            item {
                SectionTitle(text = MdtLocale.strings.securityTiersHelpTiersSectionTitle)
            }
            item {
                SectionSubtitle(text = MdtLocale.strings.securityTiersHelpTiersSectionSubtitle)
            }
            item {
                SectionContent(
                    title = MdtLocale.strings.securityTiersHelpTiersSecretTitle,
                    subtitle = MdtLocale.strings.securityTiersHelpTiersSecretSubtitle,
                    image = com.twofasapp.core.design.R.drawable.security_disclaimer_secret,
                )
            }
            item {
                SectionContent(
                    title = MdtLocale.strings.securityTiersHelpTiersHighlySecretTitle,
                    subtitle = MdtLocale.strings.securityTiersHelpTiersHighlySecretSubtitle,
                    image = com.twofasapp.core.design.R.drawable.security_disclaimer_highly_secret,
                )
            }
            item {
                SectionContent(
                    title = MdtLocale.strings.securityTiersHelpTiersTopSecretTitle,
                    subtitle = MdtLocale.strings.securityTiersHelpTiersTopSecretSubtitle,
                    image = com.twofasapp.core.design.R.drawable.security_disclaimer_top_secret,
                )
            }
            item {
                SectionTitle(text = MdtLocale.strings.securityTiersHelpLayersSectionTitle)
            }
            item {
                SectionContentTitle(text = MdtLocale.strings.securityTiersHelpTiersLayersE2eeTitle)
            }
            item {
                SectionSubtitle(text = MdtLocale.strings.securityTiersHelpTiersLayersAndroidE2EeSubtitle)
            }
            item {
                SectionContentTitle(text = MdtLocale.strings.securityTiersHelpTiersLayersAndroidSecureTitle)
            }
            item {
                SectionSubtitle(text = MdtLocale.strings.securityTiersHelpTiersLayersAndroidSecureSubtitle)
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        modifier = Modifier.padding(top = 32.dp),
        text = text,
        style = MdtTheme.typo.titleMedium,
        color = MdtTheme.color.onSurface,
    )
}

@Composable
private fun SectionContentTitle(text: String) {
    Text(
        modifier = Modifier.padding(top = 16.dp),
        text = text,
        style = MdtTheme.typo.titleMedium,
        color = MdtTheme.color.onSurface,
    )
}

@Composable
private fun SectionSubtitle(text: String) {
    Text(
        text = text,
        style = MdtTheme.typo.bodyMedium,
        color = MdtTheme.color.onSurfaceVariant,
    )
}

@Composable
private fun TiersContent() {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clip(RoundedShape16)
            .background(MdtTheme.color.surfaceContainerHigh)
            .padding(all = 16.dp),
    ) {
        Text(
            text = MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureTitle,
            style = MdtTheme.typo.titleMedium,
            color = MdtTheme.color.onSurface,
        )
        SecurityType.entries.forEachIndexed { index, securityType ->
            Tier(
                modifier = Modifier.padding(top = if (index == 0) 0.dp else 10.dp),
                selectedSecurityType = securityType,
            )
        }
    }
}

@Composable
private fun Tier(selectedSecurityType: SecurityType, modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val window = LocalWindowInfo.current

    Column(modifier = modifier) {
        var bubbleOffset by remember { mutableStateOf(0.dp) }
        var bubbleWidth by remember { mutableStateOf(0.dp) }

        Text(
            modifier = Modifier
                .offset(
                    x = if (bubbleOffset != 0.dp && bubbleWidth != 0.dp) {
                        (bubbleOffset - 32.dp - bubbleWidth / 2 - 3.dp).coerceAtMost(
                            window.containerDpSize.width - 64.dp - bubbleWidth,
                        )
                    } else {
                        0.dp
                    },
                )
                .clip(RoundedShape8)
                .background(MdtTheme.color.surfaceContainer)
                .padding(horizontal = 6.dp)
                .onGloballyPositioned { coordinates ->
                    bubbleWidth = with(density) { coordinates.size.width.toDp() }
                },
            text = when (selectedSecurityType) {
                SecurityType.Tier1 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureHigh
                SecurityType.Tier2 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureMedium
                SecurityType.Tier3 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureLow
            },
            style = MdtTheme.typo.bodyMedium.copy(fontSize = 7.sp),
            color = MdtTheme.color.onSurfaceVariant,
            maxLines = 1,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SecurityType.entries.reversed().forEach { securityType ->
                TierSegment(
                    modifier = Modifier.weight(1f),
                    securityType = securityType,
                    selectedSecurityType = selectedSecurityType,
                    onBubbleArrowPositioned = { offset ->
                        bubbleOffset = with(density) { offset.x.toDp() }
                    },
                )
            }
        }
        Text(
            modifier = Modifier.padding(top = 4.dp),
            text = when (selectedSecurityType) {
                SecurityType.Tier1 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureHighDescription
                SecurityType.Tier2 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureMediumDescription
                SecurityType.Tier3 -> MdtLocale.strings.securityTiersHelpLocalFirstSectionFigureLowDescription
            },
            style = MdtTheme.typo.bodySmall,
            color = MdtTheme.color.onSurface,
        )
    }
}

@Composable
private fun TierSegment(
    modifier: Modifier = Modifier,
    securityType: SecurityType,
    selectedSecurityType: SecurityType,
    onBubbleArrowPositioned: (Offset) -> Unit,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter,
    ) {
        Box(
            modifier = Modifier
                .padding(bottom = 2.dp)
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedShape40)
                .background(color = if (selectedSecurityType <= securityType) securityType.color() else MdtTheme.color.surfaceContainer),
        )
        if (securityType == selectedSecurityType) {
            Column(
                modifier = Modifier.align(Alignment.CenterEnd),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 6.dp, height = 3.dp)
                        .clip(RoundedBottomTriangleShape())
                        .background(MdtTheme.color.surfaceContainer)
                        .onGloballyPositioned { coordinates ->
                            onBubbleArrowPositioned(coordinates.positionOnScreen())
                        },
                )
                Box(
                    modifier = Modifier
                        .dropShadow(
                            shape = CircleShape,
                            shadow = Shadow(
                                radius = 4.dp,
                                color = securityType.color(),
                                spread = 0.dp,
                            ),
                        )
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(MdtTheme.color.onSurface)
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(securityType.color()),
                )
            }
        } else {
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

private fun SecurityType.color(): Color {
    return when (this) {
        SecurityType.Tier1 -> Color(0xFF00C457)
        SecurityType.Tier2 -> Color(0xFFFFD554)
        SecurityType.Tier3 -> Color(0xFFFF2833)
    }
}

@Composable
private fun SectionContent(title: String, subtitle: String, @DrawableRes image: Int) {
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .clip(RoundedShape16)
            .background(MdtTheme.color.surfaceContainerHigh)
            .padding(all = 16.dp),
    ) {
        Text(
            text = title,
            style = MdtTheme.typo.titleMedium,
            color = MdtTheme.color.onSurface,
        )
        Text(
            text = subtitle,
            style = MdtTheme.typo.bodyMedium,
            color = MdtTheme.color.onSurfaceVariant,
        )
        Image(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .height(160.dp),
            painter = painterResource(image),
            contentDescription = null,
        )
    }
}

private class RoundedBottomTriangleShape(
    private val bottomRadius: Float = 99f,
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density,
    ): Outline {
        val path = Path().apply {
            val width = size.width
            val height = size.height
            val halfWidth = width / 2f

            val sideLength = sqrt(halfWidth * halfWidth + height * height)
            val radius = bottomRadius.coerceAtMost(sideLength / 2f)
            val fraction = radius / sideLength

            moveTo(0f, 0f)
            lineTo(width, 0f)

            val startCurveX = width + (halfWidth - width) * (1 - fraction)
            val startCurveY = 0f + (height - 0f) * (1 - fraction)

            lineTo(startCurveX, startCurveY)

            val endCurveX = 0f + (halfWidth - 0f) * (1 - fraction)
            val endCurveY = 0f + (height - 0f) * (1 - fraction)

            quadraticTo(
                x1 = halfWidth,
                y1 = height,
                x2 = endCurveX,
                y2 = endCurveY,
            )

            close()
        }

        return Outline.Generic(path)
    }
}

@Preview
@Composable
private fun SecurityDisclaimerPreview() {
    PreviewTheme {
        SecurityDisclaimer()
    }
}

@Preview
@Composable
private fun SecurityDisclaimerModalPreview() {
    PreviewTheme {
        SecurityDisclaimerModal(onDismissRequest = {})
    }
}