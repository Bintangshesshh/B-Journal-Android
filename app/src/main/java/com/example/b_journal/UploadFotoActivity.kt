package com.example.b_journal

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UploadFotoActivity : AppCompatActivity() {

    private lateinit var ivPreview: ImageView
    private var imageBase64: String? = null
    private var albumId: Int = -1

    // 1. Logic buat milih foto dari galeri
    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            ivPreview.setImageURI(it)
            imageBase64 = convertUriToBase64(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_foto)

        ivPreview = findViewById(R.id.iv_preview)
        val btnPilih = findViewById<Button>(R.id.btn_pilih_foto)
        val btnUpload = findViewById<Button>(R.id.btn_upload)

        // Tangkap AlbumID yang dilempar dari AddAlbumActivity
        albumId = intent.getIntExtra("NEW_ALBUM_ID", -1)

        btnPilih.setOnClickListener { pickImage.launch("image/*") }

        btnUpload.setOnClickListener {
            if (imageBase64 != null) {
                uploadFotoKeVercel()
            } else {
                Toast.makeText(this, "Pilih foto dulu cok!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertUriToBase64(uri: Uri): String {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        val bitmap = BitmapFactory.decodeStream(inputStream)
        val outputStream = ByteArrayOutputStream()
        // Kompres dikit biar gak kegedean pas dikirim ke API
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, outputStream)
        val bytes = outputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private fun uploadFotoKeVercel() {
        val url = "https://b-journal-34na.vercel.app/api/upload" // Sesuaikan nanti di Web
        val params = JSONObject()
        params.put("AlbumID", albumId)
        params.put("ImageBase64", imageBase64)

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(Request.Method.POST, url, params,
            { response ->
                Toast.makeText(this, "Foto Berhasil Diupload!", Toast.LENGTH_SHORT).show()
                finish() // Balik ke Dashboard
            },
            { error ->
                Toast.makeText(this, "Gagal Upload: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(request)
    }
}