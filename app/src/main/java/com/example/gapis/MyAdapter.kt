package com.example.gapis

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_main.view.*

class MyAdapter(val items:ArrayList<MyData>): RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    var holders= arrayOfNulls<MyViewHolder>(2)

    interface OnSwitchClickListener {
        fun onSwitchClick(holder: MyViewHolder, view: View, data: MyData, position: Int)
    }
    var switchClickListener: OnSwitchClickListener? = null
    //길게 누르기
    interface OnItemLongClickListener {
        fun onItemLongClick(holder: MyViewHolder, view: View, data: MyData, position: Int):Boolean
    }
    var itemLongClickListener: OnItemLongClickListener? = null

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val system_tv: TextView = itemView.findViewById(R.id.system)
        val current_tv: TextView = itemView.findViewById(R.id.current)
        val desired_tv: TextView = itemView.findViewById(R.id.desiredTem)
        val switch:Switch=itemView.findViewById(R.id.switchMain)
        val intensity : TextView = itemView.findViewById(R.id.intensity)
        val icon:ImageView=itemView.findViewById(R.id.icon)
        val info:LinearLayout=itemView.infoLayout
        val time:TextView=itemView.timeView

        init {
            switch.setOnClickListener{
                switchClickListener?.onSwitchClick(
                    this, it,items[adapterPosition], adapterPosition
                )
            }
            itemView.setOnLongClickListener {
                itemLongClickListener?.onItemLongClick(
                    this, it, items[adapterPosition], adapterPosition
                )!!
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_main, parent, false)
        return MyViewHolder(v)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holders[position]=holder
        holder.system_tv.text=items[position].name
        holder.current_tv.text=items[position].homeTemp.toString()
        holder.desired_tv.text=items[position].desiredTemp.toString()
        holder.switch.isChecked=items[position].power=="ON"
        holder.intensity.text = items[position].intensity.toString()
        holder.icon.setImageResource(items[position].iconDrawable)

        if(items[position].power=="ON") {
            holder.info.visibility = View.VISIBLE
            holder.time.visibility=View.GONE
        }
        else
            holder.info.visibility=View.GONE
    }
}