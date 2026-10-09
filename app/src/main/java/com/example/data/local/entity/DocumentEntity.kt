package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val courseId: Long,
    val title: String,
    val fileType: String, // PDF, DOCX, TXT
    val fileSize: String,
    val content: String,
    val summary: String,
    val tags: String = "",
    val uploadedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentId: Long? = null,
    val courseId: Long? = null,
    val role: String, // "user", "assistant"
    val content: String,
    val sourceReference: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
