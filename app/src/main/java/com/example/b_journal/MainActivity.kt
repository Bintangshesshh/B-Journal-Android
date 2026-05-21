package com.example.b_journal

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    // Deklarasikan di atas biar bisa diakses di semua fungsi dalam class ini
    private lateinit var progressBar: ProgressBar
    private lateinit var btnAuthenticate: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inisialisasi komponen UI dari XML
        val etUserId = findViewById<EditText>(R.id.et_user_id)
        val etAccessCode = findViewById<EditText>(R.id.et_access_code)
        btnAuthenticate = findViewById<Button>(R.id.btn_authenticate)
        progressBar = findViewById<ProgressBar>(R.id.progress_loading)

        btnAuthenticate.setOnClickListener {
            val username = etUserId.text.toString().trim()
            val password = etAccessCode.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi kolom login!", Toast.LENGTH_SHORT).show()
            } else {
                // Aktifkan loading pas tombol diklik
                setLoadingState(true)
                prosesLoginKeVercel(username, password)
            }
        }
    }

    private fun prosesLoginKeVercel(user: String, pass: String) {
        val url = "https://b-journal-34na.vercel.app/api/login"

        val dataKirim = JSONObject()
        try {
            dataKirim.put("Username", user) // U Kapital sesuai skema DB Supabase lu
            dataKirim.put("Password", pass) // P Kapital sesuai skema DB Supabase lu
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.POST, url, dataKirim,
            { response ->
                // Matikan loading karena data udah dapet balasan
                setLoadingState(false)
                try {
                    val status = response.getBoolean("success")
                    if (status) {
                        Toast.makeText(this, "Menyala Sombong! Login Sukses!", Toast.LENGTH_SHORT).show()

                        // Pindah ke Dashboard Activity
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(this, "Login Gagal! Akun salah.", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format data dari server salah!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                // Matikan loading meskipun koneksinya error
                setLoadingState(false)
                error.printStackTrace()
                Toast.makeText(this, "Login Gagal! Masalah jaringan/akun salah.", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }

    // Fungsi pembantu buat atur visual pas lagi loading biar gak nulis kode berulang
    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            btnAuthenticate.text = "" // Kosongkan teks biar gak tabrakan sama progress bar
            progressBar.visibility = View.VISIBLE
            btnAuthenticate.isEnabled = false // Biar gak bisa diklik berkali-kali pas loading
        } else {
            btnAuthenticate.text = "AUTHENTICATE"
            progressBar.visibility = View.GONE
            btnAuthenticate.isEnabled = true
        }
    }
}