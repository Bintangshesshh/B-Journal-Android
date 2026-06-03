package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DetailAlbumActivity : AppCompatActivity() {

    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvGalleryLabel: TextView
    private lateinit var rvDetailPhotos: RecyclerView
    private lateinit var layoutKosong: LinearLayout

    private lateinit var detailFotoAdapter: DetailFotoAdapter
    private var daftarFotoLocal = ArrayList<Foto>()
    private var albumId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail_album)

        tvTitle = findViewById(R.id.tv_detail_title)
        tvDesc = findViewById(R.id.tv_detail_desc)
        tvGalleryLabel = findViewById(R.id.tv_gallery_label)
        rvDetailPhotos = findViewById(R.id.rv_detail_photos)
        layoutKosong = findViewById(R.id.layout_kosong)

        val btnEdit = findViewById<Button>(R.id.btn_edit_album)
        val fabAddPhoto = findViewById<Button>(R.id.fab_add_photo)

        albumId = intent.getIntExtra("ALBUM_ID", -1)
        val namaAlbum = intent.getStringExtra("NAMA_ALBUM")
        val deskripsi = intent.getStringExtra("DESKRIPSI")

        tvTitle.text = namaAlbum ?: "NAMA ALBUM"
        tvDesc.text = deskripsi ?: "Tidak ada deskripsi archive."

        rvDetailPhotos.layoutManager = GridLayoutManager(this, 3)
        detailFotoAdapter = DetailFotoAdapter(daftarFotoLocal)
        rvDetailPhotos.adapter = detailFotoAdapter

        fabAddPhoto.setOnClickListener {
            val intent = Intent(this, UploadFotoActivity::class.java).apply {
                putExtra("ALBUM_ID", albumId)
            }
            startActivity(intent)
        }

        btnEdit.setOnClickListener {
            val intent = Intent(this, EditDeleteAlbumActivity::class.java).apply {
                putExtra("ALBUM_ID", albumId)
                putExtra("NAMA_ALBUM", tvTitle.text.toString())
                putExtra("DESKRIPSI", tvDesc.text.toString())
            }
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        if (albumId != -1) {
            ambilDataFotoAlbum()
        }
    }

    private fun ambilDataFotoAlbum() {
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
                            val albumObj = jsonArray.getJSONObject(i)
                            if (albumObj.getInt("AlbumID") == albumId) {

                                tvTitle.text = albumObj.optString("NamaAlbum", tvTitle.text.toString())
                                tvDesc.text = albumObj.optString("Deskripsi", tvDesc.text.toString())

                                val fotoArray = albumObj.optJSONArray("foto")
                                if (fotoArray != null) {
                                    for (j in 0 until fotoArray.length()) {
                                        val fotoObj = fotoArray.getJSONObject(j)
                                        val lokasi = fotoObj.optString("LokasiFile", "")

                                        if (lokasi.isNotEmpty()) {
                                            daftarFotoLocal.add(Foto(lokasi))
                                        }
                                    }
                                }
                                break
                            }
                        }

                        if (daftarFotoLocal.isEmpty()) {
                            layoutKosong.visibility = View.VISIBLE
                            rvDetailPhotos.visibility = View.GONE
                            tvGalleryLabel.visibility = View.GONE
                        } else {
                            layoutKosong.visibility = View.GONE
                            rvDetailPhotos.visibility = View.VISIBLE
                            tvGalleryLabel.visibility = View.VISIBLE
                        }

                        detailFotoAdapter.updateData(daftarFotoLocal)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal mengolah data foto!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Gagal terhubung ke server!", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(this).add(request)
    }
}