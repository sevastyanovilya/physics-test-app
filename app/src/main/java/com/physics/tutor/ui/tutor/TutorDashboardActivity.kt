package com.physics.tutor.ui.tutor

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.physics.tutor.R
import com.physics.tutor.data.db.entity.TestResult
import com.physics.tutor.databinding.ActivityTutorDashboardBinding
import com.physics.tutor.util.CsvExporter
import com.physics.tutor.viewmodel.TutorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Панель управления репетитора.
 *
 * Вкладки (реализованы через LinearLayout + переключение видимости):
 *
 *   📊 РЕЗУЛЬТАТЫ — список всех результатов тестов с фильтром по классу.
 *      Нажатие на результат показывает полные детали.
 *      Кнопка «Найти по коду» — поиск результата по 6-значному коду ученика.
 *      Кнопка «Экспорт CSV» — экспортирует все данные через системный шаринг.
 *
 *   📝 ВОПРОСЫ — список всех вопросов с возможностью добавить/редактировать/удалить.
 */
class TutorDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTutorDashboardBinding
    private val viewModel: TutorViewModel by viewModels()
    private val resultsAdapter = ResultsAdapter()
    private val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale("ru"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupResultsList()
        setupGradeFilter()
        setupButtons()
        observeViewModel()

        // По умолчанию — вкладка результатов
        showTab(Tab.RESULTS)
    }

    private fun setupResultsList() {
        binding.rvResults.layoutManager = LinearLayoutManager(this)
        binding.rvResults.adapter = resultsAdapter
        resultsAdapter.onItemClick = { result -> showResultDetail(result) }
        resultsAdapter.onDeleteClick = { result ->
            AlertDialog.Builder(this)
                .setTitle("Удалить результат?")
                .setMessage("Результат ученика ${result.studentId} будет удалён безвозвратно.")
                .setPositiveButton("Удалить") { _, _ -> viewModel.deleteResult(result) }
                .setNegativeButton("Отмена", null)
                .show()
        }
    }

    private fun setupGradeFilter() {
        val grades = listOf("Все классы", "7 класс", "8 класс", "9 класс", "10 класс", "11 класс")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, grades)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGrade.adapter = adapter
        binding.spinnerGrade.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                val grade = if (pos == 0) null else pos + 6 // индекс 1 → класс 7
                viewModel.setGradeFilter(grade)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun setupButtons() {
        // ── Переключение вкладок ──
        binding.btnTabResults.setOnClickListener   { showTab(Tab.RESULTS) }
        binding.btnTabQuestions.setOnClickListener { showTab(Tab.QUESTIONS) }

        // ── Поиск по коду результата ──
        binding.btnSearchCode.setOnClickListener {
            val code = binding.etSearchCode.text.toString().trim()
            if (code.length != 6) {
                Toast.makeText(this, "Введите 6-значный код", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            viewModel.findByCode(code)
        }

        // ── Экспорт в CSV ──
        binding.btnExportCsv.setOnClickListener {
            exportResults()
        }

        // ── Добавить новый вопрос ──
        binding.btnAddQuestion.setOnClickListener {
            startActivity(Intent(this, QuestionEditorActivity::class.java))
        }
    }

    private fun observeViewModel() {
        // Обновляем список результатов
        viewModel.filteredResults.observe(this) { results ->
            resultsAdapter.submitList(results)
            binding.tvResultCount.text = "Всего результатов: ${results.size}"
            binding.tvEmptyResults.visibility = if (results.isEmpty()) View.VISIBLE else View.GONE
        }

        // Результат найден по коду
        viewModel.foundResult.observe(this) { result ->
            if (result != null) {
                showResultDetail(result)
                viewModel.clearFoundResult()
            } else if (binding.etSearchCode.text.isNotEmpty()) {
                Toast.makeText(this, "Результат с таким кодом не найден", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.operationSuccess.observe(this) { msg ->
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** Показывает диалог с подробным результатом теста. */
    private fun showResultDetail(result: TestResult) {
        val percent = if (result.totalQuestions > 0)
            result.correctAnswers * 100 / result.totalQuestions else 0
        val timeStr = formatTime(result.timeSpentSeconds)
        val date = dateFormat.format(Date(result.timestamp))

        val message = """
            👤 Ученик: ${result.studentId}
            📚 Класс: ${result.grade}
            ✅ Результат: ${result.correctAnswers} / ${result.totalQuestions} ($percent%)
            🏆 Уровень: ${result.level}
            ⏱ Время: $timeStr
            📅 Дата: $date
            🔑 Код: ${result.resultCode}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Результат теста")
            .setMessage(message)
            .setPositiveButton("Закрыть", null)
            .show()
    }

    private fun exportResults() {
        val results = viewModel.filteredResults.value
        if (results.isNullOrEmpty()) {
            Toast.makeText(this, "Нет данных для экспорта", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = CsvExporter.exportAndShare(this, results)
        if (intent != null) {
            startActivity(Intent.createChooser(intent, "Экспорт результатов"))
        } else {
            Toast.makeText(this, "Ошибка при создании файла", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showTab(tab: Tab) {
        binding.layoutResultsTab.visibility   = if (tab == Tab.RESULTS)   View.VISIBLE else View.GONE
        binding.layoutQuestionsTab.visibility = if (tab == Tab.QUESTIONS) View.VISIBLE else View.GONE
        binding.btnTabResults.isSelected   = (tab == Tab.RESULTS)
        binding.btnTabQuestions.isSelected = (tab == Tab.QUESTIONS)

        // Наблюдаем за вопросами только при открытии вкладки вопросов
        if (tab == Tab.QUESTIONS) {
            viewModel.allQuestions.observe(this) { questions ->
                binding.tvQuestionsCount.text = "Всего вопросов: ${questions.size}"
                // В упрощённой реализации показываем количество по классам
                val byGrade = questions.groupBy { it.grade }
                val summary = (7..11).joinToString("\n") { grade ->
                    "  ${grade} класс: ${byGrade[grade]?.size ?: 0} вопросов"
                }
                binding.tvQuestionsSummary.text = summary
            }
        }
    }

    private fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }

    enum class Tab { RESULTS, QUESTIONS }
}

// ════════════════════ АДАПТЕР РЕЗУЛЬТАТОВ ════════════════════

/** Простой RecyclerView-адаптер для отображения списка результатов тестов. */
class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.VH>() {

    var onItemClick: ((TestResult) -> Unit)? = null
    var onDeleteClick: ((TestResult) -> Unit)? = null
    private val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale("ru"))

    private var items: List<TestResult> = emptyList()

    fun submitList(newList: List<TestResult>) {
        items = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_result, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val result = items[position]
        val percent = if (result.totalQuestions > 0)
            result.correctAnswers * 100 / result.totalQuestions else 0
        val date = dateFormat.format(Date(result.timestamp))

        holder.tvStudentId.text = result.studentId
        holder.tvScore.text = "${result.correctAnswers}/${result.totalQuestions} ($percent%)"
        holder.tvLevel.text = result.level
        holder.tvGrade.text = "${result.grade} кл."
        holder.tvDate.text = date
        holder.tvCode.text = "# ${result.resultCode}"

        holder.itemView.setOnClickListener    { onItemClick?.invoke(result) }
        holder.btnDelete.setOnClickListener   { onDeleteClick?.invoke(result) }
    }

    override fun getItemCount() = items.size

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStudentId: TextView = itemView.findViewById(R.id.tvStudentId)
        val tvScore: TextView     = itemView.findViewById(R.id.tvScore)
        val tvLevel: TextView     = itemView.findViewById(R.id.tvLevel)
        val tvGrade: TextView     = itemView.findViewById(R.id.tvGrade)
        val tvDate: TextView      = itemView.findViewById(R.id.tvDate)
        val tvCode: TextView      = itemView.findViewById(R.id.tvCode)
        val btnDelete: View       = itemView.findViewById(R.id.btnDelete)
    }
}
