package com.clipflow

import com.clipflow.app.AppLaunchState
import com.clipflow.clean.CleanScheduler
import com.clipflow.cli.CliSymlinkService
import com.clipflow.headless.HeadlessPasteboardService
import com.clipflow.i18n.GlobalCopywriter
import com.clipflow.image.GenerateImageService
import com.clipflow.image.ImageHandler
import com.clipflow.paste.CurrentPaste
import com.clipflow.paste.DefaultPasteSyncProcessManager
import com.clipflow.paste.DesktopCurrentPaste
import com.clipflow.paste.DesktopGuidePasteDataService
import com.clipflow.paste.DesktopPasteExportParamFactory
import com.clipflow.paste.DesktopPasteImportParamFactory
import com.clipflow.paste.DesktopPasteMenuService
import com.clipflow.paste.DesktopPasteTagMenuService
import com.clipflow.paste.DesktopScreenshotCaptureObserver
import com.clipflow.paste.DesktopSearchContentService
import com.clipflow.paste.DesktopSourceExclusionService
import com.clipflow.paste.DesktopTransferableConsumer
import com.clipflow.paste.DesktopTransferableProducer
import com.clipflow.paste.GuidePasteDataService
import com.clipflow.paste.PasteContentEditor
import com.clipflow.paste.PasteDataHelper
import com.clipflow.paste.PasteExportParamFactory
import com.clipflow.paste.PasteExportService
import com.clipflow.paste.PasteImportParamFactory
import com.clipflow.paste.PasteImportSelection
import com.clipflow.paste.PasteImportService
import com.clipflow.paste.PasteReleaseService
import com.clipflow.paste.PasteSyncProcessManager
import com.clipflow.paste.PasteboardService
import com.clipflow.paste.ScreenshotCaptureObserver
import com.clipflow.paste.SearchContentService
import com.clipflow.paste.TransferableConsumer
import com.clipflow.paste.TransferableProducer
import com.clipflow.paste.UniversalClipboardHint
import com.clipflow.paste.getDesktopPasteboardService
import com.clipflow.paste.item.DefaultPasteItemReader
import com.clipflow.paste.item.PasteItemReader
import com.clipflow.paste.item.UpdatePasteItemHelper
import com.clipflow.paste.menu.ExtractTextMenuContributor
import com.clipflow.paste.plugin.process.DiscardOversizedNonFilePlugin
import com.clipflow.paste.plugin.process.DistinctPlugin
import com.clipflow.paste.plugin.process.FileToUrlPlugin
import com.clipflow.paste.plugin.process.FilesToImagesPlugin
import com.clipflow.paste.plugin.process.GenerateTextPlugin
import com.clipflow.paste.plugin.process.GenerateUrlPlugin
import com.clipflow.paste.plugin.process.RemoveFolderImagePlugin
import com.clipflow.paste.plugin.process.RemoveHtmlImagePlugin
import com.clipflow.paste.plugin.process.RemoveInvalidPlugin
import com.clipflow.paste.plugin.process.SortPlugin
import com.clipflow.paste.plugin.process.TextToColorPlugin
import com.clipflow.paste.plugin.type.ColorTypePlugin
import com.clipflow.paste.plugin.type.FilesTypePlugin
import com.clipflow.paste.plugin.type.HtmlTypePlugin
import com.clipflow.paste.plugin.type.ImageTypePlugin
import com.clipflow.paste.plugin.type.RtfTypePlugin
import com.clipflow.paste.plugin.type.TextTypePlugin
import com.clipflow.paste.plugin.type.UrlTypePlugin
import com.clipflow.plugin.PluginMenuContributor
import com.clipflow.plugin.ProcessPluginExecutor
import com.clipflow.plugin.ProcessPluginRunner
import com.clipflow.plugin.pluginExtraPathDirs
import com.clipflow.rendering.OpenGraphService
import com.clipflow.rendering.RenderingService
import com.clipflow.sync.FilePullService
import com.clipflow.sync.PastePullCursorManager
import com.clipflow.sync.PastePullService
import com.clipflow.sync.PastePullTrigger
import com.clipflow.task.CleanPasteTaskExecutor
import com.clipflow.task.CleanTaskTaskExecutor
import com.clipflow.task.DelayedDeletePasteTaskExecutor
import com.clipflow.task.DeletePasteTaskExecutor
import com.clipflow.task.DesktopTaskSubmitter
import com.clipflow.task.OpenGraphTaskExecutor
import com.clipflow.task.PullFileTaskExecutor
import com.clipflow.task.PullIconTaskExecutor
import com.clipflow.task.SwitchLanguageTaskExecutor
import com.clipflow.task.SyncPasteTaskExecutor
import com.clipflow.task.TaskExecutor
import com.clipflow.task.TaskSubmitter
import okio.Path
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.awt.image.BufferedImage

fun desktopPasteComponentModule(headless: Boolean): Module =
    module {
        // region Pasteboard & transfer
        single<CurrentPaste> { DesktopCurrentPaste(lazy { get() }) }
        single<DesktopSourceExclusionService> { DesktopSourceExclusionService(get()) }
        single<PasteboardService> {
            if (headless) {
                HeadlessPasteboardService(get(), get())
            } else {
                getDesktopPasteboardService(
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    get(),
                    UniversalClipboardHint(get(), get(), get()),
                )
            }
        }
        single<ScreenshotCaptureObserver> {
            if (headless) {
                ScreenshotCaptureObserver.NoOp
            } else {
                DesktopScreenshotCaptureObserver(get(), get(), get(), get())
            }
        }
        single<TransferableConsumer> {
            DesktopTransferableConsumer(
                get(),
                get(),
                get(),
                listOf(
                    get<ColorTypePlugin>(),
                    get<FilesTypePlugin>(),
                    get<HtmlTypePlugin>(),
                    get<RtfTypePlugin>(),
                    get<ImageTypePlugin>(),
                    get<TextTypePlugin>(),
                    get<UrlTypePlugin>(),
                ),
                get<ScreenshotCaptureObserver>(),
            )
        }
        single<TransferableProducer> {
            DesktopTransferableProducer(
                listOf(
                    get<FilesTypePlugin>(),
                    get<HtmlTypePlugin>(),
                    get<RtfTypePlugin>(),
                    get<ImageTypePlugin>(),
                    get<TextTypePlugin>(),
                    get<UrlTypePlugin>(),
                ),
            )
        }
        // endregion

        // region Paste menu
        single<DesktopPasteMenuService> {
            DesktopPasteMenuService(
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                get(),
                contributors = listOf(get<ExtractTextMenuContributor>(), get<PluginMenuContributor>()),
            )
        }
        single<DesktopPasteTagMenuService> {
            DesktopPasteTagMenuService(get(), get())
        }
        single<ExtractTextMenuContributor> {
            ExtractTextMenuContributor(get(), get(), get(), get(), get(), get())
        }
        single<PluginMenuContributor> { PluginMenuContributor(get(), get(), get()) }
        single<ProcessPluginExecutor> {
            val cliSymlinkService = get<CliSymlinkService>()
            val copywriter = get<GlobalCopywriter>()
            ProcessPluginExecutor(
                cliBinaryPath = { cliSymlinkService.cliBinaryPath.value?.toString() },
                language = { copywriter.language() },
                extraPathDirs = pluginExtraPathDirs(get()),
            )
        }
        single<ProcessPluginRunner> {
            ProcessPluginRunner(get(), get(), get(), get(), get(), get(), get(), get(), get(), get(), get())
        }
        // endregion

        // region Import & Export
        single<PasteExportParamFactory<Path>> { DesktopPasteExportParamFactory() }
        single<PasteExportService> { PasteExportService(get(), get(), get()) }
        single<PasteImportParamFactory<Path>> { DesktopPasteImportParamFactory() }
        single<PasteImportSelection> { PasteImportSelection() }
        single<PasteImportService> { PasteImportService(get(), get(), get(), get(), get()) }
        // endregion

        // region Paste data & items
        single<GuidePasteDataService> {
            DesktopGuidePasteDataService(get(), lazy { get<AppLaunchState>() }, get(), get(), get(), get())
        }
        single<PasteDataHelper> { PasteDataHelper(get()) }
        single<PasteItemReader> { DefaultPasteItemReader() }
        single<PasteReleaseService> {
            PasteReleaseService(
                commonConfigManager = get(),
                currentPaste = get(),
                database = get(),
                notificationManager = get(),
                pasteDao = get(),
                pasteItemReader = get(),
                pastePullCursorManager = get(),
                // Plugin order is load-bearing (mobile assembles the same commonMain
                // plugins and must keep these invariants):
                // 1. RemoveInvalid must run first.
                // 2. DiscardOversizedNonFile must precede Distinct: FirstPlugin keeps only
                //    the first item per type and clears the rest from disk, so an oversized
                //    first item would otherwise destroy a smaller surviving variant before
                //    being discarded itself.
                // 3. Generate plugins run after Distinct and chain html/rtf -> text -> url/color.
                //    Any plugin placed after DiscardOversizedNonFile must never emit an item
                //    larger than its already-size-checked source, or it bypasses the limit.
                // 4. FilesToImages/FileToUrl rely on Distinct having merged multi-file items;
                //    Remove*Image must run after every plugin that can add image items.
                // 5. Sort must be last: the first item determines the row's type/hash/search content.
                pasteProcessPlugins =
                    listOf(
                        RemoveInvalidPlugin,
                        DiscardOversizedNonFilePlugin(get(), get()),
                        DistinctPlugin(get()),
                        GenerateTextPlugin(get()),
                        GenerateUrlPlugin,
                        TextToColorPlugin,
                        FilesToImagesPlugin(get()),
                        FileToUrlPlugin(get()),
                        RemoveFolderImagePlugin(get()),
                        RemoveHtmlImagePlugin(get()),
                        SortPlugin,
                    ),
                searchContentService = get(),
                syncRuntimeInfoDao = get(),
                taskSubmitter = get(),
                userDataPathProvider = get(),
            )
        }
        single<PasteSyncProcessManager<Long>> { DefaultPasteSyncProcessManager() }
        single<SearchContentService> { DesktopSearchContentService() }
        single<PasteContentEditor> { PasteContentEditor(get(), get(), get()) }
        single<UpdatePasteItemHelper> {
            UpdatePasteItemHelper(get(), get(), get())
        }
        // endregion

        // region Sync pull
        single<FilePullService> { FilePullService(get(), get(), get(), get(), get()) }
        single<PastePullCursorManager> { PastePullCursorManager(get(), get()) }
        single<PastePullService> { PastePullService(get(), get(), get(), get()) }
        single<PastePullTrigger> { PastePullTrigger(get(), get(), get()) }
        // endregion

        // region Tasks
        single<TaskExecutor> {
            TaskExecutor(
                listOf(
                    CleanPasteTaskExecutor(get(), get()),
                    CleanTaskTaskExecutor(get()),
                    DelayedDeletePasteTaskExecutor(get()),
                    DeletePasteTaskExecutor(get()),
                    OpenGraphTaskExecutor(
                        lazy { get<RenderingService<String>>(named("urlRendering")) },
                        get(),
                    ),
                    PullFileTaskExecutor(get(), get(), get(), get(), get(), get()),
                    PullIconTaskExecutor(get(), get(), get(), get()),
                    SwitchLanguageTaskExecutor(get(), get()),
                    SyncPasteTaskExecutor(get(), get(), get(), get(), get(), get(), get(), get()),
                ),
                get(),
            )
        }
        single<TaskSubmitter> { DesktopTaskSubmitter(get(), get(), get(), lazy { get() }) }
        // endregion

        // region Misc
        single<CleanScheduler> { CleanScheduler(get(), get(), get()) }
        single<GenerateImageService> { GenerateImageService() }
        single<RenderingService<String>>(named("urlRendering")) {
            OpenGraphService(get(), get(), get<ImageHandler<BufferedImage>>(), get(), get(), get(), get())
        }
        // endregion
    }
