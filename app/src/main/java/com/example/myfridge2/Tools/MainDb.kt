package layout

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myfridge2.Tools.Dao
import com.example.myfridge2.Tools.ProductItem

@Database(entities = [ProductItem::class], version = 1)
abstract class MainDb : RoomDatabase() {
    abstract fun getDao(): Dao

    companion object {
        fun getDb(context: Context): MainDb {
            return Room.databaseBuilder(
                context.applicationContext,
                MainDb::class.java,
                "products.db"
            ).build()
        }
    }
}