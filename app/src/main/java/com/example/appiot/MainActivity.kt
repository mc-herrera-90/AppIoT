package com.example.appiot

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.activities.LoginActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.appiot.activities.UserManagementActivity
import com.example.appiot.activities.SensorsActivity
import com.example.appiot.activities.DevelopersActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val welcome = findViewById<TextView>(R.id.tvWelcome)

        val cardUsers = findViewById<LinearLayout>(R.id.cardUsers)
        val cardSensors = findViewById<LinearLayout>(R.id.cardSensors)
        val cardDevelopers = findViewById<LinearLayout>(R.id.cardDevelopers)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        // Mostrar nombre del usuario
        val userName = intent.getStringExtra("user_name")

        if (!userName.isNullOrBlank()) {
            welcome.text = "¡Bienvenido, $userName!"
        }

        // Gestión de usuarios
        cardUsers.setOnClickListener {
            val intent = Intent(this, UserManagementActivity::class.java)
            startActivity(intent)
        }

        // Sensores IoT
        cardSensors.setOnClickListener {
            val intent = Intent(this, SensorsActivity::class.java)
            startActivity(intent)
        }

        // Información de desarrolladores
        cardDevelopers.setOnClickListener {
            val intent = Intent(this, DevelopersActivity::class.java)
            startActivity(intent)
        }

        // Cerrar sesión
        btnLogout.setOnClickListener {
            SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("Cerrar sesión")
                .setContentText("¿Deseas salir de tu cuenta?")
                .setConfirmText("Sí, salir")
                .setCancelText("Cancelar")
                .setConfirmClickListener { dialog ->
                    dialog.dismiss()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                    startActivity(intent)
                    finish()
                }
                .show()
        }
    }

    private fun showMessage(title: String, message: String) {
        SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }
}