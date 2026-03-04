package com.physics.tutor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Хранилище настроек приложения в формате "ключ — значение".
 *
 * Используется для хранения:
 *   - Хэша пароля репетитора (ключ: "tutor_password_hash")
 *   - UUID ученика (ключ: "student_uuid")
 *   - Флага первого запуска (ключ: "first_run")
 */
@Entity(tableName = "app_settings")
data class AppSetting(
    @PrimaryKey
    val key: String,
    val value: String
)
