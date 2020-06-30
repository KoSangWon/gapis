package com.example.gapis.function

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.view.Gravity
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import com.example.gapis.activity.DBActivity

class MyDBHelper(val context: Context,val table1: TableLayout,val table2: TableLayout):SQLiteOpenHelper(context,
    DB_NAME,null,
    DB_VERSION
){
    companion object{
        val DB_VERSION = 1
        val DB_NAME = "gapisdb.db"
        val TABLE_NAME= arrayOf("temp","distance")

    }

    //1~200 속력까지 시간 초기값 설정
    fun initInsert(){
        val db=this.writableDatabase
        for(i in 30 downTo 25){
            val values=ContentValues()
            values.put("out",i)
            values.put("t30",5)
            values.put("t29",5)
            values.put("t28",5)
            values.put("t27",5)
            values.put("t26",5)
            values.put("t25",5)
            db?.insert(TABLE_NAME[0],null,values)
        }

        for(i in 1..200){
            val values=ContentValues()
            values.put("velocity",i)
            val time = ((4000.0/i.toDouble())*0.0875).toInt()
            values.put("time", time)
            db?.insert(TABLE_NAME[1],null,values)
        }
        db.close()
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val create_table = "create table if not exists "+ TABLE_NAME[0]+"("+
                "out integer primary key,t30 integer,t29 integer,t28 integer,t27 integer,t26 integer,t25 integer)"
        val create_table_1 = "create table if not exists "+ TABLE_NAME[1]+"("+
                "velocity integer primary key,time real)"
        db?.execSQL(create_table)
        db?.execSQL(create_table_1)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val drop_table_1 = "drop table if exists $TABLE_NAME[0]"
        val drop_table_2 = "drop table if exists $TABLE_NAME[1]"
        db?.execSQL(drop_table_1)
        db?.execSQL(drop_table_2)
        onCreate(db)
    }

    //속력, 시간 받고 update 해주는 함수
    fun updateTime(key:Int, value:Int):Boolean{
        val strsql = "select * from " + TABLE_NAME[1] + " where velocity = $key"
        val db = this.writableDatabase
        val cursor = db.rawQuery(strsql,null)
        if(cursor.moveToFirst()){
            val values=ContentValues()
            values.put("time",value)
            db.update(TABLE_NAME[1],values,"velocity=?", arrayOf(key.toString()))
            cursor.close()
            db.close()
            return true
        }
        else{
            cursor.close()
            db.close()
            return false
        }

    }

    //속력 받으면 시간 select 해주는 함수
    fun findTime(key: Int):Int{
        val strsql = "select * from " + TABLE_NAME[1] + " where velocity = $key"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql, null)
        var value = 0
        cursor.moveToFirst()
        if(cursor.count != 0){
            value = cursor.getInt(1)
            cursor.close()
            db.close()
            return value;
        }
        cursor.close()
        db.close()
        return value
    }


    fun showDB(){
        showTable(table1,0)
        showTable(table2,1)
    }

    fun showTable(table:TableLayout,tableNum:Int){
        val strsql = "select * from ${TABLE_NAME[tableNum]}"
        val db = this.readableDatabase
        val cursor = db.rawQuery(strsql,null)
        if(cursor.count!=0){
            cursor.moveToFirst()
            val count=cursor.columnCount
            val recordcount=cursor.count
            val activity = context as DBActivity
            table.removeAllViewsInLayout()

            val tablerow = TableRow(activity)
            val rowParams = TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT,count.toFloat())
            tablerow.layoutParams=rowParams
            val viewParam = TableRow.LayoutParams(0,100,1f)
            for(i in 0 until count){
                val textView = TextView(activity)
                textView.layoutParams = viewParam
                textView.text = cursor.getColumnName(i)
                textView.setBackgroundColor(Color.LTGRAY)
                textView.textSize = 15.0f
                textView.gravity = Gravity.CENTER
                tablerow.addView(textView)
            }
            table.addView(tablerow)

            do{
                val row = TableRow(activity)
                row.layoutParams = rowParams
                for(i in 0 until count){
                    val textView=TextView(activity)
                    textView.layoutParams = viewParam
                    textView.text = cursor.getString(i)
                    textView.textSize = 13.0f
                    textView.tag = i
                    textView.gravity = Gravity.CENTER
                    row.addView(textView)
                }
                table.addView(row)
            }while(cursor.moveToNext())
        }
    }


}