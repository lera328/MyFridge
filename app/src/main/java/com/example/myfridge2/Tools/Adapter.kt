package com.example.myfridge2.Tools

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myfridge2.R
import com.example.myfridge2.databinding.FridgeItemBinding
import layout.MainDb
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class Adapter : RecyclerView.Adapter<Adapter.productHolder>() {

    public var productList = ArrayList<ProductItem>()

    class productHolder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = FridgeItemBinding.bind(item)

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(product: ProductItem) {
            binding.tvName.text = product.name
            binding.textData.text =
                DataManager.getDifferenceInDays(product.purchaseDate, product.endDate)
            val prettyDateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
            //val formattedDate = prettyDateFormat.format(product.endDate)
            binding.tvPDay.text = product.purchaseDate
            binding.tvEDay.text = product.endDate
            binding.btPlus.setOnClickListener {
                var curDay = binding.tvEDay.text.toString()
                val dateFormat = SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
                ) // Формат даты соответствует строке "27.10.2023"
                val date = dateFormat.parse(curDay)
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                val newDate = calendar.time
                val newDateString = dateFormat.format(newDate)
                binding.tvEDay.text = newDateString // Обновляем отображаемую дату


                product.endDate =
                    newDateString // предполагается, что в вашем классе ProductItem есть поле endDate
                val db: MainDb = MainDb.getDb(itemView.context)

                Thread {
                    db.getDao().updateItem(product)

                }.start()

                binding.textData.text = (binding.textData.text.toString().toInt() + 1).toString()

            }
            binding.btMinus.setOnClickListener {
                var curDay = binding.tvEDay.text.toString()
                val dateFormat = SimpleDateFormat(
                    "dd.MM.yyyy",
                    Locale.getDefault()
                ) // Формат даты соответствует строке "27.10.2023"
                val date = dateFormat.parse(curDay)
                val calendar = Calendar.getInstance()
                calendar.time = date
                calendar.add(Calendar.DAY_OF_MONTH, -1)
                val newDate = calendar.time
                val newDateString = dateFormat.format(newDate)
                binding.tvEDay.text = newDateString // Обновляем отображаемую дату


                product.endDate =
                    newDateString // предполагается, что в вашем классе ProductItem есть поле endDate
                val db: MainDb = MainDb.getDb(itemView.context)

                Thread {
                    db.getDao().updateItem(product)

                }.start()

                binding.textData.text = (binding.textData.text.toString().toInt() - 1).toString()

            }

        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): productHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.fridge_item, parent, false)
        return productHolder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: productHolder, position: Int) {
        holder.bind(productList[position])
    }

    fun addProduct(product: ProductItem) {
        productList.add(product)
        notifyDataSetChanged()
    }

    fun addProductList(producList: List<ProductItem>) {
        productList = ArrayList(producList)
        notifyDataSetChanged()
    }


}