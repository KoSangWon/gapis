package com.example.gapis.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.example.gapis.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*

class MapActivity : AppCompatActivity() {


    var fusedLocationClient : FusedLocationProviderClient?=null

    lateinit var googleMap:GoogleMap
    lateinit var loc: Location
    lateinit var markedLoc:LatLng
    var from=-1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        init()
        initToolbar()
    }

    private fun initToolbar() {
        val toolbar: Toolbar = findViewById(R.id.toolbarMap)
        setSupportActionBar(toolbar)
        val actionbar = supportActionBar
        with(actionbar!!) {
            title="우리집 위치 설정하기"
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
        getUserLocation()

        val intent=getIntent()
        from=intent.getIntExtra("from",-1)
        setMapBtn.setOnClickListener {
            //색 표시
            setMapBtn.setBackgroundColor(Color.parseColor("#0078d4"))
            when(from){
                1->{
                    val intent= Intent(this,IntroActivity::class.java)
                    intent.putExtra("homeloc_lat",markedLoc.latitude)
                    intent.putExtra("homeloc_lon",markedLoc.longitude)
                    startActivity(intent)
                    ActivityCompat.finishAffinity(this)
                    finish()
                }
                2->{
                    val intent = Intent(this,MainActivity::class.java)
                    intent.putExtra("homeloc_lat",markedLoc.latitude)
                    intent.putExtra("homeloc_lon",markedLoc.longitude)
                    startActivity(intent)
                    ActivityCompat.finishAffinity(this)
                    finish()
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun getUserLocation(){
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient?.lastLocation?.addOnSuccessListener {
            loc = it
            markedLoc= LatLng(loc.latitude,loc.longitude)
            initmap()
        }
    }

    fun initmap(){
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync{
            googleMap = it
            var loc=LatLng(loc.latitude,loc.longitude)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(loc,16.0f))
            googleMap.setMinZoomPreference(10.0f)
            googleMap.setMaxZoomPreference(18.0f)
            val options= MarkerOptions()
            options.position(loc)
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            val mk1=googleMap.addMarker(options)
            initMapListener()
        }
    }

    fun initMapListener(){
        googleMap.setOnMapClickListener {
            markedLoc=it
            googleMap.clear()
            val options =MarkerOptions()
            options.position(it)
            options.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
            googleMap.addMarker(options)
        }
    }

}
