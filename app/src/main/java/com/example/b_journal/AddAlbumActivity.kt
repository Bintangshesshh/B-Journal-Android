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

class AddAlbumActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_album)

        val etNewTitle = findViewById<EditText>(R.id.et_new_title)
        val etNewDesc = findViewById<EditText>(R.id.et_new_desc)
        val btnSubmitAlbum = findViewById<Button>(R.id.btn_submit_album)

        btnSubmitAlbum.setOnClickListener {
            val title = etNewTitle.text.toString().trim()
            val desc = etNewDesc.text.toString().trim()

            if (title.isEmpty() || desc.isEmpty()) {
                Toast.makeText(this, "Judul dan Deskripsi gak boleh kosong!", Toast.LENGTH_SHORT).show()
            } else {
                kirimDataKeSupabase(title, desc, btnSubmitAlbum)
            }
        }
    }

    private fun kirimDataKeSupabase(judul: String, deskripsi: String, buttonSubmit: Button) {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        // Efek loading pas submit gaya brutalist
        buttonSubmit.text = "SAVING TO DATABASE..."
        buttonSubmit.isEnabled = false

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
                buttonSubmit.text = "+ Add Album"
                buttonSubmit.isEnabled = true
                try {
                    val success = response.getBoolean("success")
                    if (success) {
                        Toast.makeText(this, "Album Berhasil Dibuat!", Toast.LENGTH_SHORT).show()

                        if (response.has("data")) {
                            val data = response.getJSONObject("data")
                            if (data.has("AlbumID")) {
                                val newAlbumId = data.getInt("AlbumID")

                                val intent = Intent(this, UploadFotoActivity::class.java).apply {
                                    putExtra("NEW_ALBUM_ID", newAlbumId)
                                }
                                startActivity(intent)
                                finish()
                                return@JsonObjectRequest
                            }
                        }
                        finish()

                    } else {
                        Toast.makeText(this, "Gagal membuat album di server!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Respon server bermasalah, tapi album aman!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            },






            { error ->
                buttonSubmit.text = "+ Add Album"
                buttonSubmit.isEnabled = true
                error.printStackTrace()
                Toast.makeText(this, "Gagal menambahkan album!", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}