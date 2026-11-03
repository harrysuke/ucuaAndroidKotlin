package com.ucua.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ucua.app.core.di.AppContainer
import com.ucua.app.core.model.FormType
import com.ucua.app.core.repository.AuthRepository
import com.ucua.app.core.util.ReportsFilterRequest
import com.ucua.app.ui.auth.ForgotPasswordScreen
import com.ucua.app.ui.auth.ForgotViewModel
import com.ucua.app.ui.auth.LoginScreen
import com.ucua.app.ui.auth.LoginViewModel
import com.ucua.app.ui.auth.RegisterScreen
import com.ucua.app.ui.auth.RegisterViewModel
import com.ucua.app.ui.dashboard.DashboardScreen
import com.ucua.app.ui.dashboard.DashboardViewModel
import com.ucua.app.ui.profile.ProfileScreen
import com.ucua.app.ui.report.ReportFormScreen
import com.ucua.app.ui.report.ReportFormViewModel
import com.ucua.app.ui.report.ReportTypeScreen
import com.ucua.app.ui.reports.ReportDetailScreen
import com.ucua.app.ui.reports.ReportDetailViewModel
import com.ucua.app.ui.reports.ReportsScreen
import com.ucua.app.ui.reports.ReportsViewModel
import com.ucua.app.ui.theme.UcuaPrimary
import kotlinx.coroutines.launch

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT = "forgot"

    const val DASHBOARD = "dashboard"
    const val REPORTS = "reports"
    const val REPORT_TYPE = "report_type"
    const val PROFILE = "profile"

    const val REPORT_FORM = "report_form/{type}"
    const val EDIT_REPORT = "edit_report/{id}"
    const val REPORT_DETAIL = "report_detail/{id}"

    fun reportForm(type: String): String = "report_form/$type"
    fun editReport(id: Long): String = "edit_report/$id"
    fun reportDetail(id: Long): String = "report_detail/$id"
}

private data class BottomDestination(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

private val bottomDestinations = listOf(
    BottomDestination(Routes.DASHBOARD, "Home", Icons.Default.Home),
    BottomDestination(Routes.REPORTS, "Reports", Icons.Default.Assignment),
    BottomDestination(Routes.REPORT_TYPE, "Report", Icons.Default.AddCircle),
    BottomDestination(Routes.PROFILE, "Profile", Icons.Default.Person),
)

@Composable
fun UcuaNavHost(
    authRepository: AuthRepository,
    container: AppContainer,
    modifier: Modifier = Modifier,
) {
    val loggedIn by authRepository.isLoggedIn.collectAsState(initial = null)

    if (loggedIn == null) {
        SplashScreen(modifier)
        return
    }

    val navController = rememberNavController()
    val scope = rememberCoroutineScope()
    val currentUser by authRepository.currentUser.collectAsState(initial = null)

    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != null &&
        bottomDestinations.any { it.route == currentRoute }

    val logout: () -> Unit = {
        scope.launch {
            authRepository.logout()
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 3.dp,
                ) {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute == destination.route,
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(Routes.DASHBOARD) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = destination.icon,
                                    contentDescription = destination.label,
                                )
                            },
                            label = { Text(destination.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = UcuaPrimary,
                                selectedTextColor = UcuaPrimary,
                                indicatorColor = UcuaPrimary.copy(alpha = 0.12f),
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (loggedIn == true) Routes.DASHBOARD else Routes.LOGIN,
            modifier = Modifier.padding(padding),
        ) {
            // --- Authentication ------------------------------------------
            composable(Routes.LOGIN) {
                val vm: LoginViewModel = viewModel(factory = viewModelFactory {
                    initializer { LoginViewModel(authRepository) }
                })
                LoginScreen(
                    viewModel = vm,
                    onLoggedIn = {
                        navController.navigate(Routes.DASHBOARD) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                    },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) },
                    onForgotClick = { navController.navigate(Routes.FORGOT) },
                )
            }

            composable(Routes.REGISTER) {
                val vm: RegisterViewModel = viewModel(factory = viewModelFactory {
                    initializer { RegisterViewModel(authRepository) }
                })
                RegisterScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onGoToLogin = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER) { inclusive = true }
                        }
                    },
                )
            }

            composable(Routes.FORGOT) {
                val vm: ForgotViewModel = viewModel(factory = viewModelFactory {
                    initializer { ForgotViewModel(authRepository) }
                })
                ForgotPasswordScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                )
            }

            // --- Dashboard ------------------------------------------------
            composable(Routes.DASHBOARD) {
                val vm: DashboardViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        DashboardViewModel(authRepository, container.dashboardRepository)
                    }
                })
                DashboardScreen(
                    viewModel = vm,
                    onReportUc = { navController.navigate(Routes.reportForm("UC")) },
                    onReportUa = { navController.navigate(Routes.reportForm("UA")) },
                    onViewReports = { status ->
                        ReportsFilterRequest.pendingStatus = status
                        navController.navigate(Routes.REPORTS) { launchSingleTop = true }
                    },
                    onProfile = { navController.navigate(Routes.PROFILE) },
                )
            }

            // --- Reports list ---------------------------------------------
            composable(Routes.REPORTS) {
                val vm: ReportsViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        ReportsViewModel(container.reportRepository, container.lookupsRepository)
                    }
                })
                ReportsScreen(
                    viewModel = vm,
                    onReportClick = { id -> navController.navigate(Routes.reportDetail(id)) },
                    onCreateClick = { navController.navigate(Routes.REPORT_TYPE) },
                )
            }

            // --- Report type chooser --------------------------------------
            composable(Routes.REPORT_TYPE) {
                ReportTypeScreen(
                    onReportUc = { navController.navigate(Routes.reportForm("UC")) },
                    onReportUa = { navController.navigate(Routes.reportForm("UA")) },
                )
            }

            // --- Profile ---------------------------------------------------
            composable(Routes.PROFILE) {
                ProfileScreen(
                    user = currentUser,
                    onLogout = logout,
                )
            }

            // --- Create report --------------------------------------------
            composable(
                route = Routes.REPORT_FORM,
                arguments = listOf(navArgument("type") { type = NavType.StringType }),
            ) { entry ->
                val typeArg = entry.arguments?.getString("type") ?: "UC"
                val formType = if (typeArg == "UA") FormType.UA else FormType.UC
                val vm: ReportFormViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        ReportFormViewModel(
                            reportRepository = container.reportRepository,
                            lookupsRepository = container.lookupsRepository,
                            formType = formType,
                        )
                    }
                })
                ReportFormScreen(
                    viewModel = vm,
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    onDone = { navController.backToDashboard() },
                    onViewReport = { id -> navController.openReportFromForm(id) },
                )
            }

            // --- Edit report ----------------------------------------------
            composable(
                route = Routes.EDIT_REPORT,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                val vm: ReportFormViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        ReportFormViewModel(
                            reportRepository = container.reportRepository,
                            lookupsRepository = container.lookupsRepository,
                            formType = FormType.UC,
                            editReportId = id,
                        )
                    }
                })
                ReportFormScreen(
                    viewModel = vm,
                    user = currentUser,
                    onBack = { navController.popBackStack() },
                    onDone = {
                        navController.navigate(Routes.reportDetail(id)) {
                            popUpTo(Routes.REPORT_DETAIL) { inclusive = true }
                        }
                    },
                    onViewReport = { reportId ->
                        navController.navigate(Routes.reportDetail(reportId)) {
                            popUpTo(Routes.REPORT_DETAIL) { inclusive = true }
                        }
                    },
                )
            }

            // --- Report detail --------------------------------------------
            composable(
                route = Routes.REPORT_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                val vm: ReportDetailViewModel = viewModel(factory = viewModelFactory {
                    initializer {
                        ReportDetailViewModel(
                            reportRepository = container.reportRepository,
                            lookupsRepository = container.lookupsRepository,
                            reportId = id,
                        )
                    }
                })
                ReportDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() },
                    onEdit = { reportId -> navController.navigate(Routes.editReport(reportId)) },
                    onDeleted = { navController.backToReports() },
                )
            }
        }
    }
}

private fun NavHostController.backToDashboard() {
    navigate(Routes.DASHBOARD) {
        popUpTo(Routes.DASHBOARD) { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavHostController.backToReports() {
    navigate(Routes.REPORTS) {
        popUpTo(Routes.DASHBOARD) { inclusive = false }
        launchSingleTop = true
    }
}

private fun NavHostController.openReportFromForm(id: Long) {
    navigate(Routes.reportDetail(id)) {
        popUpTo(Routes.DASHBOARD) { inclusive = false }
    }
}

@Composable
private fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(UcuaPrimary, RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.HealthAndSafety,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(34.dp),
                )
            }
            Text(
                text = "U-SEE U-ACT",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 14.dp),
            )
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(top = 18.dp)
                    .size(22.dp),
                strokeWidth = 2.dp,
            )
        }
    }
}
