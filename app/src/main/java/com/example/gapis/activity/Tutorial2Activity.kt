package com.example.gapis.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gapis.R
import kotlinx.android.synthetic.main.activity_tutorial2.*

class Tutorial2Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tutorial2)
        tutorialImage2.setOnClickListener{
//            val intent= Intent(this,MainActivity::class.java)
//            startActivity(intent)
            finish()
        }
    }
}
