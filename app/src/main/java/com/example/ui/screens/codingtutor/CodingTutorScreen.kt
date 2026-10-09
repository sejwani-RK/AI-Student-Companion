package com.example.ui.screens.codingtutor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.remote.CodeTutorMode
import com.example.ui.components.AcademicStatusChip
import com.example.ui.components.CodeSnippetView
import com.example.ui.theme.*
import com.example.ui.viewmodel.CodingTutorUiState

@Composable
fun CodingTutorScreen(
    state: CodingTutorUiState,
    onLanguageChange: (String) -> Unit,
    onInputChange: (String) -> Unit,
    onRunTutor: (CodeTutorMode) -> Unit,
    onRevealNextHint: () -> Unit
) {
    val languages = listOf("Python", "Java", "JavaScript", "Kotlin", "C++")
    val presets = listOf(
        "Two Sum (Array Hash Map)" to "def two_sum(nums, target):\n    # Return indices of the two numbers such that they add up to target\n    pass",
        "Binary Search (Sorted Array)" to "def binary_search(nums, target):\n    # Return index of target in sorted array, or -1\n    pass",
        "Valid Parentheses (Stack)" to "def is_valid(s):\n    # Check if string of brackets '()[]{}' is valid\n    pass",
        "Reverse Linked List" to "class ListNode:\n    def __init__(self, val=0, next=None):\n        self.val = val\n        self.next = next"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AcademicSlateDark,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AcademicCyan)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Terminal,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Step-by-Step Programming Tutor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Deconstruct algorithms, get progressive hints, and master complexities.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.LightGray,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Language & Starter Problem Selector
        item {
            Text(
                text = "Target Language:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(languages) { lang ->
                    FilterChip(
                        selected = state.language == lang,
                        onClick = { onLanguageChange(lang) },
                        label = { Text(lang) },
                        modifier = Modifier.testTag("lang_chip_$lang")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Common CS Problem Presets:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(presets) { (title, code) ->
                    SuggestionChip(
                        onClick = { onInputChange(code) },
                        label = { Text(title, fontSize = 12.sp) }
                    )
                }
            }
        }

        // Code Editor Input
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Paste Code or Problem Statement:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = state.inputCode,
                        onValueChange = onInputChange,
                        minLines = 4,
                        maxLines = 8,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("coding_tutor_input")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Action Mode Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onRunTutor(CodeTutorMode.EXPLAIN) },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tutor_explain_button")
                        ) {
                            Text("Explain Steps", maxLines = 1, fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = { onRunTutor(CodeTutorMode.HINTS) },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tutor_hints_button")
                        ) {
                            Text("Get Hints", maxLines = 1, fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = { onRunTutor(CodeTutorMode.FIX_BUGS) },
                            enabled = !state.isLoading,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("tutor_bugs_button")
                        ) {
                            Text("Fix Bugs", maxLines = 1, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Loading Indicator
        if (state.isLoading) {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Analyzing algorithmic structures and verifying complexities...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        // Response Presentation
        val resp = state.response
        if (resp != null && !state.isLoading) {
            // Overview & Complexities
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = resp.title,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = resp.overview,
                            style = MaterialTheme.typography.bodyMedium,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AcademicStatusChip(
                                text = "Time: ${resp.timeComplexity}",
                                color = AcademicCyan,
                                icon = Icons.Default.Timer
                            )
                            AcademicStatusChip(
                                text = "Space: ${resp.spaceComplexity}",
                                color = AcademicPurple,
                                icon = Icons.Default.Memory
                            )
                        }
                    }
                }
            }

            // Step-by-Step Breakdown
            if (resp.steps.isNotEmpty()) {
                item {
                    Text(
                        text = "Step-by-Step Algorithmic Logic:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        resp.steps.forEachIndexed { idx, step ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(10.dp))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "${idx + 1}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = step,
                                        style = MaterialTheme.typography.bodyMedium,
                                        lineHeight = 20.sp,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Progressive Hints System
            if (resp.hints.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Progressive Hints (${state.revealedHintIndex} / ${resp.hints.size} Revealed):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        if (state.revealedHintIndex < resp.hints.size) {
                            TextButton(
                                onClick = onRevealNextHint,
                                modifier = Modifier.testTag("reveal_hint_button")
                            ) {
                                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Next Hint")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (state.revealedHintIndex == 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AcademicAmber.copy(alpha = 0.1f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(imageVector = Icons.Default.Lightbulb, contentDescription = null, tint = AcademicAmber)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Tap 'Next Hint' above to reveal guidance one step at a time.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            for (i in 0 until state.revealedHintIndex) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = AcademicAmber.copy(alpha = 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AcademicAmber.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = AcademicAmber, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = resp.hints[i], style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Solution Code Snippet
            if (resp.codeSnippet.isNotBlank()) {
                item {
                    Text(
                        text = "Idiomatic Solution Code:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CodeSnippetView(
                        code = resp.codeSnippet,
                        language = state.language
                    )
                }
            }

            // Common Pitfalls / Mistakes
            if (resp.commonMistakes.isNotEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = AcademicRose.copy(alpha = 0.08f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, AcademicRose.copy(alpha = 0.25f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AcademicRose,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Common University Mistakes to Avoid:",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = AcademicRose
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            resp.commonMistakes.forEach { mistake ->
                                Text(
                                    text = "• $mistake",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
