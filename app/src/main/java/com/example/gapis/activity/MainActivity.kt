package com.example.gapis.activity

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NotificationCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gapis.MyAdapter
import com.example.gapis.MyData
import com.example.gapis.R
import com.example.gapis.function.GPSManager
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import org.jsoup.Jsoup
import java.io.BufferedInputStream
import java.net.URL

class MainActivity : AppCompatActivity() {

    lateinit var sharedPreferences: SharedPreferences//최초만 실행
    lateinit var sharedPreferences_lat: SharedPreferences
    lateinit var sharedPreferences_lon: SharedPreferences
    lateinit var gpsManager: GPSManager
    lateinit var weatherBitmap: Bitmap

    companion object {
        val notInialized = 0
        val running = 1
        val stopped = 2
    }

    var gpsRunning = notInialized
    var showData = ArrayList<MyData>()//냉난방기 이름
    lateinit var adapter: MyAdapter

    //데이터베이스
    lateinit var desiredtempdb: DatabaseReference
    lateinit var aircondb: DatabaseReference
    lateinit var tempdb: DatabaseReference
    var aircondbInitialized = false
    var desiredtempdbInitialized = false
    var inited = false
    var outTemp = 28

    //집 위치
    var homeLoc_lat = -1.0
    var homeLoc_lon = -1.0


    fun SharedPreferences.Editor.putDouble(key: String, double: Double) =
        putLong(key, java.lang.Double.doubleToRawLongBits(double))

    fun SharedPreferences.getDouble(key: String, default: Double) =
        java.lang.Double.longBitsToDouble(
            getLong(
                key,
                java.lang.Double.doubleToRawLongBits(default)
            )
        )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        isFirst()
    }

    private fun isFirst() {//앱에서 최초 한번만 실행
        sharedPreferences = getSharedPreferences("checkMain", Context.MODE_PRIVATE)
        sharedPreferences_lat = getSharedPreferences("homeLoc_lat", Context.MODE_PRIVATE)
        sharedPreferences_lon = getSharedPreferences("homeLoc_lon", Context.MODE_PRIVATE)
        val checkFirst = sharedPreferences.getBoolean("checkMain", true)
        if (checkFirst) {//첫 실행 시
            //초기설정
            val intent = Intent(this, IntroActivity::class.java)
            startActivity(intent)
            //튜토리얼
            val intent2 = Intent(this, Tutorial2Activity::class.java)
            startActivity(intent2)
        } else {//그다음부터

            homeLoc_lat = sharedPreferences_lat.getDouble("homeLoc_lat", -1.0)
            homeLoc_lon = sharedPreferences_lon.getDouble("homeLoc_lon", -1.0)
            initProduct()
            initDataBase()
        }
    }

    fun makeNotification() {
        //   if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channelId = "MyChannel"
        val channelName = "MyChannelName"
        val notificationChannel = NotificationChannel(//notificationChannel
            channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableVibration(true)

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(notificationChannel)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
        )
        val snoozeIntent = Intent(this, MainActivity2::class.java)
        snoozeIntent.action = Intent.ACTION_VIEW
        snoozeIntent.putExtra(Notification.EXTRA_NOTIFICATION_ID, 0)

        val snoozePendingIntent: PendingIntent =
            PendingIntent.getBroadcast(this, 0, snoozeIntent, 0)
        //알림창 설정하기
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)//필수
            .setContentTitle("GAPIS")//제목
            .setContentText("에어컨알림")//내용
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setDefaults(Notification.DEFAULT_SOUND)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)//누르면 앱으로 들어가기
            .addAction(R.drawable.ic_launcher_foreground, "에어컨이 켜저있습니다.", snoozePendingIntent)//상세설명

        //알림 설정
        val notification = builder.build()
        manager.notify(10, notification)
    }

    private fun initProduct() {//항목에 표시할 내용
        //초기 정보 받기
        showData.add(MyData("OFF", "약", "에어컨", 0, 0, R.drawable.air_conditioner))
        showData.add(MyData("OFF", "중", "보일러", 0, 0, R.drawable.boiler))
    }

    fun initDataBase() {

        //외부 DB
        desiredtempdb =
            FirebaseDatabase.getInstance().getReference("DesiredTempDB/Place/home/desiredTemp")
        aircondb = FirebaseDatabase.getInstance().getReference("AirconDB/Place/home")
        tempdb = FirebaseDatabase.getInstance().getReference("TempDB/Place")


        desiredtempdb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                showData[0].desiredTemp = (p0.value.toString().toInt())
                desiredtempdbInitialized = true
                if (desiredtempdbInitialized && aircondbInitialized && !inited) {
                    inited = true
                    init()
                }
            }
        })
        aircondb.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                showData[0].power = (p0.child("power").value.toString())
                showData[0].intensity = (p0.child("intensity").value.toString())
                aircondbInitialized = true
                if (desiredtempdbInitialized && aircondbInitialized && !inited) {
                    inited = true
                    init()
                }
            }

        })


    }

    @SuppressLint("MissingPermission")
    private fun init() {//화면 설정하기

        getData()

        //toolbar에 actionbar 등록
        val toolbar: Toolbar = findViewById(R.id.toolbarMain)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        with(actionbar!!) {
            title = "GaPiS"
            //setIcon(R.drawable.ic_launcher_new_foreground)
            //setDisplayUseLogoEnabled(true)
            //setDisplayShowHomeEnabled(true)
        }

        //recyclerview에 adapter 등록
        recyclerViewMain.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.VERTICAL,
            false
        );


        adapter = MyAdapter(showData)//adapter 초기화

        //itemLongClickLister 달기
        adapter.itemLongClickListener = object :
            MyAdapter.OnItemLongClickListener {
            override fun onItemLongClick(
                holder: MyAdapter.MyViewHolder,
                view: View,
                data: MyData,
                position: Int
            ): Boolean {
                //정보 보내기
                val intent = Intent(this@MainActivity, ControlActivity::class.java)
                intent.putExtra("systemName", showData)
                intent.putExtra("num", position)
                startActivity(intent)

                return true
            }
        }
        recyclerViewMain.adapter = adapter

        //itemClickLister 달기
        adapter.switchClickListener = object : MyAdapter.OnSwitchClickListener {
            override fun onSwitchClick(
                holder: MyAdapter.MyViewHolder,
                view: View,
                data: MyData,
                position: Int
            ) {
                if ((view as Switch).isChecked) {
                    Toast.makeText(this@MainActivity, "${data.name} 작동을 시작합니다.", Toast.LENGTH_SHORT)
                        .show()
                    SetAirCon("ON", showData[position].intensity)
                } else {
                    Toast.makeText(this@MainActivity, "${data.name} 작동을 중지합니다.", Toast.LENGTH_SHORT)
                        .show()
                    SetAirCon("OFF", showData[position].intensity)
                }
                adapter.notifyItemChanged(holder.adapterPosition)
            }

        }

        tempdb.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                println("Failed to read Value")
            }

            override fun onDataChange(p0: DataSnapshot) {
                val value = p0.value.toString()
                val json = JSONObject(value)

                val homeTemp = json.getJSONObject("home").getString("inTemp").toInt()
                showData[0].homeTemp = homeTemp
                adapter.notifyDataSetChanged()
            }
        })

        desiredtempdb.setValue(showData[0].desiredTemp)
        SetAirCon(showData[0].power, showData[0].intensity)
        gpsManager = GPSManager(this, LatLng(homeLoc_lat, homeLoc_lon))


        MyAsyncTask().execute()

    }

    inner class MyAsyncTask : AsyncTask<String, String, Int>() {
        override fun onPreExecute() {
            super.onPreExecute()
            tempView.text = "날씨 정보를 받아오는 중"
            weatherView.visibility = View.VISIBLE
        }

        override fun doInBackground(vararg params: String?): Int {

            val doc =
                Jsoup.connect("http://api.openweathermap.org/data/2.5/weather?lat=${homeLoc_lat}&lon=${homeLoc_lon}&appid=32af338070676a90d229b310a6a46a83")
                    .ignoreContentType(true).execute().body()
            val json = JSONObject(doc).getJSONObject("main")
            val temp = json.getDouble("temp")


            val json2 = JSONObject(doc).getJSONArray("weather")
            val icon = (json2[0] as JSONObject).getString("icon")

            val url = URL("http://openweathermap.org/img/w/" + icon + ".png")
            val conn = url.openConnection()
            conn.connect()
            val bis = BufferedInputStream(conn.getInputStream())
            val bm = BitmapFactory.decodeStream(bis);
            bis.close()
            weatherBitmap = bm


            return (temp - 273.15).toInt()
        }


        override fun onPostExecute(result: Int) {
            super.onPostExecute(result)

            outTemp = result
            tempView.text = "현재 기온은 $outTemp ºC 입니다."
            weatherView.setImageBitmap(weatherBitmap)
            weatherView.visibility = View.VISIBLE
        }
    }

    private fun getData() {
        //정보 받기
        val intent = getIntent()
        val desiredTemp = intent.getIntExtra("coldTemp", -1)
        if (desiredTemp != -1)
            showData[0].desiredTemp = desiredTemp

        val changedTemp = intent.getIntExtra("changedTemp", -1)
        val num2 = intent.getIntExtra("num2", -1)
        if (num2 != -1) {
            showData[0].desiredTemp = changedTemp//온도 바뀜
        }

        val intensity = intent.getStringExtra("intensity")
        if (intensity != null)
            showData[0].intensity = intensity


        val lat = intent.getDoubleExtra("homeloc_lat", -1.0)
        val lon = intent.getDoubleExtra("homeloc_lon", -1.0)

        if (lat > 0 && lon > 0) {
            homeLoc_lat = lat
            homeLoc_lon = lon

            val editor_lat = sharedPreferences_lat.edit()
            val editor_lon = sharedPreferences_lon.edit()
            editor_lat.putDouble("homeLoc_lat", lat)
            editor_lon.putDouble("homeLoc_lon", lon)

            editor_lat.apply()
            editor_lon.apply()
        }

        Log.i("Main", "$changedTemp, $num2")
    }


    fun SetAirCon(power: String, intensity: String) {
        aircondb.child("intensity").setValue(intensity)
        aircondb.child("power").setValue(power)
        showData[0].power = power;
        showData[0].intensity = intensity
        adapter.notifyDataSetChanged()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menumain, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.item1 -> {//장소등록 화면으로 이동
                val intent = Intent(this, MapActivity::class.java)
                intent.putExtra("from", 2) // 1은 인트로에서 2는 메인에서
                startActivity(intent)
                return true
            }
            R.id.item2 -> {//DB 확인 화면
                val intent = Intent(this, DBActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.item3 -> {//추갛기 화면
                val intent = Intent(this, AddActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.item4 -> {//출처 밝히기 화면
                val intent = Intent(this, InfoActivity::class.java)
                startActivity(intent)
                return true
            }
            R.id.item5 -> {
                if (textView9.visibility == View.GONE) {
                    textView9.visibility = View.VISIBLE
                    textView10.visibility = View.VISIBLE
                    textView11.visibility = View.VISIBLE
                } else {
                    textView9.visibility = View.GONE
                    textView10.visibility = View.GONE
                    textView11.visibility = View.GONE
                }
                return true
            }
           R.id.item6 -> {
               //튜토리얼
               val intent2 = Intent(this, Tutorial2Activity::class.java)
               startActivity(intent2)
               return true
           }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onStop() {
        super.onStop()
        if(gpsRunning==running) {
            gpsManager.stopLocationUpdates()
            gpsRunning= stopped
        }
    }

    override fun onRestart() {
        super.onRestart()
        if(gpsRunning==stopped) {
            gpsManager.getUserLocation()
        }
    }

}
