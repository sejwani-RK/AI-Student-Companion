package com.example.ui.screens.planner

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.StudySessionEntity
import com.example.ui.components.AcademicStatusChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudyPlannerScreen(
    courses: List<CourseEntity>,
    sessions: List<StudySessionEntity>,
    isAiGenerating: Boolean,
    onToggleSession: (StudySessionEntity) -> Unit,
    onDeleteSession: (StudySessionEntity) -> Unit,
    onAddSession: (courseName: String, title: String, date: String, slot: String, duration: Int, priority: String, notes: String) -> Unit,
    onGenerateAiSchedule: (dailyHours: Int, examGoals: String) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var showAiScheduleDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("All") } // "All", "Upcoming", "Completed", "High Priority"

    val filteredSessions = remember(sessions, selectedFilter) {
        when (selectedFilter) {
            "Upcoming" -> sessions.filter { !it.completed }
            "Completed" -> sessions.filter { it.completed }
            "High Priority" -> sessions.filter { it.priority == "High" }
            else -> sessions
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Generator Header Card
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "AI Study Timetable",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Intelligent weekly scheduling tailored to your exam milestones and available hours.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Button(
                            onClick = { showAiScheduleDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .testTag("generate_schedule_button")
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Auto-Plan")
                        }
                    }
                }
            }
        }

        // Filter Pills & Manual Add Button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    val filters = listOf("All", "Upcoming", "Completed", "High Priority")
                    items(filters) { f ->
                        FilterChip(
                            selected = selectedFilter == f,
                            onClick = { selectedFilter = f },
                            label = { Text(f) }
                        )
                    }
                }

                FilledTonalIconButton(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .testTag("add_custom_session_btn")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Add Study Session")
                }
            }
        }

        // Session List
        if (filteredSessions.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(imageVector = Icons.Default.EventAvailable, contentDescription = null, tint = AcademicBlueLight, modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No study sessions found for filter '$selectedFilter'.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredSessions) { session ->
                PlannerSessionCard(
                    session = session,
                    onToggle = { onToggleSession(session) },
                    onDelete = { onDeleteSession(session) }
                )
            }
        }
    }

    // AI Schedule Generation Dialog
    if (showAiScheduleDialog) {
        AiScheduleGeneratorDialog(
            isGenerating = isAiGenerating,
            onDismiss = { showAiScheduleDialog = false },
            onGenerate = { hours, goals ->
                onGenerateAiSchedule(hours, goals)
                showAiScheduleDialog = false
            }
        )
    }

    // Manual Add Session Dialog
    if (showAddDialog) {
        AddSessionDialog(
            courses = courses,
            onDismiss = { showAddDialog = false },
            onConfirm = { course, title, date, slot, dur, priority, notes ->
                onAddSession(course, title, date, slot, dur, priority, notes)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun PlannerSessionCard(
    session: StudySessionEntity,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val priorityColor = when (session.priority) {
        "High" -> AcademicRose
        "Medium" -> AcademicAmber
        else -> AcademicEmerald
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = session.completed,
                onCheckedChange = { onToggle() },
                modifier = Modifier.testTag("planner_check_${session.id}")
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = session.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (session.completed) TextDecoration.LineThrough else TextDecoration.None
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${session.courseName} · ${session.targetDate} (${session.timeSlot})",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )

                if (session.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = session.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                AcademicStatusChip(
                    text = session.priority,
                    color = priorityColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AiScheduleGeneratorDialog(
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onGenerate: (dailyHours: Int, examGoals: String) -> Unit
) {
    var hours by remember { mutableStateOf(2) }
    var notes by remember { mutableStateOf("Midterms approaching for Data Structures & AI in 2 weeks.") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate AI Study Schedule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Daily Study Target: $hours hours", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                Slider(
                    value = hours.toFloat(),
                    onValueChange = { hours = it.toInt() },
                    valueRange = 1f..6f,
                    steps = 4
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Exam Deadlines & Priorities") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                if (isGenerating) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimizing study distribution with AI...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onGenerate(hours, notes) },
                enabled = !isGenerating
            ) {
                Text("Generate Schedule")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddSessionDialog(
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, String, String) -> Unit
) {
    var courseName by remember { mutableStateOf(courses.firstOrNull()?.name ?: "Data Structures & Algorithms") }
    var title by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("2026-10-12") }
    var slot by remember { mutableStateOf("15:00 - 16:30") }
    var duration by remember { mutableStateOf(90) }
    var priority by remember { mutableStateOf("High") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Study Session") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Topic or Study Task") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Date (YYYY-MM-DD)") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = slot,
                        onValueChange = { slot = it },
                        label = { Text("Time Slot") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("High", "Medium", "Low").forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            label = { Text(p) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) onConfirm(courseName, title, date, slot, duration, priority, notes)
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
