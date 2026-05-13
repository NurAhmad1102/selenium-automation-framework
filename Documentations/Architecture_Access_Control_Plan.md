# Planning: Implementasi Access Control (Test Class vs Page Object)

## Kondisi Saat Ini (As-Is)
Anda **benar 100%**. Saat ini, alur yang ideal adalah:
`Test Class` -> `BaseTest` -> `MainPagesFactory` -> `ModuleFactory` -> `PageObject` (Interaksi UI).

Namun, karena struktur kode saat ini menggunakan **Layer-Based Packaging** (folder `factories` dan `pageObject` terpisah beda package), maka Constructor dari Page Object (misal `public DashboardManageOrganizationPage(WebDriver driver)`) terpaksa harus diset `public`. 

Karena berstatus `public`, celah ini memungkinkan seorang *engineer* di `Test Class` untuk melakukan '*bypass*' dan memanggil langsung:
```java
// BISA TERJADI (Bad Practice)
DashboardManageOrganizationPage page = new DashboardManageOrganizationPage(driver);
```

## Solusi Implementasi (To-Be)

Untuk mengunci akses agar *Test Class* **mutlak** harus melalui *Factory*, berikut adalah 2 opsi *planning* implementasi untuk Front Office maupun Back Office:

### OPSI 1: Menggunakan ArchUnit (Sangat Direkomendasikan - Tanpa Merusak Folder)
Daripada merombak seluruh struktur folder yang sudah rapi, kita bisa menanamkan "Penjaga Gerbang Arsitektur" menggunakan *library* bernama **ArchUnit**.

**Plan Eksekusi:**
1. Tambahkan dependency `archunit` di `pom.xml`.
2. Buat satu *test class* khusus, misal `ArchitectureTest.java`.
3. Tulis aturan (*rules*) baku yang menyatakan: *"Class di dalam package `org.example.test..` tidak boleh memanggil constructor dari class di dalam package `org.example.pageObject..`"*
4. **Hasil:** Jika ada *engineer* yang bandel mengetik `new LoginPage()` di class test, maka saat kode di- *compile* atau di- *run*, Test akan otomatis **FAILED** dan memberitahu bahwa hal tersebut melanggar arsitektur.

**Kelebihan:** Struktur folder `factories` dan `pageObject` tetap utuh. Sangat cepat diimplementasikan.

---

### OPSI 2: Pindah ke Feature-Based Packaging (Merubah Struktur)
Solusi ini memanfaatkan *Access Modifier* bawaan Java secara murni. Agar *Page Object* tidak bisa di- *new* dari luar, maka constructor-nya harus di-set menjadi *package-private* (hapus tulisan `public`). Syarat mutlaknya: Factory dan PageObject **harus berada di folder/package yang sama**.

**Plan Eksekusi:**
1. Hapus pemisahan folder `pageObject` dan `factories`.
2. Gabungkan menjadi folder modul (misal: `org.example.modules.frontoffice.manageOrganization`).
3. Di dalam folder tersebut, tempatkan `ManageOrganizationFactory.java` dan `DashboardManageOrganizationPage.java` secara berdampingan.
4. Hapus akses `public` pada constructor `DashboardManageOrganizationPage`.
   ```java
   // Hapus modifier 'public'
   DashboardManageOrganizationPage(WebDriver driver) { ... }
   ```
5. Ubah akses di test class: karena *package-private*, hanya class `ManageOrganizationFactory` (yang sekamar) yang bisa melakukan `new DashboardManageOrganizationPage()`. Class Test yang ada di luar *package* ini akan error garis merah (Compile Error) jika mencoba instansiasi langsung.

**Kelebihan:** Diamankan secara langsung oleh *compiler* Java IDE (langsung garis merah).
**Kekurangan:** Harus merombak / memindahkan ratusan file ke struktur folder yang baru.
