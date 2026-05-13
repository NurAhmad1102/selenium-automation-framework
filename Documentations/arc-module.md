Tentu, ini adalah konten tersebut dalam format Markdown (`.md`) yang rapi agar mudah Anda simpan di dokumentasi proyek:

---

# Dokumentasi Arsitektur Framework: Implementasi Navigasi Menu

**Status:** Best Practice Recommendation

**File Terkait:**

* `ManageOrganizationFactory.java`
* `SideMenuFactory.java`
* `BackOfficeSideMenu.java`

---

## Ringkasan Konsep

Berdasarkan arsitektur yang sudah dibangun, menu **"My Organization"** adalah pengelompokan navigasi di UI, sedangkan fitur di dalamnya (seperti **"Manage Organization"**) adalah halaman kompleks yang berdiri sendiri.

Berikut adalah panduan implementasi agar *codebase* tetap bersih dan modular:

### 1. Struktur Module Factory Independen

Jangan membuat `MyOrganizationFactory` sebagai induk yang membungkus factory anak-anaknya. Hal ini mencegah terciptanya *God Object* atau hirarki yang terlalu dalam.

* **Prinsip:** Setiap **Sub-Menu** adalah **Module Factory** yang sejajar.
* **Implementasi:** `ManageOrganizationFactory` sudah benar sebagai entitas independen. Jika ada menu baru seperti "Organization Structure", buatlah `OrganizationStructureFactory` di level yang sama.

### 2. Sentralisasi Navigasi di `BackOfficeSideMenu`

Karena *Side Menu* bersifat global, pusatkan semua interaksi klik di `src/main/java/org/example/pageObject/backoffice/sideMenu/BackOfficeSideMenu.java`.

**Contoh Implementasi Code:**

```java
// Locators
private final By myOrganizationMenu = By.xpath("//div[contains(text(), 'My Organization')]");
private final By manageOrganizationSubMenu = By.xpath("//div[contains(text(), 'Manage Organization')]");
private final By organizationStructureSubMenu = By.xpath("//div[contains(text(), 'Organization Structure')]");

// Methods
public BackOfficeSideMenu expandMyOrganization() {
    // Tambahkan logic pengecekan, klik hanya jika belum di-expand
    click(myOrganizationMenu);
    return this; // Return 'this' agar bisa dilakukan method chaining
}

public void clickManageOrganization() {
    click(manageOrganizationSubMenu);
}

public void clickOrganizationStructure() {
    click(organizationStructureSubMenu);
}

```

> **Tips:** Gunakan *dynamic XPath* jika Anda ingin membuat method yang lebih fleksibel, misalnya: `public void clickSubMenu(String menuName)`.

### 3. Alur Integrasi pada Class Test

Pemisahan antara **Navigasi** dan **Logika Halaman** membuat test case menjadi lebih deskriptif dan mudah dibaca.

**Contoh pada `AddApprovalMatrix.java`:**

```java
@Test
public void testBukaManageOrganization() {
    // 1. Navigasi melalui Side Menu
    pages.sideMenu().getSideMenu()
         .expandMyOrganization()
         .clickManageOrganization();

    // 2. Interaksi dengan business logic di dalam modul
    // Panggil module factory yang spesifik untuk halaman tersebut
    pages.manageOrganization().getDashboardSlaSettingPageFactory()
         .klikSesuatuDiDashboard();
}

```

### 4. Best Practice untuk Menu Skala Besar (Dynamic Locators)

Jika aplikasi Anda memiliki **belasan Main Menu** dan **puluhan Sub Menu** seperti pada gambar (Benefits, Self Service, HyLearn, dll.), menggunakan `@FindBy` untuk setiap menu akan membuat *class* `SideMenu` sangat raksasa dan sulit dikelola (*bloated*). 

Solusi *best practice*-nya adalah menggunakan **Dynamic XPath**:

```java
// Di dalam SideMenu.java

// Method dinamis untuk ekspansi Main Menu
public void expandMainMenu(String menuName) {
    // Cari elemen berdasarkan teks (sesuaikan XPath dengan struktur HTML web Anda)
    By locator = By.xpath(String.format("//div[contains(@class, 'menu') and contains(normalize-space(), '%s')]", menuName));
    WebElement menuElement = driver.findElement(locator);
    click(menuElement);
}

// Method dinamis untuk klik Sub Menu
public void clickSubMenu(String subMenuName) {
    By locator = By.xpath(String.format("//div[contains(@class, 'sub-menu') and contains(normalize-space(), '%s')]", subMenuName));
    WebElement subMenuElement = driver.findElement(locator);
    click(subMenuElement);
}
```

**Cara pemanggilannya di Class Test:**
```java
pages.sideMenu().getSideMenu()
     .expandMainMenu("Benefits")
     .clickSubMenu("Medical Claim");

pages.sideMenu().getSideMenu()
     .expandMainMenu("My Organization")
     .clickSubMenu("Employee Transfer");
```
Dengan satu *method* dinamis, Anda sudah bisa men- *cover* 100+ kombinasi menu tanpa perlu menulis *locator* satu per satu!

---

## Kesimpulan (Summary)

| Komponen | Tanggung Jawab |
| --- | --- |
| **Navigasi (UI)** | Ditangani secara eksklusif oleh modul `sideMenu()`. |
| **Fitur (Business Logic)** | Ditangani oleh *Module Factory* independen (misal: `manageOrganization()`). |
| **Root (MainPagesFactory)** | Mengagregasi semua modul tanpa mengikuti hirarki visual UI. |

Dengan pendekatan ini, framework Anda mengikuti prinsip **Single Responsibility**, sehingga lebih mudah dirawat saat aplikasi bertambah besar.