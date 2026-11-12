package com.example.data.warning

import com.example.data.model.UserProfile
import com.example.data.model.FoodLogEntry
import com.example.data.model.UsdaFoodItem

data class HealthWarning(
    val severity: WarningSeverity,
    val titleEn: String,
    val titleRu: String,
    val descriptionEn: String,
    val descriptionRu: String
)

enum class WarningSeverity {
    DANGER,  // Red coloring indicating hazardous ingredient (allergens, celiac)
    WARNING  // Yellow coloring suggesting caution (moderation of sodium, sugars, saturated fats)
}

object DiseaseWarningEngine {

    /**
     * Checks if a food item is safe and appropriate based on the user profile,
     * returning a list of active wellness/critical warnings.
     */
    fun analyzeFood(item: UsdaFoodItem, profile: UserProfile): List<HealthWarning> {
        val warnings = mutableListOf<HealthWarning>()

        // 1. ALLERGY CHECKS (Critical Danger)
        val userAllergies = profile.allergies.lowercase()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        
        val foodAllergens = item.allergensPresent.lowercase()
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        for (allergy in userAllergies) {
            // Find matches in allergens present or item name
            val isMatch = foodAllergens.any { it.contains(allergy) || allergy.contains(it) } ||
                    item.name.lowercase().contains(allergy) ||
                    (item.brandOwner?.lowercase()?.contains(allergy) ?: false)
            
            if (isMatch) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.DANGER,
                        titleEn = "Allergy Alert: Contains $allergy!",
                        titleRu = "Аллергия: Содержит $allergy!",
                        descriptionEn = "Your profile indicates severe sensitivity to $allergy. Avoid consuming this item.",
                        descriptionRu = "В вашем профиле указана аллергия на $allergy. Избегайте употребления данного продукта."
                    )
                )
            }
        }

        // 2. GLUTEN / CELIAC CHECKS (Critical Danger)
        val isGlutenIntolerant = profile.dietaryPreferences.lowercase().contains("gluten-free") ||
                profile.dietaryPreferences.lowercase().contains("celiac") ||
                profile.diseases.lowercase().contains("celiac")

        val hasGlutenInput = item.includesGluten || 
                item.name.lowercase().contains("wheat") || 
                item.name.lowercase().contains("barley") || 
                item.name.lowercase().contains("rye") ||
                item.name.lowercase().contains("gluten")

        if (isGlutenIntolerant && hasGlutenInput) {
            warnings.add(
                HealthWarning(
                    severity = WarningSeverity.DANGER,
                    titleEn = "Gluten Danger!",
                    titleRu = "Опасно: Содержит глютен!",
                    descriptionEn = "This food contains wheat, barley, rye, or explicit gluten, which triggers your Celiac/Gluten-Free restriction.",
                    descriptionRu = "Этот продукт содержит пшеницу, ячмень, рожь или глютен, что опасно при целиакии или чувствительности к глютену."
                )
            )
        }

        // 3. DIABETES CHECKS (Caution / Warning)
        val hasDiabetes = profile.diseases.lowercase().contains("diabetes") ||
                profile.diseases.lowercase().contains("диабет")

        if (hasDiabetes) {
            // High Sugar warning (e.g., Sugar is > 10g per 100g)
            if (item.sugarG > 10f) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.WARNING,
                        titleEn = "High Sugar Warning for Diabetes",
                        titleRu = "Ограничение сахара при диабете",
                        descriptionEn = "This selection contains ${item.sugarG}g sugar per serving. Simple sugars digest quickly, causing critical glycemic spikes.",
                        descriptionRu = "Этот продукт содержит ${item.sugarG}г сахара на порцию, что может вызвать резкое повышение глюкозы в крови."
                    )
                )
            }
            // Rapid Carbs caution
            if (item.carbsG > 30f) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.WARNING,
                        titleEn = "Carbohydrate Management Required",
                        titleRu = "Высокое содержание углеводов",
                        descriptionEn = "At ${item.carbsG}g of carbs, consume in moderation as part of your carbohydrate counting goals.",
                        descriptionRu = "Содержит ${item.carbsG}г углеводов. Требуется строгий учет хлебных единиц или контроль порций."
                    )
                )
            }
        }

        // 4. HYPERTENSION CHECKS (Caution / Warning)
        val hasHypertension = profile.diseases.lowercase().contains("hypertension") ||
                profile.diseases.lowercase().contains("гипертония")

        if (hasHypertension) {
            // Sodium Limit: > 140 mg is considered high sodium per FDA guidelines
            if (item.sodiumMg > 140f) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.WARNING,
                        titleEn = "High Sodium Warning",
                        titleRu = "Избыток натрия при гипертонии",
                        descriptionEn = "Contains ${item.sodiumMg}mg sodium. High sodium elevates blood pressure by expanding blood volume.",
                        descriptionRu = "Содержит ${item.sodiumMg}мг натрия. Высокое потребление соли повышает артериальное давление."
                    )
                )
            }
        }

        // 5. OBESITY CHECKS (Caution / Warning)
        val hasObesity = profile.diseases.lowercase().contains("obesity") ||
                profile.diseases.lowercase().contains("ожирение") ||
                profile.bmi >= 30.0f

        if (hasObesity) {
            // Calorie density warning
            // Standard calorie density estimate: calories per serving size
            if (item.calories > 250f) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.WARNING,
                        titleEn = "Calorie Dense Food",
                        titleRu = "Высокая калорийность",
                        descriptionEn = "Contains ${item.calories} kcal per serving. Prioritize foods with higher water and fiber contents to feel full longer.",
                        descriptionRu = "Содержит ${item.calories} ккал на порцию. Отдавайте предпочтение продуктам с высоким содержанием клетчатки."
                    )
                )
            }
            // Saturated fats check (Fat > 15g or implied high fat)
            if (item.fatG > 12f) {
                warnings.add(
                    HealthWarning(
                        severity = WarningSeverity.WARNING,
                        titleEn = "High Saturated Fat Density",
                        titleRu = "Высокое содержание жиров",
                        descriptionEn = "Contains ${item.fatG}g fats. Limit heavy saturated fat items to support healthy insulin sensitivity.",
                        descriptionRu = "Содержит ${item.fatG}г жиров. Ограничивайте тяжелые жиры для лучшей чувствительности к инсулину."
                    )
                )
            }
        }

        return warnings
    }

    /**
     * Checks if aggregated daily consumption exceeds calculated targets.
     */
    fun checkDailyExceeded(
        currentCalories: Float,
        currentCarbs: Float,
        currentProtein: Float,
        currentFat: Float,
        profile: UserProfile
    ): List<HealthWarning> {
        val warnings = mutableListOf<HealthWarning>()

        if (currentCalories > profile.dailyCalorieTarget) {
            warnings.add(
                HealthWarning(
                    severity = WarningSeverity.WARNING,
                    titleEn = "Daily Calorie Budget Exceeded",
                    titleRu = "Превышен лимит калорий",
                    descriptionEn = "Consumed ${currentCalories.toInt()} kcal out of ${profile.dailyCalorieTarget.toInt()}.",
                    descriptionRu = "Вы потребили ${currentCalories.toInt()} ккал при лимите в ${profile.dailyCalorieTarget.toInt()}."
                )
            )
        }

        if (profile.diseases.lowercase().contains("diabetes") && currentCarbs > profile.dailyCarbTargetGrams) {
            warnings.add(
                HealthWarning(
                    severity = WarningSeverity.WARNING,
                    titleEn = "Daily Carbohydrate Budget Overloaded",
                    titleRu = "Лимит углеводов превышен",
                    descriptionEn = "Carbs intake reaches ${currentCarbs.toInt()}g, exceeding recommendation (${profile.dailyCarbTargetGrams.toInt()}g) for Glycemic Balance.",
                    descriptionRu = "Вы потребили ${currentCarbs.toInt()}г углеводов при норме в ${profile.dailyCarbTargetGrams.toInt()}г."
                )
            )
        }

        return warnings
    }
}
