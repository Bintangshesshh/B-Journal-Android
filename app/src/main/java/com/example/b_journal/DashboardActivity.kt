package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // Inisialisasi komponen input sesuai ID di layout baru
        val etAlbumTitle = findViewById<EditText>(R.id.et_album_title)
        val etAlbumDesc = findViewById<EditText>(R.id.et_album_desc)
        val btnAddAlbum = findViewById<Button>(R.id.btn_add_album)
        val btnLogout = findViewById<Button>(R.id.btn_logout)

        // Aksi tombol tambah album / submit
        btnAddAlbum.setOnClickListener {
            val title = etAlbumTitle.text.toString().trim()
            val desc = etAlbumDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Judul dan Deskripsi wajib diisi, Bin!", Toast.LENGTH_SHORT).show()
            } else {
                simpanAlbumKeSupabase(title, desc, etAlbumTitle, etAlbumDesc)
            }
        }

        // Aksi tombol logout kembali ke login screen
        btnLogout.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun simpanAlbumKeSupabase(judul: String, deskripsi: String, inputTitle: EditText, inputDesc: EditText) {
        val url = "https://b-journal-34na.vercel.app/api/albums"

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
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        Toast.makeText(this, "Album Sukses Dibuat!", Toast.LENGTH_SHORT).show()
                        // Bersihkan form setelah data masuk database
                        inputTitle.text.clear()
                        inputDesc.text.clear()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Gagal menambahkan album!", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }
}