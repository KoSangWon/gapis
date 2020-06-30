package com.example.gapis.activity

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.gapis.R
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity() {
    var coldTemp=0//에어컨 희망 온도
    var homeFlag=false
    var homeLoc_lat=-1.0
    var homeLoc_lon=-1.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            init()
        }
        else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),100)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==100){
            if(grantResults[0]== PackageManager.PERMISSION_GRANTED&&
                grantResults[1]== PackageManager.PERMISSION_GRANTED){
                init()
            }
            else{
                Toast.makeText(this,"위치정보를 제공해야 합니다.",Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),100)
            }
        }
    }

    private fun init() {
        //에어컨

        val intent =getIntent()
        homeLoc_lat=intent.getDoubleExtra("homeloc_lat",-1.0)
        homeLoc_lon=intent.getDoubleExtra("homeloc_lon",-1.0)

        if(homeLoc_lat!=-1.0 && homeLoc_lon!=-1.0){
            homeFlag=true

        }

        numberPicker1.minValue=18
        numberPicker1.maxValue=30
        numberPicker1.wrapSelectorWheel=false


        homeChecked.setOnClickListener {
            val intent= Intent(this, MapActivity::class.java)
            intent.putExtra("from",1) // 1은 인트로에서 2는 메인에서
            startActivity(intent)
            //색 표시
         //   homeChecked.setBackgroundColor(Color.parseColor("#0C283C"))
        }

        done.setOnClickListener {
            if(homeFlag==false)
                Toast.makeText(this, "집 등록 및 온도 설정을 완료해주세요.",Toast.LENGTH_SHORT).show()
            else {
                //색 표시
     //           done.setBackgroundColor(Color.parseColor("#0C283C"))

                val sharedPreferences=getSharedPreferences("checkMain", Context.MODE_PRIVATE)
                val editor=sharedPreferences.edit()
                editor.putBoolean("checkMain",false)
                editor.apply()

                coldTemp= numberPicker1.value
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("coldTemp", coldTemp)
                intent.putExtra("homeloc_lat",homeLoc_lat)
                intent.putExtra("homeloc_lon",homeLoc_lon)
                startActivity(intent)
                ActivityCompat.finishAffinity(this)
                finish()
            }
        }

    }
}
