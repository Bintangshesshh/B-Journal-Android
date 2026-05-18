package com.example.b_journal // PASTIKAN PACKAGE INI SAMA KAYAK PUNYA LU

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

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ID di bawah ini sudah disamakan dengan XML murni milik lu, Bin!
        val etUserId = findViewById<EditText>(R.id.et_user_id)
        val etAccessCode = findViewById<EditText>(R.id.et_access_code)
        val btnAuthenticate = findViewById<Button>(R.id.btn_authenticate) // <-- KUNCI SUKSESNYA DI SINI

        btnAuthenticate.setOnClickListener {
            val username = etUserId.text.toString().trim()
            val password = etAccessCode.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Isi kolom login!", Toast.LENGTH_SHORT).show()
            } else {
                prosesLoginKeVercel(username, password)
            }
        }
    }

    private fun prosesLoginKeVercel(user: String, pass: String) {
        val url = "https://b-journal-34na.vercel.app/api/login"

        val dataKirim = JSONObject()
        try {
            dataKirim.put("Username", user) // U Kapital sesuai database Supabase lu
            dataKirim.put("Password", pass) // P Kapital sesuai database Supabase lu
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.POST, url, dataKirim,
            { response ->
                try {
                    val status = response.getBoolean("success")
                    if (status) {
                        Toast.makeText(this, "Menyala Sombong! Login Sukses!", Toast.LENGTH_SHORT).show()

                        // Pindah ke Dashboard Activity
                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format data dari server salah!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Login Gagal! Akun tidak cocok.", Toast.LENGTH_SHORT).show()
            }
        )

        queue.add(request)
    }
}