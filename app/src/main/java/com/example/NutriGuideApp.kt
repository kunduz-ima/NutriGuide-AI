package com.example

import android.app.Application
import androidx.room.Room
import com.example.data.local.AppDatabase
import com.example.data.local.DataStoreManager
import com.example.data.repository.AuthRepository
import com.example.data.repository.FoodRepository
import com.example.data.repository.LocalAuthRepository
import com.example.data.repository.LocalFoodRepository

class NutriGuideApp : Application() {

    lateinit var database: AppDatabase
    lateinit var dataStoreManager: DataStoreManager
    lateinit var authRepository: AuthRepository
    lateinit var foodRepository: FoodRepository

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Initialize local Room SQLite Database instancing
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "nutriguide_database"
        )
        .fallbackToDestructiveMigration()
        .build()

        // Initialize settings preferences datastore
        dataStoreManager = DataStoreManager(applicationContext)

        // Inject dependencies into repositories
        authRepository = LocalAuthRepository(
            database.userAccountDao(),
            database.userProfileDao(),
            database.aiRecommendationDao(),
            dataStoreManager
        )

        foodRepository = LocalFoodRepository(
            database.foodLogDao()
        )
    }

    companion object {
        lateinit var instance: NutriGuideApp
            private set
    }
}
