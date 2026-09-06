# Project Alignment Guide

Dokumen ini adalah pegangan bersama untuk developer dan AI agent saat melanjutkan development project automation ini. Tujuannya sederhana: perubahan berikutnya tetap selaras dengan arsitektur yang sudah ada, tidak memotong lifecycle test, dan tidak membuat test makin sulit dirawat.

## Identitas Project

Project ini adalah UI automation framework berbasis Java 17, Selenium 4, TestNG, Maven, ExtentReports, Log4j2, Apache POI, dan ArchUnit.

Fokus utama framework:

- Menjalankan test UI Back Office dan Front Office.
- Memisahkan test flow dari detail Page Object.
- Mengelola akun test agar aman untuk multi-user dan parallel execution.
- Menghasilkan report yang membantu debugging, bukan hanya status pass/fail.
- Menjaga struktur lewat architecture test agar pola framework tidak rusak pelan-pelan.

## Cara Kerja Besar

Flow utama automation:

```text
Test Class
  -> Module Base Test
  -> BackOfficeBaseTest / FrontOfficeBaseTest
  -> BaseTest
  -> MainPagesFactory
  -> Module Factory
  -> Page Object / Component
  -> BasePage helper
```

Makna tiap layer:

- `BaseTest` mengatur lifecycle WebDriver, account allocation, network monitor, report finalization, screenshot failure, dan cleanup.
- `BackOfficeBaseTest` dan `FrontOfficeBaseTest` menentukan URL awal dan menyiapkan factory sesuai aplikasi.
- Module base test seperti `SlaSettingBaseTest` atau `ManageOrganizationBaseTest` mengatur login dan navigasi awal ke menu/module.
- Test class hanya berisi skenario bisnis dan assertion.
- Factory adalah pintu masuk resmi untuk mengambil Page Object.
- Page Object mengurus locator dan interaksi UI.
- `BasePage` adalah tempat helper interaksi dasar seperti click, type, visibility, dropdown, dan structured element log.

## Filosofi Development

Prinsip project ini adalah automation yang eksplisit, terstruktur, dan mudah didiagnosis.

Test sebaiknya membaca seperti skenario bisnis. Detail locator, click, dropdown, dan modal jangan bocor ke test class kecuali memang skenario membutuhkan detail tersebut.

Framework lebih memilih pola yang konsisten daripada jalan pintas. Kalau sebuah Page Object harus dipakai, akses melalui factory/module base. Jangan instantiate langsung di test meskipun terasa lebih cepat.

Refactor harus kecil dan bertahap. Area seperti `BaseTest`, `BasePage`, `DriverManager`, `AccountAllocator`, reporting, dan generator data punya efek lintas test. Perubahan di area itu harus disertai verifikasi lebih luas.

Data runtime dan secret harus bisa dioverride dari luar. Jangan menanam password baru atau environment-specific value langsung ke code.

## Struktur Penting

```text
src/main/java/org/example/
  config/                 Model config dari JSON dan Excel
  factories/              Factory untuk Page Object
  pageObject/             Page Object dan UI component
  utils/actionUtils/      Utility aksi dan JSON reader
  utils/report/           Reporting core dan Extent implementation
  utils/resource/         Resolver resource classpath/file fallback

src/test/java/org/example/
  base/                   Base lifecycle dan office session switching
  drivers/                ThreadLocal WebDriver manager
  listener/               TestNG listener dan report bootstrap
  mainPagesFactory/       Entry factory Back Office / Front Office
  test/                   Test scenario per module
  utils/                  Test data, account, assertion, table, network helper, DevModeUtils

src/test/resources/
  data/                   JSON, Excel login, dan data test
  testng/                 Suite TestNG
  log4j2.xml              Logging config

docs/
  account-session-logic.md
  project-alignment-guide.md
```

## Source of Truth

Gunakan sumber berikut saat ada ketidaksamaan informasi:

- Dependency, Java version, dan default suite property: `pom.xml`.
- Suite yang benar-benar dijalankan: nilai `-DsuiteXmlFile=...` atau property `suiteXmlFile` di `pom.xml`.
- Parallel setting: file XML TestNG yang sedang dipakai, bukan asumsi dari README.
- Data login dan URL: `src/test/resources/data/Data Userlogin.xlsx`.
- Config domain dan fallback password: `../src/test/resources/data/backoffice/slaStatus/data.json`.
- Account/session behavior: `docs/account-session-logic.md`.
- Dev Mode configuration & helper: `DevModeUtils.java` dan `.env`.
- Architecture boundary: `src/test/java/org/example/ArchitectureTest.java`.

## Lifecycle Test

`BaseTest.setUp()` melakukan hal berikut:

1. Menandai report sebagai dikelola oleh base test.
2. Memulai step setup di `ReportAPI`.
3. Mengalokasikan akun default berdasarkan `getRole()`.
4. Membuat WebDriver sesuai system property `browser` (atau menempel ke browser berjalan jika `DevModeUtils.isDevMode()` aktif).
5. Menyimpan driver ke `DriverManager`.
6. Memulai network monitor jika driver mendukung DevTools.
7. Membuka URL dari `getUrl()` (dilewati jika Dev Mode aktif untuk mempertahankan state URL/halaman saat ini).

`BaseTest.tearDown()` melakukan hal berikut:

1. Flush network error relevan saat test gagal.
2. Finalisasi report pass/fail/skip/broken/blocked.
3. Screenshot saat failure.
4. Quit driver (dilewati jika `DevModeUtils.isDevMode()` aktif untuk membiarkan browser tetap terbuka).
5. Clear office session, account allocation, ThreadLocal, dan Log4j ThreadContext.

Jangan menambah lifecycle paralel di test class jika bisa ditaruh di module base. Jangan memanggil `DriverManager.quitDriver()` manual dari test.

## Developer Mode (Dev Mode)

Developer Mode (`DevModeUtils`) dirancang untuk mempercepat siklus debugging dan pengujian interaktif lokal:

- **Attaching Browser**: Driver menempel ke browser Chrome/Edge yang sudah berjalan lewat `127.0.0.1:9222` (atau port dari `devPort`).
- **Persistensi Sesi**: `resetBrowserSession()` dan `driver.quit()` dilewati agar cookie, session login, dan window browser pengguna/developer tidak tertutup/teriset di akhir test.
- **Smart Bypass Login**: Module base test (`SlaSettingBaseTest`, `ManageOrganizationBaseTest`) dan `OfficeSessionSwitcher` mengecek apakah browser sudah berada di URL target atau sudah logged in; jika ya, langkah re-login dan navigasi ulang dilewati secara otomatis.
- **Konfigurasi Flexible**:
  1. System property: `-DdevMode=true`, `-DdevPort=9222`
  2. Environment variable: `DEV_MODE=true`, `DEV_PORT=9222`
  3. File `.env` di root project (`DEV_MODE=true`, `DEV_PORT=9222`)

## Account dan Session

Setiap test harus punya role awal lewat `getRole()`. Biasanya ini didefinisikan di module base test, bukan di tiap test class.

`AccountAllocator.allocateAccount(testName, getRole())` menyimpan akun awal sebagai alias `default`. Untuk flow multi-actor, test bisa memberi alias bisnis:

```java
useCurrentUserAs("submitter");
useBackOfficeUser("approver", UserRole.HR_PSOM);
useFrontOfficeUser("submitter");
```

Aturan penting:

- Alias hanya berlaku dalam satu test method/thread.
- Alias tidak perlu ditambahkan ke Excel.
- Semua akun yang dialokasikan dalam satu test thread dilepas oleh teardown.
- Gunakan `currentAccount()`, `currentNip()`, atau `accountNip("alias")` untuk user aktif.
- Field seperti `loginNip()` di module base biasanya merepresentasikan user awal module context, bukan selalu user aktif setelah switch.

Detail lengkap ada di `docs/account-session-logic.md`.

## Factory dan Page Object

Test class tidak boleh instantiate Page Object langsung.

Benar:

```java
getMainPages()
    .manageOrganization()
    .getSlaSettingFactory()
    .getSlaSettingPage();
```

Benar:

```java
getFrontOfficePages()
    .manageOrganization()
    .getDashboardManageOrganizationPage();
```

Salah:

```java
new SlaSettingPage(driver);
new DashboardManageOrganizationPage(driver);
```

Alasannya:

- Factory menjaga konsistensi dependency WebDriver.
- Module base bisa menyediakan helper yang lebih stabil.
- ArchUnit sudah menjaga aturan ini lewat `ArchitectureTest`.
- Direct instantiation membuat test cepat rapuh saat constructor/factory berubah.

Jika menambah Page Object baru:

1. Letakkan di package `pageObject` sesuai aplikasi dan module.
2. Extend atau gunakan pola `BasePage` jika ada interaksi Selenium.
3. Tambahkan factory method di module factory terkait.
4. Akses dari module base/helper/test melalui factory.
5. Jalankan `mvn test -Dtest=ArchitectureTest`.

## Data dan Resource

Path standar Maven adalah `src/test/resources`.

Gunakan `ResourcePathResolver` untuk file yang perlu dibaca dari resource, dengan fallback hanya jika perlu kompatibilitas path lama.

Contoh pola yang sudah benar:

```java
ResourcePathResolver.resolve(
    "data/data.json",
    "src/test/resources/data/data.json",
    "src/test/java/resources/data/data.json"
);
```

Jangan hardcode path lama `src/test/java/resources` untuk fitur baru.

Password fallback diambil dari `data.json`, tapi runtime override harus diutamakan:

```bash
mvn test -Dautomation.password=your-password
```

atau environment variable:

```bash
AUTOMATION_PASSWORD=your-password
```

Prioritas password:

1. System property `automation.password`.
2. Environment variable `AUTOMATION_PASSWORD`.
3. Fallback `data.json`.

## Reporting dan Debugging

Report utama:

```text
reports/index.html
reports/capture/
logs/automation-test.log
```

Flow reporting:

```text
ReportAPI
  -> ReportEventBus
  -> ExtentImplementation
  -> ExtentTabRenderer
  -> reports/index.html
```

Gunakan `ReportAPI.step(...)`, `ReportAPI.warning(...)`, `ReportAPI.fail(...)`, atau custom assertion helper agar report tetap informatif.

`BasePage` sudah mengirim structured element log saat interaksi gagal. Hindari menangkap exception Selenium lalu membuang detailnya tanpa meneruskan ke report.

Network monitor menangkap HTTP 4xx/5xx via CDP. Saat test gagal, teardown hanya flush error yang relevan sampai waktu failure. Kalau mengubah bagian network/reporting, pastikan report masih menampilkan details, element logs, network logs, dan screenshot failure.

## Parallel Execution

Framework memakai `ThreadLocal` untuk beberapa state penting:

- WebDriver di `DriverManager`.
- Account alias/current account di `AccountAllocator`.
- Office page factory di `BaseTest`/`OfficeSessionSwitcher`.
- Report data.
- Network monitor.
- Beberapa module context seperti helper dan test data.

Parallel setting hidup di file TestNG XML yang dipakai. Jangan mengubah `parallel` atau `thread-count` tanpa mengecek:

- Jumlah akun per role di Excel.
- Apakah test memakai data yang sama dan merusak state aplikasi.
- Apakah generator nama/data sudah run-scoped dan thread-safe.
- Apakah file state seperti `used_names.txt` aman untuk run paralel.

Catatan risiko saat ini: generator nama SLA masih memakai file state `used_names.txt`. Jika parallel dinaikkan atau masuk CI, bagian ini perlu dibuat run-scoped dan lebih aman untuk concurrency.

## Architecture Rules

`ArchitectureTest` adalah guard utama agar struktur tidak rusak.

Rules saat ini:

- Test Front Office tidak boleh instantiate Page Object langsung.
- Test Back Office tidak boleh instantiate Page Object langsung.
- Utility class tidak boleh instantiate Page Object langsung, kecuali component tertentu dan `BasePage`.

Jalankan minimal:

```bash
mvn test -Dtest=ArchitectureTest
```

Jika rule gagal, jangan bypass rule dengan memindahkan code sembarangan. Perbaiki alur factory/dependency injection-nya.

## Area Sensitif

Perubahan ringan yang umumnya aman:

- Menambah Page Object method kecil.
- Menambah factory method untuk Page Object baru.
- Menambah test scenario di module yang sudah ada.
- Menambah test data resource baru lewat `src/test/resources`.
- Menambah assertion/helper yang tidak mengubah lifecycle.

Perubahan yang perlu verifikasi lebih luas:

- `BaseTest`
- `BasePage`
- `DriverManager`
- `AccountAllocator`
- `OfficeSessionSwitcher`
- `Listeners`
- `ReportAPI` dan Extent implementation
- `NetworkMonitor`
- TestNG parallel config
- Excel login structure
- Generator nama/data yang menulis file state

Perubahan yang sebaiknya tidak dilakukan tanpa alasan kuat:

- Memindahkan tanggung jawab login ke tiap test class.
- Membuat WebDriver baru di luar `BaseTest`.
- Menggunakan static mutable state tanpa `ThreadLocal` atau locking yang jelas.
- Menaruh secret/password environment di repository.
- Menonaktifkan `ArchitectureTest` agar build lewat.

## Pola Menambah Test Baru

1. Pilih base yang sesuai: `BackOfficeBaseTest` atau `FrontOfficeBaseTest`, biasanya lewat module base yang sudah ada.
2. Pastikan role awal tersedia lewat `getRole()`.
3. Tambahkan Page Object method jika locator/interaksi belum tersedia.
4. Ekspos Page Object lewat factory, bukan constructor langsung.
5. Taruh navigasi umum di module base/helper.
6. Buat test class fokus pada skenario bisnis.
7. Pakai assertion helper/report API agar failure mudah dibaca.
8. Tambahkan test ke suite XML yang relevan jika memang perlu masuk suite.

## Pola Menambah Module Baru

1. Buat package Page Object di `src/main/java/org/example/pageObject/<office>/<module>`.
2. Buat factory module di `src/main/java/org/example/factories/<office>/<module>`.
3. Tambahkan entry di `BackOfficeMainPagesFactory` atau `FrontOfficeMainPagesFactory`.
4. Buat module base test di `src/test/java/org/example/test/<office>/<module>`.
5. Definisikan login/navigasi awal module di module base.
6. Simpan helper module di package test module jika helper hanya untuk test.
7. Tambahkan suite XML atau method include sesuai kebutuhan.
8. Jalankan architecture test.

## Panduan Khusus AI Agent

Saat AI agent masuk ke project ini:

1. Baca dokumen ini, `README.md`, dan `docs/account-session-logic.md`.
2. Cek status worktree sebelum edit. Banyak file bisa sedang diubah manusia/agent lain.
3. Jangan revert perubahan yang tidak dibuat sendiri.
4. Jangan membuat shortcut yang melanggar factory/Page Object boundary.
5. Jangan mengubah lifecycle inti jika request bisa diselesaikan di layer module/test/helper.
6. Jangan mengubah data Excel/JSON kecuali task memang meminta.
7. Kalau menambah file resource, gunakan `src/test/resources`.
8. Kalau menambah code yang membaca file resource, gunakan `ResourcePathResolver`.
9. Kalau menyentuh account/session/parallel/reporting, perlu verifikasi lebih luas.
10. Setelah edit, jalankan verifikasi yang sepadan dengan risiko.

Checklist sebelum selesai:

- Apakah test masih memakai factory untuk Page Object?
- Apakah semua `ThreadLocal` baru dibersihkan?
- Apakah password/URL tidak di-hardcode?
- Apakah suite XML yang diubah memang suite yang dimaksud?
- Apakah report masih mendapat step/failure detail yang berguna?
- Apakah `mvn test -Dtest=ArchitectureTest` lulus?
- Apakah dokumen terkait ikut diperbarui jika behavior berubah?

## Verifikasi Rekomendasi

Untuk perubahan arsitektur/factory ringan:

```bash
mvn test -Dtest=ArchitectureTest
```

Untuk perubahan Back Office SLA:

```bash
mvn test -Dtest=AddSlaSettingTest
```

Untuk perubahan Front Office Manage Organization:

```bash
mvn test -DsuiteXmlFile=src/test/resources/testng/testng_fo_od_smoke.xml
```

Untuk menjalankan suite sesuai default `pom.xml`:

```bash
mvn test
```

Baca Gans: Kalo misal nih, test UI gak bisa di run di environment lokal, minimalah pastikan dulu compile/architecture test di run atau jelaskan alasan tidak bisa menjalankan test.
