package com.kawaiical.app

import android.app.Application
import com.kawaiical.app.data.DiaryRepository
import com.kawaiical.app.data.db.AppDatabase
import com.kawaiical.app.data.prefs.UserPreferencesRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/** Plain manual DI — one container for the whole app. */
class AppContainer(application: Application) {
    val database: AppDatabase = AppDatabase.get(application)
    val diaryRepository: DiaryRepository = DiaryRepository(database)
    val preferencesRepository: UserPreferencesRepository = UserPreferencesRepository(application)
}

class KawaiiCalApplication : Application() {
    lateinit var container: AppContainer
        private set

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        applicationScope.launch { container.diaryRepository.seedIfEmpty() }
    }
}
