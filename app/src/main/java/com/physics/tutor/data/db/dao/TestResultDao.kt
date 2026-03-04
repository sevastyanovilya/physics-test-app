package com.physics.tutor.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.physics.tutor.data.db.entity.TestResult

/**
 * DAO для работы с результатами тестов.
 */
@Dao
interface TestResultDao {

    /** Сохранить результат теста. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: TestResult)

    /** Получить все результаты — для панели репетитора (LiveData, обновляется автоматически). */
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResultsLive(): LiveData<List<TestResult>>

    /** Получить результаты по классу. */
    @Query("SELECT * FROM test_results WHERE grade = :grade ORDER BY timestamp DESC")
    fun getResultsByGradeLive(grade: Int): LiveData<List<TestResult>>

    /** Получить все результаты для экспорта в CSV (не LiveData, вызывается из корутины). */
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    suspend fun getAllResults(): List<TestResult>

    /** Найти результат по 6-значному коду (для репетитора). */
    @Query("SELECT * FROM test_results WHERE resultCode = :code LIMIT 1")
    suspend fun getByResultCode(code: String): TestResult?

    /** Удалить результат. */
    @Delete
    suspend fun delete(result: TestResult)
}
