package com.physics.tutor.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Вопрос теста по физике.
 *
 * @param id            Уникальный идентификатор (автоинкремент)
 * @param grade         Класс: 7, 8, 9, 10 или 11
 * @param topic         Раздел физики: "Механика", "Термодинамика", "Электричество", "Оптика", "Волны", "Квантовая физика" и т.д.
 * @param difficulty    Уровень сложности: 1 = начальный, 2 = базовый, 3 = продвинутый
 * @param questionText  Текст вопроса
 * @param optionA       Вариант ответа А
 * @param optionB       Вариант ответа Б
 * @param optionC       Вариант ответа В
 * @param optionD       Вариант ответа Г
 * @param correctAnswer Правильный ответ: "A", "B", "C" или "D"
 */
@Entity(tableName = "questions")
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val grade: Int,
    val topic: String,
    val difficulty: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String
)
