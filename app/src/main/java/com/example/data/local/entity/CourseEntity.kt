package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class CourseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val code: String,
    val colorHex: String,
    val semester: String,
    val instructor: String,
    val creditHours: Int = 3,
    val createdAt: Long = System.currentTimeMillis()
)
