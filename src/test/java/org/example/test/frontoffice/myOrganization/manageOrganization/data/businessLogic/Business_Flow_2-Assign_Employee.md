# Business Flow — Assign Employee (Flow 2)

## 1. Purpose

Flow 2 — **Assign Employee** adalah proses untuk menyelesaikan assignment employee setelah perubahan Organization, Job, atau Position pada **Flow 1 — Set Organization**.

Tujuan utama Flow 2 adalah memastikan setiap employee yang terdampak perubahan Flow 1 mendapatkan **Primary Assignment** yang valid sebelum proses dapat dilanjutkan ke Flow 3.

> **Gate utama:** selama masih ada **Unassigned Employee**, Admin/HR tidak dapat melanjutkan ke Flow 3.

---

## 2. Business Story

Perubahan Organization pada Flow 1 dapat menyebabkan employee yang sebelumnya menempati suatu position tidak lagi memiliki assignment pada position tersebut.

Employee yang terdampak akan masuk ke daftar **Unassigned Employee**.

Pada Flow 2, Admin/HR harus menentukan kembali position dan organization tempat employee tersebut akan bekerja.

Employee dapat:

- dipindahkan ke Primary Assignment baru;
- ditambahkan sebagai Primary Assignment;
- ditambahkan sebagai Secondary Assignment / PJS jika business condition mengizinkan;
- di-remove dari position sehingga menjadi Unassigned dan dapat di-assign kembali.

Proses selesai ketika seluruh employee yang berada di **Unassigned Employee** sudah memiliki Primary Assignment yang valid.

---

# 3. Konsep Utama

## 3.1 Primary Assignment

Primary Assignment adalah assignment utama employee.

Business rule:

- Satu employee hanya boleh memiliki **1 Primary Assignment**.
- Jika employee dipindahkan sebagai Primary ke position lain, Primary Assignment sebelumnya ditinggalkan.
- Employee yang berasal dari Unassigned harus mendapatkan Primary Assignment agar status Unassigned-nya selesai.

---

## 3.2 Secondary Assignment / PJS

PJS adalah **Secondary Assignment**.

Jika employee di-assign sebagai PJS:

```text
Primary Assignment lama
        +
Secondary Assignment baru (PJS)
```

Business rule:

- Primary Assignment lama tetap aktif.
- Assignment baru diberi label **PJS**.
- Employee PJS tidak memiliki opsi **Move**.
- Employee PJS hanya dapat di-**Remove**.
- PJS tidak menyelesaikan status Unassigned jika employee sebelumnya berasal dari Unassigned.

### Contoh

```text
Employee A

Primary    → Staff Accountant
Secondary  → Acting Manager (PJS)
```

Employee tetap mempunyai Primary Assignment di Staff Accountant.

---

# 4. Unassigned Employee

## 4.1 Definisi

**Unassigned Employee** adalah employee yang terkena dampak perubahan Organization/Job/Position pada Flow 1 dan membutuhkan assignment kembali.

Unassigned Employee bukan berarti employee dihapus dari employee master.

Statusnya adalah:

```text
Employee
   ↓
terdampak perubahan Flow 1
   ↓
Unassigned
   ↓
harus mendapatkan Primary Assignment
```

## 4.2 Business Rule

Employee dari Unassigned harus di-assign ke **Primary Assignment**.

Jika hanya di-assign sebagai PJS:

```text
Unassigned Employee
       ↓
Secondary / PJS
       ↓
tetap Unassigned
```

Karena employee belum mempunyai Primary Assignment.

---

# 5. Entry Point Flow 2

Terdapat dua cara utama untuk melakukan assignment/move.

## 5.1 Entry Point A — Manage Incumbent

```text
Select Organization
      ↓
Manage Incumbent
      ↓
Open Position
      ↓
Manage Incumbent
      ↓
Search Employee
      ↓
Add / Assign Employee
```

Employee dapat berasal dari:

- Unassigned Employee
- Employee existing yang masih mempunyai active assignment

## 5.2 Entry Point B — Move Employee dari Unassigned

```text
Open Unassigned Employee
      ↓
Select Employee
      ↓
Move
      ↓
Select Target Organization
      ↓
Select Target Position
      ↓
Submit
```

## 5.3 Entry Point C — Move dari Existing Position

```text
Select Organization
      ↓
Manage Incumbent
      ↓
Open Position
      ↓
Select Employee
      ↓
More Options (...)
      ↓
Move
      ↓
Select Target Organization
      ↓
Select Target Position
      ↓
Submit
```

---

# 6. Add Incumbent

## 6.1 Add sebagai Primary — Employee dari Unassigned

```text
Unassigned Employee
       ↓
Add as Primary
       ↓
Primary Assignment baru
       ↓
Tidak lagi Unassigned
       ↓
Tag = Move
```

Walaupun menggunakan action **Add**, secara business dianggap **Move** karena employee berasal dari Unassigned.

## 6.2 Add sebagai Primary — Employee Existing

```text
Existing Employee
       ↓
Add as Primary
       ↓
Primary Assignment berpindah
       ↓
Assignment lama ditinggalkan
```

Employee hanya boleh memiliki satu Primary Assignment.

Untuk employee existing yang ditambahkan ke position baru/berbeda:

```text
Tag = Move
```

### Exception

Jika employee kembali ke **position existing original** sebagai Primary:

```text
Tag = No Tag
```

---

# 7. Move Incumbent

Move memindahkan Primary Assignment employee.

Contoh:

```text
Before:
Employee A
Primary → Staff Accountant

After Move:
Employee A
Primary → Senior Accountant
```

Position lama kehilangan incumbent.

Employee tetap hanya mempunyai satu Primary Assignment.

### Tag

Employee dari Unassigned:

```text
Unassigned
   ↓
Move as Primary
   ↓
Tag = Move
```

Employee existing:

```text
Primary A
   ↓
Move
   ↓
Primary B
   ↓
Tag = Move
```

### Exception — Kembali ke Original Existing Position

```text
Original:
Employee A → Staff Accountant

Remove
↓
Unassigned

Add / Move as Primary
↓
Staff Accountant
```

Hasil:

```text
No Tag
```

karena employee kembali ke position existing original sebagai Primary.

---

# 8. PJS / Secondary Assignment

Jika employee di-assign sebagai PJS:

```text
Employee A

Primary    → Position A
Secondary  → Position B (PJS)
```

Hasil:

- Primary lama tetap aktif.
- Secondary assignment dibuat.
- Assignment baru diberi label **PJS**.
- Tag = **New + PJS**.

### PJS dari Employee Existing

```text
Existing Employee
      ↓
Add as Secondary/PJS
      ↓
Primary lama tetap
      ↓
Secondary baru dibuat
      ↓
Tag = New + PJS
```

### PJS dari Unassigned

```text
Unassigned Employee
      ↓
Assign as PJS
      ↓
Secondary/PJS dibuat
      ↓
Primary belum ada
      ↓
Tetap Unassigned
```

Karena itu PJS saja tidak menyelesaikan kewajiban assignment employee.

---

# 9. Employee PJS Tidak Bisa Move

Employee yang mempunyai assignment PJS tidak mempunyai opsi **Move** untuk assignment PJS tersebut.

```text
PJS
 ↓
Remove
```

Bukan:

```text
PJS
 ↓
Move
```

---

# 10. Remove Incumbent

Remove bukan berarti menghapus employee.

Remove berarti mencabut employee dari assignment/position saat ini.

```text
Position A
   ↓
Remove Employee
   ↓
Employee → Unassigned
```

Employee tetap berada di employee master.

Setelah Remove:

- jumlah incumbent position berkurang;
- available slot bertambah;
- employee dapat di-assign kembali.

---

# 11. FTE dan Position Capacity

## 11.1 FTE adalah Hard Limit

FTE merupakan kapasitas maksimum position.

```text
FTE = 4
Incumbent = 3
Available Slot = 1
```

Maka satu employee masih dapat di-assign.

Jika:

```text
FTE = 4
Incumbent = 4
Available Slot = 0
```

maka tidak boleh menambahkan assignment lagi.

## 11.2 FTE Tidak Berubah karena Add/Move/Remove

Add, Move, dan Remove tidak mengubah nilai FTE.

Yang berubah adalah jumlah incumbent.

### Contoh Remove

Before:

```text
FTE = 4
Incumbent = 4
Available Slot = 0
```

Remove satu employee:

```text
FTE = 4
Incumbent = 3
Available Slot = 1
```

> **FTE = kapasitas position, bukan jumlah employee saat ini.**

---

# 12. Organization dan Position dari Flow 1

Semua Organization atau Position yang dihasilkan dari perubahan pada Flow 1 dianggap sebagai **Organization/Position baru** dalam konteks Flow 2.

Contoh perubahan Flow 1:

- Add New
- Move
- Rename
- Merge
- Split
- Edit Job & Position
- perubahan organization/position lainnya

Position hasil perubahan tersebut dapat menerima employee selama masih tersedia slot FTE.

```text
Position hasil Flow 1
FTE = 4
Incumbent = 2
Available Slot = 2
```

→ employee dapat di-assign sampai kapasitas mencapai 4.

---

# 13. Expand Children

**Expand Children bukan perubahan Organization.**

Fungsinya hanya untuk menampilkan child organization yang sebelumnya disembunyikan agar hierarchy tidak terlalu panjang.

```text
Directorate
 ├── Division A
 ├── Division B
 └── Division C
```

Expand Children hanya menampilkan child organization.

Expand Children **tidak membuat Organization atau Position baru**.

---

# 14. Tag Business Rule

Tag tidak ditentukan hanya berdasarkan tombol/action yang digunakan.

| Kondisi | Tag |
|---|---|
| Employee existing di luar Unassigned/tree organization → Add Primary ke position baru/berbeda | **New** |
| Employee dari Unassigned → Add Primary | **Move** |
| Employee dari Unassigned → Move Primary | **Move** |
| Employee existing → Move Primary | **Move** |
| Employee kembali ke original existing position sebagai Primary | **No Tag** |
| Employee → Secondary/PJS | **New + PJS** |
| Employee Unassigned → hanya PJS | **New + PJS**, tetapi tetap Unassigned |

> **Action button tidak selalu menentukan Tag. Business context dan hasil assignment yang menentukan Tag.**

---

# 15. Flow Utama End-to-End

```text
FLOW 1
Set Organization
       ↓
Organization / Job / Position berubah
       ↓
Employee terdampak
       ↓
Employee masuk Unassigned Employee
       ↓
────────────────────────────────
FLOW 2
Assign Employee
────────────────────────────────
       ↓
Admin/HR memilih target Organization + Position
       ↓
Validasi FTE
       ↓
Add / Move / Remove
       ↓
Tentukan Primary atau Secondary/PJS
       ↓
Assignment diperbarui
       ↓
Cek Unassigned Employee
       ↓
Unassigned > 0?
       ├── YES → tidak boleh Next
       └── NO
            ↓
          NEXT
            ↓
         FLOW 3
```

---

# 16. Business Stories

## Story A — Employee terdampak Flow 1

```text
Employee A
Primary → Staff Accountant
```

Flow 1 mengubah organization/position dan employee terdampak.

```text
Employee A → Unassigned
```

Flow 2:

```text
Add / Move as Primary
       ↓
Position baru
       ↓
Tag = Move
       ↓
Employee tidak lagi Unassigned
```

---

## Story B — Employee Existing ditambahkan sebagai Primary

```text
Employee B
Primary → Staff Accountant
```

HR melakukan Add ke position lain sebagai Primary.

Karena employee hanya boleh mempunyai satu Primary:

```text
Staff Accountant
      ↓
ditinggalkan

Senior Accountant
      ↓
Employee B
```

Untuk employee existing yang di-Add ke position baru:

```text
Tag = Move
```

---

## Story C — Employee Existing ditambahkan sebagai PJS

```text
Employee C
Primary → Staff Accountant
```

HR memilih Add → Secondary/PJS.

Hasil:

```text
Primary    → Staff Accountant
Secondary  → Manager (PJS)
```

Tag:

```text
New + PJS
```

Primary assignment tidak berubah.

---

## Story D — Remove lalu Assign Kembali

```text
Employee D
Primary → Staff Accountant
```

Remove:

```text
Staff Accountant
       ↓
Employee D → Unassigned
```

Kemudian employee dikembalikan ke position existing original:

```text
Add / Move as Primary
       ↓
Staff Accountant
```

Hasil:

```text
Primary → Staff Accountant
Tag     → No Tag
```

---

# 17. Final Gate Flow 2

Flow 2 selesai apabila:

```text
Unassigned Employee = 0
```

Tidak diperlukan semua position mempunyai incumbent penuh.

Contoh:

```text
Position A
FTE = 5
Incumbent = 5

Position B
FTE = 5
Incumbent = 3

Position C
FTE = 4
Incumbent = 1

Unassigned Employee = 0
```

Status:

```text
FLOW 2 = Complete
NEXT → FLOW 3 = Allowed
```

Sebaliknya:

```text
Unassigned Employee = 1
```

maka:

```text
NEXT → FLOW 3 = Not Allowed
```

---

# 18. Business Rules — Quick Reference

| ID | Business Rule |
|---|---|
| BR-01 | Employee hanya boleh mempunyai 1 Primary Assignment |
| BR-02 | PJS adalah Secondary Assignment |
| BR-03 | PJS mempertahankan Primary Assignment lama |
| BR-04 | Employee PJS tidak dapat Move, hanya Remove |
| BR-05 | Employee dari Unassigned harus mendapatkan Primary Assignment |
| BR-06 | Assign Unassigned hanya sebagai PJS tidak menyelesaikan status Unassigned |
| BR-07 | Remove tidak menghapus employee dari employee master |
| BR-08 | Remove membuat employee menjadi Unassigned |
| BR-09 | FTE adalah hard limit position |
| BR-10 | Add/Move/Remove mengubah occupancy, bukan nilai FTE |
| BR-11 | Organization/Position hasil perubahan Flow 1 dianggap baru pada Flow 2 |
| BR-12 | Expand Children hanya menampilkan child organization |
| BR-13 | Add Primary dari Unassigned menghasilkan Tag Move |
| BR-14 | Add Primary employee existing ke position baru menghasilkan Tag Move |
| BR-15 | Move Primary menghasilkan Tag Move |
| BR-16 | Kembali ke original existing position sebagai Primary menghasilkan No Tag |
| BR-17 | Secondary/PJS menghasilkan Tag New + PJS |
| BR-18 | Selama Unassigned Employee > 0, Flow 3 tidak dapat dilanjutkan |
| BR-19 | Flow 3 tetap dapat dilanjutkan jika Unassigned = 0 walaupun masih ada FTE kosong |
| BR-20 | Add dan Move dari Unassigned dapat mempunyai hasil business yang sama; perbedaannya adalah entry point |

---

# 19. Core Business Principle

> **Flow 1 mengubah struktur Organization/Job/Position. Jika perubahan tersebut berdampak pada employee, employee menjadi Unassigned. Flow 2 bertugas menyelesaikan kembali assignment employee tersebut ke Primary Position yang valid. Employee juga dapat ditambahkan sebagai employee baru atau Secondary/PJS sesuai kondisi. Setiap position memiliki kapasitas FTE sebagai hard limit. Flow 2 hanya boleh selesai apabila tidak ada lagi Unassigned Employee.**
