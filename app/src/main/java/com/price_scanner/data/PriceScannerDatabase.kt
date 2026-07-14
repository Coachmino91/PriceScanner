package com.price_scanner.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.price_scanner.data.dao.ScannedProductDao
import com.price_scanner.data.entities.ScannedProduct

@Database(entities = [ScannedProduct::class], version = 1, exportSchema = false)
abstract class PriceScannerDatabase : RoomDatabase() {

    abstract fun scannedProductDao(): ScannedProductDao

    companion object {
        @Volatile
        private var INSTANCE: PriceScannerDatabase? = null

        fun getDatabase(context: Context): PriceScannerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PriceScannerDatabase::class.java,
                    "price_scanner_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
