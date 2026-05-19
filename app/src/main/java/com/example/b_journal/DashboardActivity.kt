package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    private lateinit var albumAdapter: AlbumAdapter
    private val daftarAlbumLocal = ArrayList<Album>()
    // 1. TAMBAH VARIABEL SWIPE REFRESH DI SINI BIN
    private lateinit var swipeRefresh: SwipeRefreshLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val etAlbumTitle = findViewById<EditText>(R.id.et_album_title)
        val etAlbumDesc = findViewById<EditText>(R.id.et_album_desc)
        val btnAddAlbum = findViewById<Button>(R.id.btn_add_album)
        val btnLogout = findViewById<Button>(R.id.btn_logout)

        // 2. INSIALISASI SWIPE REFRESH-NYA
        swipeRefresh = findViewById(R.id.swipe_refresh)

        // Inisialisasi RecyclerView beserta Layout Manager-nya
        val rvAlbums = findViewById<RecyclerView>(R.id.rv_albums)
        rvAlbums.layoutManager = LinearLayoutManager(this)
        albumAdapter = AlbumAdapter(daftarAlbumLocal)
        rvAlbums.adapter = albumAdapter

        // Sedot data dari Supabase pas halaman kebuka pertama kali
        ambilDataFeedDariVercel()

        // 3. PASANG LOGIC PAS LAYAR DITARIK KE BAWAH
        swipeRefresh.setOnRefreshListener {
            ambilDataFeedDariVercel() // Panggil fungsi ambil data lagi
        }

        btnAddAlbum.setOnClickListener {
            val title = etAlbumTitle.text.toString().trim()
            val desc = etAlbumDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Judul dan Deskripsi wajib diisi!", Toast.LENGTH_SHORT).show()
            } else {
                btnAddAlbum.isEnabled = false
                simpanAlbumKeSupabase(title, desc, etAlbumTitle, etAlbumDesc, btnAddAlbum)
            }
        }

        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun ambilDataFeedDariVercel() {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                // 4. MATIKAN LOADING MUTING NYA PAS DATA BERHASIL KESEDOT
                swipeRefresh.isRefreshing = false
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val jsonArray = response.getJSONArray("data")
                        daftarAlbumLocal.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val album = Album(
                                id = item.getInt("AlbumID"),
                                namaAlbum = item.getString("NamaAlbum"),
                                deskripsi = item.getString("Deskripsi"),
                                tanggalDibuat = item.getString("TanggalDibuat")
                            )
                            daftarAlbumLocal.add(album)
                        }
                        albumAdapter.masukkanDataBaru(daftarAlbumLocal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal mengurai data JSON!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // 5. MATIKAN JUGA LOADINGNYA KALAU KONEKSI EROR
                swipeRefresh.isRefreshing = false
                error.printStackTrace()
                Toast.makeText(this, "Gagal memuat feed album dari server!", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }

    private fun simpanAlbumKeSupabase(
        judul: String,
        deskripsi: String,
        inputTitle: EditText,
        inputDesc: EditText,
        buttonAdd: Button
    ) {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        buttonAdd.text = "SAVING TO DATABASE..."
        buttonAdd.isEnabled = false

        val dataKirim = JSONObject()
        try {
            dataKirim.put("title", judul)
            dataKirim.put("description", deskripsi)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.POST, url, dataKirim,
            { response ->
                buttonAdd.text = "+ Add Album"
                buttonAdd.isEnabled = true
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        Toast.makeText(this, "Album Sukses Dibuat!", Toast.LENGTH_SHORT).show()
                        inputTitle.text.clear()
                        inputDesc.text.clear()

                        ambilDataFeedDariVercel()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                buttonAdd.text = "+ Add Album"
                buttonAdd.isEnabled = true
                error.printStackTrace()
                Toast.makeText(this, "Gagal menambahkan album!", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}