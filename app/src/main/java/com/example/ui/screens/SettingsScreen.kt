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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.ResultState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateBack: () -> Unit,
    lang: String
) {
    val context = LocalContext.current
    val profile by mainAppViewModel.userProfile.collectAsState()
    val savedState by mainAppViewModel.settingsSaveState.collectAsState()

    // Key inputs
    val groqKeyFlowState by mainAppViewModel.groqApiKey.collectAsState()

    // Form variables
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Female") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var activity by remember { mutableStateOf("Moderate") }
    var goal by remember { mutableStateOf("Lose") }
    var language by remember { mutableStateOf(lang) }
    var diseases by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var preferences by remember { mutableStateOf("") }
    var notificationsAllowed by remember { mutableStateOf(true) }
    var groqApiKeyStr by remember { mutableStateOf("") }

    var isInitialized by remember { mutableStateOf(false) }

    // Synchronize initial database states to form inputs
    LaunchedEffect(profile) {
        profile?.let { prof ->
            if (!isInitialized) {
                name = prof.name
                age = prof.age.toString()
                gender = prof.gender
                height = prof.heightCm.toInt().toString()
                weight = prof.weightKg.toInt().toString()
                activity = prof.activityLevel
                goal = prof.mainGoal
                diseases = prof.diseases
                allergies = prof.allergies
                preferences = prof.dietaryPreferences

                // Fetch dynamic keys
                groqApiKeyStr = groqKeyFlowState

                isInitialized = true
            }
        }
    }

    LaunchedEffect(savedState) {
        if (savedState is ResultState.Success) {
            Toast.makeText(context, if (lang == "ru") "Конфигурации сохранены!" else "Settings Saved Successfully!", Toast.LENGTH_SHORT).show()
            mainAppViewModel.clearSettingsSaveState()
            onNavigateBack()
        } else if (savedState is ResultState.Error) {
            Toast.makeText(context, (savedState as ResultState.Error).message, Toast.LENGTH_LONG).show()
            mainAppViewModel.clearSettingsSaveState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (lang == "ru") "Настройки профиля" else "Settings & Keys Manager", fontWeight = FontWeight.Bold, color = Color.White) },
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
                // Section 1: Biometric parameters
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Person, contentDescription = "User", tint = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Биометрические показатели" else "Biometrics & Metrics",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text(if (lang == "ru") "Имя клиента" else "Client Name") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { age = it },
                                    label = { Text(if (lang == "ru") "Возраст" else "Age") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { height = it },
                                    label = { Text(if (lang == "ru") "Рост (см)" else "Height (cm)") },
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { weight = it },
                                    label = { Text(if (lang == "ru") "Вес (кг)" else "Weight (kg)") },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                // Section 2: Diseases, Allergies and Dietary restrictions
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.HealthAndSafety, contentDescription = "Medical", tint = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Анамнез и ограничения" else "Pathologies & Dietary",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            OutlinedTextField(
                                value = diseases,
                                onValueChange = { diseases = it },
                                label = { Text(if (lang == "ru") "Диагнозы (например: Diabetes, Hypertension)" else "Diagnoses (e.g. Diabetes, Hypertension)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = allergies,
                                onValueChange = { allergies = it },
                                label = { Text(if (lang == "ru") "Пищевые аллергии (например: Peanut, Gluten)" else "Allergies (e.g. Peanut, Gluten)") },
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = preferences,
                                onValueChange = { preferences = it },
                                label = { Text(if (lang == "ru") "Предпочтения в питании (напр. Vegan)" else "Dietary Restrictions (e.g. Vegan)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Section 3: Applications features (Languages, Notifications and API keys)
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.SettingsInputHdmi, contentDescription = "Integrations", tint = NutriColors.DeepForest)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (lang == "ru") "Приложение и Ключи" else "Preferences & API Keys",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                            }
                            Divider(color = Color.LightGray.copy(alpha = 0.5f))

                            // Translate locale button toggler
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (lang == "ru") "Язык интерфейса" else "App Language Setting", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Row {
                                    Button(
                                        onClick = { language = "en" },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (language == "en") NutriColors.DeepForest else Color.LightGray),
                                        modifier = Modifier.padding(end = 4.dp),
                                        shape = RoundedCornerShape(6.dp)
                                    ) { Text("EN", fontSize = 10.sp) }
                                    Button(
                                        onClick = { language = "ru" },
                                        colors = ButtonDefaults.buttonColors(containerColor = if (language == "ru") NutriColors.DeepForest else Color.LightGray),
                                        shape = RoundedCornerShape(6.dp)
                                    ) { Text("RU", fontSize = 10.sp) }
                                }
                            }

                            // Notification permission switcher
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(if (lang == "ru") "WorkManager Напоминания" else "WorkManager Reminder Alarms", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                Switch(
                                    checked = notificationsAllowed,
                                    onCheckedChange = { notificationsAllowed = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = NutriColors.DeepForest)
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (lang == "ru") "Настройка персональных API ключей для авторизации внешних нейросетей:"
                                else "Define secret API keys below to utilize dynamic advanced AI features secure-wise:",
                                fontSize = 11.sp,
                                color = Color.Gray,
                                lineHeight = 15.sp
                            )

                            OutlinedTextField(
                                value = groqApiKeyStr,
                                onValueChange = { groqApiKeyStr = it },
                                label = { Text("Groq Cloud API Key") },
                                placeholder = { Text("gsk_...") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                // Save parameters action button
                item {
                    Button(
                        onClick = {
                            mainAppViewModel.saveUserSettings(
                                name = name,
                                ageStr = age,
                                gender = gender,
                                heightStr = height,
                                weightStr = weight,
                                activity = activity,
                                goal = goal,
                                lang = language,
                                diseases = diseases,
                                allergies = allergies,
                                preferences = preferences,
                                notificationsEnabled = notificationsAllowed,
                                groqKey = groqApiKeyStr
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (savedState is ResultState.Loading) {
                            CircularProgressIndicator(color = Color.White)
                        } else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Save, contentDescription = "Save")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (lang == "ru") "Сохранить изменения" else "Save Clinical Changes", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NutriColors.DeepForest)
            }
        }
    }
}
