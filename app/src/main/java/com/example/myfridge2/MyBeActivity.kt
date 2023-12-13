package com.example.myfridge2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.myfridge2.databinding.ActivityMyBeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import layout.MainDb

class MyBeActivity : AppCompatActivity() {
    lateinit var binding: ActivityMyBeBinding
    lateinit var db: MainDb

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyBeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.bottomNavigation.selectedItemId = R.id.about
        lifecycleScope.launch {
            val db = MainDb.getDb(this@MyBeActivity)
            val mostPopularProduct =
                withContext(Dispatchers.IO) { db.getDao().getMostPopularProduct() }
            binding.textView.text =
                "Самый популярный продукт за все время - ${mostPopularProduct.name}"
        }


        binding.bottomNavigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                // Ваша логика обработки выбора элемента навигации здесь
                when (item.itemId) {
                    R.id.home -> {
                        startActivity(Intent(this@MyBeActivity, MainActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        return true
                    }

                    R.id.dashboard -> {
                        startActivity(Intent(this@MyBeActivity, ReceptActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        return true
                    }

                    R.id.about -> {
                        return true
                    }

                }
                return false
            }
        })
    }
}


