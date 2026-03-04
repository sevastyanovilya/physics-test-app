package com.physics.tutor.ui.grade

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.physics.tutor.databinding.ActivityGradeSelectBinding
import com.physics.tutor.ui.test.TestActivity

/**
 * Экран выбора класса (7–11).
 *
 * Ученик нажимает на кнопку своего класса, после чего открывается
 * экран тестирования с вопросами для выбранного класса.
 *
 * studentId передаётся через весь поток: AuthActivity → GradeSelectActivity → TestActivity.
 */
class GradeSelectActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_STUDENT_ID = "extra_student_id"
    }

    private lateinit var binding: ActivityGradeSelectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGradeSelectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val studentId = intent.getStringExtra(EXTRA_STUDENT_ID) ?: "unknown"

        // Каждая кнопка запускает тест для соответствующего класса
        binding.btnGrade7.setOnClickListener  { startTest(7, studentId) }
        binding.btnGrade8.setOnClickListener  { startTest(8, studentId) }
        binding.btnGrade9.setOnClickListener  { startTest(9, studentId) }
        binding.btnGrade10.setOnClickListener { startTest(10, studentId) }
        binding.btnGrade11.setOnClickListener { startTest(11, studentId) }
    }

    private fun startTest(grade: Int, studentId: String) {
        val intent = Intent(this, TestActivity::class.java).apply {
            putExtra(TestActivity.EXTRA_GRADE, grade)
            putExtra(TestActivity.EXTRA_STUDENT_ID, studentId)
        }
        startActivity(intent)
    }
}
