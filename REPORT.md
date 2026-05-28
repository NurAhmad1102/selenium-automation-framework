# Dokumentasi Sistem Reporting (ExtentReports)

> Panduan ini menjelaskan arsitektur, flow, dan struktur class sistem reporting berbasis ExtentReports agar mudah dipahami saat ingin mengubah style, format, atau behavior laporan.

---

## Daftar Isi

1. [Gambaran Umum](#1-gambaran-umum)
2. [Flow Lengkap Report Generation](#2-flow-lengkap-report-generation)
3. [Struktur Package & File](#3-struktur-package--file)
4. [Detail Tiap Class](#4-detail-tiap-class)
5. [Konfigurasi Log4j2 Bridge](#5-konfigurasi-log4j2-bridge)
6. [Struktur HTML Report & Styling](#6-struktur-html-report--styling)
7. [Panduan Modifikasi](#7-panduan-modifikasi)

---

## 1. Gambaran Umum

Sistem reporting menggunakan **event-driven architecture** dengan ExtentReports sebagai output HTML. Terdapat 3 lapisan utama:

```
[TEST / BASE TEST]
      |  memanggil
      v
[ReportAPI]          <- satu-satunya pintu masuk untuk logging
      |  menyimpan ke ThreadLocal + broadcast ke
      v
[ReportEventBus]     <- dispatcher ke semua reporter yang terdaftar
      |
      v
[ExtentImplementation]  <- implementasi ExtentReports
      |  saat flush, memanggil
      v
[ExtentTabRenderer]  <- menghasilkan HTML tab (DETAILS / ELEMENT LOGS / NETWORK LOGS)
      |  HTML diinjeksikan ke dalam
      v
[ExtentReports (Spark Reporter)]  <- output: reports/index.html
```

Selain itu, **Log4j2** memiliki custom appender (`ExtentReportAppender`) yang secara otomatis menjembatani semua log `logger.info/warn/error()` ke `ReportAPI` tanpa perlu kode tambahan di Page Object.

---

## 2. Flow Lengkap Report Generation

### Urutan Eksekusi

```
1. TestNG Suite dimulai
   └─ Listeners.onStart()
         └─ ExtentImplementation.init()
               └─ ReportEventBus.register(new ExtentImplementation())
                     └─ ExtentReportNg.getReportObject()  [singleton, lazy init]
                           └─ ExtentSparkReporter dibuat → reports/index.html
                           └─ CSS & JS diinjeksikan via ExtentUIInjector

2. Setiap test method dimulai
   └─ Listeners.onTestStart()
         └─ ThreadContext.put("testMethod", methodName)   [untuk Log4j2 routing]
         └─ ReportAPI.startTest(methodName, description)
               └─ ThreadLocalStore.get().clear()          [reset data test sebelumnya]
               └─ ReportEventBus.startTest()
                     └─ ExtentImplementation.startTest()
                           └─ extent.createTest(formattedName)  [buat node di Extent]
                           └─ test.set(extentTest)              [simpan di ThreadLocal]

3. Selama test berjalan
   ├─ ReportAPI.step/pass/fail/network/addScreenshot()
   │     └─ LogEntry dibuat → ThreadLocalStore.get().addLog(entry)
   │     └─ ReportEventBus.postLog(entry)
   │           └─ ExtentImplementation.onLogAdded(entry)  [opsional, real-time]
   │
   └─ logger.info/warn/error() di Page Object / Utils
         └─ Log4j2 → ExtentReportAppender.append()
               └─ cek ThreadContext "testMethod" (routing ke test yang benar)
               └─ ReportAPI.step/fail() dipanggil otomatis

4. Test selesai (sukses / gagal / skip)
   └─ BaseTest.tearDown() → finalizeReport()
   │     └─ ReportAPI.pass/fail/step()
   │     └─ ReportAPI.flush()
   │           └─ ReportEventBus.flush(data)
   │                 └─ ExtentImplementation.flush(data)
   │                       └─ ExtentTabRenderer.render(data)  [generate HTML tabs]
   │                       └─ currentTest.info(renderedHtml)  [inject ke Extent node]
   │                       └─ currentTest.pass/fail()         [set status akhir]
   │                       └─ test.remove()                   [cleanup ThreadLocal]
   │
   └─ (jika tidak pakai BaseTest) Listeners.onTestSuccess/Failure/Skipped()
         └─ alur sama seperti di atas

5. Semua test selesai
   └─ Listeners.onFinish()
         └─ ExtentReportNg.getReportObject().flush()  [tulis file HTML final]
```

---

## 3. Struktur Package & File

```
src/
├── main/java/org/example/
│   └── utils/
│       ├── ExtentReportNg.java                          [singleton factory ExtentReports]
│       └── report/
│           ├── core/
│           │   ├── IAutomationReporter.java             [interface reporter]
│           │   ├── ReportAPI.java                       [public API untuk logging]
│           │   └── ReportEventBus.java                  [event dispatcher]
│           ├── implementations/
│           │   └── extent/
│           │       ├── ExtentImplementation.java        [implementasi ExtentReports]
│           │       ├── ExtentTabRenderer.java           [render HTML tabs]
│           │       └── ExtentUIInjector.java            [CSS & JS kustom]
│           └── internal/
│               ├── model/
│               │   ├── LogCategory.java                 [enum kategori log]
│               │   ├── LogEntry.java                    [model satu log entry]
│               │   └── ReportData.java                  [container log per test]
│               └── storage/
│                   └── ThreadLocalStore.java            [thread-safe storage]
│
└── test/java/org/example/
    ├── base/
    │   └── BaseTest.java                                [setup/teardown + finalize report]
    ├── listener/
    │   └── Listeners.java                               [TestNG listener]
    └── utils/
        ├── extentReportUtils/
        │   └── ExtentReportAppender.java                [Log4j2 → ReportAPI bridge]
        └── serviceUtils/
            ├── NetworkMonitor.java                      [CDP network monitoring → ReportAPI]
            └── CdpVersionHelper.java                    [reflection engine untuk versi CDP mandiri]
```

---

## 4. Detail Tiap Class

### `ExtentReportNg`
**Lokasi:** `src/main/java/org/example/utils/ExtentReportNg.java`  
**Peran:** Singleton factory untuk inisialisasi `ExtentReports` dan konfigurasi `ExtentSparkReporter`.

| Method | Deskripsi |
|--------|-----------|
| `getReportObject()` | Double-checked locking singleton. Membuat `ExtentSparkReporter` ke `reports/index.html`, menginjeksikan CSS/JS dari `ExtentUIInjector`, lalu mengembalikan instance `ExtentReports`. |
| `formatterTestName(testName)` | Mengubah camelCase method name menjadi Title Case dengan spasi. Contoh: `saveSlaSettingAsDraft` → `Save Sla Setting As Draft`. |

**Konfigurasi yang bisa diubah di sini:**
- Path output report → ubah variabel `path`
- Nama report → `reporter.config().setReportName(...)`
- Judul dokumen → `reporter.config().setDocumentTitle(...)`
- System info (tester name, dll) → `extent.setSystemInfo(...)`
- Offline mode → `reporter.config().enableOfflineMode(true/false)`

---

### `IAutomationReporter`
**Lokasi:** `src/main/java/org/example/utils/report/core/IAutomationReporter.java`  
**Peran:** Interface kontrak untuk semua implementasi reporter.

| Method | Deskripsi |
|--------|-----------|
| `startTest(testName, description)` | Dipanggil saat test baru dimulai. |
| `onLogAdded(LogEntry entry)` | Dipanggil real-time setiap ada log baru. |
| `flush(ReportData data)` | Dipanggil saat test selesai untuk finalisasi laporan. |

---

### `ReportAPI`
**Lokasi:** `src/main/java/org/example/utils/report/core/ReportAPI.java`  
**Peran:** Satu-satunya pintu masuk publik untuk semua aktivitas logging dari test maupun framework.

| Method | Deskripsi |
|--------|-----------|
| `startTest(testName, description)` | Reset `ThreadLocalStore`, set active, broadcast `startTest` ke event bus. |
| `step(message)` | Log INFO ke kategori `DETAILS`. |
| `pass(message)` | Log PASS ke kategori `DETAILS`. |
| `fail(message)` | Log FAIL ke kategori `DETAILS`. |
| `fail(Throwable error)` | Log FAIL dengan stack trace. |
| `fail(message, Throwable error)` | Log FAIL dengan pesan dan stack trace. |
| `elementError(action, locator, error)` | Log ke kategori `ELEMENT_LOGS` dengan detail action, locator, dan error. |
| `network(method, url, statusCode, request, response)` | Log ke kategori `NETWORK_LOGS`. |
| `network(name, method, url, statusCode, statusText, mimeType, occurrenceCount, request, response, timestamp)` | Overload lengkap untuk network log. |
| `addScreenshot(path)` | Tambahkan screenshot ke `ELEMENT_LOGS`. |
| `flush()` | Kirim semua log ke event bus untuk finalisasi, lalu clear `ThreadLocalStore`. |
| `isTestActive()` | Cek apakah test sedang aktif (untuk routing di `ExtentReportAppender`). |

---

### `ReportEventBus`
**Lokasi:** `src/main/java/org/example/utils/report/core/ReportEventBus.java`  
**Peran:** Dispatcher yang meneruskan event ke semua reporter yang terdaftar. Menggunakan `CopyOnWriteArrayList` untuk thread safety.

| Method | Deskripsi |
|--------|-----------|
| `register(reporter)` | Daftarkan implementasi reporter baru. |
| `startTest(testName, description)` | Broadcast ke semua reporter. |
| `postLog(entry)` | Broadcast log entry baru ke semua reporter. |
| `flush(data)` | Broadcast flush ke semua reporter dengan data lengkap test. |

---

### `ExtentImplementation`
**Lokasi:** `src/main/java/org/example/utils/report/implementations/extent/ExtentImplementation.java`  
**Peran:** Implementasi `IAutomationReporter` yang menghubungkan event ke ExtentReports.

| Method | Deskripsi |
|--------|-----------|
| `init()` | Registrasi singleton ke `ReportEventBus`. Menggunakan `AtomicBoolean` agar hanya sekali. |
| `getCurrentTest()` | Ambil `ExtentTest` milik thread saat ini. |
| `startTest(testName, description)` | Buat node test baru di Extent via `extent.createTest()`, simpan di `ThreadLocal<ExtentTest>`. |
| `onLogAdded(entry)` | Hook real-time (saat ini hanya untuk FAIL, bisa dikembangkan). |
| `flush(data)` | Panggil `ExtentTabRenderer.render(data)`, inject HTML ke test node, attach screenshot, set status PASS/FAIL, lalu `test.remove()`. |

---

### `ExtentTabRenderer`
**Lokasi:** `src/main/java/org/example/utils/report/implementations/extent/ExtentTabRenderer.java`  
**Peran:** Menghasilkan HTML string berisi 3 tab (DETAILS, ELEMENT LOGS, NETWORK LOGS) dari `ReportData`.

| Method | Deskripsi |
|--------|-----------|
| `render(data)` | Entry point. Sort log by timestamp, generate unique ID untuk tab, buat wrapper HTML dengan 3 tab. |
| `renderDetailsTable(logs)` | Tabel untuk log kategori `DETAILS`. Kolom: STATUS badge, TIMESTAMP, STEP DESCRIPTION + stack trace. |
| `renderElementsTable(logs)` | Tabel untuk log kategori `ELEMENT_LOGS`. Kolom: STATUS, TIMESTAMP, DETAILS (action, potential cause, locator, error, screenshot). |
| `renderNetworkLogs(logs)` | Tabel untuk log kategori `NETWORK_LOGS`. Filter: hanya status 4xx–5xx, hanya sebelum first failure. Menampilkan method, URL, status, request/response body (JSON pretty-printed). |
| `statusClass(status)` | Mapping `Status` → CSS class: `badge-fail`, `badge-pass`, `badge-info`. |
| `escapeHtml(value)` | Escape karakter HTML untuk keamanan output. |
| `isFailedNetworkLog(log)` | Filter: status code 400–599. |
| `resolveStatusCode(log)` | Ambil status code dari `statusCode` atau fallback ke `retryCount`. |
| `resolveNetworkName(log)` | Ambil nama API dari field `name` atau parse dari `message`. |
| `stackTraceToString(error)` | Konversi `Throwable` ke string multi-baris. |

---

### `ExtentUIInjector`
**Lokasi:** `src/main/java/org/example/utils/report/implementations/extent/ExtentUIInjector.java`  
**Peran:** Menyediakan CSS dan JavaScript kustom yang diinjeksikan ke `ExtentSparkReporter`.

| Method | Deskripsi |
|--------|-----------|
| `getCustomCss()` | Mengembalikan string CSS untuk styling tab, tabel, badge, code block, dan override tampilan Extent default. |
| `getCustomJs()` | Mengembalikan fungsi `switchTab()` untuk navigasi antar tab. |

> **Ini adalah file utama yang diubah jika ingin mengubah tampilan/style report.**

---

### `LogEntry`
**Lokasi:** `src/main/java/org/example/utils/report/internal/model/LogEntry.java`  
**Peran:** Model data untuk satu entri log.

| Field | Tipe | Deskripsi |
|-------|------|-----------|
| `timestamp` | `Instant` | Waktu log dibuat (auto). |
| `category` | `LogCategory` | DETAILS / ELEMENT_LOGS / NETWORK_LOGS. |
| `status` | `Status` | ExtentReports Status: PASS, FAIL, INFO. |
| `message` | `String` | Pesan utama log. |
| `locator` | `String` | CSS/XPath selector elemen (untuk element error). |
| `action` | `String` | Aksi yang dilakukan (click, type, dll) atau HTTP method. |
| `error` | `Throwable` | Exception yang terjadi. |
| `request` | `String` | Request body API. |
| `response` | `String` | Response body API. |
| `screenshotPath` | `String` | Path file screenshot. |
| `statusCode` | `int` | HTTP status code. |
| `occurrenceCount` | `int` | Berapa kali API dipanggil. |
| `statusText` | `String` | HTTP status text (misal "Not Found"). |
| `mimeType` | `String` | MIME type response. |
| `threadId` | `long` | ID thread (auto, untuk debugging parallel). |

---

### `LogCategory`
**Lokasi:** `src/main/java/org/example/utils/report/internal/model/LogCategory.java`  
**Peran:** Enum untuk mengelompokkan log ke tab yang sesuai.

| Nilai | Tab di Report |
|-------|---------------|
| `DETAILS` | Tab DETAILS |
| `ELEMENT_LOGS` | Tab ELEMENT LOGS |
| `NETWORK_LOGS` | Tab NETWORK LOGS |

---

### `ReportData`
**Lokasi:** `src/main/java/org/example/utils/report/internal/model/ReportData.java`  
**Peran:** Container yang menyimpan semua `LogEntry` milik satu test execution.

| Method | Deskripsi |
|--------|-----------|
| `addLog(entry)` | Tambah log entry. |
| `getLogs()` | Kembalikan copy list log. |
| `isActive()` / `setActive()` | Flag apakah test sedang berjalan. |
| `clear()` | Reset semua log dan flag active. |

---

### `ThreadLocalStore`
**Lokasi:** `src/main/java/org/example/utils/report/internal/storage/ThreadLocalStore.java`  
**Peran:** Menyimpan `ReportData` per thread agar aman untuk eksekusi paralel.

| Method | Deskripsi |
|--------|-----------|
| `get()` | Ambil `ReportData` milik thread saat ini (auto-init jika belum ada). |
| `clear()` | Clear data dan remove dari ThreadLocal (penting untuk mencegah memory leak). |

---

### `ExtentReportAppender`
**Lokasi:** `src/test/java/org/example/utils/extentReportUtils/ExtentReportAppender.java`  
**Peran:** Custom Log4j2 appender yang menjembatani semua log `logger.*()` ke `ReportAPI` secara otomatis.

| Method | Deskripsi |
|--------|-----------|
| `createAppender(...)` | Factory method (dipanggil oleh Log4j2 saat parsing `log4j2.xml`). |
| `append(event)` | Dipanggil setiap ada log event. Cek `testMethod` di ThreadContext untuk routing, lalu panggil `ReportAPI.fail/step()` sesuai level. |

**Routing logic:**
- `ERROR` → `ReportAPI.fail(message, throwable)`
- `WARN` → `ReportAPI.step("WARN: " + message)`
- `INFO` → `ReportAPI.step(message)`
- Jika `ReportAPI.isTestActive()` false → log diabaikan
- Jika `testMethod` di event berbeda dengan thread aktif → log diabaikan (parallel safety)

---

### `Listeners`
**Lokasi:** `src/test/java/org/example/listener/Listeners.java`  
**Peran:** TestNG `ITestListener` yang mengelola lifecycle report per test.

| Method | Deskripsi |
|--------|-----------|
| `onStart(context)` | Inisialisasi `ExtentImplementation.init()`. |
| `onTestStart(result)` | Set `ThreadContext`, panggil `ReportAPI.startTest()`. |
| `onTestSuccess(result)` | Jika tidak dikelola `BaseTest`: log pass + flush. |
| `onTestFailure(result)` | Jika tidak dikelola `BaseTest`: log fail + screenshot + flush. |
| `onTestSkipped(result)` | Jika tidak dikelola `BaseTest`: log skip + flush. |
| `onFinish(context)` | Panggil `ExtentReportNg.getReportObject().flush()` untuk finalisasi file HTML. |

---

### `BaseTest`
**Lokasi:** `src/test/java/org/example/base/BaseTest.java`  
**Peran:** Base class untuk semua test yang menggunakan WebDriver. Mengelola lifecycle report secara mandiri (override dari `Listeners`).

| Method | Deskripsi |
|--------|-----------|
| `setUp(result)` | Set atribut `reportManagedByBaseTest = true`, panggil `ReportAPI.step()` untuk setup info. |
| `tearDown(result)` | Flush network errors jika test gagal, panggil `finalizeReport()`, quit driver. |
| `finalizeReport(result)` | Cek status test → panggil `ReportAPI.pass/fail/step()` → `ReportAPI.flush()`. Set atribut `reportFinalized = true` untuk mencegah double flush. |
| `attachFailureScreenshot(result)` | Ambil screenshot via `SeleniumUtils.getScreenShotPath()`, kirim ke `ReportAPI.addScreenshot()`. |

---

## 5. Konfigurasi Log4j2 Bridge

**File:** `src/test/resources/log4j2.xml`

```xml
<!-- Package harus didaftarkan agar Log4j2 menemukan plugin ExtentReportAppender -->
<Configuration status="WARN" packages="org.example.utils">
    <Appenders>
        <!-- Custom appender yang menjembatani log ke ExtentReports -->
        <ExtentReportAppender name="extent">
            <PatternLayout pattern="%msg%n" />
        </ExtentReportAppender>
    </Appenders>

    <!-- Semua logger org.example menggunakan appender ini -->
    <Logger name="org.example" level="DEBUG" additivity="false">
        <AppenderRef ref="console" />
        <AppenderRef ref="rollingFile" />
        <AppenderRef ref="extent" />
    </Logger>
</Configuration>
```

Appender `ExtentReportAppender` didaftarkan dengan anotasi `@Plugin(name = "ExtentReportAppender", category = "Core")` di class `ExtentReportAppender`. Package `org.example.utils` **wajib** terdaftar di atribut `packages` pada tag `<Configuration>` agar Log4j2 menemukan plugin ini saat startup.

> **Catatan:** Jika `packages` tidak didaftarkan, appender tidak akan ditemukan dan semua log tidak akan masuk ke report tanpa error yang jelas.

---

## 6. Struktur HTML Report & Styling

### Output File
- **Path:** `reports/index.html`
- **Mode:** Offline (semua asset di-embed)

### Struktur HTML yang Dihasilkan per Test Node

```html
<div class='test-report-wrapper'>
  <!-- Tab Navigation -->
  <div class='tab-container'>
    <div class='tab-btn active' onclick="switchTab(...)">DETAILS</div>
    <div class='tab-btn' onclick="switchTab(...)">ELEMENT LOGS</div>
    <div class='tab-btn' onclick="switchTab(...)">NETWORK LOGS</div>
  </div>

  <!-- Tab: DETAILS -->
  <div id='details-{uid}' class='tab-panel active'>
    <table class='report-table details-table'>
      <thead><tr><th>STATUS</th><th>TIMESTAMP</th><th>STEP DESCRIPTION</th></tr></thead>
      <tbody>
        <tr>
          <td><span class='badge-pass'>PASS</span></td>
          <td>10:30:00 AM</td>
          <td>Setting up test environment...</td>
        </tr>
      </tbody>
    </table>
  </div>

  <!-- Tab: ELEMENT LOGS -->
  <div id='elements-{uid}' class='tab-panel'>
    <table class='report-table'>
      <!-- Action, Potential Cause, Element locator, Error, Screenshot -->
    </table>
  </div>

  <!-- Tab: NETWORK LOGS -->
  <div id='network-{uid}' class='tab-panel'>
    <table class='report-table'>
      <!-- API Name, Method, URL, Status, Request/Response body -->
    </table>
  </div>
</div>
```

### CSS Classes & Warna

| Class | Warna | Kegunaan |
|-------|-------|----------|
| `.badge-pass` | `#00af66` (hijau) | Status PASS |
| `.badge-fail` | `#e74c3c` (merah) | Status FAIL |
| `.badge-info` | `#00bcd4` (cyan) | Status INFO |
| `.tab-btn.active` | border `#e74c3c` | Tab aktif |
| `.code-block` | bg `#282c34`, text `#abb2bf` | Stack trace / code |
| `.network-detail-label` | `#555` bold | Label di network log |
| `.report-table th` | bg `#fafafa` | Header tabel |

---

## 7. Panduan Modifikasi

### Ubah Warna / Style Tab dan Badge
Edit method `getCustomCss()` di `ExtentUIInjector.java`.

```java
// Contoh: ubah warna tab aktif dari merah ke biru
.tab-btn.active { color: #2196F3; border-bottom: 3px solid #2196F3; }

// Contoh: ubah warna badge PASS
.badge-pass { background: #4CAF50; }
```

### Ubah Kolom atau Format Tabel
Edit method `renderDetailsTable()`, `renderElementsTable()`, atau `renderNetworkLogs()` di `ExtentTabRenderer.java`.

### Tambah Tab Baru
1. Tambah nilai baru di enum `LogCategory.java`
2. Tambah tab button di method `render()` di `ExtentTabRenderer.java`
3. Tambah method render baru (misal `renderCustomTab()`)
4. Tambah method log baru di `ReportAPI.java` dengan kategori baru

### Ubah Nama / Path Output Report
Edit method `getReportObject()` di `ExtentReportNg.java`:
```java
String path = System.getProperty("user.dir") + File.separator + "reports" + File.separator + "index.html";
```

### Ubah Format Nama Test di Report
Edit method `formatterTestName()` di `ExtentReportNg.java`.

### Ubah Format Timestamp di Tabel
Edit konstanta `TIME_FORMATTER` di `ExtentTabRenderer.java`:
```java
// Default: "10:30:00 AM"
private static final DateTimeFormatter TIME_FORMATTER =
    DateTimeFormatter.ofPattern("h:mm:ss a").withZone(ZoneId.systemDefault());

// Contoh ubah ke format 24 jam dengan tanggal:
DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.systemDefault());
```

### Ubah Filter Network Log
Secara default, hanya log dengan status 4xx–5xx yang ditampilkan, dan hanya yang terjadi sebelum first failure. Edit method `isFailedNetworkLog()` dan filter di `render()` di `ExtentTabRenderer.java`.

### Tambah System Info di Report
Edit `getReportObject()` di `ExtentReportNg.java`:
```java
extent.setSystemInfo("Environment", "Staging");
extent.setSystemInfo("Browser", "Chrome");
```
