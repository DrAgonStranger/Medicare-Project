package com.project.stepup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import javax.security.auth.callback.Callback

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private val SPLASH_TIME_OUT: Long = 2000 // 2 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            // This method will be executed once the timer is over
            // Start your app main activity
            startActivity(Intent(this, MainActivity::class.java))

            // close this activity
            finish()
        }, SPLASH_TIME_OUT)
    }
}