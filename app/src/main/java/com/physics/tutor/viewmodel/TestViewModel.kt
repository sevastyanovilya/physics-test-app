package com.physics.tutor.viewmodel

import android.os.CountDownTimer
import androidx.lifecycle.*
import com.physics.tutor.PhysicsApp
import com.physics.tutor.data.db.entity.Question
import com.physics.tutor.data.db.entity.TestResult
import com.physics.tutor.data.repository.AppRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана тестирования.
 *
 * Управляет:
 *   - Загрузкой вопросов для выбранного класса
 *   - Двумя таймерами: общий (30 мин) и на вопрос (60 сек)
 *   - Текущим состоянием теста (вопрос, выбранный ответ, прогресс)
 *   - Сохранением результата и генерацией кода
 *
 * Важно: таймеры останавливаются при уничтожении ViewModel (onCleared),
 * чтобы не было утечек памяти.
 */
class TestViewModel : ViewModel() {

    private val repository: AppRepository = PhysicsApp.instance.repository

    // ── Константы таймеров ──
    companion object {
        const val TEST_DURATION_MS = 30 * 60 * 1000L   // 30 минут
        const val QUESTION_DURATION_MS = 60 * 1000L     // 60 секунд
        const val TIMER_INTERVAL_MS = 1000L              // Обновление раз в секунду
    }

    // ── Список вопросов и текущий индекс ──
    private val _questions = MutableLiveData<List<Question>>(emptyList())

    private val _currentIndex = MutableLiveData(0)
    val currentIndex: LiveData<Int> = _currentIndex

    /** Текущий вопрос, вычисляемый из индекса и списка. */
    val currentQuestion: LiveData<Question?> = MediatorLiveData<Question?>().apply {
        fun update() {
            val list = _questions.value ?: return
            val idx = _currentIndex.value ?: return
            value = if (idx < list.size) list[idx] else null
        }
        addSource(_questions) { update() }
        addSource(_currentIndex) { update() }
    }

    /** Словарь ответов: индекс вопроса → выбранный вариант ("A"/"B"/"C"/"D"). */
    private val userAnswers = mutableMapOf<Int, String>()

    /** Текущий выбранный ответ для подсветки в UI. */
    private val _selectedAnswer = MutableLiveData<String?>(null)
    val selectedAnswer: LiveData<String?> = _selectedAnswer

    // ── Таймер всего теста ──
    private val _testTimeLeftMs = MutableLiveData(TEST_DURATION_MS)
    val testTimeLeftMs: LiveData<Long> = _testTimeLeftMs
    private var testTimer: CountDownTimer? = null

    // ── Таймер на текущий вопрос ──
    private val _questionTimeLeftMs = MutableLiveData(QUESTION_DURATION_MS)
    val questionTimeLeftMs: LiveData<Long> = _questionTimeLeftMs
    private var questionTimer: CountDownTimer? = null

    // ── Флаги состояния ──
    private val _isTestFinished = MutableLiveData(false)
    val isTestFinished: LiveData<Boolean> = _isTestFinished

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    /** Результат после завершения теста — передаётся в ResultActivity. */
    private val _testResult = MutableLiveData<TestResult?>(null)
    val testResult: LiveData<TestResult?> = _testResult

    // ── Класс и идентификатор ученика ──
    private var grade = 0
    private var studentId = ""
    private var testStartTimeMs = 0L

    /**
     * Загружает вопросы и запускает тест.
     * Вызывается из TestActivity после получения grade и studentId из Intent.
     */
    fun startTest(grade: Int, studentId: String) {
        if (_questions.value?.isNotEmpty() == true) return // Уже запущен
        this.grade = grade
        this.studentId = studentId
        this.testStartTimeMs = System.currentTimeMillis()

        _isLoading.value = true
        viewModelScope.launch {
            val questions = repository.getTestQuestions(grade, maxCount = 15)
            _questions.value = questions
            _isLoading.value = false
            startTestTimer()
            startQuestionTimer()
        }
    }

    /** Записывает выбор ученика для текущего вопроса. */
    fun selectAnswer(answer: String) {
        val idx = _currentIndex.value ?: return
        if (_isTestFinished.value == true) return
        userAnswers[idx] = answer
        _selectedAnswer.value = answer
    }

    /** Переход к следующему вопросу или завершение теста. */
    fun nextQuestion() {
        val idx = _currentIndex.value ?: return
        val count = _questions.value?.size ?: return
        questionTimer?.cancel()

        if (idx + 1 < count) {
            _currentIndex.value = idx + 1
            _selectedAnswer.value = userAnswers[idx + 1]
            startQuestionTimer()
        } else {
            finishTest()
        }
    }

    // ── Таймеры ──

    private fun startTestTimer() {
        testTimer?.cancel()
        testTimer = object : CountDownTimer(TEST_DURATION_MS, TIMER_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                _testTimeLeftMs.value = millisUntilFinished
            }
            override fun onFinish() {
                _testTimeLeftMs.value = 0
                finishTest()
            }
        }.start()
    }

    private fun startQuestionTimer() {
        questionTimer?.cancel()
        _questionTimeLeftMs.value = QUESTION_DURATION_MS
        questionTimer = object : CountDownTimer(QUESTION_DURATION_MS, TIMER_INTERVAL_MS) {
            override fun onTick(millisUntilFinished: Long) {
                _questionTimeLeftMs.value = millisUntilFinished
            }
            override fun onFinish() {
                _questionTimeLeftMs.value = 0
                // Вопрос пропущен — переходим к следующему
                nextQuestion()
            }
        }.start()
    }

    /** Завершает тест, считает результат и сохраняет в БД. */
    private fun finishTest() {
        if (_isTestFinished.value == true) return
        _isTestFinished.value = true
        testTimer?.cancel()
        questionTimer?.cancel()

        val questions = _questions.value ?: return
        val correct = questions.indices.count { idx ->
            userAnswers[idx] == questions[idx].correctAnswer
        }
        val elapsed = ((System.currentTimeMillis() - testStartTimeMs) / 1000).toInt()

        viewModelScope.launch {
            val result = repository.saveResult(
                studentId = studentId,
                grade = grade,
                correctAnswers = correct,
                totalQuestions = questions.size,
                timeSpentSeconds = elapsed
            )
            _testResult.value = result
        }
    }

    override fun onCleared() {
        super.onCleared()
        testTimer?.cancel()
        questionTimer?.cancel()
    }
}
