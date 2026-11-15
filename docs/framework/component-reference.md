# Framework Component Reference Manual

Dokumentasi teknis mendalam mengenai setiap modul, komponen, kelas, dan antarmuka di dalam `framework-core`.

---

## 1. Daftar Komponen & Paket

```
com.automation.framework.core/
├── account/         # Manajemen Akun & Pooling Multi-User
├── assertion/       # Assertion Kustom & Soft Assert dengan Tangkapan Layar Otomatis
├── auth/            # Kontrak Otentikasi & Login Pluggable
├── config/          # Resolusi Konfigurasi Berjenjang (Cascading Hierarchy)
├── data/            # Abstraksi Pembaca Data Uji (JSON, Excel, Properties)
├── driver/          # Siklus Hidup WebDriver & Developer Mode (DevMode)
├── listener/        # Listener TestNG & Pembersih Proses Orphaned
├── network/         # Monitor Jaringan CDP (DevTools Protocol) untuk Error HTTP 4xx/5xx
├── page/            # BasePage, Komponen UI Kustom (Dropdown, Radix UI)
├── report/          # Bus Pelaporan, ExtentReports Renderer 3-Tab
├── table/           # Utilitas Parsing Tabel HTML Dinamis
├── test/            # Kelas Dasar Pengujian (BaseTest) Agnostik
└── utils/           # Helper Selenium & Pengambilan Screenshot
```

---

## 2. Rincian Komponen & Fungsinya

### 2.1. Config Layer (`com.automation.framework.core.config`)

* **Kelas Utama**: `FrameworkConfig`
* **Fungsi**:
  Menyediakan resolusi nilai konfigurasi terpusat dengan sistem cascading priority:
  $$\text{System Properties } (-D) \longrightarrow \text{Environment Variables} \longrightarrow \text{.env} \longrightarrow \text{application-\{env\}.properties} \longrightarrow \text{framework.properties}$$
* **Metode Penting**:
  * `FrameworkConfig.get(String key, String defaultValue)`
  * `FrameworkConfig.getBoolean(String key, boolean defaultValue)`
  * `FrameworkConfig.getInt(String key, int defaultValue)`
  * `FrameworkConfig.getBrowser()` — Mengambil target browser (default: `chrome`).
  * `FrameworkConfig.isHeadless()` — Flag eksekusi headless (default: `false`).
  * `FrameworkConfig.isDevMode()` — Flag developer mode (default: `false`).
  * `FrameworkConfig.getDevPort()` — Port remote debugging (default: `9222`).

---

### 2.2. Driver Management (`com.automation.framework.core.driver`)

* **Kelas Utama**:
  * `DriverManager`: Menyimpan instance `WebDriver` dalam konteks `ThreadLocal<WebDriver>` untuk memastikan thread safety saat eksekusi paralel.
  * `DriverFactory`: Membuat instance `WebDriver` baru (mendukung Chrome, Firefox, Edge) dengan opsi profil terisolasi, flag anti-automation bypass, dan penanganan timeout.
  * `DevModeUtils`: Menghubungkan Selenium ke browser Chrome yang sudah aktif (`--remote-debugging-port=9222`), memungkinkan pengujian instan tanpa harus mengulang login.
* **Metode Penting**:
  * `DriverManager.getDriver()`: Mengambil driver milik thread saat ini.
  * `DriverManager.setDriver(WebDriver driver)`: Mendaftarkan driver ke thread.
  * `DriverManager.quitDriver()`: Menutup browser dan membersihkan ThreadLocal.
  * `DevModeUtils.isPortAvailable(int port)`: Memvalidasi ketersediaan port debugging.

---

### 2.3. Page Object & UI Component Engine (`com.automation.framework.core.page`)

* **Kelas Utama**:
  * `BasePage`: Kelas dasar abstrak untuk semua Page Object. Menyediakan pembungkus (wrapper) aman untuk interaksi elemen dengan explicit wait bawaan (`Duration.ofSeconds(15)`), auto-scroll via JavaScript, penanganan intersep klik otomatis (JS click fallback), dan pelaporan diagnostik.
  * `DropdownComponent`: Mengelola dropdown non-standar (misalnya komponen Radix UI, Headless UI, Material, atau listbox virtual) yang menggunakan `role="combobox"` atau `role="option"`.
* **Metode Penting di BasePage**:
  * `click(By locator)` / `click(WebElement element)`: Menunggu elemen clickable, mencoba klik native, dan beralih ke JS click jika terhalang overlay.
  * `typeText(By locator, String text)` / `typeText(WebElement element, String text)`: Menunggu visibility, membersihkan input, dan mengetikkan teks. (Tersedia juga alias `type()`).
  * `getText(By locator)` / `getText(WebElement element)`: Menunggu visibility dan mengembalikan teks elemen.
  * `isVisible(By locator)` / `isVisible(WebElement element)`: Mengembalikan boolean tanpa melempar TimeoutException. (Tersedia juga alias `isDisplayed()`).
  * `isNotVisible(By locator)` / `isNotVisible(WebElement element)`: Memvalidasi elemen tersembunyi atau hilang dari DOM.
  * `isElementClickable(By locator)` / `isElementClickable(WebElement element)`: Mengecek status clickable elemen.
  * `scrollMoveToElement(By locator)` / `scrollMoveToElement(WebElement element)`: Melakukan scroll halaman menuju elemen.

---

### 2.4. Pluggable Authentication (`com.automation.framework.core.auth`)

* **Antarmuka**: `LoginHandler`
* **Implementasi Bawaan**: `NoOpLoginHandler`
* **Fungsi**:
  Memisahkan logika autentikasi aplikasi dari siklus hidup framework. Setiap proyek dapat mengimplementasikan cara login yang unik (form login, SSO, token inject, API login) tanpa mengubah kode framework.
* **Metode Kontrak**:
  * `void login(WebDriver driver, UserAccount account)`
  * `boolean isLoggedIn(WebDriver driver)`
  * `void logout(WebDriver driver)`

---

### 2.5. Account Pool & Multi-User Allocation (`com.automation.framework.core.account`)

* **Antarmuka & Kelas**:
  * `UserAccount`: Kontrak data akun pengguna (`getUsername()`, `getPassword()`, `getRole()`, `getIdentifier()`).
  * `AccountAllocator`: Pengelola antrean akun thread-safe yang mendukung peminjaman akun berbasis peran (role), pencegahan tabrakan multi-thread saat uji paralel, dan timeout pengembalian.
* **Metode Penting**:
  * `AccountAllocator.init(List<UserAccount> accounts)`
  * `AccountAllocator.allocate(String role)`
  * `AccountAllocator.release(UserAccount account)`
  * `AccountAllocator.resetPool()`

---

### 2.6. Reporting Subsystem & CDP Network Monitor (`com.automation.framework.core.report` & `network`)

* **Arsitektur**:
  * `ReportAPI`: API fasad publik untuk pencatatan log langkah pengujian.
  * `ReportEventBus`: Event bus thread-safe yang mendistribusikan log ke engine pelaporan.
  * `ExtentImplementation` & `ExtentTabRenderer`: Membangun laporan interaktif HTML 3-Tab:
    1. **Tab 1: DETAILS**: Kronologi langkah pengujian terperinci dengan timestamp.
    2. **Tab 2: ELEMENT LOGS**: Diagnostik kegagalan 6-pilar (Computed CSS, Rect Box, Blocker Elements, OuterHTML, Stacktrace ringkas).
    3. **Tab 3: NETWORK LOGS**: Daftar kegagalan API/HTTP (4xx, 5xx) yang ditangkap langsung via Chrome DevTools Protocol (CDP).
  * `NetworkMonitor`: Menggunakan CDP (`Network.enable`, `Network.responseReceived`) via reflection (`CdpVersionHelper`) untuk menangkap kegagalan backend secara otomatis tanpa konfigurasi proxy eksternal.

---

### 2.7. Custom Assertions (`com.automation.framework.core.assertion`)

* **Kelas**: `CustomAssert` & `CustomSoftAssert`
* **Fungsi**:
  Memperluas fungsi assertion TestNG standar dengan integrasi otomatis ke `ReportAPI` dan penangkapan tangkapan layar (screenshot) saat terjadi kegagalan verifikasi.

---

### 2.8. Data Readers (`com.automation.framework.core.data`)

* **Antarmuka**: `DataReader<T>`
* **Implementasi**:
  * `JsonDataReader`: Membaca array atau objek JSON menjadi POJO / Java model menggunakan Gson.
  * `ExcelDataReader`: Membaca file `.xlsx` menjadi `List<Map<String, String>>` menggunakan Apache POI.
  * `ResourcePathResolver`: Menyelesaikan path file secara fleksibel (classpath, direktori proyek, atau filesystem mutlak).

---

### 2.9. Lifecycle Listeners & Process Cleanup (`com.automation.framework.core.listener`)

* **Kelas**:
  * `FrameworkListener`: Listener global TestNG yang menangani inisialisasi report, injeksi CSS/JS tab ExtentReports, dan pengumpulan ringkasan eksekusi.
  * `BrowserProcessCleaner`: Pembersih otomatis proses peramban (`chromedriver.exe`, `chrome.exe`) yang tertinggal (orphaned) dan pembersihan direktori profil sementara saat eksekusi selesai.

---

### 2.10. Generic Test Base (`com.automation.framework.core.test.BaseTest`)

* **Fungsi**:
  Kelas dasar pengujian TestNG abstrak yang mengatur siklus hidup driver, inisialisasi monitor CDP, alokasi akun otomatis (opsional), penangkapan screenshot saat test gagal, dan penutupan browser secara bersih di `@BeforeMethod` dan `@AfterMethod`.
