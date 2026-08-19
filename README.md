# Automation Test Framework

Framework automation UI berbasis Java 17, Selenium 4, TestNG, Maven, ExtentReports, Log4j2, Apache POI, dan ArchUnit.

## Status Refactor Aman

Refactor ini fokus ke area ringan-sedang dan tidak mengubah inti lifecycle automation.

Yang diubah:
- Resource test dipindahkan ke standar Maven: `src/test/resources`.
- JSON dan Excel reader memakai `ResourcePathResolver` dengan fallback ke path lama.
- Password default bisa dioverride lewat system property atau environment variable.
- Network log memakai batas waktu `capturedBefore` saat flush error.
- README disesuaikan dengan struktur aktual.

Yang tidak diubah:
- `BaseTest` setup/teardown lifecycle.
- `BasePage` click, wait, type, dropdown, dan action helper.
- `DriverManager`.
- Test class dan flow bisnis yang sudah ada.

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
  factories/              Factory untuk Page Object
  pageObject/             Page Object dan component UI
  utils/
    actionUtils/          JSON reader dan action utilities
    report/               Reporting API dan Extent implementation
    resource/             ResourcePathResolver

src/test/java/org/example/
  base/                   BaseTest, BackOfficeBaseTest, FrontOfficeBaseTest
  drivers/                DriverManager ThreadLocal
  listener/               TestNG listener
  mainPagesFactory/       Entry factory per interface
  test/                   Test class per module
  utils/                  Test utilities, account, assertion, table, service

src/test/resources/
  data/
    data.json
    Data Userlogin.xlsx
    frontoffice/Manage_organization_Data_Test.xlsx
  testng/
    master/
      testng-all.xml
    frontoffice/
      od-flow1-all.xml
      od-flow1-single-action.xml
      od-flow1-multi-action.xml
      od-flow1-smoke.xml
    backoffice/
      sla-setting.xml
    debug/
      debug-local.xml
      debug-fo.xml
  log4j2.xml
```

## Arsitektur Singkat

```text
Test Class
  -> BackOfficeBaseTest / FrontOfficeBaseTest
  -> BaseTest
  -> MainPagesFactory
  -> Module Factory
  -> Page Object
  -> BasePage action helper
```

Komponen utama:
- `DriverManager`: menyimpan `WebDriver` per thread.
- `DevModeUtils`: mengelola status & port Developer Mode (Dev Mode) untuk menempelkan driver ke browser lokal yang sedang berjalan.
- `LoginDataRepository`: membaca `Data Userlogin.xlsx` sekali untuk account dan URL.
- `AccountAllocator`: mengalokasikan akun per role, menyimpan alias akun per test thread, dan melepas semua akun saat teardown.
- `UrlProvider`: mengambil URL Back Office / Front Office dari sheet `URL`.
- `OfficeSessionSwitcher`: switch user dan switch URL Front Office / Back Office dalam satu test thread.
- `ReportAPI`: pintu masuk logging/reporting.
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
src/test/resources/data/frontoffice/Manage_organization_Data_Test.xlsx
```

Reader resource memakai `ResourcePathResolver`, sehingga file dibaca dari classpath saat test berjalan. Fallback ke path lama tetap tersedia untuk kompatibilitas, tetapi path standar baru adalah `src/test/resources`.

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
- **Business Flow 1 (Set Organization):** [`docs/business-flow-1-set-organization.md`](src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/data/businessLogic/Business-Flow-1-Set-Organization.md)
- **Business Flow 2 (Assign Employee):** [`docs/Business_Flow_2-Assign_Employee.md`](src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/data/businessLogic/Business_Flow_2-Assign_Employee.md)
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
