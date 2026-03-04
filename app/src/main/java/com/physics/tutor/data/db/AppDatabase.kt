package com.physics.tutor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.physics.tutor.data.db.dao.AppSettingDao
import com.physics.tutor.data.db.dao.QuestionDao
import com.physics.tutor.data.db.dao.TestResultDao
import com.physics.tutor.data.db.entity.AppSetting
import com.physics.tutor.data.db.entity.Question
import com.physics.tutor.data.db.entity.TestResult
import com.physics.tutor.util.DatabasePopulator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Главная Room-база данных приложения.
 *
 * При первом создании (onCreate) автоматически заполняется
 * начальным набором вопросов через [DatabasePopulator].
 *
 * Синглтон — создаётся один раз через [getInstance].
 */
@Database(
    entities = [Question::class, TestResult::class, AppSetting::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDao
    abstract fun testResultDao(): TestResultDao
    abstract fun appSettingDao(): AppSettingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "physics_test.db"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Заполняем вопросы при первом создании БД
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    DatabasePopulator.populate(database.questionDao())
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
