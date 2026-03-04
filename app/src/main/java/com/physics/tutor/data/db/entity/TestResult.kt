package com.physics.tutor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Результат прохождения теста учеником.
 *
 * Уровень знаний определяется по проценту правильных ответов:
 *   0–40%  → "Начальный"
 *   41–70% → "Базовый"
 *   71–100% → "Продвинутый"
 *
 * @param resultCode  6-значный числовой код, который ученик показывает репетитору.
 * @param studentId   UUID или PIN ученика — не содержит ФИО (соответствие ФЗ-152).
 */
@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val studentId: String,          // UUID или PIN
    val grade: Int,                  // Класс (7–11)
    val correctAnswers: Int,         // Кол-во правильных ответов
    val totalQuestions: Int,         // Всего вопросов в тесте
    val timeSpentSeconds: Int,       // Затраченное время в секундах
    val level: String,               // "Начальный" / "Базовый" / "Продвинутый"
    val resultCode: String,          // 6-значный код для репетитора
    val timestamp: Long = System.currentTimeMillis()
)
