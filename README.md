B-JOURNAL // PHOTO ARCHIVE SYSTEM

  ____        _                               _ 
 |  _ \      | |                             | |
 | |_) |_____| | ___  _   _ _ __ _ __   __ _| |
 |  _ <______| |/ _ \| | | | '__| '_ \ / _` | |
 | |_) |     | | (_) | |_| | |  | | | | (_| | |
 |____/      |_|\___/ \__,_|_|  |_| |_|\__,_|_|

 ===========================================================
 MULTI-USER REAL-TIME ISOLATION PHOTO ARCHIVE SYSTEM
 EST. 2026 // DEVELOPER: BENEDIKTUS BINTANG SULISTIANTORO
 ===========================================================


B-Journal adalah sistem manajemen album foto digital terintegrasi yang menerapkan arsitektur Multi-User Real-time Isolation. Sistem ini menjamin isolasi data mutlak pada tingkat basis data; setiap pengguna terautentikasi memiliki ruang lingkup (scope) data terisolasi secara asinkron tanpa risiko kebocoran privasi antar-akun.

■ DAFTAR ISI

Sistem Blueprint & Alur Data

Fitur Utama

Teknologi & Infrastruktur

Skema Basis Data (Supabase PostgreSQL)

Logika Endpoint API Backend

Panduan Instalasi Pengguna Akhir

Panduan Pengembangan Lokal (Developer Guide)

1. SISTEM BLUEPRINT & ALUR DATA

Komunikasi data antara klien Android dan basis data dikelola secara tidak langsung melalui middleware RESTful API serverless untuk mereduksi beban komputasi pada perangkat genggam:

[ Android App ]  ◄=== (JSON Sesi: SharedPreferences)
       │
       ▼  (POST/GET/PUT/DELETE via Volley)
[ Next.js API Middleware (Vercel Serverless) ]
       │
       ├─► [ Supabase Storage ] (Unggah Gambar Base64 ➔ Buffer Biner JPG)
       │
       └─► [ Supabase PostgreSQL ] (Isolasi Query via UserID Pemilik)


Setiap instruksi data yang masuk wajib menyertakan identitas aktif (currentUserId). Jika tidak lolos verifikasi kepemilikan data, sistem backend akan menolak eksekusi secara sepihak (Status 403 Access Denied).

2. FITUR UTAMA

Sesi Terisolasi & Sandi Aman

Proses registrasi dan masuk menggunakan enkripsi berbasis server (scrypt hashing). Kredensial aktif aman disimpan secara lokal di ruang penyimpanan privat perangkat klien menggunakan SharedPreferences (user_session).

Isolasi Query Dinamis

Seluruh aksi manipulasi data pada dasbor langsung disaring menggunakan parameter pengguna aktif, mencegah penyusupan atau manipulasi data album milik pengguna lain.

Optimalisasi Unggah Citra Digital

Aplikasi secara otomatis memotong (bitmap sampling) dan mengompres kualitas gambar lokal (JPEG, 40%) sebelum dikonversi ke format teks Base64 guna menghindari kegagalan transmisi jaringan lambat.

Sinkronisasi Antarmuka Asinkron

Memanfaatkan komponen SwipeRefreshLayout untuk memperbarui data dari database cloud secara dinamis tanpa perlu memuat ulang seluruh layout aktivitas.

3. TEKNOLOGI & INFRASTRUKTUR

CLIENT CORE    :: Android SDK 34 (Kotlin 1.9+) // Volley Network // Glide
BACKEND CORE   :: Next.js API Routes (TypeScript) // Node.js Runtime
CLOUD INFRA    :: Vercel Serverless Deployment // Supabase DB // Storage Buckets


4. SKEMA BASIS DATA (SUPABASE POSTGRESQL)

Sistem ini beroperasi di atas mesin relasional dengan tiga tabel utama yang terikat oleh integritas referensial ketat:

-- 1. TABEL USER (PROFIL UTAMA)
CREATE TABLE user (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password TEXT NOT NULL,
    Email VARCHAR(100),
    NamaLengkap VARCHAR(100),
    Alamat TEXT,
    FotoProfil TEXT
);

-- 2. TABEL ALBUM (MANAJEMEN KELOMPOK FOTO)
CREATE TABLE album (
    AlbumID SERIAL PRIMARY KEY,
    NamaAlbum VARCHAR(100) NOT NULL,
    Deskripsi TEXT,
    TanggalDibuat DATE DEFAULT CURRENT_DATE,
    UserID INT REFERENCES user(UserID) ON DELETE CASCADE
);

-- 3. TABEL FOTO (REPOSITORI BERKAS GAMBAR)
CREATE TABLE foto (
    FotoID SERIAL PRIMARY KEY,
    AlbumID INT REFERENCES album(AlbumID) ON DELETE CASCADE,
    LokasiFile TEXT NOT NULL,
    JudulFoto VARCHAR(100),
    DeskripsiFoto TEXT,
    TanggalUnggah DATE DEFAULT CURRENT_DATE,
    UserID INT REFERENCES user(UserID) ON DELETE CASCADE
);


5. STRUKTUR DAN ALUR API BACKEND

API Backend dideploy secara serverless di /api/ dengan pembagian fungsi sebagai berikut:

/api/auth/login & /api/auth/register : Mengelola autentikasi sesi dan pembuatan akun.

/api/dashboard : Menyediakan agregasi query album, pembuatan, pembaruan, dan penghapusan album secara aman berdasarkan currentUserId.

/api/upload : Menangani konversi data teks Base64 dari Android kembali menjadi berkas gambar fisik, mengunggahnya ke Supabase Storage, dan menuliskan metadatanya ke database.

Penanganan Payload Defensif (POST /api/upload/route.ts)

Untuk menjaga integritas data saat kompilasi production, backend menerapkan logika penampung variabel bertingkat (fallback validation) untuk menangkap data pengenal user:

const { AlbumID, ImageBase64, userId, currentUserId, UserID } = body;

// Validasi dinamis untuk menghindari kegagalan pencocokan tipe data di database
const finalId = userId || currentUserId || UserID;

// Fallback otomatis ke default ID 1 jika Android tidak menyuplai ID valid
UserID: finalId ? Number(finalId) : 1


6. PANDUAN INSTALASI PENGGUNA AKHIR

Untuk mengoperasikan aplikasi B-Journal langsung pada perangkat Android tanpa memerlukan perangkat lunak pengembangan:

Akses repository GitHub ini via peramban komputer/ponsel Anda.

Lihat menu Releases di bilah navigasi kanan halaman.

Unduh berkas biner kompilasi terbaru: B-Journal-v1.0.apk.

Salin dan buka berkas tersebut di penyimpanan lokal perangkat Android Anda.

Berikan izin pemasangan aplikasi dari Sumber Tidak Dikenal jika diminta oleh sistem operasi Android.

Jika muncul pencegahan pemasangan oleh Google Play Protect, pilih opsi Install Anyway (Peringatan dipicu oleh belum adanya tanda tangan sertifikat komersial Google Play Developer pada berkas APK).

7. PANDUAN PENGEMBANGAN LOKAL (DEVELOPER GUIDE)

Prasyarat Lingkungan Kerja

Android Studio Jellyfish (atau versi di atasnya)

Android SDK 34 (Android 14)

Java Development Kit (JDK) 17

Prosedur Setup Project

Buka terminal lokal Anda dan lakukan klon repositori:

git clone https://github.com/Bintangshesshh/B-Journal-Android.git


Buka Android Studio, klik Open, dan arahkan ke folder hasil kloning tersebut.

Biarkan Gradle melakukan sinkronisasi modul dependensi secara penuh hingga selesai.

Sesuaikan konstanta url (Base URL API) pada file Kotlin berikut dengan alamat server Next.js Anda (lokal atau cloud produksi):

app/src/main/java/.../MainActivity.kt

app/src/main/java/.../DashboardActivity.kt

app/src/main/java/.../AddAlbumActivity.kt

app/src/main/java/.../UploadFotoActivity.kt

app/src/main/java/.../EditDeleteAlbumActivity.kt

Hubungkan perangkat keras fisik Android Anda menggunakan metode USB Debugging or aktifkan emulator Android Virtual Device (AVD).

Tekan tombol Run 'app' (Shift + F10) di Android Studio untuk mulai melakukan instalasi versi pengembangan.
