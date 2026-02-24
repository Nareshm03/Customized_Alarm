package com.example.teacherscheduler.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.teacherscheduler.model.Notice
import com.example.teacherscheduler.ui.compose.components.*
import com.example.teacherscheduler.ui.theme.*
import com.example.teacherscheduler.viewmodel.NoticeBoardViewModel
import com.example.teacherscheduler.viewmodel.NoticeWithSeenCount
import com.example.teacherscheduler.viewmodel.UserViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NoticeBoardScreen(
    noticeBoardViewModel: NoticeBoardViewModel = hiltViewModel(),
    userViewModel: UserViewModel = hiltViewModel(),
    onCreateNotice: () -> Unit = {}
) {
    val notices by noticeBoardViewModel.notices.collectAsState()
    val isHOD by userViewModel.isHOD.collectAsState()
    val userState by userViewModel.globalUserState.collectAsState()

    LaunchedEffect(userState.department, isHOD) {
        if (userState.department.isNotEmpty()) {
            noticeBoardViewModel.loadNotices(userState.department, isHOD)
        }
    }

    Scaffold(
        containerColor = Color(0xFFF5F6FA),
        floatingActionButton = {
            if (isHOD) {
                FloatingActionButton(
                    onClick = onCreateNotice,
                    containerColor = SoftUIColors.AccentPeach,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Create Notice")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(
                start = 24.dp,
                end = 24.dp,
                top = 32.dp,
                bottom = 100.dp
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Notice Board",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextPrimary
                    )
                    if (isHOD) {
                        SoftChip(
                            text = "HOD",
                            selected = true,
                            selectedBackgroundColor = SoftUIColors.AccentPeach,
                            selectedTextColor = Color.White
                        )
                    }
                }
            }

            if (notices.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        SoftEmptyStateCard(
                            icon = Icons.Outlined.Notifications,
                            title = "No Notices",
                            subtitle = if (isHOD) "Create your first notice" else "No notices published yet"
                        )
                    }
                }
            } else {
                items(notices) { notice ->
                    NoticeCard(
                        notice = notice,
                        isHOD = isHOD,
                        onNoticeViewed = {
                            noticeBoardViewModel.markAsSeen(notice.id, userState.uid)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoticeCard(
    notice: NoticeWithSeenCount,
    isHOD: Boolean,
    onNoticeViewed: () -> Unit
) {
    LaunchedEffect(Unit) {
        if (!isHOD) {
            onNoticeViewed()
        }
    }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        PremiumCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = notice.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
                
                Text(
                    text = notice.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TextSecondary
                        )
                        Text(
                            text = formatDate(notice.createdAt),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }

                    if (isHOD) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = TextSecondary
                            )
                            Text(
                                text = "${notice.seenCount} seen",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = OutlineLight
        )
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
    return format.format(Date(timestamp))
}
