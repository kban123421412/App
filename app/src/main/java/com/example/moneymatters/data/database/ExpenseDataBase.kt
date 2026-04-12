package com.example.moneymatters.data.database

import androidx.room.Database
import com.example.moneymatters.data.dao.ExpenseDao
import com.example.moneymatters.data.model.ExpenseModel
import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Room



@Database(entities = [ExpenseModel::class], version = 1, exportSchema = false)
abstract class ExpenseDataBase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao

    companion object{
        @Volatile
        private var INSTANCE: ExpenseDataBase? = null

        fun getDatabase(context: Context): ExpenseDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDataBase::class.java,
                    "expense_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}