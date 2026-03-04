package com.physics.tutor.viewmodel

import androidx.lifecycle.*
import com.physics.tutor.PhysicsApp
import com.physics.tutor.data.db.entity.Question
import com.physics.tutor.data.db.entity.TestResult
import com.physics.tutor.data.repository.AppRepository
import kotlinx.coroutines.launch

/**
 * ViewModel для режима репетитора.
 *
 * Управляет:
 *   - Авторизацией репетитора (проверка/установка пароля)
 *   - Просмотром результатов учеников с фильтрацией
 *   - Поиском результата по коду
 *   - Добавлением, редактированием и удалением вопросов
 */
class TutorViewModel : ViewModel() {

    private val repository: AppRepository = PhysicsApp.instance.repository

    // ── Результаты авторизации ──
    private val _loginResult = MutableLiveData<Boolean?>(null)
    val loginResult: LiveData<Boolean?> = _loginResult

    private val _isPasswordSet = MutableLiveData<Boolean?>(null)
    val isPasswordSet: LiveData<Boolean?> = _isPasswordSet

    private val _operationSuccess = MutableLiveData<String?>(null)
    val operationSuccess: LiveData<String?> = _operationSuccess

    // ── Результаты тестов ──
    val allResults: LiveData<List<TestResult>> = repository.getAllResultsLive()

    private val _filteredGrade = MutableLiveData<Int?>(null)

    /** Результаты с учётом фильтра по классу (null = все классы). */
    val filteredResults: LiveData<List<TestResult>> = MediatorLiveData<List<TestResult>>().apply {
        fun update() {
            val grade = _filteredGrade.value
            val all = allResults.value ?: emptyList()
            value = if (grade == null) all else all.filter { it.grade == grade }
        }
        addSource(allResults) { update() }
        addSource(_filteredGrade) { update() }
    }

    private val _foundResult = MutableLiveData<TestResult?>(null)
    val foundResult: LiveData<TestResult?> = _foundResult

    // ── Вопросы ──
    val allQuestions: LiveData<List<Question>> = repository.getAllQuestionsLive()

    // ── Авторизация ──

    fun checkPasswordSet() {
        viewModelScope.launch {
            _isPasswordSet.value = repository.isTutorPasswordSet()
        }
    }

    fun setPassword(password: String) {
        viewModelScope.launch {
            repository.setTutorPassword(password)
            _operationSuccess.value = "Пароль репетитора установлен"
        }
    }

    fun login(password: String) {
        viewModelScope.launch {
            _loginResult.value = repository.checkTutorPassword(password)
        }
    }

    // ── Фильтрация ──

    fun setGradeFilter(grade: Int?) {
        _filteredGrade.value = grade
    }

    // ── Поиск по коду ──

    fun findByCode(code: String) {
        viewModelScope.launch {
            _foundResult.value = repository.getResultByCode(code.trim())
        }
    }

    fun clearFoundResult() {
        _foundResult.value = null
    }

    // ── Управление вопросами ──

    fun addQuestion(question: Question) {
        viewModelScope.launch {
            repository.insertQuestion(question)
            _operationSuccess.value = "Вопрос добавлен"
        }
    }

    fun updateQuestion(question: Question) {
        viewModelScope.launch {
            repository.updateQuestion(question)
            _operationSuccess.value = "Вопрос обновлён"
        }
    }

    fun deleteQuestion(question: Question) {
        viewModelScope.launch {
            repository.deleteQuestion(question)
            _operationSuccess.value = "Вопрос удалён"
        }
    }

    fun deleteResult(result: TestResult) {
        viewModelScope.launch {
            repository.deleteResult(result)
        }
    }
}
