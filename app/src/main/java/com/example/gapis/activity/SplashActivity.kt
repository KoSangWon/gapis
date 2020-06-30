package com.example.gapis.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.example.gapis.R

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        val handler=Handler()
        handler.postDelayed(splashHandler(),2000)
    }

    inner class splashHandler:Runnable{
        override fun run() {
            val intent= Intent(this@SplashActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {

    }
}
