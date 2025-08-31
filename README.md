# Spelet Playwright Java

## 🚀 Playwright tracing with Allure integration

This project records [Playwright](https://playwright.dev/java/) traces for each test. When a test fails, the trace is saved to disk and automatically attached to the Allure report for interactive debugging.

### Running tests
```bash
./gradlew clean test -Dspring.profiles.active=spelet
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

## 🌐 Configuration
Environment settings are stored in profile-specific YAML files under `src/test/resources`.
Spring Boot loads `application-<profile>.yml` based on the active profile.
Each file can also control JUnit concurrency via the `env.parallelism` block.

Example `application-spelet.yml` structure:
```yaml
env:
  parallelism:
    strategy: fixed
    threads: 4
  api:
    baseUrl: https://spelet.lv
  browser:
    name: chromium
    headless: false
    language: lv
    defaultLanguage: lv
```

### Running with different profiles
- Local run (auto-applies `local` mode profile):
  ```bash
  ./gradlew test -Dspring.profiles.active=spelet
  ```
- BrowserStack run (auto-applies `browserstack` mode profile):
  ```bash
  ./gradlew bsWin10Chrome -Dspring.profiles.active=spelet-demo
  ```
- Any other profile:
  ```bash
  ./gradlew test -Dspring.profiles.active=<profile>
  ```

The `spring.profiles.active` property selects the configuration profile and is mandatory.
Gradle tasks add the appropriate mode profile (`local` or `browserstack`) automatically.
To create a new environment, add `application-myenv.yml` and run tests with
`-Dspring.profiles.active=myenv`.

## 🧩 Custom suite names in Allure

Annotate test classes or methods with `@Suite("Human readable name")` to set a
clear, business-oriented suite name in the Allure report. When both class and
method are annotated, the method-level value takes precedence. Tests without the
annotation continue to use the default class name.
