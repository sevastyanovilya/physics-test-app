package com.physics.tutor.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.physics.tutor.databinding.ActivityResultBinding
import com.physics.tutor.ui.auth.AuthActivity
import com.physics.tutor.util.QrCodeGenerator

/**
 * Экран результатов для ученика.
 *
 * Отображает:
 *   - Итоговый балл (правильных / всего)
 *   - Процент выполнения
 *   - Уровень знаний: Начальный / Базовый / Продвинутый
 *   - QR-код с 6-значным кодом результата
 *   - Числовой 6-значный код (для случая, если QR не сканируется)
 *
 * Ученик показывает QR-код или называет числовой код репетитору.
 * Репетитор вводит код в свою панель и видит полные детали теста.
 */
class ResultActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_RESULT_ID    = "extra_result_id"
        const val EXTRA_RESULT_CODE  = "extra_result_code"
        const val EXTRA_CORRECT      = "extra_correct"
        const val EXTRA_TOTAL        = "extra_total"
        const val EXTRA_LEVEL        = "extra_level"
        const val EXTRA_GRADE        = "extra_grade"
    }

    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val resultCode = intent.getStringExtra(EXTRA_RESULT_CODE) ?: "000000"
        val correct    = intent.getIntExtra(EXTRA_CORRECT, 0)
        val total      = intent.getIntExtra(EXTRA_TOTAL, 1)
        val level      = intent.getStringExtra(EXTRA_LEVEL) ?: "Начальный"
        val grade      = intent.getIntExtra(EXTRA_GRADE, 9)

        displayResults(resultCode, correct, total, level, grade)
        generateQrCode(resultCode)

        binding.btnNewTest.setOnClickListener {
            // Возвращаемся к выбору класса
            val intent = Intent(this, AuthActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun displayResults(code: String, correct: Int, total: Int, level: String, grade: Int) {
        binding.tvGradeResult.text = "$grade класс"
        binding.tvScore.text = "$correct / $total"
        val percent = if (total > 0) correct * 100 / total else 0
        binding.tvPercent.text = "$percent%"
        binding.tvLevel.text = level
        binding.tvResultCode.text = code

        // Цвет уровня
        val levelColorRes = when (level) {
            "Продвинутый" -> com.physics.tutor.R.color.level_advanced
            "Базовый"     -> com.physics.tutor.R.color.level_basic
            else          -> com.physics.tutor.R.color.level_beginner
        }
        binding.tvLevel.setTextColor(getColor(levelColorRes))
        binding.tvLevelBadge.text = level
        binding.tvLevelBadge.setBackgroundColor(getColor(levelColorRes))
    }

    private fun generateQrCode(code: String) {
        binding.progressQr.visibility = View.VISIBLE
        binding.ivQrCode.visibility = View.GONE

        // Генерируем QR-код в фоновом потоке
        Thread {
            val bitmap = QrCodeGenerator.generate(code, sizePx = 512)
            runOnUiThread {
                binding.progressQr.visibility = View.GONE
                if (bitmap != null) {
                    binding.ivQrCode.setImageBitmap(bitmap)
                    binding.ivQrCode.visibility = View.VISIBLE
                }
            }
        }.start()
    }
}
