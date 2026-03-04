package com.physics.tutor.data.repository

import android.content.Context
import androidx.lifecycle.LiveData
import com.physics.tutor.data.db.AppDatabase
import com.physics.tutor.data.db.entity.AppSetting
import com.physics.tutor.data.db.entity.Question
import com.physics.tutor.data.db.entity.TestResult
import java.security.MessageDigest
import kotlin.random.Random

/**
 * Репозиторий — единственная точка доступа к данным для ViewModel.
 *
 * Отвечает за:
 *   - Загрузку вопросов для теста с перемешиванием
 *   - Сохранение результатов и генерацию кода результата
 *   - Управление настройками (пароль репетитора, UUID ученика)
 *   - Бизнес-логику определения уровня знаний
 */
class AppRepository(context: Context) {

    private val db = AppDatabase.getInstance(context)
    private val questionDao = db.questionDao()
    private val resultDao = db.testResultDao()
    private val settingDao = db.appSettingDao()

    // ────────────────────────────── ВОПРОСЫ ──────────────────────────────

    /**
     * Возвращает список вопросов для теста заданного класса.
     * Перемешивает вопросы и ограничивает количество (по умолчанию 15).
     */
    suspend fun getTestQuestions(grade: Int, maxCount: Int = 15): List<Question> {
        val all = questionDao.getQuestionsByGrade(grade)
        return all.shuffled().take(maxCount)
    }

    fun getAllQuestionsLive(): LiveData<List<Question>> = questionDao.getAllQuestionsLive()

    suspend fun insertQuestion(question: Question) = questionDao.insert(question)
    suspend fun updateQuestion(question: Question) = questionDao.update(question)
    suspend fun deleteQuestion(question: Question) = questionDao.delete(question)
    suspend fun getQuestionById(id: Int): Question? = questionDao.getById(id)

    // ────────────────────────────── РЕЗУЛЬТАТЫ ──────────────────────────────

    /**
     * Сохраняет результат теста.
     * Автоматически вычисляет уровень знаний и генерирует 6-значный код.
     * Возвращает сохранённый объект с кодом.
     */
    suspend fun saveResult(
        studentId: String,
        grade: Int,
        correctAnswers: Int,
        totalQuestions: Int,
        timeSpentSeconds: Int
    ): TestResult {
        val level = calculateLevel(correctAnswers, totalQuestions)
        val code = generateResultCode()
        val result = TestResult(
            studentId = studentId,
            grade = grade,
            correctAnswers = correctAnswers,
            totalQuestions = totalQuestions,
            timeSpentSeconds = timeSpentSeconds,
            level = level,
            resultCode = code
        )
        resultDao.insert(result)
        return result
    }

    /** Определяет уровень знаний по проценту правильных ответов. */
    fun calculateLevel(correct: Int, total: Int): String {
        if (total == 0) return "Начальный"
        val percent = correct * 100 / total
        return when {
            percent >= 71 -> "Продвинутый"
            percent >= 41 -> "Базовый"
            else          -> "Начальный"
        }
    }

    fun getAllResultsLive(): LiveData<List<TestResult>> = resultDao.getAllResultsLive()
    fun getResultsByGradeLive(grade: Int): LiveData<List<TestResult>> =
        resultDao.getResultsByGradeLive(grade)

    suspend fun getAllResults(): List<TestResult> = resultDao.getAllResults()

    /** Ищет результат по 6-значному коду, введённому репетитором. */
    suspend fun getResultByCode(code: String): TestResult? = resultDao.getByResultCode(code)

    suspend fun deleteResult(result: TestResult) = resultDao.delete(result)

    // ────────────────────────────── НАСТРОЙКИ ──────────────────────────────

    /** Устанавливает пароль репетитора, сохраняя только его SHA-256 хэш. */
    suspend fun setTutorPassword(password: String) {
        val hash = sha256(password)
        settingDao.set(AppSetting("tutor_password_hash", hash))
    }

    /** Проверяет введённый пароль репетитора. */
    suspend fun checkTutorPassword(password: String): Boolean {
        val stored = settingDao.get("tutor_password_hash") ?: return false
        return stored.value == sha256(password)
    }

    /** Возвращает true, если пароль репетитора уже создан. */
    suspend fun isTutorPasswordSet(): Boolean =
        settingDao.get("tutor_password_hash") != null

    /** Получает или создаёт UUID ученика на данном устройстве. */
    suspend fun getOrCreateStudentUuid(): String {
        val existing = settingDao.get("student_uuid")
        if (existing != null) return existing.value
        val newUuid = java.util.UUID.randomUUID().toString().take(8).uppercase()
        settingDao.set(AppSetting("student_uuid", newUuid))
        return newUuid
    }

    /** Сохраняет PIN, введённый учеником (не ФИО, соответствует ФЗ-152). */
    suspend fun saveStudentPin(pin: String) {
        settingDao.set(AppSetting("student_pin", pin))
    }

    suspend fun getStudentPin(): String? = settingDao.get("student_pin")?.value

    // ────────────────────────────── ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ──────────────────────────────

    /** SHA-256 хэш строки (для хранения пароля репетитора). */
    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(input.toByteArray(Charsets.UTF_8))
        return hash.joinToString("") { "%02x".format(it) }
    }

    /**
     * Генерирует уникальный 6-значный числовой код результата.
     * Диапазон: 100000–999999 (всегда 6 цифр, читабельно).
     */
    private fun generateResultCode(): String {
        return (100000 + Random.nextInt(900000)).toString()
    }
}
