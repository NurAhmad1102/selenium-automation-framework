import os
from docx import Document
from docx.shared import Inches, Pt, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.style import WD_STYLE_TYPE

def add_bullet_point(doc, text, level=0):
    p = doc.add_paragraph(text, style='List Bullet')
    p.paragraph_format.left_indent = Inches(0.25 * (level + 1))

def create_document():
    doc = Document()
    
    # Title Page
    title = doc.add_heading('Automation Test Framework Architecture Guide', 0)
    title.alignment = WD_ALIGN_PARAGRAPH.CENTER
    
    subtitle = doc.add_paragraph('Panduan Komprehensif untuk QA Automation Engineer', style='Subtitle')
    subtitle.alignment = WD_ALIGN_PARAGRAPH.CENTER
    
    doc.add_paragraph('\n' * 5)
    info = doc.add_paragraph()
    info.alignment = WD_ALIGN_PARAGRAPH.CENTER
    info.add_run('Document Version: 2.1\n').bold = True
    info.add_run('Last Updated: May 2026\n')
    info.add_run('Scope: Back Office & Front Office Automation Framework')
    
    doc.add_page_break()

    # Table of Contents
    doc.add_heading('Table of Contents', level=1)
    doc.add_paragraph("1. Introduction\n2. Gambaran Umum Folder & Fungsi File\n3. Relation Tiap File (Alur Interaksi)\n4. Cara Kerja & Logic Framework\n5. Panduan Menambahkan Modul Baru\n6. Best Practices & Tips\n7. Execution & Reporting")
    doc.add_page_break()

    # 1. Introduction
    doc.add_heading('1. Introduction', level=1)
    doc.add_paragraph(
        "Framework ini dirancang menggunakan pola Page Object Model (POM) dengan Lazy-Initialization Factory Pattern. "
        "Struktur ini memastikan bahwa automation project tetap scalable, mudah dipelihara (maintainable), dan mendukung eksekusi paralel."
    )

    # 2. Gambaran Umum Folder & Fungsi File
    doc.add_heading('2. Gambaran Umum Folder & Fungsi File', level=1)
    
    doc.add_heading('2.1 Core Logic (src/main/java)', level=2)
    struct_main = [
        ("BasePage.java", "Parent class untuk semua Page Object. Berisi wrapper Selenium (click, type, wait) dengan logging otomatis."),
        ("pageObject/", "Menyimpan class Page Object spesifik per modul. Berisi locator (@FindBy) dan method aksi."),
        ("factories/", "Mengatur instansiasi Page Object menggunakan Lazy Initialization (objek dibuat hanya saat dibutuhkan)."),
        ("drivers/DriverManager.java", "Manajemen WebDriver menggunakan ThreadLocal untuk mendukung eksekusi paralel."),
        ("utils/actionUtils/ActionUtils.java", "Helper tambahan untuk aksi kompleks (Calendar, custom Dropdown AntDesign, clear input).")
    ]
    for file, desc in struct_main:
        p = doc.add_paragraph(style='Normal')
        p.add_run(f"• {file}: ").bold = True
        p.add_run(desc)

    doc.add_heading('2.2 Test Execution (src/test/java)', level=2)
    struct_test = [
        ("BaseTest.java", "Lifecycle management (@BeforeMethod, @AfterMethod). Mengatur setup browser dan alokasi akun."),
        ("BackOfficeBaseTest / FrontOfficeBaseTest", "Spesialisasi setup untuk masing-masing platform (URL, Role)."),
        ("MainPagesFactory.java", "Entry point utama bagi Test Class untuk mengakses semua Page Object."),
        ("utils/account/AccountAllocator.java", "Manajemen pembagian akun dari Excel untuk mendukung paralel testing."),
        ("test/", "Tempat skenario test (Test Cases) disimpan, diorganisir per modul.")
    ]
    for file, desc in struct_test:
        p = doc.add_paragraph(style='Normal')
        p.add_run(f"• {file}: ").bold = True
        p.add_run(desc)

    # 3. Relation Tiap File (Alur Interaksi)
    doc.add_heading('3. Relation Tiap File (Alur Interaksi)', level=1)
    doc.add_paragraph("Alur interaksi antar komponen dalam framework ini mengikuti hirarki berikut:")
    relations = [
        "1. Test Class memanggil factory method yang tersedia di Base Test (misal: loginPages()).",
        "2. Base Test mengambil instance dari MainPagesFactory.",
        "3. MainPagesFactory memanggil Module Factory spesifik (misal: LoginPagesFactory).",
        "4. Module Factory mengembalikan instance Page Object (misal: LoginPage).",
        "5. Page Object melakukan interaksi UI menggunakan method dari BasePage."
    ]
    for rel in relations:
        add_bullet_point(doc, rel)

    # 4. Cara Kerja & Logic Framework
    doc.add_heading('4. Cara Kerja & Logic Framework', level=1)
    
    doc.add_heading('4.1 Lazy Initialization', level=2)
    doc.add_paragraph(
        "Page Object tidak dibuat di awal. Saat test memanggil login().getLoginPage(), factory akan mengecek apakah "
        "objek sudah ada. Jika belum, baru di-instansiasi. Ini menghemat penggunaan memory."
    )

    doc.add_heading('4.2 Parallel-Ready with ThreadLocal', level=2)
    doc.add_paragraph(
        "Setiap thread eksekusi memiliki WebDriver-nya sendiri yang disimpan dalam ThreadLocal. "
        "Hal ini memungkinkan banyak browser berjalan sekaligus tanpa saling mengganggu."
    )

    doc.add_heading('4.3 Account Allocation', level=2)
    doc.add_paragraph(
        "Sebelum test berjalan, AccountAllocator akan mengambil akun yang tersedia dari file Excel. Ini mencegah "
        "penggunaan akun yang sama oleh dua test yang berjalan secara bersamaan (Race Condition)."
    )

    # 5. Panduan Menambahkan Modul Baru
    doc.add_heading('5. Panduan Menambahkan Modul Baru', level=1)
    doc.add_paragraph("Jika Anda ingin menambahkan modul baru (contoh: 'UserManagement'), ikuti urutan berikut:")
    
    steps = [
        "Langkah 1: Buat Page Object. Buat class UserListPage.java di folder pageObject, extends BasePage. Definisikan locator dan method aksi.",
        "Langkah 2: Buat Module Factory. Buat class UserManagementFactory.java di folder factories untuk mengelola Page Object modul tersebut.",
        "Langkah 3: Daftarkan di MainPagesFactory. Tambahkan getter method untuk UserManagementFactory di MainPagesFactory.java.",
        "Langkah 4: Daftarkan di BaseTest. Tambahkan shortcut method di BackOfficeBaseTest (atau FrontOfficeBaseTest) agar test bisa langsung memanggil modul.",
        "Langkah 5: Buat Test Class. Buat skenario test di folder test/ yang extends BaseTest terkait."
    ]
    for step in steps:
        add_bullet_point(doc, step)

    # 6. Best Practices & Tips
    doc.add_heading('6. Best Practices & Tips', level=1)
    practices = [
        "Jangan Hardcode: Gunakan TestDataFactory untuk URL, password, dan data environment.",
        "Explicit Waits: Gunakan method dari BasePage atau ActionUtils. JANGAN gunakan Thread.sleep().",
        "Assertions di Test: Page Object hanya bertugas interaksi dan ambil data. Pengecekan (Assert) dilakukan di Test Class.",
        "Selector: Prioritaskan ID atau CSS Selector yang unik. Hindari XPath yang terlalu panjang.",
        "Clean Up: Selalu pastikan browser ditutup di @AfterMethod via BaseTest."
    ]
    for practice in practices:
        add_bullet_point(doc, practice)

    # 7. Execution & Reporting
    doc.add_heading('7. Execution & Reporting', level=1)
    doc.add_paragraph("• Jalankan Test: mvn clean test")
    doc.add_paragraph("• Laporan: Cek folder target/ExtentReports/ untuk hasil detail eksekusi dan screenshot kegagalan.")

    # Save Document
    filename = 'Automation_Test_Framework_Architecture.docx'
    doc.save(filename)
    print(f"Document updated and generated successfully: {filename}")

if __name__ == "__main__":
    create_document()
