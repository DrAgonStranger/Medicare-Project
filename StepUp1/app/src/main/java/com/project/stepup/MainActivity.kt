package com.project.stepup

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.hardware.SensorManager as SensorManager
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import com.project.stepup.R.id.buttonHome
import com.project.stepup.R.id.buttonNotification
import com.project.stepup.R.id.buttonProfile

class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager? = null

    private var running = false
    private var totalSteps = 0f
    private var prevTotalSteps = 0f

    // Average step in meters
    private val stepLength = 0.78
    // Calories burned per 1 step
    private val caloriesPerStep = 0.04

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val buttonProfile = findViewById<ImageButton>(buttonProfile)

        buttonProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val buttonNotification = findViewById<ImageButton>(buttonNotification)

        buttonNotification.setOnClickListener {
            val intent = Intent(this, NotificationActivity::class.java)
            startActivity(intent)
        }

        loadData()
        resetSteps()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        /* //testing connection between steps, progress bar, dist and cals
        val circularProgressBar = findViewById<CircularProgressBar>(R.id.progress_circular)
        val newStepsTxt = findViewById<TextView>(R.id.newSteps)
        val distanceTextView = findViewById<TextView>(R.id.distance)
        val caloriesTextView = findViewById<TextView>(R.id.calories)

        val currentSteps = 1800
        newStepsTxt.text = ("$currentSteps")


        circularProgressBar.setProgressWithAnimation(currentSteps.toFloat())

        val distance = (currentSteps * stepLength) / 1000
        distanceTextView.text = "Distance: %.2f km".format(distance)

        val calories = currentSteps * caloriesPerStep
        caloriesTextView.text = "Calories: %.2f kcal".format(calories)*/
    }

    override fun onResume() {
        super.onResume()
        running = true
        val stepSensor: Sensor? = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

        if(stepSensor == null) {
            //for older versions without sensors
            Toast.makeText(this, "No sensor detected on this device", Toast.LENGTH_SHORT).show()
        } else {
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        }

    }

    override fun onSensorChanged(event: SensorEvent?) {
        val circularProgressBar = findViewById<CircularProgressBar>(R.id.progress_circular)
        val newStepsTxt = findViewById<TextView>(R.id.newSteps)
        val distanceTextView = findViewById<TextView>(R.id.distance)
        val caloriesTextView = findViewById<TextView>(R.id.calories)

        if(running) {
            totalSteps = event?.values?.get(0) ?: 0f
            val currentSteps = (totalSteps - prevTotalSteps).toInt()
            newStepsTxt.text = ("$currentSteps")


            circularProgressBar.setProgressWithAnimation(currentSteps.toFloat())

            val distance = (currentSteps * stepLength) / 1000
            distanceTextView.text = "Distance: %.2f km".format(distance)

            val calories = currentSteps * caloriesPerStep
            caloriesTextView.text = "Calories: %.2f kcal".format(calories)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }

    override fun onPause() {
        super.onPause()
        running = false
        //for calculating the difference when resumed
        prevTotalSteps = totalSteps
    }

    private fun resetSteps() {
        val newStepsTxt = findViewById<TextView>(R.id.newSteps)

        newStepsTxt.setOnClickListener {
            Toast.makeText(this, "Long tap to reset steps", Toast.LENGTH_SHORT).show()
        }

        newStepsTxt.setOnLongClickListener {
            prevTotalSteps = totalSteps
            newStepsTxt.text = 0.toString()
            saveData()

            true
        }
    }

    private fun saveData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putFloat("key1", prevTotalSteps)
        editor.apply()
    }

    private fun loadData() {
        val sharedPreferences = getSharedPreferences("myPrefs", Context.MODE_PRIVATE)
        val savedNumber = sharedPreferences.getFloat("key1", 0f)
        Log.d("Main Activity", "$savedNumber")
        prevTotalSteps = savedNumber
    }
}