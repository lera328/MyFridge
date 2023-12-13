package com.example.myfridge2.Tools

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class ProductItem(
    @PrimaryKey(autoGenerate = true)
    var Id: Int? = null,
    @ColumnInfo(name = "name")
    var name: String,
   @ColumnInfo(name = "purchaseDate")
   var purchaseDate: String?,
   @ColumnInfo(name = "endDate")
   var endDate: String?
)