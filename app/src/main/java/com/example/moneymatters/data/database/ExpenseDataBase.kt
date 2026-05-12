package com.example.moneymatters.data.database

import androidx.room.Database
import com.example.moneymatters.data.dao.ExpenseDao
import com.example.moneymatters.data.model.ExpenseModel
import android.content.Context
import androidx.room.RoomDatabase
import androidx.room.Room
import com.example.moneymatters.data.dao.GoalDao
import com.example.moneymatters.data.model.GoalModel



@Database(entities = [ExpenseModel::class, GoalModel::class], version = 2, exportSchema = false)
abstract class ExpenseDataBase: RoomDatabase() {
    abstract fun expenseDao(): ExpenseDao
    abstract fun goalDao(): GoalDao


    companion object{
        @Volatile
        private var INSTANCE: ExpenseDataBase? = null

        fun getDatabase(context: Context): ExpenseDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ExpenseDataBase::class.java,
                    "expense_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}