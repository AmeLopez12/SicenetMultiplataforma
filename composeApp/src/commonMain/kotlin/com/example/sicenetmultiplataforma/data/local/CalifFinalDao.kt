package com.example.sicenetmultiplataforma.data.local

import androidx.room.*
import com.example.sicenetmultiplataforma.data.model.CalifFinal
import kotlinx.coroutines.flow.Flow

@Dao
interface CalifFinalDao {
    @Query("SELECT * FROM calificaciones_finales ORDER BY id ASC")
    fun getAllCalifFinal(): Flow<List<CalifFinal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(califFinal: List<CalifFinal>)

    @Query("DELETE FROM calificaciones_finales")
    suspend fun deleteAll()

    @Query("SELECT MAX(lastUpdate) FROM calificaciones_finales")
    suspend fun getLastUpdateTime(): Long?
}