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
                Toast.makeText(this, "Judul dan Deskripsi gak boleh kosong, Bin!", Toast.LENGTH_SHORT).show()
            } else {
                kirimDataKeSupabase(title, desc, btnSubmitAlbum)
            }
        }
    }

    private fun kirimDataKeSupabase(judul: String, deskripsi: String, buttonSubmit: Button) {
        val url = "https://b-journal-34na.vercel.app/api/dashboard"

        // Efek loading brutalist pas submit
        buttonSubmit.text = "SAVING TO SUPABASE..."
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
                        Toast.makeText(this, "Album Sukses Dibuat!", Toast.LENGTH_SHORT).show()

                        // Tutup halaman ini dan balik ke Dashboard otomatis
                        setResult(RESULT_OK)
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
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