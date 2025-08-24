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

## 📊 How to read the enhanced Allure report

Our Allure report now serves as a comprehensive diagnostic dashboard for every test run.

### 1. Performance analysis (all tests)
Open any test case. In the **Test Body** section you'll find a tree of nested steps. Each step displays its execution time, helping to spot slow areas in the application or test code.

### 2. Failed test analysis (three levels)
When a test fails, open it in Allure and inspect the following attachments:

1. **Visual context** – check the `Screenshot` and `Current URL` attachments to see what the user saw at the moment of failure.
2. **Frontend context** – review the `Browser Console Logs` attachment. Look for `[ERROR]` messages that often explain the root cause (JavaScript exceptions, network issues, etc.).
3. **Deep analysis** – download the `Playwright Trace` (`.zip`) and open it locally with:
   ```bash
   npx playwright show-trace <trace-file>
   ```
   This provides an interactive timeline for step-by-step debugging.

Only failed tests include console logs and trace attachments to keep reports lightweight.
