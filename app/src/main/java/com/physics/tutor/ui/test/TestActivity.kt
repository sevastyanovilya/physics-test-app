package com.physics.tutor.ui.test

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.physics.tutor.R
import com.physics.tutor.databinding.ActivityTestBinding
import com.physics.tutor.ui.result.ResultActivity
import com.physics.tutor.viewmodel.TestViewModel

/**
 * Экран прохождения теста.
 *
 * Ключевые элементы UI:
 *   - Два прогресс-индикатора и метки времени: общий таймер теста + таймер на вопрос
 *   - Текст вопроса и четыре кнопки вариантов ответа (A, Б, В, Г)
 *   - Кнопка «Следующий вопрос» / «Завершить тест»
 *   - Индикатор прогресса (вопрос X из N)
 *
 * Логика:
 *   - При выборе ответа кнопка выделяется зелёным фоном (#2E7D32).
 *   - Остальные кнопки возвращаются к стандартному стилю.
 *   - При истечении таймера вопроса → автоматический переход к следующему.
 *   - При истечении общего таймера или последнем вопросе → сохранение результата.
 *   - После сохранения → переход на ResultActivity.
 */
class TestActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GRADE = "extra_grade"
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    private lateinit var binding: ActivityTestBinding
    private val viewModel: TestViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val grade = intent.getIntExtra(EXTRA_GRADE, 9)
        val studentId = intent.getStringExtra(EXTRA_STUDENT_ID) ?: "unknown"

        binding.tvGradeTitle.text = "$grade класс"

        // ── Запускаем тест через ViewModel ──
        viewModel.startTest(grade, studentId)

        observeViewModel()
        setupButtons()
    }

    private fun observeViewModel() {
        // Отображение текущего вопроса
        viewModel.currentQuestion.observe(this) { question ->
            if (question == null) return@observe
            binding.tvQuestionText.text = question.questionText
            binding.btnOptionA.text = "А.  ${question.optionA}"
            binding.btnOptionB.text = "Б.  ${question.optionB}"
            binding.btnOptionC.text = "В.  ${question.optionC}"
            binding.btnOptionD.text = "Г.  ${question.optionD}"
            binding.tvTopic.text = question.topic
            updateDifficultyLabel(question.difficulty)
        }

        // Прогресс (вопрос X из N)
        viewModel.currentIndex.observe(this) { idx ->
            val total = 15 // maxCount из репозитория
            binding.tvProgress.text = "Вопрос ${idx + 1} из $total"
            binding.progressQuestions.progress = ((idx + 1) * 100 / total)
            // Метка кнопки: на последнем вопросе — «Завершить тест»
            binding.btnNext.text = if (idx + 1 >= total) "Завершить тест" else "Следующий вопрос"
        }

        // Подсветка выбранного ответа
        viewModel.selectedAnswer.observe(this) { selected ->
            resetOptionButtons()
            when (selected) {
                "A" -> highlightButton(binding.btnOptionA)
                "B" -> highlightButton(binding.btnOptionB)
                "C" -> highlightButton(binding.btnOptionC)
                "D" -> highlightButton(binding.btnOptionD)
            }
        }

        // Общий таймер теста (30 мин)
        viewModel.testTimeLeftMs.observe(this) { ms ->
            binding.tvTestTimer.text = "Тест: ${formatTime(ms)}"
            // Красный цвет, когда осталось менее 5 минут
            val colorRes = if (ms < 5 * 60 * 1000L)
                getColor(R.color.error_red) else getColor(R.color.text_primary)
            binding.tvTestTimer.setTextColor(colorRes)
        }

        // Таймер на вопрос (60 сек)
        viewModel.questionTimeLeftMs.observe(this) { ms ->
            val sec = (ms / 1000).toInt()
            binding.tvQuestionTimer.text = "$sec с"
            binding.progressQuestionTimer.progress = (sec * 100 / 60)
            // Красный цвет, когда менее 10 секунд
            val colorRes = if (sec <= 10)
                getColor(R.color.error_red) else getColor(R.color.green_primary)
            binding.tvQuestionTimer.setTextColor(colorRes)
            binding.progressQuestionTimer.setIndicatorColor(getColor(
                if (sec <= 10) R.color.error_red else R.color.green_primary
            ))
        }

        // Загрузка данных
        viewModel.isLoading.observe(this) { loading ->
            binding.layoutLoading.visibility = if (loading) View.VISIBLE else View.GONE
            binding.layoutTest.visibility = if (loading) View.GONE else View.VISIBLE
        }

        // Переход к результатам
        viewModel.testResult.observe(this) { result ->
            if (result != null) {
                val intent = Intent(this, ResultActivity::class.java).apply {
                    putExtra(ResultActivity.EXTRA_RESULT_ID, result.id)
                    putExtra(ResultActivity.EXTRA_RESULT_CODE, result.resultCode)
                    putExtra(ResultActivity.EXTRA_CORRECT, result.correctAnswers)
                    putExtra(ResultActivity.EXTRA_TOTAL, result.totalQuestions)
                    putExtra(ResultActivity.EXTRA_LEVEL, result.level)
                    putExtra(ResultActivity.EXTRA_GRADE, result.grade)
                }
                startActivity(intent)
                finish()
            }
        }
    }

    private fun setupButtons() {
        binding.btnOptionA.setOnClickListener { viewModel.selectAnswer("A") }
        binding.btnOptionB.setOnClickListener { viewModel.selectAnswer("B") }
        binding.btnOptionC.setOnClickListener { viewModel.selectAnswer("C") }
        binding.btnOptionD.setOnClickListener { viewModel.selectAnswer("D") }
        binding.btnNext.setOnClickListener    { viewModel.nextQuestion() }
    }

    /** Сбрасывает все кнопки ответов к стандартному стилю. */
    private fun resetOptionButtons() {
        listOf(binding.btnOptionA, binding.btnOptionB, binding.btnOptionC, binding.btnOptionD)
            .forEach { btn ->
                btn.setBackgroundResource(R.drawable.bg_option_normal)
                btn.setTextColor(getColor(R.color.text_primary))
            }
    }

    /** Выделяет выбранную кнопку зелёным фоном. */
    private fun highlightButton(btn: Button) {
        btn.setBackgroundResource(R.drawable.bg_option_selected)
        btn.setTextColor(getColor(R.color.white))
    }

    /** Устанавливает текстовый лейбл уровня сложности. */
    private fun updateDifficultyLabel(difficulty: Int) {
        binding.tvDifficulty.text = when (difficulty) {
            1    -> "★☆☆ Начальный"
            2    -> "★★☆ Базовый"
            3    -> "★★★ Продвинутый"
            else -> ""
        }
    }

    /** Форматирует миллисекунды в строку MM:SS. */
    private fun formatTime(ms: Long): String {
        val totalSec = ms / 1000
        val min = totalSec / 60
        val sec = totalSec % 60
        return "%02d:%02d".format(min, sec)
    }

    // Запрещаем возврат назад во время теста — чтобы не сбить результат
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Намеренно не вызываем super — блокируем кнопку "Назад" во время теста
    }
}
