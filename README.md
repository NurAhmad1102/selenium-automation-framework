# Selenium 4 Automation Framework

[![CI Build](https://github.com/NurAhmad1102/automations-arjuna/actions/workflows/ci.yml/badge.svg)](https://github.com/NurAhmad1102/automations-arjuna/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-blue?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.44.0-43B02A?style=flat-square&logo=selenium)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red?style=flat-square)](https://testng.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

Modular web automation framework built on **Java 17** and **Selenium 4**. It serves as a reliable, reusable foundation for web applications without requiring you to build testing infrastructure from scratch.

---

## Table of Contents
- [Tools and Technologies](#tools-and-technologies)
- [Architecture and Project Structure](#architecture-and-project-structure)
- [Installation and Usage in Your Project](#installation-and-usage-in-your-project)
- [Core Component Functions](#core-component-functions)
- [System Requirements](#system-requirements)
- [Step-by-Step Quick Start](#step-by-step-quick-start)
- [Key Features](#key-features)
  - [Developer Mode (DevMode)](#1-developer-mode-devmode---save-debugging-time)
  - [3-Tab Interactive Reporting & CDP Error Capture](#2-3-tab-interactive-reporting-and-cdp-error-capture)
  - [Multi-User Account Pooling](#3-multi-user-account-pooling-thread-safe)
  - [Modern UI Components](#4-modern-ui-component-support)
- [CI/CD Integration](#cicd-integration)
- [Troubleshooting & FAQ](#troubleshooting--faq)
- [Documentation Index](#documentation-index)
- [License](#license)

---

## Tools and Technologies

This framework integrates proven libraries from the Java and QA automation ecosystem:

| Tool / Library | Version | Role and Purpose |
|----------------|---------|------------------|
| **Java SDK** | `17 (LTS)` | Core programming language offering modern performance, Records, and Pattern Matching. |
| **Selenium WebDriver** | `4.44.0` | Browser automation engine (Chrome, Firefox, Edge) following W3C WebDriver standards. |
| **TestNG** | `7.10.2` | Test runner, lifecycle management (`@Test`, `@BeforeMethod`, `@DataProvider`), and parallel execution. |
| **ExtentReports** | `5.0.9` | Interactive HTML reporting engine customized into a 3-tab diagnostic interface. |
| **Chrome DevTools Protocol (CDP)** | `v148+` | Real-time network traffic monitoring for automatic backend HTTP 4xx/5xx error detection. |
| **Apache POI** | `5.3.0` | Utility for reading test data from Microsoft Excel (`.xlsx`) files. |
| **Google Gson** | `2.12.1` | JSON serialization and deserialization into Java models and POJOs. |
| **Log4j2** | `2.24.3` | High-performance asynchronous structured logging with granular levels (INFO, DEBUG, ERROR). |
| **ArchUnit** | `1.4.1` | Architecture testing tool to guarantee that core modules remain strictly decoupled and agnostic. |
| **Lombok** | `1.18.30` | Boilerplate reduction for data classes (`@Data`, `@Getter`, `@Builder`). |
| **Apache Maven** | `3.8+` | Build tool, dependency management, and multi-module lifecycle orchestration. |
| **GitHub Actions** | `v4` | Automated CI/CD pipeline for headless test execution on cloud Linux runners. |

---

## Architecture and Project Structure

The project uses a standard **Maven Multi-Module** structure:

```text
automations-framework/
├── .github/workflows/
│   └── ci.yml                          # GitHub Actions CI Pipeline
│
├── framework-core/                     # CORE LIBRARY (100% Agnostic, Reusable)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/automation/framework/core/
│       │   ├── account/                # UserAccount, AccountAllocator (Thread-safe pool)
│       │   ├── assertion/              # CustomAssert & CustomSoftAssert (Auto-screenshot)
│       │   ├── auth/                   # LoginHandler & NoOpLoginHandler (Pluggable auth)
│       │   ├── config/                 # FrameworkConfig (Cascading configuration)
│       │   ├── data/                   # DataReader, JsonDataReader, ExcelDataReader, ResourcePathResolver
│       │   ├── driver/                 # DriverManager, DriverFactory, DevModeUtils
│       │   ├── listener/               # FrameworkListener, BrowserProcessCleaner
│       │   ├── network/                # NetworkMonitor (CDP), CdpVersionHelper, ApiError
│       │   ├── page/                   # BasePage, DropdownComponent (Radix/Listbox)
│       │   ├── report/                 # ReportAPI, ReportEventBus, ExtentTabRenderer (3-Tab HTML)
│       │   ├── table/                  # WebTable (Dynamic HTML Table parser)
│       │   ├── test/                   # BaseTest (Generic test lifecycle & teardown)
│       │   └── utils/                  # SeleniumUtils (Screenshot & file helpers)
│       └── test/java/com/automation/framework/core/
│           ├── account/                # AccountAllocatorTest
│           ├── architecture/           # CoreArchitectureTest (ArchUnit boundary tests)
│           ├── config/                 # FrameworkConfigTest
│           └── report/                 # OutcomeResolverTest
│
├── framework-sample/                   # STARTER TEMPLATE & DEMO APPLICATION
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/automation/framework/sample/
│       │   ├── auth/                   # SampleLoginHandler
│       │   └── pages/                  # SampleSearchPage (extends BasePage)
│       └── test/java/com/automation/framework/sample/
│           ├── base/                   # SampleBaseTest (extends BaseTest)
│           ├── resources/              # application.properties, log4j2.xml, sample-suite.xml
│           └── tests/                  # SampleSearchTest (End-to-End Test Case)
│
├── docs/framework/                     # DOCUMENTATION & TECHNICAL GUIDES
│   ├── getting-started.md              # Initial onboarding guide
│   ├── page-object-pattern.md          # Page Object and component pattern guide
│   ├── test-data-management.md         # JSON and Excel DataReader guide
│   ├── developer-mode.md               # Developer Mode guide (port 9222)
│   ├── reporting-and-network-monitoring.md # 3-Tab ExtentReports and CDP network guide
│   ├── multi-account-and-parallel-testing.md # Account pooling and thread-safety guide
│   ├── ci-cd-pipeline.md               # CI/CD guide for GitHub Actions and Jenkins
│   ├── migration-guide.md              # Migration guide from legacy monolithic frameworks
│   └── component-reference.md          # Complete technical component reference manual
│
├── pom.xml                             # Root Parent POM (framework-parent)
├── LICENSE                             # Open Source License (Apache 2.0)
└── README.md                           # Main project documentation
```

---

## Installation and Usage in Your Project

You can integrate `framework-core` into your automated test projects using one of the following approaches:

### Option 1: Local Maven Installation (Recommended for Teams)
Clone this repository and install `framework-core` into your local Maven cache (`~/.m2/repository`):

```bash
git clone https://github.com/NurAhmad1102/automations-arjuna.git
cd automations-arjuna
mvn clean install -DskipTests
```

Then in your test project's `pom.xml`, add the `framework-core` dependency:

```xml
<dependency>
    <groupId>com.automation.framework</groupId>
    <artifactId>framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Option 2: Using JitPack (Without Private Artifact Servers)
If this repository is hosted on GitHub, projects can consume it directly via [JitPack](https://jitpack.io):

1. Add the JitPack repository to your `pom.xml`:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Add the dependency using JitPack's multi-module format:
```xml
<dependency>
    <groupId>com.github.NurAhmad1102.automations-arjuna</groupId>
    <artifactId>framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Option 3: Use `framework-sample` as a Starter Template
To start a new test suite quickly without setting up a POM file from scratch:
1. Clone this repository.
2. Use the `framework-sample` module as your starter template.
3. Update `base.url` in `src/test/resources/application.properties`, and write your Page Objects and tests.

---

## Core Component Functions

Every component is built around **Single Responsibility**, **High Cohesion**, and **Loose Coupling**:

### 1. Centralized Configuration Layer (`FrameworkConfig`)
Uses a cascading priority hierarchy with standardized fallbacks:
$$\text{CLI System Properties } (-D) \longrightarrow \text{Environment Variables} \longrightarrow \text{.env File} \longrightarrow \text{application.properties} \longrightarrow \text{framework.properties}$$
Timeouts, target browsers, base URLs, and headless flags can all be changed at runtime.

### 2. Driver Management (`DriverManager`, `DriverFactory`, `DevModeUtils`)
* **Thread Safety**: Stores WebDriver instances inside `ThreadLocal<WebDriver>`.
* **Multi-Browser**: Supports Chrome, Firefox, and Microsoft Edge with isolated browser profiles.
* **DevMode**: Connects Selenium directly to an active browser session on port 9222.

### 3. Page Object and Component Engine (`BasePage`, `DropdownComponent`, `WebTable`)
* **Resilient Actions**: Methods like `click()`, `typeText()`, and `getText()` include built-in explicit waits and auto-scroll (with `type()` and `isDisplayed()` aliases).
* **Auto JavaScript Fallback**: If an overlay intercepts a native click, `BasePage` automatically triggers a JavaScript click fallback.
* **Custom Components**: First-class support for virtualized dropdowns (Radix UI, Headless UI) and dynamic HTML tables.

### 4. 3-Tab Interactive Reporting and CDP Error Capture (`ReportAPI`, `ExtentTabRenderer`, `NetworkMonitor`)
Generates an HTML report organized into three distinct tabs:
* **Tab 1: DETAILS**: Chronological execution logs with timestamps and status badges.
* **Tab 2: ELEMENT LOGS**: Six-pillar diagnostics on element failure (computed CSS, bounding box, blocker overlay, outerHTML, and simplified stack trace).
* **Tab 3: NETWORK LOGS**: Captures HTTP 4xx and 5xx API errors directly via Chrome DevTools Protocol without external proxies.

### 5. Multi-User Account Allocator (`AccountAllocator`, `UserAccount`)
Thread-safe, role-based account pool management. Prevents account collisions during parallel test runs and supports multi-stage workflows (such as Maker to Checker to Approver).

### 6. Pluggable Authentication (`LoginHandler`)
Decouples login procedures from the core framework. Tests can use form login, session cookies, or API token injection by implementing the `LoginHandler` interface.

### 7. Data Readers (`DataReader<T>`, `JsonDataReader`, `ExcelDataReader`)
Reads test data from JSON files directly into Java models or from Excel spreadsheets (`.xlsx`) into `List<Map<String, String>>`.

### 8. Cleanup and Anti-Leak Sweeper (`BrowserProcessCleaner`)
Automatically terminates orphaned browser and driver processes (`chromedriver.exe`, `chrome.exe`) and cleans temporary profile folders after test runs.

---

## System Requirements

Before running the project, ensure your environment has:
* **Java Development Kit (JDK)**: Version 17 or higher ([Download OpenJDK 17](https://adoptium.net/)).
* **Apache Maven**: Version 3.8.0 or higher ([Download Maven](https://maven.apache.org/download.cgi)).
* **Web Browser**: Latest Google Chrome (or Firefox / Edge).
* **Git**: Version 2.30+.

Verify your installation:
```bash
java -version
mvn -version
```

---

## Step-by-Step Quick Start

Here is a complete walkthrough for creating and running an end-to-end test.

### Step 1: Create a Page Object Class
Extend `com.automation.framework.core.page.BasePage`. Encapsulate locators and element interactions here:

```java
package com.mycompany.pages;

import com.automation.framework.core.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchPage extends BasePage {

    // 1. Define private locators
    private final By searchInputBox = By.name("q");
    private final By searchSubmitBtn = By.name("btnK");
    private final By searchResultsContainer = By.id("search");

    // 2. Constructor passes WebDriver to super
    public SearchPage(WebDriver driver) {
        super(driver);
    }

    // 3. Define clear action methods
    public SearchPage enterKeyword(String keyword) {
        typeText(searchInputBox, keyword);
        return this;
    }

    public SearchPage submitSearch() {
        click(searchSubmitBtn);
        return this;
    }

    public boolean isResultDisplayed() {
        return isVisible(searchResultsContainer);
    }
}
```

---

### Step 2: Configure Test Parameters (`application.properties`)
Create or edit `src/test/resources/application.properties`:

```properties
# Target browser: chrome, firefox, edge
browser=chrome

# Headless mode: true for CI/CD, false for local inspection
headless=false

# Application base URL
base.url=https://example.com

# Default test timeout in seconds
timeout.default=15
```

---

### Step 3: Create an Application Base Test Class
Extend `com.automation.framework.core.test.BaseTest` to define your target URL or login handler:

```java
package com.mycompany.base;

import com.automation.framework.core.config.FrameworkConfig;
import com.automation.framework.core.test.BaseTest;

public abstract class ApplicationBaseTest extends BaseTest {

    @Override
    protected String getUrl() {
        return FrameworkConfig.get("base.url", "https://example.com");
    }
}
```

> [!NOTE]
> `BaseTest` automatically reads `base.url` from configuration. Overriding `getUrl()` is optional and only needed for custom dynamic logic.

---

### Step 4: Write Your TestNG Test Case
Extend your base test and write tests using `CustomAssert` to automatically capture screenshots on failure:

```java
package com.mycompany.tests;

import com.automation.framework.core.assertion.CustomAssert;
import com.automation.framework.core.driver.DriverManager;
import com.automation.framework.core.report.core.ReportAPI;
import com.mycompany.base.ApplicationBaseTest;
import com.mycompany.pages.SearchPage;
import org.testng.annotations.Test;

public class SearchWorkflowTest extends ApplicationBaseTest {

    @Test(description = "Verify keyword search and results display")
    public void testSearchFunctionality() {
        ReportAPI.step("Initialize SearchPage object");
        SearchPage searchPage = new SearchPage(DriverManager.getDriver());

        ReportAPI.step("Enter search query");
        searchPage.enterKeyword("Selenium Automation Framework");

        ReportAPI.step("Submit search");
        searchPage.submitSearch();

        ReportAPI.step("Validate that results are displayed");
        CustomAssert.assertTrue(
            searchPage.isResultDisplayed(),
            "Search results should be displayed after submission"
        );
    }
}
```

---

### Step 5: Execute Tests

#### Using Maven CLI:
```bash
# Run all tests
mvn test

# Run in headless mode
mvn test -Dheadless=true

# Run with Firefox
mvn test -Dbrowser=firefox
```

#### Using a TestNG Suite XML File:
```bash
mvn test -DsuiteXmlFile=src/test/resources/testng/sample-suite.xml
```

---

### Step 6: Review the Interactive Report
After test execution, HTML reports are generated automatically in the `reports/` directory:
```text
reports/
└── ExtentReport_20261108_161500.html
```
Open this file in any web browser to inspect execution steps, element diagnostics, and captured API errors.

---

## Key Features

### 1. Developer Mode (DevMode) - Save Debugging Time
When developing tests for pages that require complex multi-step login flows, you can avoid logging in repeatedly for every test run:

1. Launch Chrome with remote debugging enabled:
   ```bash
   chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"
   ```
2. Manually log in to your application and navigate to the target test page.
3. Run your test with the `-DdevMode=true` flag:
   ```bash
   mvn test -DdevMode=true -Dtest=SearchWorkflowTest
   ```
> **Result**: Selenium attaches directly to the open browser tab without launching a new window, without repeating login, and without closing the browser when the test ends.

---

### 2. 3-Tab Interactive Reporting and CDP Error Capture
The execution report provides comprehensive diagnostic insight:
* **DETAILS**: Structured step-by-step execution log with timestamps.
* **ELEMENT LOGS**: Diagnoses NoSuchElement or ElementClickIntercepted failures, including computed CSS styles and blocker element coordinates.
* **NETWORK LOGS**: Captures HTTP 400, 401, 403, 500, and 502 responses directly via Chrome DevTools Protocol without proxy configuration.

---

### 3. Multi-User Account Pooling (Thread-Safe)
Manage user accounts safely in parallel multi-threaded executions:

```java
// Option A: Programmatic manual allocation
UserAccount admin = AccountAllocator.allocate("ADMIN");
try {
    // Perform admin actions
} finally {
    AccountAllocator.release(admin);
}

// Option B: Lifecycle-managed allocation via BaseTest (Recommended)
public class AdminWorkflowTest extends ApplicationBaseTest {
    @Override
    protected String getRole() {
        return "ADMIN"; // Automatically allocated in setUp and released in tearDown
    }

    @Test
    public void testAdminFeature() {
        UserAccount admin = AccountAllocator.getCurrentAccount();
        // Execute test steps
    }
}
```

---

### 4. Modern UI Component Support
For applications built with modern frontend frameworks (React, Vue, Angular, Radix UI, Shadcn, Headless UI):

```java
import com.automation.framework.core.page.DropdownComponent;
import com.automation.framework.core.table.WebTable;

// Select from a Radix UI or Shadcn custom virtualized dropdown
DropdownComponent dropdown = new DropdownComponent(driver, By.id("organization-select-trigger"));
dropdown.searchAndSelect("Finance", "Finance Division Jakarta");

// Read and interact with dynamic HTML tables
WebTable table = new WebTable(driver, By.id("users-table"));
int rowIndex = table.findRowIndexByCellValue("Email", "john.doe@example.com");
table.getCellElement(rowIndex, "Action").findElement(By.cssSelector(".btn-edit")).click();
```

---

## CI/CD Integration

Ready-to-use pipeline configurations are available for **GitHub Actions** and **Jenkins**.

### GitHub Actions
The pre-configured workflow file is located at [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
It installs Google Chrome, runs multi-module builds, validates ArchUnit architecture rules, and executes headless tests on every `push` or `pull_request`.

---

## Troubleshooting & FAQ

### 1. `IllegalStateException: chromedriver must be executable`
* **Cause**: File permissions or corrupted driver cache.
* **Solution**: Selenium 4 manages drivers automatically through Selenium Manager. Clear the cache directory at `~/.cache/selenium` and run `mvn test` again.

### 2. `DevMode Connection Refused (port 9222)`
* **Cause**: Chrome was not started with `--remote-debugging-port=9222`.
* **Solution**: Close all existing Chrome windows, then run:
  ```bash
  chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"
  ```

### 3. `ElementClickInterceptedException`
* **Solution**: Methods in `BasePage` automatically fall back to JavaScript clicks. If an element is blocked by a backdrop or spinner, the framework attempts a native click and then executes a fallback JavaScript click to avoid premature failure.

---

## Documentation Index

Explore detailed documentation in [`docs/framework/`](docs/framework/):

| Document | Description |
|----------|-------------|
| [Getting Started](docs/framework/getting-started.md) | Onboarding guide for new team members. |
| [Component Reference](docs/framework/component-reference.md) | Comprehensive reference for classes and interfaces in framework-core. |
| [Page Object Guide](docs/framework/page-object-pattern.md) | Design patterns for Page Objects, BasePage, and WebTable. |
| [Test Data Management](docs/framework/test-data-management.md) | Data-driven testing with JSON and Excel files. |
| [Developer Mode Guide](docs/framework/developer-mode.md) | Remote debugging on port 9222 for rapid test authoring. |
| [Reporting & CDP Guide](docs/framework/reporting-and-network-monitoring.md) | 3-tab report architecture and network log capture. |
| [Multi-Account & Parallel](docs/framework/multi-account-and-parallel-testing.md) | Thread-safe account pooling and parallel test execution. |
| [CI/CD Pipeline Guide](docs/framework/ci-cd-pipeline.md) | Pipeline setup for GitHub Actions and Jenkins. |
| [Migration Guide](docs/framework/migration-guide.md) | Step-by-step guide for migrating legacy Selenium projects. |

---

## License

This project is open-source software licensed under the **Apache License 2.0**.
See [LICENSE](LICENSE) for full copyright and licensing details.
