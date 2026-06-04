B-Journal Application System

B-Journal adalah sistem manajemen album foto digital terintegrasi yang terdiri dari aplikasi klien Android (Native Kotlin) dan layanan backend API (Next.js & TypeScript). Sistem ini mengimplementasikan arsitektur Multi-User Real-time Isolation yang menjamin keamanan data pada tingkat basis data; setiap pengguna terautentikasi memiliki ruang lingkup (scope) data terisolasi sehingga hanya dapat mengelola aset, foto, dan album miliknya sendiri tanpa risiko kebocoran data antar-pengguna.

Daftar Isi

Deskripsi dan Arsitektur Sistem

Fitur Utama Sistem

Teknologi yang Digunakan

Skema Basis Data (Database Schema)

Struktur dan Alur API Backend

Panduan Instalasi Pengguna Akhir

Panduan Pengembangan Lokal (Developer Guide)

1. Deskripsi dan Arsitektur Sistem

Sistem B-Journal dirancang untuk mengatasi masalah latensi dan sinkronisasi data pada aplikasi galeri berbasis cloud. Komunikasi antara aplikasi Android dan basis data dilakukan secara tidak langsung melalui middleware RESTful API yang dideploy pada infrastruktur serverless.

Ketika pengguna melakukan operasi data (seperti membuat album atau mengunggah foto), aplikasi Android akan mengirimkan permintaan HTTP secara asinkron. Backend API kemudian memvalidasi identitas pengguna (UserID) sebelum mengeksekusi instruksi pada basis data Supabase PostgreSQL dan memanipulasi berkas pada Object Storage.

2. Fitur Utama Sistem

Autentikasi Sesi Terisolasi: Proses registrasi dan autentikasi pengguna menggunakan verifikasi berbasis server. Token sesi dan UserID disimpan secara lokal di perangkat klien menggunakan SharedPreferences.

Isolasi Data Multi-Pengguna: Kueri data pada backend disaring secara ketat berdasarkan parameter pengguna aktif, mencegah akses ilegal terhadap album milik pengguna lain.

Manajemen Konten Komprehensif (CRUD): Fungsionalitas penuh untuk membuat, membaca, memperbarui, dan menghapus data album serta foto secara real-time.

Optimalisasi Unggah Citra Digital: Aplikasi klien mengonversi berkas gambar lokal menjadi format Base64 dengan optimasi bitmap sampling untuk mereduksi ukuran berkas sebelum ditransmisikan via jaringan.

Sinkronisasi Jaringan Asinkron: Menggunakan komponen SwipeRefreshLayout untuk memicu pembaruan data dari server secara dinamis tanpa perlu memuat ulang seluruh antarmuka aplikasi.

3. Teknologi yang Digunakan

Klien Android (Frontend)

Bahasa Pemrograman: Kotlin 1.9+

Komponen Antarmuka: Material Components, RecyclerView (GridLayoutManager), SwipeRefreshLayout

Pustaka Jaringan: Volley HTTP Library (Manajemen antrean permintaan asinkron)

Manajemen Sesi: SharedPreferences

Layanan API (Backend)

Framework: Next.js (API Routes)

Bahasa Pemrograman: TypeScript

Runtime & Hosting: Node.js / Vercel Serverless Environment

Basis Data & Penyimpanan (Cloud)

Mesin Basis Data: Supabase PostgreSQL

Penyimpanan Objek: Supabase Storage Buckets (Penyimpanan citra biner)

4. Skema Basis Data (Database Schema)

Sistem ini beroperasi menggunakan tiga tabel relasional dengan konfigurasi integritas referensial sebagai berikut:

-- 1. Tabel Profil Pengguna
CREATE TABLE user (
    UserID SERIAL PRIMARY KEY,
    Username VARCHAR(50) UNIQUE NOT NULL,
    Password TEXT NOT NULL,
    NamaLengkap VARCHAR(100)
);

-- 2. Tabel Manajemen Album
CREATE TABLE album (
    AlbumID SERIAL PRIMARY KEY,
    NamaAlbum VARCHAR(100) NOT NULL,
    Deskripsi TEXT,
    TanggalDibuat DATE DEFAULT CURRENT_DATE,
    UserID INT REFERENCES user(UserID) ON DELETE CASCADE
);

-- 3. Tabel Repositori Foto
CREATE TABLE foto (
    FotoID SERIAL PRIMARY KEY,
    AlbumID INT REFERENCES album(AlbumID) ON DELETE CASCADE,
    LokasiFile TEXT NOT NULL,
    JudulFoto VARCHAR(100),
    DeskripsiFoto TEXT,
    TanggalUnggah DATE DEFAULT CURRENT_DATE,
    UserID INT REFERENCES user(UserID) ON DELETE CASCADE
);


5. Struktur dan Alur API Backend

Layanan backend API diimplementasikan pada direktori app/api/ dengan pembagian endpoint sebagai berikut:

/api/auth/login & /api/auth/register: Mengelola gerbang masuk dan pembuatan akun pengguna.

/api/dashboard: Menyediakan data agregat album berdasarkan UserID spesifik.

/api/upload: Menangani dekodasi Base64 menjadi buffer biner, proses unggah ke Supabase Storage Bucket, serta pencatatan metadata foto ke tabel database.

Logika Penanganan Unggah (POST /api/upload/route.ts)

Sistem menggunakan pendekatan defensif untuk menangkap variasi parameter UserID yang dikirimkan oleh klien Android guna menghindari kesalahan tipe data pada kompilasi tingkat produksi:

const { AlbumID, ImageBase64, userId, currentUserId, UserID } = body;
// Menggabungkan variasi identitas ke dalam satu variabel valid
const finalId = userId || currentUserId || UserID;

// Eksekusi insert pada Supabase dengan fallback nilai default keamanan
UserID: finalId ? Number(finalId) : 1


6. Panduan Instalasi Pengguna Akhir

Bagi pengguna yang ingin langsung mengoperasikan aplikasi B-Journal di perangkat Android tanpa melakukan kompilasi kode:

Akses halaman repositori GitHub ini melalui peramban.

Lihat pada bilah navigasi kanan, klik pada bagian Releases.

Unduh berkas biner kompilasi terbaru: B-Journal-v1.0.apk.

Pindahkan berkas .apk tersebut ke dalam direktori penyimpanan internal perangkat Android Anda.

Jalankan berkas tersebut dan berikan izin instalasi untuk "Sumber Tidak Dikenal" (Allow installation from unknown sources) jika diminta oleh sistem operasi.

Apabila muncul dialog keamanan dari Google Play Protect, pilih opsi Install Anyway (Peringatan muncul disebabkan paket aplikasi belum ditandatangani dengan sertifikat komersial Google Play Store resmi).

7. Panduan Pengembangan Lokal (Developer Guide)

Prasyarat Lingkungan Kerja

Android Studio Jellyfish (atau versi yang lebih baru)

Android SDK 34 (Android 14)

Java Development Kit (JDK) 17

Langkah-Langkah Klonalisasi dan Sinkronisasi

Buka terminal lokal Anda dan lakukan kloning repositori ini:

git clone https://github.com/Bintangshesshh/B-Journal-Android.git


Jalankan Android Studio, pilih opsi Open, lalu arahkan ke direktori hasil kloning tersebut.

Biarkan sistem melakukan sinkronisasi dependensi melalui Gradle secara penuh. Pastikan perangkat Anda terhubung ke jaringan internet.

Konfigurasikan alamat endpoint API Anda. Sesuaikan variabel konstanta url (Base URL) pada file-file aktivitas berikut agar mengarah ke server lokal atau server produksi Anda sendiri:

app/src/main/java/.../MainActivity.kt

app/src/main/java/.../DashboardActivity.kt

app/src/main/java/.../AddAlbumActivity.kt

app/src/main/java/.../UploadFotoActivity.kt

app/src/main/java/.../EditDeleteAlbumActivity.kt

Hubungkan perangkat keras Android melalui fitur USB Debugging atau aktifkan Android Virtual Device (AVD).

Eksekusi kompilasi kode dengan menekan tombol Run 'app' (Shift + F10) pada Android Studio.
