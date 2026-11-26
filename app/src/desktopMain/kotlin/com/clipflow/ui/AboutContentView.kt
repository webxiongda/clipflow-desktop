package com.clipflow.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clipflow.app.AppInfo
import com.clipflow.app.AppUrls
import com.clipflow.config.DesktopConfigManager
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.ui.base.ClipFlowLogoView
import com.clipflow.ui.base.IconData
import com.clipflow.ui.base.UISupport
import com.clipflow.ui.base.x
import com.clipflow.ui.settings.SettingListItem
import com.clipflow.ui.settings.SettingSectionCard
import com.clipflow.ui.theme.AppUISize.giant
import com.clipflow.ui.theme.AppUISize.medium
import com.clipflow.ui.theme.AppUISize.small
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.xxLarge
import com.clipflow.ui.theme.AppUISize.xxxxLarge
import com.composables.icons.fontawesome.FontAwesome
import com.composables.icons.fontawesome.brands.Github
import com.composables.icons.materialsymbols.MaterialSymbols
import com.composables.icons.materialsymbols.rounded.Auto_awesome
import com.composables.icons.materialsymbols.rounded.Favorite
import com.composables.icons.materialsymbols.rounded.Feedback
import com.composables.icons.materialsymbols.rounded.Language
import com.composables.icons.materialsymbols.rounded.Mail
import com.composables.icons.materialsymbols.rounded.School
import org.koin.compose.koinInject

private const val GITHUB_URL = "https://github.com/ClipFlow"
private const val TWITTER_URL = "https://x.com/ClipFlow"

@Composable
fun AboutContentView() {
    val appInfo = koinInject<AppInfo>()
    val appUrls = koinInject<AppUrls>()
    val uiSupport = koinInject<UISupport>()
    val configManager = koinInject<DesktopConfigManager>()
    val copywriter = koinInject<GlobalCopywriter>()
    val navigationManager = koinInject<NavigationManager>()
    val themeExt = LocalThemeExtState.current

    val config by configManager.config.collectAsState()

    // The whole page scrolls: the brand block used to take the leftover height
    // (weight), and on a 700dp window the two cards plus footer left it ~100dp,
    // so the name, version and social links were measured down to zero height.
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Brand Section
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = xxLarge),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            ClipFlowLogoView(
                size = giant,
                enableDebugToggle = true,
            )

            Spacer(modifier = Modifier.height(medium))

            Text(
                text =
                    if (!config.enableDebugMode) {
                        "ClipFlow"
                    } else {
                        "ClipFlow [Debug]"
                    },
                style =
                    MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp,
                    ),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.padding(top = tiny),
                text = "Version ${appInfo.displayVersion()}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            // Social Links
            Row(
                modifier = Modifier.padding(top = medium),
                horizontalArrangement = Arrangement.spacedBy(medium),
            ) {
                SocialLinkButton(
                    icon = FontAwesome.Brands.Github,
                    onClick = { uiSupport.openUrlInBrowser(GITHUB_URL) },
                )
                SocialLinkButton(
                    icon = x(),
                    onClick = { uiSupport.openUrlInBrowser(TWITTER_URL) },
                )
                SocialLinkButton(
                    icon = MaterialSymbols.Rounded.Language,
                    onClick = { uiSupport.openClipFlowWebInBrowser() },
                )
            }
        }

        // Resources Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(small),
        ) {
            Text(
                text = copywriter.getText("resources"),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingSectionCard {
                SettingListItem(
                    title = "official_website",
                    subtitle = "official_website_desc",
                    icon = IconData(MaterialSymbols.Rounded.Language, themeExt.blueIconColor),
                ) {
                    uiSupport.openClipFlowWebInBrowser()
                }
                HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
                SettingListItem(
                    title = "getting_started",
                    subtitle = "getting_started_desc",
                    icon = IconData(MaterialSymbols.Rounded.School, themeExt.greenIconColor),
                ) {
                    uiSupport.openClipFlowWebInBrowser("tutorial/pasteboard")
                }
                HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
                SettingListItem(
                    title = "change_log",
                    subtitle = "change_log_desc",
                    icon = IconData(MaterialSymbols.Rounded.Auto_awesome, themeExt.purpleIconColor),
                ) {
                    navigationManager.navigateAndClearStack(ChangeLog)
                }
            }
        }

        Spacer(modifier = Modifier.height(xxLarge))

        // Support Section
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(small),
        ) {
            Text(
                text = copywriter.getText("support"),
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.SemiBold,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            SettingSectionCard {
                SettingListItem(
                    title = "feedback",
                    subtitle = "feedback_desc",
                    icon = IconData(MaterialSymbols.Rounded.Feedback, themeExt.amberIconColor),
                ) {
                    uiSupport.openUrlInBrowser(appUrls.issueTrackerUrl)
                }
                HorizontalDivider(modifier = Modifier.padding(start = xxxxLarge))
                SettingListItem(
                    title = "contact_us",
                    subtitle = "contact_us_desc",
                    icon = IconData(MaterialSymbols.Rounded.Mail, themeExt.cyanIconColor),
                ) {
                    uiSupport.openEmailClient(appUrls.supportEmail)
                }
            }
        }

        // Footer
        Column(
            modifier = Modifier.padding(vertical = medium),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(tiny),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Made with",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Icon(
                    imageVector = MaterialSymbols.Rounded.Favorite,
                    contentDescription = null,
                    modifier = Modifier.size(12.dp),
                    tint = Color(0xFFEF4444),
                )
                Text(
                    text = "by ClipFlow Team",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "\u00A9 2024 Compile Future",
                style =
                    MaterialTheme.typography.labelSmall.copy(
                        letterSpacing = 0.5.sp,
                    ),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SocialLinkButton(
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun SocialLinkButton(
    icon: Painter,
    onClick: () -> Unit,
) {
    Box(
        modifier =
            Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
