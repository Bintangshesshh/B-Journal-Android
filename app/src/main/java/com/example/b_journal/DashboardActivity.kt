package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class DashboardActivity : AppCompatActivity() {

    private lateinit var rvAlbums: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private var daftarAlbumLocal = ArrayList<Album>()

    // Variabel tunggal untuk antrian Volley agar hemat RAM
    private lateinit var requestQueue: RequestQueue

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // 1. DAFTARIN KOMPONEN DARI XML
        val btnAddAlbum = findViewById<Button>(R.id.fab_add_album)
        rvAlbums = findViewById(R.id.rv_albums)

        // 2. SETUP RECYCLER VIEW (GRID 2 KOLOM)
        rvAlbums.layoutManager = GridLayoutManager(this, 2)
        albumAdapter = AlbumAdapter(daftarAlbumLocal)
        rvAlbums.adapter = albumAdapter

        // 3. INITIALIZE VOLLEY QUEUE (Cuma sekali pas halaman dibuat)
        requestQueue = Volley.newRequestQueue(this)

        // 4. AKSI TOMBOL TAMBAH ALBUM (+)
        btnAddAlbum.setOnClickListener {
            val intent = Intent(this, AddAlbumActivity::class.java)
            startActivity(intent)
        }
    }

    // Refresh data otomatis tiap kali user kembali ke halaman ini
    override fun onResume() {
        super.onResume()
        ambilDataDashboard()
    }

    private fun ambilDataDashboard() {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val jsonArray = response.getJSONArray("data")
                        daftarAlbumLocal.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)

                            val id = item.getInt("AlbumID")
                            val judul = item.getString("NamaAlbum")
                            val deskripsi = item.getString("Deskripsi")
                            val tanggalDibuat = item.optString("TanggalDibuat", "No Date")

                            // 🟢 FIX UTAMA: Bongkar array "foto" untuk ngambil item pertama jadi thumbnail
                            var urlGambar = ""
                            val fotoArray = item.optJSONArray("foto")
                            if (fotoArray != null && fotoArray.length() > 0) {
                                val fotoPertama = fotoArray.getJSONObject(0)
                                urlGambar = fotoPertama.optString("LokasiFile", "")
                            }

                            // Masukkan ke local list dengan URL gambar yang valid dari API
                            daftarAlbumLocal.add(Album(id, judul, deskripsi, tanggalDibuat, urlGambar))
                        }

                        // Kirim data baru ke adapter agar layout diperbarui
                        albumAdapter.masukkanDataBaru(daftarAlbumLocal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memproses data server", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Eror koneksi ke server", Toast.LENGTH_SHORT).show()
            }
        )
        // Masukkan ke antrian tunggal requestQueue
        requestQueue.add(request)
    }
}