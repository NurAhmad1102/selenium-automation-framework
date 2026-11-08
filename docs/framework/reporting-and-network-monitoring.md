# 📊 Reporting & Chrome DevTools Network Diagnostics

The framework includes a custom 3-tab HTML test execution report powered by **ExtentReports** with custom CSS/JS styling and automatic diagnostic inspection.

---

## 📑 3-Tab Report Structure

Reports are generated at `reports/index.html` after every execution.

```text
┌────────────────────────────────────────────────────────┐
│  [DETAILS]   |   [ELEMENT LOGS]   |   [NETWORK LOGS]   │
└────────────────────────────────────────────────────────┘
```

### 1. `DETAILS` Tab
- Displays sequential step-by-step test execution progress.
- Clean badges for `ACTION` and `CHECK`.
- Highlights user values, parameters, and locators in distinct color-coded chips.

### 2. `ELEMENT LOGS` Tab
When an element failure occurs (e.g. `TimeoutException`, `ElementClickInterceptedException`, or `AssertionError`), this tab displays the **6 Diagnostic Pillars**:
1. **Locator Identity**: Strategy (XPath, CSS, By) & resolved locator value with a one-click copy button.
2. **Execution Context & Wait Metadata**: Action type, timeout duration, elapsed time.
3. **State Snapshot & Computed Styles**: `isDisplayed`, `isEnabled`, `isSelected`, Bounding Box coordinates, CSS visibility, display, opacity, and pointer-events.
4. **Intercept / Obstruction Details**: Identifies exactly which modal backdrop, spinner, or toast overlay blocked the click.
5. **DOM Snippet**: OuterHTML of the element or active modal container.
6. **Error Signature**: Filtered concise exception message and interactive, syntax-highlighted stacktrace.

### 3. `NETWORK LOGS` Tab
- Automatically captures failed HTTP requests (**HTTP 4xx & 5xx**) via the Chrome DevTools Protocol (CDP).
- Captures request URL, HTTP method, status code, status text, request payload, and response body.
- Automatically normalizes path parameters (UUIDs and IDs) into `{uuid}` and `{id}` to aggregate identical failing endpoints.
- Displays only network failures that occurred **prior to or at the moment of test failure**, preventing unrelated background noise from cluttering logs.

---

## 📸 Automated Screenshots

Screenshots are captured automatically:
1. Whenever a test method fails or is skipped due to a broken precondition.
2. On each failed soft assertion in `CustomSoftAssert`.
3. Manually via `ReportAPI.addScreenshot(path)`.

Screenshots are embedded cleanly in `reports/capture/{timestamp}/` and rendered with hover-zoom in the report gallery.
