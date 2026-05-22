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
9. [Informasi Tambahan](#9-informasi-tambahan)

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

### Cara Test Class Memanggil PageObject

Di `ManageOrganizationFlow1.java`, Test class **tidak pernah menyentuh factory chain secara langsung**. Semua akses PageObject dilakukan melalui:

1. **`manageOrganizationHelper()`** ” untuk aksi multi-langkah yang sudah dibungkus di Helper
2. **`dashboardManageOrganizationPage()`** ” shortcut accessor dari BaseTest untuk akses langsung ke PageObject

```java
// ManageOrganizationFlow1.java ” contoh nyata
public class ManageOrganizationFlow1 extends ManageOrganizationBaseTest {

    @Test(groups = {"Manage Organization"}, description = "Validate Manage Organization page is displayed")
    public void validateManageOrganizationPageIsDisplayed() {
        logger.info("Starting test: validateManageOrganizationPageIsDisplayed");

        // Akses via Helper ” Helper yang pegang factory chain
        Assert.assertTrue(
                manageOrganizationHelper().isManageOrganizationPageDisplayed(),
                "Expected Manage Organization page to be displayed, but it was not."
        );

        logger.info("Finished test: validateManageOrganizationPageIsDisplayed");
    }
}
```

```java
// ManageOrganizationHelper.java ” Helper menerima FrontOfficeMainPagesFactory
public class ManageOrganizationHelper {
    private final DashboardManageOrganizationPage dashboardManageOrganizationPage;

    // Constructor menerima factory, bukan driver langsung
    public ManageOrganizationHelper(FrontOfficeMainPagesFactory backOfficeMainPagesFactory) {
        this.dashboardManageOrganizationPage = backOfficeMainPagesFactory
                .manageOrganization()
                .getDashboardManageOrganizationPage();
    }

    public boolean isManageOrganizationPageDisplayed() {
        return dashboardManageOrganizationPage.isManageOrganizationHeaderDisplayed();
    }
}
```

```java
// ManageOrganizationBaseTest.java ” init Helper dengan getFrontOfficePages()
manageOrganizationHelper.set(new ManageOrganizationHelper(getFrontOfficePages()));
```

### Navigasi ke Halaman Modul (goToXxx)

Navigasi di Front Office menggunakan `FrontOfficeMenu` enum, bukan hardcode string:

```java
// Di ManageOrganizationBaseTest.goToManageOrganization()
getFrontOfficePages().login().getLoginPage().setUsernameField(loginNip);
getFrontOfficePages().login().getLoginPage().setPasswordField(password);
getFrontOfficePages().login().getLoginPage().clickSignInButton();

// Navigasi via enum ” type-safe, tidak hardcode string
getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
        .expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION);
getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
        .clickSubMenu(FrontOfficeMenu.MANAGE_ORGANIZATION);
```

### Aturan Akses PageObject

Test class tidak boleh membuat PageObject langsung dengan constructor.

```java
// SALAH ” jangan lakukan ini di Test class
new DashboardManageOrganizationPage(driver);
new SlaSettingPage(driver);
```

```java
// BENAR ” akses via Helper (pola utama di Flow1)
manageOrganizationHelper().isManageOrganizationPageDisplayed();

// BENAR ” akses via shortcut accessor dari BaseTest
dashboardManageOrganizationPage().isManageOrganizationHeaderDisplayed();

// BENAR ” factory chain, hanya di layer BaseTest atau Helper
getFrontOfficePages().manageOrganization().getDashboardManageOrganizationPage();
```

Rule ini dijaga oleh `ArchitectureTest`. Jika test class melakukan `new XxxPage(driver)`, architecture test akan gagal.

### Ringkasan Kelas yang Dilalui

| Urutan | Kelas | Lokasi | Peran |
|--------|-------|--------|-------|
| 1 | `XxxFlow / XxxTest` | `src/test/.../test/` | Berisi @Test method dan assertion |
| 2 | `XxxBaseTest` | `src/test/.../test/` | Setup konteks modul, init Helper, shortcut accessor |
| 3 | `FrontOffice/BackOfficeBaseTest` | `src/test/.../base/` | Init MainPagesFactory, set URL dan role |
| 4 | `BaseTest` | `src/test/.../base/` | Setup/teardown WebDriver dan account |
| 5 | `DriverManager` | `src/test/.../drivers/` | ThreadLocal WebDriver holder |
| 6 | `AccountAllocator` | `src/test/.../utils/account/` | Alokasi akun per thread |
| 7 | `FrontOfficeMainPagesFactory` | `src/test/.../backOfficeMainPagesFactory/` | Root factory Front Office |
| 8 | `ModuleFactory` | `src/main/.../factories/` | Factory per modul (lazy init) |
| 9 | `XxxPage` | `src/main/.../pageObject/` | Locator dan interaksi UI |
| 10 | `BasePage` | `src/main/.../pageObject/base/` | Wrapper Selenium + wait + log |

---

## 3. Cara Menambah Module Baru

Contoh kasus: menambah menu **"Menu A"** di **Front Office**.

Setiap modul menghasilkan **3 class di sisi test**: Helper, BaseTest, dan Flow/Test class.

### Diagram Alur Penambahan (Bottom-Up)

```
[1] Buat PageObject
        |
        v
[2] Buat ModuleFactory
        |
        v
[3] Daftarkan di FrontOfficeMainPagesFactory (atau MainPagesFactory)
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
[7] Buat Flow / Test Class
```

---

### Langkah 1 ”Buat PageObject"

Lokasi: `src/main/java/org/example/pageObject/frontoffice/menuA/`

```java
package org.example.pageObject.frontoffice.menuA;

import org.example.pageObject.base.BasePage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

public class DashboardMenuAPage extends BasePage {

    @FindBy(xpath = "//h1[contains(text(),'Menu A')]")
    private WebElement headerMenuA;

    public DashboardMenuAPage(WebDriver driver) {
        super(driver);
        PageFactory.initElements(driver, this);
    }

    public boolean isMenuAHeaderDisplayed() {
        return isVisible(headerMenuA);
    }
}
```

---

### Langkah 2 ”Buat ModuleFactory"

Lokasi: `src/main/java/org/example/factories/frontoffice/menuA/`

```java
package org.example.factories.frontoffice.menuA;

import org.example.pageObject.frontoffice.menuA.DashboardMenuAPage;
import org.openqa.selenium.WebDriver;

public class MenuAFactory {
    private final WebDriver driver;
    private DashboardMenuAPage dashboardMenuAPage;

    public MenuAFactory(WebDriver driver) {
        this.driver = driver;
    }

    public DashboardMenuAPage getDashboardMenuAPage() {
        if (dashboardMenuAPage == null) {
            dashboardMenuAPage = new DashboardMenuAPage(driver);
        }
        return dashboardMenuAPage;
    }
}
```

---

### Langkah 3 ”Daftarkan di FrontOfficeMainPagesFactory"

File: `src/test/java/org/example/backOfficeMainPagesFactory/FrontOfficeMainPagesFactory.java`

```java
import org.example.factories.frontoffice.menuA.MenuAFactory;

public class FrontOfficeMainPagesFactory {
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

### Langkah 4 ”Tambah Entry di FrontOfficeMenu Enum"

File: `src/main/java/org/example/enums/frontoffice/FrontOfficeMenu.java`

```java
// Tambahkan entry baru sesuai label di UI
MENU_A("Menu A"),
MENU_A_SUB("Sub Menu A");
```

Navigasi di `FrontOfficeSideMenu` otomatis bekerja karena XPath dibangun dari label enum.

---

### Langkah 5 ”Buat Helper Class"

Helper menerima `FrontOfficeMainPagesFactory` (bukan `WebDriver`) lewat constructor, lalu mengambil PageObject dari factory chain. Ini pola yang sama dengan `ManageOrganizationHelper.java`.

Lokasi: `src/test/java/org/example/test/frontoffice/menuA/`

```java
package org.example.test.frontoffice.menuA;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.example.backOfficeMainPagesFactory.FrontOfficeMainPagesFactory;
import org.example.pageObject.frontoffice.menuA.DashboardMenuAPage;

public class MenuAHelper {
    private final Logger logger = LogManager.getLogger(getClass());
    private final DashboardMenuAPage dashboardMenuAPage;

    // Terima FrontOfficeMainPagesFactory, bukan WebDriver langsung
    public MenuAHelper(FrontOfficeMainPagesFactory backOfficeMainPagesFactory) {
        this.dashboardMenuAPage = backOfficeMainPagesFactory.menuA().getDashboardMenuAPage();
    }

    public boolean isMenuAPageDisplayed() {
        logger.info("Validate Menu A page is displayed");
        return dashboardMenuAPage.isMenuAHeaderDisplayed();
    }
}
```

---

### Langkah 6 ”Buat ModuleBaseTest"

Pola sama dengan `ManageOrganizationBaseTest.java`: login, navigasi via enum, init Helper dengan `getFrontOfficePages()`, shortcut accessor ke PageObject.

```java
package org.example.test.frontoffice.menuA;

import org.example.base.FrontOfficeBaseTest;
import org.example.drivers.DriverManager;
import org.example.enums.frontoffice.FrontOfficeMenu;
import org.example.pageObject.frontoffice.menuA.DashboardMenuAPage;
import org.example.utils.TestDataFactory;
import org.example.utils.account.AccountAllocator;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public abstract class MenuABaseTest extends FrontOfficeBaseTest {

    private final ThreadLocal<String> loginNip = new ThreadLocal<>();
    private final ThreadLocal<MenuAHelper> menuAHelper = new ThreadLocal<>();

    @BeforeMethod
    public void initMenuAContext(org.testng.ITestResult result) {
        String nip = AccountAllocator.getNip();
        loginNip.set(nip);

        goToMenuA(nip);

        // Helper menerima getFrontOfficePages(), bukan driver
        menuAHelper.set(new MenuAHelper(getFrontOfficePages()));
    }

    protected void goToMenuA(String loginNip) {
        logger.info("Login untuk user: {}", loginNip);
        String password = AccountAllocator.getPassword();
        if (password == null || password.isEmpty()) {
            password = TestDataFactory.getPassword();
        }

        getFrontOfficePages().login().getLoginPage().setUsernameField(loginNip);
        getFrontOfficePages().login().getLoginPage().setPasswordField(password);
        getFrontOfficePages().login().getLoginPage().clickSignInButton();

        // Navigasi via enum ” type-safe
        getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
                .expandMainMenu(FrontOfficeMenu.MENU_A);
        getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
                .clickSubMenu(FrontOfficeMenu.MENU_A_SUB);

        if (!dashboardMenuAPage().isMenuAHeaderDisplayed()) {
            throw new RuntimeException("Menu A page header not visible");
        }
        logger.info("Berhasil masuk ke Menu A");
    }

    @AfterMethod(alwaysRun = true)
    public void cleanMenuAContext() {
        loginNip.remove();
        menuAHelper.remove();
    }

    // Shortcut accessor ke PageObject
    protected DashboardMenuAPage dashboardMenuAPage() {
        return getFrontOfficePages().menuA().getDashboardMenuAPage();
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

### Langkah 7 ”Buat Flow / Test Class"

Test class hanya berisi `@Test` methods dan `Assert`. Akses PageObject selalu melalui Helper atau shortcut accessor dari BaseTest ” tidak pernah langsung ke factory chain.

Pola ini sama persis dengan `ManageOrganizationFlow1.java`.

```java
package org.example.test.frontoffice.menuA;

import org.testng.Assert;
import org.testng.annotations.Test;

public class MenuAFlow1 extends MenuABaseTest {

    @Test(groups = {"Menu A"}, description = "Validate Menu A page is displayed")
    public void validateMenuAPageIsDisplayed() {
        logger.info("Starting test: validateMenuAPageIsDisplayed");

        // Akses via Helper ” pola utama
        Assert.assertTrue(
                menuAHelper().isMenuAPageDisplayed(),
                "Expected Menu A page to be displayed, but it was not."
        );

        logger.info("Finished test: validateMenuAPageIsDisplayed");
    }

    @Test(groups = {"Menu A"}, description = "Validate header text")
    public void validateMenuAHeader() {
        logger.info("Starting test: validateMenuAHeader");

        // Akses via shortcut accessor dari BaseTest ” untuk aksi sederhana
        Assert.assertTrue(
                dashboardMenuAPage().isMenuAHeaderDisplayed(),
                "Menu A header not displayed"
        );

        logger.info("Finished test: validateMenuAHeader");
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
|                    | Menerima FrontOfficeMainPagesFactory via          |
|                    | constructor (bukan WebDriver langsung).          |
+--------------------+--------------------------------------------------+
| MenuABaseTest      | Setup/teardown konteks modul (@Before/@After).   |
| (abstract)         | Init Helper dengan getFrontOfficePages().        |
|                    | Navigasi via FrontOfficeMenu enum.               |
|                    | Menyediakan shortcut accessor ke PageObject.     |
+--------------------+--------------------------------------------------+
| MenuAFlow1         | Hanya berisi @Test methods dan Assert.           |
|                    | Extends MenuABaseTest.                           |
|                    | Akses PageObject via Helper atau accessor.       |
|                    | Tidak ada factory chain, tidak ada new XxxPage.  |
+--------------------+--------------------------------------------------+
```

### Perbedaan Helper Front Office vs Back Office

| Aspek | Front Office | Back Office |
|-------|-------------|-------------|
| Constructor Helper | `FrontOfficeMainPagesFactory` | `MainPagesFactory` + dependency utility dari BaseTest jika dibutuhkan |
| Contoh nyata | `ManageOrganizationHelper` | `SlaSettingHelper` |
| Navigasi | Via `FrontOfficeMenu` enum | Via hardcode xpath di SideMenu |
| Root factory | `getFrontOfficePages()` | `getMainPages()` |

#### A. PageObject sebagai Shortcut (di BaseTest)

Di dalam `ManageOrganizationBaseTest`, Anda menyediakan method accessor seperti:

- `dashboardManageOrganizationPage()`
- `filterUpdateOrganizationPage()`

#### Fungsinya sebagai Shortcut

Sesuai dengan analisis sebelumnya, method ini di level `BaseTest` memang berfungsi sebagai "jalan pintas" bagi test class untuk melakukan hal-hal sederhana tanpa harus lewat helper, seperti:

- **Simple Verification**  
  `Assert.assertTrue(dashboardManageOrganizationPage().isSomeElementVisible())`

- **Quick Check**  
  Pengecekan status elemen sebelum/sesudah langkah utama.

- **Minimal Interaction**  
  Jika test hanya membutuhkan satu klik sederhana yang tidak memiliki logika bisnis kompleks.

---

#### B. Helper sebagai Handler Steps (di ManageOrganizationHelper)

Sedangkan untuk `ManageOrganizationHelper`, Anda telah menggunakannya untuk membungkus alur yang lebih kompleks (multi-steps), contohnya:

- `navigateToUpdateOrganization()`  
  Meng-handle klik sekaligus melakukan verifikasi transisi halaman.

- `performFilterOrganization(String requestType)`  
  Meng-handle pengisian form filter yang mungkin melibatkan banyak elemen.

---

#### Kesimpulan Arsitektur **Separation of Concerns**:

- **Class Test**  
  Fokus pada skenario dan assertion.

- **Shortcut BaseTest**  
  Memberikan fleksibilitas akses cepat ke `PageObject` untuk hal-hal non-bisnis (biasanya hanya untuk verifikasi UI sederhana).

- **Helper**  
  Menjadi engine yang menjalankan langkah-langkah prosedural/bisnis yang reusable.


### Checklist Penambahan Module

```
[ ] 1. PageObject dibuat di src/main/.../pageObject/<platform>/<namaModul>/
[ ] 2. Factory dibuat di src/main/.../factories/<platform>/<namaModul>/
[ ] 3. Factory didaftarkan di FrontOfficeMainPagesFactory (atau MainPagesFactory)
[ ] 4. Entry enum ditambahkan di FrontOfficeMenu (untuk Front Office)
       atau locator + method ditambahkan di BackOfficeSideMenu (untuk Back Office)
[ ] 5. Helper dibuat - constructor menerima root factory dan dependency dari BaseTest, bukan membuat PageObject sendiri
[ ] 6. XxxBaseTest dibuat: login, navigasi via enum, init Helper, shortcut accessor
[ ] 7. XxxFlow/XxxTest dibuat ” hanya @Test + Assert, akses via Helper atau accessor
[ ] 8. Pastikan tidak ada new XxxPage(driver) di Test/Helper
[ ] 9. Jalankan ArchitectureTest: mvn test -Dtest=ArchitectureTest
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
goToManageOrganization() di BaseTest
    |
    | memanggil dengan enum value
    v
getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
    .expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION)
    |
    | enum.getLabel() -> "My Organization"
    v
XPath: //a[.//span[normalize-space()='My Organization']]
    |
    v
Selenium klik elemen di browser
```

### Di Mana Enum Ini Digunakan

Didefinisikan di:
```
src/main/java/org/example/enums/frontoffice/FrontOfficeMenu.java
```

Dikonsumsi di (PageObject):
```
src/main/java/org/example/pageObject/frontoffice/frontOfficeSideMenu/FrontOfficeSideMenu.java
```

```java
public void expandMainMenu(FrontOfficeMenu menu) {
    By locator = By.xpath(
        String.format("//a[.//span[normalize-space()='%s']]", menu.getLabel())
    );
    click(driver.findElement(locator));
}

public void clickSubMenu(FrontOfficeMenu subMenu) {
    By locator = By.xpath(
        String.format("//*[contains(text(), '%s')]", subMenu.getLabel())
    );
    click(driver.findElement(locator));
}
```

Dipanggil dari (BaseTest):
```
src/test/java/org/example/test/frontoffice/.../ManageOrganizationBaseTest.java
```

```java
// Di dalam goToManageOrganization()
getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
        .expandMainMenu(FrontOfficeMenu.MY_ORGANIZATION);
getFrontOfficePages().sideMenu().getFrontOfficeSideMenu()
        .clickSubMenu(FrontOfficeMenu.MANAGE_ORGANIZATION);
```

Enum hanya dipakai di layer **BaseTest** (dalam method `goToXxx`), tidak di Test class maupun Helper.

### Cara Menambah Menu Baru ke Enum

```java
// Di FrontOfficeMenu.java ” tambahkan satu baris
NAMA_MENU_BARU("Label Menu di UI");
```

Tidak perlu ubah kode di tempat lain. XPath akan otomatis terbentuk dari label.

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
|           |   +-- FrontOfficeSideMenu.java  <- Konsumen FrontOfficeMenu enum
|           +-- manageOrganization/
|               +-- DashboardManageOrganizationPage.java
|
+-- test/java/org/example/
    +-- base/
    |   +-- BaseTest.java                  <- Setup/teardown WebDriver
    |   +-- BackOfficeBaseTest.java        <- Init MainPagesFactory
    |   +-- FrontOfficeBaseTest.java       <- Init FrontOfficeMainPagesFactory
    +-- drivers/
    |   +-- DriverManager.java             <- ThreadLocal<WebDriver>
    +-- backOfficeMainPagesFactory/
    |   +-- MainPagesFactory.java              <- Root factory Back Office
    |   +-- FrontOfficeMainPagesFactory.java   <- Root factory Front Office
    +-- test/
    |   +-- backoffice/
    |   |   +-- manageOrganization/
    |   |       +-- slaSetting/
    |   |           +-- SlaSettingHelper.java    <- Helper (terima driver + factory)
    |   |           +-- SlaSettingBaseTest.java  <- Base modul
    |   |           +-- SlaSettingTest.java      <- Test (@Test + Assert)
    |   +-- frontoffice/
    |       +-- myOrganization/
    |           +-- manageOrganization/
    |               +-- ManageOrganizationHelper.java   <- Helper (terima factory saja)
    |               +-- ManageOrganizationBaseTest.java <- Base modul
    |               +-- ManageOrganizationFlow1.java    <- Flow (@Test + Assert)
    +-- utils/
        +-- account/
        |   +-- AccountAllocator.java  <- Alokasi akun per thread
        +-- actionUtils/
        +-- tableUtils/
        +-- ExcelReader.java
```

---

## 6. Architecture Rules

Rule arsitektur didefinisikan di:

```
src/test/java/org/example/ArchitectureTest.java
```

Tujuannya adalah menjaga agar class test tidak bypass factory saat mengambil PageObject.

Contoh yang dilarang:

```java
// Di Test class atau Helper ” DILARANG
new DashboardManageOrganizationPage(driver);
new SlaSettingPage(driver);
```

Contoh yang benar:

```java
// Di Test class ” via Helper
manageOrganizationHelper().isManageOrganizationPageDisplayed();

// Di Test class ” via shortcut accessor dari BaseTest
dashboardManageOrganizationPage().isManageOrganizationHeaderDisplayed();

// Di BaseTest atau Helper ” via factory chain
getFrontOfficePages().manageOrganization().getDashboardManageOrganizationPage();
getMainPages().manageOrganization().getSlaSettingFactory().getSlaSettingPage();
```

Factory di `org.example.factories..` tetap boleh membuat PageObject karena factory adalah jalur resmi pembuatan object.

Jalankan rule:

```bash
mvn test -Dtest=ArchitectureTest
```

---

## 7. Scalability & Maintenance (Best Practice)

Seiring bertambahnya jumlah Page Object (PO), framework harus tetap menjaga pemisahan tanggung jawab antara Test, BaseTest, Helper, Factory, dan Page Object.

### A. Avoid "Fat BaseTest"
Jangan memasukkan semua PO sebagai field permanen di `XxxBaseTest`. Gunakan aturan berikut:

1. **BaseTest hanya untuk setup dan shortcut tipis**: `XxxBaseTest` boleh menyediakan accessor singkat seperti `dashboardManageOrganizationPage()` atau `filterUpdateOrganizationPage()`, tetapi object tetap dibuat melalui factory.
2. **Helper untuk flow lintas halaman**: rangkaian aksi seperti `Dashboard -> Update Organization -> Filter -> Generate Preview` harus dibungkus di `XxxHelper`, sehingga Test layer tetap fokus pada intent dan assertion.
3. **Page Object untuk aksi satu halaman**: locator, klik, input, dan pemilihan dropdown tetap berada di Page Object masing-masing.
4. **Factory sebagai satu pintu pembuatan PO**: jangan instantiate Page Object langsung di Test atau Helper. Selalu akses melalui factory chain.

### B. Fluent Interface (Page Chaining)
Jika aksi pada satu Page Object mengarah ke halaman lain dan factory tersedia pada level Helper, biarkan Page Object menjalankan klik, lalu Helper mengambil halaman berikutnya dari factory. Ini menjaga Page Object tetap sederhana dan tidak bergantung pada factory.

```java
// Di DashboardManageOrganizationPage
public void clickUpdateOrganizationButton() {
    click(updateOrganizationButton);
}

// Di ManageOrganizationHelper
public void navigateToUpdateOrganization() {
    dashboardManageOrganizationPage.clickUpdateOrganizationButton();
    if (!filterUpdateOrganizationPage.isUpdateOrganizationHeaderDisplayed()) {
        throw new RuntimeException("Failed to navigate to Filter Update Organization Page");
    }
}
```

### C. Parallel Safety
Pastikan semua factory dan helper selalu diakses melalui `ThreadLocal` atau di-instantiate di dalam `@BeforeMethod`. Jangan pernah menggunakan `static` field untuk Page Object atau WebDriver.

Contoh pola aman:

```java
private final ThreadLocal<ManageOrganizationHelper> manageOrganizationHelper = new ThreadLocal<>();

@BeforeMethod
public void initManageOrganizationContext() {
    manageOrganizationHelper.set(new ManageOrganizationHelper(getFrontOfficePages()));
}

@AfterMethod(alwaysRun = true)
public void cleanManageOrganizationContext() {
    manageOrganizationHelper.remove();
}
```

---

## 8. Network Monitoring (CDP)

Framework ini dilengkapi dengan `NetworkMonitor`, utilitas berbasis **Chrome DevTools Protocol (CDP)** untuk menangkap response HTTP server error selama test Selenium berjalan.

Tujuan utama fitur ini adalah membantu investigasi saat test UI gagal. HTTP 5xx **tidak otomatis membuat test gagal**. Response 5xx hanya menjadi informasi tambahan di `reports/index.html` jika test tersebut memang sudah failed karena assertion, exception, timeout, atau penyebab UI lain.

### Fitur Utama

- **Auto-capture HTTP 5xx**: Menangkap setiap response dengan status code `>= 500`.
- **Failure-only reporting**: Data network hanya ditulis ke ExtentReports ketika test gagal.
- **Non-blocking behavior**: Status 500 tidak mengubah status TestNG menjadi failed.
- **Response detail**: Menulis URL, status code, status text, mime type, dan response body jika berhasil diambil.
- **Thread-safe buffer**: Event CDP disimpan di buffer per test, lalu di-flush ke report dari lifecycle TestNG.

### Cara Kerja

1. `BaseTest.setUp()` membuat `ChromeDriver`.
2. `NetworkMonitor` dibuat dan menjalankan CDP session sebelum `driver.get(targetUrl)`.
3. Listener CDP mendengarkan event:
   - `Network.responseReceived()` untuk mendeteksi status `>= 500`.
   - `Network.loadingFinished()` untuk mengambil response body.
   - `Network.loadingFailed()` untuk mencatat request yang gagal selesai.
4. Response 5xx disimpan dulu di buffer internal `NetworkMonitor`.
5. Ketika test gagal, `Listeners.onTestFailure()` mengambil monitor aktif dan memanggil `flushServerErrorsToReport(...)`.
6. Detail response 5xx muncul di `reports/index.html` sebagai info tambahan pada test yang failed.

Contoh output di ExtentReports:

```text
API Error Captured During Failed Test (HTTP 500)
URL: https://example/api/organization-preview
Status: 500 Internal Server Error
Mime Type: application/json
Response Body:
{ ... }
```

### Penggunaan

Tidak perlu memanggil `NetworkMonitor` dari test class. Semua test yang extend chain base berikut otomatis memakai network monitoring:

```text
Test class
  -> ManageOrganizationBaseTest / SlaSettingBaseTest / module base
  -> FrontOfficeBaseTest / BackOfficeBaseTest
  -> BaseTest
```

Contoh test tetap fokus pada flow dan assertion:

```java
@Test
public void validateGeneratePreviewTest() {
    manageOrganizationHelper().navigateToUpdateOrganization();
    manageOrganizationHelper().setFilterUpdateOrganizationPage(true);
    Assert.assertTrue(
            filterUpdateOrganizationPage().isOrganizationTreeDisplayed(),
            "Organization Tree is not generated");
}
```

Jika test di atas gagal dan selama flow ada API response `500`, detail API akan otomatis ditempel ke test tersebut di `reports/index.html`.

### Dependency CDP

CDP Selenium harus match dengan major version Chrome yang digunakan. Project ini memakai:

```xml
<selenium.version>4.44.0</selenium.version>

<dependency>
  <groupId>org.seleniumhq.selenium</groupId>
  <artifactId>selenium-devtools-v148</artifactId>
  <version>${selenium.version}</version>
</dependency>
```

Saat Chrome major version berubah, misalnya dari `148` ke `149`, dependency `selenium-devtools-v148` dan import package di `NetworkMonitor` perlu disesuaikan ke versi CDP yang tersedia dari Selenium.

### Troubleshooting

Jika response 500 tidak muncul di report:

1. Pastikan test benar-benar failed. Jika test pass, response 5xx tidak ditulis ke ExtentReports.
2. Cek `logs/automation-test.log` untuk log:

```text
Network monitoring started via CDP
Detected HTTP 500 for URL: ...
```

3. Jika muncul error `no-op implementation of the CDP`, berarti versi Chrome tidak cocok dengan dependency `selenium-devtools-vNN`.
4. Pastikan test berjalan melalui base class project (`BaseTest`) agar monitor otomatis aktif.
5. Pastikan report yang dibuka adalah report run terbaru: `reports/index.html`.

---

## 9. Informasi Tambahan

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
| Selenium | 4.44.0 | Browser automation + Chrome DevTools Protocol v148 |
| TestNG | 7.10.2 | Test framework dan parallelism |
| ExtentReports | 5.0.9 | HTML test report |
| Log4j2 | 2.24.3 | Logging |
| Apache POI | 5.3.0 | Baca data dari Excel |
| Gson | 2.12.1 | Parse JSON config |
| Lombok | 1.18.30 | Boilerplate reduction |
| ArchUnit | 1.4.1 | Architecture rule testing |
