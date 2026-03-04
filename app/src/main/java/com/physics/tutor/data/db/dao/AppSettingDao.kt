package com.physics.tutor.data.db.dao

import androidx.room.*
import com.physics.tutor.data.db.entity.AppSetting

/**
 * DAO для настроек приложения (ключ–значение).
 */
@Dao
interface AppSettingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(setting: AppSetting)

    @Query("SELECT * FROM app_settings WHERE `key` = :key LIMIT 1")
    suspend fun get(key: String): AppSetting?

    @Query("DELETE FROM app_settings WHERE `key` = :key")
    suspend fun delete(key: String)
}
