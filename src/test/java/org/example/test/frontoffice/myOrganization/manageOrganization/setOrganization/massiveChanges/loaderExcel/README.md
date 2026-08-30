# 📖 Dokumentasi Logic & Arsitektur Automasi Massive Organization (Flow 1)

Dokumentasi ini merangkum seluruh arsitektur, aliran data, penataan package terstruktur, serta petunjuk eksekusi untuk modul **Massive Add Organization & Position Automation (D1 s/d D6)** pada FrontOffice Manage Organization Set Organization Flow 1.

---

## 📁 1. Struktur Package Terstruktur (`massiveChanges`)

Lokasi Root Package:
`src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/`

```
massiveChanges/
├── model/                                  # [SHARED MODEL]
│   └── MassiveOrgModel.java                # Model Data Objek Organisasi & Posisi
│
├── data/                                   # [SHARED DATA]
│   ├── JobLOV.txt                          # Referensi LOV Pekerjaan (733 records)
│   ├── LocationLOV.txt                     # Referensi LOV Lokasi Penugasan (248 records)
│   ├── Rc_codeLOV.txt                      # Referensi LOV Kode RC (3 records)
│   └── organization-development-flow1.xlsx # Template Excel Asal (Flow 1 Export)
│
├── nonExcel/                               # [NON-EXCEL FLOW - JAVA MEMORY]
│   ├── MassiveOrgDataFactory.java          # Java Data Factory (Membuat Data di Memori RAM)
│   ├── MassiveOrgDataFactoryTest.java      # Unit Test Java Data Factory
│   └── MassiveChanges.java                 # TestNG UI Test Class (Eksekusi Non-Excel)
│
└── loaderExcel/                            # [EXCEL FLOW - PYTHON & EXCEL READER]
    ├── README.md                           # Dokumentasi ini
    ├── ExcelLoaderConfig.java              # Parameter Konfigurasi Terpusat
    ├── UniqueSeedManager.java              # Single Source of Truth Unik Terpusat
    ├── Flow1ExcelExportReader.java         # Pembaca Data Excel POI Java
    ├── Flow1ExcelExportReaderTest.java     # Unit Test Parser Excel & Seed Validation
    ├── MassiveOrgExcelTest.java            # TestNG UI Test Class (Eksekusi Excel)
    ├── generate_massive_orgs_excel.py      # Script Python Generator File Excel Baru
    └── organization-development-flow1-generated.xlsx # File Output Excel Hasil Generate
```

---

## ⚙️ 2. Centralized Configuration ([`ExcelLoaderConfig.java`](file:///d:/MyCave/Refactor/AutomationTest/src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/ExcelLoaderConfig.java))

Seluruh parameter eksekusi dan path file ditampung secara terpusat:

| Variabel Parameter | Tipe Data | Nilai Default | Deskripsi |
| :--- | :--- | :--- | :--- |
| `SOURCE_TEMPLATE_EXCEL_PATH` | `String` | `.../data/organization-development-flow1.xlsx` | Path template Excel asal |
| `GENERATED_OUTPUT_EXCEL_PATH`| `String` | `.../loaderExcel/organization-development-flow1-generated.xlsx` | Path file Excel BARU hasil generate |
| `AUTO_POPULATE_PROCEED` | `boolean` | `true` | `true`: suffix timestamp unik; `false`: nama statis |
| `TARGET_ORG_COUNT` | `int` | `30` | Jumlah organisasi yang diproses (`0` atau `30` untuk semua) |
| `D0_ORGANIZATION` | `String` | `"Corporate Assurance ID"` | Nama organisasi D0 baseline |
| `D0_PRIMARY_POSITION` | `String` | `"Chief Audit Executive"` | Nama posisi primary D0 baseline |
| `POSITION_NAMING_TYPE` | `String` | `"Area of Responsibility / Job Scope"` | Dropdown choice Position Naming Type |

---

## 🔒 3. Logika Garansi Bebas Duplikat Terpusat ([`UniqueSeedManager.java`](file:///d:/MyCave/Refactor/AutomationTest/src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/UniqueSeedManager.java))

Untuk menjamin bahwa nama organisasi dan posisi **TIDAK PERNAH DUPLIKAT** (baik antar-flow Non-Excel vs Excel, maupun antar-run lintas hari), digunakan pilar **Precision Timestamp Suffixing**:

$$\text{Suffix Unik} = \text{\_`[SYSTEM_TIMESTAMP_MILLIS]`\_`[ATOMIC_COUNTER]`}$$

- **Keunggulan**:
  1. **Hukum Fisika Waktu**: Jam komputer (`System.currentTimeMillis()`) selalu berjalan maju dan tidak pernah berulang.
  2. **Zero Overhead ($\mathcal{O}(1)$)**: Instantaneous, tanpa menyimpan file history di disk yang dapat membengkak dan memperlambat pencarian.
  3. **Synchronized Across Flows**: Baik `MassiveOrgDataFactory` (Java) maupun `Flow1ExcelExportReader` (Excel) menggunakan `UniqueSeedManager` yang sama.

---

## 🔄 4. Aliran Data: Non-Excel vs Excel Flow

### A. Non-Excel Flow (`massiveChanges.nonExcel`)
```
[ MassiveOrgDataFactory ] ──► (Generates MassiveOrgModel in RAM) ──► [ SetOrganizationHelper (Selenium) ] ──► [ Web UI ]
```

### B. Excel Flow (`massiveChanges.loaderExcel`)
```
[ generate_massive_orgs_excel.py ]
               │
               ▼ (Writes to NEW output file)
[ organization-development-flow1-generated.xlsx ]
               │
               ▼ (Parsed by Flow1ExcelExportReader)
    [ MassiveOrgExcelTest ] ──► [ SetOrganizationHelper (Selenium) ] ──► [ Web UI ]
```

---

## 🎛️ 5. Fleksibilitas Batas Jumlah Node (`limit` / `targetOrgCount`)

Parameter `limit` bersifat **BEBAS (Fleksibel)**:
- Pengguna dapat mengisikan angka integer berapapun (misal: `1`, `3`, `5`, `7`, `10`, `15`, `30`).
- Sistem (baik Java Factory maupun Excel Reader) secara otomatis memotong (*slice*) hirarki organisasi top-down tepat pada kuota `limit` tersebut.

---

## 🚀 6. Perintah Eksekusi

### A. Regenerasi Data ke File Excel Baru:
```powershell
python src/test/java/org/example/test/frontoffice/myOrganization/manageOrganization/setOrganization/massiveChanges/loaderExcel/generate_massive_orgs_excel.py organization-development-flow1-generated.xlsx 30 True
```

### B. Menjalankan Unit Test (Factory & Excel Reader Validation):
```powershell
mvn test -Dtest="Flow1ExcelExportReaderTest,MassiveOrgDataFactoryTest"
```

### C. Menjalankan Test Arsitektur Framework:
```powershell
mvn test -Dtest=ArchitectureTest
```

### D. Menjalankan UI Test Automation:
```powershell
# Jalankan UI Test dari Excel
mvn test -Dtest=MassiveOrgExcelTest

# Jalankan UI Test Non-Excel
mvn test -Dtest=MassiveChanges
```
