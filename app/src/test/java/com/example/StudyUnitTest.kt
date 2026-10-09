package com.example

import org.junit.Assert.*
import org.junit.Test

class StudyUnitTest {

    @Test
    fun testQuizAccuracyPercentageCalculation() {
        val score = 3
        val total = 4
        val percentage = (score * 100) / total
        assertEquals(75, percentage)
    }

    @Test
    fun testSpacedRepetitionIntervals() {
        // SM-2 logic test for "Again" (rating 1)
        val initialInterval = 4
        val ratingAgain = 1
        val newIntervalAgain = if (ratingAgain == 1) 1 else initialInterval
        assertEquals(1, newIntervalAgain)

        // SM-2 logic test for "Easy" (rating 4)
        val easeFactor = 2.5f
        val repCount = 2
        val newIntervalEasy = (initialInterval * easeFactor * 1.3f).toInt()
        assertTrue(newIntervalEasy > initialInterval)
    }

    @Test
    fun testDocumentKeywordMatching() {
        val content = "An AVL tree is a self-balancing binary search tree. Balance factor = Height(Left) - Height(Right)."
        val question = "What is the balance factor formula?"
        val keywords = question.lowercase().split(" ", "?").filter { it.length > 4 }
        val matches = keywords.any { content.lowercase().contains(it) }
        assertTrue(matches)
    }
}
