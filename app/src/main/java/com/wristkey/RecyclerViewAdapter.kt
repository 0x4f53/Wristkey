package com.wristkey

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray


data class Token(val id: String, val accountName: String, val code: String, val counter: String)

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
    override fun getItemCount(): Int {
        return tokenList.size
    }
    //the class is holding the list view
    class ViewHolder(context: Context, itemView: View) : RecyclerView.ViewHolder(itemView) {
        val context = context
        fun bindItems(token: Token) {
            var currentTheme = appData.getString("theme", "000000")
            val timeCard=itemView.findViewById<CardView>(R.id.TimeCard)
            val accountName = itemView.findViewById<TextView>(R.id.ServiceName)
            val code  = itemView.findViewById<TextView>(R.id.Code)
            if (currentTheme == "F7F7F7") {
                timeCard.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                accountName.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                code.setTextColor(ColorStateList.valueOf(Color.parseColor("#000000")))
                accountName.text = "white"
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
            accountName.isSelected = true
            code.text = token.code.replace("...".toRegex(), "$0 ")
            val tokenId=token.id
            accountName.setOnLongClickListener {
                val intent = Intent(context, ManualEntryActivity::class.java)
                intent.putExtra("token_id", tokenId)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            }

            code.setOnLongClickListener {
                val qrcodeData: String
                val sitename: String
                val accountNameForQRCode: String
                if (token.accountName.contains("(") && token.accountName.contains(")")) {
                    accountNameForQRCode = token.accountName.substringAfter("(").substringBefore(")")
                    sitename = token.accountName.substringBefore("(")
                    qrcodeData = "otpauth://totp/${accountNameForQRCode}?secret=${JSONArray(logins.getString(tokenId, null))[1]}&issuer=${sitename}" // where 1 is the array index for the secret
                } else {
                    sitename = token.accountName.substringBefore("(")
                    qrcodeData = "otpauth://totp/?secret=${JSONArray(logins.getString(tokenId, null))[1]}&issuer=${sitename}" // where 1 is the array index for the secret
                }

                val intent = Intent(context, QRCodeActivity::class.java)
                intent.putExtra("qr_data", qrcodeData)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
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
        fun bindItems(token: Token) {
            var currentTheme = appData.getString("theme", "000000")
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
            accountName.isSelected = true
            code.text = token.code.replace("...".toRegex(), "$0 ")
            counter.text = "#"+token.counter
            val currentCounterValue=token.counter.toInt()
            val tokenId=token.id
            code.setOnClickListener{
                var newCounterValue=currentCounterValue+1
                val getCurrentData=(appData.getString(token.id, "").toString())
                val newData=getCurrentData+newCounterValue
                logins.edit().putString(tokenId, newData).apply()
                logins.edit().apply()
                code.text = "Code used"
                Handler().postDelayed({
                    code.text = token.code
                }, 5000)
            }

            accountName.setOnLongClickListener{
                val intent = Intent(context, ManualEntryActivity::class.java)
                intent.putExtra("token_id", tokenId)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            }

            code.setOnLongClickListener {
                val qrcodeData: String
                val sitename: String
                val accountNameForQRCode: String
                if (token.accountName.contains("(") && token.accountName.contains(")")) {
                    accountNameForQRCode = token.accountName.substringAfter("(").substringBefore(")")
                    sitename = token.accountName.substringBefore("(")
                    qrcodeData = "otpauth://hotp/${accountNameForQRCode}?secret=${JSONArray(appData.getString(tokenId, null))[2]}&issuer=${sitename}" // where 2 is the array index for the secret
                } else {
                    sitename = token.accountName.substringBefore("(")
                    qrcodeData = "otpauth://hotp/?secret=${JSONArray(appData.getString(tokenId, null))[2]}&issuer=${sitename}" // where 2 is the array index for the secret
                }

                val intent = Intent(context, QRCodeActivity::class.java)
                intent.putExtra("qr_data", qrcodeData)
                intent.flags = FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                true
            }
        }
    }
}