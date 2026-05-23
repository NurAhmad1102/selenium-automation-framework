# Panduan Arsitektur Automation Test Framework

> Stack: Java 17, Selenium 4, TestNG, Maven, ExtentReports, Log4j2, Apache POI

---

## Daftar Isi

1. [Gambaran Umum Arsitektur](#1-gambaran-umum-arsitektur)
2. [Flow Pemanggilan: Test ke PageObject](#2-flow-pemanggilan-test-ke-pageobject)
3. [Cara Menambah Module Baru](#3-cara-menambah-module-baru)
4. [Penggunaan Enum FrontOfficeMenu](#4-penggunaan-enum-frontofficemenu)
5. [Struktur Direktori](#5-struktur-direktori)
6. [Architecture Rules](#6-architecture-rules)
7. [Scalability & Maintenance](#7-scalability--maintenance-best-practice)
8. [Network Monitoring (CDP)](#8-network-monitoring-cdp)
9. [Enhanced Error Logging & CustomAssert](#9-enhanced-error-logging--customassert)
10. [Informasi Tambahan](#10-informasi-tambahan)

---

## 1. Gambaran Umum Arsitektur

Framework ini menggunakan pola **Page Object Model (POM)** dikombinasikan dengan **Factory Pattern** dan **Thread-Local Singleton** untuk mendukung eksekusi paralel yang aman.

```
+------------------------------------------------------------------+
|                        LAYER DIAGRAM                             |
+------------------------------------------------------------------+
|  [TEST LAYER]      XxxFlow / XxxTest                             |
|                         |                                        |
|  [BASE TEST]       XxxBaseTest                                   |
|                    BackOffice/FrontOfficeBaseTest                 |
|                    BaseTest                                      |
|                         |                                        |
|  [FACTORY LAYER]   MainPagesFactory / FrontOfficeMainPagesFactory|
|                    ModuleFactory (e.g. ManageOrganizationFactory)|
|                                                                  |
|  [PAGE OBJECT]     XxxPage extends BasePage                      |
|                                                                  |
|  [INFRA]           DriverManager (ThreadLocal<WebDriver>)        |
|                    AccountAllocator (ThreadLocal<UserConfig>)    |
+------------------------------------------------------------------+
```

---

## 2. Flow Pemanggilan: Test ke PageObject

Acuan: `ManageOrganizationFlow1.java` (Front Office) dan `SlaSettingTest.java` (Back Office).

### Diagram Lengkap ”Front Office"

```
ManageOrganizationFlow1.java
        |
        | extends
        v
ManageOrganizationBaseTest.java  (abstract)
        | @BeforeMethod initManageOrganizationContext():
        |   goToManageOrganization(nip)  <- login + navigasi via enum
        |   manageOrganizationHelper.set(new ManageOrganizationHelper(getFrontOfficePages()))
        |   actionUtils.set(new ActionUtils(driver))
        |
        | shortcut accessor:
        |   dashboardManageOrganizationPage()
        |   manageOrganizationHelper()
        |
        | extends
        v
FrontOfficeBaseTest.java
        | (setUp() -> new FrontOfficeMainPagesFactory(driver))
        | (menyediakan: getFrontOfficePages())
        | extends
        v
BaseTest.java
        | @BeforeMethod setUp():
        |   AccountAllocator.allocateAccount(testName, role)
        |   new ChromeDriver(options)
        |   DriverManager.setDriver(driver)
        |
        | @AfterMethod tearDown():
        |   DriverManager.quitDriver()
        |   AccountAllocator.releaseAccount()
        |
        v
FrontOfficeMainPagesFactory.java
        | (lazy init semua module factory)
        |-- login()           -> FrontOfficeLoginPagesFactory
        |-- sideMenu()        -> FrontOfficeSideMenuFactory
        +-- manageOrganization() -> ManageOrganizationFactory (FO)
                |
                v
        ManageOrganizationFactory (FO)
                +-- getDashboardManageOrganizationPage()
                        |
                        v
                DashboardManageOrganizationPage.java (extends BasePage)
                        | @FindBy locators
                        | action methods
                        v
                BasePage.java
                        click(), typeText(), isVisible()
                        WebDriverWait (10 detik)
                        Logger (Log4j2)
```

---

## 8. Network Monitoring (CDP)

Framework ini dilengkapi dengan fitur **Network Monitoring** menggunakan *Chrome DevTools Protocol (CDP)*. Fitur ini secara otomatis memantau lalu lintas jaringan selama tes berjalan dan mencatat kegagalan API ke dalam laporan.

### Fitur Utama
- **Auto-Capture Status 5xx**: Setiap respon HTTP dengan status code >= 500 akan ditangkap secara otomatis.
- **Detailed Logging**: Mencatat URL, Status Code, Status Text, dan Mime Type.
- **Response Body Capture**: Mencoba mengambil *response body* (JSON/Text) dari API yang gagal untuk memudahkan debugging.
- **ExtentReports Integration**: Error jaringan akan muncul langsung di dalam laporan tes pada bagian log detail.

---

## 9. Enhanced Error Logging & CustomAssert

Framework ini telah ditingkatkan untuk memberikan informasi detail saat terjadi kegagalan, baik karena masalah Selenium (elemen tidak ditemukan/diklik) maupun kegagalan validasi (Assertion).

### A. Deteksi Otomatis Kegagalan Elemen (Log4j2 Bridge & Diagnostics)
Framework ini menggunakan kombinasi **Log4j2 Bridge Appender** dan **Diagnostic Collector** untuk memberikan laporan yang cerdas:
- **Transparansi Layer**: Page Objects tetap bersih dari kode pelaporan langsung.
- **Real-time Logging**: Langkah-langkah positif (klik, ketik) tetap dicatat secara real-time sebagai `INFO`.
- **Conditional Diagnostic**: Pesan seperti `Locator not visible` (dari `isVisible`) tidak akan mengotori laporan jika tes berhasil. Pesan ini disimpan secara internal dan **hanya akan muncul jika tes gagal**.
- **Root Cause Focus**: Saat tes gagal, pesan diagnosa tersebut akan dimunculkan dengan label **Potential Cause** dan status **FAIL**, memberikan petunjuk langsung mengapa asersi atau aksi selanjutnya gagal.

### B. Penggunaan CustomAssert
Untuk mendapatkan log yang rapi di ExtentReports saat validasi gagal, gunakan `CustomAssert` alih-alih `Assert` bawaan TestNG. `CustomAssert` juga terintegrasi secara aman dalam eksekusi paralel.

---

## 10. Informasi Tambahan

### Thread Safety dan Parallel Execution

| Komponen | ThreadLocal Type | Tujuan |
|----------|-----------------|--------|
| `DriverManager` | `ThreadLocal<WebDriver>` | Setiap thread punya driver sendiri |
| `FrontOfficeBaseTest.mainPages` | `ThreadLocal<FrontOfficeMainPagesFactory>` | Factory tidak di-share antar thread |
| `BackOfficeBaseTest.mainPages` | `ThreadLocal<MainPagesFactory>` | Factory tidak di-share antar thread |
| `AccountAllocator.currentAccount` | `ThreadLocal<UserloginConfig>` | Akun tidak bentrok antar thread |

### Lazy Initialization Pattern

Semua factory menggunakan pola ini:

```java
public DashboardMenuAPage getDashboardMenuAPage() {
    if (dashboardMenuAPage == null) {
        dashboardMenuAPage = new DashboardMenuAPage(driver);
    }
    return dashboardMenuAPage;
}
```

### Data Test dari Excel

```
src/test/java/resources/data/Data Userlogin.xlsx
```

Kolom: `role`, `nip`, `username`, `password`, `urlBackOffice`, `urlFrontOffice`

Akun dikelompokkan per role dan didistribusikan round-robin ke thread paralel.

### Menjalankan Test

```bash
# Semua test
mvn test

# Group tertentu
mvn test -Dgroups="Manage Organization"

# Architecture rule saja
mvn test -Dtest=ArchitectureTest
```

### Laporan Test

- `reports/index.html` ” ExtentReports HTML report
- `logs/automation-test.log` ” Log lengkap per eksekusi

### Dependency Utama

| Library | Versi | Kegunaan |
|---------|-------|----------|
| Selenium | 4.35.0 | Browser automation |
| TestNG | 7.10.2 | Test framework dan parallelism |
| ExtentReports | 5.0.9 | HTML test report |
| Log4j2 | 2.24.3 | Logging |
| Apache POI | 5.3.0 | Baca data dari Excel |
| Gson | 2.12.1 | Parse JSON config |
| Lombok | 1.18.30 | Boilerplate reduction |
| ArchUnit | 1.4.1 | Architecture rule testing |
