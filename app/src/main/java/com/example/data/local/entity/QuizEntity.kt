package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "quizzes")
data class QuizEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val title: String,
    val difficulty: String, // Easy, Medium, Hard
    val questionsJson: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_attempts")
data class QuizAttemptEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val quizId: Long,
    val courseId: Long,
    val quizTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val percentage: Int,
    val completedAt: Long = System.currentTimeMillis()
)
