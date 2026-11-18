package com.example.appiot.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R

class UserManagementActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_management)

        val btnAddUser = findViewById<Button>(R.id.btnAddUser)
        val btnListUsers = findViewById<Button>(R.id.btnListUsers)

        // Abrir formulario de registro en "Modo CRUD"
        btnAddUser.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.putExtra("from_crud", true) // ¡Aquí enviamos la señal clave!
            startActivity(intent)
        }

        // Abrir listado de usuarios
        btnListUsers.setOnClickListener {
            startActivity(
                Intent(this, UserListActivity::class.java)
            )
        }
    }
}