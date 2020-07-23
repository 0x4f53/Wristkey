package com.owais.wristkey

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class TimeCardAdapter(context: Context, private val tokenList: ArrayList<Token>, val clickListener: (Token) -> Unit) : RecyclerView.Adapter<TimeCardAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    val context = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.time_card, parent, false)
        return ViewHolder(context, v)
    }
    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(tokenList[position])
        val item : Token = tokenList[position]
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }
    //this method is giving the size of the list
    override fun getItemCount() : Int {
        return tokenList.size
    }
    //the class is holding the list view
    class ViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = context
        private val storageFile = "app_storage"
        private val storage: SharedPreferences = context.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        val storageEditor: SharedPreferences.Editor =  storage.edit()
        fun bindItems(token: Token) {
            var currentTheme = storage.getString("theme", "000000")
            val timeCard=itemView.findViewById<CardView>(R.id.TimeCard)
            val accountName = itemView.findViewById<TextView>(R.id.ServiceName)
            val code  = itemView.findViewById<TextView>(R.id.Code)
            val counter = itemView.findViewById<TextView>(R.id.Counter)
            if (currentTheme == "F7F7F7") {
                timeCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            } else if (currentTheme == "192835") {
                timeCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3A4149"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            } else if (currentTheme == "000000") {
                timeCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0E1013"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            }
            accountName.text = token.accountName
            accountName.ellipsize = TextUtils.TruncateAt.MARQUEE
            accountName.isSelected=true
            accountName.isSingleLine=true
            code.text = token.code
            val tokenNumber=token.tokenNumber.toString()
            accountName.setOnLongClickListener{
                tokenNumberForDeleteActivity = tokenNumber
                context.startActivity(Intent(context, DeleteActivity::class.java))
                true
            }
        }
    }
}


class CounterCardAdapter(context: Context, private val tokenList: ArrayList<Token>, val clickListener: (Token) -> Unit) : RecyclerView.Adapter<CounterCardAdapter.ViewHolder>() {
    //this method is returning the view for each item in the list
    val context = context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.counter_card, parent, false)
        return ViewHolder(context, v)
    }
    //this method is binding the data on the list
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bindItems(tokenList[position])
        val item : Token = tokenList[position]
        holder.itemView.setOnClickListener {
            clickListener(item)
        }
    }
    //this method is giving the size of the list
    override fun getItemCount(): Int {
        return tokenList.size
    }
    //the class is holding the list view
    class ViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = context
        private val storageFile = "app_storage"
        private val storage: SharedPreferences = context.getSharedPreferences(storageFile, Context.MODE_PRIVATE)
        val storageEditor: SharedPreferences.Editor =  storage.edit()
        fun bindItems(token: Token) {
            var currentTheme = storage.getString("theme", "000000")
            val counterCard=itemView.findViewById<CardView>(R.id.CounterCard)
            val accountName = itemView.findViewById<TextView>(R.id.ServiceName)
            val code  = itemView.findViewById<TextView>(R.id.Code)
            val counter = itemView.findViewById<TextView>(R.id.Counter)
            if (currentTheme == "F7F7F7") {
                counterCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                counter.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
            } else if (currentTheme == "192835") {
                counterCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#3A4149"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                counter.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            } else if (currentTheme == "000000") {
                counterCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#0E1013"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
                counter.setTextColor(ColorStateList.valueOf(Color.parseColor("#FFFFFF")))
            }
            accountName.text = token.accountName
            accountName.ellipsize = TextUtils.TruncateAt.MARQUEE
            accountName.isSelected=true
            accountName.isSingleLine=true
            code.text = token.code
            counter.text = "#"+token.counter
            val currentCounterValue=token.counter.toInt()
            val tokenNumber=token.tokenNumber.toString()
            code.setOnClickListener{
                var newCounterValue=currentCounterValue+1
                val getCurrentData=(storage.getString(token.tokenNumber.toString(), "").toString()).replaceAfter("◆", "")
                val newData=getCurrentData+newCounterValue+"▮"
                storageEditor.putString(tokenNumber, newData)
                storageEditor.commit()
            }
            accountName.setOnLongClickListener{
                tokenNumberForDeleteActivity = tokenNumber
                context.startActivity(Intent(context, DeleteActivity::class.java))
                true
            }
        }
    }
}