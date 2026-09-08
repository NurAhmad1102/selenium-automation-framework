# Automation Test Framework

Framework automation UI berbasis Java 17, Selenium 4, TestNG, Maven, ExtentReports, Log4j2, Apache POI, dan ArchUnit.

## Status Arsitektur & Refactor Terkini

Framework telah mengalami restrukturisasi dan refaktorisasi arsitektur komprehensif:

1. **Modularisasi Factory**:
   - `ManageOrganizationFactory` dipecah menjadi factory modular:
     - `setOrganizationModals()`: modal khusus Flow 1 (`AddFormModal`, `MergeFormModal`, `MoveFormModal`, `RenameFormModal`, `SplitFormModal`, dsb.)
     - `assignEmployeeModals()`: modal khusus Flow 2 (`AddIncumbentModal`, `EmployeeMovementModal`, `ManageIncumbentModal`, `UnassignedEmployeeListModal`)
     - `commonModals()`: modal bersama lintas flow (`ConfirmationActionModal`, `SuccessModal`, `ChangesLogModal`)
     - `justificationSections()`: sub-section khusus Flow 3
   - Menghilangkan cross-flow dependency dan eager modal initialization.

2. **Pemisahan Tegas Action Helper & Verifier (Pola AAA)**:
   - **Action Helper** (`SetOrganizationHelper`, `AssignEmployeeHelper`): Berfungsi 100% murni sebagai alur aksi UI / Flow Coordinator. Seluruh wrapper assertion, verifikasi audit log, dan query pemeriksaan telah dihapus.
   - **Verifier** (`SetOrganizationVerifier`, `AssignEmployeeVerifier`): Berfungsi 100% murni menangani verifikasi, assertions (`CustomAssert`, `CustomSoftAssert`), pengecekan tags incumbent, serta audit log changes.

3. **Data Caching & Immutability**:
   - `ManageOrganizationExcelReader` menerapkan deep-cloning pada data cache sebelum default merging dijalankan (`new OrganizationTestData(rawData)`).
   - Objek di dalam cache terisolasi dari mutasi data antar test method (`data.setJobName(...)`).

4. **Dekopling BasePage & Custom Dropdown**:
   - Logika manipulasi DOM listbox/dropdown Radix diisolasi ke `CustomDropdownComponent.java`.
   - `BasePage` berfokus pada core interactions, sementara logging log4j2 dan ExtentReport diselaraskan tanpa duplikasi log.

5. **Restrukturisasi Direktori & Standarisasi Package**:
   - Model data terpusat di `org.example.model.frontoffice`.
   - Enums Front Office terpusat di `org.example.enums.frontoffice.manageOrganization`.
   - Main factories terpusat di `org.example.factories.frontoffice` dan `org.example.factories.backoffice`.
   - Casing package Back Office diseragamkan ke lowercase (`org.example.pageObject.backoffice.slaSettingMenu.slasetting`).
   - Resource LOV dan template dipindahkan ke `src/test/resources/data/frontoffice/massiveChanges/`.
   - Generator scripts Python dipindahkan ke `scripts/generators/`.
   - Dokumentasi bisnis dialokasikan ke `docs/businessLogic/` dan `docs/massiveChanges/`.

## Stack

| Area | Teknologi |
| --- | --- |
| Language | Java 17 |
| Build | Maven |
| Test runner | TestNG |
| UI automation | Selenium 4 |
| Reporting | ExtentReports + custom tab renderer |
| Logging | Log4j2 |
| Test data | JSON + Excel via Apache POI |
| Architecture guard | ArchUnit |

## Struktur Project

```text
src/main/java/org/example/
  config/                 Model konfigurasi
  enums/
    frontoffice/
      manageOrganization/ Enums Front Office (AssignmentTag, ConfirmAction, OrgMenuAction, OrgSheetName)
  factories/
    frontoffice/          FrontOfficeMainPagesFactory, ManageOrganizationFactory, modal factories
    backoffice/           BackOfficeMainPagesFactory, SlaSettingFactory
  model/
    frontoffice/          Data model (OrganizationTestData, MassiveOrgModel)
  pageObject/             Page Object dan component UI
    common/               CustomDropdownComponent
    frontoffice/          Page & Modals Front Office
    backoffice/           Page & Modals Back Office
  utils/
    actionUtils/          JSON reader dan action utilities
    report/               Reporting API dan Extent implementation
    resource/             ResourcePathResolver

src/test/java/org/example/
  base/                   BaseTest, BackOfficeBaseTest, FrontOfficeBaseTest
  drivers/                DriverManager ThreadLocal
  listener/               TestNG listener
  test/
    frontoffice/          Test classes, Action Helpers, dan Verifiers
    backoffice/           Test classes Back Office
  utils/                  Test utilities, account, assertion, table, service

src/test/resources/
  data/
    Data Userlogin.xlsx
    backoffice/
      slaStatus/          data.json
    frontoffice/
      manageOrganization/ Manage_organization_Data_Test.xlsx
      massiveChanges/     DeptLOV.txt, JobLOV.txt, LocationLOV.txt, Rc_codeLOV.txt
  testng/
    master/               testng-all.xml
    frontoffice/          od-flow1-all.xml, od-flow1-smoke.xml, etc.
    backoffice/           sla-setting.xml
  log4j2.xml

scripts/
  generators/             Python test data generator scripts

docs/
  businessLogic/          Business-Flow-1-Set-Organization.md, Business_Flow_2-Assign_Employee.md
  massiveChanges/         Dokumentasi massive changes & performance
```

## Arsitektur Singkat

```text
Test Class (turunan ManageOrganizationBaseTest)
  │
  ├── Action Phase:
  │     └─ setOrganizationHelper() / assignEmployeeHelper()
  │           └─ Lazy Page Object / Modals via Factory
  │                 └─ BasePage / CustomDropdownComponent
  │
  └── Assertion Phase:
        └─ setOrganizationVerifier() / assignEmployeeVerifier()
              └─ CustomAssert / CustomSoftAssert
              └─ Direct Page / Modal Inspection
              └─ ChangesLogModal Verification
```

### Standar Penulisan Test (Pola AAA)

```java
@Test(groups = {"smoke"}, description = "Verify that user can add new organization with valid data")
public void addNewOrganizationTest() {
    // 1. Arrange
    OrganizationTestData data = setOrganizationHelper().getTestData(OrgSheetName.ADD_NEW);

    // 2. Act (Murni Alur Aksi UI via Helper)
    manageOrganizationHelper().goToFlow1SetOrganization();
    setOrganizationHelper().addNewOrganization(data);
    setOrganizationHelper().fillJobPosition(data, OrgMenuAction.ADD_NEW);
    setOrganizationHelper().confirmAction(ConfirmAction.ADD);

    // 3. Assert (Murni Verifikasi via Verifier)
    CustomAssert.assertTrue(
        setOrganizationVerifier().isNodeOrganizationDisplayed(data.getNewOrganizationName()),
        "New Organization node is not displayed in the organization tree"
    );
    setOrganizationVerifier().verifyDetailNodeOrganization(data);

    // Audit Log Verification
    setOrganizationHelper().clickChangeLogButton();
    CustomAssert.assertTrue(
        setOrganizationVerifier().verifyOrganizationChangesLog("New Organization", data.getNewOrganizationName(), data.getPrimaryOrganization()),
        "Changes Log for New Organization is not displayed correctly"
    );
}
```

Komponen utama:
- `DriverManager`: menyimpan `WebDriver` per thread via `ThreadLocal`.
- `DevModeUtils`: mengelola status & port Developer Mode (Dev Mode) untuk menempelkan driver ke browser lokal yang sedang berjalan.
- `LoginDataRepository`: membaca `Data Userlogin.xlsx` sekali untuk account dan URL.
- `AccountAllocator`: mengalokasikan akun per role, menyimpan alias akun per test thread, dan melepas semua akun saat teardown.
- `UrlProvider`: mengambil URL Back Office / Front Office dari sheet `URL`.
- `OfficeSessionSwitcher`: switch user dan switch URL Front Office / Back Office dalam satu test thread.
- `ReportAPI`: pintu masuk logging/reporting ke ExtentReports.
- `NetworkMonitor`: capture HTTP 4xx/5xx via CDP.
- `ArchitectureTest`: guard agar test tidak instantiate Page Object langsung.

## Menjalankan Test

```bash
# Suite default dari pom.xml (master/testng-all.xml)
mvn test

# Architecture rules saja
mvn test -Dtest=ArchitectureTest

# Test class tertentu
mvn test -Dtest=AddSlaSettingTest

# Browser tertentu
mvn test -Dbrowser=firefox
```

Suite default:

```text
src/test/resources/testng/master/testng-all.xml
```

Suite Front Office OD smoke:

```text
src/test/resources/testng/frontoffice/od-flow1-smoke.xml
```

## Developer Mode (Dev Mode)

Developer Mode memungkinkan pengujian UI berjalan langsung di browser Chrome/Edge lokal yang sudah terbuka tanpa membuka browser baru, tanpa melakukan re-login di setiap eksekusi, dan tanpa menutup window browser setelah test selesai (`driver.quit()` dilewati).

### 1. Menjalankan Browser dengan Remote Debugging Port

Jalankan Chrome/Edge melalui terminal/command line dengan flag remote debugging (default port `9222`):

```bash
# Windows Chrome
chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"

# Edge
msedge.exe --remote-debugging-port=9222 --user-data-dir="C:\edge-dev-profile"
```

### 2. Memfungsikan Dev Mode

Konfigurasi Dev Mode dibaca dengan urutan prioritas:
1. **System Property**: `-DdevMode=true` atau `-DdevPort=9222`
2. **Environment Variable**: `DEV_MODE=true` atau `DEV_PORT=9222`
3. **File `.env`** pada root project:
   ```env
   DEV_MODE=true
   DEV_PORT=9222
   ```

### 3. Eksekusi Test dalam Dev Mode

```bash
# Menggunakan System Property
mvn test -DdevMode=true -Dtest=MoveOrganizationTest

# Jika menggunakan file .env (DEV_MODE=true)
mvn test -Dtest=MoveOrganizationTest
```

Perilaku utama saat Dev Mode aktif:
- `DriverFactory` akan menempel (attach) ke instance browser yang berjalan di `127.0.0.1:9222`.
- Base navigation & login dilewati jika browser sudah berada pada halaman target atau sudah dalam keadaan logged in.
- `resetBrowserSession()` dan `driver.quit()` dilewati sehingga sesi login dan window browser tetap terbuka untuk mempercepat siklus debugging.

## Konfigurasi Data

Data login dan URL:

```text
src/test/resources/data/Data Userlogin.xlsx
```

Sheet `URL` dibaca oleh `UrlProvider`. Base test memakai key:

```text
Backoffice_staging
Frontoffice_staging
```

Data konfigurasi domain:

```text
src/test/resources/data/data.json
```

Data Front Office Manage Organization:

```text
src/test/resources/data/frontoffice/manageOrganization/Manage_organization_Data_Test.xlsx
```

Data LOV & Template Massive Changes:

```text
src/test/resources/data/frontoffice/massiveChanges/
```

Reader resource memakai `ResourcePathResolver`, sehingga file dibaca dari classpath saat test berjalan dengan fallback otomatis ke filesystem.

## Account Session dan Multi User

Setiap test tetap wajib menentukan default role lewat `getRole()` di base/module test. Akun default itu otomatis disimpan dengan alias `default`, sehingga test biasa tidak perlu setup tambahan.

Untuk flow multi-actor, gunakan alias akun di level test method:

```java
@Test
public void approvalTrxUpdateEmployee() {
    useCurrentUserAs("submitter");
    submit();

    useBackOfficeUser("approver", UserRole.HR_PSOM);
    approve();

    useBackOfficeUser("submitter");
    verify();
}
```

Untuk flow lintas aplikasi, helper Front Office dan Back Office tersedia dari semua turunan `BaseTest`:

```java
@Test
public void approvalTrxUpdateEmployee() {
    useCurrentUserAs("submitter");
    submit();

    useBackOfficeUser("approver", UserRole.HR_PSOM);
    approve();

    useFrontOfficeUser("submitter");
    verify();
}
```

Alias seperti `submitter` dan `approver` hanya berlaku dalam satu test method dan tidak perlu ditambahkan ke Excel. Semua akun yang pernah dialokasikan dalam test thread akan dilepas oleh `BaseTest.tearDown()` melalui `releaseAllAccounts()`.

Dokumentasi teknis lengkap:
- **Account & Session Logic:** [`docs/account-session-logic.md`](docs/account-session-logic.md)
- **Business Flow 1 (Set Organization):** [`docs/businessLogic/Business-Flow-1-Set-Organization.md`](docs/businessLogic/Business-Flow-1-Set-Organization.md)
- **Business Flow 2 (Assign Employee):** [`docs/businessLogic/Business_Flow_2-Assign_Employee.md`](docs/businessLogic/Business_Flow_2-Assign_Employee.md)
- **Reporting System:** [`REPORT.md`](REPORT.md)
- **Project Alignment Guide:** [`docs/project-alignment-guide.md`](docs/project-alignment-guide.md)

## Password Override

Password fallback dari `data.json` dapat dioverride agar secret tidak perlu diubah di repo.

```bash
mvn test -Dautomation.password=your-password
```

Atau:

```bash
AUTOMATION_PASSWORD=your-password
```

Prioritas:
1. System property `automation.password`.
2. Environment variable `AUTOMATION_PASSWORD`.
3. Fallback dari `data.json`.

## Parallel Execution

Suite default berjalan paralel:

```xml
<suite name="Test End To End" parallel="methods" thread-count="3">
```

Komponen yang sudah memakai `ThreadLocal`:
- `DriverManager`
- Main pages factory di base test
- `AccountAllocator.currentAccount`
- `AccountAllocator` alias akun per test thread
- Report storage
- Network monitor

Jika account untuk role tertentu habis saat parallel run, test akan menunggu sampai account dilepas. Timeout default 30 detik dan bisa dioverride:

```bash
mvn test -Daccount.wait.timeout.seconds=120
```

Catatan: generator nama SLA masih menulis state ke `used_names.txt`. Jika parallel execution dinaikkan atau sering jalan di CI, bagian ini perlu dibuat run-scoped dan thread-safe.

## Reporting

Output utama:

```text
reports/index.html
reports/capture/
logs/automation-test.log
```

Flow:

```text
ReportAPI
  -> ReportEventBus
  -> ExtentImplementation
  -> ExtentTabRenderer
  -> reports/index.html
```

Tab report:
- `DETAILS`
- `ELEMENT LOGS`
- `NETWORK LOGS`

Network log hanya mengirim error yang terjadi sampai batas waktu failure saat `capturedBefore` tersedia.

## Architecture Rules

```bash
mvn test -Dtest=ArchitectureTest
```

Rules saat ini:
- Front Office test tidak boleh instantiate Page Object langsung.
- Back Office test tidak boleh instantiate Page Object langsung.
- Utility class tidak boleh instantiate Page Object langsung, kecuali component tertentu.

## Batas Aman Refactor Berikutnya

Aman untuk dilakukan bertahap:
- Tambah env/system property untuk config.
- Tambah resource reader untuk data baru.
- Perbaiki report/network diagnostic.
- Tambah ArchUnit rule.

Perlu regression lebih luas jika mengubah:
- `BaseTest`
- `BasePage`
- `DriverManager`
- `AccountAllocator`
- generator test data yang mempengaruhi parallel run

## Verifikasi Minimal

Setelah refactor ringan-sedang:

```bash
mvn test -Dtest=ArchitectureTest
```

Setelah perubahan account, browser, atau data:

```bash
mvn test -Dtest=AddSlaSettingTest
```

Untuk Front Office Manage Organization:

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng/testng_fo_od_smoke.xml
```
