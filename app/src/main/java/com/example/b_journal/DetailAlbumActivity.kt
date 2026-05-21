package com.example.b_journal

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class DetailAlbumActivity : AppCompatActivity() {

    private var albumId: Int = -1
    private lateinit var fotoAdapter: DetailFotoAdapter
    private val daftarFotoLocal = ArrayList<Foto>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_album)

        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvDesc = findViewById<TextView>(R.id.tv_detail_desc)
        val rvPhotos = findViewById<RecyclerView>(R.id.rv_detail_photos)

        // Tangkap data operan dari Dashboard
        albumId = intent.getIntExtra("ALBUM_ID", -1)
        val albumName = intent.getStringExtra("ALBUM_NAME") ?: "ALBUM"
        val albumDesc = intent.getStringExtra("ALBUM_DESC") ?: ""

        tvTitle.text = albumName.uppercase()
        tvDesc.text = albumDesc

        // Set Recycler View bentuk GRID 2 Kolom
        rvPhotos.layoutManager = GridLayoutManager(this, 2)
        fotoAdapter = DetailFotoAdapter(daftarFotoLocal)
        rvPhotos.adapter = fotoAdapter

        // Tembak data foto dari Vercel
        ambilKoleksiFotoDariVercel()
    }

    private fun ambilKoleksiFotoDariVercel() {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val jsonArray = response.getJSONArray("data")
                        daftarFotoLocal.clear()

                        // Cari album yang ID-nya COCOK sama yang diklik
                        for (i in 0 until jsonArray.length()) {
                            val item = jsonArray.getJSONObject(i)
                            val currentAlbumId = item.getInt("AlbumID")

                            if (currentAlbumId == albumId) {
                                val fotoArray = item.optJSONArray("foto")
                                if (fotoArray != null) {
                                    for (j in 0 until fotoArray.length()) {
                                        val objFoto = fotoArray.getJSONObject(j)
                                        val urlFoto = objFoto.optString("LokasiFile", "")
                                        daftarFotoLocal.add(Foto(urlFoto))
                                    }
                                }
                                break // Kalau udah ketemu langsung stop loop
                            }
                        }

                        // Update isi grid foto
                        fotoAdapter.updateData(daftarFotoLocal)

                        if (daftarFotoLocal.isEmpty()) {
                            Toast.makeText(this, "Album ini belum memiliki foto!", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal memuat foto!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Eror koneksi ke server!", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}