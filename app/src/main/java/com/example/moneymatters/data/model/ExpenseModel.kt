package com.example.moneymatters.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ExpenseModel")
data class ExpenseModel(
    @PrimaryKey
    val id: Int? = null,
    val title: String,
    val amount: Double,
    val category: String,
    val date: String
)
