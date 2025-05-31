package com.example.languageflashcards

object QuizLogic {

    fun generateShuffledAnswers(
        correctAnswer: String,
        allAnswers: List<String>,
        numberOfOptions: Int = 3
    ): List<String> {
        val wrongAnswers = allAnswers.filter { it != correctAnswer }.shuffled().take(numberOfOptions - 1).toMutableList()
        val insertIndex = (0..wrongAnswers.size).random()
        wrongAnswers.add(insertIndex, correctAnswer)
        return wrongAnswers
    }
}
