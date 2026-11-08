# ⚡ Developer Mode (DevMode) Guide

Developer Mode allows automated tests to attach directly to an **already running, authenticated Chrome or Edge browser window** without:
- Opening a new browser window.
- Re-executing tedious login steps.
- Closing the browser when the test finishes (`driver.quit()` is skipped).

This drastically reduces debugging iteration time from minutes to seconds!

---

## 🚀 How to Use Developer Mode

### 1. Launch Browser with Remote Debugging Port

Open terminal or Command Prompt and run Chrome or Edge with `--remote-debugging-port=9222`:

```bash
# Google Chrome (Windows)
chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"

# Microsoft Edge (Windows)
msedge.exe --remote-debugging-port=9222 --user-data-dir="C:\edge-dev-profile"
```

> **Tip**: Log into your application manually in this browser window and navigate to the page you are developing tests for.

---

### 2. Activate DevMode in Tests

You can activate DevMode through any of the following 3 methods:

#### Method A: Maven Command Line Flag
```bash
mvn test -DdevMode=true -Dtest=MySpecificTest
```

#### Method B: Root `.env` file
Create or edit `.env` in the project root:
```env
DEV_MODE=true
DEV_PORT=9222
```

#### Method C: OS Environment Variable
```powershell
$env:DEV_MODE="true"
$env:DEV_PORT="9222"
```

---

## 🛠️ Behavior in DevMode

When `devMode=true`:
1. `DriverFactory` connects to `127.0.0.1:9222` instead of launching a new browser process.
2. If multiple tabs are open, `DriverFactory` automatically focuses on your active application tab.
3. `BaseTest.setUp()` skips initial URL navigation and login if already on the application screen.
4. `BaseTest.tearDown()` skips `driver.quit()`, keeping the window open for immediate inspection and repeated runs.
