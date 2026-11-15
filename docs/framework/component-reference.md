# Framework Component Reference Manual

Comprehensive technical documentation for modules, components, classes, and interfaces in `framework-core`.

---

## 1. Package and Component Overview

```text
com.automation.framework.core/
├── account/         # Account management and thread-safe multi-user pooling
├── assertion/       # Custom assertions and soft assertions with automated screenshots
├── auth/            # Pluggable authentication interfaces and login handlers
├── config/          # Cascading configuration hierarchy resolution
├── data/            # Test data reader abstractions (JSON, Excel, properties)
├── driver/          # WebDriver lifecycle management and Developer Mode (DevMode)
├── listener/        # Global TestNG listeners and orphaned browser process cleanup
├── network/         # Chrome DevTools Protocol (CDP) network monitor for HTTP 4xx/5xx errors
├── page/            # BasePage and custom UI components (dropdowns, Radix UI)
├── report/          # Event bus and 3-tab ExtentReports renderer
├── table/           # Dynamic HTML table parsing utilities
├── test/            # Agnostic base test class (BaseTest)
└── utils/           # Selenium helper utilities and screenshot capturing
```

---

## 2. Component Details and Capabilities

### 2.1. Configuration Layer (`com.automation.framework.core.config`)

* **Primary Class**: `FrameworkConfig`
* **Purpose**:
  Provides centralized configuration resolution using a cascading priority hierarchy:
  System Properties (-D) -> Environment Variables -> .env -> application-{env}.properties -> framework.properties
* **Key Methods**:
  * `FrameworkConfig.get(String key, String defaultValue)`
  * `FrameworkConfig.getBoolean(String key, boolean defaultValue)`
  * `FrameworkConfig.getInt(String key, int defaultValue)`
  * `FrameworkConfig.getBrowser()`: Retrieves the target browser (default: `chrome`).
  * `FrameworkConfig.isHeadless()`: Indicates whether headless mode is enabled (default: `false`).
  * `FrameworkConfig.isDevMode()`: Indicates whether Developer Mode is enabled (default: `false`).
  * `FrameworkConfig.getDevPort()`: Retrieves the remote debugging port (default: `9222`).

---

### 2.2. Driver Management (`com.automation.framework.core.driver`)

* **Primary Classes**:
  * `DriverManager`: Stores `WebDriver` instances inside `ThreadLocal<WebDriver>` to guarantee thread safety during parallel test runs.
  * `DriverFactory`: Instantiates new `WebDriver` objects (supporting Chrome, Firefox, and Edge) with isolated browser profiles, anti-detection flags, and timeout management.
  * `DevModeUtils`: Connects Selenium directly to an active browser session on port `9222`, allowing instant test execution without restarting browsers or repeating logins.
* **Key Methods**:
  * `DriverManager.getDriver()`: Returns the WebDriver instance for the current execution thread.
  * `DriverManager.setDriver(WebDriver driver)`: Binds a WebDriver instance to the current thread.
  * `DriverManager.quitDriver()`: Closes the browser session and removes the ThreadLocal reference.
  * `DevModeUtils.isPortAvailable(int port)`: Checks whether the debugging port is available.

---

### 2.3. Page Object and UI Component Engine (`com.automation.framework.core.page`)

* **Primary Classes**:
  * `BasePage`: Abstract base class for all Page Objects. Provides safe wrapper methods for UI actions with built-in explicit waits (default 15 seconds), automatic JavaScript scroll, click interception fallback (JavaScript click), and diagnostic error reporting.
  * `DropdownComponent`: Handles non-standard dropdowns (such as Radix UI, Headless UI, Material UI, or custom listboxes) that use `role="combobox"` or `role="option"` instead of native HTML `<select>` tags.
* **Key Methods in BasePage**:
  * `click(By locator)` / `click(WebElement element)`: Waits until the element is clickable, attempts a standard click, and falls back to a JavaScript click if intercepted by an overlay.
  * `typeText(By locator, String text)` / `typeText(WebElement element, String text)`: Waits for visibility, clears existing text, and enters input (alias: `type`).
  * `getText(By locator)` / `getText(WebElement element)`: Waits for visibility and returns the visible text.
  * `isVisible(By locator)` / `isVisible(WebElement element)`: Returns `true` if visible, and safely returns `false` without throwing `TimeoutException` (alias: `isDisplayed`).
  * `isNotVisible(By locator)` / `isNotVisible(WebElement element)`: Verifies that an element is hidden or absent from the DOM.
  * `isElementClickable(By locator)` / `isElementClickable(WebElement element)`: Checks whether an element is enabled and clickable.
  * `scrollMoveToElement(By locator)` / `scrollMoveToElement(WebElement element)`: Scrolls the element into the viewport using JavaScript.

---

### 2.4. Pluggable Authentication (`com.automation.framework.core.auth`)

* **Interface**: `LoginHandler`
* **Default Implementation**: `NoOpLoginHandler`
* **Purpose**:
  Decouples application login logic from framework test lifecycle. Each test project can implement its own login procedure (form login, SSO, API token injection) without modifying core framework code.
* **Contract Methods**:
  * `void login(WebDriver driver, UserAccount account)`
  * `boolean isLoggedIn(WebDriver driver)`
  * `void logout(WebDriver driver)`

---

### 2.5. Account Pool and Multi-User Allocation (`com.automation.framework.core.account`)

* **Interface and Class**:
  * `UserAccount`: Data contract for user credentials (`getUsername()`, `getPassword()`, `getRole()`, `getIdentifier()`).
  * `AccountAllocator`: Thread-safe account manager supporting role-based leasing, atomic reservation to prevent parallel collisions, and automatic account release during test teardown.
* **Key Methods**:
  * `AccountAllocator.registerAccounts(Collection<? extends UserAccount> accounts)`
  * `AccountAllocator.allocate(String role)`
  * `AccountAllocator.useAccount(String alias, String testName, String role)`
  * `AccountAllocator.release(UserAccount account)`
  * `AccountAllocator.releaseAllAccounts()`

---

### 2.6. Reporting Subsystem and CDP Network Monitor (`com.automation.framework.core.report` & `network`)

* **Architecture**:
  * `ReportAPI`: Public facade API for recording test steps and status events.
  * `ReportEventBus`: Thread-safe event bus that distributes log events to report engines.
  * `ExtentImplementation` and `ExtentTabRenderer`: Generate a custom 3-Tab interactive HTML report:
    1. **Tab 1: DETAILS**: Chronological step-by-step test execution log with timestamps.
    2. **Tab 2: ELEMENT LOGS**: Six diagnostic failure pillars (computed CSS, bounding box, blocker overlay elements, outerHTML, and filtered stack trace).
    3. **Tab 3: NETWORK LOGS**: Backend HTTP errors (4xx, 5xx) captured in real time via Chrome DevTools Protocol (CDP).
  * `NetworkMonitor`: Uses CDP (`Network.enable`, `Network.responseReceived`) to track API requests without requiring an external proxy server.

---

### 2.7. Custom Assertions (`com.automation.framework.core.assertion`)

* **Classes**: `CustomAssert` and `CustomSoftAssert`
* **Purpose**:
  Extends standard TestNG assertions by automatically logging results to `ReportAPI` and attaching full-page screenshots whenever a verification fails.

---

### 2.8. Data Readers (`com.automation.framework.core.data`)

* **Interface**: `DataReader<T>`
* **Implementations**:
  * `JsonDataReader`: Maps JSON objects or arrays into Java POJO models using Gson.
  * `ExcelDataReader`: Reads `.xlsx` spreadsheets into `List<Map<String, String>>` using Apache POI.
  * `ResourcePathResolver`: Resolves file paths across classpath resources, working directories, and absolute file system locations.

---

### 2.9. Lifecycle Listeners and Process Cleanup (`com.automation.framework.core.listener`)

* **Classes**:
  * `FrameworkListener`: Global TestNG listener handling report initialization, custom tab assets, and suite summary generation.
  * `BrowserProcessCleaner`: Sweeper utility that registers a JVM shutdown hook to terminate orphaned browser processes (`chromedriver.exe`, `chrome.exe`) and cleans up temporary browser profiles.

---

### 2.10. Generic Test Base (`com.automation.framework.core.test.BaseTest`)

* **Purpose**:
  Abstract base class for test execution. Manages the WebDriver lifecycle, initializes CDP monitoring, performs automatic login (optional), captures failure diagnostics and screenshots, and ensures clean resource teardown in `@BeforeMethod` and `@AfterMethod`.
