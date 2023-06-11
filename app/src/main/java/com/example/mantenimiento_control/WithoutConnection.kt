package com.example.mantenimiento_control

import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class WithoutConnection : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_without_connection)

        val try_again_button = findViewById<Button>(R.id.try_again_button)

        try_again_button.setOnClickListener {
            checkInternet()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)

        return (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))

    }

    private fun checkInternet() {
        val bundle = intent.extras
        val email = bundle?.getString("email")
        val provider = bundle?.getString("provider")

        if (isNetworkAvailable()) {
            val intent = Intent(this,  MainActivity::class.java).apply {
                putExtra("email",email)
                putExtra("provider",provider)
            }
            startActivity(intent)
            finish()
        } else{
            Toast.makeText(this, "Sigues sin conexión a Internet.", Toast.LENGTH_SHORT).show()
        }
    }
}