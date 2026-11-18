package com.example.appiot.activities

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R
import com.airbnb.lottie.LottieAnimationView

class SplashActivity : AppCompatActivity() {

    private val splashDuration = 5000L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Configurar animación Lottie
        val lottieSplash = findViewById<LottieAnimationView>(R.id.lottieSplash)
        lottieSplash.setSpeed(1.6f)

        // Redirigir al Login después de 5 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }, splashDuration)
    }
}