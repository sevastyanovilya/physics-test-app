package com.physics.tutor.ui.tutor

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.physics.tutor.data.db.entity.Question
import com.physics.tutor.databinding.ActivityQuestionEditorBinding
import com.physics.tutor.viewmodel.TutorViewModel

/**
 * Редактор вопросов — позволяет добавить новый или отредактировать существующий вопрос.
 *
 * Если передан EXTRA_QUESTION_ID — загружаем существующий вопрос для редактирования.
 * Если нет — создаём новый.
 *
 * Все поля валидируются перед сохранением: текст вопроса, четыре варианта ответа,
 * правильный ответ, класс и уровень сложности.
 */
class QuestionEditorActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_QUESTION_ID = "extra_question_id"
    }

    private lateinit var binding: ActivityQuestionEditorBinding
    private val viewModel: TutorViewModel by viewModels()
    private var editingQuestion: Question? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionEditorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupButtons()

        // Если передан ID — загружаем вопрос для редактирования
        val questionId = intent.getIntExtra(EXTRA_QUESTION_ID, -1)
        if (questionId != -1) {
            loadQuestion(questionId)
        }

        viewModel.operationSuccess.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun setupSpinners() {
        // Выбор класса
        val grades = listOf("7", "8", "9", "10", "11")
        binding.spinnerGrade.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, grades).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Уровень сложности
        val difficulties = listOf("1 — Начальный", "2 — Базовый", "3 — Продвинутый")
        binding.spinnerDifficulty.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, difficulties).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        // Правильный ответ
        val answers = listOf("A — Вариант А", "B — Вариант Б", "C — Вариант В", "D — Вариант Г")
        binding.spinnerCorrectAnswer.adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, answers).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
    }

    private fun setupButtons() {
        binding.btnSave.setOnClickListener { saveQuestion() }
        binding.btnCancel.setOnClickListener { finish() }
    }

    private fun loadQuestion(id: Int) {
        viewModel.allQuestions.observe(this) { questions ->
            val q = questions.find { it.id == id } ?: return@observe
            editingQuestion = q
            binding.tvTitle.text = "Редактировать вопрос"
            binding.etTopic.setText(q.topic)
            binding.etQuestionText.setText(q.questionText)
            binding.etOptionA.setText(q.optionA)
            binding.etOptionB.setText(q.optionB)
            binding.etOptionC.setText(q.optionC)
            binding.etOptionD.setText(q.optionD)

            // Устанавливаем spinner-значения
            binding.spinnerGrade.setSelection(q.grade - 7) // 7 → 0, 8 → 1, ...
            binding.spinnerDifficulty.setSelection(q.difficulty - 1)
            val answerIndex = listOf("A", "B", "C", "D").indexOf(q.correctAnswer)
            if (answerIndex >= 0) binding.spinnerCorrectAnswer.setSelection(answerIndex)
        }
    }

    private fun saveQuestion() {
        val topic    = binding.etTopic.text.toString().trim()
        val text     = binding.etQuestionText.text.toString().trim()
        val optA     = binding.etOptionA.text.toString().trim()
        val optB     = binding.etOptionB.text.toString().trim()
        val optC     = binding.etOptionC.text.toString().trim()
        val optD     = binding.etOptionD.text.toString().trim()

        // Валидация
        if (topic.isEmpty() || text.isEmpty() || optA.isEmpty() ||
            optB.isEmpty() || optC.isEmpty() || optD.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        val grade      = binding.spinnerGrade.selectedItemPosition + 7
        val difficulty = binding.spinnerDifficulty.selectedItemPosition + 1
        val correctAnswer = listOf("A", "B", "C", "D")[binding.spinnerCorrectAnswer.selectedItemPosition]

        val question = Question(
            id = editingQuestion?.id ?: 0,
            grade = grade,
            topic = topic,
            difficulty = difficulty,
            questionText = text,
            optionA = optA,
            optionB = optB,
            optionC = optC,
            optionD = optD,
            correctAnswer = correctAnswer
        )

        if (editingQuestion != null) {
            viewModel.updateQuestion(question)
        } else {
            viewModel.addQuestion(question)
        }
    }
}
