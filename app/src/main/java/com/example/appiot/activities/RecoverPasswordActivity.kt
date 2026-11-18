package com.example.appiot.activities

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import cn.pedant.SweetAlert.SweetAlertDialog
import com.example.appiot.R
import org.json.JSONArray

class RecoverPasswordActivity : AppCompatActivity() {

    companion object {
        private const val SIMULATED_CODE = "12345"
        private const val PREFERENCES_NAME = "SYNTRA_USERS"
        private const val USERS_KEY = "users"
    }

    private lateinit var email: EditText
    private lateinit var recoveryCode: EditText
    private lateinit var newPassword: EditText
    private lateinit var confirmPassword: EditText

    private lateinit var emailStep: LinearLayout
    private lateinit var codeStep: LinearLayout
    private lateinit var passwordStep: LinearLayout

    private var verifiedEmail = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recover_password)

        email = findViewById(R.id.etRecoveryEmail)
        recoveryCode = findViewById(R.id.etRecoveryCode)
        newPassword = findViewById(R.id.etNewPassword)
        confirmPassword = findViewById(R.id.etConfirmPassword)

        emailStep = findViewById(R.id.layoutEmailStep)
        codeStep = findViewById(R.id.layoutCodeStep)
        passwordStep = findViewById(R.id.layoutPasswordStep)

        val btnSendCode = findViewById<Button>(R.id.btnSendCode)
        val btnVerifyCode = findViewById<Button>(R.id.btnVerifyCode)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        // Etapa 1: verificar correo y generar código
        btnSendCode.setOnClickListener {
            verifyEmail()
        }

        // Etapa 2: validar código
        btnVerifyCode.setOnClickListener {
            verifyCode()
        }

        // Etapa 3: cambiar contraseña
        btnChangePassword.setOnClickListener {
            changePassword()
        }
    }

    private fun verifyEmail() {
        val emailText = email.text.toString().trim().lowercase()

        when {
            emailText.isEmpty() -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Campo obligatorio",
                    "Ingresa tu correo electrónico."
                )
            }

            !Patterns.EMAIL_ADDRESS.matcher(emailText).matches() -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Correo inválido",
                    "Ingresa una dirección de correo válida."
                )
            }

            !userExists(emailText) -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Usuario no encontrado",
                    "No existe una cuenta registrada con ese correo."
                )
            }

            else -> {
                verifiedEmail = emailText

                SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                    .setTitleText("Código generado")
                    .setContentText(
                        "Tu código de recuperación es: $SIMULATED_CODE"
                    )
                    .setConfirmText("Continuar")
                    .setConfirmClickListener {
                        it.dismiss()
                        emailStep.visibility = View.GONE
                        codeStep.visibility = View.VISIBLE
                    }
                    .show()
            }
        }
    }

    private fun verifyCode() {
        val enteredCode = recoveryCode.text.toString().trim()

        if (enteredCode.isEmpty()) {
            showAlert(
                SweetAlertDialog.ERROR_TYPE,
                "Campo obligatorio",
                "Ingresa el código de recuperación."
            )
            return
        }

        if (enteredCode.length != 5 || enteredCode != SIMULATED_CODE) {
            showAlert(
                SweetAlertDialog.ERROR_TYPE,
                "Código incorrecto",
                "El código ingresado no es válido."
            )
            return
        }

        codeStep.visibility = View.GONE
        passwordStep.visibility = View.VISIBLE

        showAlert(
            SweetAlertDialog.SUCCESS_TYPE,
            "Código verificado",
            "Ahora puedes crear tu nueva contraseña."
        )
    }

    private fun changePassword() {
        val passwordText = newPassword.text.toString()
        val confirmation = confirmPassword.text.toString()

        when {
            !isStrongPassword(passwordText) -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Contraseña débil",
                    "Debe tener al menos 8 caracteres, una mayúscula, una minúscula, un número y un carácter especial."
                )
            }

            passwordText != confirmation -> {
                showAlert(
                    SweetAlertDialog.ERROR_TYPE,
                    "Las contraseñas no coinciden",
                    "Verifica que ambas contraseñas sean iguales."
                )
            }

            else -> {
                updateUserPassword(passwordText)
            }
        }
    }

    private fun isStrongPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() }
    }

    private fun userExists(email: String): Boolean {
        val users = getUsers()

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)

            if (user.optString("email").equals(email, true)) {
                return true
            }
        }

        return false
    }

    private fun updateUserPassword(password: String) {
        val users = getUsers()
        var userUpdated = false

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)

            if (user.optString("email").equals(verifiedEmail, true)) {
                user.put("password", password)
                userUpdated = true
                break
            }
        }

        if (!userUpdated) {
            showAlert(
                SweetAlertDialog.ERROR_TYPE,
                "Error",
                "No se encontró la cuenta para actualizar."
            )
            return
        }

        getSharedPreferences(PREFERENCES_NAME, MODE_PRIVATE)
            .edit()
            .putString(USERS_KEY, users.toString())
            .apply()

        SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
            .setTitleText("Contraseña actualizada")
            .setContentText("Ya puedes ingresar a SYNTRA con tu nueva contraseña.")
            .setConfirmText("Volver al Login")
            .setConfirmClickListener {
                it.dismiss()
                finish()
            }
            .show()
    }

    private fun getUsers(): JSONArray {
        val preferences = getSharedPreferences(
            PREFERENCES_NAME,
            MODE_PRIVATE
        )

        return JSONArray(
            preferences.getString(USERS_KEY, "[]")
        )
    }

    private fun showAlert(type: Int, title: String, message: String) {
        SweetAlertDialog(this, type)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }
}
