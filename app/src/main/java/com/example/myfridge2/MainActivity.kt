package com.example.myfridge2

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myfridge2.Tools.Adapter
import com.example.myfridge2.Tools.NotificationService


import com.example.myfridge2.Tools.ProductItem
import com.example.myfridge2.Tools.Scaner
import com.example.myfridge2.Tools.SwipeToDelRecycler
import com.example.myfridge2.Tools.channelId
import com.example.myfridge2.Tools.messageExtra
import com.example.myfridge2.Tools.titleExtra
import com.example.myfridge2.databinding.ActivityMainBinding
import layout.MainDb
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private val REQUEST_CODE_SCANNER = 1
    private val adapter = Adapter()
    lateinit var db: MainDb
    private var productList = ArrayList<ProductItem>()

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation.selectedItemId = R.id.home

        binding.bottomNavigation.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.about -> {
                    startActivity(Intent(this@MainActivity, MyBeActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }

                R.id.dashboard -> {
                    startActivity(Intent(this@MainActivity, ReceptActivity::class.java))
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                    true
                }

                R.id.home -> {
                    true
                }

                else -> false
            }
        }

        db = MainDb.getDb(this)

        db.getDao().getAllItems().asLiveData().observe(this) { items ->
            productList.clear()
            productList.addAll(items)
            adapter.addProductList(productList)
            adapter.notifyDataSetChanged()
        }

        binding.rcView.layoutManager = LinearLayoutManager(this@MainActivity)
        binding.rcView.adapter = adapter

        init()
    }

    //получение textQr
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCANNER && resultCode == RESULT_OK) {
            val textQr = data?.getStringExtra("textQr")
            // Делайте необходимые действия с полученным textQr
            createNotification()
            getNames(textQr)
            init()
        }
    }

    override fun onResume() {
        super.onResume()
        db.getDao().getAllItems().asLiveData().observe(this) {
            productList.clear() // Очистить список перед добавлением новых элементов
            productList.addAll(it)
            adapter.addProductList(productList)
        }
    }

    //в адаптер лист кладем
    private fun init() {
        //удаление
        val swipeToDelRecycler = object : SwipeToDelRecycler() {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val item = productList[position]

                db = MainDb.getDb(this@MainActivity)
                Thread {
                    db.getDao().deleteItem(item)

                    // Обновляем список productList после удаления
                    productList.removeAt(position)

                }.start()
                db.getDao().getAllItems().asLiveData().observe(this@MainActivity) { items ->
                    productList.clear()
                    productList.addAll(items)
                    adapter.addProductList(productList)
                    adapter.notifyDataSetChanged()
                    adapter.notifyItemRemoved(position)
                }

            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDelRecycler)
        itemTouchHelper.attachToRecyclerView(binding.rcView)
    }


    //парсинг json
    fun getNames(s: String?): String {
        try {
            val jsonObject = JSONObject(s)
            val itemsArray =
                jsonObject.getJSONObject("data").getJSONObject("json").getJSONArray("items")

            val dateTimeString =
                jsonObject.getJSONObject("data").getJSONObject("json").getString("dateTime")
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateTimeString)
            val prettyDateFormat = SimpleDateFormat("dd.MM.yyyy ", Locale.getDefault())
            val formattedDate = prettyDateFormat.format(date)


            var result: String = ""
            val calendar = Calendar.getInstance()
            calendar.time = date
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val newDate = calendar.time
            val formattedDayEnd = prettyDateFormat.format(newDate)
            calendar.set(Calendar.HOUR_OF_DAY, 1)
            calendar.set(Calendar.MINUTE, 57)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)// на 9 утра пока настройка

            for (i in 0 until itemsArray.length()) {
                val itemObject = itemsArray.getJSONObject(i)
                val name = itemObject.getString("name")

                //val product = Product(i, name, date, date)
                Log.println(Log.ERROR, "oooooooo", calendar.time.toString())

                val productItem = ProductItem(null, name, formattedDate, formattedDayEnd)
                Thread {
                    db.getDao().insertItem(productItem)
                }.start()
                val id: Int = productList.size + i
                Log.println(Log.ERROR, "oooooooo", id.toString())
                scheduleNotification(
                    "Мой холодильник",
                    "Скоро истечет срок годности у " + productItem.name,
                    calendar.time,
                    id
                )
            }
            Log.println(Log.ERROR, "oooooooo", result + formattedDate)
            return result + formattedDate
        } catch (e: Exception) {
            Log.println(Log.ERROR, "qqq", e.message.toString())
        }
        return "null"
    }




    public fun updateProduct(product: ProductItem) {
        db = MainDb.getDb(this)

        // Вызываем метод обновления в DAO
        db.getDao()
            .updateItem(product) // предполагается, что вы имеете метод updateItem в вашем Dao, который обновляет запись в базе данных
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notif Channel"
            val desc = "A description of the channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance)
            channel.description = desc

            // Указание звука для уведомлений. Звук должен находиться в папке res/raw.
            val soundUri =
                Uri.parse("android.resource://" + applicationContext.packageName + "/" + R.raw.my_notification_sound)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()
            // Устанавливаем звук и вибрацию для канала.
            channel.setSound(soundUri, audioAttributes)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    public fun scheduleNotification(
        title: String,
        message: String,
        date: Date,
        notificationId_: Int
    ) {
        val intent = Intent(applicationContext, NotificationService::class.java)
        intent.putExtra(titleExtra, title)
        intent.putExtra(messageExtra, message)
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            notificationId_,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        Log.d("Notification", "Notification ID for $notificationId_")
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager


        val alarmTime = date.time // П
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            date.time,
            pendingIntent
        )
    }
    //переход к сканеру
    fun onClick(view: View) {
        try {
            val intent = Intent(this, Scaner::class.java)
            startActivityForResult(intent, REQUEST_CODE_SCANNER)
        }catch (e:Exception){
            Log.e("qqq", e.message.toString())
        }
    }
}