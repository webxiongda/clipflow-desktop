package com.clipflow.paste

import com.clipflow.app.AppInfo
import com.clipflow.app.AppLaunchState
import com.clipflow.db.paste.PasteDao
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.paste.item.PasteItemReader

class DesktopGuidePasteDataService(
    appInfo: AppInfo,
    appLaunchState: Lazy<AppLaunchState>,
    copywriter: GlobalCopywriter,
    pasteDao: PasteDao,
    pasteItemReader: PasteItemReader,
    searchContentService: SearchContentService,
) : GuidePasteDataService(appInfo, appLaunchState, copywriter, pasteDao, pasteItemReader, searchContentService) {

    override val guideKey: String = "desktop_guide_"
}
