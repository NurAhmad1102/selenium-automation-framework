# Account Session Logic

Dokumen ini menjelaskan cara kerja alokasi akun, alias akun, switch user, dan pola penggunaan untuk test biasa maupun flow multi-actor.

## Ringkasan

Setiap test yang turun dari `BaseTest` selalu membutuhkan user awal. User awal itu ditentukan oleh `getRole()` di base/module test.

Untuk test biasa, cukup definisikan `getRole()` di base module. Tidak perlu memanggil helper switch user.

Untuk test multi-user, user awal tetap berasal dari `getRole()`, lalu test method bisa switch ke user lain menggunakan alias.

Contoh flow multi-actor:

```text
HR_OD submit
HR_PSOM approve
HR_OD verify again with the same submitter account
```

Desain saat ini mendukung kebutuhan tersebut:

- Test biasa tetap memakai default role dari `getRole()`.
- Akun default otomatis disimpan sebagai alias `default`.
- Test multi-user bisa memberi alias bisnis seperti `submitter` dan `approver`.
- Test bisa kembali ke akun awal yang sama, bukan mengambil akun lain dengan role yang sama.
- Test lintas aplikasi bisa switch antara Front Office dan Back Office dalam satu method.
- Semua akun yang dialokasikan dalam satu test thread dilepas saat teardown.
- URL aplikasi dibaca terpisah oleh `UrlProvider`, bukan oleh `AccountAllocator`.

## Default Role

`getRole()` tetap perlu ada untuk setiap class test yang turun dari `BaseTest`. Best practice-nya, taruh `getRole()` di base test module, bukan di setiap test class.

Contoh di base module:

```java
@Override
protected String getRole() {
    return UserRole.HR_OD.getValue();
}
```

Alasannya, `BaseTest.setUp()` selalu melakukan allocation user awal:

```java
AccountAllocator.allocateAccount(testName, getRole());
```

Jika tidak ada method test yang perlu switch multi-account, cukup taruh `getRole()` di base module saja. Test method tidak perlu memanggil `useCurrentUserAs(...)`, `useBackOfficeUser(...)`, atau `useFrontOfficeUser(...)`.

Contoh test biasa:

```java
public class AddOrganizationTest extends ManageOrganizationBaseTest {
    @Test
    public void addOrganization() {
        submit();
        verify();
    }
}
```

## Konsep Alias

Alias adalah nama lokal untuk akun dalam satu test method. Alias tidak perlu ditambahkan ke `Data Userlogin.xlsx`.

Contoh alias:

```text
default
submitter
approver
verifier
maker
checker
```

Saat `BaseTest.setUp()` berjalan, akun dari `getRole()` otomatis dialokasikan dan disimpan sebagai alias:

```text
default
```

Jika test ingin memberi nama bisnis ke akun default, gunakan:

```java
useCurrentUserAs("submitter");
```

Alias `submitter` dan `default` akan menunjuk ke akun yang sama.

## API Utama

API umum dari `BaseTest`:

```java
useCurrentUserAs("submitter");
currentAccount();
currentNip();
currentUsername();
currentAccountAlias();
account("submitter");
accountNip("submitter");
accountUsername("submitter");
```

API Back Office dari `BackOfficeBaseTest`:

```java
useBackOfficeUser("approver", UserRole.HR_PSOM);
useBackOfficeUser("submitter");
useBackOfficeUser("default");
```

API Front Office dari `FrontOfficeBaseTest`:

```java
useFrontOfficeUser("employee", UserRole.HR_OD);
useFrontOfficeUser("default");
```

Kedua helper tersebut juga tersedia dari `BaseTest`, sehingga test yang extend `FrontOfficeBaseTest` tetap bisa memanggil `useBackOfficeUser(...)`, dan test yang extend `BackOfficeBaseTest` tetap bisa memanggil `useFrontOfficeUser(...)`.

## Contoh Flow Back Office

```java
@Override
protected String getRole() {
    return UserRole.HR_OD.getValue();
}

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

Penjelasan:

- `getRole()` mengalokasikan akun `HR_OD` sebagai user awal dan menyimpannya sebagai alias `default`.
- `useCurrentUserAs("submitter")` menambahkan alias `submitter` ke akun `HR_OD` yang sama.
- `useBackOfficeUser("approver", UserRole.HR_PSOM)` mengalokasikan akun `HR_PSOM`, menyimpannya sebagai `approver`, lalu login ulang ke Back Office.
- `useBackOfficeUser("submitter")` kembali ke akun `HR_OD` yang sama seperti awal, lalu login ulang.

Jika tidak butuh nama bisnis, bisa langsung kembali ke user awal:

```java
useBackOfficeUser("default");
```

## Contoh Flow Lintas Aplikasi

Gunakan base sesuai default URL pertama. Jika test dimulai dari Front Office, extend base Front Office. Jika test dimulai dari Back Office, extend base Back Office.

```java
public class TransactionApprovalTest extends FrontOfficeBaseTest {
    @Override
    protected String getRole() {
        return UserRole.HR_OD.getValue();
    }

    @Test
    public void approvalTrxUpdateEmployee() {
        useCurrentUserAs("submitter");

        submit();

        useBackOfficeUser("approver", UserRole.HR_PSOM);
        approve();

        useFrontOfficeUser("submitter");
        verify();
    }
}
```

Penjelasan:

- Test pertama kali dibuka di Front Office karena extend `FrontOfficeBaseTest`.
- `useBackOfficeUser("approver", UserRole.HR_PSOM)` mengalokasikan approver, reset browser session, pindah ke URL Back Office, lalu login.
- `useFrontOfficeUser("submitter")` mengambil akun submitter yang sama seperti awal, reset browser session, pindah ke URL Front Office, lalu login.
- Jika butuh page factory lintas aplikasi, gunakan `backOfficePages()` atau `frontOfficePages()` dari base test, atau `officeSession().backOfficePages()` / `officeSession().frontOfficePages()`.

## Technical Flow

`Data Userlogin.xlsx` dibaca satu kali oleh `LoginDataRepository`. Hasilnya dipakai oleh:

- `AccountAllocator` untuk account list.
- `UrlProvider` untuk URL Back Office dan Front Office.
- `OfficeSessionSwitcher` untuk login ulang ke Front Office atau Back Office saat switch user.

State account disimpan di `AccountAllocator`:

```java
ThreadLocal<Map<String, UserloginConfig>> accountAliases
ThreadLocal<UserloginConfig> currentAccount
ThreadLocal<String> currentAlias
ConcurrentHashMap<UserloginConfig, Boolean> allocatedAccounts
```

Saat allocate:

1. Cari pool account berdasarkan role.
2. Lock account dengan `allocatedAccounts.putIfAbsent(...)`.
3. Simpan account ke alias lokal thread.
4. Set account sebagai current account.

Saat switch ke alias existing:

1. Ambil account dari `accountAliases`.
2. Set account sebagai current account.
3. Helper office reset browser session.
4. Helper office login ulang dengan account tersebut.

Saat teardown:

1. `BaseTest.tearDown()` memanggil `AccountAllocator.releaseAllAccounts()`.
2. Semua account unik dari alias thread dilepas dari `allocatedAccounts`.
3. `ThreadLocal` current account, current alias, dan alias map dibersihkan.

## Parallel Safety

Parallel safety berasal dari dua lapis:

- `ThreadLocal` memastikan alias dan current account hanya milik test thread berjalan.
- `ConcurrentHashMap.putIfAbsent(...)` memastikan satu account fisik tidak dipakai dua test thread sekaligus.

Jika semua account untuk role tertentu sedang dipakai, test akan menunggu sampai ada account yang release. Timeout default:

```text
30 seconds
```

Override timeout:

```bash
mvn test -Daccount.wait.timeout.seconds=120
```

Jika timeout habis, test fail dengan pesan role, alias, dan test name yang meminta account.

## Data Excel

Tidak perlu column alias tambahan.

Excel tetap menyimpan data account:

```text
role
nip
username
password
url
```

Alias adalah concern test runtime, bukan test data.

URL dibaca dari sheet `URL` melalui `UrlProvider`. Base test memakai:

```java
UrlProvider.getBackOfficeUrl();
UrlProvider.getFrontOfficeUrl();
```

Key utama yang dicari:

```text
Backoffice_staging
Frontoffice_staging
```

## Catatan Penggunaan

Gunakan `getRole()` di base module untuk menentukan user awal semua test dalam module tersebut.

Gunakan `useCurrentUserAs("submitter")` di awal test multi-user jika ingin nama actor bisnis yang jelas.

Gunakan `useBackOfficeUser("default")` atau `useFrontOfficeUser("default")` jika hanya ingin balik ke user awal tanpa alias bisnis.

Setelah switch user, pakai `currentNip()` atau `currentAccount()` untuk mendapatkan user aktif. `loginNip()` yang ada di beberapa module base masih merepresentasikan user awal module context.

Tambahkan role baru ke `UserRole` jika role tersebut sering dipakai:

```java
NEW_ROLE("Role Name From Excel")
```

Untuk role yang jarang dipakai, helper juga menerima string:

```java
useBackOfficeUser("checker", "Role Name From Excel");
```
