package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DashboardActivity : AppCompatActivity() {

    private lateinit var albumAdapter: AlbumAdapter
    private val daftarAlbumLocal = ArrayList<Album>()
    private lateinit var swipeRefresh: SwipeRefreshLayout

    // Penangkap sinyal pas balik dari halaman AddAlbum biar otomatis refresh data
    private val launcherAddAlbum = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            ambilDataFeedDariVercel() // Auto refresh feed pas kelar nambah data!
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val btnLogout = findViewById<Button>(R.id.btn_logout)
        val fabAddAlbum = findViewById<FloatingActionButton>(R.id.fab_add_album)
        swipeRefresh = findViewById(R.id.swipe_refresh)

        val rvAlbums = findViewById<RecyclerView>(R.id.rv_albums)
        rvAlbums.layoutManager = LinearLayoutManager(this)
        albumAdapter = AlbumAdapter(daftarAlbumLocal)
        rvAlbums.adapter = albumAdapter

        // Ambil data pas pertama masuk
        ambilDataFeedDariVercel()

        // Logic ditarik ke bawah
        swipeRefresh.setOnRefreshListener {
            ambilDataFeedDariVercel()
        }

        // Pindah ke halaman input pas di klik
        fabAddAlbum.setOnClickListener {
            val intent = Intent(this, AddAlbumActivity::class.java)
            launcherAddAlbum.launch(intent)
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
                swipeRefresh.isRefreshing = false
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val jsonArray = response.getJSONArray("data")
                        daftarAlbumLocal.clear()

                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)

                            // 1. AMBIL ARRAY "foto" DARI JSON hasil join
                            val fotoArray = item.optJSONArray("foto")

                            // 2. AMBIL URL FOTO PERTAMA JIKA ADA
                            val fotoUrl = if (fotoArray != null && fotoArray.length() > 0) {
                                fotoArray.getJSONObject(0).optString("LokasiFile", "")
                            } else {
                                ""
                            }

                            val album = Album(
                                id = item.getInt("AlbumID"),
                                namaAlbum = item.getString("NamaAlbum"),
                                deskripsi = item.getString("Deskripsi"),
                                tanggalDibuat = item.getString("TanggalDibuat"),
                                urlGambar = fotoUrl
                            )
                            daftarAlbumLocal.add(album)
                        }

                        albumAdapter.masukkanDataBaru(daftarAlbumLocal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal mengurai data!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                swipeRefresh.isRefreshing = false
                error.printStackTrace()
                Toast.makeText(this, "Gagal memuat feed!", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}