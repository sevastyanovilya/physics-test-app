package com.physics.tutor

import android.app.Application
import com.physics.tutor.data.repository.AppRepository

/**
 * Класс Application — точка входа в приложение.
 *
 * Инициализирует репозиторий и делает его доступным из любого места.
 * Это позволяет ViewModel получать репозиторий без передачи Context вручную.
 */
class PhysicsApp : Application() {

    /**
     * Единственный экземпляр репозитория на всё приложение (синглтон).
     * Инициализируется лениво (lazy) — только при первом обращении.
     */
    val repository: AppRepository by lazy {
        AppRepository(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        /** Глобальная ссылка для доступа к репозиторию из ViewModel-фабрик. */
        lateinit var instance: PhysicsApp
            private set
    }
}
