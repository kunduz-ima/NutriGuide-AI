package com.example.ui.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.NutriGuideApp
import com.example.data.model.FoodLogEntry
import com.example.data.model.UserProfile
import com.example.data.model.UsdaFoodItem
import com.example.data.repository.AuthRepository
import com.example.data.repository.FoodRepository
import com.example.data.warning.DiseaseWarningEngine
import com.example.data.warning.HealthWarning
import com.example.data.warning.NutritionCalculator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    object Idle : AuthUiState
    object Loading : AuthUiState
    data class Success(val profile: UserProfile) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

sealed interface ResetUiState {
    object Idle : ResetUiState
    object Loading : ResetUiState
    data class Success(val passwordHint: String) : ResetUiState
    data class Error(val message: String) : ResetUiState
}

sealed interface ResultState {
    object Idle : ResultState
    object Loading : ResultState
    object Success : ResultState
    data class Error(val message: String) : ResultState
}

/**
 * Handle authentication status, language choices, and local logouts.
 */
class AuthViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    val loggedInEmail: StateFlow<String?> = authRepository.getLoggedInEmail()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val appLanguage: StateFlow<String> = authRepository.getLanguageFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "en")

    private val _state = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _resetState = MutableStateFlow<ResetUiState>(ResetUiState.Idle)
    val resetState: StateFlow<ResetUiState> = _resetState.asStateFlow()

    fun login(email: String, psw: String) {
        if (email.isBlank() || psw.isBlank()) {
            _state.value = AuthUiState.Error("Please fill all fields / Пожалуйста, заполните все поля.")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.login(email, psw)
                .onSuccess { _state.value = AuthUiState.Success(it) }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun register(email: String, psw: String, question: String) {
        if (email.isBlank() || psw.isBlank() || question.isBlank()) {
            _state.value = AuthUiState.Error("Please fill all fields / Пожалуйста, заполните все поля.")
            return
        }
        _state.value = AuthUiState.Loading
        viewModelScope.launch {
            authRepository.register(email, psw, question)
                .onSuccess { _state.value = AuthUiState.Success(it) }
                .onFailure { _state.value = AuthUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun forgotPassword(email: String, answer: String) {
        if (email.isBlank() || answer.isBlank()) {
            _resetState.value = ResetUiState.Error("Please enter email and answer / Укажите email и ответ.")
            return
        }
        _resetState.value = ResetUiState.Loading
        viewModelScope.launch {
            authRepository.forgotPassword(email, answer)
                .onSuccess { _resetState.value = ResetUiState.Success(it) }
                .onFailure { _resetState.value = ResetUiState.Error(it.message ?: "Check your inputs") }
        }
    }

    fun changeLanguage(lang: String) {
        viewModelScope.launch {
            authRepository.saveLanguage(lang)
        }
    }

    fun clearState() {
        _state.value = AuthUiState.Idle
        _resetState.value = ResetUiState.Idle
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            clearState()
        }
    }
}

/**
 * Handle Multi-step Onboarding screen state and calculate dynamic clinical values locally.
 */
class OnboardingViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    var currentStep = MutableStateFlow(1)
    val name = MutableStateFlow("")
    val age = MutableStateFlow("28")
    val gender = MutableStateFlow("Female") // Male, Female
    val heightCm = MutableStateFlow("165")
    val weightKg = MutableStateFlow("60")
    val activityLevel = MutableStateFlow("Moderate") // Sedentary, Light, Moderate, Active, VeryActive
    val mainGoal = MutableStateFlow("Lose") // Lose, Maintain, Gain

    val checkedDiseases = MutableStateFlow(setOf<String>())
    val checkedPreferences = MutableStateFlow(setOf<String>())
    val checkedAllergies = MutableStateFlow(setOf<String>())

    private val _computedTargets = MutableStateFlow<NutritionCalculator.CalculatedMetrics?>(null)
    val computedTargets: StateFlow<NutritionCalculator.CalculatedMetrics?> = _computedTargets.asStateFlow()

    private val _saveState = MutableStateFlow<ResultState>(ResultState.Idle)
    val saveState: StateFlow<ResultState> = _saveState.asStateFlow()

    fun nextStep() {
        if (currentStep.value == 4) {
            calculateTargets()
        }
        currentStep.value += 1
    }

    fun prevStep() {
        if (currentStep.value > 1) {
            currentStep.value -= 1
        }
    }

    fun toggleDisease(d: String) {
        val current = checkedDiseases.value
        checkedDiseases.value = if (current.contains(d)) current - d else current + d
    }

    fun togglePreference(p: String) {
        val current = checkedPreferences.value
        checkedPreferences.value = if (current.contains(p)) current - p else current + p
    }

    fun toggleAllergy(a: String) {
        val current = checkedAllergies.value
        checkedAllergies.value = if (current.contains(a)) current - a else current + a
    }

    private fun calculateTargets() {
        val ageInt = age.value.toIntOrNull() ?: 30
        val heightF = heightCm.value.toFloatOrNull() ?: 170f
        val weightF = weightKg.value.toFloatOrNull() ?: 70f
        
        val targetMetrics = NutritionCalculator.calculate(
            age = ageInt,
            gender = gender.value,
            heightCm = heightF,
            weightKg = weightF,
            activityLevel = activityLevel.value,
            mainGoal = mainGoal.value,
            diseases = checkedDiseases.value.joinToString(",")
        )
        _computedTargets.value = targetMetrics
    }

    fun saveProfile(email: String) {
        _saveState.value = ResultState.Loading
        viewModelScope.launch {
            val targets = _computedTargets.value ?: NutritionCalculator.calculate(
                age = age.value.toIntOrNull() ?: 30,
                gender = gender.value,
                heightCm = heightCm.value.toFloatOrNull() ?: 170f,
                weightKg = weightKg.value.toFloatOrNull() ?: 70f,
                activityLevel = activityLevel.value,
                mainGoal = mainGoal.value,
                diseases = checkedDiseases.value.joinToString(",")
            )

            val profile = UserProfile(
                email = email,
                name = name.value.ifBlank { email.substringBefore("@") },
                age = age.value.toIntOrNull() ?: 30,
                gender = gender.value,
                heightCm = heightCm.value.toFloatOrNull() ?: 170f,
                weightKg = weightKg.value.toFloatOrNull() ?: 70f,
                activityLevel = activityLevel.value,
                mainGoal = mainGoal.value,
                diseases = checkedDiseases.value.joinToString(","),
                dietaryPreferences = checkedPreferences.value.joinToString(","),
                allergies = checkedAllergies.value.joinToString(","),
                bmi = targets.bmi,
                bmr = targets.bmr,
                tdee = targets.tdee,
                dailyCalorieTarget = targets.dailyCalorieTarget,
                dailyProteinTargetGrams = targets.dailyProteinTargetGrams,
                dailyCarbTargetGrams = targets.dailyCarbTargetGrams,
                dailyFatTargetGrams = targets.dailyFatTargetGrams,
                isOnboarded = true
            )

            authRepository.saveProfile(profile)
                .onSuccess { _saveState.value = ResultState.Success }
                .onFailure { _saveState.value = ResultState.Error(it.message ?: "Failed to saveOnboardingProfile") }
        }
    }
}


/**
 * Shared state between food logger and warnings engine interfaces.
 */
class MainAppViewModel(
    private val authRepository: AuthRepository,
    private val foodRepository: FoodRepository
) : ViewModel() {

    private val _currentDate = MutableStateFlow("2026-06-02") // matches the ISO metadata nicely
    val currentDate: StateFlow<String> = _currentDate.asStateFlow()

    // Load active profile cleanly
    @OptIn(ExperimentalCoroutinesApi::class)
    val userProfile: StateFlow<UserProfile?> = authRepository.getLoggedInEmail()
        .flatMapLatest { email ->
            if (email != null) authRepository.getProfileFlow(email) else flowOf(null)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Load logged items for current date
    @OptIn(ExperimentalCoroutinesApi::class)
    val loggedFoods: StateFlow<List<FoodLogEntry>> = combine(
        authRepository.getLoggedInEmail(),
        _currentDate
    ) { email, date -> Pair(email, date) }
        .flatMapLatest { (email, date) ->
            if (email != null) {
                foodRepository.getFoodLogs(email, date)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Pull ALL logged history for the heatmap & historical analytics
    @OptIn(ExperimentalCoroutinesApi::class)
    val allLoggedFoods: StateFlow<List<FoodLogEntry>> = authRepository.getLoggedInEmail()
        .flatMapLatest { email ->
            if (email != null) {
                foodRepository.getAllFoodLogs(email)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Load cached AI Recommendation
    @OptIn(ExperimentalCoroutinesApi::class)
    val aiRecommendation: StateFlow<com.example.data.model.AIRecommendation?> = userProfile
        .flatMapLatest { profile ->
            if (profile != null) {
                authRepository.getRecommendationFlow(profile.email)
            } else {
                flowOf(null)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UsdaFoodItem>>(emptyList())
    val searchResults: StateFlow<List<UsdaFoodItem>> = _searchResults.asStateFlow()

    private val _selectedFoodItem = MutableStateFlow<UsdaFoodItem?>(null)
    val selectedFoodItem: StateFlow<UsdaFoodItem?> = _selectedFoodItem.asStateFlow()

    // API Keys state
    val groqApiKey: StateFlow<String> = authRepository.getGroqKeyFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    // Warnings for currently searched item
    val selectedItemWarnings: StateFlow<List<HealthWarning>> = combine(
        _selectedFoodItem,
        userProfile
    ) { item, profile ->
        if (item != null && profile != null) {
            DiseaseWarningEngine.analyzeFood(item, profile)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Warnings for overall daily targets
    val dailyLimitWarnings: StateFlow<List<HealthWarning>> = combine(
        loggedFoods,
        userProfile
    ) { foods, profile ->
        if (profile != null) {
            val totalCal = foods.sumOf { it.calories.toDouble() }.toFloat()
            val totalCarbs = foods.sumOf { it.carbsG.toDouble() }.toFloat()
            val totalProt = foods.sumOf { it.proteinG.toDouble() }.toFloat()
            val totalFat = foods.sumOf { it.fatG.toDouble() }.toFloat()
            
            DiseaseWarningEngine.checkDailyExceeded(totalCal, totalCarbs, totalProt, totalFat, profile)
        } else {
            emptyList()
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _exportState = MutableStateFlow<ResultState>(ResultState.Idle)
    val exportState: StateFlow<ResultState> = _exportState.asStateFlow()

    private val _recommendationGeneratingState = MutableStateFlow<ResultState>(ResultState.Idle)
    val recommendationGeneratingState: StateFlow<ResultState> = _recommendationGeneratingState.asStateFlow()

    private val _scanResult = MutableStateFlow<com.example.data.remote.FoodScanResult?>(null)
    val scanResult: StateFlow<com.example.data.remote.FoodScanResult?> = _scanResult.asStateFlow()

    private val _scanState = MutableStateFlow<ResultState>(ResultState.Idle)
    val scanState: StateFlow<ResultState> = _scanState.asStateFlow()

    private val _settingsSaveState = MutableStateFlow<ResultState>(ResultState.Idle)
    val settingsSaveState: StateFlow<ResultState> = _settingsSaveState.asStateFlow()

    init {
        // Trigger initial preset search
        searchFood("")
    }

    fun setDate(date: String) {
        _currentDate.value = date
    }

    fun searchFood(query: String) {
        _searchQuery.value = query
        viewModelScope.launch {
            val results = foodRepository.searchFood(query)
            _searchResults.value = results
        }
    }

    fun selectFoodItem(item: UsdaFoodItem?) {
        _selectedFoodItem.value = item
    }

    fun addFoodToLog(mealType: String, quantityGrams: Float = 100f) {
        val item = _selectedFoodItem.value ?: return
        val email = userProfile.value?.email ?: return
        
        // Adjust values by portion size/relative factor (reference USDA is per 100g)
        val factor = quantityGrams / 100f
        
        val entry = FoodLogEntry(
            userEmail = email,
            dateString = _currentDate.value,
            mealType = mealType,
            name = item.name,
            brandOwner = item.brandOwner,
            fdcId = item.fdcId,
            calories = item.calories * factor,
            proteinG = item.proteinG * factor,
            carbsG = item.carbsG * factor,
            fatG = item.fatG * factor,
            sodiumMg = item.sodiumMg * factor,
            sugarG = item.sugarG * factor,
            includesGluten = item.includesGluten,
            allergensPresent = item.allergensPresent,
            quantityGrams = quantityGrams
        )

        viewModelScope.launch {
            foodRepository.addFoodLog(entry)
                .onSuccess {
                    _selectedFoodItem.value = null
                }
        }
    }

    fun deleteLog(id: Long) {
        viewModelScope.launch {
            foodRepository.deleteFoodLog(id)
        }
    }

    fun clearExportState() {
        _exportState.value = ResultState.Idle
    }

    fun clearScan() {
        _scanResult.value = null
        _scanState.value = ResultState.Idle
    }

    fun clearSettingsSaveState() {
        _settingsSaveState.value = ResultState.Idle
    }

    /**
     * Trigger fresh Groq AI recommendation based on biometric profiles
     */
    fun generateRecommendation() {
        val profile = userProfile.value ?: return
        _recommendationGeneratingState.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val groqKey = groqApiKey.value
                val rec = com.example.data.remote.AIApiClient.generateRecommendations(profile, groqKey)
                authRepository.saveRecommendation(rec)
                _recommendationGeneratingState.value = ResultState.Success
            } catch (e: Exception) {
                _recommendationGeneratingState.value = ResultState.Error(e.localizedMessage ?: "AI generation failed.")
            }
        }
    }

    /**
     * Call AI Vision models to estimate metrics from captured food artwork
     */
    fun scanFoodPhoto(bitmap: Bitmap) {
        _scanState.value = ResultState.Loading
        viewModelScope.launch {
            try {
                // In simulated mode, we don't strictly need a real key check here
                val res = com.example.data.remote.AIApiClient.analyzeFoodPhoto(bitmap, "")
                _scanResult.value = res
                _scanState.value = ResultState.Success
            } catch (e: java.lang.Exception) {
                _scanState.value = ResultState.Error(e.localizedMessage ?: "Food photo analysis failed.")
            }
        }
    }

    /**
     * Persist finalized identified food item back to active food logs database
     */
    fun saveScannedFood(mealType: String, foodName: String, calories: Float, protein: Float, carbs: Float, fat: Float, weightGrams: Float) {
        val email = userProfile.value?.email ?: return
        val entry = FoodLogEntry(
            userEmail = email,
            dateString = _currentDate.value,
            mealType = mealType,
            name = foodName,
            brandOwner = "Camera Scan",
            fdcId = "scanned_${System.currentTimeMillis()}",
            calories = calories,
            proteinG = protein,
            carbsG = carbs,
            fatG = fat,
            sodiumMg = 0f,
            sugarG = 0f,
            quantityGrams = weightGrams
        )
        viewModelScope.launch {
            foodRepository.addFoodLog(entry)
            _scanResult.value = null
            _scanState.value = ResultState.Idle
        }
    }

    /**
     * Save updated settings profile metrics to Room DB & preferences
     */
    fun saveUserSettings(
        name: String,
        ageStr: String,
        gender: String,
        heightStr: String,
        weightStr: String,
        activity: String,
        goal: String,
        lang: String,
        diseases: String,
        allergies: String,
        preferences: String,
        notificationsEnabled: Boolean,
        groqKey: String
    ) {
        val currentProfile = userProfile.value ?: return
        _settingsSaveState.value = ResultState.Loading
        viewModelScope.launch {
            try {
                val ageVal = ageStr.toIntOrNull() ?: currentProfile.age
                val heightVal = heightStr.toFloatOrNull() ?: currentProfile.heightCm
                val weightVal = weightStr.toFloatOrNull() ?: currentProfile.weightKg

                // Re-calculate local Mifflin calorie targets dynamically
                val targets = NutritionCalculator.calculate(
                    age = ageVal,
                    gender = gender,
                    heightCm = heightVal,
                    weightKg = weightVal,
                    activityLevel = activity,
                    mainGoal = goal,
                    diseases = diseases
                )

                val updatedProfile = currentProfile.copy(
                    name = name,
                    age = ageVal,
                    gender = gender,
                    heightCm = heightVal,
                    weightKg = weightVal,
                    activityLevel = activity,
                    mainGoal = goal,
                    diseases = diseases,
                    allergies = allergies,
                    dietaryPreferences = preferences,
                    bmr = targets.bmr,
                    tdee = targets.tdee,
                    dailyCalorieTarget = targets.dailyCalorieTarget,
                    dailyProteinTargetGrams = targets.dailyProteinTargetGrams,
                    dailyCarbTargetGrams = targets.dailyCarbTargetGrams,
                    dailyFatTargetGrams = targets.dailyFatTargetGrams
                )

                authRepository.saveProfile(updatedProfile)
                authRepository.saveLanguage(lang)
                authRepository.saveGroqKey(groqKey)

                // Modify notifications in WorkManager
                if (notificationsEnabled) {
                    com.example.data.notification.ReminderScheduler.scheduleAllReminders(NutriGuideApp.instance)
                } else {
                    com.example.data.notification.ReminderScheduler.cancelAllReminders(NutriGuideApp.instance)
                }

                _settingsSaveState.value = ResultState.Success
            } catch (e: Exception) {
                _settingsSaveState.value = ResultState.Error(e.localizedMessage ?: "Failed saving configurations")
            }
        }
    }

    /**
     * Local Export mechanism to save logs into highly readable CSV and authentic PDF files locally on device.
     */
    fun exportReport(context: Context, isCsv: Boolean) {
        val profile = userProfile.value ?: return
        
        _exportState.value = ResultState.Loading
        
        viewModelScope.launch {
            try {
                // Use a separate context-independent dispatcher for IO
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    if (isCsv) {
                        val logs = foodRepository.getAllFoodLogs(profile.email).first()
                        com.example.data.export.Exporter.exportLogsToCsv(context, profile, logs)
                    } else {
                        val logs = foodRepository.getAllFoodLogs(profile.email).first()
                        val recommendation = authRepository.getRecommendationFlow(profile.email).first()
                        com.example.data.export.Exporter.exportReportToPdf(context, profile, recommendation, logs)
                    }
                }
                _exportState.value = ResultState.Success
            } catch (e: Exception) {
                _exportState.value = ResultState.Error("Could not write report: ${e.localizedMessage}")
            }
        }
    }
}

/**
 * Standard ViewModel Factor to manage and inject arguments safely from single application context instance.
 */
class ViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val app = NutriGuideApp.instance
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> {
                AuthViewModel(app.authRepository) as T
            }
            modelClass.isAssignableFrom(OnboardingViewModel::class.java) -> {
                OnboardingViewModel(app.authRepository) as T
            }
            modelClass.isAssignableFrom(MainAppViewModel::class.java) -> {
                MainAppViewModel(app.authRepository, app.foodRepository) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
