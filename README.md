# Panduan Arsitektur Automation Test Framework

> Stack: Java 17 · Selenium 4 · TestNG · Maven · ExtentReports · Log4j2 · Apache POI

---

## Daftar Isi

1. [Gambaran Umum Arsitektur](#1-gambaran-umum-arsitektur)
2. [Flow Pemanggilan: Test ke PageObject](#2-flow-pemanggilan-test-ke-pageobject)
3. [Cara Menambah Module Baru](#3-cara-menambah-module-baru)
4. [Penggunaan Enum FrontOfficeMenu](#4-penggunaan-enum-frontofficemenu)
5. [Struktur Direktori](#5-struktur-direktori)
6. [Informasi Tambahan](#6-informasi-tambahan)

---

## 1. Gambaran Umum Arsitektur

Framework ini menggunakan pola **Page Object Model (POM)** dikombinasikan dengan **Factory Pattern** dan **Thread-Local Singleton** untuk mendukung eksekusi paralel yang aman.

```
+------------------------------------------------------------------+
|                        LAYER DIAGRAM                             |
+------------------------------------------------------------------+
|  [TEST LAYER]      XxxTest.java                                  |
|                         |                                        |
|  [BASE TEST]       XxxBaseTest                                   |
|                    BackOffice/FrontOfficeBaseTest                 |
|                    BaseTest                                      |
|                         |                                        |
|  [FACTORY LAYER]   MainPagesFactory                              |
|                    ModuleFactory (e.g. ManageOrganizationFactory)|
|                    SubModuleFactory (e.g. SlaSettingFactory)     |
|                                                                  |
|  [PAGE OBJECT]     XxxPage extends BasePage                      |
|                                                                  |
|  [INFRA]           DriverManager (ThreadLocal<WebDriver>)        |
|                    AccountAllocator (ThreadLocal<UserConfig>)    |
+------------------------------------------------------------------+
```

---

## 2. Flow Pemanggilan: Test ke PageObject

### Diagram Lengkap

```
SlaSettingTest.java
        |
        | extends
        v
SlaSettingBaseTest.java
        | (menyediakan helper: slaSettingPage(), dashboardSlaSettingPage(), dll.)
        | extends
        v
BackOfficeBaseTest.java
        | (setUp() -> new MainPagesFactory(driver))
        | (menyediakan: getMainPages(), loginPages(), sideMenu())
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
DriverManager.java
(ThreadLocal<WebDriver>)
        |
        v
MainPagesFactory.java
        | (lazy init semua module factory)
        |-- login()              -> BackOfficeLoginPagesFactory
        |-- sideMenu()           -> SideMenuFactory
        |-- manageOrganization() -> ManageOrganizationFactory
        +-- workflowEngine()     -> WorkflowEngineFactory
                |
                v
        ManageOrganizationFactory.java
                | (lazy init sub-module factory)
                |-- getDashboardSlaSettingPageFactory()
                |-- getSlaSettingFactory()
                |-- getModalFactory()
                +-- getPopUpFactory()
                        |
                        v
                SlaSettingFactory.java
                        |-- getSlaSettingPage()
                        +-- getSlaSettingDetail()
                                |
                                v
                        SlaSettingPage.java (extends BasePage)
                                | @FindBy locators
                                | action methods
                                v
                        BasePage.java
                                click(), typeText(), isVisible()
                                WebDriverWait (10 detik)
                                Logger (Log4j2)
```

### Contoh Nyata di Kode

```java
// Di SlaSettingTest.java
@Test
public void checkDetailSlaSettingActive() {
    // Akses PageObject melalui chain factory
    slaSettingPage().someMethod();
    dashboardSlaSettingPage().isSlaSettingPage();
}
```

```java
// Di SlaSettingBaseTest.java — helper method yang membungkus chain factory
protected SlaSettingPage slaSettingPage() {
    return getMainPages()
            .manageOrganization()
            .getSlaSettingFactory()
            .getSlaSettingPage();
}
```

### Ringkasan Kelas yang Dilalui

| Urutan | Kelas | Lokasi | Peran |
|--------|-------|--------|-------|
| 1 | `XxxTest` | `src/test/.../test/` | Berisi @Test method dan assertion |
| 2 | `XxxBaseTest` | `src/test/.../test/` | Setup konteks spesifik modul |
| 3 | `BackOffice/FrontOfficeBaseTest` | `src/test/.../base/` | Init MainPagesFactory, set URL dan role |
| 4 | `BaseTest` | `src/test/.../base/` | Setup/teardown WebDriver dan account |
| 5 | `DriverManager` | `src/test/.../drivers/` | ThreadLocal WebDriver holder |
| 6 | `AccountAllocator` | `src/test/.../utils/account/` | Alokasi akun per thread |
| 7 | `MainPagesFactory` | `src/test/.../mainPagesFactory/` | Root factory semua modul |
| 8 | `ModuleFactory` | `src/main/.../factories/` | Factory per modul (lazy init) |
| 9 | `SubModuleFactory` | `src/main/.../factories/` | Factory per sub-modul |
| 10 | `XxxPage` | `src/main/.../pageObject/` | Locator dan interaksi UI |
| 11 | `BasePage` | `src/main/.../pageObject/base/` | Wrapper Selenium + wait + log |

---

## 3. Cara Menambah Module Baru

Contoh kasus: menambah menu **"Menu A"** di **Back Office**.

Setiap modul terdiri dari **7 langkah** dan menghasilkan **3 class di sisi test** (Helper, BaseTest, Test).

### Diagram Alur Penambahan (Bottom-Up)

```
[1] Buat PageObject
        |
        v
[2] Buat ModuleFactory
        |
        v
[3] Daftarkan di MainPagesFactory
        |
        v
[4] Tambah navigasi di SideMenu PageObject
        |
        v
[5] Buat Helper Class
        |
        v
[6] Buat ModuleBaseTest
        |
        v
[7] Buat Test Class
```

---

### Langkah 1 — Buat PageObject

Lokasi: `src/main/java/org/example/pageObject/backoffice/menuA/`

```java
package org.example.pageObject.backoffice.menuA;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class MenuAPage extends BasePage {

    @FindBy(xpath = "//h1[text()='Menu A']")
    private WebElement headerMenuA;

    @FindBy(id = "btn-tambah")
    private WebElement btnTambah;

    public MenuAPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isMenuAPage() {
        return isVisible(headerMenuA);
    }

    public void clickTambah() {
        click(btnTambah);
    }
}
```

---

### Langkah 2 — Buat ModuleFactory

Lokasi: `src/main/java/org/example/factories/backoffice/menuA/`

```java
package org.example.factories.backoffice.menuA;

import org.example.pageObject.backoffice.menuA.MenuAPage;
import org.openqa.selenium.WebDriver;

public class MenuAFactory {
    private final WebDriver driver;
    private MenuAPage menuAPage;

    public MenuAFactory(WebDriver driver) {
        this.driver = driver;
    }

    public MenuAPage getMenuAPage() {
        if (menuAPage == null) {
            menuAPage = new MenuAPage(driver);
        }
        return menuAPage;
    }
}
```

---

### Langkah 3 — Daftarkan di MainPagesFactory

File: `src/test/java/org/example/mainPagesFactory/MainPagesFactory.java`

```java
import org.example.factories.backoffice.menuA.MenuAFactory;

public class MainPagesFactory {
    // ... field yang sudah ada ...
    private MenuAFactory menuAFactory;  // <- TAMBAH INI

    // ... method yang sudah ada ...

    public MenuAFactory menuA() {       // <- TAMBAH INI
        if (menuAFactory == null) {
            menuAFactory = new MenuAFactory(driver);
        }
        return menuAFactory;
    }
}
```

---

### Langkah 4 — Tambah Navigasi di SideMenu

File: `src/main/java/org/example/pageObject/backoffice/sideMenu/BackOfficeSideMenu.java`

```java
@FindBy(xpath = "//div[text()='Menu A']")
private WebElement menuAItem;

public void clickMenuA() {
    click(menuAItem);
}
```

---

### Langkah 5 — Buat Helper Class

Helper adalah kelas utilitas yang mengelompokkan **aksi multi-langkah** yang sering dipakai ulang di banyak test case. Tujuannya agar Test class tetap bersih dan tidak berisi logika UI yang panjang.

Aturan:
- Tidak extends apapun, bukan test class
- Menerima `WebDriver` dan `MainPagesFactory` lewat constructor
- Berisi method yang merangkai beberapa aksi PageObject menjadi satu operasi bisnis

Referensi nyata: `SlaSettingHelper.java` — berisi method seperti `createSlaSetting()`, `submitSlaSetting()`, `addSlaDetailHelper()` yang masing-masing merangkai 4-10 langkah PageObject.

Lokasi: `src/test/java/org/example/test/backoffice/menuA/`

```java
package org.example.test.backoffice.menuA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.mainPagesFactory.MainPagesFactory;
import org.example.pageObject.backoffice.menuA.MenuAPage;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;

public class MenuAHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final MenuAPage menuAPage;

    // Terima WebDriver dan MainPagesFactory dari BaseTest
    public MenuAHelper(WebDriver driver, MainPagesFactory mainPagesFactory) {
        this.menuAPage = mainPagesFactory.menuA().getMenuAPage();
    }

    // Contoh: method yang merangkai beberapa aksi menjadi satu operasi bisnis
    public void tambahDataMenuA(String namaData) {
        logger.info("Menambah data Menu A: {}", namaData);
        menuAPage.clickTambah();
        menuAPage.isiFormNama(namaData);
        menuAPage.clickSimpan();
        Assert.assertTrue(menuAPage.isDataTersimpan(namaData),
                "Data gagal tersimpan: " + namaData);
    }
}
```

---

### Langkah 6 — Buat ModuleBaseTest

ModuleBaseTest adalah abstract class yang menjadi **base khusus untuk satu modul**. Tugasnya:
- Login dan navigasi ke halaman modul (`@BeforeMethod`)
- Inisialisasi Helper dan utils yang dibutuhkan modul (`@BeforeMethod`)
- Menyediakan shortcut accessor ke PageObject via factory chain
- Cleanup ThreadLocal setelah test selesai (`@AfterMethod`)

Referensi nyata: `SlaSettingBaseTest.java` — init `TableUtils`, `SlaSettingHelper`, `ActionUtils` via ThreadLocal, dan menyediakan shortcut seperti `slaSettingPage()`, `dashboardSlaSettingPage()`, `deleteSlaSettingModal()`.

```java
package org.example.test.backoffice.menuA;

import org.example.base.BackOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.pageObject.backoffice.menuA.MenuAPage;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class MenuABaseTest extends BackOfficeBaseTest {

    // Gunakan ThreadLocal agar aman saat parallel execution
    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<MenuAHelper> menuAHelper = new ThreadLocal<>();

    @BeforeMethod
    public void initMenuAContext(org.testng.ITestResult result) {
        String nip = AccountAllocator.getNip();
        loginNip.set(nip);

        // 1. Login dan navigasi ke halaman modul
        goToMenuA(nip);

        // 2. Inisialisasi helper — driver dan factory sudah siap di titik ini
        menuAHelper.set(new MenuAHelper(DriverManager.getDriver(), getMainPages()));
    }

    protected void goToMenuA(String nip) {
        logger.info("Login untuk user: {}", nip);
        String password = AccountAllocator.getPassword();
        if (password == null || password.isEmpty()) {
            password = TestDataFactory.getPassword();
        }

        loginPages().getLoginPage().setUsernameField(nip);
        loginPages().getLoginPage().setPasswordField(password);
        loginPages().getLoginPage().clickLoginButton();
        loginPages().getLoginPage().isHeaderWelcomeVisible();

        sideMenu().getSideMenu().clickMenuA();
        menuAPage().isMenuAPage();
        logger.info("Berhasil masuk ke Menu A");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanMenuAContext() {
        // Wajib remove semua ThreadLocal untuk mencegah memory leak
        loginNip.remove();
        menuAHelper.remove();
    }

    // Shortcut accessor ke PageObject via factory chain
    protected MenuAPage menuAPage() {
        return getMainPages().menuA().getMenuAPage();
    }

    // Shortcut accessor ke Helper
    protected MenuAHelper menuAHelper() {
        return menuAHelper.get();
    }

    protected String loginNip() {
        return loginNip.get();
    }
}
```

---

### Langkah 7 — Buat Test Class

Test class hanya berisi `@Test` methods dan `Assert`. Semua setup, navigasi, dan logika multi-langkah sudah ditangani oleh BaseTest dan Helper.

```java
package org.example.test.backoffice.menuA;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MenuATest extends MenuABaseTest {

    @Test(groups = {"Menu A"}, description = "Verifikasi halaman Menu A terbuka")
    public void verifyMenuAPageLoaded() {
        logger.info("Start Test: Verify Menu A Page Loaded");
        Assert.assertTrue(menuAPage().isMenuAPage(), "Halaman Menu A tidak terbuka");
        logger.info("End Test: Verify Menu A Page Loaded");
    }

    @Test(groups = {"Menu A"}, description = "Tambah data baru di Menu A")
    public void tambahDataBaru() {
        logger.info("Start Test: Tambah Data Baru");

        // Aksi sederhana langsung via PageObject
        menuAPage().clickTambah();

        // Aksi multi-langkah via Helper
        menuAHelper().tambahDataMenuA("Data Test 001");

        logger.info("End Test: Tambah Data Baru");
    }
}
```

---

### Ringkasan Peran Ketiga Class di Sisi Test

```
+--------------------+--------------------------------------------------+
| Class              | Peran                                            |
+--------------------+--------------------------------------------------+
| MenuAHelper        | Merangkai aksi multi-langkah yang reusable.      |
|                    | Tidak ada @Test, tidak extends apapun.           |
|                    | Menerima driver + factory via constructor.       |
+--------------------+--------------------------------------------------+
| MenuABaseTest      | Setup/teardown konteks modul (@Before/@After).   |
| (abstract)         | Init Helper dan utils dengan ThreadLocal.        |
|                    | Menyediakan shortcut accessor ke PageObject.     |
|                    | Berisi goToXxx() untuk login + navigasi.         |
+--------------------+--------------------------------------------------+
| MenuATest          | Hanya berisi @Test methods dan Assert.           |
|                    | Extends MenuABaseTest.                           |
|                    | Tidak ada logika setup/navigasi di sini.         |
+--------------------+--------------------------------------------------+
```

### Kapan Perlu Helper, Kapan Tidak

Gunakan Helper jika ada aksi yang:
- Terdiri dari 3+ langkah PageObject berurutan
- Dipakai ulang di lebih dari satu test method
- Melibatkan Assert di tengah alur (bukan hanya di akhir test)

Jika aksinya sederhana dan hanya dipakai sekali, langsung panggil PageObject dari Test class saja.

---

### Checklist Penambahan Module

```
[ ] 1. PageObject dibuat di src/main/.../pageObject/<platform>/<namaModul>/
[ ] 2. Factory dibuat di src/main/.../factories/<platform>/<namaModul>/
[ ] 3. Factory didaftarkan di MainPagesFactory (atau FrontOfficeMainPagesFactory)
[ ] 4. Navigasi ditambahkan di SideMenu PageObject
[ ] 5. Helper class dibuat (jika ada aksi multi-langkah yang reusable)
[ ] 6. XxxBaseTest dibuat: login, navigasi, init Helper, shortcut accessor
[ ] 7. XxxTest dibuat dengan @Test methods dan Assert
```

---

## 4. Penggunaan Enum FrontOfficeMenu

### Lokasi File

```
src/main/java/org/example/enums/frontoffice/FrontOfficeMenu.java
```

### Isi Enum

```java
public enum FrontOfficeMenu {
    // Main Menus
    DASHBOARD("Dashboard"),
    BENEFITS("Benefits"),
    SELF_SERVICE("Self Service"),
    MY_ORGANIZATION("My Organization"),
    HYLEARN("HyLearn"),
    PERFORMANCE_MANAGEMENT("Performance Management"),
    HIRING_ONBOARDING("Hiring & Onboarding"),
    MPP_MONITORING("MPP Monitoring"),
    FEEDBACK_SURVEY("Feedback & Survey"),

    // Sub Menus (My Organization)
    ORGANIZATION_STRUCTURE("Organization Structure"),
    JOB_DESCRIPTION_COMPETENCIES("Job Description & Competencies"),
    EMPLOYEE_TRANSFER("Employee Transfer"),
    MANAGE_ORGANIZATION("Manage Organization"),
    ORGANIZATION_CHANGES_APPROVAL("Organization Changes Approval");

    private final String label;

    FrontOfficeMenu(String label) { this.label = label; }

    public String getLabel() { return label; }
}
```

### Bagaimana Cara Kerjanya

```
Test Class
    |
    | memanggil dengan enum value
    v
FrontOfficeSideMenu.expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION)
    |
    | enum.getLabel() -> "My Organization"
    v
XPath dinamis: //a[.//span[normalize-space()='My Organization']]
    |
    v
Selenium klik elemen di browser
```

### Di Mana Enum Ini Digunakan

Didefinisikan di:
```
src/main/java/org/example/enums/frontoffice/FrontOfficeMenu.java
```

Dikonsumsi di:
```
src/main/java/org/example/pageObject/frontoffice/frontOfficeSideMenu/FrontOfficeSideMenu.java
```

```java
// Untuk klik/expand Main Menu
public void expandMainMenu(FrontOfficeMenu menu) {
    By locator = By.xpath(
        String.format("//a[.//span[normalize-space()='%s']]", menu.getLabel())
    );
    click(driver.findElement(locator));
}

// Untuk klik Sub Menu
public void clickSubMenu(FrontOfficeMenu subMenu) {
    By locator = By.xpath(
        String.format("//*[contains(text(), '%s')]", subMenu.getLabel())
    );
    click(driver.findElement(locator));
}
```

Cara memanggilnya dari Test / BaseTest:

```java
// Expand main menu dulu, lalu klik sub menu
getFrontOfficePages()
    .sideMenu()
    .getFrontOfficeSideMenu()
    .expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION);   // <- set enum di sini

getFrontOfficePages()
    .sideMenu()
    .getFrontOfficeSideMenu()
    .clickSubMenu(FrontOfficeMenu.MANAGE_ORGANIZATION); // <- set enum di sini
```

### Cara Menambah Menu Baru ke Enum

Jika ada menu baru di aplikasi Front Office, cukup tambahkan entry baru di enum:

```java
// Di FrontOfficeMenu.java
NAMA_MENU_BARU("Label Menu di UI");
```

Tidak perlu ubah kode di tempat lain. XPath akan otomatis terbentuk dari label.

### Kapan Perlu Buat Enum Baru

Buat file enum baru jika menambah modul Back Office yang punya banyak menu dinamis dan ingin navigasinya type-safe seperti Front Office.

Contoh: `src/main/java/org/example/enums/backoffice/BackOfficeMenu.java`

---

## 5. Struktur Direktori

```
src/
+-- main/java/org/example/
|   +-- config/                    # Konfigurasi data test (struktur JSON/Excel)
|   +-- enums/
|   |   +-- frontoffice/
|   |       +-- FrontOfficeMenu.java   <- Enum menu Front Office
|   +-- factories/
|   |   +-- backoffice/            # Factory per modul Back Office
|   |   |   +-- login/
|   |   |   +-- sideMenu/
|   |   |   +-- manageOrganization/
|   |   |   +-- workflowEngine/
|   |   +-- frontoffice/           # Factory per modul Front Office
|   |       +-- login/
|   |       +-- sideMenuFactory/
|   |       +-- dashboard/
|   +-- model/                     # Data model (e.g. SlaSettingModel)
|   +-- pageObject/
|       +-- base/
|       |   +-- BasePage.java      <- Base semua PageObject
|       +-- backoffice/
|       +-- frontoffice/
|           +-- frontOfficeSideMenu/
|               +-- FrontOfficeSideMenu.java
|
+-- test/java/org/example/
    +-- base/
    |   +-- BaseTest.java              <- Setup/teardown WebDriver
    |   +-- BackOfficeBaseTest.java    <- Init MainPagesFactory
    |   +-- FrontOfficeBaseTest.java   <- Init FrontOfficeMainPagesFactory
    +-- drivers/
    |   +-- DriverManager.java         <- ThreadLocal<WebDriver>
    +-- mainPagesFactory/
    |   +-- MainPagesFactory.java          <- Root factory Back Office
    |   +-- FrontOfficeMainPagesFactory.java <- Root factory Front Office
    +-- test/
    |   +-- backoffice/
    |   |   +-- manageOrganization/
    |   |       +-- slaSetting/
    |   |           +-- SlaSettingHelper.java    <- Helper (aksi reusable)
    |   |           +-- SlaSettingBaseTest.java  <- Base modul (setup/accessor)
    |   |           +-- SlaSettingTest.java      <- Test (@Test + Assert)
    |   +-- frontoffice/
    |       +-- myOrganization/
    |           +-- manageOrganization/
    |               +-- ManageOrganizationBaseTest.java
    |               +-- ManageOrganizationHelper.java
    +-- utils/
        +-- account/
        |   +-- AccountAllocator.java  <- Alokasi akun per thread
        +-- actionUtils/
        +-- tableUtils/
        +-- ExcelReader.java
```

---

## 6. Informasi Tambahan

### Thread Safety dan Parallel Execution

Framework ini dirancang untuk parallel test execution. Tiga komponen kunci yang menggunakan ThreadLocal:

| Komponen | ThreadLocal Type | Tujuan |
|----------|-----------------|--------|
| `DriverManager` | `ThreadLocal<WebDriver>` | Setiap thread punya driver sendiri |
| `BackOfficeBaseTest.mainPages` | `ThreadLocal<MainPagesFactory>` | Factory tidak di-share antar thread |
| `AccountAllocator.currentAccount` | `ThreadLocal<UserloginConfig>` | Akun tidak bentrok antar thread |

### Lazy Initialization Pattern

Semua factory menggunakan pola ini — objek hanya dibuat saat pertama kali dibutuhkan:

```java
public MenuAPage getMenuAPage() {
    if (menuAPage == null) {
        menuAPage = new MenuAPage(driver);
    }
    return menuAPage;
}
```

### Data Test dari Excel

Akun login dibaca dari file Excel:
```
src/test/java/resources/data/Data Userlogin.xlsx
```

Kolom yang dibutuhkan: `role`, `nip`, `username`, `password`, `urlBackOffice`, `urlFrontOffice`

Akun dikelompokkan per role dan didistribusikan round-robin ke thread yang berjalan paralel.

### Konfigurasi TestNG

Suite XML ada di:
```
src/test/java/resources/testng/testng.xml
```

Jalankan semua test:
```bash
mvn test
```

Jalankan group tertentu:
```bash
mvn test -Dgroups="Sla Setting"
```

### Laporan Test

Setelah test selesai, laporan tersedia di:
- `reports/index.html` — ExtentReports HTML report
- `logs/automation-test.log` — Log lengkap per eksekusi

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
