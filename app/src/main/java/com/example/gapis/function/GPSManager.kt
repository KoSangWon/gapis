package com.example.gapis.function

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.location.Location
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.TableLayout
import androidx.appcompat.app.AlertDialog
import com.example.gapis.R
import com.example.gapis.activity.MainActivity
import com.example.gapis.activity.MainActivity.Companion.running
import com.google.android.gms.location.*
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.concurrent.timer

class GPSManager(val context: Context,val home:LatLng){

    companion object{
        val NEARHOME=1
        val FARHOME=2
        val SPEEDMEAUSRE=3
        val AWAYHOME=4
        val NOTDEFINED=5

        val NEARBOUNDARY=15
        val FARBOUNDARY =400
        val SPEEDBOUNDARY = 500
    }
    var myDBHelper: MyDBHelper
    var fusedLocationClient : FusedLocationProviderClient?=null
    var locationCallBack: LocationCallback?=null
    var locationRequest: LocationRequest?=null

    var dialog: AlertDialog.Builder = AlertDialog.Builder(context)
    var locationNow:Location?=null
    var locationHome:Location?=null
    var locationFar:Location?=null
    var maxSpeed=0

    var distance=0f
    var start=false

    //for test
    val editText=(context as MainActivity).textView9
    val editText2=(context as MainActivity).textView10
    val editText3=(context as MainActivity).textView11


    var whereHome= NOTDEFINED
    var secondsExpect=0
    var timeInFar=0
    var goingHome=false


    init{
        dialog.setTitle("에어컨 스케쥴 알림")
        dialog.setMessage("쾌적한 집을 위해 에어컨을 도착 전에 미리 켜둘까요?")
        dialog.setIcon(R.mipmap.ic_launcher)
        var dialogListener= DialogInterface.OnClickListener { dialog, which ->
            when(which){
                DialogInterface.BUTTON_POSITIVE->
                    goingHome=true
            }
        }
        dialog.setPositiveButton("YES",dialogListener)
        dialog.setNegativeButton("NO",null)

        myDBHelper= MyDBHelper(context, TableLayout(context), TableLayout(context))
        getUserLocation()
    }


    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            locationNow=it
            locationHome=Location(locationNow)
            locationHome?.latitude=home.latitude
            locationHome?.longitude=home.longitude



            /*
            distance = locationHome!!.distanceTo(locationNow)
            if(distance>HOMEBOUNDARY){
                val distances=myDBHelper.getDistanceDB()
                for(i in 0..9){
                    rangePerMinute[i]=distances[i]
                }

                if(distance > rangePerMinute[9]){
                    whereHome=AWAYHOME
                    startLocationUpdates()
                }
            }
            else {
                startLocationUpdates()
            }
            */
            if(locationHome!=null) {
                distance = locationHome!!.distanceTo(locationNow)

                val main=context as MainActivity
                if(distance > NEARHOME && main.showData[0].power=="ON"){
                    main.makeNotification()
                }

                if (distance > SPEEDBOUNDARY) {
                    whereHome = AWAYHOME
                    startLocationUpdates()
                    (context as MainActivity).gpsRunning = running
                }
            }

        }

    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates(){
        locationRequest = LocationRequest.create()?.apply{
            interval =100
            fastestInterval = 100
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallBack = object:LocationCallback(){
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult?:return
                for(location in locationResult.locations){
                    locationNow=location
                    onLocationChanged(location)
                }
            }
        }
        fusedLocationClient?.requestLocationUpdates(
            locationRequest,
            locationCallBack,
            Looper.getMainLooper()
        )
    }


    fun stopLocationUpdates(){
        fusedLocationClient?.removeLocationUpdates(locationCallBack)
    }

    fun calculateTime() : Int{

        //y=10x-210 x: 온도 (섭씨) y: 초
        var outTemp=(context as MainActivity).outTemp  //외부 온도
        var desiredTemp = (context as MainActivity).showData[0].desiredTemp
        var homeTemp = (context as MainActivity).showData[0].homeTemp

        var sec=10
        //(context as MainActivity).
        return sec;
    }

    fun onLocationChanged(location: Location?) {



        locationNow = location
        distance = locationHome!!.distanceTo(locationNow)

        //for test
        var la = String.format("%.5f", location?.latitude)
        var lo = String.format("%.5f", location?.longitude)
        editText.text = "위치감지 위도 : $la, 경도 : $lo"
        editText2.setText("거리 계산 $distance")
        Log.i("로그 -> 거리", "$distance")
        //Log.i("로그 -> 거리계산","$distance")




        if (distance < SPEEDBOUNDARY && whereHome== AWAYHOME) {
            whereHome = SPEEDMEAUSRE
            if(maxSpeed<location!!.speed) {
                maxSpeed = location!!.speed.toInt()
                Log.i("로그 - > 최대 속도 갱신","$maxSpeed")
            }
        }

        if(distance < FARBOUNDARY && whereHome == SPEEDMEAUSRE ){
            whereHome = FARHOME

            dialog.show() // -> 질문
            start=true
            locationFar = Location(locationNow)
            secondsExpect = myDBHelper.findTime(maxSpeed) - calculateTime() //-> 예상 시간받아오기
            //secondsExpect=60-calculateTime()
            Log.i("로그 -> 예상시간(초)","$secondsExpect")

            Log.i("로그 -> ","타이머 시작")
            timer(period = 1000, initialDelay = 1000) {

                Log.i("로그 -> 타이머 시간", "$timeInFar")
                if(distance< NEARBOUNDARY){
                    Log.i("로그 -> 집 도착 거리, 시간","$distance $timeInFar")
                    if(goingHome) {
                        myDBHelper.updateTime(maxSpeed,timeInFar) //-> 시간 기록하기
                        goingHome=false
                        whereHome= NEARHOME
                    }
                    cancel()
                }
                timeInFar++;
            }
        }

        if(whereHome== NEARHOME){
            (context as MainActivity).SetAirCon("ON", "약")
            stopLocationUpdates()
        }

        if(goingHome && start){
            (context as MainActivity).adapter.holders[0]!!.time.visibility= View.VISIBLE
            (context as MainActivity).adapter.holders[0]!!.time.text="약 ${secondsExpect-timeInFar} 초 뒤에 켜질 예정입니다."

        }
        if(goingHome&&start && timeInFar>=secondsExpect){
            Log.i("로그 -> 에어컨 킴","$timeInFar")
            (context as MainActivity).SetAirCon("ON", "강")
            (context as MainActivity).adapter.holders[0]!!.time.visibility= View.GONE
            start=false
        }

    }
}