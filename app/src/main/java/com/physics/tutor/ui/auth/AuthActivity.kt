package com.physics.tutor.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.physics.tutor.PhysicsApp
import com.physics.tutor.databinding.ActivityAuthBinding
import com.physics.tutor.ui.grade.GradeSelectActivity
import com.physics.tutor.ui.tutor.TutorLoginActivity
import kotlinx.coroutines.launch

/**
 * Экран авторизации ученика.
 *
 * Предоставляет два способа идентификации:
 *
 *   1. UUID (автоматический) — кнопка «Войти без PIN».
 *      На устройстве генерируется и сохраняется короткий UUID (8 символов).
 *      Ученик видит свой UUID и может его запомнить.
 *      Это главный рекомендуемый способ — не требует ввода.
 *
 *   2. PIN (4 цифры) — ученик вводит свой PIN вручную.
 *      PIN хранится локально и не привязан к ФИО (соответствие ФЗ-152).
 *
 * В нижней части экрана — кнопка перехода в режим репетитора.
 */
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding
    private val repo by lazy { PhysicsApp.instance.repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadOrShowUuid()
        setupButtons()
    }

    /** Загружает сохранённый UUID или генерирует новый, отображает на экране. */
    private fun loadOrShowUuid() {
        lifecycleScope.launch {
            val uuid = repo.getOrCreateStudentUuid()
            binding.tvUuidValue.text = uuid
        }
    }

    private fun setupButtons() {
        // ── Войти по UUID (без ввода) ──
        binding.btnLoginUuid.setOnClickListener {
            lifecycleScope.launch {
                val uuid = repo.getOrCreateStudentUuid()
                openGradeSelect(uuid)
            }
        }

        // ── Показать/скрыть поле для ввода PIN ──
        binding.btnShowPinEntry.setOnClickListener {
            val visible = binding.layoutPinEntry.visibility == View.VISIBLE
            binding.layoutPinEntry.visibility = if (visible) View.GONE else View.VISIBLE
        }

        // ── Войти по PIN ──
        binding.btnLoginPin.setOnClickListener {
            val pin = binding.etPin.text.toString().trim()
            if (pin.length != 4 || !pin.all { it.isDigit() }) {
                Toast.makeText(this, "Введите ровно 4 цифры", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            lifecycleScope.launch {
                repo.saveStudentPin(pin)
                openGradeSelect(pin)
            }
        }

        // ── Режим репетитора ──
        binding.btnTutorMode.setOnClickListener {
            startActivity(Intent(this, TutorLoginActivity::class.java))
        }
    }

    private fun openGradeSelect(studentId: String) {
        val intent = Intent(this, GradeSelectActivity::class.java).apply {
            putExtra(GradeSelectActivity.EXTRA_STUDENT_ID, studentId)
        }
        startActivity(intent)
    }
}
