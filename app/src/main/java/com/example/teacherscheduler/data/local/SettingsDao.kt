package com.example.teacherscheduler.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.teacherscheduler.model.AppSettings

@Dao
interface SettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(settings: AppSettings): Long

    @Update
    suspend fun update(settings: AppSettings)

    @Query("SELECT * FROM app_settings LIMIT 1")
    fun getSettings(): LiveData<AppSettings?>

    @Query("SELECT * FROM app_settings LIMIT 1")
    suspend fun getSettingsSync(): AppSettings?
}