package com.example.appiot.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.appiot.R
import cn.pedant.SweetAlert.SweetAlertDialog

class RegisterActivity : AppCompatActivity() {

    private var currentStep = 1
    private var isFromCrud = false

    private lateinit var layoutStep1: LinearLayout
    private lateinit var layoutStep2: LinearLayout
    private lateinit var layoutStep3: LinearLayout
    private lateinit var tvStepDescription: TextView

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etRegisterEmail: EditText
    private lateinit var etRegisterPassword: EditText
    private lateinit var etConfirmPassword: EditText

    private lateinit var btnRegisterAction: Button
    private lateinit var tvBackToLogin: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        layoutStep1 = findViewById(R.id.layoutStep1)
        layoutStep2 = findViewById(R.id.layoutStep2)
        layoutStep3 = findViewById(R.id.layoutStep3)
        tvStepDescription = findViewById(R.id.tvStepDescription)

        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etRegisterEmail = findViewById(R.id.etRegisterEmail)
        etRegisterPassword = findViewById(R.id.etRegisterPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)

        btnRegisterAction = findViewById(R.id.btnRegisterAction)
        tvBackToLogin = findViewById(R.id.tvBackToLogin)

        val letterFilter = android.text.InputFilter { source, start, end, dest, dstart, dend ->
            for (i in start until end) {
                val char = source[i]
                if (!Character.isLetter(char) && !Character.isSpaceChar(char)) {
                    return@InputFilter ""
                }
            }
            null
        }

        etFirstName.filters = arrayOf(letterFilter)
        etLastName.filters = arrayOf(letterFilter)

        btnRegisterAction.setOnClickListener {
            avanzarPaso()
        }

        tvBackToLogin.setOnClickListener {
            finish()
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                when (currentStep) {
                    3 -> {
                        animarTransicion(layoutStep3, layoutStep2)
                        tvStepDescription.text = "Información de contacto"
                        btnRegisterAction.text = "SIGUIENTE"
                        currentStep = 2
                    }
                    2 -> {
                        animarTransicion(layoutStep2, layoutStep1)
                        tvStepDescription.text = "Datos Personales"
                        currentStep = 1
                    }
                    1 -> {
                        finish()
                    }
                }
            }
        })

        val tvMainTitle = findViewById<TextView>(R.id.tvMainTitle)
        val ivRegisterLogo = findViewById<ImageView>(R.id.ivRegisterLogo)
        val llBackToLogin = findViewById<LinearLayout>(R.id.llBackToLogin)

        isFromCrud = intent.getBooleanExtra("from_crud", false)

        if (isFromCrud) {
            tvMainTitle.text = "Ingresar usuario"
            ivRegisterLogo.visibility = View.GONE
            llBackToLogin.visibility = View.GONE
        }
    }

    private fun formatName(input: String): String {
        return input.trim().lowercase().split("\\s+".toRegex()).joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    private fun avanzarPaso() {
        when (currentStep) {
            1 -> {
                if (etFirstName.text.isBlank() || etLastName.text.isBlank()) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "Campos obligatorios", "Por favor, ingresa tu nombre y apellido.")
                    return
                }
                animarTransicion(layoutStep1, layoutStep2)
                ocultarTeclado()
                tvStepDescription.text = "Información de contacto"
                currentStep = 2
            }
            2 -> {
                val emailText = etRegisterEmail.text.toString().trim()
                if (emailText.isBlank()) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "Campo obligatorio", "Por favor, ingresa tu correo electrónico.")
                    return
                }
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "Formato inválido", "Ingresa una dirección de correo válida.")
                    return
                }
                animarTransicion(layoutStep2, layoutStep3)
                tvStepDescription.text = "Seguridad de la cuenta"
                btnRegisterAction.text = "CREAR CUENTA"
                currentStep = 3
            }
            3 -> {
                val pass = etRegisterPassword.text.toString()
                val confPass = etConfirmPassword.text.toString()

                if (pass.isBlank() || confPass.isBlank()) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "Campos obligatorios", "Por favor, completa ambas contraseñas.")
                    return
                }
                if (pass.length < 6) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "Contraseña débil", "La contraseña debe tener al menos 6 caracteres.")
                    return
                }
                if (pass != confPass) {
                    showAlert(SweetAlertDialog.ERROR_TYPE, "No coinciden", "Las contraseñas no son idénticas.")
                    return
                }

                val sharedPreferences = getSharedPreferences("SYNTRA_USERS", MODE_PRIVATE)
                val usersJsonString = sharedPreferences.getString("users", "[]")
                val usersArray = org.json.JSONArray(usersJsonString)

                val newUser = org.json.JSONObject()
                newUser.put("firstName", formatName(etFirstName.text.toString()))
                newUser.put("lastName", formatName(etLastName.text.toString()))
                newUser.put("email", etRegisterEmail.text.toString().trim().lowercase())
                newUser.put("password", pass)

                usersArray.put(newUser)

                val editor = sharedPreferences.edit()
                editor.putString("users", usersArray.toString())
                editor.apply()

                val successDialog = SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE)
                successDialog.titleText = if (isFromCrud) "Usuario registrado" else "¡Cuenta creada!"
                successDialog.contentText = if (isFromCrud) "El usuario se ha añadido a la base de datos." else "Tu registro se completó con éxito."
                successDialog.setCancelable(false)
                successDialog.setConfirmText("Aceptar")
                successDialog.setConfirmClickListener {
                    it.dismissWithAnimation()
                    if (isFromCrud) {
                        finish()
                    } else {
                        val intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }
                successDialog.show()
            }
        }
    }

    private fun showAlert(type: Int, title: String, message: String) {
        SweetAlertDialog(this, type)
            .setTitleText(title)
            .setContentText(message)
            .show()
    }

    private fun animarTransicion(vistaOcultar: View, vistaMostrar: View) {
        vistaMostrar.alpha = 0f
        vistaMostrar.visibility = View.VISIBLE

        vistaOcultar.animate()
            .alpha(0f)
            .setDuration(300)
            .withEndAction {
                vistaOcultar.visibility = View.GONE
            }

        vistaMostrar.animate()
            .alpha(1f)
            .setDuration(300)
            .setListener(null)
    }

    private fun ocultarTeclado() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }
}