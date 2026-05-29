# Panduan Arsitektur Automation Test Framework

> Stack: Java 17, Selenium 4, TestNG, Maven, ExtentReports, Log4j2, Apache POI

---

## Daftar Isi

1. [Gambaran Umum Arsitektur](#1-gambaran-umum-arsitektur)
2. [Flow Pemanggilan: Test ke PageObject](#2-flow-pemanggilan-test-ke-pageobject)
3. [Cara Menambah Module Baru](#3-cara-menambah-module-baru)
4. [Penggunaan Enum FrontOfficeMenu](#4-penggunaan-enum-frontofficemenu)
5. [Struktur Direktori](#5-struktur-direktori)
6. [Architecture Rules (ArchUnit)](#6-architecture-rules-archunit)
7. [Scalability & Maintenance](#7-scalability--maintenance-best-practice)
8. [Network Monitoring (CDP)](#8-network-monitoring-cdp)
9. [Enhanced Error Logging & CustomAssert](#9-enhanced-error-logging--customassert)
10. [Account Allocator & Excel Data](#10-account-allocator--excel-data)
11. [Log4j2 Bridge (ExtentReportAppender)](#11-log4j2-bridge-extentreportappender)
12. [Informasi Tambahan](#12-informasi-tambahan)
8. [Network Monitoring (CDP)](#8-network-monitoring-cdp)
9. [Enhanced Error Logging & CustomAssert](#9-enhanced-error-logging--customassert)
10. [Account Allocator & Excel Data](#10-account-allocator--excel-data)
11. [Log4j2 Bridge (ExtentReportAppender)](#11-log4j2-bridge-extentreportappender)
12. [Informasi Tambahan](#12-informasi-tambahan)

---

## 1. Gambaran Umum Arsitektur

Framework ini menggunakan pola **Page Object Model (POM)** dikombinasikan dengan **Factory Pattern** dan **Thread-Local Singleton** untuk mendukung eksekusi paralel yang aman.

```
+------------------------------------------------------------------+
|                        LAYER DIAGRAM                             |
+------------------------------------------------------------------+
|  [TEST LAYER]      XxxTest                                       |
|                         |                                        |
|  [BASE TEST]       XxxBaseTest (BackOffice / FrontOffice)        |
|                    BaseTest                                      |
|                         |                                        |
|  [FACTORY LAYER]   BackOfficeMainPagesFactory                    |
|                    FrontOfficeMainPagesFactory                   |
|                    ModuleFactory (e.g. ManageOrganizationFactory)|
|                                                                  |
|  [PAGE OBJECT]     XxxPage extends BasePage                      |
|                                                                  |
|  [INFRA]           DriverManager (ThreadLocal<WebDriver>)        |
|                    AccountAllocator (ThreadLocal<UserloginConfig>)|
|                    NetworkMonitor (CDP)                          |
|                    ReportAPI (Logging & Reporting)               |
+------------------------------------------------------------------+
```

---

## 2. Flow Pemanggilan: Test ke PageObject

### Diagram Back Office

```
SlaSettingTest.java
        |
        | extends
        v
SlaSettingBaseTest.java  (abstract)
        | @BeforeMethod: login + navigasi ke SLA Setting
        |
        | extends
        v
BackOfficeBaseTest.java
        | (setUp() -> new BackOfficeMainPagesFactory(driver))
        | (menyediakan: getMainPages())
        | extends
        v
BaseTest.java
        | @BeforeMethod setUp():
        |   AccountAllocator.allocateAccount(testName, role)
        |   new ChromeDriver(options)
        |   DriverManager.setDriver(driver)
        |   NetworkMonitor.startMonitoring()
        |   driver.get(targetUrl)
        |
        | @AfterMethod tearDown():
        |   NetworkMonitor.flushServerErrorsToReport()
        |   finalizeReport()  -> ReportAPI.flush()
        |   DriverManager.quitDriver()
        |   AccountAllocator.releaseAccount()
        |
        v
BackOfficeMainPagesFactory.java
        | (lazy init semua module factory)
        |-- login()              -> BackOfficeLoginPagesFactory
        |-- sideMenu()           -> SideMenuFactory
        |-- manageOrganization() -> ManageOrganizationFactory (BO)
        +-- workflowEngine()     -> WorkflowEngineFactory
                |
                v
        ManageOrganizationFactory (BO)
                +-- getSlaSettingPage()
                        |
                        v
                SlaSettingPage.java (extends BasePage)
                        | @FindBy locators
                        | action methods
                        v
                BasePage.java
                        click(), typeText(), isVisible()
                        selectDropdownModal(), getAttributeValue()
                        WebDriverWait (10 detik)
                        Logger (Log4j2)
```

### Diagram Front Office

```
ManageOrganizationTest.java
        |
        | extends
        v
ManageOrganizationBaseTest.java  (abstract)
        | @BeforeMethod: login + navigasi via FrontOfficeMenu enum
        |
        | extends
        v
FrontOfficeBaseTest.java
        | (setUp() -> new FrontOfficeMainPagesFactory(driver))
        | (menyediakan: getFrontOfficePages())
        | extends
        v
BaseTest.java
        |
        v
FrontOfficeMainPagesFactory.java
        |-- login()              -> FrontOfficeLoginPagesFactory
        |-- sideMenu()           -> FrontOfficeSideMenuFactory
        |-- dashboard()          -> FrontOfficeDashboardFactory
        +-- manageOrganization() -> ManageOrganizationFactory (FO)
```

---

## 3. Cara Menambah Module Baru

Contoh: menambah module **"Payroll"** di Back Office.

**1. Buat Page Object**

```java
// src/main/java/org/example/pageObject/backoffice/payrollMenu/PayrollPage.java
public class PayrollPage extends BasePage {
    @FindBy(id = "payroll-table")
    private WebElement payrollTable;

    public PayrollPage(WebDriver driver) { super(driver); }

    public boolean isPayrollTableVisible() {
        return isVisible(payrollTable);
    }
}
```

**2. Buat Factory**

```java
public class PayrollFactory {
    private final WebDriver driver;
    private PayrollPage payrollPage;

    public PayrollFactory(WebDriver driver) { this.driver = driver; }

    public PayrollPage getPayrollPage() {
        if (payrollPage == null) payrollPage = new PayrollPage(driver);
        return payrollPage;
    }
}
```

**3. Daftarkan di BackOfficeMainPagesFactory**

```java
public PayrollFactory payroll() {
    if (payrollFactory == null) payrollFactory = new PayrollFactory(driver);
    return payrollFactory;
}
```

**4. Buat Test Class** yang extend `BackOfficeBaseTest`, lalu daftarkan di `testng.xml`.

---

## 4. Penggunaan Enum FrontOfficeMenu

`FrontOfficeMenu` digunakan untuk navigasi menu di Front Office secara type-safe, menghindari hardcoded string.

```java
sideMenuPage.navigateTo(FrontOfficeMenu.MY_ORGANIZATION);
```

Jika label menu berubah, cukup update enum tanpa menyentuh test class.

---

## 5. Struktur Direktori

```
project-root/
├── src/main/java/org/example/
│   ├── config/
│   │   ├── Config.java
│   │   ├── slaSettingStructure/
│   │   │   ├── SlaSettingConfig.java, CreateConfig.java
│   │   │   ├── EffTodayConfig.java, EffFutureConfig.java
│   │   │   ├── PropertyConfig.java, SingleConfig.java, MultipleConfig.java
│   │   │   ├── SlaDetailPropertyConfig.java
│   │   │   ├── alertConfirmation/
│   │   │   │   ├── AlertMessageConfig.java, ConfirmationConfig.java
│   │   │   │   ├── ConfirmationDetailConfig.java, EmptyMandatoryFieldConfig.java
│   │   │   │   ├── EmptyDataConfig.java, NotifyConfig.java
│   │   │   └── slaPreviousSetting/SlaExistingConfig.java
│   │   └── userLoginStructure/UserloginConfig.java
│   ├── enums/frontoffice/FrontOfficeMenu.java
│   ├── model/
│   │   ├── SlaSettingModel.java
│   │   └── SlaDetailModel.java
│   ├── pageObject/
│   │   ├── base/BasePage.java
│   │   ├── backoffice/ (login, sideMenu, slaSettingMenu, workflowEngineMenu)
│   │   └── frontoffice/ (login, frontOfficeSideMenu, dashboard, manageOrganization)
│   ├── factories/
│   │   ├── backoffice/ (login, sideMenu, manageOrganization, workflowEngine)
│   │   └── frontoffice/ (login, sideMenuFactory, dashboard, manageOrganization)
│   └── utils/
│       ├── ExtentReportNg.java
│       ├── report/core/ (IAutomationReporter, ReportAPI, ReportEventBus)
│       ├── report/implementations/extent/ (ExtentImplementation, ExtentTabRenderer, ExtentUIInjector)
│       ├── report/internal/model/ (LogEntry, LogCategory, ReportData)
│       ├── report/internal/storage/ (ThreadLocalStore)
│       └── actionUtils/ (ActionUtils, JsonReader)
│
├── src/test/java/org/example/
│   ├── base/ (BaseTest, BackOfficeBaseTest, FrontOfficeBaseTest)
│   ├── drivers/DriverManager.java
│   ├── listener/Listeners.java
│   ├── mainPagesFactory/ (BackOfficeMainPagesFactory, FrontOfficeMainPagesFactory)
│   ├── test/
│   │   ├── backoffice/manageOrganization/slaSetting/
│   │   │   (AddSlaSettingTest, EditSlaSettingTest, SlaSettingTest, SlaDetailTest)
│   │   ├── backoffice/workflowEngine/
│   │   └── frontoffice/myOrganization/
│   ├── utils/
│   │   ├── account/AccountAllocator.java
│   │   ├── CustomAssert.java, ExcelReader.java, TestDataFactory.java, AssertionUtils.java
│   │   ├── extentReportUtils/ExtentReportAppender.java
│   │   ├── generateName/ (SlaNameGenerator, SlaNameRepository, SlaPeriodNameService)
│   │   ├── serviceUtils/ (NetworkMonitor, SeleniumUtils, ApiError)
│   │   └── tableUtils/TableUtils.java
│   └── ArchitectureTest.java
│
├── src/test/resources/
│   ├── log4j2.xml
│   ├── testng/testng.xml
│   └── data/Data Userlogin.xlsx
│
├── reports/
│   ├── index.html          <- ExtentReports output
│   ├── capture/            <- Screenshots per test failure
│   └── spark/              <- ExtentReports assets (offline mode)
└── logs/
    └── automation-test.log <- Rolling log files
```

---

## 6. Architecture Rules (ArchUnit)

Project ini menggunakan **ArchUnit** untuk menegakkan aturan arsitektur secara otomatis sebagai bagian dari test suite.

**File:** `src/test/java/org/example/ArchitectureTest.java`

Menjalankan architecture test saja:
```bash
mvn test -Dtest=ArchitectureTest
```

---

## 7. Scalability & Maintenance Best Practice

- **Tambah test baru**: Buat class di `test/backoffice/` atau `test/frontoffice/`, extend base test yang sesuai.
- **Tambah akun test**: Tambah baris di sheet `ACM` pada `Data Userlogin.xlsx`. `AccountAllocator` otomatis mendistribusikannya.
- **Tambah URL environment**: Tambah baris di sheet `URL` pada `Data Userlogin.xlsx`.
- **Ubah thread count**: Edit `thread-count` di `testng.xml`.
- **Tambah module baru**: Ikuti langkah di [section 3](#3-cara-menambah-module-baru).
- **Ubah tampilan report**: Edit `ExtentUIInjector.java` (CSS/JS) atau `ExtentTabRenderer.java` (struktur HTML). Lihat `report.md` untuk panduan lengkap.

---

## 8. Network Monitoring (CDP) & Version Agnosticism

Framework ini dilengkapi dengan **Network Monitoring** berbasis *Chrome DevTools Protocol (CDP)* yang didekopel secara dinamis melalui reflection. Ini membuat monitoring jaringan kebal terhadap pembaruan versi browser secara tiba-tiba.

**File:**
- [NetworkMonitor.java](file:///d:/MyCave/Refactor/AutomationTest/src/test/java/org/example/utils/serviceUtils/NetworkMonitor.java)
- [CdpVersionHelper.java](file:///d:/MyCave/Refactor/AutomationTest/src/test/java/org/example/utils/serviceUtils/CdpVersionHelper.java) (Reflection Engine)

### Fitur Utama
- **CDP Version Agnostic**: Menggunakan `CdpVersionHelper` untuk memuat paket CDP (`devtools.v125` s.d. `v148`) yang sesuai dengan versi browser saat runtime menggunakan Java Reflection.
- **Auto-Capture Status 4xx & 5xx**: Setiap respon HTTP 400–599 ditangkap secara otomatis.
- **Detailed Logging**: Mencatat URL, Method, Status Code, Status Text, Mime Type.
- **Response & Request Body Capture**: Mengambil payload request dan response body secara dinamis.
- **Temporal Filter**: Hanya menampilkan network error sebelum first test failure untuk meminimalkan noise debug.
- **ExtentReports Integration**: Error jaringan diinjeksi ke tab **NETWORK LOGS** pada laporan Extent.

### Method Utama

| Method | Deskripsi |
|--------|-----------|
| `startMonitoring()` | Memulai CDP session dan mendaftarkan event listener melalui reflection. |
| `flushServerErrorsToReport(test, capturedBefore)` | Mengirimkan API error yang ditangkap ke `ReportAPI`. |
| `stopMonitoring()` | Menghentikan CDP session dan membersihkan resource thread. |

---

## 9. Enhanced Error Logging & CustomAssert

### A. Log4j2 Bridge (ExtentReportAppender)

Semua `logger.info/warn/error()` di Page Object dan utility class **otomatis** masuk ke laporan ExtentReports.

**File:** `src/test/java/org/example/utils/extentReportUtils/ExtentReportAppender.java`

| Log Level | Hasil di Report |
|-----------|----------------|
| `logger.error(...)` | `ReportAPI.fail(message, throwable)` → badge FAIL di tab DETAILS |
| `logger.warn(...)` | `ReportAPI.step("WARN: " + message)` → badge INFO di tab DETAILS |
| `logger.info(...)` | `ReportAPI.step(message)` → badge INFO di tab DETAILS |

Log diabaikan jika tidak ada test aktif atau jika `testMethod` di ThreadContext berbeda (parallel safety).

### B. Deteksi Otomatis Kegagalan Elemen

`BasePage` menggunakan `logger.warn()` untuk kondisi seperti elemen tidak visible. Karena Log4j2 bridge aktif, pesan ini otomatis masuk ke laporan sebagai diagnostik tanpa mengotori Page Object dengan kode reporting.

### C. CustomAssert

Gunakan `CustomAssert` sebagai pengganti `Assert` bawaan TestNG.

**File:** `src/test/java/org/example/utils/CustomAssert.java`

```java
// Pakai ini (bukan Assert.assertEquals):
CustomAssert.assertEquals(actual, expected, "Pesan error");
CustomAssert.assertTrue(condition, "Pesan error");
```

| Method | Deskripsi |
|--------|-----------|
| `assertTrue(condition, message)` | Assert true + log ke ReportAPI. |
| `assertEquals(actual, expected, message)` | Assert equals + log ke ReportAPI. |
| `assertFalse(condition, message)` | Assert false + log ke ReportAPI. |
| `fail(message)` | Manual failure + log ke ReportAPI. |

---

## 10. Account Allocator & Excel Data

### AccountAllocator (Thread-Safe Account Lock System)

**File:** [AccountAllocator.java](file:///d:/MyCave/Refactor/AutomationTest/src/test/java/org/example/utils/account/AccountAllocator.java)

Mengelola distribusi akun test ke thread paralel secara aman menggunakan mekanisme **State Locking** (bukan round-robin murni). Sistem ini mencegah beberapa thread menggunakan akun yang sama pada saat bersamaan (menghindari tabrakan sesi).

- **Penguncian Akun**: Menggunakan `ConcurrentHashMap` sebagai mutex lock.
- **Queue/Wait Timeout**: Jika semua akun untuk suatu role sedang sibuk, thread lain akan mengantre (retry setiap 1 detik) hingga batas waktu **30 detik** sebelum melempar exception.

| Method | Deskripsi |
|--------|-----------|
| `allocateAccount(testName, role)` | Mengunci dan mengambil akun yang kosong untuk role tertentu (dengan antrean 30 detik). |
| `getNip()` / `getUsername()` / `getPassword()` | Mengambil kredensial akun yang teralokasi ke thread aktif. |
| `getUrl(interfaceType)` | Mengambil URL berdasarkan tipe interface. |
| `releaseAccount()` | Membuka kunci akun agar dapat langsung digunakan oleh thread test lainnya. |

### ExcelReader & Data File

**File:** `src/test/java/org/example/utils/ExcelReader.java`  
**Data:** `src/test/java/resources/data/Data Userlogin.xlsx`

**Sheet ACM** — data akun per role:

| Kolom | Deskripsi |
|-------|-----------|
| role | Role pengguna |
| nip | Nomor Induk Pegawai |
| username | Username login |
| password | Password login |

**Sheet URL** — URL per environment:

| Kolom | Deskripsi |
|-------|-----------|
| interface | Nama interface (backoffice_staging, frontoffice_staging) |
| url | URL target |

Untuk menambah akun atau environment baru, cukup tambah baris di sheet yang sesuai.

---

## 11. Log4j2 Bridge (ExtentReportAppender)

**File konfigurasi:** `src/test/resources/log4j2.xml`

```xml
<Configuration packages="org.example.utils">
    <Appenders>
        <ExtentReportAppender name="extent">
            <PatternLayout pattern="%msg%n" />
        </ExtentReportAppender>
    </Appenders>
    <Loggers>
        <Logger name="org.example" level="DEBUG" additivity="false">
            <AppenderRef ref="extent" />
        </Logger>
    </Loggers>
</Configuration>
```

Anotasi plugin: `@Plugin(name = "ExtentReportAppender", category = "Core")`.  
Package `org.example.utils` harus terdaftar di atribut `packages` pada `<Configuration>` agar Log4j2 menemukan plugin ini saat startup.

---

## 12. Informasi Tambahan

### Thread Safety dan Parallel Execution

| Komponen | ThreadLocal | Tujuan |
|----------|-------------|--------|
| `DriverManager` | `ThreadLocal<WebDriver>` | Setiap thread punya driver sendiri |
| `BackOfficeBaseTest.mainPages` | `ThreadLocal<BackOfficeMainPagesFactory>` | Factory tidak di-share antar thread |
| `FrontOfficeBaseTest.mainPages` | `ThreadLocal<FrontOfficeMainPagesFactory>` | Factory tidak di-share antar thread |
| `AccountAllocator.currentAccount` | `ThreadLocal<UserloginConfig>` | Akun tidak bentrok antar thread |
| `ExtentImplementation.test` | `ThreadLocal<ExtentTest>` | Test node Extent per thread |
| `ThreadLocalStore` | `ThreadLocal<ReportData>` | Log storage per thread |
| `NetworkMonitor.currentMonitor` | `ThreadLocal<NetworkMonitor>` | Monitor CDP per thread |

Konfigurasi paralel di `testng.xml`:
```xml
<suite name="Test End To End" parallel="methods" thread-count="3">
```

### Lazy Initialization Pattern

```java
public SlaSettingPage getSlaSettingPage() {
    if (slaSettingPage == null) {
        slaSettingPage = new SlaSettingPage(driver);
    }
    return slaSettingPage;
}
```

### Menjalankan Test

```bash
# Semua test sesuai testng.xml (Default browser: chrome)
mvn test

# Menjalankan dengan browser tertentu (chrome, firefox, edge)
mvn test -Dbrowser=firefox

# Test class tertentu
mvn test -Dtest=AddSlaSettingTest -Dbrowser=edge

# Architecture rules saja
mvn test -Dtest=ArchitectureTest
```

### Laporan Test

- `reports/index.html` — ExtentReports HTML report (offline, bisa dibuka tanpa internet)
- `reports/capture/` — Screenshot per test failure, diorganisir per timestamp
- `logs/automation-test.log` — Log lengkap, rolling harian, retensi 30 hari

### Dependency Utama

| Library | Versi | Kegunaan |
|---------|-------|----------|
| Selenium Java | 4.44.0 | Browser automation |
| Selenium DevTools v148 | 4.44.0 | Chrome DevTools Protocol (CDP) |
| TestNG | 7.10.2 | Test framework dan parallelism |
| ExtentReports | 5.0.9 | HTML test report |
| Log4j2 | 2.24.3 | Logging |
| Apache POI | 5.3.0 | Baca data dari Excel |
| Gson | 2.12.1 | Parse JSON (config & network response) |
| Lombok | 1.18.30 | Boilerplate reduction |
| ArchUnit | 1.4.1 | Architecture rule testing |
| Commons IO | 2.20.0 | File utilities (screenshot) |
