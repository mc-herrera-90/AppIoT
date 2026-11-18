package com.example.appiot.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R
import cn.pedant.SweetAlert.SweetAlertDialog
import org.json.JSONArray
import com.example.appiot.MainActivity

class LoginActivity : AppCompatActivity() {

    private val preferencesName = "SYNTRA_USERS"
    private val usersKey = "users"

    private var currentStep = 1

    private lateinit var email: EditText
    private lateinit var password: EditText
    private lateinit var btnLoginAction: Button
    private lateinit var layoutStep1: LinearLayout
    private lateinit var layoutStep2: LinearLayout
    private lateinit var tvStepDescription: TextView
    private lateinit var logo: ImageView
    private lateinit var llRegisterContainer: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        email = findViewById(R.id.etEmail)
        password = findViewById(R.id.etPassword)
        btnLoginAction = findViewById(R.id.btnLoginAction)
        layoutStep1 = findViewById(R.id.layoutStep1)
        layoutStep2 = findViewById(R.id.layoutStep2)
        tvStepDescription = findViewById(R.id.tvStepDescription)
        logo = findViewById(R.id.ivLoginLogo)
        llRegisterContainer = findViewById(R.id.llRegisterContainer)
        val tvRegister = findViewById<TextView>(R.id.tvRegister)
        val tvForgotPassword = findViewById<TextView>(R.id.tvForgotPassword)

        btnLoginAction.setOnClickListener {
            when (currentStep) {
                1 -> validarPaso1()
                2 -> validarPaso2()
            }
        }

        tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        tvForgotPassword.setOnClickListener {
            startActivity(Intent(this, RecoverPasswordActivity::class.java))
        }
    }

    private fun validarPaso1() {
        val emailText = email.text.toString().trim().lowercase()

        when {
            emailText.isEmpty() -> {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Campo obligatorio", "Ingresa tu correo electrónico.")
            }
            !Patterns.EMAIL_ADDRESS.matcher(emailText).matches() -> {
                showAlert(SweetAlertDialog.ERROR_TYPE, "Correo inválido", "Ingresa una dirección de correo válida.")
            }
            else -> {
                // Ocultar teclado al avanzar de paso
                ocultarTeclado()
                currentStep = 2
                mostrarPaso2()
            }
        }
    }

    private fun mostrarPaso2() {
        layoutStep1.visibility = View.GONE
        layoutStep2.visibility = View.VISIBLE
        btnLoginAction.text = "INGRESAR"
        tvStepDescription.text = "Ingresa a tu cuenta para continuar"
        password.requestFocus()
    }

    private fun validarPaso2() {
        val emailText = email.text.toString().trim().lowercase()
        val passwordText = password.text.toString()

        if (passwordText.isEmpty()) {
            showAlert(SweetAlertDialog.ERROR_TYPE, "Campo obligatorio", "Ingresa tu contraseña.")
            return
        }

        ocultarTeclado()

        if (validateUser(emailText, passwordText)) {
            // Mostrar SweetAlert de carga "Iniciando sesión..."
            val pDialog = SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE)
            pDialog.progressHelper.barColor = resources.getColor(R.color.primary)
            pDialog.titleText = "Iniciando sesión..."
            pDialog.setCancelable(false)
            pDialog.show()

            // Delay de 1.2 segundos para simular el inicio de sesión fluido y transicionar al Main
            Handler(Looper.getMainLooper()).postDelayed({
                pDialog.dismissWithAnimation()
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("user_name", getUserName(emailText))
                startActivity(intent)
                finish()
            }, 1200)

        } else {
            showAlert(SweetAlertDialog.ERROR_TYPE, "Acceso denegado", "El correo o la contraseña son incorrectos.")
        }
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    private fun getUserName(email: String): String {
        val preferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val users = JSONArray(preferences.getString(usersKey, "[]"))

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(email, ignoreCase = true)) {
                return user.optString("firstName")
            }
        }
        return "usuario"
    }

    private fun validateUser(email: String, password: String): Boolean {
        val preferences = getSharedPreferences(preferencesName, MODE_PRIVATE)
        val users = JSONArray(preferences.getString(usersKey, "[]"))

        for (i in 0 until users.length()) {
            val user = users.getJSONObject(i)
            if (user.optString("email").equals(email, ignoreCase = true) && user.optString("password") == password) {
                return true
            }
        }
        return false
    }

    private fun showAlert(type: Int, title: String, message: String) {
        SweetAlertDialog(this, type)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }
}