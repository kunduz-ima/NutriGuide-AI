package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.ResultState
import org.json.JSONArray

data class WeeklyDayPlan(
    val day: String,
    val breakfast: String,
    val lunch: String,
    val dinner: String,
    val snack: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIRecommendationScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateBack: () -> Unit,
    lang: String
) {
    val context = LocalContext.current
    val recommendation by mainAppViewModel.aiRecommendation.collectAsState()
    val generatingState by mainAppViewModel.recommendationGeneratingState.collectAsState()

    val weeklyList = remember(recommendation?.weeklyPlanJson) {
        val json = recommendation?.weeklyPlanJson ?: ""
        if (json.isBlank()) return@remember emptyList<WeeklyDayPlan>()
        try {
            val arr = JSONArray(json)
            val list = mutableListOf<WeeklyDayPlan>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    WeeklyDayPlan(
                        day = obj.optString("day", "Day ${i + 1}"),
                        breakfast = obj.optString("breakfast", ""),
                        lunch = obj.optString("lunch", ""),
                        dinner = obj.optString("dinner", ""),
                        snack = obj.optString("snack", "")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList<WeeklyDayPlan>()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == "ru") "Клинический ИИ Нутрициолог" else "AI Clinical Recommendations", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Recommendation engine trigger
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Psychology, contentDescription = "AI", tint = NutriColors.DeepForest, modifier = Modifier.size(50.dp))
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (lang == "ru") "Генератор рекомендаций Groq AI" else "Groq AI Clinical Recommendation Engine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = NutriColors.DeepForest
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (lang == "ru") 
                                "Генерирует индивидуальное руководство по питанию с учетом вашей истории болезней, аллергий и ИМТ." 
                                else "Analyzes your unique clinical diagnoses, height, weight, activity and targets using Groq model reasoning.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 15.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        if (generatingState is ResultState.Loading) {
                            CircularProgressIndicator(color = NutriColors.DeepForest)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (lang == "ru") "Опрашиваем ИИ медика..." else "Contacting Medical AI. Please wait...",
                                fontSize = 12.sp,
                                color = Color.DarkGray
                            )
                        } else {
                            Button(
                                onClick = { mainAppViewModel.generateRecommendation() },
                                colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = "Sparkle")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (lang == "ru") "Получить ИИ рекомендации" else "Generate/Sync AI Plan", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // Recommendations presentation
            recommendation?.let { rec ->
                // Advice card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MedicalServices, contentDescription = "Clinical", tint = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Клиническое врачебное предписание" else "Personalized Nutrition Advice",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = rec.advice,
                                fontSize = 12.sp,
                                lineHeight = 18.sp,
                                color = Color.Black
                            )
                        }
                    }
                }

                // Foods to Avoid / Prioritize cards side-by-side or stacked
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Avoid
                        Card(
                            colors = CardDefaults.cardColors(containerColor = NutriColors.DangerLight),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.Cancel, contentDescription = "Avoid", tint = NutriColors.DangerRed)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (lang == "ru") "Избегайте этих продуктов" else "Clinical Foods To Avoid",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = NutriColors.DangerRed
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rec.foodsToAvoid,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }

                        // Prioritize
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = "Prioritize", tint = Color(0xFF2E7D32))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (lang == "ru") "Продукты в приоритете" else "Clinical Foods To Prioritize",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF2E7D32)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = rec.foodsToPrioritize,
                                        fontSize = 12.sp,
                                        color = Color.Black,
                                        lineHeight = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Weekly Plan Section
                if (weeklyList.isNotEmpty()) {
                    item {
                        Text(
                            text = if (lang == "ru") "План питания на неделю" else "7-Day Weekly Meal Plan",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = NutriColors.DeepForest,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    weeklyList.forEach { dayPlan ->
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = dayPlan.day,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = NutriColors.LightEmerald
                                    )
                                    Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                                    
                                    MealRow(if (lang == "ru") "Завтрак" else "Breakfast", dayPlan.breakfast, Icons.Filled.Egg)
                                    MealRow(if (lang == "ru") "Обед" else "Lunch", dayPlan.lunch, Icons.Filled.Restaurant)
                                    MealRow(if (lang == "ru") "Ужин" else "Dinner", dayPlan.dinner, Icons.Filled.LocalPizza)
                                    MealRow(if (lang == "ru") "Перекус" else "Snack", dayPlan.snack, Icons.Filled.Cookie)
                                }
                            }
                        }
                    }
                }

                // Today's single day suggestions (legacy/quick view)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.MenuBook, contentDescription = "Menu", tint = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Рекомендации на сегодня" else "Today's Quick Suggestions",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))

                            MealRow(if (lang == "ru") "Завтрак" else "Breakfast", rec.breakfastSuggestions, Icons.Filled.Egg)
                            MealRow(if (lang == "ru") "Обед" else "Lunch", rec.lunchSuggestions, Icons.Filled.Restaurant)
                            MealRow(if (lang == "ru") "Ужин" else "Dinner", rec.dinnerSuggestions, Icons.Filled.LocalPizza)
                            MealRow(if (lang == "ru") "Перекусы" else "Snack", rec.snackSuggestions, Icons.Filled.Cookie)
                        }
                    }
                }
            } ?: run {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (lang == "ru") "План еще не сгенерирован. Нажмите кнопку выше для синхронизации." else "No recommendations cached yet. Tap generate button.",
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MealRow(mealName: String, suggestion: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(NutriColors.LightPastelGreen, RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = mealName, tint = NutriColors.DeepForest, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(mealName, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = NutriColors.DeepForest)
            Text(suggestion, fontSize = 12.sp, color = Color.DarkGray, lineHeight = 16.sp)
        }
    }
}
