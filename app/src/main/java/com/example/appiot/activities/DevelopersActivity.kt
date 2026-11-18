package com.example.appiot.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R

class DevelopersActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_developers)

        // Elementos del perfil
        val marco = findViewById<ImageView>(R.id.ivMarco)
        val btnGithubMarco = findViewById<Button>(R.id.btnGithubMarco)

        // Cargar fotografía
        marco.setImageResource(R.drawable.marco)

        // Enlace de GitHub
        btnGithubMarco.setOnClickListener {
            val url = "https://github.com/mc-herrera-90"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        }
    }
}
