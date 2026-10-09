package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class FlashcardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val courseName: String,
    val front: String,
    val back: String,
    val hint: String = "",
    val category: String = "General",
    val intervalDays: Int = 1,
    val repetitionCount: Int = 0,
    val easeFactor: Float = 2.5f,
    val nextReviewDate: Long = System.currentTimeMillis(),
    val masteryLevel: String = "New" // New, Learning, Reviewing, Mastered
)

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Long = 1,
    val displayName: String = "Alex Rivera",
    val email: String = "alex.rivera@university.edu",
    val university: String = "State University of Technology",
    val major: String = "Computer Science & Engineering",
    val semester: String = "Fall 2026",
    val dailyGoalMinutes: Int = 120,
    val streakDays: Int = 5,
    val totalStudyMinutes: Int = 1450,
    val isGoogleSignedIn: Boolean = true
)
