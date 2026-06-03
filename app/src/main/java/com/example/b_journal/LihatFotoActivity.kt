package com.example.b_journal

import android.os.Bundle
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class LihatFotoActivity : AppCompatActivity() {

    private lateinit var ivFullScreen: ImageView
    private lateinit var scaleGestureDetector: ScaleGestureDetector
    private var scaleFactor = 1.0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lihat_foto)

        ivFullScreen = findViewById(R.id.iv_full_screen)
        val urlFotoAsli = intent.getStringExtra("URL_FOTO_ASLI")

        if (!urlFotoAsli.isNullOrEmpty()) {
            Glide.with(this)
                .load(urlFotoAsli)
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivFullScreen)
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

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            scaleGestureDetector.onTouchEvent(event)
        }
        return super.onTouchEvent(event)
    }
}