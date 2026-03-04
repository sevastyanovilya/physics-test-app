package com.physics.tutor.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.physics.tutor.data.db.entity.Question

/**
 * DAO для работы с вопросами тестов.
 * Все методы, кроме LiveData-потоков, вызываются из корутин (suspend).
 */
@Dao
interface QuestionDao {

    /** Получить все вопросы для указанного класса (для построения теста). */
    @Query("SELECT * FROM questions WHERE grade = :grade ORDER BY difficulty, RANDOM()")
    suspend fun getQuestionsByGrade(grade: Int): List<Question>

    /** Получить вопросы по классу и разделу. */
    @Query("SELECT * FROM questions WHERE grade = :grade AND topic = :topic ORDER BY difficulty")
    suspend fun getQuestionsByGradeAndTopic(grade: Int, topic: String): List<Question>

    /** Все вопросы — для экрана редактирования в режиме репетитора. */
    @Query("SELECT * FROM questions ORDER BY grade, topic, difficulty")
    fun getAllQuestionsLive(): LiveData<List<Question>>

    /** Количество вопросов в БД — для проверки при инициализации. */
    @Query("SELECT COUNT(*) FROM questions")
    suspend fun getCount(): Int

    /** Добавить новый вопрос. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(question: Question)

    /** Добавить список вопросов (при инициализации БД). */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(questions: List<Question>)

    /** Обновить существующий вопрос. */
    @Update
    suspend fun update(question: Question)

    /** Удалить вопрос. */
    @Delete
    suspend fun delete(question: Question)

    /** Получить вопрос по ID. */
    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getById(id: Int): Question?
}
