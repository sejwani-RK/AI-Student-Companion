package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.entity.*
import com.example.data.remote.AcademicAiService
import com.example.data.remote.CodeTutorMode
import com.example.data.remote.CodeTutorResponse
import com.example.data.remote.QuizQuestionModel
import com.example.data.repository.StudyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray

data class CodingTutorUiState(
    val language: String = "Python",
    val inputCode: String = "def two_sum(nums, target):\n    # Find indices of two numbers that add to target\n    pass",
    val mode: CodeTutorMode = CodeTutorMode.EXPLAIN,
    val isLoading: Boolean = false,
    val response: CodeTutorResponse? = null,
    val revealedHintIndex: Int = 0
)

data class ActiveQuizState(
    val quiz: QuizEntity,
    val questions: List<QuizQuestionModel>,
    val currentQuestionIndex: Int = 0,
    val userAnswers: Map<Int, Int> = emptyMap(), // questionIndex -> selectedOptionIndex
    val isSubmitted: Boolean = false,
    val score: Int = 0,
    val showExplanation: Boolean = false
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class StudyViewModel(private val repository: StudyRepository) : ViewModel() {

    val courses: StateFlow<List<CourseEntity>> = repository.courses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val documents: StateFlow<List<DocumentEntity>> = repository.documents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizzes: StateFlow<List<QuizEntity>> = repository.quizzes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizAttempts: StateFlow<List<QuizAttemptEntity>> = repository.attempts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val studySessions: StateFlow<List<StudySessionEntity>> = repository.studySessions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val flashcards: StateFlow<List<FlashcardEntity>> = repository.flashcards
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userProfile: StateFlow<UserProfileEntity?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Current selections
    private val _selectedDocument = MutableStateFlow<DocumentEntity?>(null)
    val selectedDocument: StateFlow<DocumentEntity?> = _selectedDocument.asStateFlow()

    private val _selectedCourseId = MutableStateFlow<Long?>(null)
    val selectedCourseId: StateFlow<Long?> = _selectedCourseId.asStateFlow()

    // Notes Q&A Chat
    val chatMessages: StateFlow<List<ChatMessageEntity>> = _selectedDocument
        .flatMapLatest { doc ->
            repository.getChatMessages(doc?.id, _selectedCourseId.value)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isAiThinking = MutableStateFlow(false)
    val isAiThinking: StateFlow<Boolean> = _isAiThinking.asStateFlow()

    private val _userMessageInput = MutableStateFlow("")
    val userMessageInput: StateFlow<String> = _userMessageInput.asStateFlow()

    // Coding Tutor State
    private val _codingTutorState = MutableStateFlow(CodingTutorUiState())
    val codingTutorState: StateFlow<CodingTutorUiState> = _codingTutorState.asStateFlow()

    // Active Quiz Session
    private val _activeQuiz = MutableStateFlow<ActiveQuizState?>(null)
    val activeQuiz: StateFlow<ActiveQuizState?> = _activeQuiz.asStateFlow()

    // Active Flashcard Index for Review Session
    private val _activeCardIndex = MutableStateFlow(0)
    val activeCardIndex: StateFlow<Int> = _activeCardIndex.asStateFlow()

    private val _isCardFlipped = MutableStateFlow(false)
    val isCardFlipped: StateFlow<Boolean> = _isCardFlipped.asStateFlow()

    // Toast / Feedback message
    private val _snackMessage = MutableStateFlow<String?>(null)
    val snackMessage: StateFlow<String?> = _snackMessage.asStateFlow()

    init {
        // Automatically select first document once loaded
        viewModelScope.launch {
            documents.filter { it.isNotEmpty() }.first().let { docs ->
                if (_selectedDocument.value == null) {
                    _selectedDocument.value = docs.firstOrNull()
                }
            }
        }
    }

    fun clearSnackMessage() {
        _snackMessage.value = null
    }

    fun selectDocument(doc: DocumentEntity) {
        _selectedDocument.value = doc
    }

    fun selectCourseFilter(courseId: Long?) {
        _selectedCourseId.value = courseId
    }

    fun updateMessageInput(input: String) {
        _userMessageInput.value = input
    }

    fun askNoteQuestion(customQuestion: String? = null) {
        val question = customQuestion ?: _userMessageInput.value.trim()
        if (question.isBlank()) return

        val doc = _selectedDocument.value
        val course = courses.value.find { it.id == doc?.courseId }
        val courseName = course?.name ?: "General Academic"
        val docTitle = doc?.title ?: "Academic Notes"
        val docContent = doc?.content ?: "Lecture notes"

        _userMessageInput.value = ""
        _isAiThinking.value = true

        viewModelScope.launch {
            try {
                repository.sendQuestion(
                    question = question,
                    documentId = doc?.id ?: 1L,
                    courseId = doc?.courseId ?: 1L,
                    documentTitle = docTitle,
                    documentContent = docContent,
                    courseName = courseName
                )
            } catch (e: Exception) {
                _snackMessage.value = "Error generating response: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    // Courses Actions
    fun createCourse(name: String, code: String, colorHex: String, semester: String, instructor: String, credits: Int) {
        viewModelScope.launch {
            repository.addCourse(
                CourseEntity(
                    name = name,
                    code = code,
                    colorHex = colorHex,
                    semester = semester,
                    instructor = instructor,
                    creditHours = credits
                )
            )
            _snackMessage.value = "Course '$name' created successfully!"
        }
    }

    fun deleteCourse(course: CourseEntity) {
        viewModelScope.launch {
            repository.deleteCourse(course)
            _snackMessage.value = "Course removed."
        }
    }

    // Notes Actions
    fun uploadNote(courseId: Long, title: String, content: String, fileType: String = "TXT") {
        viewModelScope.launch {
            _isAiThinking.value = true
            val firstLine = content.lines().firstOrNull { it.isNotBlank() } ?: "Lecture Notes"
            val summary = "Study material covering ${title.removeSuffix(".pdf").removeSuffix(".docx")}: $firstLine"

            val id = repository.addDocument(
                DocumentEntity(
                    courseId = courseId,
                    title = title,
                    fileType = fileType,
                    fileSize = "${(content.length / 1024).coerceAtLeast(1)} KB",
                    content = content,
                    summary = summary
                )
            )
            val newDoc = repository.getDocument(id)
            if (newDoc != null) {
                _selectedDocument.value = newDoc
            }
            _isAiThinking.value = false
            _snackMessage.value = "Document '$title' uploaded and indexed!"
        }
    }

    fun deleteDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            repository.deleteDocument(doc)
            if (_selectedDocument.value?.id == doc.id) {
                _selectedDocument.value = documents.value.firstOrNull { it.id != doc.id }
            }
            _snackMessage.value = "Document deleted."
        }
    }

    // Quizzes Actions
    fun generateQuizForDocument(doc: DocumentEntity, difficulty: String, count: Int) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val course = courses.value.find { it.id == doc.courseId }
            try {
                val quizId = repository.createAiQuiz(
                    courseId = doc.courseId,
                    courseName = course?.name ?: "Course",
                    documentTitle = doc.title,
                    content = doc.content,
                    difficulty = difficulty,
                    count = count
                )
                val newQuiz = repository.getQuiz(quizId)
                if (newQuiz != null) {
                    startQuiz(newQuiz)
                }
                _snackMessage.value = "AI Quiz generated with $count questions!"
            } catch (e: Exception) {
                _snackMessage.value = "Failed to generate quiz: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun startQuiz(quiz: QuizEntity) {
        val questions = parseQuestionsJson(quiz.questionsJson)
        _activeQuiz.value = ActiveQuizState(
            quiz = quiz,
            questions = questions,
            currentQuestionIndex = 0,
            userAnswers = emptyMap(),
            isSubmitted = false,
            score = 0,
            showExplanation = false
        )
    }

    fun selectQuizAnswer(optionIndex: Int) {
        val current = _activeQuiz.value ?: return
        if (current.isSubmitted) return

        val newAnswers = current.userAnswers.toMutableMap()
        newAnswers[current.currentQuestionIndex] = optionIndex
        _activeQuiz.value = current.copy(
            userAnswers = newAnswers,
            showExplanation = true
        )
    }

    fun nextQuizQuestion() {
        val current = _activeQuiz.value ?: return
        if (current.currentQuestionIndex < current.questions.size - 1) {
            _activeQuiz.value = current.copy(
                currentQuestionIndex = current.currentQuestionIndex + 1,
                showExplanation = current.userAnswers.containsKey(current.currentQuestionIndex + 1)
            )
        } else {
            // Finish Quiz
            finishQuiz()
        }
    }

    fun previousQuizQuestion() {
        val current = _activeQuiz.value ?: return
        if (current.currentQuestionIndex > 0) {
            _activeQuiz.value = current.copy(
                currentQuestionIndex = current.currentQuestionIndex - 1,
                showExplanation = current.userAnswers.containsKey(current.currentQuestionIndex - 1)
            )
        }
    }

    private fun finishQuiz() {
        val current = _activeQuiz.value ?: return
        var correctCount = 0
        for ((idx, q) in current.questions.withIndex()) {
            val selected = current.userAnswers[idx]
            if (selected == q.correctAnswerIndex) {
                correctCount++
            }
        }

        _activeQuiz.value = current.copy(
            isSubmitted = true,
            score = correctCount,
            showExplanation = true
        )

        viewModelScope.launch {
            repository.recordQuizAttempt(
                quizId = current.quiz.id,
                courseId = current.quiz.courseId,
                title = current.quiz.title,
                score = correctCount,
                total = current.questions.size
            )
        }
    }

    fun exitQuiz() {
        _activeQuiz.value = null
    }

    private fun parseQuestionsJson(json: String): List<QuizQuestionModel> {
        val list = mutableListOf<QuizQuestionModel>()
        try {
            val arr = JSONArray(json)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val opts = mutableListOf<String>()
                val optsArr = obj.getJSONArray("options")
                for (j in 0 until optsArr.length()) opts.add(optsArr.getString(j))

                list.add(
                    QuizQuestionModel(
                        question = obj.getString("question"),
                        options = opts,
                        correctAnswerIndex = obj.getInt("correctAnswerIndex"),
                        explanation = obj.getString("explanation"),
                        sourceCitation = obj.optString("sourceCitation", "Lecture Reference")
                    )
                )
            }
        } catch (e: Exception) {
            // fallback single question
            list.add(
                QuizQuestionModel(
                    question = "What is the key takeaway of this topic?",
                    options = listOf("Concept Mastery", "Memorization", "No impact", "Random"),
                    correctAnswerIndex = 0,
                    explanation = "Understanding core theoretical definitions ensures mastery.",
                    sourceCitation = "Course notes"
                )
            )
        }
        return list
    }

    // Coding Tutor
    fun setCodingLanguage(lang: String) {
        _codingTutorState.value = _codingTutorState.value.copy(language = lang)
    }

    fun updateCodingInput(code: String) {
        _codingTutorState.value = _codingTutorState.value.copy(inputCode = code)
    }

    fun runCodingTutor(mode: CodeTutorMode) {
        val current = _codingTutorState.value
        _codingTutorState.value = current.copy(isLoading = true, mode = mode, revealedHintIndex = 0)

        viewModelScope.launch {
            try {
                val response = AcademicAiService.getProgrammingTutorHelp(
                    language = current.language,
                    problemOrCode = current.inputCode,
                    mode = mode
                )
                _codingTutorState.value = _codingTutorState.value.copy(
                    isLoading = false,
                    response = response
                )
            } catch (e: Exception) {
                _codingTutorState.value = _codingTutorState.value.copy(isLoading = false)
                _snackMessage.value = "Tutor analysis error: ${e.message}"
            }
        }
    }

    fun revealNextHint() {
        val current = _codingTutorState.value
        val maxHints = current.response?.hints?.size ?: 0
        if (current.revealedHintIndex < maxHints) {
            _codingTutorState.value = current.copy(revealedHintIndex = current.revealedHintIndex + 1)
        }
    }

    // Study Planner Actions
    fun toggleSessionComplete(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.updateSessionCompletion(session.id, !session.completed)
            _snackMessage.value = if (!session.completed) "Session marked completed (+45 mins study time)!" else "Session uncompleted."
        }
    }

    fun addStudySession(courseName: String, title: String, date: String, slot: String, duration: Int, priority: String, notes: String) {
        viewModelScope.launch {
            val course = courses.value.find { it.name.contains(courseName, ignoreCase = true) }
            val courseId = course?.id ?: 1L
            repository.addStudySession(
                StudySessionEntity(
                    courseId = courseId,
                    courseName = courseName,
                    title = title,
                    targetDate = date,
                    timeSlot = slot,
                    durationMinutes = duration,
                    priority = priority,
                    notes = notes
                )
            )
            _snackMessage.value = "Study session added to schedule!"
        }
    }

    fun generateAiWeeklySchedule(dailyHours: Int, examGoals: String) {
        viewModelScope.launch {
            _isAiThinking.value = true
            val courseNames = courses.value.map { it.name }
            val courseMap = courses.value.associate { it.name to it.id }
            try {
                repository.generateAndSaveSchedule(courseNames, dailyHours, examGoals, courseMap)
                _snackMessage.value = "AI generated 7-day personalized study schedule!"
            } catch (e: Exception) {
                _snackMessage.value = "Schedule generation failed: ${e.message}"
            } finally {
                _isAiThinking.value = false
            }
        }
    }

    fun deleteSession(session: StudySessionEntity) {
        viewModelScope.launch {
            repository.deleteStudySession(session)
            _snackMessage.value = "Session removed."
        }
    }

    // Flashcards Actions
    fun flipCard() {
        _isCardFlipped.value = !_isCardFlipped.value
    }

    fun rateFlashcard(card: FlashcardEntity, rating: Int) {
        viewModelScope.launch {
            repository.reviewFlashcard(card, rating)
            _isCardFlipped.value = false
            val currentIdx = _activeCardIndex.value
            if (currentIdx < flashcards.value.size - 1) {
                _activeCardIndex.value = currentIdx + 1
            } else {
                _activeCardIndex.value = 0
                _snackMessage.value = "Review session completed! Great job!"
            }
        }
    }

    fun addFlashcard(courseId: Long, courseName: String, front: String, back: String, hint: String, category: String) {
        viewModelScope.launch {
            repository.addFlashcard(
                FlashcardEntity(
                    courseId = courseId,
                    courseName = courseName,
                    front = front,
                    back = back,
                    hint = hint,
                    category = category
                )
            )
            _snackMessage.value = "Flashcard created!"
        }
    }

    fun deleteFlashcard(card: FlashcardEntity) {
        viewModelScope.launch {
            repository.deleteFlashcard(card)
            _snackMessage.value = "Flashcard deleted."
        }
    }
}
