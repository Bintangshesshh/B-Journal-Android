package com.example.b_journal

import android.content.Context
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
    private lateinit var btnUpload: Button
    private var imageBase64: String? = null
    private var albumId: Int = -1

    private val pickImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
        btnUpload = findViewById<Button>(R.id.btn_upload)

        albumId = intent.getIntExtra("ALBUM_ID", -1)

        if (albumId == -1) {
            Toast.makeText(this, "Eror: ID Album bermasalah!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        btnPilih.setOnClickListener { pickImage.launch("image/*") }

        btnUpload.setOnClickListener {
            if (imageBase64 != null) {
                btnUpload.isEnabled = false
                btnUpload.text = "Uploading... Please Wait"

                uploadFotoKeVercel()
            } else {
                Toast.makeText(this, "Pilih foto dulu!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun convertUriToBase64(uri: Uri): String {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inSampleSize = 2
            }
            val bitmap = BitmapFactory.decodeStream(inputStream, null, options)
            val outputStream = ByteArrayOutputStream()

            bitmap?.compress(Bitmap.CompressFormat.JPEG, 40, outputStream)
            val bytes = outputStream.toByteArray()

            return Base64.encodeToString(bytes, Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    private fun uploadFotoKeVercel() {
        val url = "https://b-journal-34na.vercel.app/api/upload"

        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
        val currentUserId = sharedPref.getInt("USER_ID", -1)

        val params = JSONObject()
        try {
            params.put("AlbumID", albumId)
            params.put("ImageBase64", imageBase64)
            params.put("UserID", currentUserId)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val queue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, params,
            { response ->
                btnUpload.isEnabled = true
                btnUpload.text = "EXECUTE_UPLOAD_PUSH"

                Toast.makeText(this, "Foto Berhasil Diupload!", Toast.LENGTH_SHORT).show()
                finish()
            },
            { error ->
                btnUpload.isEnabled = true
                btnUpload.text = "EXECUTE_UPLOAD_PUSH"

                error.printStackTrace()
                val responseBody = error.networkResponse?.data?.let { String(it) }
                if (!responseBody.isNullOrEmpty()) {
                    Toast.makeText(this, "Server Error: $responseBody", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Gagal Upload: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json; charset=utf-8"
                return headers
            }
        }
        queue.add(request)
    }
}