package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AcademicTopAppBar
import com.example.ui.screens.codingtutor.CodingTutorScreen
import com.example.ui.screens.courses.CoursesScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.flashcards.FlashcardsScreen
import com.example.ui.screens.notes.NotesAssistantScreen
import com.example.ui.screens.planner.StudyPlannerScreen
import com.example.ui.screens.progress.ProgressScreen
import com.example.ui.screens.quizzes.QuizScreen
import com.example.ui.viewmodel.StudyViewModel

enum class StudyDestination(val label: String, val icon: ImageVector) {
    DASHBOARD("Home", Icons.Default.Home),
    NOTES("AI Notes", Icons.Default.Description),
    QUIZZES("Quizzes", Icons.Default.Quiz),
    CODING_TUTOR("Tutor", Icons.Default.Code),
    PLANNER("Planner", Icons.Default.CalendarToday),
    COURSES("Courses", Icons.Default.School),
    FLASHCARDS("Cards", Icons.Default.Psychology),
    PROGRESS("Progress", Icons.Default.Insights)
}

@Composable
fun StudyMainScreen(viewModel: StudyViewModel) {
    var currentDestination by remember { mutableStateOf(StudyDestination.DASHBOARD) }
    var destinationStack by remember { mutableStateOf(listOf(StudyDestination.DASHBOARD)) }
    var showProfileModal by remember { mutableStateOf(false) }

    val courses by viewModel.courses.collectAsStateWithLifecycle()
    val documents by viewModel.documents.collectAsStateWithLifecycle()
    val quizzes by viewModel.quizzes.collectAsStateWithLifecycle()
    val quizAttempts by viewModel.quizAttempts.collectAsStateWithLifecycle()
    val studySessions by viewModel.studySessions.collectAsStateWithLifecycle()
    val flashcards by viewModel.flashcards.collectAsStateWithLifecycle()
    val profile by viewModel.userProfile.collectAsStateWithLifecycle()

    val selectedDocument by viewModel.selectedDocument.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val isAiThinking by viewModel.isAiThinking.collectAsStateWithLifecycle()
    val messageInput by viewModel.userMessageInput.collectAsStateWithLifecycle()
    val codingTutorState by viewModel.codingTutorState.collectAsStateWithLifecycle()
    val activeQuiz by viewModel.activeQuiz.collectAsStateWithLifecycle()
    val activeCardIndex by viewModel.activeCardIndex.collectAsStateWithLifecycle()
    val isCardFlipped by viewModel.isCardFlipped.collectAsStateWithLifecycle()
    val snackMessage by viewModel.snackMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMessage) {
        snackMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSnackMessage()
        }
    }

    fun navigateTo(dest: StudyDestination) {
        if (currentDestination != dest) {
            destinationStack = destinationStack + dest
            currentDestination = dest
        }
    }

    // Handle Android system back gesture
    BackHandler(enabled = destinationStack.size > 1) {
        val updated = destinationStack.dropLast(1)
        destinationStack = updated
        currentDestination = updated.lastOrNull() ?: StudyDestination.DASHBOARD
    }

    Scaffold(
        topBar = {
            AcademicTopAppBar(
                title = when (currentDestination) {
                    StudyDestination.DASHBOARD -> "AI Student Companion"
                    StudyDestination.NOTES -> "AI Notes Assistant"
                    StudyDestination.QUIZZES -> "Practice Quizzes & MCQs"
                    StudyDestination.CODING_TUTOR -> "Programming Tutor"
                    StudyDestination.PLANNER -> "Study Planner"
                    StudyDestination.COURSES -> "My Courses"
                    StudyDestination.FLASHCARDS -> "Spaced Repetition"
                    StudyDestination.PROGRESS -> "Learning Analytics"
                },
                subtitle = when (currentDestination) {
                    StudyDestination.DASHBOARD -> "Fall 2026 Academic Dashboard"
                    StudyDestination.NOTES -> "Grounded Lecture Q&A"
                    StudyDestination.QUIZZES -> "Knowledge Testing"
                    StudyDestination.CODING_TUTOR -> "Step-by-Step Logic"
                    StudyDestination.PLANNER -> "Weekly Timetable"
                    StudyDestination.COURSES -> "Course & Credit Management"
                    StudyDestination.FLASHCARDS -> "Active Recall (SM-2)"
                    StudyDestination.PROGRESS -> "Performance & Goals"
                },
                streakDays = profile?.streakDays ?: 6,
                onProfileClick = { showProfileModal = true },
                actions = {
                    // Quick jump icon for Courses & Flashcards
                    IconButton(
                        onClick = { navigateTo(StudyDestination.COURSES) },
                        modifier = Modifier.testTag("top_bar_courses_icon")
                    ) {
                        Icon(imageVector = Icons.Default.School, contentDescription = "Courses")
                    }
                    IconButton(
                        onClick = { navigateTo(StudyDestination.PROGRESS) },
                        modifier = Modifier.testTag("top_bar_progress_icon")
                    ) {
                        Icon(imageVector = Icons.Default.Insights, contentDescription = "Progress")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val primaryNavItems = listOf(
                    StudyDestination.DASHBOARD,
                    StudyDestination.NOTES,
                    StudyDestination.QUIZZES,
                    StudyDestination.CODING_TUTOR,
                    StudyDestination.PLANNER
                )

                primaryNavItems.forEach { dest ->
                    val isSelected = currentDestination == dest
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { navigateTo(dest) },
                        icon = {
                            Icon(
                                imageVector = dest.icon,
                                contentDescription = dest.label
                            )
                        },
                        label = {
                            Text(
                                text = dest.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("nav_item_${dest.name.lowercase()}")
                    )
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                StudyDestination.DASHBOARD -> {
                    DashboardScreen(
                        courses = courses,
                        documents = documents,
                        studySessions = studySessions,
                        quizAttempts = quizAttempts,
                        profile = profile,
                        onNavigateToNotes = { navigateTo(StudyDestination.NOTES) },
                        onNavigateToQuizzes = { navigateTo(StudyDestination.QUIZZES) },
                        onNavigateToCodingTutor = { navigateTo(StudyDestination.CODING_TUTOR) },
                        onNavigateToPlanner = { navigateTo(StudyDestination.PLANNER) },
                        onNavigateToFlashcards = { navigateTo(StudyDestination.FLASHCARDS) },
                        onNavigateToCourses = { navigateTo(StudyDestination.COURSES) },
                        onToggleSession = { viewModel.toggleSessionComplete(it) }
                    )
                }

                StudyDestination.NOTES -> {
                    NotesAssistantScreen(
                        courses = courses,
                        documents = documents,
                        selectedDocument = selectedDocument,
                        chatMessages = chatMessages,
                        isAiThinking = isAiThinking,
                        messageInput = messageInput,
                        onDocumentSelect = { viewModel.selectDocument(it) },
                        onMessageInputChange = { viewModel.updateMessageInput(it) },
                        onSendMessage = { viewModel.askNoteQuestion(it) },
                        onUploadNote = { courseId, title, content, type ->
                            viewModel.uploadNote(courseId, title, content, type)
                        },
                        onGenerateQuizFromDoc = { doc ->
                            viewModel.generateQuizForDocument(doc, "Medium", 3)
                            navigateTo(StudyDestination.QUIZZES)
                        },
                        onDeleteDocument = { viewModel.deleteDocument(it) }
                    )
                }

                StudyDestination.QUIZZES -> {
                    QuizScreen(
                        quizzes = quizzes,
                        attempts = quizAttempts,
                        documents = documents,
                        activeQuiz = activeQuiz,
                        isGenerating = isAiThinking,
                        onStartQuiz = { viewModel.startQuiz(it) },
                        onSelectAnswer = { viewModel.selectQuizAnswer(it) },
                        onNextQuestion = { viewModel.nextQuizQuestion() },
                        onPreviousQuestion = { viewModel.previousQuizQuestion() },
                        onExitQuiz = { viewModel.exitQuiz() },
                        onGenerateQuiz = { doc, diff, count ->
                            viewModel.generateQuizForDocument(doc, diff, count)
                        }
                    )
                }

                StudyDestination.CODING_TUTOR -> {
                    CodingTutorScreen(
                        state = codingTutorState,
                        onLanguageChange = { viewModel.setCodingLanguage(it) },
                        onInputChange = { viewModel.updateCodingInput(it) },
                        onRunTutor = { viewModel.runCodingTutor(it) },
                        onRevealNextHint = { viewModel.revealNextHint() }
                    )
                }

                StudyDestination.PLANNER -> {
                    StudyPlannerScreen(
                        courses = courses,
                        sessions = studySessions,
                        isAiGenerating = isAiThinking,
                        onToggleSession = { viewModel.toggleSessionComplete(it) },
                        onDeleteSession = { viewModel.deleteSession(it) },
                        onAddSession = { c, t, d, s, dur, p, n ->
                            viewModel.addStudySession(c, t, d, s, dur, p, n)
                        },
                        onGenerateAiSchedule = { h, g ->
                            viewModel.generateAiWeeklySchedule(h, g)
                        }
                    )
                }

                StudyDestination.COURSES -> {
                    CoursesScreen(
                        courses = courses,
                        onCreateCourse = { name, code, col, sem, inst, cr ->
                            viewModel.createCourse(name, code, col, sem, inst, cr)
                        },
                        onDeleteCourse = { viewModel.deleteCourse(it) }
                    )
                }

                StudyDestination.FLASHCARDS -> {
                    FlashcardsScreen(
                        courses = courses,
                        flashcards = flashcards,
                        activeCardIndex = activeCardIndex,
                        isCardFlipped = isCardFlipped,
                        onFlipCard = { viewModel.flipCard() },
                        onRateCard = { card, rating ->
                            viewModel.rateFlashcard(card, rating)
                        },
                        onAddFlashcard = { cId, cName, f, b, h, cat ->
                            viewModel.addFlashcard(cId, cName, f, b, h, cat)
                        },
                        onDeleteCard = { viewModel.deleteFlashcard(it) }
                    )
                }

                StudyDestination.PROGRESS -> {
                    ProgressScreen(
                        profile = profile,
                        courses = courses,
                        quizAttempts = quizAttempts,
                        flashcards = flashcards,
                        onPracticeWeakTopics = { navigateTo(StudyDestination.QUIZZES) }
                    )
                }
            }
        }
    }

    // Profile Details Modal
    if (showProfileModal) {
        AlertDialog(
            onDismissRequest = { showProfileModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Student Study Profile")
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Name: ${profile?.displayName ?: "Alex Rivera"}", fontWeight = FontWeight.SemiBold)
                    Text("Email: ${profile?.email ?: "alex.rivera@university.edu"}")
                    Text("University: ${profile?.university ?: "State University of Technology"}")
                    Text("Major: ${profile?.major ?: "Computer Science & Engineering"}")
                    Text("Current Semester: ${profile?.semester ?: "Fall 2026"}")
                    Text("Daily Study Target: ${profile?.dailyGoalMinutes ?: 90} minutes")
                    Text("Active Streak: ${profile?.streakDays ?: 6} days 🔥")
                    Text("Total Accumulated Time: ${(profile?.totalStudyMinutes ?: 1380) / 60} hours")
                }
            },
            confirmButton = {
                Button(onClick = { showProfileModal = false }) {
                    Text("Close")
                }
            }
        )
    }
}
