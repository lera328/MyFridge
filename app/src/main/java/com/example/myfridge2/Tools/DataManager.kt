package com.example.myfridge2.Tools

import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import java.text.ParseException
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class DataManager {
    companion object {
    @RequiresApi(Build.VERSION_CODES.O)
    public fun getDifferenceInDays(date1:String?, date2:String?):String{
        try {
            val dateString1 = date1?.replace(" ", "")
            val dateString2 = date2?.replace(" ", "")
            val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
            val date1 = LocalDate.parse(dateString1, formatter)
            val date2 = LocalDate.parse(dateString2, formatter)
            val differenceInDays = ChronoUnit.DAYS.between(date1, date2)

            return differenceInDays.toString()
        } catch (e: ParseException) {
            e.printStackTrace()
            return ("exception")
        }
    }

    }
}