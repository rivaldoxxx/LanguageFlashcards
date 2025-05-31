package com.example.languageflashcards

import org.junit.Assert.*
import org.junit.Test

class QuizLogicTest {

    private val allWords = listOf("cat", "dog", "bird", "snake", "lion")

    @Test
    fun `generateShuffledAnswers always contains correct answer`() {
        val correct = "dog"
        repeat(10) {
            val answers = QuizLogic.generateShuffledAnswers(correct, allWords)
            assertTrue(answers.contains(correct))
        }
    }

    @Test
    fun `generateShuffledAnswers returns exactly 3 options`() {
        val correct = "bird"
        val answers = QuizLogic.generateShuffledAnswers(correct, allWords)
        assertEquals(3, answers.size)
    }

    @Test
    fun `generateShuffledAnswers does not duplicate correct answer`() {
        val correct = "cat"
        val answers = QuizLogic.generateShuffledAnswers(correct, allWords)
        val countCorrect = answers.count { it == correct }
        assertEquals(1, countCorrect)
    }

    @Test
    fun `generateShuffledAnswers returns randomized order`() {
        val correct = "snake"
        val setOfOrders = mutableSetOf<List<String>>()

        repeat(20) {
            val order = QuizLogic.generateShuffledAnswers(correct, allWords)
            setOfOrders.add(order)
        }

        assertTrue(setOfOrders.size > 1) // kilka różnych kombinacji
    }
}
