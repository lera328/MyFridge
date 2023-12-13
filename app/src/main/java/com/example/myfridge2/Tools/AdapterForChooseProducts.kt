package com.example.myfridge2.Tools

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.example.myfridge2.R
import com.example.myfridge2.databinding.ProductFoChooseItemBinding

class AdapterForChooseProducts : RecyclerView.Adapter<AdapterForChooseProducts.Holder>() {
    var productList = ArrayList<ProductItem>()
    val selectedProductsList = ArrayList<ProductItem>()

    inner class Holder(item: View) : RecyclerView.ViewHolder(item) {
        val binding = ProductFoChooseItemBinding.bind(item)
        //public val selectedProductsList = ArrayList<ProductItem>()
        @RequiresApi(Build.VERSION_CODES.O)

        public fun bind(product: ProductItem) {
            binding.tvProductNAme.text = product.name
            binding.checkBox.isChecked = false
            binding.checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    selectedProductsList.add(product)
                } else {
                    selectedProductsList.remove(product)
                }
            }

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.product_fo_choose_item, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int {
        return productList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(productList[position])
    }

    fun addProductList(producList: List<ProductItem>) {
        productList = ArrayList(producList)
        notifyDataSetChanged()
    }
    fun getSelectedProducts(): List<ProductItem> {
        return selectedProductsList
    }
    fun clearSelections() {
        selectedProductsList.clear()
        notifyDataSetChanged()
    }
}