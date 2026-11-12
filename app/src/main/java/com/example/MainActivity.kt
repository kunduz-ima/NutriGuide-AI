package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainAppViewModel
import com.example.ui.viewmodel.OnboardingViewModel
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Initialize ViewModels using manual DI factory
                val factory = remember { ViewModelFactory() }
                val authViewModel: AuthViewModel = viewModel(factory = factory)
                val onboardingViewModel: OnboardingViewModel = viewModel(factory = factory)
                val mainAppViewModel: MainAppViewModel = viewModel(factory = factory)

                // State driven session tracker
                val emailSession by authViewModel.loggedInEmail.collectAsState()
                val profileState by mainAppViewModel.userProfile.collectAsState()
                val lang by authViewModel.appLanguage.collectAsState()

                // Navigation router state
                var currentRoute by remember { mutableStateOf("login") }

                // Synchronize navigation flow with active auth status and profile onboarding
                LaunchedEffect(emailSession, profileState) {
                    val email = emailSession
                    val profile = profileState

                    if (email == null) {
                        currentRoute = "login"
                    } else if (profile == null || !profile.isOnboarded) {
                        currentRoute = "onboarding"
                    } else {
                        // User has profile and logged in! Jump to dashboard
                        if (currentRoute == "login" || currentRoute == "onboarding") {
                            currentRoute = "dashboard"
                        }
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Crossfade(
                        targetState = currentRoute,
                        modifier = Modifier.padding(innerPadding),
                        label = "NavTransition"
                    ) { route ->
                        when (route) {
                            "login" -> {
                                LoginScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToRegister = { currentRoute = "register" },
                                    onNavigateToForgot = { currentRoute = "forgot_password" }
                                )
                            }
                            "register" -> {
                                RegisterScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToLogin = { currentRoute = "login" }
                                )
                            }
                            "forgot_password" -> {
                                ForgotPasswordScreen(
                                    authViewModel = authViewModel,
                                    onNavigateToLogin = { currentRoute = "login" }
                                )
                            }
                            "onboarding" -> {
                                emailSession?.let { email ->
                                    OnboardingScreen(
                                        onboardingViewModel = onboardingViewModel,
                                        userEmail = email,
                                        onOnboardingFinished = { currentRoute = "dashboard" },
                                        lang = lang
                                    )
                                }
                            }
                            "dashboard" -> {
                                DashboardScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    authViewModel = authViewModel,
                                    onNavigateToSearch = { currentRoute = "search" },
                                    onNavigateToLog = { currentRoute = "log" },
                                    onNavigateToSettings = { currentRoute = "settings" },
                                    onNavigateToAI = { currentRoute = "ai_recommendations" },
                                    onNavigateToAnalytics = { currentRoute = "analytics" },
                                    onNavigateToScan = { currentRoute = "food_recognition" },
                                    lang = lang
                                )
                            }
                            "log" -> {
                                FoodLogScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateToDashboard = { currentRoute = "dashboard" },
                                    onNavigateToSearch = { currentRoute = "search" },
                                    lang = lang
                                )
                            }
                            "search" -> {
                                FoodSearchScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateToDashboard = { currentRoute = "dashboard" },
                                    onNavigateToLog = { currentRoute = "log" },
                                    lang = lang
                                )
                            }
                            "settings" -> {
                                SettingsScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateBack = { currentRoute = "dashboard" },
                                    lang = lang
                                )
                            }
                            "ai_recommendations" -> {
                                AIRecommendationScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateBack = { currentRoute = "dashboard" },
                                    lang = lang
                                )
                            }
                            "analytics" -> {
                                AnalyticsScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateBack = { currentRoute = "dashboard" },
                                    lang = lang
                                )
                            }
                            "food_recognition" -> {
                                FoodScanScreen(
                                    mainAppViewModel = mainAppViewModel,
                                    onNavigateBack = { currentRoute = "dashboard" },
                                    lang = lang
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
