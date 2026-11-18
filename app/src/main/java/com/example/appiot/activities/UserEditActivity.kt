package com.example.appiot.activities

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R
import cn.pedant.SweetAlert.SweetAlertDialog
import org.json.JSONArray

class UserEditActivity : AppCompatActivity() {

    private val preferencesName = "SYNTRA_USERS"
    private val usersKey = "users"

    private lateinit var firstName: EditText
    private lateinit var lastName: EditText
    private lateinit var email: EditText

    private var originalEmail = ""
    private var originalFirstName = ""
    private var originalLastName = ""
    private var originalEmailInput = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        firstName = findViewById(R.id.etEditFirstName)
        lastName = findViewById(R.id.etEditLastName)
        email = findViewById(R.id.etEditEmail)

        // Filtro para bloquear números y caracteres especiales
        val letterFilter = android.text.InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                if (!Character.isLetter(char) && !Character.isSpaceChar(char)) {
                    return@InputFilter ""
                }
            }
            null
        }

        firstName.filters = arrayOf(letterFilter)
        lastName.filters = arrayOf(letterFilter)

        val btnSave = findViewById<Button>(R.id.btnSaveChanges)
        val btnDelete = findViewById<Button>(R.id.btnDeleteUser)

        originalEmail = intent.getStringExtra("user_email") ?: ""

        if (originalEmail.isBlank() || !loadUser()) {
            SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("Usuario no encontrado")
                .setContentText("No fue posible cargar los datos del usuario.")
                .setConfirmClickListener {
                    it.dismiss()
                    finish()
                }
                .show()
            return
        }

        btnSave.setOnClickListener {
            saveChanges()
        }

        btnDelete.setOnClickListener {
            confirmDelete()
        }
    }

    private fun loadUser(): Boolean {
        val users = getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)

            if (user.optString("email").equals(originalEmail, true)) {
                originalFirstName = user.optString("firstName")
                originalLastName = user.optString("lastName")
                originalEmailInput = user.optString("email")

                firstName.setText(originalFirstName)
                lastName.setText(originalLastName)
                email.setText(originalEmailInput)
                return true
            }
        }
        return false
    }

    // Función para normalizar nombres
    private fun formatName(input: String): String {
        return input.trim().lowercase().split("\\s+".toRegex()).joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    private fun saveChanges() {
        val rawName = firstName.text.toString()
        val rawSurname = lastName.text.toString()

        // Sanitizamos y formateamos antes de guardar o comparar
        val name = formatName(rawName)
        val surname = formatName(rawSurname)
        val newEmail = email.text.toString().trim().lowercase()

        when {
            // Comparación mejorada considerando el formato
            name == formatName(originalFirstName) &&
                    surname == formatName(originalLastName) &&
                    newEmail == originalEmailInput.lowercase() -> {
                showAlert(
                    SweetAlertDialog.WARNING_TYPE,
                    "Sin cambios",
                    "No se han detectado modificaciones en los datos del usuario."
                )
            }

            name.length < 2 || surname.length < 2 -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Datos inválidos",
                    "Ingresa un nombre y apellido válidos."
                )
            }

            !Patterns.EMAIL_ADDRESS.matcher(newEmail).matches() -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Correo inválido",
                    "Ingresa una dirección de correo válida."
                )
            }

            emailAlreadyUsed(newEmail) -> {
                showAlert(
                    SweetAlertDialog.WARNING_TYPE,
                    "Correo duplicado",
                    "Otro usuario ya utiliza ese correo."
                )
            }

            else -> {
                val users = getUsers()

                for (i in 0 until users.length()) {
                    val user = users.getJSONObject(i)

                    if (user.optString("email").equals(originalEmail, true)) {
                        user.put("firstName", name)
                        user.put("lastName", surname)
                        user.put("email", newEmail)
                        break
                    }
                }

                saveUsers(users)

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Cambios guardados")
                    .setContentText("Los datos del usuario se actualizaron correctamente.")
                    .setConfirmClickListener {
                        it.dismiss()
                        finish()
                    }
                    .show()
            }
        }
    }

    private fun confirmDelete() {
        SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
            .setTitleText("Eliminar usuario")
            .setContentText("Esta acción no se puede deshacer. ¿Deseas continuar?")
            .setCancelText("Cancelar")
            .setConfirmText("Eliminar")
            .setConfirmClickListener {
                it.dismiss()
                deleteUser()
            }
            .show()
    }

    private fun deleteUser() {
        val users = getUsers()
        val updatedUsers = JSONArray()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)

            if (!user.optString("email").equals(originalEmail, true)) {
                updatedUsers.put(user)
            }
        }

        saveUsers(updatedUsers)

        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Usuario eliminado")
            .setContentText("El registro fue eliminado correctamente.")
            .setConfirmClickListener {
                it.dismiss()
                finish()
            }
            .show()
    }

    private fun emailAlreadyUsed(value: String): Boolean {
        val users = getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            val savedEmail = user.optString("email")

            if (savedEmail.equals(value, true) && !savedEmail.equals(originalEmail, true)) {
                return true
            }
        }
        return false
    }

    private fun getUsers(): JSONArray {
        val preferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        return JSONArray(preferences.getString(usersKey, "[]"))
    }

    private fun saveUsers(users: JSONArray) {
        getSharedPreferences(preferencesName, MODE_PRIVATE)
            .edit()
            .putString(usersKey, users.toString())
            .apply()
    }

    private fun showAlert(type: Int, title: String, message: String) {
        SweetAlertDialog(this, type)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }
}