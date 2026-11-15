# 🚀 Enterprise Selenium 4 Automation Framework

[![CI Build](https://github.com/OWNER/REPOSITORY/actions/workflows/ci.yml/badge.svg)](https://github.com/OWNER/REPOSITORY/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-17%2B-blue?style=flat-square&logo=openjdk)](https://openjdk.org/)
[![Selenium](https://img.shields.io/badge/Selenium-4.44.0-43B02A?style=flat-square&logo=selenium)](https://www.selenium.dev/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red?style=flat-square)](https://testng.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg?style=flat-square)](LICENSE)

<!-- Catatan: Ganti OWNER/REPOSITORY pada badge di atas dengan path repositori GitHub Anda setelah di-push -->

Framework otomatisasi pengujian web berbasis **Java 17** dan **Selenium 4** yang dirancang dengan standar arsitektur kelas enterprise, **100% project-agnostic**, dan modular. Siap digunakan sebagai fondasi reusable untuk berbagai macam aplikasi web tanpa perlu membangun infrastruktur otomasi dari awal.

---

## 📑 Daftar Isi
- [Daftar Tools & Teknologi](#-daftar-tools--teknologi)
- [Arsitektur & Struktur Proyek](#-arsitektur--struktur-proyek)
- [Instalasi & Penggunaan di Proyek Anda](#-instalasi--penggunaan-di-proyek-anda)
- [Fungsi Komponen Utama](#-fungsi-komponen-utama)
- [Prasyarat Sistem](#-prasyarat-sistem)
- [Panduan Penggunaan (Contoh 1 Test Lengkap)](#-panduan-penggunaan-contoh-1-test-lengkap)
- [Fitur Unggulan](#-fitur-unggulan)
  - [Developer Mode (DevMode)](#1-developer-mode-devmode---hemat-waktu-debugging)
  - [Pelaporan Interaktif 3-Tab & CDP Error Capture](#2-pelaporan-interaktif-3-tab--cdp-error-capture)
  - [Multi-User Account Pooling](#3-multi-user-account-pooling-thread-safe)
  - [Komponen UI Modern (Dropdown Radix UI & WebTable)](#4-penanganan-komponen-ui-modern)
- [Integrasi CI/CD](#-integrasi-cicd)
- [Troubleshooting & FAQ](#-troubleshooting--faq)
- [Dokumentasi Lengkap](#-dokumentasi-lengkap)
- [Lisensi](#-lisensi)

---

## 🛠️ Daftar Tools & Teknologi

Framework ini mengintegrasikan pustaka-pustaka terbaik di ekosistem Java & QA Automation:

| Alat / Library | Versi | Peran & Fungsi dalam Framework |
|----------------|-------|--------------------------------|
| **Java SDK** | `17 (LTS)` | Bahasa pemrograman utama dengan performa modern, Records, dan Pattern Matching. |
| **Selenium WebDriver** | `4.44.0` | Mesin penggerak browser web (Chrome, Firefox, Edge) berbasis W3C WebDriver standard. |
| **TestNG** | `7.10.2` | Test runner, lifecycle management (`@Test`, `@BeforeMethod`, `@DataProvider`), dan eksekusi paralel. |
| **ExtentReports** | `5.0.9` | Mesin pelaporan HTML interaktif yang telah dikustomisasi menjadi antarmuka 3-Tab. |
| **Chrome DevTools Protocol (CDP)** | `v148+` | Monitoring lalu lintas jaringan secara real-time untuk mendeteksi error HTTP (4xx / 5xx) backend. |
| **Apache POI** | `5.3.0` | Utilitas pembaca data pengujian dari file Microsoft Excel (`.xlsx`). |
| **Google Gson** | `2.12.1` | Serialisasi dan deserialisasi data pengujian berformat JSON ke dalam Java POJO / model. |
| **Log4j2** | `2.24.3` | Logging terstruktur asynchronous berkecepatan tinggi dengan level granular (INFO, DEBUG, ERROR). |
| **ArchUnit** | `1.4.1` | Penjaga arsitektur kode (Architecture Guard) untuk memastikan modul core tetap 100% agnostic. |
| **Lombok** | `1.18.30` | Reduksi boilerplate kode (`@Data`, `@Getter`, `@Builder`). |
| **Apache Maven** | `3.8+` | Manajemen build, dependensi, dan siklus hidup multi-module reactor. |
| **GitHub Actions** | `v4` | Otomasi pipeline CI/CD untuk pengujian headless di lingkungan cloud Linux. |

---

## 🏛️ Arsitektur & Struktur Proyek

Proyek ini menggunakan struktur **Maven Multi-Module**:

```text
automations-framework/
├── .github/workflows/
│   └── ci.yml                          # ⚙️ GitHub Actions CI Pipeline
│
├── framework-core/                     # 📦 CORE LIBRARY (100% Agnostik, Reusable)
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
├── framework-sample/                   # 💡 STARTER TEMPLATE & DEMO APPLICATION
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
├── docs/framework/                     # 📚 BUKU PANDUAN & DOKUMENTASI TEKNIS
│   ├── getting-started.md              # Panduan setup awal
│   ├── page-object-pattern.md          # Panduan penulisan Page Object & komponen
│   ├── test-data-management.md         # Panduan JSON & Excel DataReader
│   ├── developer-mode.md               # Panduan Developer Mode (port 9222)
│   ├── reporting-and-network-monitoring.md # Panduan ExtentReports 3-Tab & CDP
│   ├── multi-account-and-parallel-testing.md # Panduan Account Pooling & Thread-safety
│   ├── ci-cd-pipeline.md               # Panduan CI/CD GitHub Actions & Jenkins
│   ├── migration-guide.md              # Panduan migrasi dari framework monolitik lama
│   └── component-reference.md          # Dokumentasi teknis referensi semua kelas
│
├── pom.xml                             # 🛠️ Root Parent POM (framework-parent)
├── LICENSE                             # 📄 Lisensi Open Source (Apache 2.0)
└── README.md                           # 📖 Dokumentasi utama proyek
```

---

## 📦 Instalasi & Penggunaan di Proyek Anda

Anda dapat mengintegrasikan `framework-core` ke dalam proyek pengujian otomasi Anda dengan beberapa cara mudah:

### Opsi 1: Instalasi Lokal Maven (Direkomendasikan untuk Tim Internal)
Clone repositori ini dan jalankan perintah install ke Maven repository lokal Anda (`~/.m2/repository`):

```bash
git clone https://github.com/OWNER/REPOSITORY.git
cd REPOSITORY
mvn clean install -DskipTests
```

Kemudian pada file `pom.xml` di proyek pengujian Anda, tambahkan dependensi `framework-core`:

```xml
<dependency>
    <groupId>com.automation.framework</groupId>
    <artifactId>framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Opsi 2: Menggunakan JitPack (Tanpa Setup Server Artefak)
Jika repositori ini di-host secara publik di GitHub, pengguna dapat langsung mengonsumsi pustaka ini via [JitPack](https://jitpack.io):

1. Tambahkan repository JitPack di `pom.xml` proyek Anda:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

2. Tambahkan dependensi:
```xml
<dependency>
    <groupId>com.github.OWNER</groupId>
    <artifactId>framework-core</artifactId>
    <version>1.0.0</version>
</dependency>
```

### Opsi 3: Gunakan `framework-sample` Sebagai Starter Template (Fork / Clone)
Bagi yang ingin langsung mulai membuat test suite baru tanpa konfigurasi pom dari nol:
1. Clone repositori ini.
2. Gunakan modul `framework-sample` sebagai boiler-plate.
3. Ubah konfigurasi `base.url` di `src/test/resources/application.properties` dan buat Page Object serta Test Anda sendiri.

---

## 🧩 Fungsi Komponen Utama

Setiap komponen dirancang dengan prinsip **Single Responsibility**, **High Cohesion**, dan **Loose Coupling**:

### 1. Centralized Configuration Layer (`FrameworkConfig`)
Menggunakan hierarki cascading dinamis tanpa hardcode:
$$\text{CLI System Properties } (-D) \longrightarrow \text{Environment Variables} \longrightarrow \text{.env File} \longrightarrow \text{application.properties} \longrightarrow \text{framework.properties}$$
Nilai timeout, peramban target, URL basis, dan flag headless dapat diubah secara instan saat runtime.

### 2. Driver Management (`DriverManager`, `DriverFactory`, `DevModeUtils`)
* **Thread Safety**: Menyimpan instance browser di dalam `ThreadLocal<WebDriver>`.
* **Multi-Browser**: Mendukung Chrome, Firefox, dan Microsoft Edge dengan profil terisolasi.
* **DevMode**: Kemampuan unik untuk menyambungkan Selenium ke browser yang sudah terbuka via remote debugging port 9222.

### 3. Page Object & Component Engine (`BasePage`, `DropdownComponent`, `WebTable`)
* **Resilient Actions**: Metode `click()`, `typeText()`, `getText()` dilengkapi explicit wait bawaan dan auto-scroll JavaScript (juga tersedia alias `type()` dan `isDisplayed()`).
* **Auto JavaScript Fallback**: Bila klik native Selenium terhalang oleh overlay UI (misalnya loading spinner atau modal), `BasePage` otomatis mengeksekusi fallback click via JavaScript tanpa membuat test gagal.
* **Komponen Khusus**: Dukungan langsung untuk dropdown modern (Radix UI / Headless UI virtual list) dan pembacaan tabel HTML dinamis berdasarkan nama kolom header.

### 4. Pelaporan Interaktif 3-Tab & CDP Error Capture (`ReportAPI`, `ExtentTabRenderer`, `NetworkMonitor`)
Setiap pengujian menghasilkan laporan HTML yang terbagi ke dalam 3 tab:
* **Tab 1: DETAILS**: Langkah-langkah pengujian langkah demi langkah.
* **Tab 2: ELEMENT LOGS**: Diagnostik kegagalan 6-pilar saat elemen tidak ditemukan (Computed CSS, Bounding Box, Element Blocker, OuterHTML, dan ringkasan Stacktrace).
* **Tab 3: NETWORK LOGS**: Penangkapan otomatis respons backend yang gagal (HTTP status 4xx / 5xx) via Chrome DevTools Protocol tanpa proxy eksternal.

### 5. Multi-User Account Allocator (`AccountAllocator`, `UserAccount`)
Mekanisme antrean akun pengguna thread-safe berbasis peran (*role-based pooling*). Menghindari tabrakan sesi saat eksekusi paralel dan mendukung skenario workflow bertingkat (misalnya: Maker &rarr; Checker &rarr; Approver).

### 6. Pluggable Authentication (`LoginHandler`)
Pemisahan logika autentikasi dari framework inti. Pengujian dapat menggunakan form login, bypass cookie SSO, atau token injeksi hanya dengan mengimplementasikan antarmuka `LoginHandler`.

### 7. Data Readers (`DataReader<T>`, `JsonDataReader`, `ExcelDataReader`)
Membaca data pengujian dari file JSON (dipetakan langsung ke model Java/DTO) atau spreadsheet Excel (`.xlsx`) ke dalam format `List<Map<String, String>>`.

### 8. Cleanup & Anti-Leak Sweeper (`BrowserProcessCleaner`)
Secara otomatis membersihkan proses zombie (`chromedriver.exe`, `chrome.exe`) dan menghapus folder profil sementara di akhir siklus eksekusi.

---

## 💻 Prasyarat Sistem

Sebelum menjalankan proyek, pastikan perangkat Anda telah terpasang:
* **Java Development Kit (JDK)**: Versi 17 atau yang lebih baru ([Unduh OpenJDK 17](https://adoptium.net/)).
* **Apache Maven**: Versi 3.8.0 atau yang lebih baru ([Unduh Maven](https://maven.apache.org/download.cgi)).
* **Web Browser**: Google Chrome versi terbaru (atau Firefox / Edge).
* **Git**: Versi 2.30+.

Verifikasi instalasi dengan perintah berikut:
```bash
java -version
mvn -version
```

---

## 🚀 Panduan Penggunaan (Contoh 1 Test Lengkap)

Berikut adalah panduan lengkap langkah demi langkah untuk membuat dan menjalankan satu skenario pengujian otomasi end-to-end dari awal.

### Langkah 1: Buat Page Object Class
Buat kelas Page Object yang mewakili halaman web dengan mewarisi `com.automation.framework.core.page.BasePage`. Semua locator dan interaksi elemen dibungkus di sini:

```java
package com.mycompany.pages;

import com.automation.framework.core.page.BasePage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class SearchPage extends BasePage {

    // 1. Definisikan locator elemen privat
    private final By searchInputBox = By.name("q");
    private final By searchSubmitBtn = By.name("btnK");
    private final By searchResultsContainer = By.id("search");

    // 2. Konstruktor menerima WebDriver
    public SearchPage(WebDriver driver) {
        super(driver);
    }

    // 3. Buat metode aksi yang jelas dan mudah dibaca
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

### Langkah 2: Konfigurasi Parameter Uji (`application.properties`)
Tempatkan file konfigurasi di `src/test/resources/application.properties`:

```properties
# Target Browser: chrome, firefox, edge
browser=chrome

# Mode Headless (true untuk CI/CD, false untuk inspeksi visual)
headless=false

# Base URL Aplikasi
base.url=https://example.com

# Timeout Pengujian Default (Detik)
timeout.default=15
```

---

### Langkah 3: Buat Kelas Test Base (Opsional untuk Abstraksi URL)
Wariskan `com.automation.framework.core.test.BaseTest` untuk menentukan URL awal pengujian atau konfigurasi autentikasi:

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

> [!TIP]
> `BaseTest` secara default langsung membaca `base.url` dari `FrameworkConfig.getBaseUrl()`. Override metode `getUrl()` bersifat opsional, hanya jika Anda memerlukan penyesuaian URL dinamis.
```

---

### Langkah 4: Tulis Test Case TestNG
Wariskan kelas base dan tulis skenario uji dengan assertion kustom yang secara otomatis menangkap screenshot jika verifikasi gagal:

```java
package com.mycompany.tests;

import com.automation.framework.core.assertion.CustomAssert;
import com.automation.framework.core.driver.DriverManager;
import com.automation.framework.core.report.core.ReportAPI;
import com.mycompany.base.ApplicationBaseTest;
import com.mycompany.pages.SearchPage;
import org.testng.annotations.Test;

public class SearchWorkflowTest extends ApplicationBaseTest {

    @Test(description = "Memverifikasi pencarian kata kunci dan tampilan hasil")
    public void testSearchFunctionality() {
        ReportAPI.step("Inisialisasi Search Page Object");
        SearchPage searchPage = new SearchPage(DriverManager.getDriver());

        ReportAPI.step("Memasukkan kata kunci pencarian");
        searchPage.enterKeyword("Selenium Automation Framework");

        ReportAPI.step("Menekan tombol cari");
        searchPage.submitSearch();

        ReportAPI.step("Memvalidasi hasil pencarian muncul di layar");
        CustomAssert.assertTrue(
            searchPage.isResultDisplayed(),
            "Hasil pencarian harus tampil di layar setelah submit"
        );
    }
}
```

---

### Langkah 5: Jalankan Pengujian

#### Menjalankan via Maven CLI:
```bash
# Jalankan seluruh test suite dari terminal
mvn test

# Jalankan dalam mode headless
mvn test -Dheadless=true

# Jalankan hanya pada browser Firefox
mvn test -Dbrowser=firefox
```

#### Menjalankan via TestNG Suite XML:
```bash
mvn test -DsuiteXmlFile=src/test/resources/testng/sample-suite.xml
```

---

### Langkah 6: Buka Laporan Interaktif
Setelah eksekusi selesai, laporan pengujian HTML akan dibuat secara otomatis di direktori `reports/`:
```text
reports/
└── ExtentReport_20261108_161500.html
```
Buka file tersebut di browser apa pun untuk melihat visualisasi langkah uji, diagnostik elemen, dan log request API yang gagal.

---

## 🌟 Fitur Unggulan

### 1. Developer Mode (DevMode) - Hemat Waktu Debugging!
Saat mengembangkan pengujian untuk halaman yang membutuhkan proses login rumit atau multi-step form, Anda tidak perlu mengulang proses login setiap kali test dijalankan.

1. Buka Google Chrome dengan remote debugging port aktif:
   ```bash
   chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"
   ```
2. Lakukan login secara manual di browser tersebut dan buka halaman yang ingin diuji.
3. Jalankan pengujian Anda dengan flag `-DdevMode=true`:
   ```bash
   mvn test -DdevMode=true -Dtest=SearchWorkflowTest
   ```
> **Hasil**: Selenium akan langsung terhubung ke tab browser yang sedang aktif tersebut tanpa membuka browser baru, tanpa login ulang, dan tanpa menutup browser saat test selesai!

---

### 2. Pelaporan Interaktif 3-Tab & CDP Error Capture
Laporan pengujian menyediakan transparansi penuh untuk developer dan QA:
* **DETAILS**: Log eksekusi terstruktur dengan snapshot waktu nyata.
* **ELEMENT LOGS**: Menganalisa penyebab NoSuchElement atau ElementClickIntercepted hingga ke CSS computed style dan koordinat elemen penutup (*blocker overlay*).
* **NETWORK LOGS**: Mengidentifikasi kegagalan HTTP 400, 401, 403, 500, atau 502 secara langsung dari DevTools Protocol browser tanpa perlu konfigurasi proxy manual.

---

### 3. Multi-User Account Pooling (Thread-Safe)
Kelola ratusan akun pengguna untuk pengujian multi-thread tanpa race-condition:

```java
// Opsi A: Alokasi manual programatik
UserAccount admin = AccountAllocator.allocate("ADMIN");
try {
    // Jalankan transaksi admin
} finally {
    AccountAllocator.release(admin);
}

// Opsi B: Alokasi otomatis berbasis lifecycle BaseTest (Direkomendasikan)
public class AdminWorkflowTest extends ApplicationBaseTest {
    @Override
    protected String getRole() {
        return "ADMIN"; // Dialokasikan otomatis saat setUp & dirilis saat tearDown
    }

    @Test
    public void testAdminFeature() {
        UserAccount admin = AccountAllocator.getCurrentAccount();
        // Lakukan pengujian...
    }
}
```

---

### 4. Penanganan Komponen UI Modern
Untuk aplikasi yang dibangun menggunakan React, Vue, Angular, atau komponen modern (Shadcn, Radix UI, Headless UI):

```java
import com.automation.framework.core.page.DropdownComponent;
import com.automation.framework.core.table.WebTable;

// Mengisi Dropdown Virtual List Radix UI / Shadcn
DropdownComponent dropdown = new DropdownComponent(driver, By.id("organization-select-trigger"));
dropdown.searchAndSelect("Finance", "Finance Division Jakarta");

// Membaca Data Tabel HTML Dinamis
WebTable table = new WebTable(driver, By.id("users-table"));
int rowIndex = table.findRowIndexByCellValue("Email", "john.doe@example.com");
table.getCellElement(rowIndex, "Action").findElement(By.cssSelector(".btn-edit")).click();
```

---

## 🔄 Integrasi CI/CD

Framework ini dilengkapi konfigurasi pipeline siap pakai untuk **GitHub Actions** dan **Jenkins**.

### GitHub Actions
File workflow otomatis telah tersedia di [`.github/workflows/ci.yml`](.github/workflows/ci.yml).
Pipeline otomatis menginstal Google Chrome, menjalankan kompilasi multi-module, memvalidasi ArchUnit architecture guard, dan menjalankan pengujian headless setiap kali terjadi `push` atau `pull_request`.

---

## ❓ Troubleshooting & FAQ

### 1. `IllegalStateException: chromedriver must be executable`
* **Penyebab**: Perizinan eksekusi file driver pada OS lokal atau file cache driver korup.
* **Solusi**: Selenium 4 mengelola driver secara otomatis via *Selenium Manager*. Hapus direktori cache sementara di `~/.cache/selenium` lalu jalankan kembali `mvn test`.

### 2. `DevMode Connection Refused (port 9222)`
* **Penyebab**: Browser Chrome belum dijalankan dengan parameter `--remote-debugging-port=9222`.
* **Solusi**: Tutup seluruh instance Chrome, lalu buka melalui command prompt:
  ```bash
  chrome.exe --remote-debugging-port=9222 --user-data-dir="C:\chrome-dev-profile"
  ```

### 3. `ElementClickInterceptedException`
* **Solusi**: Metode `click()` di `BasePage` sudah memiliki fallback JavaScript otomatis. Jika elemen tertutup modal atau animasi memudar, framework akan mencoba klik reguler lalu mengeksekusi klik JS tanpa membuat pengujian gagal secara prematur.

---

## 📚 Dokumentasi Lengkap

Pelajari panduan spesifik lainnya di dalam direktori [`docs/framework/`](docs/framework/):

| Dokumen | Topik Pembahasan |
|---------|------------------|
| [📖 Getting Started](docs/framework/getting-started.md) | Panduan langkah awal untuk developer baru. |
| [🧩 Component Reference](docs/framework/component-reference.md) | Rincian lengkap seluruh kelas dan interface dalam framework-core. |
| [📄 Page Object Guide](docs/framework/page-object-pattern.md) | Pola desain Page Object Model, BasePage, dan WebTable. |
| [📊 Test Data Management](docs/framework/test-data-management.md) | Teknik data-driven testing dengan JSON dan Excel. |
| [⚡ Developer Mode Guide](docs/framework/developer-mode.md) | Cara kerja remote debugging port 9222 dan efisiensi waktu. |
| [📈 Reporting & CDP Guide](docs/framework/reporting-and-network-monitoring.md) | Arsitektur pelaporan 3-tab dan penangkapan network log. |
| [👥 Multi-Account & Parallel](docs/framework/multi-account-and-parallel-testing.md) | Alokasi akun multi-thread dan eksekusi paralel. |
| [🔄 CI/CD Pipeline Guide](docs/framework/ci-cd-pipeline.md) | Konfigurasi pipeline GitHub Actions dan Jenkins. |
| [🔀 Migration Guide](docs/framework/migration-guide.md) | Panduan langkah migrasi dari proyek Selenium monolitik lama. |

---

## 📄 Lisensi

Proyek ini dilisensikan di bawah lisensi terbuka **Apache License 2.0**.
Lihat berkas [LICENSE](LICENSE) untuk informasi hak cipta dan ketentuan penggunaan secara lengkap.
