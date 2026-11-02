package com.calculator.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CalculationDao {

    @Query("SELECT * FROM calculation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<CalculationRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculation(record: CalculationRecord)

    @Delete
    suspend fun deleteCalculation(record: CalculationRecord)

    @Query("DELETE FROM calculation_history")
    suspend fun clearAll()
}
