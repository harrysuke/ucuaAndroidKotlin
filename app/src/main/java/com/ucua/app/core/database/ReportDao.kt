package com.ucua.app.core.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReportDao {

    @Query("SELECT * FROM cached_reports ORDER BY id DESC")
    fun observeAll(): Flow<List<ReportEntity>>

    @Query("SELECT * FROM cached_reports WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(reports: List<ReportEntity>): Unit

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(report: ReportEntity): Unit

    @Query("DELETE FROM cached_reports WHERE id = :id")
    suspend fun deleteById(id: Long): Unit

    @Query("DELETE FROM cached_reports")
    suspend fun clearAll(): Unit
}
