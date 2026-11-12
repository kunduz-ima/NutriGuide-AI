package com.example.data.export

import android.app.DownloadManager
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.widget.Toast
import com.example.data.model.AIRecommendation
import com.example.data.model.FoodLogEntry
import com.example.data.model.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter

object Exporter {

    /**
     * Export all logs for the current user into a fully compliant CSV document
     */
    fun exportLogsToCsv(context: Context, profile: UserProfile, logs: List<FoodLogEntry>): File {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val fileName = "NutriGuide_Calorie_Log_${profile.name.replace(" ", "_")}_${System.currentTimeMillis()}.csv"
        val file = File(directory, fileName)
        
        FileWriter(file).use { writer ->
            writer.write("User Email,Name,Date,Meal Type,Food Name,Quantity (g),Calories,Protein (g),Carbs (g),Fat (g),Sodium (mg),Sugar (g)\n")
            logs.forEach { log ->
                val normalizedEmail = profile.email
                val normalizedName = profile.name.replace("\"", "\"\"")
                val foodNameClean = log.name.replace("\"", "\"\"")
                val brandClean = (log.brandOwner ?: "Generic").replace("\"", "\"\"")
                writer.write("\"$normalizedEmail\",\"$normalizedName\",\"${log.dateString}\",\"${log.mealType}\",\"$foodNameClean\",${log.quantityGrams},${log.calories},${log.proteinG},${log.carbsG},${log.fatG},${log.sodiumMg},${log.sugarG}\n")
            }
        }
        
        // Add to DownloadManager to make it visible to user
        addToDownloads(context, file, "text/csv")
        
        return file
    }

    /**
     * Generate an authentic Clinical PDF Medical Report using native Android Canvas-based PdfDocument.
     * Incorporates user metrics, clinical diseases, AI recommendations, and full logs history.
     */
    fun exportReportToPdf(
        context: Context,
        profile: UserProfile,
        recommendation: AIRecommendation?,
        allLogs: List<FoodLogEntry>
    ): File {
        val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        val fileName = "NutriGuide_AI_Clinical_Report_${profile.name.replace(" ", "_")}_${System.currentTimeMillis()}.pdf"
        val file = File(directory, fileName)

        val pdfDocument = PdfDocument()
        
        // standard Letter/A4 format page size (595 x 842 points)
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        
        val canvas: Canvas = page.canvas
        val paint = Paint()
        var yPosition = 50f

        // Draw header background
        paint.color = Color.parseColor("#0F5257") // Deep Forest
        canvas.drawRect(30f, 30f, 565f, 90f, paint)

        // Draw header text
        paint.color = Color.WHITE
        paint.textSize = 20f
        paint.isFakeBoldText = true
        canvas.drawText("NUTRIGUIDE AI: MOBCURATED CLINICAL REPORT", 45f, 65f, paint)

        paint.textSize = 10f
        paint.isFakeBoldText = false
        canvas.drawText("Empowered by Groq Recommendation Engine & Local Anamnesis", 45f, 80f, paint)

        // Draw User Profile Info
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.isFakeBoldText = true
        yPosition = 120f
        canvas.drawText("1. CLIENT BIOMETRIC PROFILE", 30f, yPosition, paint)

        paint.isFakeBoldText = false
        paint.textSize = 10f
        yPosition += 20f
        canvas.drawText("Client Name: ${profile.name}", 40f, yPosition, paint)
        canvas.drawText("Biological Gender: ${profile.gender}", 300f, yPosition, paint)

        yPosition += 15f
        canvas.drawText("Age: ${profile.age} years  |  Height: ${profile.heightCm} cm  |  Weight: ${profile.weightKg} kg", 40f, yPosition, paint)
        canvas.drawText("BMI Status: ${"%.2f".format(profile.bmi)}", 300f, yPosition, paint)

        yPosition += 15f
        canvas.drawText("Client Aesthetic/Health Goal: ${profile.mainGoal}", 40f, yPosition, paint)
        canvas.drawText("Daily Calorie Target limit: ${profile.dailyCalorieTarget.toInt()} kcal", 300f, yPosition, paint)

        yPosition += 15f
        canvas.drawText("Prescribed Macros: Protein: ${profile.dailyProteinTargetGrams.toInt()}g | Carbs: ${profile.dailyCarbTargetGrams.toInt()}g | Fats: ${profile.dailyFatTargetGrams.toInt()}g", 40f, yPosition, paint)

        // Draw Medical Anamnesis
        yPosition += 30f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("2. REGISTERED CLINICAL DIAGNOSES & ALLERGIES", 30f, yPosition, paint)

        paint.isFakeBoldText = false
        paint.textSize = 10f
        yPosition += 20f
        val diseases = profile.diseases.ifBlank { "No severe pathology declared / Не зафиксировано" }
        canvas.drawText("Documented Medical Issues: $diseases", 40f, yPosition, paint)

        yPosition += 15f
        val allergies = profile.allergies.ifBlank { "No known food allergies declared / Отсутствует" }
        canvas.drawText("Registered Allergens: $allergies", 40f, yPosition, paint)

        yPosition += 15f
        val preconfiguredPreferences = profile.dietaryPreferences.ifBlank { "None" }
        canvas.drawText("Dietary Preferences: $preconfiguredPreferences", 40f, yPosition, paint)

        // Draw Groq Recommendation Sections
        yPosition += 35f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("3. GROQ AI CLINICAL RECOMMENDATIONS", 30f, yPosition, paint)

        paint.textSize = 10f
        if (recommendation != null) {
            paint.isFakeBoldText = false
            yPosition += 20f
            
            // Advice - Wrap text safely
            val adviceLines = splitTextIntoLines(recommendation.advice, 95)
            canvas.drawText("Advice Insight / Общие рекомендации:", 40f, yPosition, paint)
            yPosition += 15f
            adviceLines.forEach { line ->
                canvas.drawText(line, 50f, yPosition, paint)
                yPosition += 12f
            }

            yPosition += 10f
            canvas.drawText("Foods to Avoid completely: ${recommendation.foodsToAvoid}", 40f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("Nutritional Priorities: ${recommendation.foodsToPrioritize}", 40f, yPosition, paint)
            
            yPosition += 20f
            paint.isFakeBoldText = true
            canvas.drawText("AI COMPLIANT MEAL PLAN SUGGESTIONS:", 40f, yPosition, paint)
            paint.isFakeBoldText = false
            yPosition += 15f
            canvas.drawText("• Breakfast: ${recommendation.breakfastSuggestions}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("• Lunch: ${recommendation.lunchSuggestions}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("• Dinner: ${recommendation.dinnerSuggestions}", 50f, yPosition, paint)
            yPosition += 15f
            canvas.drawText("• Snacks: ${recommendation.snackSuggestions}", 50f, yPosition, paint)
        } else {
            paint.isFakeBoldText = false
            yPosition += 20f
            canvas.drawText("No synchronized artificial advice cached yet. Sync AI on recommendations tab.", 40f, yPosition, paint)
        }

        // Draw Section 4: Nutrition History Summary
        yPosition += 35f
        paint.textSize = 12f
        paint.isFakeBoldText = true
        canvas.drawText("4. NUTRITION LOGS REGISTRY HISTORY (Last logged items)", 30f, yPosition, paint)

        paint.isFakeBoldText = false
        paint.textSize = 9f
        yPosition += 15f

        if (allLogs.isEmpty()) {
            canvas.drawText("No logged intake entries recorded for the client yet.", 40f, yPosition, paint)
        } else {
            canvas.drawText("Meal Type | Food Item Name | Qty (g) | Calories | Macros (P/C/F)", 40f, yPosition, paint)
            canvas.drawLine(40f, yPosition + 5f, 540f, yPosition + 5f, paint)
            
            yPosition += 10f
            allLogs.take(12).forEach { log ->
                yPosition += 12f
                if (yPosition < 800f) {
                    canvas.drawText(
                        "${log.mealType} | ${log.name.take(30)} | ${log.quantityGrams.toInt()}g | ${log.calories.toInt()} kcal | P:${log.proteinG.toInt()}g C:${log.carbsG.toInt()}g F:${log.fatG.toInt()}g", 
                        40f, 
                        yPosition, 
                        paint
                    )
                }
            }
        }

        // Footer disclaimer
        paint.textSize = 8f
        paint.color = Color.GRAY
        canvas.drawText("This medical report is generated digitally using AI and is meant as a biometric aid.", 30f, 825f, paint)

        pdfDocument.finishPage(page)

        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        
        pdfDocument.close()
        
        // Add to DownloadManager
        addToDownloads(context, file, "application/pdf")
        
        return file
    }

    private fun addToDownloads(context: Context, file: File, mimeType: String) {
        try {
            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.addCompletedDownload(
                file.name,
                "NutriGuide Clinical Export",
                true,
                mimeType,
                file.absolutePath,
                file.length(),
                true
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun splitTextIntoLines(text: String, maxChars: Int): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        words.forEach { word ->
            if (currentLine.length + word.length + 1 > maxChars) {
                lines.add(currentLine.toString())
                currentLine = StringBuilder(word)
            } else {
                if (currentLine.isNotEmpty()) currentLine.append(" ")
                currentLine.append(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
