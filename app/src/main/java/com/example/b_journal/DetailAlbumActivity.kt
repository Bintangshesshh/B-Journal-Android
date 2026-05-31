package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
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

    // Simpan teks nama dan deskripsi di level class buat jaga-jaga kalau dipanggil tempat lain
    private var albumName: String = "ALBUM"
    private var albumDesc: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_album)

        val tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        val tvDesc = findViewById<TextView>(R.id.tv_detail_desc)
        val rvPhotos = findViewById<RecyclerView>(R.id.rv_detail_photos)
        val btnEditAlbum = findViewById<Button>(R.id.btn_edit_album)
        val fabAddPhoto = findViewById<Button>(R.id.fab_add_photo)

        albumId = intent.getIntExtra("ALBUM_ID", -1)
        albumName = intent.getStringExtra("ALBUM_NAME") ?: "ALBUM"
        albumDesc = intent.getStringExtra("ALBUM_DESC") ?: ""

        tvTitle.text = albumName.uppercase()
        tvDesc.text = albumDesc

        btnEditAlbum.setOnClickListener {
            val intent = Intent(this, EditDeleteAlbumActivity::class.java).apply {
                putExtra("ALBUM_ID", albumId)
                putExtra("ALBUM_TITLE", albumName)
                putExtra("ALBUM_DESC", albumDesc)
            }
            startActivity(intent)
        }

        fabAddPhoto.setOnClickListener {
            val intent = Intent(this, UploadFotoActivity::class.java).apply {
                putExtra("ALBUM_ID", albumId)
            }
            startActivity(intent)
        }

        rvPhotos.layoutManager = GridLayoutManager(this, 2)
        fotoAdapter = DetailFotoAdapter(daftarFotoLocal)
        rvPhotos.adapter = fotoAdapter
    }

    // 🟢 PINDAH KE SINI: Biar kalau abis upload foto, pas balik layarnya langsung nge-refresh
    override fun onResume() {
        super.onResume()
        ambilKoleksiFotoDariVercel()
    }

    private fun ambilKoleksiFotoDariVercel() {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        val request = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        val jsonArray = response.getJSONArray("data")
                        daftarFotoLocal.clear()

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
                                break
                            }
                        }

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
        // 🟢 FIX: Langsung dimasukin ke antrian tanpa bikin variabel penampung baru
        Volley.newRequestQueue(this).add(request)
    }
}