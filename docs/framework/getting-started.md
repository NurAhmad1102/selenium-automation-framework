# 🚀 Getting Started with the Automation Framework

Welcome to the **Agnostic Selenium Automation Framework**. This guide explains how to set up, configure, and create tests for any web application in minutes.

---

## 📦 1. Project Architecture

The framework is structured as a Maven multi-module project:

```text
├── framework-core/         # Core reusable framework library (100% agnostic)
├── framework-sample/       # Ready-to-run demo project and starter template
└── pom.xml                 # Parent POM managing dependencies and plugins
```

---

## 🛠️ 2. Core Concepts

### A. Foundational Base Classes
- **`BasePage`** (`com.automation.framework.core.page.BasePage`):
  Base class for all Page Objects. Provides auto-waiting, JavaScript click fallbacks, element inspection, and custom dropdown handling.
- **`BaseTest`** (`com.automation.framework.core.test.BaseTest`):
  Base class for all Test classes. Manages `WebDriver` lifecycle, thread-safe session cleanup, DevTools CDP network error capturing, and ExtentReports generation.
- **`CustomAssert` & `CustomSoftAssert`** (`com.automation.framework.core.assertion.*`):
  Custom assertions that automatically attach failure context and screenshots to your HTML reports.

---

## 💡 3. Quick Start: Create Your First Test

### Step 1: Create a Page Object
Extend `BasePage` and encapsulate locators and user interactions:

```java
package com.myproject.pages;

import com.automation.framework.core.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    private final By usernameInput = By.id("username");
    private final By passwordInput = By.id("password");
    private final By submitButton = By.cssSelector("button[type='submit']");

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    public void login(String username, String password) {
        typeText(driver.findElement(usernameInput), username);
        typeText(driver.findElement(passwordInput), password);
        click(submitButton);
    }
}
```

### Step 2: (Optional) Implement `LoginHandler`
If your application requires authentication, implement `LoginHandler`:

```java
package com.myproject.auth;

import com.automation.framework.core.account.UserAccount;
import com.automation.framework.core.auth.LoginHandler;
import com.myproject.pages.LoginPage;
import org.openqa.selenium.WebDriver;

public class AppLoginHandler implements LoginHandler {

    @Override
    public void login(WebDriver driver, UserAccount account) {
        new LoginPage(driver).login(account.getUsername(), account.getPassword());
    }

    @Override
    public boolean isLoggedIn(WebDriver driver) {
        return !driver.findElements(By.id("user-avatar")).isEmpty();
    }

    @Override
    public void logout(WebDriver driver) {
        driver.findElement(By.id("logout-button")).click();
    }
}
```

### Step 3: Create Your Base Test
Extend `BaseTest` and specify your application URL and login handler:

```java
package com.myproject.base;

import com.automation.framework.core.auth.LoginHandler;
import com.automation.framework.core.config.FrameworkConfig;
import com.automation.framework.core.test.BaseTest;
import com.myproject.auth.AppLoginHandler;

public abstract class AppBaseTest extends BaseTest {

    private final AppLoginHandler loginHandler = new AppLoginHandler();

    @Override
    protected String getUrl() {
        return FrameworkConfig.get("base.url", "https://yourapp.example.com");
    }

    @Override
    protected LoginHandler getLoginHandler() {
        return loginHandler;
    }
}
```

### Step 4: Write Your Test Case
Create standard TestNG test methods using `CustomAssert`:

```java
package com.myproject.tests;

import com.automation.framework.core.assertion.CustomAssert;
import com.automation.framework.core.driver.DriverManager;
import com.myproject.base.AppBaseTest;
import org.testng.annotations.Test;

public class LoginVerificationTest extends AppBaseTest {

    @Test(description = "Verify user can navigate and view dashboard")
    public void verifyDashboardLanding() {
        String url = DriverManager.getDriver().getCurrentUrl();
        CustomAssert.assertTrue(url.contains("/dashboard"), "Expected user to land on /dashboard");
    }
}
```

---

## ⚙️ 4. Configuration

Configurations are loaded automatically from `application.properties`, `.env`, environment variables, or System properties (`-Dproperty=value`).

Example `application.properties`:
```properties
base.url=https://staging.myapp.com
browser=chrome
headless=false
timeout.default=15
```

Override at runtime:
```bash
mvn test -Dbrowser=firefox -Dheadless=true
```
