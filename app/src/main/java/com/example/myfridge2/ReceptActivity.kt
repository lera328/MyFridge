package com.example.myfridge2

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myfridge2.Tools.AdapterForChooseProducts
import com.example.myfridge2.Tools.ApiCallback
import com.example.myfridge2.Tools.OpenAIApiClient
import com.example.myfridge2.Tools.ProductItem
import com.example.myfridge2.databinding.ActivityReceptBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import layout.MainDb

class ReceptActivity : AppCompatActivity(), ApiCallback {
    lateinit var binding: ActivityReceptBinding
    private val adapterForChooseProducts = AdapterForChooseProducts()
    lateinit var db: MainDb
    private var productList = ArrayList<ProductItem>()
    private val mainHandler = Handler(Looper.getMainLooper())

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReceptBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigation.selectedItemId = R.id.dashboard

        binding.bottomNavigation.setOnNavigationItemSelectedListener(object :
            BottomNavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                when (item.itemId) {
                    R.id.home -> {
                        startActivity(Intent(this@ReceptActivity, MainActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        return true
                    }

                    R.id.about -> {
                        startActivity(Intent(this@ReceptActivity, MyBeActivity::class.java))
                        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
                        return true
                    }

                    R.id.dashboard -> {
                        return true
                    }

                }
                return false
            }
        })
        db = MainDb.getDb(this)
        db.getDao().getAllItems().asLiveData().observe(this) { items ->
            productList.clear()
            productList.addAll(items)
            adapterForChooseProducts.addProductList(productList)
            adapterForChooseProducts.notifyDataSetChanged()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this@ReceptActivity)
        binding.recyclerView.adapter = adapterForChooseProducts
        openAIApiClient = OpenAIApiClient(this, mainHandler)
    }

    override fun onResume() {
        super.onResume()
        db.getDao().getAllItems().asLiveData().observe(this) {
            productList.clear() // Очистить список перед добавлением новых элементов
            productList.addAll(it)
            adapterForChooseProducts.addProductList(productList)
        }
    }

    private lateinit var openAIApiClient: OpenAIApiClient
    fun onClick(view: View) {
        if (binding.button2.text == "Сгенерировать рецепт") {
            binding.progressBar.visibility=View.VISIBLE
            binding.textView6.visibility=View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.button2.setText("Заново")
            val selectedProducts = adapterForChooseProducts.getSelectedProducts()
            var string = ""
            for (i in selectedProducts) string += " ${i.name}"
            openAIApiClient.getResponse(string)
            binding.textViewResponse.visibility = View.VISIBLE
        } else {
            binding.recyclerView.visibility = View.VISIBLE
            binding.textView6.visibility = View.VISIBLE
            binding.button2.setText("Сгенерировать рецепт")
            adapterForChooseProducts.clearSelections()
            binding.textViewResponse.visibility = View.GONE
        }

    }

    override fun onSuccess(response: String) {
        runOnUiThread {
            binding.progressBar.visibility=View.GONE
            binding.textViewResponse.text = response
        }
        //binding.textViewResponse.text = response
    }

    override fun onFailure(errorMessage: String) {
        runOnUiThread {
            binding.progressBar.visibility=View.GONE
            binding.textViewResponse.text = "Ошибка генирации, попробуйте снова ($errorMessage)"
        }
    }
}
