package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FoodLogEntry
import com.example.data.model.UserProfile
import com.example.data.model.UsdaFoodItem
import com.example.data.warning.HealthWarning
import com.example.data.warning.WarningSeverity
import com.example.ui.viewmodel.*

/**
 * Cohesive translation resource manager mapping strings locally.
 */
object Translations {
    private val content = mapOf(
        "app_title" to Pair("NutriGuide AI", "NutriGuide AI"),
        "login" to Pair("Sign In", "Войти"),
        "register" to Pair("Create Account", "Регистрация"),
        "email" to Pair("Email", "Электронная почта"),
        "password" to Pair("Password", "Пароль"),
        "security_question" to Pair("Your first pet's name? (Security)", "Имя первого питомца? (Вопрос)"),
        "answer" to Pair("Secret Keyword Answer", "Ответ на секретный вопрос"),
        "forgot_psw" to Pair("Forgot Password?", "Забыли пароль?"),
        "forgot_title" to Pair("Recover Account", "Восстановление пароля"),
        "forgot_desc" to Pair("Verification answer recovers credentials safely.", "Секретное слово поможет восстановить пароль."),
        "onboarding" to Pair("Clinical Metrics Setup", "Клинический профиль"),
        "age" to Pair("Age (Years)", "Возраст (лет)"),
        "weight" to Pair("Weight (kg)", "Вес (кг)"),
        "height" to Pair("Height (cm)", "Рост (см)"),
        "gender" to Pair("Biological Gender", "Биологический пол"),
        "activity" to Pair("Activity Multiplier", "Физическая нагрузка"),
        "goal" to Pair("Caloric Directive", "Цель калорийности"),
        "diseases" to Pair("Medical Diagnosis Records", "Диагнозы и заболевания"),
        "preferences" to Pair("Dietary Preferences", "Диетические предпочтения"),
        "allergies" to Pair("Severe Food Allergies", "Пищевая аллергия"),
        "calculate" to Pair("Inspect Formula Targets", "Посмотреть расчеты"),
        "dashboard" to Pair("Health Hub", "Главная"),
        "food_log" to Pair("Food Registry Log", "Дневник еды"),
        "search_food" to Pair("USDA Database Search", "Поиск продуктов"),
        "calories" to Pair("Calories", "Калории"),
        "protein" to Pair("Protein", "Белки"),
        "carbs" to Pair("Carbs", "Углеводы"),
        "fat" to Pair("Fat", "Жиры"),
        "sodium" to Pair("Sodium", "Натрий"),
        "sugar" to Pair("Sugar", "Сахар"),
        "calories_left" to Pair("remaining", "осталось"),
        "add_item" to Pair("Add Food Item", "Добавить продукт"),
        "severity_danger" to Pair("CRITICAL INGREDIENT ALERT", "КРИТИЧЕСКОЕ ПРЕДУПРЕЖДЕНИЕ"),
        "severity_warning" to Pair("DIETARY CAUTION ADVICE", "РЕКОМЕНДАЦИЯ К ПРЕДОСТЕРЕЖЕНИЮ"),
        "empty_log" to Pair("No entries logged for today yet.", "Дневник питания на сегодня пуст."),
        "quick_add_label" to Pair("Portion size (grams):", "Размер порции в граммах:"),
        "export_csv" to Pair("Save CSV Sheet", "Сохранить CSV"),
        "export_pdf" to Pair("Save Health Report", "Экспорт PDF отчета"),
        "export_success" to Pair("Report successfully saved to Documents directory!", "Отчет успешно экспортирован в Документы!"),
        "details_header" to Pair("USDA Clinical Profile", "Пищевой профиль USDA"),
        "no_warnings" to Pair("No compliance notifications for this product.", "Продукт идеален для вашего анамнеза.")
    )

    fun get(key: String, lang: String): String {
        val entry = content[key] ?: return key
        return if (lang == "ru") entry.second else entry.first
    }
}

/**
 * Theme color container representing a clean, high-contrast healthy green design with sharp red danger alerts.
 */
object NutriColors {
    val DeepForest = Color(0xFF0F5257)
    val LightEmerald = Color(0xFF0B6A6F)
    val LightPastelGreen = Color(0xFFEAF5F5)
    val AmberWarm = Color(0xFFFFB21A)
    val AmberLight = Color(0xFFFFF7E6)
    val DangerRed = Color(0xFFC93B2B)
    val DangerLight = Color(0xFFFDECEB)
}

// ==========================================
// 1. AUTHENTICATION SCREENS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit
) {
    val lang by authViewModel.appLanguage.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Error) {
            Toast.makeText(context, (authState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            authViewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("app_title", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest),
                actions = {
                    IconButton(onClick = { authViewModel.changeLanguage(if (lang == "en") "ru" else "en") }) {
                        Icon(Icons.Filled.Language, contentDescription = "Language", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Translations.get("login", lang),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NutriColors.DeepForest
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Translations.get("email", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("login_email_input"),
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "EmailIcon") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Translations.get("password", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("login_password_input"),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "LockIcon") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = NutriColors.LightEmerald)
                    } else {
                        Button(
                            onClick = { authViewModel.login(email, password) },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("login_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Translations.get("login", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = Translations.get("forgot_psw", lang),
                        color = NutriColors.LightEmerald,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .clickable { onNavigateToForgot() }
                            .padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (lang == "ru") "Впервые здесь? " else "Don't have an account? ")
                Text(
                    text = Translations.get("register", lang),
                    color = NutriColors.DeepForest,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onNavigateToRegister() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val lang by authViewModel.appLanguage.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var questionAnswer by remember { mutableStateOf("") }

    LaunchedEffect(authState) {
        if (authState is AuthUiState.Error) {
            Toast.makeText(context, (authState as AuthUiState.Error).message, Toast.LENGTH_LONG).show()
            authViewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("register", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Translations.get("email", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("register_email_input"),
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text(Translations.get("password", lang)) },
                        modifier = Modifier.fillMaxWidth().testTag("register_password_input"),
                        leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = "Lock") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = questionAnswer,
                        onValueChange = { questionAnswer = it },
                        label = { Text(Translations.get("security_question", lang)) },
                        placeholder = { Text(if (lang == "ru") "Имя питомца" else "e.g. Buddy") },
                        modifier = Modifier.fillMaxWidth().testTag("register_question_input"),
                        leadingIcon = { Icon(Icons.Filled.QuestionMark, contentDescription = "Question") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (authState is AuthUiState.Loading) {
                        CircularProgressIndicator(color = NutriColors.LightEmerald)
                    } else {
                        Button(
                            onClick = { authViewModel.register(email, password, questionAnswer) },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("register_button"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(Translations.get("register", lang), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    val lang by authViewModel.appLanguage.collectAsState()
    val resetState by authViewModel.resetState.collectAsState()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var secretAnswer by remember { mutableStateOf("") }

    LaunchedEffect(resetState) {
        if (resetState is ResetUiState.Error) {
            Toast.makeText(context, (resetState as ResetUiState.Error).message, Toast.LENGTH_LONG).show()
            authViewModel.clearState()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("forgot_title", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = Translations.get("forgot_desc", lang),
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(Translations.get("email", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.Email, contentDescription = "Email") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = secretAnswer,
                        onValueChange = { secretAnswer = it },
                        label = { Text(Translations.get("security_question", lang)) },
                        modifier = Modifier.fillMaxWidth(),
                        leadingIcon = { Icon(Icons.Filled.QuestionMark, contentDescription = "Query") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    if (resetState is ResetUiState.Loading) {
                        CircularProgressIndicator(color = NutriColors.LightEmerald)
                    } else if (resetState is ResetUiState.Success) {
                        Surface(
                            color = NutriColors.LightPastelGreen,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = if (lang == "ru") "Пароль найден успешно!" else "Password recovered successfully!",
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Text(
                                    text = "${Translations.get("password", lang)}: ${(resetState as ResetUiState.Success).passwordHint}",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NutriColors.DangerRed
                                )
                            }
                        }
                        Button(
                            onClick = onNavigateToLogin,
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (lang == "ru") "Назад ко входу" else "Back to Login")
                        }
                    } else {
                        Button(
                            onClick = { authViewModel.forgotPassword(email, secretAnswer) },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (lang == "ru") "Восстановить" else "Validate Recovery", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 2. MULTI-STEP ONBOARDING WIZARD
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onboardingViewModel: OnboardingViewModel,
    userEmail: String,
    onOnboardingFinished: () -> Unit,
    lang: String
) {
    val step by onboardingViewModel.currentStep.collectAsState()
    val saveState by onboardingViewModel.saveState.collectAsState()
    val context = LocalContext.current

    // Gather states
    val name by onboardingViewModel.name.collectAsState()
    val age by onboardingViewModel.age.collectAsState()
    val gender by onboardingViewModel.gender.collectAsState()
    val height by onboardingViewModel.heightCm.collectAsState()
    val weight by onboardingViewModel.weightKg.collectAsState()
    val activity by onboardingViewModel.activityLevel.collectAsState()
    val goal by onboardingViewModel.mainGoal.collectAsState()
    
    val selectedDiseases by onboardingViewModel.checkedDiseases.collectAsState()
    val selectedPreferences by onboardingViewModel.checkedPreferences.collectAsState()
    val selectedAllergies by onboardingViewModel.checkedAllergies.collectAsState()

    val computedTargets by onboardingViewModel.computedTargets.collectAsState()

    LaunchedEffect(saveState) {
        if (saveState is ResultState.Success) {
            onOnboardingFinished()
        } else if (saveState is ResultState.Error) {
            Toast.makeText(context, (saveState as ResultState.Error).message, Toast.LENGTH_LONG).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("onboarding", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest),
                navigationIcon = {
                    if (step > 1 && step < 5) {
                        IconButton(onClick = { onboardingViewModel.prevStep() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Prev", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(20.dp)
        ) {
            // Step marker indicator
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                for (i in 1..5) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .padding(horizontal = 3.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(if (i <= step) NutriColors.DeepForest else Color.LightGray)
                    )
                }
            }

            Card(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (step) {
                        1 -> { // Step 1: Name and Gender
                            item {
                                Text(
                                    text = if (lang == "ru") "Давайте познакомимся" else "Introduce Yourself",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Text(
                                    text = if (lang == "ru") "Введите ваше имя и выберите биологический пол." else "Set your name and biological characteristics.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = name,
                                    onValueChange = { onboardingViewModel.name.value = it },
                                    label = { Text(if (lang == "ru") "Ваше имя" else "First Name") },
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_name"),
                                    singleLine = true
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Text(Translations.get("gender", lang), fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val isMale = gender == "Male"
                                    Button(
                                        onClick = { onboardingViewModel.gender.value = "Male" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (isMale) NutriColors.DeepForest else Color.LightGray
                                        ),
                                        modifier = Modifier.weight(1f).testTag("gender_male")
                                    ) {
                                        Text(if (lang == "ru") "Мужской" else "Male", color = Color.White)
                                    }
                                    Button(
                                        onClick = { onboardingViewModel.gender.value = "Female" },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (!isMale) NutriColors.DeepForest else Color.LightGray
                                        ),
                                        modifier = Modifier.weight(1f).testTag("gender_female")
                                    ) {
                                        Text(if (lang == "ru") "Женский" else "Female", color = Color.White)
                                    }
                                }
                            }
                        }
                        2 -> { // Step 2: Weight, Height, Age
                            item {
                                Text(
                                    text = if (lang == "ru") "Клинические параметры" else "Physiologic Metrics",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedTextField(
                                    value = age,
                                    onValueChange = { onboardingViewModel.age.value = it },
                                    label = { Text(Translations.get("age", lang)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_age")
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = height,
                                    onValueChange = { onboardingViewModel.heightCm.value = it },
                                    label = { Text(Translations.get("height", lang)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_height")
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = weight,
                                    onValueChange = { onboardingViewModel.weightKg.value = it },
                                    label = { Text(Translations.get("weight", lang)) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth().testTag("onboarding_weight")
                                )
                            }
                        }
                        3 -> { // Step 3: Goals and Activity
                            item {
                                Text(
                                    text = if (lang == "ru") "Режим жизни и цели" else "Lifestyle Decisions",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Text(Translations.get("activity", lang), fontWeight = FontWeight.Bold)
                                val activities = listOf(
                                    "Sedentary" to Pair("Low activity (Sitting office work)", "Малоподвижный (Офис)"),
                                    "Light" to Pair("Light activity (1-3 sessions / wk)", "Легкая нагрузка (1-3 тренировки)"),
                                    "Moderate" to Pair("Moderate physical load (3-5 sessions)", "Умеренная (3-5 тренировок)"),
                                    "Active" to Pair("High physical activity (Hard sports)", "Высокая (каждый день)"),
                                    "VeryActive" to Pair("Heavy athletic schedule / labour", "Экстремальная (тяжелый спорт)")
                                )

                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    activities.forEach { (key, names) ->
                                        val isSelected = activity == key
                                        Surface(
                                            color = if (isSelected) NutriColors.LightPastelGreen else Color(0xFAF4F4F4),
                                            shape = RoundedCornerShape(8.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clickable { onboardingViewModel.activityLevel.value = key }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(selected = isSelected, onClick = { onboardingViewModel.activityLevel.value = key })
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(if (lang == "ru") names.second else names.first, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(16.dp))
                                Text(Translations.get("goal", lang), fontWeight = FontWeight.Bold)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    val goals = listOf(
                                        "Lose" to Pair("Lose", "Сбросить"),
                                        "Maintain" to Pair("Maintain", "Удержать"),
                                        "Gain" to Pair("Gain", "Набрать")
                                    )
                                    goals.forEach { (key, pair) ->
                                        val isSelected = goal == key
                                        Button(
                                            onClick = { onboardingViewModel.mainGoal.value = key },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) NutriColors.DeepForest else Color.LightGray
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(if (lang == "ru") pair.second else pair.first, color = Color.White, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                        4 -> { // Step 4: Medical diseases, options, diets, allergies
                            item {
                                Text(
                                    text = if (lang == "ru") "Клинический анамнез" else "Clinical Anamnesis Intake",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                // Diseases
                                Text(Translations.get("diseases", lang), fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                                val diseases = listOf("Diabetes", "Hypertension", "Obesity", "Celiac")
                                diseases.forEach { d ->
                                    val checked = selectedDiseases.contains(d)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { onboardingViewModel.toggleDisease(d) }
                                    ) {
                                        Checkbox(checked = checked, onCheckedChange = { onboardingViewModel.toggleDisease(d) })
                                        Text(d)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                // Preferences
                                Text(Translations.get("preferences", lang), fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                                val prefs = listOf("Vegetarian", "Vegan", "Gluten-Free", "Low-Carb")
                                prefs.forEach { p ->
                                    val checked = selectedPreferences.contains(p)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { onboardingViewModel.togglePreference(p) }
                                    ) {
                                        Checkbox(checked = checked, onCheckedChange = { onboardingViewModel.togglePreference(p) })
                                        Text(p)
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                // Allergies
                                Text(Translations.get("allergies", lang), fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                                val allergiesList = listOf("Peanuts", "Dairy", "Egg", "Fish", "Wheat", "Soy")
                                allergiesList.forEach { a ->
                                    val checked = selectedAllergies.contains(a)
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.fillMaxWidth().clickable { onboardingViewModel.toggleAllergy(a) }
                                    ) {
                                        Checkbox(checked = checked, onCheckedChange = { onboardingViewModel.toggleAllergy(a) })
                                        Text(a)
                                    }
                                }
                            }
                        }
                        5 -> { // Step 5: Computations Review Screen
                            computedTargets?.let { targets ->
                                item {
                                    Text(
                                        text = if (lang == "ru") "Ваш персональный рацион" else "Your Prescribed Bio-Plan",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = NutriColors.DeepForest,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(16.dp))

                                    // BMI card
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = NutriColors.LightPastelGreen),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(Translations.get("bmi_index", lang), fontWeight = FontWeight.SemiBold)
                                            Text("${"%.2f".format(targets.bmi)}", fontSize = 36.sp, fontWeight = FontWeight.Black, color = NutriColors.DeepForest)
                                            val bmiInterp = when {
                                                targets.bmi < 18.5f -> if (lang == "ru") "Дефицит массы" else "Underweight"
                                                targets.bmi < 25.0f -> if (lang == "ru") "Нормальный вес" else "Normal Weight"
                                                targets.bmi < 30.0f -> if (lang == "ru") "Избыточный вес" else "Overweight"
                                                else -> if (lang == "ru") "Ожирение" else "Obesity"
                                            }
                                            Text("Classification: $bmiInterp", fontWeight = FontWeight.Bold, color = Color.Gray)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Text(if (lang == "ru") "Рекомендуемые калории и макронутриенты" else "Macro & Caloric Prescriptions", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Targets dashboard review
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.weight(1.2f).background(Color(0xFFF7FDFD), RoundedCornerShape(8.dp)).padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(Translations.get("calories", lang), fontSize = 12.sp)
                                            Text("${targets.dailyCalorieTarget.toInt()}", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = NutriColors.DeepForest)
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f).background(Color(0xFFFFFBEA), RoundedCornerShape(8.dp)).padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(Translations.get("protein", lang), fontSize = 11.sp)
                                            Text("${targets.dailyProteinTargetGrams.toInt()}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NutriColors.AmberWarm)
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f).background(Color(0xFFFFF5F4), RoundedCornerShape(8.dp)).padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(Translations.get("carbs", lang), fontSize = 11.sp)
                                            Text("${targets.dailyCarbTargetGrams.toInt()}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = NutriColors.DangerRed)
                                        }
                                        Column(
                                            modifier = Modifier.weight(1f).background(Color(0xFFF4FAF5), RoundedCornerShape(8.dp)).padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(Translations.get("fat", lang), fontSize = 11.sp)
                                            Text("${targets.dailyFatTargetGrams.toInt()}g", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF2E7D32))
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))
                                    Text(
                                        text = if (lang == "ru") "*Расчет произведен локально по формуле Миффлина-Джеора с учетом сопутствующих клинических состояний." 
                                               else "*Computed 100% locally using Mifflins protocol incorporating clinical disease weight thresholds.",
                                        fontSize = 11.sp,
                                        color = Color.LightGray,
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Lower Action Row navigation bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step < 5) {
                    val labelNext = if (step == 4) Translations.get("calculate", lang) else (if (lang == "ru") "Далее" else "Next")
                    Button(
                        onClick = { onboardingViewModel.nextStep() },
                        colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("onboarding_next"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(labelNext, fontWeight = FontWeight.Bold)
                    }
                } else {
                    if (saveState is ResultState.Loading) {
                        CircularProgressIndicator(color = NutriColors.DeepForest, modifier = Modifier.align(Alignment.CenterVertically))
                    } else {
                        Button(
                            onClick = { onboardingViewModel.saveProfile(userEmail) },
                            colors = ButtonDefaults.buttonColors(containerColor = NutriColors.LightEmerald),
                            modifier = Modifier.fillMaxWidth().height(50.dp).testTag("onboarding_finish_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (lang == "ru") "Сохранить и войти" else "Complete & Access Dashboard", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// ==========================================
// 3. MAIN APP SCREENS: DASHBOARD & WORKPLACE
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    mainAppViewModel: MainAppViewModel,
    authViewModel: AuthViewModel,
    onNavigateToSearch: () -> Unit,
    onNavigateToLog: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAI: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToScan: () -> Unit,
    lang: String
) {
    val context = LocalContext.current
    val profile by mainAppViewModel.userProfile.collectAsState()
    val loggedFoods by mainAppViewModel.loggedFoods.collectAsState()
    
    // Warnings collections
    val dailyLimitWarnings by mainAppViewModel.dailyLimitWarnings.collectAsState()
    val exportState by mainAppViewModel.exportState.collectAsState()

    LaunchedEffect(exportState) {
        if (exportState is ResultState.Success) {
            Toast.makeText(context, Translations.get("export_success", lang), Toast.LENGTH_LONG).show()
            mainAppViewModel.clearExportState()
        } else if (exportState is ResultState.Error) {
            Toast.makeText(context, (exportState as ResultState.Error).message, Toast.LENGTH_LONG).show()
            mainAppViewModel.clearExportState()
        }
    }

    profile?.let { prof ->
        // Aggregate targets & currently logged intakes
        val totalCalories = loggedFoods.sumOf { it.calories.toDouble() }.toFloat()
        val totalProtein = loggedFoods.sumOf { it.proteinG.toDouble() }.toFloat()
        val totalCarbs = loggedFoods.sumOf { it.carbsG.toDouble() }.toFloat()
        val totalFat = loggedFoods.sumOf { it.fatG.toDouble() }.toFloat()

        val calRatio = if (prof.dailyCalorieTarget > 0) (totalCalories / prof.dailyCalorieTarget).coerceIn(0f, 1f) else 0f
        val protRatio = if (prof.dailyProteinTargetGrams > 0) (totalProtein / prof.dailyProteinTargetGrams).coerceIn(0f, 1f) else 0f
        val carbRatio = if (prof.dailyCarbTargetGrams > 0) (totalCarbs / prof.dailyCarbTargetGrams).coerceIn(0f, 1f) else 0f
        val fatRatio = if (prof.dailyFatTargetGrams > 0) (totalFat / prof.dailyFatTargetGrams).coerceIn(0f, 1f) else 0f

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(Translations.get("dashboard", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest),
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.White)
                        }
                        IconButton(onClick = { authViewModel.changeLanguage(if (lang == "en") "ru" else "en") }) {
                            Icon(Icons.Filled.Language, contentDescription = "Language", tint = Color.White)
                        }
                        IconButton(onClick = { authViewModel.logout() }) {
                            Icon(Icons.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                        }
                    }
                )
            },
            bottomBar = {
                BottomNavigationBar(
                    activeScreen = "dashboard",
                    onSelect = { tag ->
                        if (tag == "log") onNavigateToLog()
                        else if (tag == "search") onNavigateToSearch()
                    },
                    lang = lang
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = onNavigateToSearch,
                    containerColor = NutriColors.LightEmerald,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Add Food")
                }
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
                // Header card
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .background(NutriColors.LightPastelGreen, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Person, contentDescription = "Face", tint = NutriColors.DeepForest)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "${if (lang == "ru") "Привет" else "Hello"}, ${prof.name}!",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NutriColors.DeepForest
                                )
                                Text(
                                    text = "BMI Index: ${"%.1f".format(prof.bmi)} • goal: ${prof.mainGoal}",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }

                // ADVANCED TOOLS GRID WIDGETS
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text(
                            text = if (lang == "ru") "Инструменты NutriGuide AI" else "NutriGuide AI Advanced Clinics",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = NutriColors.DeepForest
                        )
                        
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            // Camera scan
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.weight(1f).clickable { onNavigateToScan() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.CameraEnhance, contentDescription = "Scan", tint = NutriColors.DeepForest, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (lang == "ru") "Сканер фото" else "Photo Vision", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NutriColors.DeepForest, textAlign = TextAlign.Center)
                                    Text(if (lang == "ru") "Groq Vision" else "Groq Vision", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            // AI recommendations
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.weight(1.3f).clickable { onNavigateToAI() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.AutoAwesome, contentDescription = "AI", tint = NutriColors.LightEmerald, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (lang == "ru") "ИИ Советы" else "Medical AI", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NutriColors.DeepForest, textAlign = TextAlign.Center)
                                    Text(if (lang == "ru") "Groq Llama" else "Groq Llama advice", fontSize = 9.sp, color = Color.Gray)
                                }
                            }

                            // Analytics
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier.weight(1f).clickable { onNavigateToAnalytics() },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.GridOn, contentDescription = "Heatmap", tint = NutriColors.AmberWarm, modifier = Modifier.size(28.dp))
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(if (lang == "ru") "Аналитика" else "Analytics", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NutriColors.DeepForest, textAlign = TextAlign.Center)
                                    Text(if (lang == "ru") "Тепловая карта" else "Heatmaps", fontSize = 9.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                // Callouts Warning list
                if (dailyLimitWarnings.isNotEmpty()) {
                    items(dailyLimitWarnings) { warning ->
                        WarningAlertBox(warning, lang)
                    }
                }

                // Progress trackers Ring
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = Translations.get("calories", lang),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium,
                                color = NutriColors.DeepForest
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(140.dp)
                            ) {
                                CircularProgressIndicator(
                                    progress = { calRatio },
                                    modifier = Modifier.fillMaxSize(),
                                    strokeWidth = 12.dp,
                                    color = NutriColors.DeepForest,
                                    trackColor = NutriColors.LightPastelGreen,
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${totalCalories.toInt()}",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Black,
                                        color = NutriColors.DeepForest
                                    )
                                    Text(
                                        text = "/ ${prof.dailyCalorieTarget.toInt()} kcal",
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            // Macros horizontal bars
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                MacroIndicatorBar(
                                    label = Translations.get("protein", lang),
                                    currentVal = totalProtein,
                                    targetVal = prof.dailyProteinTargetGrams,
                                    ratio = protRatio,
                                    color = NutriColors.AmberWarm
                                )
                                MacroIndicatorBar(
                                    label = Translations.get("carbs", lang),
                                    currentVal = totalCarbs,
                                    targetVal = prof.dailyCarbTargetGrams,
                                    ratio = carbRatio,
                                    color = NutriColors.DangerRed
                                )
                                MacroIndicatorBar(
                                    label = Translations.get("fat", lang),
                                    currentVal = totalFat,
                                    targetVal = prof.dailyFatTargetGrams,
                                    ratio = fatRatio,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                // Reports export triggers
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = Translations.get("export_report", lang),
                                fontWeight = FontWeight.Bold,
                                color = NutriColors.DeepForest
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { mainAppViewModel.exportReport(context, isCsv = true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = "CSV")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(Translations.get("export_csv", lang), fontSize = 11.sp)
                                }
                                Button(
                                    onClick = { mainAppViewModel.exportReport(context, isCsv = false) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NutriColors.LightEmerald),
                                    modifier = Modifier.weight(1.2f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Filled.PictureAsPdf, contentDescription = "PDF")
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(Translations.get("export_pdf", lang), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MacroIndicatorBar(
    label: String,
    currentVal: Float,
    targetVal: Float,
    ratio: Float,
    color: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("${currentVal.toInt()}g / ${targetVal.toInt()}g", fontSize = 12.sp, color = Color.Gray)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFFF0F0F0)
        )
    }
}

// ==========================================
// 4. FOOD LOG REGISTRY REPORT SCREEN
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLogScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToSearch: () -> Unit,
    lang: String
) {
    val loggedFoods by mainAppViewModel.loggedFoods.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("food_log", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                activeScreen = "log",
                onSelect = { tag ->
                    if (tag == "dashboard") onNavigateToDashboard()
                    else if (tag == "search") onNavigateToSearch()
                },
                lang = lang
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(16.dp)
        ) {
            if (loggedFoods.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Filled.Restaurant,
                            contentDescription = "Empty",
                            modifier = Modifier.size(60.dp),
                            tint = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(Translations.get("empty_log", lang), color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                val meals = listOf("Breakfast", "Lunch", "Dinner", "Snack")
                
                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    meals.forEach { meal ->
                        val mealItems = loggedFoods.filter { it.mealType.equals(meal, ignoreCase = true) }
                        if (mealItems.isNotEmpty()) {
                            item {
                                Text(
                                    text = meal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    color = NutriColors.DeepForest
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                            items(mealItems) { item ->
                                FoodLogItemCard(item = item, onDelete = { mainAppViewModel.deleteLog(item.id) }, lang)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FoodLogItemCard(
    item: FoodLogEntry,
    onDelete: () -> Unit,
    lang: String
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                Text(
                    text = "${item.brandOwner ?: "Generic"} • ${item.quantityGrams.toInt()}g",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("${Translations.get("calories", lang)}: ${item.calories.toInt()}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Text("P: ${item.proteinG.toInt()}g", fontSize = 11.sp)
                    Text("C: ${item.carbsG.toInt()}g", fontSize = 11.sp)
                    Text("F: ${item.fatG.toInt()}g", fontSize = 11.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = NutriColors.DangerRed)
            }
        }
    }
}

// ==========================================
// 5. USDA DATABASE SEARCH & SPECIFICATIONS
// ==========================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodSearchScreen(
    mainAppViewModel: MainAppViewModel,
    onNavigateToDashboard: () -> Unit,
    onNavigateToLog: () -> Unit,
    lang: String
) {
    val query by mainAppViewModel.searchQuery.collectAsState()
    val searchResults by mainAppViewModel.searchResults.collectAsState()
    val selectedItem by mainAppViewModel.selectedFoodItem.collectAsState()

    // Warnings
    val selectedItemWarnings by mainAppViewModel.selectedItemWarnings.collectAsState()

    var mealOptionExpanded by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var quantityInputGrams by remember { mutableStateOf("100") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(Translations.get("search_food", lang), fontWeight = FontWeight.Bold, color = Color.White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = NutriColors.DeepForest)
            )
        },
        bottomBar = {
            BottomNavigationBar(
                activeScreen = "search",
                onSelect = { tag ->
                    if (tag == "dashboard") onNavigateToDashboard()
                    else if (tag == "log") onNavigateToLog()
                },
                lang = lang
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(NutriColors.LightPastelGreen)
                .padding(16.dp)
        ) {
            // Search Input field
            OutlinedTextField(
                value = query,
                onValueChange = { mainAppViewModel.searchFood(it) },
                placeholder = { Text(if (lang == "ru") "Поиск продуктов (напр. egg, oats, pizza)" else "Search e.g. egg, bread, chicken") },
                modifier = Modifier.fillMaxWidth().testTag("food_search_input"),
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "SearchIcon") },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NutriColors.DeepForest,
                    unfocusedContainerColor = Color.White,
                    focusedContainerColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (selectedItem == null) {
                // Display list selection
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(searchResults) { food ->
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { mainAppViewModel.selectFoodItem(food) }
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(food.name, fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                                Text(
                                    text = "${food.brandOwner ?: "Generic"} • ${food.calories.toInt()} kcal / 100g",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            } else {
                val item = selectedItem!!
                // Food Details + Allergy compliance Engine UI overlay
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            // Header detail close button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(Translations.get("details_header", lang), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color.Gray)
                                IconButton(onClick = { mainAppViewModel.selectFoodItem(null) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Close")
                                }
                            }
                            Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = NutriColors.DeepForest)
                            Text(item.brandOwner ?: "Generic Reference Brand", color = Color.Gray, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Clinical Warnings checklist container inside detail
                        item {
                            Text(Translations.get("warning_title", lang), fontWeight = FontWeight.Bold, color = NutriColors.DeepForest)
                            if (selectedItemWarnings.isEmpty()) {
                                Surface(
                                    color = Color(0xFFF2FAF2),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.CheckCircle, contentDescription = "Safe", tint = Color(0xFF2E7D32))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(Translations.get("no_warnings", lang), color = Color(0xFF2E7D32), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    selectedItemWarnings.forEach { WarningRowInDetails(it, lang) }
                                }
                            }
                        }

                        // Food nutritional list values
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(if (lang == "ru") "Характеристики (на 100г)" else "Nutrients Matrix (Per 100g)", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(6.dp))
                            NutrientDetailsGrid(item, lang)
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // Quick logging fields: Meal dropdown and serving sizes input
                        item {
                            Text(Translations.get("quick_add_label", lang), fontWeight = FontWeight.Bold)
                            OutlinedTextField(
                                value = quantityInputGrams,
                                onValueChange = { quantityInputGrams = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Text(if (lang == "ru") "Прием пищи:" else "Target Meal Slot:", fontWeight = FontWeight.Bold)
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { mealOptionExpanded = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(selectedMealType, color = NutriColors.DeepForest)
                                        Icon(Icons.Filled.ArrowDropDown, contentDescription = "Drop", tint = NutriColors.DeepForest)
                                    }
                                }
                                DropdownMenu(
                                    expanded = mealOptionExpanded,
                                    onDismissRequest = { mealOptionExpanded = false },
                                    modifier = Modifier.fillMaxWidth(0.9f)
                                ) {
                                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { m ->
                                        DropdownMenuItem(
                                            text = { Text(m) },
                                            onClick = {
                                                selectedMealType = m
                                                mealOptionExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    val grams = quantityInputGrams.toFloatOrNull() ?: 100f
                                    mainAppViewModel.addFoodToLog(selectedMealType, grams)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NutriColors.DeepForest),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("add_to_log_button")
                            ) {
                                Text(Translations.get("add_item", lang), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NutrientDetailsGrid(item: UsdaFoodItem, lang: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("calories", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.calories.toInt()} kcal", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("protein", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.proteinG} g", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("carbs", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.carbsG} g", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("fat", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.fatG} g", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("sodium", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.sodiumMg} mg", fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(Translations.get("sugar", lang), color = Color.Gray, fontSize = 13.sp)
                Text("${item.sugarG} g", fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ==========================================
// CENTRAL SHARED COMPOSE PARTS
// ==========================================

@Composable
fun WarningAlertBox(warning: HealthWarning, lang: String) {
    val isDanger = warning.severity == WarningSeverity.DANGER
    val cardColor = if (isDanger) NutriColors.DangerLight else NutriColors.AmberLight
    val borderColor = if (isDanger) NutriColors.DangerRed else NutriColors.AmberWarm
    val txtCaption = if (isDanger) Translations.get("severity_danger", lang) else Translations.get("severity_warning", lang)

    Card(
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = if (isDanger) Icons.Filled.Dangerous else Icons.Filled.Warning,
                contentDescription = "Alert",
                tint = borderColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = txtCaption,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = borderColor
                )
                Text(
                    text = if (lang == "ru") warning.titleRu else warning.titleEn,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (lang == "ru") warning.descriptionRu else warning.descriptionEn,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun WarningRowInDetails(warning: HealthWarning, lang: String) {
    val isDanger = warning.severity == WarningSeverity.DANGER
    val colorAccent = if (isDanger) NutriColors.DangerRed else NutriColors.AmberWarm
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDanger) NutriColors.DangerLight else NutriColors.AmberLight, RoundedCornerShape(8.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = if (isDanger) Icons.Filled.Error else Icons.Filled.Warning,
            contentDescription = "Alert",
            tint = colorAccent,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = if (lang == "ru") warning.titleRu else warning.titleEn,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = colorAccent
            )
            Text(
                text = if (lang == "ru") warning.descriptionRu else warning.descriptionEn,
                fontSize = 11.sp,
                color = Color.DarkGray
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    activeScreen: String,
    onSelect: (String) -> Unit,
    lang: String
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = activeScreen == "dashboard",
            onClick = { onSelect("dashboard") },
            icon = { Icon(Icons.Filled.Home, contentDescription = "Dashboard") },
            label = { Text(Translations.get("dashboard", lang), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeScreen == "log",
            onClick = { onSelect("log") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Log") },
            label = { Text(Translations.get("food_log", lang), fontSize = 11.sp) }
        )
        NavigationBarItem(
            selected = activeScreen == "search",
            onClick = { onSelect("search") },
            icon = { Icon(Icons.Filled.Search, contentDescription = "Search") },
            label = { Text(Translations.get("search_food", lang), fontSize = 11.sp) }
        )
    }
}
