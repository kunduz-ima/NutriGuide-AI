package com.example.data.repository

import com.example.data.local.DataStoreManager
import com.example.data.local.UserAccountDao
import com.example.data.local.UserProfileDao
import com.example.data.model.FoodLogEntry
import com.example.data.model.UserAccount
import com.example.data.model.UserProfile
import com.example.data.model.UsdaFoodItem
import com.example.data.remote.UsdaFoodSearchProvider
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun getLoggedInEmail(): Flow<String?>
    suspend fun login(email: String, password: String): Result<UserProfile>
    suspend fun register(email: String, password: String, questionAnswer: String): Result<UserProfile>
    suspend fun forgotPassword(email: String, questionAnswer: String): Result<String>
    fun getProfileFlow(email: String): Flow<UserProfile?>
    suspend fun getProfileSync(email: String): UserProfile?
    suspend fun saveProfile(profile: UserProfile): Result<Unit>
    suspend fun logout()
    
    // Application settings
    fun getLanguageFlow(): Flow<String>
    suspend fun saveLanguage(lang: String)

    // API Keys and custom logic
    fun getGroqKeyFlow(): Flow<String>
    suspend fun saveGroqKey(groq: String)
    fun getRecommendationFlow(email: String): Flow<com.example.data.model.AIRecommendation?>
    suspend fun saveRecommendation(rec: com.example.data.model.AIRecommendation)
}

class LocalAuthRepository(
    private val accountDao: UserAccountDao,
    private val profileDao: UserProfileDao,
    private val recDao: com.example.data.local.AIRecommendationDao,
    private val prefs: DataStoreManager
) : AuthRepository {

    override fun getLoggedInEmail(): Flow<String?> = prefs.loggedInEmailFlow

    override fun getGroqKeyFlow(): Flow<String> = prefs.groqApiKeyFlow

    override suspend fun saveGroqKey(groq: String) {
        prefs.setGroqApiKey(groq)
    }

    override fun getRecommendationFlow(email: String): Flow<com.example.data.model.AIRecommendation?> = recDao.getRecommendationFlow(email)

    override suspend fun saveRecommendation(rec: com.example.data.model.AIRecommendation) {
        recDao.insertRecommendation(rec)
    }

    override suspend fun login(email: String, password: String): Result<UserProfile> {
        val normalized = email.trim().lowercase()
        val account = accountDao.getAccountByEmail(normalized)
            ?: return Result.failure(Exception("Account with this email does not exist / Аккаунт с этим email не существует."))
        
        if (account.passwordHash != password) { // Note: Simple verification for functional offline prototype
            return Result.failure(Exception("Invalid password / Неверный пароль."))
        }

        prefs.setLoggedInUser(normalized)
        val profile = profileDao.getProfileByEmail(normalized)
            ?: UserProfile(
                email = normalized,
                name = normalized.substringBefore("@"),
                age = 25,
                gender = "Female",
                heightCm = 165f,
                weightKg = 60f,
                activityLevel = "Moderate",
                mainGoal = "Maintain",
                diseases = "",
                dietaryPreferences = "",
                allergies = "",
                bmi = 22f,
                bmr = 1400f,
                tdee = 2000f,
                dailyCalorieTarget = 2000f,
                dailyProteinTargetGrams = 100f,
                dailyCarbTargetGrams = 250f,
                dailyFatTargetGrams = 65f
            )
        
        return Result.success(profile)
    }

    override suspend fun register(email: String, password: String, questionAnswer: String): Result<UserProfile> {
        val normalized = email.trim().lowercase()
        if (accountDao.getAccountByEmail(normalized) != null) {
            return Result.failure(Exception("Email already registered / Email уже зарегистрирован."))
        }

        val account = UserAccount(email = normalized, passwordHash = password, securityQuestionAnswer = questionAnswer)
        accountDao.insertAccount(account)
        
        val profile = UserProfile(
            email = normalized,
            name = normalized.substringBefore("@"),
            age = 25,
            gender = "Female",
            heightCm = 165f,
            weightKg = 60f,
            activityLevel = "Moderate",
            mainGoal = "Maintain",
            diseases = "",
            dietaryPreferences = "",
            allergies = "",
            bmi = 22f,
            bmr = 1400f,
            tdee = 2000f,
            dailyCalorieTarget = 2000f,
            dailyProteinTargetGrams = 100f,
            dailyCarbTargetGrams = 250f,
            dailyFatTargetGrams = 65f
        )
        profileDao.insertProfile(profile)
        
        prefs.setLoggedInUser(normalized)
        return Result.success(profile)
    }

    override suspend fun forgotPassword(email: String, questionAnswer: String): Result<String> {
        val normalized = email.trim().lowercase()
        val account = accountDao.getAccountByEmail(normalized)
            ?: return Result.failure(Exception("Account not found / Аккаунт не найден."))
        
        if (account.securityQuestionAnswer != questionAnswer) {
            return Result.failure(Exception("Incorrect answer / Неверный ответ."))
        }
        
        return Result.success(account.passwordHash)
    }

    override fun getProfileFlow(email: String): Flow<UserProfile?> = profileDao.getProfileByEmailFlow(email)

    override suspend fun getProfileSync(email: String): UserProfile? = profileDao.getProfileByEmail(email)

    override suspend fun saveProfile(profile: UserProfile): Result<Unit> {
        profileDao.insertProfile(profile)
        return Result.success(Unit)
    }

    override suspend fun logout() {
        prefs.setLoggedInUser(null)
    }

    override fun getLanguageFlow(): Flow<String> = prefs.appLanguageFlow

    override suspend fun saveLanguage(lang: String) {
        prefs.setAppLanguage(lang)
    }
}

interface FoodRepository {
    suspend fun searchFood(query: String): List<UsdaFoodItem>
    fun getFoodLogs(email: String, date: String): Flow<List<FoodLogEntry>>
    fun getAllFoodLogs(email: String): Flow<List<FoodLogEntry>>
    suspend fun addFoodLog(entry: FoodLogEntry): Result<Unit>
    suspend fun deleteFoodLog(id: Long)
}

class LocalFoodRepository(
    private val foodDao: com.example.data.local.FoodLogDao
) : FoodRepository {

    override suspend fun searchFood(query: String): List<UsdaFoodItem> {
        return UsdaFoodSearchProvider.search(query)
    }

    override fun getFoodLogs(email: String, date: String): Flow<List<FoodLogEntry>> = foodDao.getFoodLogsByDate(email, date)

    override fun getAllFoodLogs(email: String): Flow<List<FoodLogEntry>> = foodDao.getAllFoodLogs(email)

    override suspend fun addFoodLog(entry: FoodLogEntry): Result<Unit> {
        foodDao.insertFoodLog(entry)
        return Result.success(Unit)
    }

    override suspend fun deleteFoodLog(id: Long) {
        foodDao.deleteFoodLogById(id)
    }
}
