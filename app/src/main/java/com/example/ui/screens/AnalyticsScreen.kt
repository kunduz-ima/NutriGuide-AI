package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodLogEntry
import com.example.ui.viewmodel.MainAppViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateBack: () -> Unit,
    lang: String
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Daily, 1: Weekly, 2: Monthly, 3: Yearly
    val allLogs by mainAppViewModel.allLoggedFoods.collectAsState()
    val profile by mainAppViewModel.userProfile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == "ru") "Аналитика и Активность" else "Biometric Analytics", fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        }
    ) { padding ->
        profile?.let { prof ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(NutriColors.LightPastelGreen)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Period tab items selection
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        TabRow(
                            selectedTabIndex = selectedTab,
                            containerColor = Color.White,
                            contentColor = NutriColors.DeepForest
                        ) {
                            listOf(
                                if (lang == "ru") "День" else "Daily",
                                if (lang == "ru") "Неделя" else "Weekly",
                                if (lang == "ru") "Месяц" else "Monthly",
                                if (lang == "ru") "Год" else "Yearly"
                            ).forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTab == index,
                                    onClick = { selectedTab = index },
                                    text = { Text(title, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) }
                                )
                            }
                        }
                    }
                }

                // GitHub style activity heatmap
                item {
                    CalendarHeatmapCard(allLogs = allLogs, lang = lang)
                }

                // Main analytical charts
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = when (selectedTab) {
                                    0 -> if (lang == "ru") "Дневной баланс нутриентов" else "Daily Nutrient Adherence"
                                    1 -> if (lang == "ru") "Недельная калорийность (ккал)" else "Weekly Calorie Intake Trend (kcal)"
                                    2 -> if (lang == "ru") "Длина трека в этом месяце" else "Monthly Intake Consistency"
                                    else -> if (lang == "ru") "Среднее за год" else "Yearly Summary Graph"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NutriColors.DeepForest
                            )
                            Spacer(modifier = Modifier.height(16.dp))

                            when (selectedTab) {
                                0 -> DailyBreakdownView(allLogs, prof, lang)
                                1 -> WeeklyTrendingView(allLogs, lang)
                                2 -> MonthlyTrendingView(allLogs, lang)
                                3 -> YearlyTrendingView(allLogs, lang)
                            }
                        }
                    }
                }

                // Summary metrics card box
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = NutriColors.DeepForest),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.VerifiedUser, contentDescription = "Medical", tint = NutriColors.LightEmerald)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Клиническое заключение" else "Clinical Nutritionist Verdict",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 15.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = getClinicalVerdict(allLogs, prof, lang),
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        } ?: Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NutriColors.DeepForest)
        }
    }
}

@Composable
fun DailyBreakdownView(logs: List<FoodLogEntry>, profile: com.example.data.model.UserProfile, lang: String) {
    val df = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val todayStr = df.format(Date())
    val todayLogs = logs.filter { it.dateString == todayStr }

    val totalCalories = todayLogs.sumOf { it.calories.toDouble() }.toFloat()
    val totalProtein = todayLogs.sumOf { it.proteinG.toDouble() }.toFloat()
    val totalCarbs = todayLogs.sumOf { it.carbsG.toDouble() }.toFloat()
    val totalFat = todayLogs.sumOf { it.fatG.toDouble() }.toFloat()

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "${if (lang == "ru") "Всего сегодня:" else "Today's Intake Total:"} ${totalCalories.toInt()} kcal / ${profile.dailyCalorieTarget.toInt()} kcal limit",
            fontWeight = FontWeight.SemiBold,
            fontSize = 14.sp
        )

        // Progress bar for calories
        val calRatio = if (profile.dailyCalorieTarget > 0) (totalCalories / profile.dailyCalorieTarget).coerceIn(0f, 1.2f) else 0f
        LinearProgressIndicator(
            progress = { calRatio.coerceAtMost(1f) },
            modifier = Modifier.fillMaxWidth().height(10.dp),
            color = if (calRatio > 1f) NutriColors.DangerRed else NutriColors.DeepForest,
            trackColor = NutriColors.LightPastelGreen,
        )

        Spacer(modifier = Modifier.height(4.dp))

        // Simple customized grid or list of macronutrient values
        listOf(
            Triple(Translations.get("protein", lang), totalProtein, profile.dailyProteinTargetGrams),
            Triple(Translations.get("carbs", lang), totalCarbs, profile.dailyCarbTargetGrams),
            Triple(Translations.get("fat", lang), totalFat, profile.dailyFatTargetGrams)
        ).forEach { (label, cur, tar) ->
            val macroRatio = if (tar > 0) (cur / tar).coerceIn(0f, 1f) else 0f
            Column {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(label, fontSize = 12.sp, color = Color.Gray)
                    Text("${cur.toInt()}g / ${tar.toInt()}g limit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                LinearProgressIndicator(
                    progress = { macroRatio },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = NutriColors.LightEmerald,
                    trackColor = Color(0xFFEEEEEE),
                )
            }
        }
    }
}

@Composable
fun WeeklyTrendingView(logs: List<FoodLogEntry>, lang: String) {
    // Collect last 7 calendar days calorie inputs
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val cal = Calendar.getInstance()
    val weekValues = mutableListOf<Pair<String, Float>>()

    for (i in 6 downTo 0) {
        val dateCopy = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
        val dateStr = sdf.format(dateCopy.time)
        val dayLogs = logs.filter { it.dateString == dateStr }
        val calories = dayLogs.sumOf { it.calories.toDouble() }.toFloat()
        
        val formatDay = SimpleDateFormat("EEE", Locale.getDefault())
        val dayAbbreviated = formatDay.format(dateCopy.time)
        weekValues.add(Pair(dayAbbreviated, calories))
    }

    // Dynamic graph rendering using Canvas
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .padding(top = 10.dp)
    ) {
        val maxVal = (weekValues.maxOfOrNull { it.second } ?: 1000f).coerceAtLeast(2000f)
        val spaceBetween = size.width / 7
        val canvasHeight = size.height - 40f

        // Draw horizontal grid line values
        val paintGridLines = Color(0xFFECECEC)
        drawLine(paintGridLines, Offset(0f, canvasHeight * 0.25f), Offset(size.width, canvasHeight * 0.25f), strokeWidth = 1f)
        drawLine(paintGridLines, Offset(0f, canvasHeight * 0.5f), Offset(size.width, canvasHeight * 0.5f), strokeWidth = 1f)
        drawLine(paintGridLines, Offset(0f, canvasHeight * 0.75f), Offset(size.width, canvasHeight * 0.75f), strokeWidth = 1f)

        // Draw vertical columns
        weekValues.forEachIndexed { idx, pair ->
            val colX = (idx * spaceBetween) + (spaceBetween / 2) - 16f
            val percentageHeight = pair.second / maxVal
            val colHeight = canvasHeight * percentageHeight
            val colY = canvasHeight - colHeight

            // Gradient coloring
            val barColor = if (pair.second > 2200f) Color(0xFFC62828) else Color(0xFF0F5257)

            drawRect(
                color = barColor,
                topLeft = Offset(colX, colY),
                size = Size(32f, colHeight.coerceAtLeast(4f))
            )
        }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        weekValues.forEach { pair ->
            Text(pair.first, fontSize = 11.sp, modifier = Modifier.width(36.dp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MonthlyTrendingView(logs: List<FoodLogEntry>, lang: String) {
    val daysWithLog = logs.groupBy { it.dateString }
    val totalLoggedCount = daysWithLog.size
    val compliantLoggingCount = daysWithLog.values.count { dayEntries ->
        val dayCal = dayEntries.sumOf { it.calories.toDouble() }.toFloat()
        dayCal in 1200f..2500f
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (lang == "ru") "Дней с логами за 30 дней:" else "Days recorded (past 30D):", fontSize = 13.sp)
            Text("$totalLoggedCount", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (lang == "ru") "Дней идеального баланса:" else "Clinically compliant days:", fontSize = 13.sp)
            Text("$compliantLoggingCount", fontWeight = FontWeight.Bold, color = NutriColors.LightEmerald, fontSize = 13.sp)
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text(
            if (lang == "ru") "Месячный отчет демонстрирует стабильность заполнения дневника. Соблюдение оптимального дефицита важно для здорового стажа." 
            else "A consistent track of nutrient logs builds reliable metabolic predictions. Maintain daily logs to improve accuracy.",
            fontSize = 12.sp,
            color = Color.DarkGray
        )
    }
}

@Composable
fun YearlyTrendingView(logs: List<FoodLogEntry>, lang: String) {
    val currentYearLogsCount = logs.size
    val avgDailyCal = if (logs.isNotEmpty()) {
        val grouped = logs.groupBy { it.dateString }
        grouped.values.map { day -> day.map { it.calories }.sum() }.average().toFloat()
    } else 0f

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (lang == "ru") "Резюме за год:" else "Annual Biometric Summary:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (lang == "ru") "Всего зафиксировано блюд:" else "Total food entries logged:", fontSize = 13.sp)
            Text("$currentYearLogsCount", fontWeight = FontWeight.Bold)
        }
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(if (lang == "ru") "Средняя калорийность:" else "Yearly mean calorie intake:", fontSize = 13.sp)
            Text("${avgDailyCal.toInt()} kcal / day", fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
        }
    }
}

@Composable
fun CalendarHeatmapCard(allLogs: List<FoodLogEntry>, lang: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (lang == "ru") "Календарь активности (GitHub-стиль)" else "Nutrition Logs Heatmap (GitHub-Style)",
                fontWeight = FontWeight.Bold,
                color = NutriColors.DeepForest,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Build a 14 columns by 7 rows matrix representation representing the last 14 weeks
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.WEEK_OF_YEAR, -13) // go back 13 weeks (total 14 weeks)
            calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // start the week correctly

            val weeksList = mutableListOf<List<Triple<String, Int, Boolean>>>() // Date, Calories, isToday

            val todayStr = sdf.format(Date())

            for (w in 0..13) {
                val weekDays = mutableListOf<Triple<String, Int, Boolean>>()
                for (d in 0..6) {
                    val dateStr = sdf.format(calendar.time)
                    val daysLog = allLogs.filter { it.dateString == dateStr }
                    val calSum = daysLog.map { it.calories }.sum().toInt()
                    
                    weekDays.add(Triple(dateStr, calSum, dateStr == todayStr))
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                weeksList.add(weekDays)
            }

            // Draw grid in Compose
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                weeksList.forEach { week ->
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        week.forEach { (date, cals, isToday) ->
                            val colorBox = when {
                                cals == 0 -> Color(0xFFF3F4F6) // very light gray
                                cals < 500 -> Color(0xFFC7E9B4) // light green
                                cals < 1500 -> Color(0xFF7FCDBB) // medium green
                                cals < 2500 -> Color(0xFF1D91C0) // intense teal
                                else -> Color(0xFF081D58) // deep clinical dark blue
                            }

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(colorBox, RoundedCornerShape(2.dp))
                                    .border(
                                        if (isToday) 1.5.dp else 0.dp,
                                        if (isToday) NutriColors.DangerRed else Color.Transparent,
                                        RoundedCornerShape(2.dp)
                                    )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(if (lang == "ru") "Реже" else "Less", fontSize = 10.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                listOf(0, 400, 1200, 2000, 3000).forEach { score ->
                    val colorSample = when {
                        score == 0 -> Color(0xFFF3F4F6)
                        score < 500 -> Color(0xFFC7E9B4)
                        score < 1500 -> Color(0xFF7FCDBB)
                        score < 2500 -> Color(0xFF1D91C0)
                        else -> Color(0xFF081D58)
                    }
                    Box(modifier = Modifier.size(11.dp).background(colorSample, RoundedCornerShape(1.dp)))
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(if (lang == "ru") "Чаще" else "More", fontSize = 10.sp, color = Color.Gray)
            }
        }
    }
}

private fun getClinicalVerdict(logs: List<FoodLogEntry>, profile: com.example.data.model.UserProfile, lang: String): String {
    val totalLogCount = logs.size
    if (totalLogCount == 0) {
        return if (lang == "ru") {
            "Вы еще не зафиксировали никаких продуктов в дневнике. Рекомендуется начать логирование для получения верифицированных выводов."
        } else {
            "No client food records identified. Register your meals today to kickstart the clinical diagnostic engine."
        }
    }

    val targetExceededDays = logs.groupBy { it.dateString }.values.count { day ->
        val sumCals = day.sumOf { it.calories.toDouble() }.toFloat()
        sumCals > profile.dailyCalorieTarget
    }

    return if (lang == "ru") {
        if (targetExceededDays > 0) {
            "Зафиксированы случаи превышения калорийности над Mifflin таргетом. Внимание! Снизьте углеводы и добавьте клетчатку."
        } else {
            "Профиль в превосходном тонусе! Индекс веса и макроэлементы соответствуют медицинским ожиданиям."
        }
    } else {
        if (targetExceededDays > 0) {
            "Identified occasional calorie targets exceeding current Mifflin recommendations. Reduce sodium and restrict heavy processed carbs."
        } else {
            "Excellent compliance standard! Your biometric trends align perfectly with therapeutic guidelines."
        }
    }
}
