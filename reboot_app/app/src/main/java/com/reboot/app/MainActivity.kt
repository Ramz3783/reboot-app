package com.reboot.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.reboot.app.data.model.UserProfile
import com.reboot.app.data.model.WorkoutCatalog
import com.reboot.app.data.repository.RebootRepository
import com.reboot.app.navigation.Routes
import com.reboot.app.ui.screens.achievements.AchievementsScreen
import com.reboot.app.ui.screens.auth.LoginScreen
import com.reboot.app.ui.screens.auth.RegisterScreen
import com.reboot.app.ui.screens.createtask.CreateTaskScreen
import com.reboot.app.ui.screens.focus.FocusScreen
import com.reboot.app.ui.screens.habits.HabitsScreen
import com.reboot.app.ui.screens.home.HomeScreen
import com.reboot.app.ui.screens.mentor.MentorScreen
import com.reboot.app.ui.screens.onboarding.AiThinkingScreen
import com.reboot.app.ui.screens.onboarding.OnboardingGoalsScreen
import com.reboot.app.ui.screens.onboarding.OnboardingProblemsScreen
import com.reboot.app.ui.screens.onboarding.OnboardingProfileScreen
import com.reboot.app.ui.screens.plans.PlansScreen
import com.reboot.app.ui.screens.profile.ProfileScreen
import com.reboot.app.ui.screens.progress.ProgressScreen
import com.reboot.app.ui.screens.pro.ProScreen
import com.reboot.app.ui.screens.settings.SettingsScreen
import com.reboot.app.ui.screens.splash.SplashScreen
import com.reboot.app.ui.screens.voice.VoiceScreen
import com.reboot.app.ui.screens.workout.WorkoutScreen
import com.reboot.app.ui.theme.RebootBottomBar
import com.reboot.app.ui.theme.RebootTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val repository = (application as RebootApp).repository
        setContent {
            RebootTheme {
                RebootNavGraph(repository)
            }
        }
    }
}

private val mainTabRoutes = setOf(Routes.HOME, Routes.PLANS, Routes.MENTOR, Routes.PROFILE)

@Composable
fun RebootNavGraph(repository: RebootRepository) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val profile by repository.userProfile.collectAsState(initial = UserProfile())
    val tasks by repository.tasks.collectAsState(initial = emptyList())
    val habits by repository.habits.collectAsState(initial = emptyList())
    val plans by repository.plans.collectAsState(initial = emptyList())
    val achievements by repository.achievements.collectAsState(initial = emptyList())
    val model by repository.groqModel.collectAsState(initial = "llama-3.3-70b-versatile")
    val notifications by repository.notificationsEnabled.collectAsState(initial = true)
    val silentMode by repository.silentMode.collectAsState(initial = false)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.SPLASH
    val showBottomBar = currentRoute in mainTabRoutes

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                RebootBottomBar(
                    currentRoute = currentRoute,
                    onTabSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(Routes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    },
                    onFabClick = { navController.navigate(Routes.CREATE_TASK) }
                )
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            NavHost(navController = navController, startDestination = Routes.SPLASH) {

                composable(Routes.SPLASH) {
                    SplashScreen(
                        onCheckStatus = {
                            repository.runDailyMaintenance()
                            val p = repository.getUserProfileOnce()
                            p.isLoggedIn to p.isOnboarded
                        },
                        onNavigate = { loggedIn, onboarded ->
                            val dest = when {
                                !loggedIn -> Routes.LOGIN
                                !onboarded -> Routes.ONBOARD_PROBLEMS
                                else -> Routes.HOME
                            }
                            navController.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
                        }
                    )
                }

                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLogin = { email ->
                            scope.launch {
                                repository.updateProfile { it.copy(isLoggedIn = true, email = email) }
                                val onboarded = repository.getUserProfileOnce().isOnboarded
                                navController.navigate(if (onboarded) Routes.HOME else Routes.ONBOARD_PROBLEMS) {
                                    popUpTo(Routes.LOGIN) { inclusive = true }
                                }
                            }
                        },
                        onGoRegister = { navController.navigate(Routes.REGISTER) }
                    )
                }

                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegister = { name, email ->
                            scope.launch {
                                repository.updateProfile { it.copy(isLoggedIn = true, name = name, email = email) }
                                navController.navigate(Routes.ONBOARD_PROBLEMS) { popUpTo(Routes.REGISTER) { inclusive = true } }
                            }
                        },
                        onGoLogin = { navController.navigate(Routes.LOGIN) }
                    )
                }

                composable(Routes.ONBOARD_PROBLEMS) {
                    OnboardingProblemsScreen(initialSelected = profile.problems) { selected ->
                        scope.launch {
                            repository.updateProfile { it.copy(problems = selected) }
                            navController.navigate(Routes.ONBOARD_GOALS)
                        }
                    }
                }

                composable(Routes.ONBOARD_GOALS) {
                    OnboardingGoalsScreen(initialSelected = profile.goals) { selected ->
                        scope.launch {
                            repository.updateProfile { it.copy(goals = selected) }
                            navController.navigate(Routes.ONBOARD_THINKING)
                        }
                    }
                }

                composable(Routes.ONBOARD_THINKING) {
                    AiThinkingScreen(
                        onDone = {
                            scope.launch {
                                val p = repository.getUserProfileOnce()
                                repository.seedFromOnboarding(p.problems, p.goals)
                                navController.navigate(Routes.ONBOARD_PROFILE) {
                                    popUpTo(Routes.ONBOARD_THINKING) { inclusive = true }
                                }
                            }
                        }
                    )
                }

                composable(Routes.ONBOARD_PROFILE) {
                    OnboardingProfileScreen(initialName = profile.name) { name, birth, height, weight ->
                        scope.launch {
                            repository.updateProfile {
                                it.copy(
                                    name = name, birthDate = birth, heightCm = height,
                                    weightKg = weight, isOnboarded = true
                                )
                            }
                            navController.navigate(Routes.HOME) { popUpTo(Routes.ONBOARD_PROBLEMS) { inclusive = true } }
                        }
                    }
                }

                composable(Routes.HOME) {
                    HomeScreen(
                        profile = profile,
                        tasks = tasks,
                        onToggleTask = { id -> scope.launch { repository.toggleTask(id) } },
                        onOpenWorkout = { workoutId -> navController.navigate(Routes.workoutRoute(workoutId)) },
                        onSeeAllTasks = { }
                    )
                }

                composable(Routes.PLANS) {
                    PlansScreen(
                        plans = plans,
                        onApplyTemplate = { template -> scope.launch { repository.applyTemplate(template) } },
                        onCreatePlan = { navController.navigate(Routes.CREATE_TASK) }
                    )
                }

                composable(Routes.MENTOR) {
                    MentorScreen(
                        model = model,
                        onOpenVoice = { navController.navigate(Routes.VOICE) },
                        getHistory = { mode -> repository.chatHistory(mode) },
                        onSendMessage = { mode, msg -> repository.appendChatMessage(mode, msg) }
                    )
                }

                composable(Routes.VOICE) {
                    VoiceScreen(model = model, onClose = { navController.popBackStack() })
                }

                composable(Routes.PROGRESS) {
                    ProgressScreen(profile = profile)
                }

                composable(Routes.PROFILE) {
                    ProfileScreen(
                        profile = profile,
                        onNavigate = { route -> navController.navigate(route) },
                        onLogout = {
                            scope.launch {
                                repository.logout()
                                navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } }
                            }
                        }
                    )
                }

                composable(Routes.ACHIEVEMENTS) {
                    AchievementsScreen(achievements = achievements, onBack = { navController.popBackStack() })
                }

                composable(Routes.HABITS) {
                    HabitsScreen(
                        habits = habits,
                        onToggle = { id -> scope.launch { repository.toggleHabit(id) } },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.CREATE_TASK) {
                    CreateTaskScreen(
                        onBack = { navController.popBackStack() },
                        onCreate = { task -> scope.launch { repository.addTask(task) } }
                    )
                }

                composable(Routes.FOCUS) {
                    FocusScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    route = Routes.WORKOUT,
                    arguments = listOf(navArgument("workoutId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val workoutId = backStackEntry.arguments?.getString("workoutId")
                    val workout = WorkoutCatalog.forId(workoutId)
                    if (workout != null) {
                        WorkoutScreen(
                            workout = workout,
                            onBack = { navController.popBackStack() },
                            onCompleted = {
                                scope.launch {
                                    val matchingTask = tasks.firstOrNull { it.workoutId == workoutId && !it.done }
                                    if (matchingTask != null) repository.toggleTask(matchingTask.id)
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }

                composable(Routes.SETTINGS) {
                    SettingsScreen(
                        currentModel = model,
                        notificationsEnabled = notifications,
                        silentMode = silentMode,
                        onBack = { navController.popBackStack() },
                        onSaveModel = { m -> scope.launch { repository.setGroqModel(m) } },
                        onToggleNotifications = { v -> scope.launch { repository.setNotifications(v) } },
                        onToggleSilent = { v -> scope.launch { repository.setSilentMode(v) } }
                    )
                }

                composable(Routes.PRO) {
                    ProScreen(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}
