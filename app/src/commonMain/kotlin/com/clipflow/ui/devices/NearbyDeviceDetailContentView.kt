package com.clipflow.ui.devices

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import com.clipflow.ui.theme.AppUIColors
import com.clipflow.ui.theme.AppUISize.tinyRoundedCornerShape
import com.clipflow.ui.theme.AppUISize.xLarge

@Composable
fun SyncScope.NearbyDeviceDetailContentView() {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(AppUIColors.contentBackground)
                .clip(tinyRoundedCornerShape),
        verticalArrangement = Arrangement.spacedBy(xLarge),
    ) {
        NearbyDeviceDetailHeaderView()
        NearbyDeviceInfoSection()
    }
}
