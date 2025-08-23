# Spelet Playwright Java

## 🚀 Playwright tracing with Allure integration

This project records [Playwright](https://playwright.dev/java/) traces for each test. When a test fails, the trace is saved to disk and automatically attached to the Allure report for interactive debugging.

### Running tests
```bash
./gradlew clean test
```

### Analysing a failed test
1. Generate and open the Allure report as usual.
2. Locate a test marked as **FAILED** and download the `Playwright Trace` attachment.
3. View the trace locally with:
   ```bash
   npx playwright show-trace path/to/trace.zip
   ```
   This opens a timeline that lets you step through actions, inspect DOM snapshots and review network/console logs.

### Trace artifacts
Trace files are stored in `build/traces` using the test name for easy identification. Traces are only saved for failed tests to keep the directory clean.

The existing Allure integration remains the single source for failure analysis, now enriched with Playwright trace attachments.
