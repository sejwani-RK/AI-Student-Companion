package com.example.ui.screens.notes

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
import com.example.data.local.entity.ChatMessageEntity
import com.example.data.local.entity.CourseEntity
import com.example.data.local.entity.DocumentEntity
import com.example.ui.components.AcademicStatusChip
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesAssistantScreen(
    courses: List<CourseEntity>,
    documents: List<DocumentEntity>,
    selectedDocument: DocumentEntity?,
    chatMessages: List<ChatMessageEntity>,
    isAiThinking: Boolean,
    messageInput: String,
    onDocumentSelect: (DocumentEntity) -> Unit,
    onMessageInputChange: (String) -> Unit,
    onSendMessage: (String?) -> Unit,
    onUploadNote: (courseId: Long, title: String, content: String, fileType: String) -> Unit,
    onGenerateQuizFromDoc: (DocumentEntity) -> Unit,
    onDeleteDocument: (DocumentEntity) -> Unit
) {
    var showUploadDialog by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Chat Q&A, 1: Document Text
    var showDocDetailsSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Document Selector Strip
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Lecture Notes (${documents.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FilledTonalButton(
                        onClick = { showUploadDialog = true },
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("upload_note_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UploadFile,
                            contentDescription = "Upload Note",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Note", style = MaterialTheme.typography.labelMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Horizontal list of uploaded documents
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(documents) { doc ->
                        val isSelected = doc.id == selectedDocument?.id
                        val badgeColor = when (doc.fileType.uppercase()) {
                            "PDF" -> AcademicRose
                            "DOCX" -> AcademicBlueLight
                            else -> AcademicCyan
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .clickable { onDocumentSelect(doc) }
                                .testTag("doc_item_${doc.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = badgeColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = doc.fileType,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = badgeColor,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = doc.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.widthIn(max = 160.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

        // Selected Document Header & View Tabs
        if (selectedDocument != null) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedDocument.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = selectedDocument.summary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        IconButton(
                            onClick = { onGenerateQuizFromDoc(selectedDocument) },
                            modifier = Modifier.testTag("gen_quiz_from_doc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Quiz,
                                contentDescription = "Generate Quiz from this Document",
                                tint = AcademicEmerald
                            )
                        }

                        IconButton(
                            onClick = { onDeleteDocument(selectedDocument) },
                            modifier = Modifier.testTag("delete_doc_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Document",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Mode Toggle: Q&A Chat vs Full Document
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.Transparent,
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("AI Notes Q&A") }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("View Extracted Notes") }
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

            if (selectedTab == 0) {
                // CHAT Q&A VIEW
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        contentPadding = PaddingValues(top = 12.dp, bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Suggested questions chips
                        item {
                            Text(
                                text = "Suggested Questions (Grounded):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val suggestions = when {
                                    selectedDocument.title.contains("AVL", ignoreCase = true) -> listOf(
                                        "When is a double LR rotation needed?",
                                        "What is the maximum balance factor allowed?",
                                        "Compare AVL search speed to Red-Black trees."
                                    )
                                    selectedDocument.title.contains("A*", ignoreCase = true) -> listOf(
                                        "What makes a heuristic admissible?",
                                        "Explain f(n) = g(n) + h(n)",
                                        "Manhattan vs Euclidean distance differences?"
                                    )
                                    else -> listOf(
                                        "Summarize key definitions from these notes",
                                        "What are the main formulas to memorize?",
                                        "Explain core algorithmic complexities"
                                    )
                                }
                                items(suggestions) { question ->
                                    SuggestionChip(
                                        onClick = { onSendMessage(question) },
                                        label = { Text(question, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }

                        // Chat Messages
                        items(chatMessages) { msg ->
                            ChatMessageCard(message = msg)
                        }

                        if (isAiThinking) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "Analyzing lecture notes & verifying citations...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Chat Input Bar
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = messageInput,
                            onValueChange = onMessageInputChange,
                            placeholder = { Text("Ask any question from notes...") },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("notes_chat_input"),
                            shape = RoundedCornerShape(24.dp),
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        FilledIconButton(
                            onClick = { onSendMessage(null) },
                            enabled = messageInput.isNotBlank() && !isAiThinking,
                            modifier = Modifier.testTag("notes_send_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send Question"
                            )
                        }
                    }
                }
            } else {
                // FULL DOCUMENT TEXT READER
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = selectedDocument.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    AcademicStatusChip(
                                        text = selectedDocument.fileSize,
                                        color = AcademicBlueLight
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = selectedDocument.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp
                                )
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
                Text(
                    text = "No document selected. Add or select notes above to begin Q&A.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Upload / Add Note Dialog
    if (showUploadDialog) {
        UploadNoteDialog(
            courses = courses,
            onDismiss = { showUploadDialog = false },
            onConfirm = { courseId, title, content, type ->
                onUploadNote(courseId, title, content, type)
                showUploadDialog = false
            }
        )
    }
}

@Composable
private fun ChatMessageCard(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .widthIn(max = 330.dp)
                .border(
                    width = if (isUser) 0.dp else 1.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = RoundedCornerShape(16.dp)
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(AcademicBlueLight)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Study Assistant",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = AcademicBlueLight
                        )
                    }
                }

                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                    lineHeight = 21.sp
                )

                // Source Grounding Citation Pill
                if (!message.sourceReference.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = AcademicBlueContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bookmark,
                                contentDescription = null,
                                tint = AcademicBluePrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Source: ${message.sourceReference}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 11.sp,
                                color = AcademicBluePrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UploadNoteDialog(
    courses: List<CourseEntity>,
    onDismiss: () -> Unit,
    onConfirm: (Long, String, String, String) -> Unit
) {
    var selectedCourseId by remember { mutableStateOf(courses.firstOrNull()?.id ?: 1L) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    var selectedFileType by remember { mutableStateOf("PDF") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add & Index Lecture Notes") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Select Course:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )

                // Course selector
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(courses) { course ->
                        FilterChip(
                            selected = selectedCourseId == course.id,
                            onClick = { selectedCourseId = course.id },
                            label = { Text(course.code) }
                        )
                    }
                }

                // File Type Selector
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("PDF", "DOCX", "TXT").forEach { type ->
                        FilterChip(
                            selected = selectedFileType == type,
                            onClick = { selectedFileType = type },
                            label = { Text(type) }
                        )
                    }
                }

                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Document Title (e.g., Lecture 06: Graph Algorithms)") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Document Text / Lecture Notes Content") },
                    placeholder = { Text("Paste extracted lecture slides or notes text here...") },
                    minLines = 4,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalTitle = if (noteTitle.isNotBlank()) noteTitle else "Lecture Notes ($selectedFileType)"
                    val finalContent = if (noteContent.isNotBlank()) noteContent else "Sample lecture note content for $finalTitle"
                    onConfirm(selectedCourseId, finalTitle, finalContent, selectedFileType)
                },
                enabled = noteTitle.isNotBlank() || noteContent.isNotBlank()
            ) {
                Text("Index & Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
