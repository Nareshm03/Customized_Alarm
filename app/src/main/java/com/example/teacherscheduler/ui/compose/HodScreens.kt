package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.HodDashboardViewModel
import com.example.teacherscheduler.viewmodel.UserViewModel
import com.example.teacherscheduler.viewmodel.TeacherListViewModel
import com.example.teacherscheduler.viewmodel.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HodDashboardScreen(
    onNavigateToProfile: () -> Unit,
    onNavigateToSettings: () -> Unit,
    hodViewModel: HodDashboardViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val stats by hodViewModel.stats.collectAsState()
    val userState by userViewModel.globalUserState.collectAsState()

    LaunchedEffect(userState.department) {
        if (userState.department.isNotEmpty()) {
            hodViewModel.loadDashboardStats(userState.department)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .verticalScroll(rememberScrollState())
    ) {
        SoftTopAppBar(
            title = "HOD Dashboard",
            actions = {
                IconButton(onClick = onNavigateToSettings) {
                    Icon(Icons.Outlined.Settings, contentDescription = "Settings", tint = TextPrimary)
                }
                IconButton(onClick = onNavigateToProfile) {
                    Icon(Icons.Outlined.Person, contentDescription = "Profile", tint = TextPrimary)
                }
            }
        )

        Column(
            modifier = Modifier.padding(AppSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome, ${userState.name}",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )

            Text(
                text = "Department: ${userState.department}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.People,
                    title = "Teachers",
                    value = stats.totalTeachers.toString(),
                    color = SoftUIColors.AccentLavender
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Assignment,
                    title = "Pending",
                    value = stats.pendingTasks.toString(),
                    color = SoftUIColors.AccentPeach
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.Notifications,
                    title = "Notices",
                    value = stats.noticesPublished.toString(),
                    color = SoftUIColors.AccentMint
                )
                StatCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Outlined.School,
                    title = "Classes Today",
                    value = stats.classesToday.toString(),
                    color = Color(0xFF9C27B0)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    value: String,
    color: Color
) {
    PremiumCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = TextPrimary
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AssignTaskScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SoftEmptyStateCard(
            icon = Icons.Outlined.Assignment,
            title = "Assign Task",
            subtitle = "Assign tasks to teachers"
        )
    }
}

@Composable
fun DepartmentOverviewScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        SoftEmptyStateCard(
            icon = Icons.Outlined.Business,
            title = "Department Overview",
            subtitle = "Department statistics and info"
        )
    }
}

@Composable
fun TeachersListScreen(
    teacherListViewModel: TeacherListViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel()
) {
    val teachers by teacherListViewModel.teachers.collectAsState()
    val userState by userViewModel.globalUserState.collectAsState()

    LaunchedEffect(userState.department) {
        if (userState.department.isNotEmpty()) {
            teacherListViewModel.loadTeachers(userState.department)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
    ) {
        SoftTopAppBar(title = "Teachers")

        if (teachers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SoftEmptyStateCard(
                    icon = Icons.Outlined.People,
                    title = "No Teachers",
                    subtitle = "No teachers found in your department"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(AppSpacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teachers) { teacher ->
                    TeacherCard(
                        teacher = teacher,
                        onAssignTask = { /* TODO: Navigate to assign task */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun TeacherCard(
    teacher: Teacher,
    onAssignTask: () -> Unit
) {
    PremiumCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = TextPrimary
                )
                Text(
                    text = teacher.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Text(
                    text = "ID: ${teacher.teacherId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Button(
                onClick = onAssignTask,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SoftUIColors.AccentPeach
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Assignment,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Assign")
            }
        }
    }
}
