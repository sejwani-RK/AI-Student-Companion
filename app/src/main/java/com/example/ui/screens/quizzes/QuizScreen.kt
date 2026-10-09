package com.example.ui.screens.quizzes

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.DocumentEntity
import com.example.data.local.entity.QuizAttemptEntity
import com.example.data.local.entity.QuizEntity
import com.example.ui.components.AcademicStatusChip
import com.example.ui.theme.*
import com.example.ui.viewmodel.ActiveQuizState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    quizzes: List<QuizEntity>,
    attempts: List<QuizAttemptEntity>,
    documents: List<DocumentEntity>,
    activeQuiz: ActiveQuizState?,
    isGenerating: Boolean,
    onStartQuiz: (QuizEntity) -> Unit,
    onSelectAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onExitQuiz: () -> Unit,
    onGenerateQuiz: (doc: DocumentEntity, difficulty: String, count: Int) -> Unit
) {
    var showGenerateDialog by remember { mutableStateOf(false) }

    if (activeQuiz != null) {
        // ACTIVE QUIZ TAKING SESSION
        ActiveQuizView(
            state = activeQuiz,
            onSelectAnswer = onSelectAnswer,
            onNextQuestion = onNextQuestion,
            onPreviousQuestion = onPreviousQuestion,
            onExit = onExitQuiz
        )
    } else {
        // QUIZ LIST & GENERATOR DASHBOARD
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header & Generator CTA
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
                                    text = "AI Practice Quizzes",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Generate custom multiple-choice tests grounded in your uploaded lecture notes.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }

                            Button(
                                onClick = { showGenerateDialog = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .testTag("generate_quiz_modal_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("New Quiz")
                            }
                        }
                    }
                }
            }

            // Available Practice Quizzes
            item {
                Text(
                    text = "Ready to Take (${quizzes.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(quizzes) { quiz ->
                QuizCardItem(
                    quiz = quiz,
                    onStart = { onStartQuiz(quiz) }
                )
            }

            // Quiz Attempt History
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Attempt History & Scores (${attempts.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (attempts.isEmpty()) {
                item {
                    Text(
                        text = "No attempts yet. Take a quiz above to start tracking your progress!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(attempts) { attempt ->
                    AttemptHistoryCard(attempt = attempt)
                }
            }
        }
    }

    // Modal Dialog to Generate New Quiz from Notes
    if (showGenerateDialog) {
        GenerateQuizDialog(
            documents = documents,
            isGenerating = isGenerating,
            onDismiss = { showGenerateDialog = false },
            onConfirm = { doc, difficulty, count ->
                onGenerateQuiz(doc, difficulty, count)
                showGenerateDialog = false
            }
        )
    }
}

@Composable
private fun ActiveQuizView(
    state: ActiveQuizState,
    onSelectAnswer: (Int) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onExit: () -> Unit
) {
    val total = state.questions.size
    val currentIdx = state.currentQuestionIndex
    val question = state.questions.getOrNull(currentIdx)
    val selectedAnswer = state.userAnswers[currentIdx]
    val progress = if (total > 0) (currentIdx + 1).toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Quiz Top Bar
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
                    IconButton(
                        onClick = onExit,
                        modifier = Modifier.testTag("exit_quiz_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Exit Quiz")
                    }

                    Text(
                        text = "Question ${currentIdx + 1} of $total",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    AcademicStatusChip(
                        text = state.quiz.difficulty,
                        color = when (state.quiz.difficulty) {
                            "Hard" -> AcademicRose
                            "Medium" -> AcademicAmber
                            else -> AcademicEmerald
                        }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }

        if (state.isSubmitted) {
            // QUIZ COMPLETION SUMMARY
            QuizCompletionCard(
                state = state,
                onExit = onExit
            )
        } else if (question != null) {
            // QUESTION CARD & CHOICES
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = question.question,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )
                        }
                    }
                }

                // Options list
                items(question.options.indices.toList()) { optIdx ->
                    val optText = question.options[optIdx]
                    val isChosen = selectedAnswer == optIdx
                    val isCorrect = optIdx == question.correctAnswerIndex
                    val hasAnswered = selectedAnswer != null

                    val (containerColor, borderColor, textColor) = when {
                        !hasAnswered -> if (isChosen) Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.onPrimaryContainer)
                        else Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline, MaterialTheme.colorScheme.onSurface)

                        hasAnswered && isCorrect -> Triple(AcademicEmerald.copy(alpha = 0.15f), AcademicEmerald, AcademicEmerald)
                        hasAnswered && isChosen && !isCorrect -> Triple(AcademicRose.copy(alpha = 0.15f), AcademicRose, AcademicRose)
                        else -> Triple(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = containerColor,
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                            .clickable(enabled = selectedAnswer == null) {
                                onSelectAnswer(optIdx)
                            }
                            .testTag("quiz_option_$optIdx")
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = borderColor.copy(alpha = 0.2f),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    val letter = ('A' + optIdx).toString()
                                    Text(
                                        text = letter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = borderColor
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = optText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = textColor,
                                modifier = Modifier.weight(1f)
                            )

                            if (hasAnswered && isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Correct",
                                    tint = AcademicEmerald
                                )
                            } else if (hasAnswered && isChosen && !isCorrect) {
                                Icon(
                                    imageVector = Icons.Default.Cancel,
                                    contentDescription = "Incorrect",
                                    tint = AcademicRose
                                )
                            }
                        }
                    }
                }

                // Explanation Card (Shows immediately when student answers)
                if (selectedAnswer != null) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = AcademicBlueContainer.copy(alpha = 0.4f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, AcademicBluePrimary.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = AcademicBluePrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Explanation & Grounding",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = AcademicBluePrimary
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = question.explanation,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = "Citation: ${question.sourceCitation}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AcademicBluePrimary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // Bottom Navigation Controls
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onPreviousQuestion,
                        enabled = currentIdx > 0,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Previous")
                    }

                    Button(
                        onClick = onNextQuestion,
                        enabled = selectedAnswer != null,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("next_question_button")
                    ) {
                        Text(if (currentIdx == total - 1) "Complete Quiz" else "Next Question")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuizCompletionCard(
    state: ActiveQuizState,
    onExit: () -> Unit
) {
    val total = state.questions.size
    val score = state.score
    val percentage = if (total > 0) (score * 100) / total else 0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            shape = CircleShape,
            color = if (percentage >= 70) AcademicEmerald.copy(alpha = 0.15f) else AcademicAmber.copy(alpha = 0.15f),
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (percentage >= 70) Icons.Default.EmojiEvents else Icons.Default.School,
                    contentDescription = null,
                    tint = if (percentage >= 70) AcademicEmerald else AcademicAmber,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = if (percentage >= 70) "Outstanding Performance!" else "Good Practice Effort!",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "You scored $score out of $total questions ($percentage%).",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total Questions", style = MaterialTheme.typography.labelSmall)
                    Text("$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Correct", style = MaterialTheme.typography.labelSmall)
                    Text("$score", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = AcademicEmerald)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Accuracy", style = MaterialTheme.typography.labelSmall)
                    Text("$percentage%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onExit,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("finish_quiz_exit_button")
        ) {
            Text("Back to Quizzes Dashboard")
        }
    }
}

@Composable
private fun QuizCardItem(
    quiz: QuizEntity,
    onStart: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = quiz.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AcademicStatusChip(
                        text = quiz.difficulty,
                        color = when (quiz.difficulty) {
                            "Hard" -> AcademicRose
                            "Medium" -> AcademicAmber
                            else -> AcademicEmerald
                        }
                    )
                }
            }

            Button(
                onClick = onStart,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("start_quiz_btn_${quiz.id}")
            ) {
                Text("Start")
            }
        }
    }
}

@Composable
private fun AttemptHistoryCard(attempt: QuizAttemptEntity) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attempt.quizTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Score: ${attempt.score}/${attempt.totalQuestions}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AcademicStatusChip(
                text = "${attempt.percentage}%",
                color = if (attempt.percentage >= 70) AcademicEmerald else AcademicAmber
            )
        }
    }
}

@Composable
private fun GenerateQuizDialog(
    documents: List<DocumentEntity>,
    isGenerating: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (DocumentEntity, String, Int) -> Unit
) {
    var selectedDoc by remember { mutableStateOf(documents.firstOrNull()) }
    var selectedDifficulty by remember { mutableStateOf("Medium") }
    var questionCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Generate AI Quiz from Notes") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Source Lecture Document:", style = MaterialTheme.typography.labelMedium)

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(documents) { doc ->
                        FilterChip(
                            selected = selectedDoc?.id == doc.id,
                            onClick = { selectedDoc = doc },
                            label = { Text(doc.title, maxLines = 1) }
                        )
                    }
                }

                Text("Difficulty:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Easy", "Medium", "Hard").forEach { diff ->
                        FilterChip(
                            selected = selectedDifficulty == diff,
                            onClick = { selectedDifficulty = diff },
                            label = { Text(diff) }
                        )
                    }
                }

                Text("Number of Questions:", style = MaterialTheme.typography.labelMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(3, 5, 10).forEach { cnt ->
                        FilterChip(
                            selected = questionCount == cnt,
                            onClick = { questionCount = cnt },
                            label = { Text("$cnt MCQs") }
                        )
                    }
                }

                if (isGenerating) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Formulating questions with AI...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedDoc?.let { onConfirm(it, selectedDifficulty, questionCount) }
                },
                enabled = selectedDoc != null && !isGenerating
            ) {
                Text("Generate Quiz")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
