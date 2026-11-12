package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Model representing registered accounts for local authentication.
 */
@Entity(tableName = "user_accounts")
data class UserAccount(
    @PrimaryKey val email: String,
    val passwordHash: String,
    val securityQuestionAnswer: String
)

/**
 * User Profile containing health metrics and targets computed locally.
 */
@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String,
    val age: Int,
    val gender: String, // Male, Female
    val heightCm: Float,
    val weightKg: Float,
    val activityLevel: String, // Sedentary, Light, Moderate, Active, VeryActive
    val mainGoal: String, // Lose, Maintain, Gain
    
    // Serialized Lists (comma-separated for SQLite simplicity)
    val diseases: String, // e.g. "Diabetes,Hypertension"
    val dietaryPreferences: String, // e.g. "Vegan,Vegetarian,Gluten-free"
    val allergies: String, // e.g. "Peanuts,Dairy,Shellfish,Soy"
    
    // Locally calculated indicators
    val bmi: Float,
    val bmr: Float,
    val tdee: Float,
    val dailyCalorieTarget: Float,
    val dailyProteinTargetGrams: Float,
    val dailyCarbTargetGrams: Float,
    val dailyFatTargetGrams: Float,
    
    val isOnboarded: Boolean = false
)

/**
 * Logged Food Items representing breakfasts, lunches, dinners, and snacks.
 */
@Entity(tableName = "food_log_entries")
data class FoodLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val userEmail: String,
    val dateString: String, // yyyy-MM-dd
    val mealType: String, // Breakfast, Lunch, Dinner, Snack
    val name: String,
    val brandOwner: String? = null,
    val fdcId: String, // USDA ID or "local_custom"
    val calories: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val sodiumMg: Float = 0f,
    val sugarG: Float = 0f,
    val includesGluten: Boolean = false,
    val allergensPresent: String = "", // comma-separated allergens
    val quantityGrams: Float = 100f
)

/**
 * USDA search result representation.
 */
data class UsdaFoodItem(
    val fdcId: String,
    val name: String,
    val brandOwner: String?,
    val calories: Float,
    val proteinG: Float,
    val carbsG: Float,
    val fatG: Float,
    val sodiumMg: Float = 0f,
    val sugarG: Float = 0f,
    val includesGluten: Boolean = false,
    val allergensPresent: String = "",
    val category: String = ""
)

/**
 * AI-generated recommendations cached locally
 */
@Entity(tableName = "ai_recommendations")
data class AIRecommendation(
    @PrimaryKey val email: String,
    val advice: String,
    val foodsToAvoid: String, // comma-separated or list text
    val foodsToPrioritize: String, // comma-separated or list text
    val breakfastSuggestions: String,
    val lunchSuggestions: String,
    val dinnerSuggestions: String,
    val snackSuggestions: String,
    val weeklyPlanJson: String = "", // JSON string containing 7-day plan with recipes
    val timestamp: Long = System.currentTimeMillis()
)

