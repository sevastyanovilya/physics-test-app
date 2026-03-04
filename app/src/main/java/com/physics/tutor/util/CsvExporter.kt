package com.physics.tutor.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.physics.tutor.data.db.entity.TestResult
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

/**
 * Экспорт результатов тестов в CSV-файл.
 *
 * Файл сохраняется в директорию Documents (Android 10+)
 * или на внешнее хранилище (Android 7–9).
 *
 * После создания файла возвращает Intent для открытия
 * через любое приложение (Google Drive, почта и т.д.).
 */
object CsvExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Экспортирует список результатов в CSV и возвращает Intent для шаринга.
     *
     * @return Intent для ACTION_SEND или null при ошибке
     */
    fun exportAndShare(context: Context, results: List<TestResult>): Intent? {
        return try {
            val file = createCsvFile(context, results)
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Результаты тестов по физике")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createCsvFile(context: Context, results: List<TestResult>): File {
        // Сохраняем во внутреннее хранилище (не требует разрешений)
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val fileName = "physics_results_${System.currentTimeMillis()}.csv"
        val file = File(dir, fileName)

        FileWriter(file, false).use { writer ->
            // Заголовок
            writer.write("ID;Ученик;Класс;Правильных;Всего;Время(с);Уровень;Код;Дата\n")
            // Данные
            results.forEach { r ->
                val date = dateFormat.format(Date(r.timestamp))
                writer.write(
                    "${r.id};${r.studentId};${r.grade} класс;" +
                    "${r.correctAnswers};${r.totalQuestions};${r.timeSpentSeconds};" +
                    "${r.level};${r.resultCode};$date\n"
                )
            }
        }
        return file
    }
}
