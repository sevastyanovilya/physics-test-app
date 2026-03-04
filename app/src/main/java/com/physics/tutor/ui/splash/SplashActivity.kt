package com.physics.tutor.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.physics.tutor.PhysicsApp
import com.physics.tutor.databinding.ActivitySplashBinding
import com.physics.tutor.ui.auth.AuthActivity
import com.physics.tutor.ui.tutor.TutorLoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Экран-заставка (Splash Screen).
 *
 * При первом запуске приложения проверяет, установлен ли пароль репетитора.
 * Если пароля нет — открывает экран создания пароля (TutorLoginActivity с флагом setup=true).
 * Если пароль уже есть — переходит к авторизации ученика (AuthActivity).
 *
 * Задержка 1,5 секунды — чтобы успел инициализироваться логотип.
 */
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        lifecycleScope.launch {
            delay(1500L) // Небольшая пауза для инициализации
            val repo = PhysicsApp.instance.repository
            val isPasswordSet = repo.isTutorPasswordSet()

            val intent = if (!isPasswordSet) {
                // Первый запуск — нужно установить пароль репетитора
                Intent(this@SplashActivity, TutorLoginActivity::class.java).apply {
                    putExtra(TutorLoginActivity.EXTRA_SETUP_MODE, true)
                }
            } else {
                // Обычный запуск — экран ученика
                Intent(this@SplashActivity, AuthActivity::class.java)
            }

            startActivity(intent)
            finish()
        }
    }
}
