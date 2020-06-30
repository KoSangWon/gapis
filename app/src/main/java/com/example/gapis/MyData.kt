package com.example.gapis

import java.io.Serializable

//이름 현재온도 희망온도 순
class MyData(
    var power:String,
    var intensity:String,
    var name:String,
    var homeTemp:Int,
    var desiredTemp:Int,
    var iconDrawable: Int
             ) :Serializable{
}