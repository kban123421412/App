package com.example.moneymatters.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.moneymatters.data.model.GoalModel


@Dao
interface GoalDao{
    @Insert
    fun insertGoal(goal: GoalModel)

    @Update
    fun updateGoal(goal: GoalModel)

    @Query("SELECT * FROM goal_table ORDER BY ID DESC")
    fun getAllGoals(): LiveData<List<GoalModel>>

}