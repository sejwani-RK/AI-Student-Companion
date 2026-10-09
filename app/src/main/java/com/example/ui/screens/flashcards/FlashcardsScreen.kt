package com.example.ui.screens.flashcards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.FlashcardEntity
import com.example.ui.components.AcademicStatusChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardsScreen(
    courses: List<CourseEntity>,
    flashcards: List<FlashcardEntity>,
    activeCardIndex: Int,
    isCardFlipped: Boolean,
    onFlipCard: () -> Unit,
    onRateCard: (card: FlashcardEntity, rating: Int) -> Unit,
    onAddFlashcard: (courseId: Long, courseName: String, front: String, back: String, hint: String, category: String) -> Unit,
    onDeleteCard: (FlashcardEntity) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var activeMode by remember { mutableStateOf(0) } // 0: Study Session, 1: Browse Deck

    val activeCard = flashcards.getOrNull(activeCardIndex)
    val rotation by animateFloatAsState(
        targetValue = if (isCardFlipped) 180f else 0f,
        animationSpec = tween(durationMillis = 400),
        label = "cardFlipAnimation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Header
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Spaced Repetition Flashcards",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${flashcards.size} total cards across ${courses.size} courses",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.testTag("new_flashcard_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New Card")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TabRow(
                    selectedTabIndex = activeMode,
                    containerColor = Color.Transparent,
                    divider = {}
                ) {
                    Tab(
                        selected = activeMode == 0,
                        onClick = { activeMode = 0 },
                        text = { Text("Review Deck") }
                    )
                    Tab(
                        selected = activeMode == 1,
                        onClick = { activeMode = 1 },
                        text = { Text("Browse All Cards") }
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        if (activeMode == 0) {
            // INTERACTIVE FLIP CARD SESSION
            if (activeCard != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Card Indicator & Course Name
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Card ${activeCardIndex + 1} of ${flashcards.size}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        AcademicStatusChip(
                            text = activeCard.masteryLevel,
                            color = when (activeCard.masteryLevel) {
                                "Mastered" -> AcademicEmerald
                                "Reviewing" -> AcademicCyan
                                "Learning" -> AcademicAmber
                                else -> AcademicPurple
                            }
                        )
                    }

                    // 3D Animated Flip Card
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(320.dp)
                            .graphicsLayer {
                                rotationY = rotation
                                cameraDistance = 12f * density
                            }
                            .border(1.5.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(20.dp))
                            .clickable { onFlipCard() }
                            .testTag("flashcard_flip_surface")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (rotation <= 90f) {
                                // Front of card
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AcademicBluePrimary.copy(alpha = 0.1f)
                                    ) {
                                        Text(
                                            text = "${activeCard.courseName} · ${activeCard.category}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicBluePrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(20.dp))

                                    Text(
                                        text = activeCard.front,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 28.sp
                                    )

                                    if (activeCard.hint.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = "💡 Hint: ${activeCard.hint}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            textAlign = TextAlign.Center
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    Text(
                                        text = "Tap to flip and check answer",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = AcademicBlueLight
                                    )
                                }
                            } else {
                                // Back of card (Answer) - Mirror horizontally to avoid reverse text
                                Column(
                                    modifier = Modifier.graphicsLayer { rotationY = 180f },
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = AcademicEmerald.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "ANSWER & EXPLANATION",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = AcademicEmerald,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    Text(
                                        text = activeCard.back,
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Center,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        lineHeight = 24.sp
                                    )
                                }
                            }
                        }
                    }

                    // SM-2 Spaced Repetition Rating Buttons
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (isCardFlipped) "Rate your recall ease (SM-2 Spaced Repetition):" else "Flip card to reveal answer and rate",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { onRateCard(activeCard, 1) },
                                enabled = isCardFlipped,
                                colors = ButtonDefaults.buttonColors(containerColor = AcademicRose),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rate_again_btn")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Again", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("<1d", fontSize = 10.sp)
                                }
                            }

                            Button(
                                onClick = { onRateCard(activeCard, 2) },
                                enabled = isCardFlipped,
                                colors = ButtonDefaults.buttonColors(containerColor = AcademicAmber),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rate_hard_btn")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Hard", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("2d", fontSize = 10.sp)
                                }
                            }

                            Button(
                                onClick = { onRateCard(activeCard, 3) },
                                enabled = isCardFlipped,
                                colors = ButtonDefaults.buttonColors(containerColor = AcademicBlueLight),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rate_good_btn")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Good", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("4d", fontSize = 10.sp)
                                }
                            }

                            Button(
                                onClick = { onRateCard(activeCard, 4) },
                                enabled = isCardFlipped,
                                colors = ButtonDefaults.buttonColors(containerColor = AcademicEmerald),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("rate_easy_btn")
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Easy", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("7d", fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No flashcards found. Create your first card above!")
                }
            }
        } else {
            // BROWSE ALL FLASHCARDS LIST
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(flashcards) { card ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AcademicStatusChip(
                                    text = card.courseName,
                                    color = AcademicBluePrimary
                                )

                                IconButton(
                                    onClick = { onDeleteCard(card) },
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

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Q: ${card.front}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "A: ${card.back}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }

    // Add Flashcard Dialog
    if (showAddDialog) {
        AddFlashcardDialog(
            courses = courses,
            onDismiss = { showAddDialog = false },
            onConfirm = { courseId, courseName, front, back, hint, category ->
                onAddFlashcard(courseId, courseName, front, back, hint, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun AddFlashcardDialog(
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String, String, String) -> Unit
) {
    var selectedCourse by remember { mutableStateOf(courses.firstOrNull()) }
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var hint by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Core Concepts") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Flashcard") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Select Course:", style = MaterialTheme.typography.labelMedium)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(courses) { course ->
                        FilterChip(
                            selected = selectedCourse?.id == course.id,
                            onClick = { selectedCourse = course },
                            label = { Text(course.code) }
                        )
                    }
                }

                OutlinedTextField(
                    value = front,
                    onValueChange = { front = it },
                    label = { Text("Front (Question / Prompt)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = back,
                    onValueChange = { back = it },
                    label = { Text("Back (Answer / Explanation)") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = hint,
                    onValueChange = { hint = it },
                    label = { Text("Optional Hint") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedCourse?.let { c ->
                        if (front.isNotBlank() && back.isNotBlank()) {
                            onConfirm(c.id, c.name, front, back, hint, category)
                        }
                    }
                },
                enabled = front.isNotBlank() && back.isNotBlank() && selectedCourse != null
            ) {
                Text("Save Card")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
