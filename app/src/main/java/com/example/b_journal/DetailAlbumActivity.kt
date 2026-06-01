package com.example.b_journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
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

    private lateinit var tvTitle: TextView
    private lateinit var tvDesc: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        albumId = intent.getIntExtra("ALBUM_ID", -1)
        val albumOwnerId = intent.getIntExtra("ALBUM_OWNER_ID", -1)
        val albumName = intent.getStringExtra("ALBUM_NAME") ?: "ALBUM"
        val albumDesc = intent.getStringExtra("ALBUM_DESC") ?: ""

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentLoggedInUserId = sharedPref.getInt("USER_ID", -1)

        val isOwner = (currentLoggedInUserId == albumOwnerId || currentLoggedInUserId == -1)

        if (isOwner) {
            setContentView(R.layout.activity_detail_album)

            val btnEditAlbum = findViewById<Button>(R.id.btn_edit_album)
            val fabAddPhoto = findViewById<Button>(R.id.fab_add_photo)

            btnEditAlbum.setOnClickListener {
                val currentTitle = tvTitle.text.toString()
                val currentDesc = tvDesc.text.toString()

                val intent = Intent(this, EditDeleteAlbumActivity::class.java).apply {
                    putExtra("ALBUM_ID", albumId)
                    putExtra("ALBUM_TITLE", currentTitle)
                    putExtra("ALBUM_DESC", currentDesc)
                }
                startActivity(intent)
            }

            fabAddPhoto.setOnClickListener {
                val intent = Intent(this, UploadFotoActivity::class.java).apply {
                    putExtra("ALBUM_ID", albumId)
                }
                startActivity(intent)
            }
        } else {
            setContentView(R.layout.activity_detail_album_viewer)
        }

        tvTitle = findViewById<TextView>(R.id.tv_detail_title)
        tvDesc = findViewById<TextView>(R.id.tv_detail_desc)
        val rvPhotos = findViewById<RecyclerView>(R.id.rv_detail_photos)

        tvTitle.text = albumName.uppercase()
        tvDesc.text = albumDesc

        rvPhotos.layoutManager = GridLayoutManager(this, 2)
        fotoAdapter = DetailFotoAdapter(daftarFotoLocal)
        rvPhotos.adapter = fotoAdapter
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        albumId = intent.getIntExtra("ALBUM_ID", -1)

        val judulBaru = intent.getStringExtra("ALBUM_NAME")
        val deskripsiBaru = intent.getStringExtra("ALBUM_DESC")

        if (!judulBaru.isNullOrEmpty()) {
            tvTitle.text = judulBaru.uppercase()
        }
        if (deskripsiBaru != null) {
            tvDesc.text = deskripsiBaru
        }
    }

    override fun onResume() {
        super.onResume()

        if (albumId != -1) {
            ambilKoleksiFotoDariVercel()
        }
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
                                val namaAlbumTerbaru = item.getString("NamaAlbum")
                                val deskripsiTerbaru = item.getString("Deskripsi")
                                tvTitle.text = namaAlbumTerbaru.uppercase()
                                tvDesc.text = deskripsiTerbaru

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
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error -> error.printStackTrace() }
        )
        Volley.newRequestQueue(this).add(request)
    }
}