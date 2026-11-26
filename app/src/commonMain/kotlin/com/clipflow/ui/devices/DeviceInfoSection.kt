package com.clipflow.ui.devices

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.clipflow.db.sync.SyncRuntimeInfo
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.ui.base.InfoItem
import com.clipflow.ui.base.SectionHeader
import com.clipflow.ui.base.TableData
import com.clipflow.ui.base.TableRow
import com.clipflow.ui.base.TableRowImpl
import com.clipflow.ui.theme.AppUIColors
import com.clipflow.ui.theme.AppUISize.large2X
import com.clipflow.ui.theme.AppUISize.small2X
import com.clipflow.ui.theme.AppUISize.tiny
import com.clipflow.ui.theme.AppUISize.tiny5X
import org.koin.compose.koinInject

@Composable
fun DeviceScope.DeviceInfoSection() {
    val copywriter = koinInject<GlobalCopywriter>()

    val tableData =
        remember(syncRuntimeInfo) {
            syncRuntimeInfo.toTableData(copywriter)
        }

    Column(verticalArrangement = Arrangement.spacedBy(small2X)) {
        SectionHeader("device_info")

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            colors =
                CardDefaults.elevatedCardColors(
                    containerColor = AppUIColors.sectionCardBackground,
                ),
            border = BorderStroke(tiny5X, AppUIColors.sectionCardBorder),
        ) {
            val data = tableData.getData()

            Column(modifier = Modifier.padding(horizontal = large2X, vertical = tiny)) {
                data.forEachIndexed { index, row ->
                    val columns = row.getColumns()
                    InfoItem(columns[0], columns[1])
                    if (index < data.size - 1) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}

fun SyncRuntimeInfo.toTableData(copywriter: GlobalCopywriter): TableData =
    object : TableData {
        override fun getData(): List<TableRow> =
            listOf(
                TableRowImpl(
                    listOf(
                        copywriter.getText("app_version"),
                        appVersion,
                    ),
                ),
                TableRowImpl(
                    listOf(
                        copywriter.getText("user_name"),
                        userName,
                    ),
                ),
                TableRowImpl(
                    listOf(
                        copywriter.getText("device_id"),
                        deviceId,
                    ),
                ),
                TableRowImpl(
                    listOf(
                        copywriter.getText("arch"),
                        platform.arch,
                    ),
                ),
                TableRowImpl(
                    listOf(
                        copywriter.getText("connect_host"),
                        connectHostAddress ?: copywriter.getText("unknown"),
                    ),
                ),
                TableRowImpl(
                    listOf(
                        copywriter.getText("port"),
                        if (port <= 0) {
                            copywriter.getText("unknown")
                        } else {
                            port.toString()
                        },
                    ),
                ),
            )
    }
