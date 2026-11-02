package com.example.smartwidget

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.Calendar

class WidgetConfigActivity : AppCompatActivity(), SensorEventListener {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private lateinit var prefs: WidgetPreferences
    private var selectedTargetEpoch: Long = 0L
    private var selectedBgUri: Uri? = null
    private var sensorManager: SensorManager? = null

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            selectedBgUri = it
            Toast.makeText(this, "Background selected!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setResult(Activity.RESULT_CANCELED)
        setContentView(R.layout.activity_widget_config)

        prefs = WidgetPreferences(this)
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setupStepSensor()

        val btnPickDate = findViewById<Button>(R.id.btn_pick_date)
        val btnPickBg = findViewById<Button>(R.id.btn_pick_bg)
        val btnResetBg = findViewById<Button>(R.id.btn_reset_bg)
        val btnSave = findViewById<Button>(R.id.btn_save)

        btnPickDate.setOnClickListener {
            val c = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val target = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                }
                selectedTargetEpoch = target.timeInMillis
                btnPickDate.text = "$day/${month + 1}/$year"
            }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
        }

        btnPickBg.setOnClickListener {
            imagePickerLauncher.launch(arrayOf("image/*"))
        }

        btnResetBg.setOnClickListener {
            selectedBgUri = null
            prefs.saveBgImageUri(appWidgetId, null)
            Toast.makeText(this, "Reset to Glass theme", Toast.LENGTH_SHORT).show()
        }

        btnSave.setOnClickListener {
            if (selectedTargetEpoch > 0) {
                prefs.saveTargetDate(appWidgetId, selectedTargetEpoch)
            }
            if (selectedBgUri != null) {
                prefs.saveBgImageUri(appWidgetId, selectedBgUri.toString())
            }

            // Trigger widget update
            val appWidgetManager = AppWidgetManager.getInstance(this)
            SmartWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetId, prefs)

            val resultValue = Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            setResult(Activity.RESULT_OK, resultValue)
            finish()
        }
    }

    private fun setupStepSensor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.ACTIVITY_RECOGNITION), 101)
            }
        }
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        stepSensor?.let {
            sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.values?.firstOrNull()?.let { totalSteps ->
            prefs.saveCachedSteps(totalSteps.toInt())
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}