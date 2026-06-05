package com.example.b_journal

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bumptech.glide.Glide
import java.net.URLEncoder

class LihatFotoActivity : AppCompatActivity() {

    private lateinit var ivFullScreen: ImageView
    private lateinit var btnDeleteFoto: ImageButton
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f
    private var urlFotoAsli: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lihat_foto)

        ivFullScreen = findViewById(R.id.iv_full_screen)
        btnDeleteFoto = findViewById(R.id.btn_delete_foto)

        urlFotoAsli = intent.getStringExtra("URL_FOTO_ASLI")

        if (!urlFotoAsli.isNullOrEmpty()) {
            Glide.with(this)
                .load(urlFotoAsli)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivFullScreen)
        }

        btnDeleteFoto.setOnClickListener {
            if (!urlFotoAsli.isNullOrEmpty()) {
                hapusFotoDariServer()
            } else {
                Toast.makeText(this, "URL Foto tidak valid!", Toast.LENGTH_SHORT).show()
            }
        }

        scaleGestureDetector = ScaleGestureDetector(this, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = Math.max(1.0f, Math.min(scaleFactor, 5.0f))
                ivFullScreen.scaleX = scaleFactor
                ivFullScreen.scaleY = scaleFactor
                return true
            }
        })
    }

    private fun hapusFotoDariServer() {
        try {
            val encodedUrl = URLEncoder.encode(urlFotoAsli, "UTF-8")
            val url = "https://b-journal.vercel.app/api/upload?urlFoto=$encodedUrl"

            val request = JsonObjectRequest(
                Request.Method.DELETE, url, null,
                { response ->
                    Toast.makeText(this, "Foto berhasil dihapus!", Toast.LENGTH_SHORT).show()
                    finish()
                },
                { error ->
                    error.printStackTrace()
                    Toast.makeText(this, "Gagal menghapus foto dari server", Toast.LENGTH_SHORT).show()
                }
            )
            Volley.newRequestQueue(this).add(request)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Eror encoding data!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) scaleGestureDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }
}