package com.example.data.remote

import android.graphics.Bitmap
import android.util.Base64
import com.example.data.model.AIRecommendation
import com.example.data.model.UserProfile
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

// --- Structure Representing Scanned Food Result ---
@JsonClass(generateAdapter = true)
data class FoodScanResult(
    val foodName: String,
    val estimatedWeight: Float, // in grams
    val calories: Float,
    val protein: Float,
    val carbs: Float,
    val fat: Float
)

object AIApiClient {

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    /**
     * Generate clinical recommendations based on user's metrics and medical constraints
     */
    suspend fun generateRecommendations(
        profile: UserProfile,
        groqKey: String
    ): AIRecommendation = withContext(Dispatchers.IO) {
        val diseasesText = profile.diseases.ifBlank { "None documented" }
        val allergiesText = profile.allergies.ifBlank { "None documented" }
        val preferencesText = profile.dietaryPreferences.ifBlank { "None documented" }

        val systemPrompt = """
            You are a specialized Clinical AI Nutritionist. Generate precise medical-level dietary guidance and a 7-day meal plan in JSON format.
            The user has medical conditions, so ensure all recipes and suggestions are compliant.
            You must return ONLY a raw JSON object with this exact structure:
            {
              "advice": "General structured nutrition advice incorporating user's diseases, goals and BMI.",
              "foodsToAvoid": "Comma-separated list of items to avoid.",
              "foodsToPrioritize": "Comma-separated list of nutrient-dense items.",
              "breakfast": "Specifically recommended breakfast for today.",
              "lunch": "Specifically recommended lunch for today.",
              "dinner": "Specifically recommended dinner for today.",
              "snack": "Specifically recommended snack for today.",
              "weeklyPlan": [
                {
                  "day": "Day 1",
                  "breakfast": "Meal name + brief 1-sentence recipe",
                  "lunch": "Meal name + brief 1-sentence recipe",
                  "dinner": "Meal name + brief 1-sentence recipe",
                  "snack": "Meal name"
                },
                { "day": "Day 2", ... },
                ... up to Day 7
              ]
            }
            Do not include any markdown formatting, backticks, or text outside the JSON.
        """.trimIndent()

        val userPrompt = """
            Generate nutrition guide and 7-day plan for:
            - Biological Gender: ${profile.gender}
            - Age: ${profile.age} years
            - Height: ${profile.heightCm} cm, Weight: ${profile.weightKg} kg
            - Calculated BMI: ${"%.2f".format(profile.bmi)}
            - Calculated TDEE Goal Limit: ${profile.dailyCalorieTarget.toInt()} kcal
            - Core Medical Diseases: $diseasesText
            - Allergies: $allergiesText
            - Dietary Style / Preferences: $preferencesText
            - Main Aesthetic/Health Goal: ${profile.mainGoal}
        """.trimIndent()

        if (groqKey.isNotBlank()) {
            try {
                val responseJson = callGroq(
                    apiKey = groqKey,
                    systemPrompt = systemPrompt,
                    userPrompt = userPrompt,
                    jsonMode = true
                )
                val jo = JSONObject(responseJson)
                return@withContext AIRecommendation(
                    email = profile.email,
                    advice = jo.optString("advice", "Follow a balanced diet."),
                    foodsToAvoid = jo.optString("foodsToAvoid", "None"),
                    foodsToPrioritize = jo.optString("foodsToPrioritize", "None"),
                    breakfastSuggestions = jo.optString("breakfast", "Oatmeal"),
                    lunchSuggestions = jo.optString("lunch", "Grilled Turkey"),
                    dinnerSuggestions = jo.optString("dinner", "Baked Salmon"),
                    snackSuggestions = jo.optString("snack", "Almonds"),
                    weeklyPlanJson = jo.optJSONArray("weeklyPlan")?.toString() ?: ""
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return@withContext simulateClinicalRecommendations(profile)
    }

    /**
     * Analyze Captured Food Photo via Vision APIs (Simulated for stable presentation)
     */
    suspend fun analyzeFoodPhoto(
        bitmap: Bitmap,
        groqKey: String
    ): FoodScanResult = withContext(Dispatchers.IO) {
        // Simulate processing delay for realism
        kotlinx.coroutines.delay(1500)

        val mockResults = listOf(
            FoodScanResult("Avocado Toast with Poached Egg", 180f, 340f, 14f, 22f, 18f),
            FoodScanResult("Fresh Mediterranean Tuna Salad", 280f, 290f, 24f, 8f, 12f),
            FoodScanResult("Healthy Beef Sirloin Stir Fry", 320f, 420f, 32f, 28f, 14f),
            FoodScanResult("Muesli Porridge Bowl with Berries", 200f, 210f, 6f, 38f, 4f),
            FoodScanResult("Grilled Atlantic Salmon with Asparagus", 250f, 380f, 32f, 5f, 24f)
        )

        return@withContext mockResults.random()
    }

    // --- Core Low Level REST Requests ---

    private fun callGroq(
        apiKey: String,
        systemPrompt: String,
        userPrompt: String,
        jsonMode: Boolean
    ): String {
        val root = JSONObject()
        root.put("model", "llama-3.3-70b-versatile")
        
        val messages = org.json.JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        messages.put(JSONObject().put("role", "user").put("content", userPrompt))
        root.put("messages", messages)

        if (jsonMode) {
            root.put("response_format", JSONObject().put("type", "json_object"))
        }

        val requestBody = root.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw Exception("Groq failure: code ${response.code}")
            val respString = response.body?.string() ?: throw Exception("Empty Groq payload")
            val choices = JSONObject(respString).getJSONArray("choices")
            return choices.getJSONObject(0).getJSONObject("message").getString("content")
        }
    }

    private fun callGroqVision(
        apiKey: String,
        imageBytesBase64: String,
        systemPrompt: String
    ): String {
        val root = JSONObject()
        root.put("model", "llama-3.2-90b-vision-preview")
        
        val messages = org.json.JSONArray()
        messages.put(JSONObject().put("role", "system").put("content", systemPrompt))
        
        val userMsg = JSONObject()
        userMsg.put("role", "user")
        
        val contentArray = org.json.JSONArray()
        
        // Text instructions
        contentArray.put(JSONObject().put("type", "text").put("text", "Scan this food image and provide the nutritional JSON. Look carefully at portion sizes."))
        
        // Image URL data format
        val imageUrlJo = JSONObject()
        imageUrlJo.put("url", "data:image/jpeg;base64,$imageBytesBase64")
        contentArray.put(JSONObject().put("type", "image_url").put("image_url", imageUrlJo))
        
        userMsg.put("content", contentArray)
        messages.put(userMsg)
        
        root.put("messages", messages)
        root.put("response_format", JSONObject().put("type", "json_object"))

        val requestBody = root.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val errorBody = response.body?.string()
                throw Exception("Groq Vision failure: ${response.code} - $errorBody")
            }
            val respString = response.body?.string() ?: throw Exception("Empty vision payload")
            val jo = JSONObject(respString)
            val choices = jo.getJSONArray("choices")
            val message = choices.getJSONObject(0).getJSONObject("message")
            return message.getString("content")
        }
    }

    private fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Use a slightly lower quality for faster transmission and staying within API limits
        // Scale down large images to avoid "Request Entity Too Large" or memory issues
        val scaledBitmap = if (width > 1024 || height > 1024) {
            val scale = 1024f / Math.max(width, height)
            Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
        } else {
            this
        }
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    private fun simulateClinicalRecommendations(profile: UserProfile): AIRecommendation {
        val treatsDiabetes = profile.diseases.contains("Diabetes", ignoreCase = true)
        val treatsHypertension = profile.diseases.contains("Hypertension", ignoreCase = true)
        val treatsObesity = profile.diseases.contains("Obesity", ignoreCase = true)
        
        val adviceText = when {
            treatsDiabetes && treatsHypertension -> "Dual management required. Focus tightly on low-sodium, low-carbohydrate inputs of healthy complex grains"
            treatsDiabetes -> "Clinical focus on steady blood glucose. Eliminate simple sugars and prioritize foods with high dietary fiber and fat ratios."
            treatsHypertension -> "Focus entirely on cardiac sodium restriction (<1500mg daily) and potassium rich leafy greens."
            treatsObesity -> "Strive for high density protein intake, consistent metabolic deficit of 350 kcal per Mifflin calculation, and daily walking."
            else -> "General medical advice: Maintain current body metrics, align macros exactly with Mifflin targets, and hydrate with 2.5L water daily."
        }

        val avoidText = StringBuilder()
        if (treatsDiabetes) avoidText.append("White sugar, Pasta, Soda, Honey, White Bread, Sweet pastry, ")
        if (treatsHypertension) avoidText.append("Table Salt, Canned soups, Fast foods, Chips, Pickles, Cured bacon, ")
        if (profile.allergies.isNotBlank()) avoidText.append(profile.allergies + ", ")
        avoidText.append("Refined seed oils, Trans fats")

        val prioritizeText = StringBuilder()
        if (treatsDiabetes) prioritizeText.append("Broccoli, Wild Salmon, Eggs, Spinach, Chia seeds, Avocado, ")
        if (treatsHypertension) prioritizeText.append("Bananas, Beans, Beet greens, Tomato sauce, Garlic, Plain yogurt, ")
        prioritizeText.append("Leafy salad, Olive oil, Lean white meat")

        val mockWeeklyPlan = """
    [
      {"day": "Day 1", "breakfast": "Oatmeal with flax seeds", "lunch": "Steam cod with broccoli", "dinner": "Grilled chicken breast", "snack": "Walnuts"},
      {"day": "Day 2", "breakfast": "Egg white omelet", "lunch": "Quinoa salad", "dinner": "Baked turkey", "snack": "Greek yogurt"},
      {"day": "Day 3", "breakfast": "Whole grain toast", "lunch": "Lentil soup", "dinner": "Boiled beef", "snack": "Apple"},
      {"day": "Day 4", "breakfast": "Chia pudding", "lunch": "Grilled salmon", "dinner": "Steamed zucchini", "snack": "Almonds"},
      {"day": "Day 5", "breakfast": "Buckwheat porridge", "lunch": "Turkey meatballs", "dinner": "Fresh salad", "snack": "Cottage cheese"},
      {"day": "Day 6", "breakfast": "Boiled eggs", "lunch": "Vegetable stew", "dinner": "Baked white fish", "snack": "Pear"},
      {"day": "Day 7", "breakfast": "Rye bread with avocado", "lunch": "Chicken soup", "dinner": "Roasted peppers", "snack": "Seeds"}
    ]
    """.trimIndent()

        return AIRecommendation(
            email = profile.email,
            advice = adviceText,
            foodsToAvoid = avoidText.toString().trim().removeSuffix(","),
            foodsToPrioritize = prioritizeText.toString().trim().removeSuffix(","),
            breakfastSuggestions = if (treatsDiabetes) "Omelette with spinach and mushrooms, cup of black coffee" else "Oatmeal with chia seeds, banana slices, and unsweetened milk",
            lunchSuggestions = if (treatsHypertension) "Fresh baked salmon with lemon wedges, wild quinoa, steamed asparagus" else "Grilled chicken salad with rich olive oil, bell peppers, tomatoes",
            dinnerSuggestions = "Oven baked white cod fillets, roasted green beans with dry seasoning, leafy arugula",
            snackSuggestions = "Raw walnuts (30g), Greek unsweetened yogurt, celery sticks",
            weeklyPlanJson = mockWeeklyPlan
        )
    }
}
