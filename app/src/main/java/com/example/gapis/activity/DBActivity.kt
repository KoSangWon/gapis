package com.example.gapis.activity

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.gapis.R
import com.example.gapis.function.MyDBHelper
import kotlinx.android.synthetic.main.activity_d_b.*

class DBActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences//최초만 실행
    lateinit var myDBHelper: MyDBHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_d_b)
        init()
        initToolbar()
    }
    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbarDB)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        with(actionbar!!) {
            title="온도와 시간 및 거리 확인하기"
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
        }
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menucontrol,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
    fun init(){
        myDBHelper= MyDBHelper(this, tableLayout1, tableLayout2)
        sharedPreferences=getSharedPreferences("checkDB", Context.MODE_PRIVATE)
        val checkFirst=sharedPreferences.getBoolean("checkDB",true)
        if(checkFirst){//첫 실행 시
            myDBHelper.initInsert()
            val editor=sharedPreferences.edit()
            editor.putBoolean("checkDB",false)
            editor.apply()

            myDBHelper.showDB()
        }

        else{//그다음부터
            myDBHelper.showDB()
        }
    }
}
