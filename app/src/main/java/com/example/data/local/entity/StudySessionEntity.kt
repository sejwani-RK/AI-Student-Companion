package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "study_sessions")
data class StudySessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val courseName: String,
    val title: String,
    val targetDate: String, // e.g., "2026-10-10"
    val timeSlot: String,   // e.g., "14:00 - 15:30"
    val durationMinutes: Int,
    val priority: String,   // High, Medium, Low
    val completed: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
