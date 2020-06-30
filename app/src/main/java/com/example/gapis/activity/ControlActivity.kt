package com.example.gapis.activity

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.gapis.MyData
import com.example.gapis.R
import kotlinx.android.synthetic.main.activity_control.*

@Suppress("UNCHECKED_CAST")
class ControlActivity : AppCompatActivity() {

    var data=ArrayList<MyData>()
    var num=0//에어컨인지 보일러인지
    var name=""//기기 이름

    lateinit var intensity:String
    lateinit var temp:TextView//혀재온도
    lateinit var humd:TextView//습도
    lateinit var desiredTemp:TextView//희망온도
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_control)
        settingData()
        init()
        action()
    }

    private fun action() {//온도 설정하기
        var tmp:Int
        upButton.setOnClickListener{
           tmp=(desiredTemp.text.toString().toInt()+1)
            //upButton.setBackgroundColor(Color.parseColor("#0078d4"))
            //downButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            if(tmp==31){
                tmp-=1
            }
            desiredTemp.text=tmp.toString()
        }
        downButton.setOnClickListener{
            tmp=(desiredTemp.text.toString().toInt()-1)
            //upButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            //downButton.setBackgroundColor(Color.parseColor("#0078d4"))
            if(tmp==17){
                tmp+=1
            }
            desiredTemp.text=tmp.toString()
        }
    }

    fun settingData(){
        //정보 받기
        val intent= getIntent()
        data= intent.getSerializableExtra("systemName") as ArrayList<MyData>
        intensity=data[0].intensity
        num=intent.getIntExtra("num",-1)

        //text 표시하기
        temp=findViewById(R.id.temperature)
        humd=findViewById(R.id.humidity)
        desiredTemp=findViewById(R.id.desiredTem)
        temp.text = data[num].homeTemp.toString()
        desiredTemp.text = data[num].desiredTemp.toString()
        humd.text = 33.toString()
        name = data[num].name

        //온도 설정 완료 버튼, 정보 보내기
        okButton.setOnClickListener {
            var changedTemp=desiredTemp.text.toString().toInt()
            val intent= Intent(this, MainActivity::class.java)
            intent.putExtra("intensity",intensity)
            intent.putExtra("changedTemp",changedTemp)
            intent.putExtra("num2",num)//에어컨인지 보일러인지

            Toast.makeText(this, "설정되었습니다.",Toast.LENGTH_SHORT).show()
            Log.i("control","$changedTemp, $num")
            startActivity(intent)
            ActivityCompat.finishAffinity(this)
            finish()
        }
    }
    private fun init() {
        val toolbar: Toolbar = findViewById(R.id.toolbarControl)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        with(actionbar!!) {
            title=name
            setDisplayHomeAsUpEnabled(true)
            setHomeAsUpIndicator(R.drawable.back)
            //setIcon(R.drawable.ic_launcher_new_foreground)
            //setDisplayUseLogoEnabled(true)
        }

        //바람세기 버튼
        lowButton.setOnClickListener {
            intensity="약"

            //색 표시
            lowButton.setBackgroundColor(Color.parseColor("#0078d4"))
            midButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            highButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
        }
        midButton.setOnClickListener {
            intensity="중"
            //색 표시
            lowButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            midButton.setBackgroundColor(Color.parseColor("#0078d4"))
            highButton.setBackgroundColor(Color.parseColor("#c7e0f4"))}
        highButton.setOnClickListener {
            intensity="강"
            //색 표시
            lowButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            midButton.setBackgroundColor(Color.parseColor("#c7e0f4"))
            highButton.setBackgroundColor(Color.parseColor("#0078d4"))
        }

        if(intensity=="약")
            lowButton.callOnClick()
        if(intensity=="중")
            midButton.callOnClick()
        if(intensity=="강")
            highButton.callOnClick()

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

//            R.id.alarm -> {//알림 설정하기
//                return true
//
//            }
//            R.id.item1 -> {// 미정
//                return true
//
//            }
//            R.id.item2 -> {//미정
//                return true
//
//            }
            else -> return super.onOptionsItemSelected(item)
        }
    }
}
