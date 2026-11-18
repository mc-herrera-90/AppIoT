package com.example.appiot.activities

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.appiot.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SensorsActivity : AppCompatActivity() {

    private lateinit var temperature: TextView
    private lateinit var humidity: TextView
    private lateinit var sensorDate: TextView
    private lateinit var connectionStatus: TextView
    private lateinit var temperatureImage: ImageView

    private lateinit var bulbImage: ImageView
    private lateinit var bulbStatus: TextView

    private lateinit var flashlightImage: ImageView
    private lateinit var flashlightStatus: TextView

    private lateinit var requestQueue: RequestQueue
    private var requestInProgress = false

    private val handler = Handler(Looper.getMainLooper())
    private val refreshInterval = 5000L
    private val clockInterval = 1000L

    private var bulbIsOn = false
    private var flashlightIsOn = false

    // Actualiza la hora del teléfono cada segundo, independientemente de la API.
    private val clockTask = object : Runnable {
        override fun run() {
            sensorDate.text = getCurrentDate()
            handler.postDelayed(this, clockInterval)
        }
    }

    // Consulta los sensores cada cinco segundos.
    private val sensorRefreshTask = object : Runnable {
        override fun run() {
            getSensorData()
            handler.postDelayed(this, refreshInterval)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sensors)

        temperature = findViewById(R.id.tvTemperature)
        humidity = findViewById(R.id.tvHumidity)
        sensorDate = findViewById(R.id.tvSensorDate)
        connectionStatus = findViewById(R.id.tvConnectionStatus)
        temperatureImage = findViewById(R.id.ivTemperature)

        bulbImage = findViewById(R.id.ivBulb)
        bulbStatus = findViewById(R.id.tvBulbStatus)

        flashlightImage = findViewById(R.id.ivFlashlight)
        flashlightStatus = findViewById(R.id.tvFlashlightStatus)

        requestQueue = Volley.newRequestQueue(applicationContext)

        bulbImage.setOnClickListener {
            bulbIsOn = !bulbIsOn
            updateBulb()
        }

        flashlightImage.setOnClickListener {
            flashlightIsOn = !flashlightIsOn
            updateFlashlight()
        }

        // Mostrar hora y datos inmediatamente al entrar.
        sensorDate.text = getCurrentDate()
        handler.post(clockTask)
        handler.post(sensorRefreshTask)
    }

    // Consultar temperatura y humedad desde el WebService.
    private fun getSensorData() {
        if (requestInProgress) return

        requestInProgress = true
        val url = "https://www.pnk.cl/muestra_datos.php"

        val request = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            { response ->
                requestInProgress = false

                try {
                    val temperatureValue =
                        response.getString("temperatura").toFloat()

                    val humidityValue =
                        response.getString("humedad").toFloat()

                    temperature.text = String.format(
                        Locale.getDefault(),
                        "%.1f °C",
                        temperatureValue
                    )

                    humidity.text = String.format(
                        Locale.getDefault(),
                        "%.1f %%",
                        humidityValue
                    )

                    changeTemperatureImage(temperatureValue)

                    connectionStatus.text = "Conectado al servidor"
                    connectionStatus.setTextColor(getColor(R.color.success))

                } catch (exception: Exception) {
                    connectionStatus.text = "Error al interpretar los datos"
                    connectionStatus.setTextColor(getColor(R.color.error))
                }
            },
            {
                requestInProgress = false
                connectionStatus.text = "Sin conexión con el servidor"
                connectionStatus.setTextColor(getColor(R.color.error))
            }
        )

        requestQueue.add(request)
    }

    private fun changeTemperatureImage(value: Float) {
        if (value >= 20f) {
            temperatureImage.setImageResource(R.drawable.tempalta)
        } else {
            temperatureImage.setImageResource(R.drawable.tempbaja)
        }
    }

    private fun updateBulb() {
        if (bulbIsOn) {
            bulbImage.setImageResource(R.drawable.bulb_on)
            bulbStatus.text = "Encendida"
            bulbStatus.setTextColor(getColor(R.color.success))
        } else {
            bulbImage.setImageResource(R.drawable.bulb_off)
            bulbStatus.text = "Apagada"
            bulbStatus.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun updateFlashlight() {
        if (flashlightIsOn) {
            flashlightImage.setImageResource(R.drawable.flashlight_on)
            flashlightStatus.text = "Encendida"
            flashlightStatus.setTextColor(getColor(R.color.success))
        } else {
            flashlightImage.setImageResource(R.drawable.flashlight_off)
            flashlightStatus.text = "Apagada"
            flashlightStatus.setTextColor(getColor(R.color.text_secondary))
        }
    }

    private fun getCurrentDate(): String {
        val formatter = SimpleDateFormat(
            "dd MMMM yyyy, hh:mm:ss a",
            Locale.getDefault()
        )
        return formatter.format(Date())
    }

    override fun onDestroy() {
        handler.removeCallbacks(clockTask)
        handler.removeCallbacks(sensorRefreshTask)

        if (::requestQueue.isInitialized) {
            requestQueue.cancelAll { true }
        }

        super.onDestroy()
    }
}
