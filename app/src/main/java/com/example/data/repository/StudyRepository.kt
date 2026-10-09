package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.entity.*
import com.example.data.remote.*
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class StudyRepository(private val database: AppDatabase) {
    private val courseDao = database.courseDao()
    private val documentDao = database.documentDao()
    private val quizDao = database.quizDao()
    private val studySessionDao = database.studySessionDao()
    private val flashcardDao = database.flashcardDao()
    private val userProfileDao = database.userProfileDao()

    // Courses
    val courses: Flow<List<CourseEntity>> = courseDao.getAllCourses()
    suspend fun getCourse(id: Long) = courseDao.getCourseById(id)
    suspend fun addCourse(course: CourseEntity) = courseDao.insertCourse(course)
    suspend fun updateCourse(course: CourseEntity) = courseDao.updateCourse(course)
    suspend fun deleteCourse(course: CourseEntity) = courseDao.deleteCourse(course)

    // Documents & Notes
    val documents: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
    fun getDocumentsForCourse(courseId: Long): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsForCourse(courseId)

    suspend fun getDocument(id: Long) = documentDao.getDocumentById(id)
    suspend fun addDocument(doc: DocumentEntity) = documentDao.insertDocument(doc)
    suspend fun deleteDocument(doc: DocumentEntity) = documentDao.deleteDocument(doc)

    // Chat / Q&A
    fun getChatMessages(docId: Long?, courseId: Long?): Flow<List<ChatMessageEntity>> =
        documentDao.getMessagesForDocOrCourse(docId, courseId)

    val allChatMessages: Flow<List<ChatMessageEntity>> = documentDao.getAllMessages()

    suspend fun sendQuestion(
        question: String,
        documentId: Long,
        courseId: Long,
        documentTitle: String,
        documentContent: String,
        courseName: String
    ): ChatMessageEntity {
        // Save user message
        val userMsg = ChatMessageEntity(
            documentId = documentId,
            courseId = courseId,
            role = "user",
            content = question,
            timestamp = System.currentTimeMillis()
        )
        documentDao.insertChatMessage(userMsg)

        // Generate Grounded Answer
        val aiResponse = AcademicAiService.answerNotesQuestion(
            question = question,
            documentTitle = documentTitle,
            documentContent = documentContent,
            courseName = courseName
        )

        // Save AI message with citation
        val aiMsg = ChatMessageEntity(
            documentId = documentId,
            courseId = courseId,
            role = "assistant",
            content = aiResponse.answer,
            sourceReference = aiResponse.sourceCitation,
            timestamp = System.currentTimeMillis()
        )
        val insertedId = documentDao.insertChatMessage(aiMsg)
        return aiMsg.copy(id = insertedId)
    }

    // Quizzes
    val quizzes: Flow<List<QuizEntity>> = quizDao.getAllQuizzes()
    val attempts: Flow<List<QuizAttemptEntity>> = quizDao.getAllAttempts()

    suspend fun getQuiz(id: Long) = quizDao.getQuizById(id)

    suspend fun createAiQuiz(
        courseId: Long,
        courseName: String,
        documentTitle: String,
        content: String,
        difficulty: String,
        count: Int
    ): Long {
        val questions = AcademicAiService.generateQuizQuestions(
            topic = "$courseName: $documentTitle",
            content = content,
            difficulty = difficulty,
            questionCount = count
        )

        val questionsJson = JSONArray().apply {
            for (q in questions) {
                val obj = JSONObject().apply {
                    put("question", q.question)
                    val optsArr = JSONArray()
                    for (opt in q.options) optsArr.put(opt)
                    put("options", optsArr)
                    put("correctAnswerIndex", q.correctAnswerIndex)
                    put("explanation", q.explanation)
                    put("sourceCitation", q.sourceCitation)
                }
                put(obj)
            }
        }.toString()

        val quiz = QuizEntity(
            courseId = courseId,
            title = "$documentTitle - $difficulty Quiz",
            difficulty = difficulty,
            questionsJson = questionsJson
        )
        return quizDao.insertQuiz(quiz)
    }

    suspend fun recordQuizAttempt(
        quizId: Long,
        courseId: Long,
        title: String,
        score: Int,
        total: Int
    ): Long {
        val percentage = if (total > 0) (score * 100) / total else 0
        val attempt = QuizAttemptEntity(
            quizId = quizId,
            courseId = courseId,
            quizTitle = title,
            score = score,
            totalQuestions = total,
            percentage = percentage,
            completedAt = System.currentTimeMillis()
        )
        // Award study time
        userProfileDao.addStudyTime(minutes = total * 3)
        return quizDao.insertAttempt(attempt)
    }

    // Study Sessions
    val studySessions: Flow<List<StudySessionEntity>> = studySessionDao.getAllSessions()
    suspend fun addStudySession(session: StudySessionEntity) =
        studySessionDao.insertSession(session)

    suspend fun updateSessionCompletion(id: Long, completed: Boolean) {
        studySessionDao.setCompleted(id, completed)
        if (completed) {
            userProfileDao.addStudyTime(minutes = 45, streakBonus = 0)
        }
    }

    suspend fun deleteStudySession(session: StudySessionEntity) =
        studySessionDao.deleteSession(session)

    suspend fun generateAndSaveSchedule(
        subjects: List<String>,
        dailyHours: Int,
        priorityNotes: String,
        courseMap: Map<String, Long>
    ) {
        val generated = AcademicAiService.generateStudySchedule(subjects, dailyHours, priorityNotes)
        val entities = generated.map { g ->
            val courseId = courseMap[g.courseName] ?: 1L
            StudySessionEntity(
                courseId = courseId,
                courseName = g.courseName,
                title = g.title,
                targetDate = g.targetDate,
                timeSlot = g.timeSlot,
                durationMinutes = g.durationMinutes,
                priority = g.priority,
                completed = false,
                notes = g.notes
            )
        }
        studySessionDao.insertAll(entities)
    }

    // Flashcards & Spaced Repetition (SM-2)
    val flashcards: Flow<List<FlashcardEntity>> = flashcardDao.getAllFlashcards()

    suspend fun addFlashcard(card: FlashcardEntity) = flashcardDao.insertFlashcard(card)
    suspend fun deleteFlashcard(card: FlashcardEntity) = flashcardDao.deleteFlashcard(card)

    // SM-2 Spaced Repetition Rating: Again (1), Hard (2), Good (3), Easy (4)
    suspend fun reviewFlashcard(card: FlashcardEntity, rating: Int) {
        val newRepetition: Int
        val newInterval: Int
        var newEase = card.easeFactor

        // SuperMemo SM-2 algorithm implementation
        when (rating) {
            1 -> { // Again
                newRepetition = 0
                newInterval = 1
                newEase = (card.easeFactor - 0.2f).coerceAtLeast(1.3f)
            }
            2 -> { // Hard
                newRepetition = card.repetitionCount + 1
                newInterval = (card.intervalDays * 1.2f).toInt().coerceAtLeast(2)
                newEase = (card.easeFactor - 0.15f).coerceAtLeast(1.3f)
            }
            3 -> { // Good
                newRepetition = card.repetitionCount + 1
                newInterval = if (card.repetitionCount == 0) 1 else if (card.repetitionCount == 1) 4 else (card.intervalDays * card.easeFactor).toInt()
                // Ease unchanged
            }
            else -> { // Easy (4)
                newRepetition = card.repetitionCount + 1
                newInterval = if (card.repetitionCount == 0) 2 else if (card.repetitionCount == 1) 6 else (card.intervalDays * card.easeFactor * 1.3f).toInt()
                newEase = (card.easeFactor + 0.15f).coerceAtMost(3.0f)
            }
        }

        val mastery = when {
            newRepetition >= 4 -> "Mastered"
            newRepetition >= 2 -> "Reviewing"
            newRepetition >= 1 -> "Learning"
            else -> "New"
        }

        val updatedCard = card.copy(
            intervalDays = newInterval,
            repetitionCount = newRepetition,
            easeFactor = newEase,
            nextReviewDate = System.currentTimeMillis() + (newInterval * 86400000L),
            masteryLevel = mastery
        )
        flashcardDao.updateFlashcard(updatedCard)
        userProfileDao.addStudyTime(minutes = 2)
    }

    // User Profile
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getProfile()
    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdate(profile)
}
