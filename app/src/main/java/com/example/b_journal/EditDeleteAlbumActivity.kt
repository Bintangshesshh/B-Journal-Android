package com.example.b_journal

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class EditDeleteAlbumActivity : AppCompatActivity() {

    private var albumId: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_delete_album)

        val etEditTitle = findViewById<EditText>(R.id.et_edit_title)
        val etEditDesc = findViewById<EditText>(R.id.et_edit_desc)
        val btnUpdateAlbum = findViewById<Button>(R.id.btn_update_album)
        val btnDeleteAlbum = findViewById<Button>(R.id.btn_delete_album)

        albumId = intent.getIntExtra("ALBUM_ID", 0)
        etEditTitle.setText(intent.getStringExtra("ALBUM_TITLE"))
        etEditDesc.getStringExtra("ALBUM_DESC")?.let { etEditDesc.setText(it) }

        btnUpdateAlbum.setOnClickListener {
            val title = etEditTitle.text.toString().trim()
            val desc = etEditDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Kolom edit tidak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                eksekusiUpdate(title, desc)
            }
        }

        btnDeleteAlbum.setOnClickListener {
            eksekusiDelete()
        }
    }

    private fun eksekusiUpdate(judulBaru: String, deskripsiBaru: String) {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"
        val queue = Volley.newRequestQueue(this)

        val dataKirim = JSONObject().apply {
            put("albumId", albumId)
            put("title", judulBaru)
            put("description", deskripsiBaru)
        }

        val request = JsonObjectRequest(
            Request.Method.PUT, url, dataKirim,
            { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Album Berhasil Diubah!", Toast.LENGTH_SHORT).show()
                    finish() // Kembali ke dashboard dengan sukses
                }
            },
            { Toast.makeText(this, "Gagal meng-update album!", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }

    private fun eksekusiDelete() {
        val url = "https://b-journal-34na.vercel.app/api/dashboard?albumId=$albumId"
        val queue = Volley.newRequestQueue(this)

        val request = JsonObjectRequest(
            Request.Method.DELETE, url, null,
            { response ->
                if (response.getBoolean("success")) {
                    Toast.makeText(this, "Album Telah Dihapus!", Toast.LENGTH_SHORT).show()
                    finish() // Tutup halaman, otomatis terhapus dari list dashboard
                }
            },
            { Toast.makeText(this, "Gagal menghapus album!", Toast.LENGTH_SHORT).show() }
        )
        queue.add(request)
    }
}