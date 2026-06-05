package com.example.b_journal

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.AuthFailureError
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var btnAuthenticate: Button

    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)
        setContentView(R.layout.activity_main)

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
                setLoadingState(true)
                prosesLoginKeVercel(username, password)
            }
        }
    }

    private fun prosesLoginKeVercel(user: String, pass: String) {
        val url = "https://b-journal.vercel.app/api/auth/login"

        val dataKirim = JSONObject()
        try {
            dataKirim.put("username", user)
            dataKirim.put("password", pass)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val queue = Volley.newRequestQueue(this)
        val request = object : JsonObjectRequest(
            Request.Method.POST, url, dataKirim,
            { response ->
                setLoadingState(false)
                try {
                    val status = response.getBoolean("success")
                    if (status) {
                        val userObj = response.getJSONObject("user")
                        val namaLengkap = userObj.optString("NamaLengkap", "User")

                        val userIdDariServer = userObj.optInt("UserID", -1)

                        val sharedPref = getSharedPreferences("user_session", Context.MODE_PRIVATE)
                        val editor = sharedPref.edit()
                        editor.putInt("USER_ID", userIdDariServer)
                        editor.apply()

                        Toast.makeText(this, "Halo $namaLengkap!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, DashboardActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val msg = response.optString("message", "Login Gagal!")
                        Toast.makeText(this, "Server: $msg", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Format respon bermasalah!", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                setLoadingState(false)
                error.printStackTrace()

                val responseBody = error.networkResponse?.data?.let { String(it) }
                if (!responseBody.isNullOrEmpty()) {
                    try {
                        val jsonErr = JSONObject(responseBody)
                        val serverMsg = jsonErr.optString("message", "Eror tidak diketahui")
                        Toast.makeText(this, "Eror: $serverMsg", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Koneksi Bermasalah!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Gagal koneksi ke server!", Toast.LENGTH_SHORT).show()
                }
            }
        ) {
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/json; charset=utf-8"
                headers["Accept"] = "application/json"
                return headers
            }
        }

        queue.add(request)
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            btnAuthenticate.text = ""
            progressBar.visibility = View.VISIBLE
            btnAuthenticate.isEnabled = false
        } else {
            btnAuthenticate.text = "AUTHENTICATE"
            progressBar.visibility = View.GONE
            btnAuthenticate.isEnabled = true
        }
    }
}