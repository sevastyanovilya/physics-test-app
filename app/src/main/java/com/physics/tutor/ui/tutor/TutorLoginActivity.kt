package com.physics.tutor.ui.tutor

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.physics.tutor.databinding.ActivityTutorLoginBinding
import com.physics.tutor.viewmodel.TutorViewModel

/**
 * Экран входа в режим репетитора.
 *
 * Работает в двух режимах:
 *
 *   1. SETUP_MODE (первый запуск) — ввод нового пароля + подтверждение.
 *      Пароль хранится в виде SHA-256 хэша в локальной БД.
 *
 *   2. LOGIN_MODE (обычный вход) — ввод уже существующего пароля.
 *
 * После успешного входа открывается TutorDashboardActivity.
 */
class TutorLoginActivity : AppCompatActivity() {

    companion object {
        /** Если true — режим первоначальной установки пароля. */
        const val EXTRA_SETUP_MODE = "extra_setup_mode"
    }

    private lateinit var binding: ActivityTutorLoginBinding
    private val viewModel: TutorViewModel by viewModels()

    private var isSetupMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        isSetupMode = intent.getBooleanExtra(EXTRA_SETUP_MODE, false)

        if (isSetupMode) {
            // Режим первого запуска — показываем поле подтверждения пароля
            binding.tvTitle.text = "Установите пароль репетитора"
            binding.tvSubtitle.text = "Пароль защищает доступ к результатам учеников и редактированию вопросов. Запомните его — восстановление не предусмотрено."
            binding.layoutConfirm.visibility = View.VISIBLE
            binding.btnLogin.text = "Создать пароль"
        } else {
            // Обычный режим — только ввод пароля
            binding.tvTitle.text = "Режим репетитора"
            binding.tvSubtitle.text = "Введите пароль для доступа к результатам и вопросам"
            binding.layoutConfirm.visibility = View.GONE
            binding.btnLogin.text = "Войти"
        }

        binding.btnLogin.setOnClickListener { handleLogin() }

        // Кнопка "Назад к ученику" — только в режиме логина (не в setup)
        if (!isSetupMode) {
            binding.btnBack.visibility = View.VISIBLE
            binding.btnBack.setOnClickListener { finish() }
        }

        observeViewModel()
    }

    private fun handleLogin() {
        val password = binding.etPassword.text.toString().trim()

        if (password.length < 4) {
            Toast.makeText(this, "Пароль должен содержать минимум 4 символа", Toast.LENGTH_SHORT).show()
            return
        }

        if (isSetupMode) {
            val confirm = binding.etPasswordConfirm.text.toString().trim()
            if (password != confirm) {
                Toast.makeText(this, "Пароли не совпадают", Toast.LENGTH_SHORT).show()
                return
            }
            viewModel.setPassword(password)
        } else {
            viewModel.login(password)
        }
    }

    private fun observeViewModel() {
        viewModel.loginResult.observe(this) { success ->
            if (success == null) return@observe
            if (success) {
                openDashboard()
            } else {
                Toast.makeText(this, "Неверный пароль", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.operationSuccess.observe(this) { msg ->
            if (msg != null && isSetupMode) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                openDashboard()
            }
        }
    }

    private fun openDashboard() {
        startActivity(Intent(this, TutorDashboardActivity::class.java))
        finish()
    }
}
