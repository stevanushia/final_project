package com.example.final_project

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.window.SplashScreen
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class SplashScreenActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Enable SplashScreen API (Android 12+)
        super.onCreate(savedInstanceState)

        installSplashScreen().setKeepOnScreenCondition { false }

            // Keep the splash screen visible until MainActivity is ready
//        installSplashScreen().setKeepOnScreenCondition { false }

        // Navigate to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 5000) // Forces a 2-second delay

    }
}