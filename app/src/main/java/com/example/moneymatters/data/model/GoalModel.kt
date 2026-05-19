package com.example.moneymatters.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity ("goal_table")
data class GoalModel (
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val targetAmount: Double,
    var currentAmount: Double = 0.0 //starts goal at 0

)