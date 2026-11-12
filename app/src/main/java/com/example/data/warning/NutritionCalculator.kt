package com.example.data.warning

import com.example.data.model.UserProfile
import kotlin.math.max

object NutritionCalculator {

    data class CalculatedMetrics(
        val bmi: Float,
        val bmr: Float,
        val tdee: Float,
        val dailyCalorieTarget: Float,
        val dailyProteinTargetGrams: Float,
        val dailyCarbTargetGrams: Float,
        val dailyFatTargetGrams: Float
    )

    /**
     * Compute all critical targets instantly using only local parameters.
     */
    fun calculate(
        age: Int,
        gender: String, // "Male" or "Female"
        heightCm: Float,
        weightKg: Float,
        activityLevel: String, // "Sedentary", "Light", "Moderate", "Active", "VeryActive"
        mainGoal: String, // "Lose", "Maintain", "Gain"
        diseases: String // e.g. "Diabetes", "Hypertension", "Obesity"
    ): CalculatedMetrics {
        // 1. BMI
        val heightMeters = heightCm / 100f
        val bmi = if (heightMeters > 0) weightKg / (heightMeters * heightMeters) else 0f

        // 2. BMR (Mifflin-St Jeor Equation)
        val bmr = if (gender.trim().lowercase().startsWith("m")) {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) + 5f
        } else {
            (10f * weightKg) + (6.25f * heightCm) - (5f * age) - 161f
        }

        // 3. TDEE based on activity levels
        val activityFactor = when (activityLevel.trim().lowercase()) {
            "sedentary" -> 1.200f
            "light" -> 1.375f
            "moderate" -> 1.550f
            "active" -> 1.725f
            "veryactive" -> 1.900f
            else -> 1.200f
        }
        val tdee = bmr * activityFactor

        // 4. Calorie Target by Weight Goal
        var calorieTarget = when (mainGoal.trim().lowercase()) {
            "lose" -> tdee - 500f
            "gain" -> tdee + 400f
            else -> tdee // maintain
        }

        // Safety caps
        val minAllowableCalories = if (gender.trim().lowercase().startsWith("m")) 1500f else 1200f
        calorieTarget = max(calorieTarget, minAllowableCalories)

        // 5. Macronutrient Distribution
        // Special consideration: if user has Diabetes, restrict carbohydrate pool (e.g. 40% of calories) 
        // If user is highly active, increase protein.
        val hasDiabetes = diseases.lowercase().contains("diabetes")
        val hasObesity = diseases.lowercase().contains("obesity") || bmi >= 30f

        val proteinPercentage: Float
        val carbPercentage: Float
        val fatPercentage: Float

        when {
            hasDiabetes -> {
                // Control carb spikes, moderate higher protein / healthy fats
                proteinPercentage = 0.25f  // 25% protein
                fatPercentage = 0.35f      // 35% fats (unsaturated focus)
                carbPercentage = 0.40f     // 40% carbs
            }
            hasObesity -> {
                // Lower overall calorie densities
                proteinPercentage = 0.30f  // High protein to feel full longer
                fatPercentage = 0.25f
                carbPercentage = 0.45f
            }
            mainGoal.trim().lowercase() == "gain" -> {
                proteinPercentage = 0.22f
                fatPercentage = 0.23f
                carbPercentage = 0.55f
            }
            else -> {
                // Standard optimal health splits
                proteinPercentage = 0.20f
                fatPercentage = 0.25f
                carbPercentage = 0.55f
            }
        }

        val calorieProtein = calorieTarget * proteinPercentage
        val calorieCarbs = calorieTarget * carbPercentage
        val calorieFat = calorieTarget * fatPercentage

        // Convert calories to grams: Protein = 4 kcal/g, Carbs = 4 kcal/g, Fat = 9 kcal/g
        val pGrams = calorieProtein / 4f
        val cGrams = calorieCarbs / 4f
        val fGrams = calorieFat / 9f

        return CalculatedMetrics(
            bmi = bmi,
            bmr = bmr,
            tdee = tdee,
            dailyCalorieTarget = calorieTarget,
            dailyProteinTargetGrams = pGrams,
            dailyCarbTargetGrams = cGrams,
            dailyFatTargetGrams = fGrams
        )
    }
}
