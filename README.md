# Spelet Playwright Java

## 🚀 Playwright HTML report with tracing

This project is configured to record [Playwright](https://playwright.dev/java/) traces for each test and
produce an interactive HTML report. The report helps debug failed UI tests by
providing full context: DOM snapshots, network activity, console logs and
screenshots.

### Running tests
```bash
./gradlew clean test
```
After the test run finishes, Gradle will automatically attempt to open the
Playwright HTML report in your default browser. The generated files are located
under `playwright-report/index.html`.

### Analysing a failed test
1. Open the HTML report and locate a test marked as **FAILED**.
2. Expand the test details and click the **Trace** icon.
3. A new tab with the Playwright Trace Viewer will open. Use it to step through
the test actions, inspect DOM snapshots, review network requests and console
logs.

### Trace artifacts
Trace files are stored in `build/traces` with unique names for each test. These
artifacts can be kept for later analysis or attached to CI job logs.

The existing Allure integration remains untouched and can continue to be used
alongside the Playwright HTML report.
