package com.example.b_journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class DashboardActivity : AppCompatActivity() {

    private lateinit var rvAlbums: RecyclerView
    private lateinit var albumAdapter: AlbumAdapter
    private var daftarAlbumLocal = ArrayList<Album>()
    private lateinit var requestQueue: RequestQueue

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_dashboard)

        val btnAddAlbum = findViewById<Button>(R.id.fab_add_album)
        val btnExit = findViewById<Button>(R.id.btn_logout)
        rvAlbums = findViewById(R.id.rv_albums)

        swipeRefreshLayout = findViewById(R.id.swipe_refresh)

        rvAlbums.layoutManager = GridLayoutManager(this, 2)
        albumAdapter = AlbumAdapter(daftarAlbumLocal)
        rvAlbums.adapter = albumAdapter

        requestQueue = Volley.newRequestQueue(this)

        btnAddAlbum.setOnClickListener {
            val intent = Intent(this, AddAlbumActivity::class.java)
            startActivity(intent)
        }

        btnExit.setOnClickListener {
            val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.clear()
            editor.apply()

            Toast.makeText(this, "Session dihapus, berhasil keluar!", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
        }

        swipeRefreshLayout.setOnRefreshListener {
            ambilDataDashboard()
        }
    }

    override fun onResume() {
        super.onResume()
        ambilDataDashboard()
    }

    private fun ambilDataDashboard() {
        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("USER_ID", -1)

        val url = "https://b-journal-34na.vercel.app/api/dashboard?currentUserId=$currentUserId"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                swipeRefreshLayout.isRefreshing = false

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
                            val albumOwnerId = item.getInt("UserID")

                            var urlGambar = ""
                            val fotoArray = item.optJSONArray("foto")
                            if (fotoArray != null && fotoArray.length() > 0) {
                                val fotoPertama = fotoArray.getJSONObject(0)
                                urlGambar = fotoPertama.optString("LokasiFile", "")
                            }

                            daftarAlbumLocal.add(Album(id, judul, deskripsi, tanggalDibuat, urlGambar, albumOwnerId))
                        }
                        albumAdapter.masukkanDataBaru(daftarAlbumLocal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memproses data server", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                swipeRefreshLayout.isRefreshing = false
                error.printStackTrace()
                Toast.makeText(this, "Eror koneksi ke server", Toast.LENGTH_SHORT).show()
            }
        )
        requestQueue.add(request)
    }
}