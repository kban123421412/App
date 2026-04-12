package com.example.moneymatters.data.dao

import androidx.room.Dao
import androidx.room.*
import com.example.moneymatters.data.model.ExpenseModel
import androidx.lifecycle.LiveData
import androidx.room.OnConflictStrategy


@Dao
interface ExpenseDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertExpense(expense: ExpenseModel)

    @Query("SELECT * FROM ExpenseModel ORDER BY ID DESC")
    fun getAllExpenses(): LiveData<List<ExpenseModel>>

    @Delete
    fun deleteExpense(expense: ExpenseModel)


    @Query("SELECT SUM(amount) FROM ExpenseModel")
    fun getTotalAmount(): LiveData<Double>

}