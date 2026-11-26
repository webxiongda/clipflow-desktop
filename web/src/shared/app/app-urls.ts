/**
 * App-wide external URLs. Mirrors the desktop `AppUrls` interface.
 * Desktop reads these from `app-urls.properties`; the extension keeps
 * them as constants since it has no classpath resources.
 */
export const AppUrls = {
  homeUrl: "https://clipflow.com",
  changeLogUrl: "https://github.com/ClipFlow/clipflow-desktop/blob/main/CHANGELOG.md",
  issueTrackerUrl: "https://github.com/ClipFlow/clipflow-desktop/issues",
} as const;
